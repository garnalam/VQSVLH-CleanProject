import java.util.Arrays;

final class VqsvBattleRuntime {
    private VqsvBattleRuntime() {
    }
}

final class SourceBattleRuntime implements Blocking {
    private final int actorId;
    private final int[] encounter;
    private final int[] flags;
    private final int[] battleMode;
    private final int[] branchTargets;
    private final int forcedResultIndex;
    private final boolean sourceBattleSlice;
    private int phase;
    private int wait;
    private SourceBattleUnit enemy;
    private SourceBattleUnit player;
    private int turn;
    private boolean bunnyTutorialShown;
    private boolean captureStarted;

    SourceBattleRuntime(int actorId, int[] encounter, int[] flags, int[] battleMode, int[] branchTargets) {
        this(actorId, encounter, flags, battleMode, branchTargets, 0, false);
}

    SourceBattleRuntime(int actorId, int[] encounter, int[] flags, int[] battleMode,
                               int[] branchTargets, int forcedResultIndex) {
        this(actorId, encounter, flags, battleMode, branchTargets, forcedResultIndex, false);
}

    SourceBattleRuntime(int actorId, int[] encounter, int[] flags, int[] battleMode,
                               int[] branchTargets, int forcedResultIndex, boolean sourceBattleSlice) {
        this.actorId = actorId;
        this.encounter = encounter;
        this.flags = flags;
        this.battleMode = battleMode;
        this.branchTargets = branchTargets;
        this.forcedResultIndex = forcedResultIndex;
        this.sourceBattleSlice = sourceBattleSlice;
}

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        switch (phase) {
            case 0:
                enemy = SourceBattleUnit.enemyFromEncounter(encounter);
                player = SourceBattleUnit.playerFromSourcePets(s.sourcePets);
                if (isKidnappingBattle()) {
                    player = SourceBattleUnit.fallback(-1, 6, 3, "Neil", 120, 22, 12, 10);
                }
                s.worldEventActor = actorId;
                s.battleEventActor = actorId;
                s.battleEncounter = Arrays.copyOf(encounter, encounter.length);
                s.battleCanLose = flags.length > 0 && flags[0] == 0;
                s.battleScriptLocksInput = flags.length > 1 && flags[1] == 0;
                s.battleMode = battleMode.length > 0 ? battleMode[0] : -1;
                s.battleBackgroundMode = battleMode.length > 1 ? battleMode[1] : -1;
                s.battleResultIndex = -2;
                s.battleBranchTarget = resolveBranch(s.battleResultIndex);
                s.battleCaptureTutorial = isBunnyCaptureBattle();
                syncRenderState(s, VqsvText.Battle.START);
                s.sourceStateTrace.add("PORTED/APPROX source battle runtime actor=" + actorId
                        + " encounter=" + Arrays.toString(encounter)
                        + " flags=" + Arrays.toString(flags)
                        + " mode=" + Arrays.toString(battleMode)
                        + " enemy=" + enemy
                        + " player=" + player
                        + " branchTargets=" + Arrays.toString(branchTargets)
                        + "; NOT full game.d command UI/status/effect engine");
                s.effect.startFade(2, 0);
                phase = 1;
                return false;
            case 1:
                if (!s.effect.doneOverlay(s)) {
                    return false;
                }
                s.battleOverlayTicks = 1;
                wait = 18;
                phase = 2;
                return false;
            case 2:
                s.battleOverlayTicks = 1;
                if (wait-- > 0) {
                    return false;
                }
                if (advanceBattle(s)) {
                    wait = 18;
                    phase = 3;
                } else {
                    wait = 22;
                }
                return false;
            case 3:
                s.battleOverlayTicks = 1;
                if (wait-- > 0) {
                    return false;
                }
                s.battleOverlayTicks = 0;
                s.battleCaptureTutorial = false;
                s.effect.startFade(1, 0);
                phase = 4;
                return false;
            case 4:
                return s.effect.doneOverlay(s);
            default:
                return true;
        }
}

    private boolean advanceBattle(VqsvIntroDemo.Scene s) {
        if (isBunnyCaptureBattle()) {
            return advanceBunnyCapture(s);
        }
        if (isKidnappingBattle()) {
            int damage = Math.max(1, enemy.basicDamageTo(player));
            player.damage(damage);
            turn++;
            syncRenderState(s, enemy.name + VqsvText.Battle.DAMAGE + damage + VqsvText.Battle.DAMAGE_SUFFIX);
            if (!player.alive()) {
                s.battleResultIndex = forcedResultIndex;
                s.battleBranchTarget = resolveBranch(s.battleResultIndex);
                s.battleLog = VqsvText.Battle.NEIL_LOST + s.battleResultIndex;
                s.sourceStateTrace.add("PORTED/APPROX kidnapping battle resolved by source stats; resultIndex="
                        + s.battleResultIndex + " branch=" + s.battleBranchTarget);
                return true;
            }
            return false;
        }
        SourceBattleUnit first = player.speed >= enemy.speed ? player : enemy;
        SourceBattleUnit second = first == player ? enemy : player;
        applyAttack(s, first, second);
        if (!second.alive()) {
            return finishNormalBattle(s);
        }
        applyAttack(s, second, first);
        if (!first.alive()) {
            return finishNormalBattle(s);
        }
        turn++;
        return false;
}

    private boolean advanceBunnyCapture(VqsvIntroDemo.Scene s) {
        if (!bunnyTutorialShown) {
            int damage = Math.max(1, player.basicDamageTo(enemy));
            enemy.damage(damage);
            turn++;
            if (enemy.hp > enemy.maxHp / 2) {
                syncRenderState(s, player.name + VqsvText.Battle.DAMAGE + damage + VqsvText.Battle.DAMAGE_SUFFIX);
                return false;
            }
            bunnyTutorialShown = true;
            syncRenderState(s, VqsvText.Battle.BUNNY_WEAK);
            s.sourceStateTrace.add("PORTED/APPROX bunny tutorial source game.d.l(): HP<=50% then prompt capture ball");
            return false;
        }
        if (!captureStarted) {
            captureStarted = true;
            syncRenderState(s, VqsvText.Battle.BALL_CHOSEN);
            return false;
        }
        enemy.hp = 0;
        s.battleResultIndex = forcedResultIndex;
        s.battleBranchTarget = resolveBranch(s.battleResultIndex);
        syncRenderState(s, VqsvText.Battle.BUNNY_CAUGHT + s.battleResultIndex);
        s.sourceStateTrace.add("PORTED/APPROX bunny capture resolved; op47 sees l=-1/continue success path in manual script");
        return true;
}

    private void applyAttack(VqsvIntroDemo.Scene s, SourceBattleUnit attacker, SourceBattleUnit target) {
        int damage = attacker.basicDamageTo(target);
        target.damage(damage);
        syncRenderState(s, attacker.name + VqsvText.Battle.DAMAGE + damage + VqsvText.Battle.DAMAGE_SUFFIX);
}

    private boolean finishNormalBattle(VqsvIntroDemo.Scene s) {
        int result = player.alive() ? 0 : Math.max(0, forcedResultIndex);
        if (!player.alive() && forcedResultIndex == 0 && isElderBattle()) {
            player.hp = 1;
            enemy.hp = 0;
            result = 0;
            syncRenderState(s, VqsvText.Battle.ELDER_DONE);
        }
        s.battleResultIndex = result;
        s.battleBranchTarget = resolveBranch(s.battleResultIndex);
        s.sourceStateTrace.add("PORTED/APPROX battle resolved resultIndex="
                + s.battleResultIndex + " branch=" + s.battleBranchTarget
                + " playerHp=" + player.hp + "/" + player.maxHp
                + " enemyHp=" + enemy.hp + "/" + enemy.maxHp);
        return true;
}

    private void syncRenderState(VqsvIntroDemo.Scene s, String log) {
        s.battleEnemyName = enemy.name;
        s.battleEnemyLevel = enemy.level;
        s.battleEnemyVisualId = enemy.visualId;
        s.battleEnemyElement = enemy.element;
        s.battleEnemyMaxHp = enemy.maxHp;
        s.battleEnemyHp = enemy.hp;
        s.battlePlayerName = player.name;
        s.battlePlayerLevel = player.level;
        s.battlePlayerVisualId = player.visualId;
        s.battlePlayerElement = player.element;
        s.battlePlayerMaxHp = player.maxHp;
        s.battlePlayerHp = player.hp;
        s.battlePlayerEnergy = 0;
        s.battlePlayerMaxEnergy = player.nextLevelEnergy();
        byte relation = player.elementRelationTo(enemy);
        if (relation == 0) {
            s.battlePlayerPowerPercent = 300;
            s.battleEnemyPowerPercent = 60;
        } else if (relation == 1) {
            s.battlePlayerPowerPercent = 60;
            s.battleEnemyPowerPercent = 300;
        } else {
            s.battlePlayerPowerPercent = 100;
            s.battleEnemyPowerPercent = 100;
        }
        s.battleTurn = turn;
        s.battleLog = log;
}

    private boolean isKidnappingBattle() {
        return encounter.length >= 2 && encounter[0] == 5 && encounter[1] == 20;
}

    private boolean isBunnyCaptureBattle() {
        return encounter.length >= 1 && encounter[0] == 34;
}

    private boolean isElderBattle() {
        return encounter.length >= 1 && encounter[0] == 68;
}

    private int resolveBranch(int resultIndex) {
        if (resultIndex < 0) {
            return -1;
        }
        if (branchTargets.length == 0) {
            return -1;
        }
        int index = Math.max(0, resultIndex);
        if (index >= branchTargets.length) {
            index = branchTargets.length - 1;
        }
        return branchTargets[index];
}
}
