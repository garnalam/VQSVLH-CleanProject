package com.vqsv.rebuild.state;

import com.vqsv.rebuild.input.InputSnapshot;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;

public final class GameStateMachine {
    private final Deque<GameState> stack = new ArrayDeque<>();

    public void replace(GameState state) {
        stack.clear();
        stack.push(state);
    }

    public void push(GameState state) {
        stack.push(state);
    }

    public void pop() {
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    public void tick(InputSnapshot input) {
        GameState current = stack.peek();
        if (current != null) {
            current.tick(input, this);
        }
    }

    public void render(Graphics2D graphics) {
        GameState current = stack.peek();
        if (current != null) {
            current.render(graphics);
        }
    }

    public String currentStateNameForSmoke() {
        GameState current = stack.peek();
        return current == null ? "none" : current.getClass().getSimpleName();
    }

    public Object currentStateForSmoke() {
        return stack.peek();
    }
}
