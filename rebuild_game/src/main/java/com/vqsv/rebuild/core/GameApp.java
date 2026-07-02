package com.vqsv.rebuild.core;

import com.vqsv.rebuild.input.InputState;
import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.runtime.GamePanel;
import com.vqsv.rebuild.state.BootFlowState;
import com.vqsv.rebuild.state.GameStateMachine;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public final class GameApp {
    private final GameConfig config;
    private final AssetPaths assets;
    private final InputState input;
    private final GameStateMachine states;

    public GameApp(GameConfig config, AssetPaths assets) {
        this.config = config;
        this.assets = assets;
        this.input = new InputState();
        this.states = new GameStateMachine();
        this.states.replace(new BootFlowState(assets));
    }

    public void start() {
        SwingUtilities.invokeLater(this::createWindow);
    }

    private void createWindow() {
        GamePanel panel = new GamePanel(config, input, states);

        JFrame frame = new JFrame(config.title());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        panel.requestFocusInWindow();
        panel.startLoop();
    }
}
