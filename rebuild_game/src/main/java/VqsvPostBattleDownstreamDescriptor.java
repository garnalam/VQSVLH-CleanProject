final class VqsvPostBattleDownstreamDescriptor {
    static final VqsvPostBattleDownstreamDescriptor SCENE1_ROOM1_GROUP0_BUNNY =
            new VqsvPostBattleDownstreamDescriptor("scene1 room1 group0 Bunny")
                    .expectOp56(1, 50, 0)
                    .expectOp23(1, 0, 1)
                    .expectOp40(VqsvText.Scene1Room1Group0.TASK_RETURN_ELDER)
                    .expectOp14(1, 1, 0);

    static final VqsvPostBattleDownstreamDescriptor SCENE1_ROOM0_GROUP6_ELDER =
            new VqsvPostBattleDownstreamDescriptor("scene1 room0 group6 Elder")
                    .expectOp31(0, 0, 500)
                    .expectOp17(0, 4, 10)
                    .expectOp17(0, 11, 2)
                    .expectOp19(5, 1)
                    .expectOp23(1, 0, 4)
                    .expectOp23(1, 0, 5)
                    .expectOp45(2, VqsvText.Scene1Room0Group6.TASK_BICH_THUY)
                    .expectOp40(VqsvText.Scene1Room0Group6.FREE_WORLD)
                    .expectOp14(1, 0, 6);

    private final String label;
    private int op56Mode = Integer.MIN_VALUE;
    private int op56ActorId = -1;
    private int op56State;
    private int[][] op23 = new int[0][];
    private int[][] op17 = new int[0][];
    private int[] op19;
    private int[] op31;
    private int[] op14;
    private int op45TaskFlag = Integer.MIN_VALUE;
    private String op45Text;
    private String op40Text;

    private VqsvPostBattleDownstreamDescriptor(String label) {
        this.label = label;
    }

    private VqsvPostBattleDownstreamDescriptor expectOp56(int mode, int actorId, int state) {
        op56Mode = mode;
        op56ActorId = actorId;
        op56State = state;
        return this;
    }

    private VqsvPostBattleDownstreamDescriptor expectOp23(int sceneId, int roomIndex, int groupIndex) {
        op23 = append(op23, new int[]{sceneId, roomIndex, groupIndex});
        return this;
    }

    private VqsvPostBattleDownstreamDescriptor expectOp17(int mode, int itemId, int qty) {
        op17 = append(op17, new int[]{mode, itemId, qty});
        return this;
    }

    private VqsvPostBattleDownstreamDescriptor expectOp19(int rewardId, int qty) {
        op19 = new int[]{rewardId, qty};
        return this;
    }

    private VqsvPostBattleDownstreamDescriptor expectOp31(int mode, int currencyKind, int amount) {
        op31 = new int[]{mode, currencyKind, amount};
        return this;
    }

    private VqsvPostBattleDownstreamDescriptor expectOp45(int taskFlag, String text) {
        op45TaskFlag = taskFlag;
        op45Text = text;
        return this;
    }

    private VqsvPostBattleDownstreamDescriptor expectOp40(String text) {
        op40Text = text;
        return this;
    }

    private VqsvPostBattleDownstreamDescriptor expectOp14(int sceneId, int roomIndex, int groupIndex) {
        op14 = new int[]{sceneId, roomIndex, groupIndex};
        return this;
    }

    void traceAndAssert(VqsvIntroDemo.Scene s) {
        int eventIndex = s.eventIndex;
        assertOp56(s);
        assertOp31(s);
        assertOp17(s);
        assertOp19(s);
        assertOp23(s);
        assertOp14(s);
        traceOp45(s);
        traceOp40(s);
        if (s.eventIndex != eventIndex) {
            throw new IllegalStateException("Post-battle downstream descriptor mutated eventIndex for "
                    + label + " before=" + eventIndex + " after=" + s.eventIndex);
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source PostBattleDownstreamDescriptor " + label
                + " trace/assert complete no eventIndex mutation");
    }

    private void assertOp56(VqsvIntroDemo.Scene s) {
        if (op56Mode == Integer.MIN_VALUE) {
            return;
        }
        if (op56ActorId >= 0 && op56ActorId < s.actors.length && s.actors[op56ActorId] != null) {
            boolean expectedVisible = op56Mode == 0;
            if (s.actors[op56ActorId].visible != expectedVisible) {
                throw new IllegalStateException(label + " op56 actor visibility mismatch actor="
                        + op56ActorId + " expectedVisible=" + expectedVisible
                        + " actual=" + s.actors[op56ActorId].visible);
            }
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source PostBattleDownstreamDescriptor " + label
                + " op56 mode=" + op56Mode
                + " actor=" + op56ActorId
                + " state=" + op56State);
    }

    private void assertOp31(VqsvIntroDemo.Scene s) {
        if (op31 == null) {
            return;
        }
        if (op31[0] == 0 && op31[1] == 0 && s.sourceMoney < op31[2]) {
            throw new IllegalStateException(label + " op31 money missing expectedAtLeast="
                    + op31[2] + " actual=" + s.sourceMoney);
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source PostBattleDownstreamDescriptor " + label
                + " op31 [" + op31[0] + "," + op31[1] + "," + op31[2] + "]"
                + " money=" + s.sourceMoney + " badges=" + s.sourceBadges);
    }

    private void assertOp17(VqsvIntroDemo.Scene s) {
        for (int[] row : op17) {
            int itemId = row[1];
            int qty = row[2];
            if (row[0] == 0 && VqsvSourceOps.sourceItemCount(s, itemId) < qty) {
                throw new IllegalStateException(label + " op17 item missing item="
                        + itemId + " expectedAtLeast=" + qty
                        + " actual=" + VqsvSourceOps.sourceItemCount(s, itemId));
            }
            s.sourceStateTrace.add("PORTED/PARTIAL source PostBattleDownstreamDescriptor " + label
                    + " op17 [" + row[0] + "," + itemId + "," + qty + "]"
                    + " count=" + VqsvSourceOps.sourceItemCount(s, itemId));
        }
    }

    private void assertOp19(VqsvIntroDemo.Scene s) {
        if (op19 == null) {
            return;
        }
        SourceSpecialReward reward = s.sourceSpecialRewards.get(op19[0]);
        int stack = reward == null ? 0 : reward.stackCount;
        boolean unlocked = reward != null && reward.unlocked;
        if (stack < op19[1] && !unlocked) {
            throw new IllegalStateException(label + " op19 reward missing reward="
                    + op19[0] + " expectedQty=" + op19[1]
                    + " stack=" + stack + " unlocked=" + unlocked);
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source PostBattleDownstreamDescriptor " + label
                + " op19 [" + op19[0] + "," + op19[1] + "]"
                + " stack=" + stack + " unlocked=" + unlocked);
    }

    private void assertOp23(VqsvIntroDemo.Scene s) {
        for (int[] row : op23) {
            if (!s.sourceEventStateComplete(row[0], row[1], row[2])) {
                throw new IllegalStateException(label + " op23 event state incomplete ["
                        + row[0] + "," + row[1] + "," + row[2] + "]="
                        + s.sourceEventState(row[0], row[1], row[2]));
            }
            s.sourceStateTrace.add("PORTED/PARTIAL source PostBattleDownstreamDescriptor " + label
                    + " op23 [" + row[0] + "," + row[1] + "," + row[2] + "]=3");
        }
    }

    private void assertOp14(VqsvIntroDemo.Scene s) {
        if (op14 == null) {
            return;
        }
        if (!s.sourceEventStateComplete(op14[0], op14[1], op14[2])) {
            throw new IllegalStateException(label + " op14 event state incomplete ["
                    + op14[0] + "," + op14[1] + "," + op14[2] + "]="
                    + s.sourceEventState(op14[0], op14[1], op14[2]));
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source PostBattleDownstreamDescriptor " + label
                + " op14 [" + op14[0] + "," + op14[1] + "," + op14[2] + "]=3");
    }

    private void traceOp45(VqsvIntroDemo.Scene s) {
        if (op45TaskFlag == Integer.MIN_VALUE) {
            return;
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source PostBattleDownstreamDescriptor " + label
                + " op45 taskFlag=" + op45TaskFlag
                + " text=\"" + op45Text + "\""
                + " game.c.t trace-only");
    }

    private void traceOp40(VqsvIntroDemo.Scene s) {
        if (op40Text == null) {
            return;
        }
        s.sourceStateTrace.add("PORTED/PARTIAL source PostBattleDownstreamDescriptor " + label
                + " op40 text=\"" + op40Text + "\"");
    }

    private static int[][] append(int[][] rows, int[] row) {
        int[][] out = new int[rows.length + 1][];
        System.arraycopy(rows, 0, out, 0, rows.length);
        out[rows.length] = row;
        return out;
    }
}
