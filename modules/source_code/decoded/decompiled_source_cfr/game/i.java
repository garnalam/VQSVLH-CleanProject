/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Graphics
 *  javax.microedition.lcdui.Image
 *  javax.microedition.media.Manager
 *  javax.microedition.media.MediaException
 *  javax.microedition.media.Player
 *  javax.microedition.media.control.VolumeControl
 */
package game;

import game.d;
import game.f;
import game.g;
import game.h;
import game.k;
import java.io.InputStream;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

public final class i
extends an {
    private static i h = null;
    private byte i;
    private byte j;
    private int k;
    private Image l;
    private an m;
    private static int n = 0;
    private static int o = 10;
    private String[] p = new String[]{"H\u1ecfa h\u1ec7 kh\u1eafc m\u1ed9c h\u1ec7", "M\u1ed9c h\u1ec7 kh\u1eafc th\u1ed5 h\u1ec7", "Th\u1ed5 h\u1ec7 kh\u1eafc th\u1ee7y h\u1ec7", "Th\u1ee7y h\u1ec7 kh\u1eafc h\u1ecfa h\u1ec7", "Qu\u1ef7 h\u1ec7 kh\u1eafc phong h\u1ec7", "Phong h\u1ec7 kh\u1eafc \u0111i\u1ec7n h\u1ec7", "\u0110i\u1ec7n h\u1ec7 kh\u1eafc qu\u1ef7 h\u1ec7"};
    private String[] q = new String[]{"Thuy\u1ec1n c\u00e0ng \u0111i c\u00e0ng xa", "B\u01a1i... b\u01a1i...", "\u0110\u00edch \u0111\u1ebfn ng\u00e0y c\u00e0ng g\u1ea7n", "Thuy\u1ec1n nh\u1ecf \u0111ang ch\u1ea1y"};
    public long a = 0L;
    public long b = 0L;
    public long c = 0L;
    public long d = 0L;
    public long e = 0L;
    public long f = 0L;
    private boolean r = false;
    private String s = "";
    public byte g = 0;
    private Player t;
    private VolumeControl u;
    private static String v = null;

    public static i a() {
        if (h == null) {
            h = new i();
            v = "0";
        }
        return h;
    }

    public final void c(boolean bl) {
        if (bl) {
            this.c();
            return;
        }
        this.j();
    }

    public final void c() {
        this.f = System.currentTimeMillis();
        this.a((byte)3);
        super.c(true);
    }

    private void j() {
        this.a((byte)1);
        super.c(false);
    }

    public final void a(byte by) {
        if (by >= 24) {
            return;
        }
        this.j = this.i;
        switch (this.i) {
            case 2: {
                break;
            }
            case 4: {
                break;
            }
            case 16: {
                break;
            }
            case 15: {
                break;
            }
            case 6: {
                break;
            }
            case 7: 
            case 9: 
            case 12: 
            case 22: 
            case 23: {
                n = 0;
                break;
            }
            case 10: {
                break;
            }
            case 8: 
            case 11: 
            case 13: {
                break;
            }
            case 3: {
                b.a().a = -1;
            }
        }
        this.i = by;
        switch (by) {
            case 3: {
                b.a().c(0, 19);
                break;
            }
            case 15: {
                this.l = ae.a("/data/logo/", "0");
                break;
            }
            case 12: 
            case 22: {
                game.i.u();
                n = 0;
                break;
            }
            case 23: {
                game.i.u();
                n = 0;
                by = (byte)ae.a(this.q.length);
                this.s = this.q[by];
                break;
            }
            case 9: {
                game.i.u();
                n = 0;
                by = (byte)ae.a(this.p.length);
                this.s = this.p[by];
                break;
            }
            case 11: {
                game.k.a().S.g = true;
                break;
            }
            case 2: {
                if (game.k.a().c == null) break;
                game.k.a().c.J();
            }
        }
        this.k = 0;
    }

    public final byte e() {
        return this.i;
    }

    public final void g() {
        if (this.i == 9 || this.i == 22 || this.i == 23) {
            this.n();
            this.r = true;
            this.P();
            return;
        }
        if (this.i != 2) {
            this.n();
            this.r = true;
            this.a((byte)2);
            this.P();
        }
    }

    private void k() {
        this.a("0");
        this.a(this.j);
        this.r = false;
        this.P();
    }

    public final boolean d() {
        this.s();
        this.S = game.h.a();
        this.R = ab.a();
        this.S.a(this);
        am.a();
        aa.a();
        aq.a();
        game.i.e(0);
        game.i.D();
        game.k.a();
        game.k.i();
        game.i.t();
        return true;
    }

    public final void f() {
        if (this.m != null) {
            this.m.f();
            this.m = null;
        }
    }

    public final void b() {
        if (!this.Y) {
            return;
        }
        this.S();
        switch (this.i) {
            case 3: {
                if (!game.i.v()) {
                    this.d();
                }
                if (!b.a().b || !game.i.v()) break;
                this.a((byte)15);
                break;
            }
            case 2: {
                if (!this.k(262144)) break;
                this.k();
                break;
            }
            case 4: {
                if (this.k(131072)) {
                    this.j();
                    break;
                }
                if (!this.k(262144)) break;
                this.k();
                break;
            }
            case 16: {
                ++this.k;
                if (this.k < 10) break;
                this.l = null;
                this.a((byte)6);
                break;
            }
            case 15: {
                ++this.k;
                if (this.k < 10) break;
                this.k = 0;
                this.l = null;
                this.l = ae.a("/data/logo/", "cwalogo");
                this.a((byte)16);
                break;
            }
            case 6: {
                if (this.k(131072)) {
                    this.g = (byte)2;
                    this.a(v);
                    this.a((byte)7);
                    break;
                }
                if (!this.k(262144)) break;
                this.g = 0;
                this.a((byte)7);
                break;
            }
            case 9: 
            case 22: 
            case 23: {
                this.f();
                this.m = game.k.a();
                this.m.d();
                game.g.U = false;
                this.a((byte)11);
                if (this.r) {
                    i i2 = this;
                    if (i2.i != 2) {
                        i2.a((byte)2);
                    }
                }
                this.a(this.m);
                break;
            }
            case 10: {
                this.f();
                this.m = game.k.a();
                ((k)this.m).p();
                this.a(this.m);
                this.a((byte)11);
                break;
            }
            case 7: {
                this.f();
                this.m = game.f.a();
                this.m.d();
                this.a(this.m);
                this.a((byte)8);
                break;
            }
            case 12: {
                if (!game.i.v()) {
                    this.m = null;
                    this.m = game.d.a();
                    this.m.d();
                    this.a(this.m);
                    if (((d)this.m).b == 0) {
                        b.a().c(-2013265920, 6);
                    } else if (((d)this.m).b == 2) {
                        b.a().c(-2013265920, 8);
                    } else if (((d)this.m).b == 1) {
                        b.a().c(-2013265920, 7);
                    }
                }
                if (game.i.v()) {
                    b.a().b();
                    game.g.U = false;
                }
                if (!b.a().b) break;
                ((d)this.m).g();
                this.a((byte)13);
                break;
            }
            case 8: 
            case 11: 
            case 13: 
            case 20: {
                if (this.m == null) break;
                this.m.b();
            }
        }
        if (game.k.a().f == 3 && game.k.a().g == 7 && this.c == 0L && this.a != 0L) {
            this.b = System.currentTimeMillis();
        }
        this.e = System.currentTimeMillis();
    }

    public final void b(Graphics object) {
        if (!this.Y) {
            return;
        }
        object.setFont(game.i.D());
        switch (this.i) {
            case 3: {
                object.setColor(0xFFFFFF);
                object.fillRect(0, 0, (int)game.i.w(), (int)game.i.x());
                b.a().a((Graphics)object);
                return;
            }
            case 2: {
                object.setColor(0);
                object.fillRect(0, 0, (int)game.i.w(), (int)game.i.x());
                object.setColor(0xFFFFFF);
                object.drawString("Tr\u00f2 ch\u01a1i t\u1ea1m d\u1eebng", game.i.w() >> 1, (int)game.i.z(), 33);
                object.drawString("Ph\u1ea3n h\u1ed3i", game.i.w() - 2, game.i.x() - 2, 40);
                return;
            }
            case 4: {
                return;
            }
            case 15: {
                object.setColor(0xFFFFFF);
                object.fillRect(0, 0, (int)game.i.w(), (int)game.i.x());
                object.setColor(0xFFFFFF);
                object.setFont(game.i.D());
                if (this.l == null) break;
                object.drawImage(this.l, (int)game.i.y(), (int)game.i.z(), 3);
                return;
            }
            case 16: {
                object.setColor(game.i.C());
                object.fillRect(0, 0, (int)game.i.w(), (int)game.i.x());
                if (this.l == null) break;
                object.drawImage(this.l, (game.i.w() - this.l.getWidth()) / 2, (game.i.x() - this.l.getHeight()) / 2, 20);
                return;
            }
            case 6: {
                object.setColor(0);
                object.fillRect(0, 0, (int)game.i.w(), (int)game.i.x());
                object.setFont(game.i.D());
                object.setColor(0xFFFFFF);
                object.drawString(game.i.f(8), game.i.w() >> 1, game.i.z() - 12, 17);
                object.drawString(game.i.f(4), 2, game.i.x() - 2, 36);
                object.drawString(game.i.f(5), game.i.w() - 2, game.i.x() - 2, 40);
                object.setColor(16739328);
                object.drawString(game.i.f(9), game.i.w() >> 1, game.i.z() + 12, 17);
                return;
            }
            case 7: {
                return;
            }
            case 12: {
                game.k.a().b.a((Graphics)object);
                if (!game.i.v()) break;
                b.a().a((Graphics)object);
                game.g.U = false;
                return;
            }
            case 9: {
                Graphics graphics = object;
                object = this;
                if (game.g.U) {
                    byte by;
                    graphics.setColor(0);
                    graphics.fillRect(0, 0, (int)game.i.w(), (int)game.i.x());
                    if (n % 4 == 3) {
                        game.g.o().a((byte)1, (byte)-1, false);
                    } else {
                        game.g.o().a((byte)(n % 4), (byte)-1, false);
                    }
                    game.g.o().n = by = (byte)(n % 4);
                    game.g.o().a(graphics, j.a().a, j.a().b - n);
                } else {
                    graphics.setColor(0);
                    graphics.fillRect(0, 0, (int)game.i.w(), (int)game.i.x());
                }
                if (n < 148) {
                    n += o;
                }
                if (n > 148) {
                    n = 148;
                }
                if (!game.g.U) {
                    graphics.setColor(0);
                    graphics.fillRect(45, game.i.x() - 48, 150, 5);
                    graphics.setColor(7877410);
                    graphics.fillRect(46, game.i.x() - 47, 148, 3);
                    graphics.setColor(16707204);
                    graphics.fillRect(46, game.i.x() - 47, n, 3);
                    graphics.setColor(0xFFFFFF);
                    graphics.drawString(object.s, game.i.w() >> 1, game.i.x() - 70, 17);
                }
                return;
            }
            case 23: {
                Graphics graphics = object;
                object = this;
                graphics.setColor(0);
                graphics.fillRect(0, 0, (int)game.i.w(), (int)game.i.x());
                graphics.setColor(0xFFFFFF);
                graphics.drawString(object.s, game.i.w() >> 1, game.i.x() >> 1, 17);
                return;
            }
            case 10: {
                return;
            }
            case 8: 
            case 11: 
            case 13: 
            case 20: {
                if (this.m == null) break;
                this.m.b((Graphics)object);
            }
        }
    }

    private void a(String string) {
        v = string;
        if (this.g == 0) {
            return;
        }
        try {
            if (this.t != null) {
                if (this.o()) {
                    this.t.start();
                    return;
                }
            } else {
                game.i.a().getClass();
                InputStream inputStream = aj.a("/data/sound/" + string + ".mid");
                this.t = Manager.createPlayer((InputStream)inputStream, (String)"audio/midi");
                this.t.realize();
                this.u = (VolumeControl)this.t.getControl("VolumeControl");
                this.u.setLevel(this.g * 30);
                this.t.prefetch();
                this.t.setLoopCount(-1);
                this.t.start();
                inputStream.close();
            }
            return;
        }
        catch (MediaException mediaException) {
            h.a(mediaException, "startMusic");
            return;
        }
        catch (Exception exception) {
            h.a(exception, "/data/sound" + string + ".mid");
            return;
        }
    }

    private void n() {
        if (this.t == null) {
            return;
        }
        this.t.deallocate();
        this.t.close();
        this.t = null;
    }

    private boolean o() {
        try {
            this.t.prefetch();
            if (this.t.getState() == 300) {
                return true;
            }
        }
        catch (Exception exception) {}
        return false;
    }

    private void p() {
        if (this.u != null) {
            this.u.setLevel(this.g * 30);
        }
    }

    public final void h() {
        this.g = (byte)(this.g + 1);
        if (this.g > 3) {
            this.g = (byte)3;
        }
        if (this.g > 0) {
            this.p();
            this.a(v);
        }
    }

    public final void i() {
        this.g = (byte)(this.g - 1);
        if (this.g < 0) {
            this.g = 0;
        }
        if (this.g == 0) {
            this.n();
            return;
        }
        this.p();
    }
}

