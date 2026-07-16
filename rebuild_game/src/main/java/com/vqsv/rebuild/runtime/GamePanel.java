package com.vqsv.rebuild.runtime;

import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.debug.VqsvDebugLog;
import com.vqsv.rebuild.input.InputState;
import com.vqsv.rebuild.state.GameStateMachine;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public final class GamePanel extends JPanel implements Runnable {
    private final GameConfig config;
    private final InputState input;
    private final GameStateMachine states;
    private final BufferedImage backBuffer;
    private Thread loopThread;
    private volatile boolean running;
    private int debugFrame;

    public GamePanel(GameConfig config, InputState input, GameStateMachine states) {
        this.config = config;
        this.input = input;
        this.states = states;
        this.backBuffer = new BufferedImage(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(GameConfig.LOGICAL_WIDTH * config.scale(), GameConfig.LOGICAL_HEIGHT * config.scale()));
        setFocusable(true);
        setBackground(Color.BLACK);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                VqsvDebugLog.log("input keyPressed code=" + event.getKeyCode()
                        + " text=" + KeyEvent.getKeyText(event.getKeyCode())
                        + " focusOwner=" + isFocusOwner());
                input.setKey(event.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent event) {
                VqsvDebugLog.log("input keyReleased code=" + event.getKeyCode()
                        + " text=" + KeyEvent.getKeyText(event.getKeyCode())
                        + " focusOwner=" + isFocusOwner());
                input.setKey(event.getKeyCode(), false);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                requestFocusInWindow();
                int logicalX = logicalX(event.getX());
                int logicalY = logicalY(event.getY());
                VqsvDebugLog.log("input mousePressed screen=[" + event.getX() + "," + event.getY()
                        + "] logical=[" + logicalX + "," + logicalY + "] focusOwner=" + isFocusOwner());
                input.pressPointer(
                        clamp(logicalX, 0, GameConfig.LOGICAL_WIDTH - 1),
                        clamp(logicalY, 0, GameConfig.LOGICAL_HEIGHT - 1));
            }
        });
        MouseAdapter pointerMotion = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                input.movePointer(
                        clamp(logicalX(event.getX()), 0, GameConfig.LOGICAL_WIDTH - 1),
                        clamp(logicalY(event.getY()), 0, GameConfig.LOGICAL_HEIGHT - 1));
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                mouseMoved(event);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent event) {
                requestFocusInWindow();
                input.rotateWheel(event.getWheelRotation());
                VqsvDebugLog.log("input mouseWheel rotation=" + event.getWheelRotation()
                        + " screen=[" + event.getX() + "," + event.getY()
                        + "] logical=[" + logicalX(event.getX()) + "," + logicalY(event.getY()) + "]");
            }
        };
        addMouseMotionListener(pointerMotion);
        addMouseWheelListener(pointerMotion);
    }

    public void startLoop() {
        if (running) {
            return;
        }
        running = true;
        loopThread = new Thread(this, "vqsv-game-loop");
        loopThread.setDaemon(true);
        loopThread.start();
    }

    @Override
    public void run() {
        long frameNanos = config.tickMillis() * 1_000_000L;
        long nextFrame = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            if (now >= nextFrame) {
                tick();
                repaint();
                nextFrame += frameNanos;
                if (nextFrame < now - frameNanos) {
                    nextFrame = now + frameNanos;
                }
            } else {
                long sleepMillis = Math.max(1L, (nextFrame - now) / 1_000_000L);
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
        }
    }

    private void tick() {
        debugFrame++;
        if (debugFrame % 30 == 0) {
            VqsvDebugLog.log("panel tick frame=" + debugFrame
                    + " focusOwner=" + isFocusOwner()
                    + " state=" + states.currentStateNameForSmoke());
        }
        states.tick(input.snapshot());
        input.finishFrame();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private int logicalX(int componentX) {
        return componentX * GameConfig.LOGICAL_WIDTH / Math.max(1, getWidth());
    }

    private int logicalY(int componentY) {
        return componentY * GameConfig.LOGICAL_HEIGHT / Math.max(1, getHeight());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D bufferGraphics = backBuffer.createGraphics();
        try {
            bufferGraphics.setColor(Color.BLACK);
            bufferGraphics.fillRect(0, 0, backBuffer.getWidth(), backBuffer.getHeight());
            states.render(bufferGraphics);
        } finally {
            bufferGraphics.dispose();
        }

        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(backBuffer, 0, 0, getWidth(), getHeight(), null);
        } finally {
            g2.dispose();
        }
    }
}
