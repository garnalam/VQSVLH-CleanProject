interface Skill {
    String[] checkpointsForSuite(String suite);

    boolean runTimeline(String checkpoint, String outPath);
}
