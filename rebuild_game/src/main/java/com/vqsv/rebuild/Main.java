package com.vqsv.rebuild;

import com.vqsv.rebuild.core.GameApp;
import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.ResourceSmokeCheck;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        GameConfig config = GameConfig.defaultConfig();
        AssetPaths assets = AssetPaths.fromWorkingTree(config);
        if (args.length > 0 && "--check".equals(args[0])) {
            System.out.println("VQSV Liet Hoa rebuild release check");
            System.out.println("projectRoot=" + config.projectRoot());
            for (String line : new ResourceSmokeCheck(assets).run()) {
                System.out.println(line);
            }
            return;
        }
        if (args.length > 0 && args[0].startsWith("--smoke")) {
            System.err.println("Smoke checkpoints are handled by VqsvIntroDemo, not com.vqsv.rebuild.Main.");
            System.err.println("Use: java -cp build/classes VqsvIntroDemo --smoke-checkpoint <checkpoint> <out.png>");
            return;
        }
        GameApp app = new GameApp(config, assets);
        app.start();
    }
}
