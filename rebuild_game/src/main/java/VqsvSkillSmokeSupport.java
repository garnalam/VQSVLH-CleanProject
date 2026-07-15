final class VqsvSkillSmokeSupport {
    private static final Skill[] LANES = {
            FireSkill.INSTANCE,
            WoodSkill.INSTANCE,
            EarthSkill.INSTANCE,
            WaterSkill.INSTANCE,
            ElectricSkill.INSTANCE,
            ShadowSkill.INSTANCE,
            WindSkill.INSTANCE
    };

    private VqsvSkillSmokeSupport() {
    }

    static boolean runTimeline(String checkpoint, String outPath) {
        for (Skill lane : LANES) {
            if (lane.runTimeline(checkpoint, outPath)) {
                return true;
            }
        }
        return false;
    }

    static String[] checkpointsForSuite(String suite) {
        for (Skill lane : LANES) {
            String[] checkpoints = lane.checkpointsForSuite(suite);
            if (checkpoints != null) {
                return checkpoints;
            }
        }
        return null;
    }
}
