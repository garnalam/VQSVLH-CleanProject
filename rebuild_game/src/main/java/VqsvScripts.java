import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class VqsvScripts {
    private static final List<ScriptInfo> ALL = Collections.unmodifiableList(Arrays.asList(
            Scene0IntroScript.INFO,
            Scene1Room3EntryScript.INFO,
            Scene1Room0Group0Script.INFO,
            Scene1Room1BunnyScript.INFO,
            Scene1Room0Group2ElderScript.INFO,
            Scene1Room0Group3PetScript.INFO,
            Scene1Room0Group6ElderBattleScript.INFO
    ));

    private VqsvScripts() {
    }

    static List<ScriptInfo> all() {
        return ALL;
    }

    static final class ScriptInfo {
        final String id;
        final String source;
        final String records;
        final String status;
        final String logicLocation;
        final String[] notes;

        ScriptInfo(String id, String source, String records, String status,
                   String logicLocation, String... notes) {
            this.id = id;
            this.source = source;
            this.records = records;
            this.status = status;
            this.logicLocation = logicLocation;
            this.notes = notes;
        }
    }
}
