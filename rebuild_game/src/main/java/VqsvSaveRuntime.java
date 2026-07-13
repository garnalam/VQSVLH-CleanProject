import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

final class VqsvSaveRuntime {
    private static final int VERSION = 1;
    private static final Path SAVE_PATH = Paths.get("build", "save", "vqsv_autosave.properties");

    private VqsvSaveRuntime() {
    }

    static boolean hasSave() {
        return Files.isRegularFile(SAVE_PATH);
    }

    static boolean save(VqsvIntroDemo.Scene s) {
        Properties p = new Properties();
        p.setProperty("version", String.valueOf(VERSION));
        p.setProperty("eventIndex", String.valueOf(s.eventIndex));
        p.setProperty("scene", String.valueOf(s.currentSceneId));
        p.setProperty("room", String.valueOf(s.currentRoomIndex));
        p.setProperty("camera", s.cameraX + "," + s.cameraY);
        p.setProperty("player", s.playerX + "," + s.playerY + "," + s.player.direction + "," + bool(s.player.visible));
        p.setProperty("sourceMoney", String.valueOf(s.sourceMoney));
        p.setProperty("sourceBadges", String.valueOf(s.sourceBadges));
        p.setProperty("sourceGameCF", bool(s.sourceGameCF));
        p.setProperty("sourcePetRefreshOps", String.valueOf(s.sourcePetRefreshOps));
        p.setProperty("sourceAvoidMonsterTicks", String.valueOf(s.sourceAvoidMonsterTicks));
        p.setProperty("sourceAvoidMonsterElapsed", String.valueOf(s.sourceAvoidMonsterElapsed));
        p.setProperty("sourceEggActive", bool(s.sourceEggActive));
        p.setProperty("sourceEggType", String.valueOf(s.sourceEggType));
        p.setProperty("sourceEggProgress", String.valueOf(s.sourceEggProgress));
        p.setProperty("sourceEggKnownSpecies", join(s.sourceEggKnownSpecies));
        p.setProperty("sourceRideBlocked", join(s.sourceRideBlocked));
        p.setProperty("sourceRideActiveIndex", String.valueOf(s.sourceRideActiveIndex));
        p.setProperty("sourcePlayerMoveSpeed", String.valueOf(s.sourcePlayerMoveSpeed));
        writeActors(p, s);
        writeEventStates(p, s);
        writeBag(p, s);
        writeEquipment(p, s);
        writeSpecialRewards(p, s);
        writePets(p, "pet", s.sourcePets);
        writePets(p, "bankPet", s.sourcePetBank);
        try {
            Files.createDirectories(SAVE_PATH.getParent());
            try (OutputStream out = Files.newOutputStream(SAVE_PATH)) {
                p.store(out, "VQSV rebuild source-backed partial autosave");
            }
            s.sourceStateTrace.add("PORTED/PARTIAL save game.k.k route snapshot path=" + SAVE_PATH
                    + " scene=" + s.currentSceneId + " room=" + s.currentRoomIndex
                    + " eventIndex=" + s.eventIndex
                    + " pets=" + s.sourcePets.size());
            return true;
        } catch (IOException ex) {
            s.sourceStateTrace.add("PENDING save failed path=" + SAVE_PATH + " error=" + ex.getMessage());
            return false;
        }
    }

