/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Graphics
 *  javax.microedition.lcdui.Image
 */
package game;

import game.c;
import game.g;
import game.h;
import game.i;
import game.k;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public final class f
extends an {
    private static f a;
    private static int[] b;
    private static int c;
    private byte d = 0;
    private Image e;
    private Image f;
    private byte g = (byte)10;
    private int[][] h = new int[this.g][5];
    private int[] i = new int[]{28, 3, 21, 22, 50, 5, 17, 17};
    private byte j = (byte)30;
    private byte k = (byte)30;
    private boolean l = false;
    private int m = 0;
    private int[] n = new int[this.g];
    private int[] o = new int[]{3958719, 3958719, 3958719, 7248110, 7248110, 9943031};

    public static f a() {
        if (a == null) {
            a = new f();
        }
        return a;
    }

    public final boolean d() {
        this.S = game.h.a();
        this.R = ab.a();
        this.S.a(this);
        c = 0;
        b = W ? new int[]{504, 503, 505, 506, 507, 508} : new int[]{503, 505, 506, 507, 508};
        if (this.e == null) {
            this.e = ae.b("/data/img/", "img_833");
        }
        if (this.f == null) {
            this.f = ae.b("/data/tex/", "menu");
        }
        this.c();
        this.a((byte)0);
        return true;
    }

    private void c() {
        for (int i2 = 0; i2 < this.g; ++i2) {
            this.h[i2][0] = -ae.a(this.j);
            this.h[i2][1] = game.f.x() + ae.a(this.k);
            this.h[i2][2] = ae.a(2);
            this.h[i2][3] = ae.b(1, 5);
            this.h[i2][4] = ae.b(3, 5);
        }
    }

    private void e() {
        game.k.D = false;
        game.k.G = 0;
        game.c.f = true;
        game.k.x = false;
        game.k.a().f = 0;
        game.k.a().g = 0;
        if (game.k.a().M != null) {
            game.k.a().M.e();
        }
        if (game.k.a().c != null) {
            game.k.a().c.p();
        }
        if (this.S != null) {
            this.S.b();
        }
        game.g.o().y = false;
        W = false;
        game.i.a().a((byte)9);
    }

    public final void b() {
        if (!this.Y) {
            return;
        }
        this.S();
        switch (this.P) {
            case 0: {
                f f2 = this;
                if (f2.k(16400)) {
                    if (--c < 0) {
                        c = b.length - 1;
                    }
                } else if (f2.k(32832)) {
                    if (++c > b.length - 1) {
                        c = 0;
                    }
                } else if (f2.k(196640)) {
                    if (W) {
                        switch (c) {
                            case 0: {
                                f f3 = f2;
                                if (game.k.a().M != null) {
                                    game.k.a().M.e();
                                }
                                if (game.k.a().c != null) {
                                    game.k.a().c.p();
                                }
                                if (f3.S != null) {
                                    f3.S.b();
                                }
                                game.g.o().y = false;
                                game.i.a().a((byte)9);
                                game.i.a().a((byte)9);
                                break;
                            }
                            case 1: {
                                f2.a((byte)5);
                                break;
                            }
                            case 2: {
                                f2.a((byte)1);
                                break;
                            }
                            case 3: {
                                f2.a((byte)2);
                                break;
                            }
                            case 4: {
                                f2.a((byte)3);
                                break;
                            }
                            case 5: {
                                f2.a((byte)4);
                            }
                        }
                    } else {
                        switch (c) {
                            case 0: {
                                f2.e();
                                break;
                            }
                            case 1: {
                                f2.a((byte)1);
                                break;
                            }
                            case 2: {
                                f2.a((byte)2);
                                break;
                            }
                            case 3: {
                                f2.a((byte)3);
                                break;
                            }
                            case 4: {
                                f2.a((byte)4);
                            }
                        }
                    }
                }
                f2.S.f();
                f2 = this;
                if (!f2.l) break;
                ++f2.m;
                if (f2.m < 100) break;
                for (int i2 = 0; i2 < f2.n.length; ++i2) {
                    f2.n[i2] = 0;
                }
                f2.m = 0;
                f2.c();
                f2.l = false;
                break;
            }
            case 1: {
                this.S.t();
                break;
            }
            case 2: {
                this.S.p();
                break;
            }
            case 3: {
                this.S.r();
                break;
            }
            case 4: {
                if (this.k(131072)) {
                    game.i.a().a((byte)1);
                    break;
                }
                if (!this.k(262144)) break;
                this.a((byte)0);
                break;
            }
            case 5: {
                if (this.k(131104)) {
                    game.k.a();
                    game.k.o();
                    this.e();
                    break;
                }
                if (!this.k(262144)) break;
                this.a((byte)0);
                this.R.a("/data/ui/msgtip.ui");
            }
        }
        this.R.c();
    }

    public final void b(Graphics graphics) {
        switch (this.P) {
            case 0: {
                int n2;
                if (this.f != null) {
                    graphics.drawImage(this.f, 0, 0, 20);
                }
                int n3 = game.f.x() - 20;
                int n4 = (game.f.w() - game.f.D().stringWidth(game.f.f(b[c]))) / 2;
                String string = game.f.f(b[c]);
                Graphics graphics2 = graphics;
                f f2 = this;
                graphics2.setColor(f2.o[f2.d]);
                graphics2.drawString(string, n4, n3 - 1, 36);
                graphics2.drawString(string, n4, n3 + 1, 36);
                graphics2.drawString(string, n4 - 1, n3, 36);
                graphics2.drawString(string, n4 + 1, n3, 36);
                graphics2.setColor(0xFFFFFF);
                graphics2.drawString(string, n4, n3, 36);
                f2.d = (byte)(f2.d + 1);
                if (f2.d >= 6) {
                    f2.d = 0;
                }
                graphics2 = graphics;
                f2 = this;
                if (f2.l) break;
                for (n2 = 0; n2 < f2.g; ++n2) {
                    graphics2.drawRegion(f2.e, f2.i[f2.h[n2][2] << 2], f2.i[(f2.h[n2][2] << 2) + 1], f2.i[(f2.h[n2][2] << 2) + 2], f2.i[(f2.h[n2][2] << 2) + 3], 0, f2.h[n2][0], f2.h[n2][1], 20);
                    int[] nArray = f2.h[n2];
                    nArray[0] = nArray[0] + f2.h[n2][3];
                    int[] nArray2 = f2.h[n2];
                    nArray2[1] = nArray2[1] - f2.h[n2][4];
                    if (f2.h[n2][0] <= game.f.w() && f2.h[n2][1] >= 0) continue;
                    int n5 = n2;
                    f2.n[n5] = f2.n[n5] + 1;
                }
                for (n2 = 0; n2 < f2.n.length && f2.n[n2] > 0; ++n2) {
                }
                if (n2 < f2.n.length) break;
                f2.l = true;
                break;
            }
            case 1: 
            case 2: 
            case 3: {
                Graphics graphics3 = graphics;
                for (int i2 = 0; i2 < game.f.x() / 20; ++i2) {
                    if (i2 % 2 == 0) {
                        graphics3.setColor(10440998);
                    } else {
                        graphics3.setColor(12082732);
                    }
                    graphics3.fillRect(0, i2 * 20, (int)game.f.w(), 20);
                }
                break;
            }
            case 4: {
                graphics.setColor(0);
                graphics.fillRect(0, 0, (int)game.f.w(), (int)game.f.x());
                graphics.setColor(0xFFFFFF);
                graphics.drawString("B\u1ea1n c\u00f3 mu\u1ed1n tho\u00e1t kh\u00f4ng?", (int)game.f.y(), game.f.z() - 10, 17);
                graphics.drawString("", 2, (int)game.f.x(), 36);
                graphics.drawString("Kh\u00f4ng", game.f.w() - 2, (int)game.f.x(), 40);
            }
        }
        this.R.a(graphics);
    }

    public final void f() {
        this.e = null;
        this.f = null;
        this.R.b();
    }

    public final void a(byte by) {
        this.P = by;
        switch (by) {
            case 2: {
                this.S.o();
                return;
            }
            case 3: {
                this.S.q();
                return;
            }
            case 1: {
                this.S.s();
                return;
            }
            case 0: {
                return;
            }
            case 5: {
                this.S.H();
                this.S.a("C\u00f3 ch\u1eafc ch\u1eafn x\u00f3a d\u1eef li\u1ec7u c\u0169 \u0111\u1ec3 ch\u01a1i m\u1edbi kh\u00f4ng?");
            }
        }
    }

    static {
        b = null;
    }
}

