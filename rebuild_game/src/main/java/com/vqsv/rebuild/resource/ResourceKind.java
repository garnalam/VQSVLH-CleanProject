package com.vqsv.rebuild.resource;

public enum ResourceKind {
    IMG("img"),
    SPR("spr"),
    MAP("map"),
    MOD("mod"),
    SCRIPT("script"),
    FONT("root_misc"),
    UI("ui"),
    SOUND("sound"),
    EVENT("event");

    private final String folderName;

    ResourceKind(String folderName) {
        this.folderName = folderName;
    }

    public String folderName() {
        return folderName;
    }
}