    static boolean loadInto(VqsvIntroDemo.Scene s) {
        if (!hasSave()) {
            return false;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(SAVE_PATH)) {
            p.load(in);
        } catch (IOException ex) {
            return false;
        }
        if (intProp(p, "version", -1) != VERSION) {
            return false;
        }
        int scene = intProp(p, "scene", -1);
        int room = intProp(p, "room", -1);
        int[] camera = ints(p.getProperty("camera", "0,0"));
        loadRoom(s, scene, room, value(camera, 0, 0), value(camera, 1, 0));
        s.eventIndex = intProp(p, "eventIndex", s.eventIndex);
        s.currentSceneId = scene;
        s.currentRoomIndex = room;
        restorePlayer(s, p);
        restoreActors(s, p);
        s.sourceMoney = intProp(p, "sourceMoney", 0);
        s.sourceBadges = intProp(p, "sourceBadges", 0);
        s.sourceGameCF = boolProp(p, "sourceGameCF", false);
        s.sourcePetRefreshOps = intProp(p, "sourcePetRefreshOps", 0);
        s.sourceAvoidMonsterTicks = intProp(p, "sourceAvoidMonsterTicks", 0);
        s.sourceAvoidMonsterElapsed = intProp(p, "sourceAvoidMonsterElapsed", 0);
        s.sourceEggActive = boolProp(p, "sourceEggActive", false);
        s.sourceEggType = intProp(p, "sourceEggType", 0);
        s.sourceEggProgress = intProp(p, "sourceEggProgress", 0);
        copyInto(ints(p.getProperty("sourceEggKnownSpecies", "")), s.sourceEggKnownSpecies);
        copyInto(ints(p.getProperty("sourceRideBlocked", "")), s.sourceRideBlocked);
        s.sourceRideActiveIndex = intProp(p, "sourceRideActiveIndex", -1);
        s.sourcePlayerMoveSpeed = intProp(p, "sourcePlayerMoveSpeed", 4);
        restoreEventStates(s, p);
        restoreBag(s, p);
        restoreEquipment(s, p);
        restoreSpecialRewards(s, p);
        restorePets(p, "pet", s.sourcePets);
        restorePets(p, "bankPet", s.sourcePetBank);
        repairKnownRouteSave(s);
        s.current = null;
        s.text = null;
        s.choice = null;
        s.battleOverlayTicks = 0;
        s.savePromptVisible = false;
        s.sourceStateTrace.add("PORTED/PARTIAL save loaded route snapshot path=" + SAVE_PATH
                + " scene=" + scene + " room=" + room + " eventIndex=" + s.eventIndex
                + " pets=" + s.sourcePets.size());
        return true;
    }

    private static void repairKnownRouteSave(VqsvIntroDemo.Scene s) {
        if (s.currentSceneId == 1 && s.currentRoomIndex == 1
                && s.sourceEventStateComplete(1, 1, 1)
                && !s.sourceEventStateComplete(1, 1, 0)
                && !s.playerIntersectsSourceRect(370, 176, 80, 32)
                && !s.playerIntersectsSourceRect(290, 96, 240, 192)) {
            int oldEventIndex = s.eventIndex;
            s.setPlayerPositionApprox(374, 180);
            s.setCameraCenter(374, 180);
            s.eventIndex = room1BunnyOp13EventIndex();
            s.sourceStateTrace.add("PORTED/PARTIAL save load unstuck room1 Bunny checkpoint"
                    + " -> player=[374,180] op13=[370,176,80,32]"
                    + " eventIndex=" + oldEventIndex + "->" + s.eventIndex);
        }
    }

    private static int room1BunnyOp13EventIndex() {
        return VqsvIntroDemo.Scene.room1BunnyOp13EventIndex >= 0
                ? VqsvIntroDemo.Scene.room1BunnyOp13EventIndex
                : 123;
    }

    private static void loadRoom(VqsvIntroDemo.Scene s, int scene, int room, int cameraX, int cameraY) {
        if (scene == 1 && room == 1) {
            s.loadScene1Room1(cameraX + VqsvIntroDemo.W / 2, cameraY + VqsvIntroDemo.H / 2);
        } else if (scene == 1 && room == 0) {
            s.loadScene1Room0(cameraX + VqsvIntroDemo.W / 2, cameraY + VqsvIntroDemo.H / 2);
        } else if (scene == 1 && room == 2) {
            s.loadScene1Room2(cameraX + VqsvIntroDemo.W / 2, cameraY + VqsvIntroDemo.H / 2);
        } else {
            s.loadScene1Room1(cameraX + VqsvIntroDemo.W / 2, cameraY + VqsvIntroDemo.H / 2);
        }
        s.cameraX = cameraX;
        s.cameraY = cameraY;
        if (s.mapRenderer != null) {
            s.mapRenderer.setCamera(cameraX, cameraY);
        }
    }

    private static void writeActors(Properties p, VqsvIntroDemo.Scene s) {
        p.setProperty("actor.count", String.valueOf(s.actors.length));
        for (int i = 0; i < s.actors.length; i++) {
            Actor a = s.actors[i];
            if (a != null) {
                p.setProperty("actor." + i, a.x + "," + a.y + "," + a.direction + "," + bool(a.visible));
            }
        }
    }

