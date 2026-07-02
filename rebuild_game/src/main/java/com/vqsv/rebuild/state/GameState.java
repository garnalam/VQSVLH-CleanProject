package com.vqsv.rebuild.state;

import com.vqsv.rebuild.input.InputSnapshot;

import java.awt.Graphics2D;

public interface GameState {
    void tick(InputSnapshot input, GameStateMachine states);

    void render(Graphics2D graphics);
}
