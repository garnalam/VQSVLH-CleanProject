/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Canvas
 *  javax.microedition.lcdui.Graphics
 */
package game;

import game.GameMIDLet;
import game.i;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

public final class e
extends Canvas
implements Runnable {
    private static e b = null;
    private static GameMIDLet c;
    private static ap d;
    private i e;
    public static e a;
    private long f = 0L;
    private long g = 0L;
    private long h = 0L;
    private int i = 0;

    public static e a(GameMIDLet gameMIDLet) {
        if (b == null) {
            b = new e(gameMIDLet);
        }
        return b;
    }

    public static e a() {
        return b;
    }

    private e(GameMIDLet object) {
        a = this;
        this.setFullScreenMode(true);
        c = object;
        an.A();
        an.a((short)this.getWidth(), (short)this.getHeight());
        this.e = game.i.a();
        this.e.c();
        object = this.e;
        if (d != null) {
            d.c(false);
            d = null;
        }
        if (object != null) {
            ((ap)object).c(true);
            d = object;
        }
        new Thread(this).start();
    }

    protected final void hideNotify() {
        if (!an.T && this.e.e() > 1) {
            this.e.g();
        }
    }

    protected final void paint(Graphics graphics) {
        if (this.e.e() > 1) {
            this.e.b(graphics);
        }
    }

    public final void run() {
        while (this.e.e() > 1) {
            this.f = System.currentTimeMillis();
            this.e.b();
            this.repaint();
            this.serviceRepaints();
            this.g = System.currentTimeMillis();
            this.h = this.g - this.f;
            if (this.h > (long)an.B()) {
                this.h = an.B();
            }
            try {
                Thread.sleep((long)an.B() - this.h);
            }
            catch (InterruptedException interruptedException) {}
        }
        c.destroyApp(true);
    }

    protected final void keyPressed(int n2) {
        if (this.e != null) {
            this.e.i(n2);
        }
    }

    protected final void keyReleased(int n2) {
        if (this.e != null) {
            this.e.j(n2);
        }
    }

    protected final void pointerPressed(int n2, int n3) {
        this.i = 0;
        if (this.e.e() == 13) {
            if (ae.a(n2, n3, 38, 225, 50)) {
                this.i = -6;
            } else if (ae.a(n2, n3, 150, 225, 50)) {
                this.i = -7;
            }
        } else if (ae.a(n2, n3, 0, 280, 40)) {
            this.i = -6;
        } else if (ae.a(n2, n3, 200, 280, 40)) {
            this.i = -7;
        }
        this.keyPressed(this.i);
    }

    protected final void pointerReleased(int n2, int n3) {
        this.keyReleased(this.i);
        if (this.e != null) {
            this.e.d(n2, n3);
        }
    }
}