    private static void restoreActors(VqsvIntroDemo.Scene s, Properties p) {
        for (String name : p.stringPropertyNames()) {
            if (!name.startsWith("actor.")) {
                continue;
            }
            String indexText = name.substring("actor.".length());
            if ("count".equals(indexText)) {
                continue;
            }
            int index = parseInt(indexText, -1);
            if (index < 0 || index >= s.actors.length || s.actors[index] == null) {
                continue;
            }
            int[] row = ints(p.getProperty(name, ""));
            Actor a = s.actors[index];
            a.x = value(row, 0, a.x);
            a.y = value(row, 1, a.y);
            a.direction = value(row, 2, a.direction);
            a.visible = value(row, 3, a.visible ? 1 : 0) != 0;
            a.applyMode(0);
        }
    }

    private static void restorePlayer(VqsvIntroDemo.Scene s, Properties p) {
        int[] row = ints(p.getProperty("player", ""));
        s.playerX = value(row, 0, s.playerX);
        s.playerY = value(row, 1, s.playerY);
        s.player.x = s.playerX;
        s.player.y = s.playerY;
        s.player.direction = value(row, 2, s.player.direction);
        s.player.visible = value(row, 3, 1) != 0;
        s.player.applyMode(0);
    }

    private static void writeEventStates(Properties p, VqsvIntroDemo.Scene s) {
        Map<String, Byte> snapshot = s.eventState.snapshotStates();
        p.setProperty("eventState.count", String.valueOf(snapshot.size()));
        int i = 0;
        for (String key : new TreeSet<>(snapshot.keySet())) {
            p.setProperty("eventState." + i, key + "=" + snapshot.get(key));
            i++;
        }
    }

    private static void restoreEventStates(VqsvIntroDemo.Scene s, Properties p) {
        Map<String, Byte> snapshot = new HashMap<>();
        int count = intProp(p, "eventState.count", 0);
        for (int i = 0; i < count; i++) {
            String row = p.getProperty("eventState." + i, "");
            int at = row.indexOf('=');
            if (at <= 0) {
                continue;
            }
            snapshot.put(row.substring(0, at), (byte) parseInt(row.substring(at + 1), 0));
        }
        s.eventState.restoreStates(snapshot);
    }

    private static void writeBag(Properties p, VqsvIntroDemo.Scene s) {
        p.setProperty("bag.count", String.valueOf(s.sourceBagItems.size()));
        int i = 0;
        for (Integer id : new TreeSet<>(s.sourceBagItems.keySet())) {
            BagItem item = s.sourceBagItems.get(id);
            p.setProperty("bag." + i, item.id + "," + item.count + "," + item.bagChannel + "," + bool(item.keepAtZero));
            i++;
        }
    }

    private static void restoreBag(VqsvIntroDemo.Scene s, Properties p) {
        s.sourceBagItems.clear();
        int count = intProp(p, "bag.count", 0);
        for (int i = 0; i < count; i++) {
            int[] row = ints(p.getProperty("bag." + i, ""));
            if (row.length >= 4) {
                s.sourceBagItems.put(row[0], new BagItem(row[0], row[1], row[2], row[3] != 0));
            }
        }
    }

    private static void writeEquipment(Properties p, VqsvIntroDemo.Scene s) {
        p.setProperty("equipment.count", String.valueOf(s.sourceEquipmentItems.size()));
        for (int i = 0; i < s.sourceEquipmentItems.size(); i++) {
            SourceEquipmentItem item = s.sourceEquipmentItems.get(i);
            p.setProperty("equipment." + i, item.id + "," + bool(item.equippedFlag));
        }
    }

    private static void restoreEquipment(VqsvIntroDemo.Scene s, Properties p) {
        s.sourceEquipmentItems.clear();
        int count = intProp(p, "equipment.count", 0);
        for (int i = 0; i < count; i++) {
            int[] row = ints(p.getProperty("equipment." + i, ""));
            if (row.length >= 2) {
                s.sourceEquipmentItems.add(new SourceEquipmentItem(row[0], row[1] != 0));
            }
        }
    }

