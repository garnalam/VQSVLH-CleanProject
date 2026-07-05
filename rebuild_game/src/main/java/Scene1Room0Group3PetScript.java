import java.util.Arrays;
import java.util.List;

final class Scene1Room0Group3PetScript {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene1_room0_group3_pet",
            "modules/event/decoded/data__event__scene_1.mid.json",
            "room0 group3 pet offer/choice sequence",
            "PORTED_MANUAL_WITH_APPROX; op9 effect still approximate",
            "Room0Group3PetOffer",
            "Covers op15, op2, op38, op4, op35, op87, op41, op3, op9, op14.",
            "ChoiceBox has dedicated smoke checkpoint room0_pet_choice_ui."
    );

    private Scene1Room0Group3PetScript() {
    }

    static void appendTo(List<Event> e) {
        e.add(s -> new Room0Group3PetOffer());
    }
}

final class Room0Group3PetOffer implements Blocking {
    private static final int[] PET_IDS = {53, 54, 55};
    private static final int[] PET_BRANCH_TARGETS = {4, 8, 12};
    private static final int[][] OP87_ARGS = {
            {0, 51, 7, 3, 2, 30, 45, 0},
            {0, 17, 7, 3, 2, 10, 45, 0},
            {0, 6, 7, 3, 2, 0, 45, 0}
    };
    private boolean started;
    private int phase;
    private int selectedActor = -1;
    private int selectedPetIndex = -1;
    private int choiceIndex;
    private boolean upWasDown;
    private boolean downWasDown;
    private boolean leftWasDown;
    private boolean rightWasDown;
    private Blocking effectWait;

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!s.op15CheckEventState(1, 0, 2)) {
            s.tickFreeWorldPlayer();
            return false;
        }
        if (!started) {
            started = true;
            VqsvIntroDemo.Scene.setActive(s, PET_IDS, new int[]{0, 0, 0});
            s.sourceStateTrace.add("PORTED room0 group3 op15 [1,0,2] pass");
            s.sourceStateTrace.add("PORTED room0 group3 op2 show pets ids=[53,54,55] dirs=[0,0,0]");
            s.sourceStateTrace.add("PORTED room0 group3 op38 wait pets=[53,54,55] branches=[4,8,12]");
        }
        if (phase == 0) {
            if (s.key0) {
                for (int i = 0; i < PET_IDS.length; i++) {
                    int petId = PET_IDS[i];
                    if (!s.playerInteractsActorSourceMask(petId)) {
                        continue;
                    }
                    selectedActor = petId;
                    selectedPetIndex = i;
                    s.stopPlayerForSourceEvent();
                    s.sourceStateTrace.add("PORTED room0 group3 op38 selected actor="
                            + petId + " branch=" + PET_BRANCH_TARGETS[i]);
                    s.text = TextBox.dialog(s.font, VqsvText.Scene1Room0Group3.ELDER,
                            petDescription(petId), 1);
                    phase = 1;
                    return false;
                }
            }
            s.tickFreeWorldPlayer();
            return false;
        }
        if (phase == 1) {
            if (s.text != null && s.text.readyForKey && s.key0) {
                s.text.confirm();
                return false;
            }
            if (s.text == null) {
                choiceIndex = 0;
                upWasDown = s.keyUp;
                downWasDown = s.keyDown;
                leftWasDown = s.keyLeft;
                rightWasDown = s.keyRight;
                s.choice = ChoiceBox.optionUi(0, VqsvText.Scene1Room0Group3.YES_NO_OPTIONS);
                s.sourceStateTrace.add("PORTED/APPROX room0 group3 op35 choice shown actor="
                        + selectedActor + " branches=" + op35BranchesForSelectedPet()
                        + "; source game.h mode=0 /data/ui/option.ui coordinates/cells ported, full ao renderer pending");
                phase = 2;
            }
            return false;
        }
        if (phase == 2) {
            if ((s.keyUp && !upWasDown) || (s.keyLeft && !leftWasDown)) {
                s.choice.move(-1);
            }
            if ((s.keyDown && !downWasDown) || (s.keyRight && !rightWasDown)) {
                s.choice.move(1);
            }
            upWasDown = s.keyUp;
            downWasDown = s.keyDown;
            leftWasDown = s.keyLeft;
            rightWasDown = s.keyRight;
            if (s.choice != null && s.key0) {
                choiceIndex = s.choice.selectedIndex();
                s.choice = null;
                if (choiceIndex == 1) {
                    s.sourceStateTrace.add("PORTED room0 group3 op35 selected No branch target=2 return to op38");
                    phase = 0;
                    selectedActor = -1;
                    selectedPetIndex = -1;
                    return false;
                }
                applyOp87(s);
                s.text = TextBox.openBox(VqsvText.Common.ITEM_REWARD_PREFIX
                        + petRewardName(selectedActor));
                phase = 3;
            }
            return false;
        }
        if (phase == 3) {
            if (s.text != null && s.text.readyForKey && s.key0) {
                s.text.confirm();
                s.sourceStateTrace.add("PORTED room0 group3 op41 [16] jump to record 15");
                VqsvIntroDemo.Scene.hide(s, PET_IDS);
                s.sourceStateTrace.add("PORTED room0 group3 op3 hide pets ids=[53,54,55] states=[1,1,1]");
                effectWait = s.op9SourceEffect("room0 group3", 2, 0, 0, 0, 0, 0);
                phase = 4;
            }
            return false;
        }
        if (phase == 4) {
            if (effectWait != null && !effectWait.tick(s)) {
                return false;
            }
            s.op14CompleteEvent(1, 0, 3);
            s.sourceStateTrace.add("PORTED room0 group3 op14 complete");
            return true;
        }
        return false;
    }

    private void applyOp87(VqsvIntroDemo.Scene s) {
        int[] args = OP87_ARGS[selectedPetIndex];
        if (args[0] == 0) {
            SourcePetState pet = new SourcePetState(args[7], args[1], args[2], args[3], args[4], args[5], args[6]);
            s.sourcePets.add(pet);
            s.sourceStateTrace.add("PORTED/APPROX room0 group3 op87 addPet args="
                    + Arrays.toString(args)
                    + " stored slot=" + pet.slot
                    + " species=" + pet.speciesId
                    + " level=" + pet.level
                    + " skills=[" + pet.skillIds[0] + "," + pet.skillIds[1] + "]"
                    + "; full game.g pet inventory UI still pending");
        } else {
            s.sourceStateTrace.add("UNKNOWN room0 group3 op87 unsupported mode args=" + Arrays.toString(args));
        }
    }

    private String op35BranchesForSelectedPet() {
        switch (selectedActor) {
            case 53:
                return "[6,2]";
            case 54:
                return "[10,2]";
            case 55:
                return "[14,2]";
            default:
                return "[]";
        }
    }

    private static String petRewardName(int petId) {
        switch (petId) {
            case 53:
                return VqsvText.Scene1Room0Group3.PENGUIN;
            case 54:
                return VqsvText.Scene1Room0Group3.FROG;
            case 55:
                return VqsvText.Scene1Room0Group3.DRAGON;
            default:
                return VqsvText.Common.SOURCE_PET_REWARD_FALLBACK;
        }
    }

    private static String petDescription(int petId) {
        switch (petId) {
            case 53:
                return VqsvText.Scene1Room0Group3.PENGUIN;
            case 54:
                return VqsvText.Scene1Room0Group3.FROG;
            case 55:
                return VqsvText.Scene1Room0Group3.DRAGON;
            default:
                return "";
        }
    }
}
