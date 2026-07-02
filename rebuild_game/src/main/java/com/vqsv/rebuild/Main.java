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
            System.out.println("VQSV rebuild skeleton");
            System.out.println("projectRoot=" + config.projectRoot());
            for (String line : new ResourceSmokeCheck(assets).run()) {
                System.out.println(line);
            }
            return;
        }
        GameApp app = new GameApp(config, assets);
        app.start();
    }
}