    private static void writeSpecialRewards(Properties p, VqsvIntroDemo.Scene s) {
        p.setProperty("special.count", String.valueOf(s.sourceSpecialRewards.size()));
        int i = 0;
        for (Integer id : new TreeSet<>(s.sourceSpecialRewards.keySet())) {
            SourceSpecialReward reward = s.sourceSpecialRewards.get(id);
            p.setProperty("special." + i, reward.id + "," + bool(reward.unlocked)
                    + "," + reward.stackCount);
            i++;
        }
    }

    private static void restoreSpecialRewards(VqsvIntroDemo.Scene s, Properties p) {
        s.sourceSpecialRewards.clear();
        int count = intProp(p, "special.count", 0);
        for (int i = 0; i < count; i++) {
            int[] row = ints(p.getProperty("special." + i, ""));
            if (row.length < 3) {
                continue;
            }
            SourceSpecialReward reward = SourceSpecialReward.fromSourceDb(row[0]);
            reward.unlocked = row[1] != 0;
            reward.stackCount = row[2];
            reward.gameGPath = reward.id == 7 || reward.id == 8 || reward.id == 9
                    ? "save restore game.g.c stack special item"
                    : "save restore game.g.i unlock vector entry";
            s.sourceSpecialRewards.put(row[0], reward);
        }
    }

    private static void writePets(Properties p, String prefix, java.util.List<SourcePetState> pets) {
        p.setProperty(prefix + ".count", String.valueOf(pets.size()));
        for (int i = 0; i < pets.size(); i++) {
            SourcePetState pet = pets.get(i);
            p.setProperty(prefix + "." + i + ".core", pet.slot + "," + pet.speciesId + "," + pet.level
                    + "," + pet.arg3 + "," + pet.arg4 + "," + pet.refreshCount);
            p.setProperty(prefix + "." + i + ".skills", join(pet.skillIds));
            p.setProperty(prefix + "." + i + ".cooldowns", join(pet.skillCooldowns));
            p.setProperty(prefix + "." + i + ".payload", join(pet.sourcePayload));
            p.setProperty(prefix + "." + i + ".specialUse", String.valueOf(pet.sourceSpecialUseId));
        }
    }

    private static void restorePets(Properties p, String prefix, java.util.List<SourcePetState> pets) {
        pets.clear();
        int count = intProp(p, prefix + ".count", 0);
        for (int i = 0; i < count; i++) {
            int[] core = ints(p.getProperty(prefix + "." + i + ".core", ""));
            if (core.length < 5) {
                continue;
            }
            SourcePetState pet = new SourcePetState();
            pet.slot = core[0];
            pet.speciesId = core[1];
            pet.level = core[2];
            pet.arg3 = core[3];
            pet.arg4 = core[4];
            pet.refreshCount = value(core, 5, 0);
            copyInto(ints(p.getProperty(prefix + "." + i + ".skills", "")), pet.skillIds);
            copyInto(ints(p.getProperty(prefix + "." + i + ".cooldowns", "")), pet.skillCooldowns);
            pet.sourcePayload = ints(p.getProperty(prefix + "." + i + ".payload", ""));
            pet.sourceSpecialUseId = intProp(p, prefix + "." + i + ".specialUse", -1);
            pets.add(pet);
        }
    }

    private static String join(int[] values) {
        if (values == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                out.append(',');
            }
            out.append(values[i]);
        }
        return out.toString();
    }

    private static int[] ints(String text) {
        if (text == null || text.isEmpty()) {
            return new int[0];
        }
        String[] parts = text.split(",");
        ArrayList<Integer> values = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                values.add(parseInt(part, 0));
            }
        }
        int[] out = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
    }

    private static void copyInto(int[] source, int[] target) {
        for (int i = 0; i < target.length && i < source.length; i++) {
            target[i] = source[i];
        }
    }

    private static int intProp(Properties p, String key, int fallback) {
        return parseInt(p.getProperty(key), fallback);
    }

    private static boolean boolProp(Properties p, String key, boolean fallback) {
        String value = p.getProperty(key);
        if (value == null) {
            return fallback;
        }
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    private static int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private static int value(int[] row, int index, int fallback) {
        return index >= 0 && index < row.length ? row[index] : fallback;
    }

    private static String bool(boolean value) {
        return value ? "1" : "0";
    }
}
