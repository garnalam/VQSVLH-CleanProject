final class Scene1Room0Group3PetScript {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene1_room0_group3_pet",
            "modules/event/decoded/data__event__scene_1.mid.json",
            "room0 group3 pet offer/choice sequence",
            "PORTED_MANUAL_WITH_APPROX; op9 effect still approximate",
            "VqsvIntroDemo.Room0Group3PetOffer",
            "Covers op15, op2, op38, op4, op35, op87, op41, op3, op9, op14.",
            "ChoiceBox has dedicated smoke checkpoint room0_pet_choice_ui."
    );

    private Scene1Room0Group3PetScript() {
    }

    static void appendTo(java.util.List<Event> e) {
        e.add(s -> new VqsvIntroDemo.Room0Group3PetOffer());
    }
}
