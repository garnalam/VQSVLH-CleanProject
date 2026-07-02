/*
 * Decompiled with CFR 0.152.
 */
package game;

import game.b;
import game.c;
import game.d;
import game.g;
import game.i;
import game.k;
import java.util.Vector;

public final class h
implements i {
    private static h n;
    private an o;
    private ab p = ab.a();
    private g q;
    protected int a;
    protected int b;
    protected int c;
    private int r;
    protected int d;
    protected int e;
    private int s;
    protected int f;
    protected boolean g;
    private String t = "";
    private byte u;
    private byte v;
    private int w;
    protected int h;
    private int x;
    protected int i;
    private int[] y;
    private int[] z;
    public byte j = (byte)-1;
    private static String[] A;
    private static short[] B;
    private Vector C = new Vector();
    private int D = 0;
    private int E = 0;
    private int F = 0;
    private int G = 0;
    public int k = 0;
    boolean l = false;
    private int H;
    private int I;
    private int J = 0;
    private int K = 0;
    private int L = 0;
    private int M = 0;
    private String[] N = new String[]{"/data/ui/option.ui", "/data/ui/answer.ui", "/data/ui/wharf1.ui"};
    public short[] m = new short[]{9, 0, 120, 448, 9, 1, 136, 272, 9, 2, 208, 256, 9, 3, 80, 264, 9, 4, 112, 288, 9, 5, 40, 280, 9, 6, 136, 328, 9, 7, 104, 328};
    private String[] O = new String[]{"\u0110\u1ea1t \u0111\u01b0\u1ee3c 2000 kim ti\u1ec1n", "\u0110\u1ea1t \u0111\u01b0\u1ee3c 5 Phong \u1ea5n c\u1ea7u", "\u0110\u1ea1t \u0111\u01b0\u1ee3c 5 B\u00e1nh Sandwich", "\u0110\u1ea1t \u0111\u01b0\u1ee3c 2 Sinh m\u1ec7nh th\u1ea1ch", "\u0110\u1ea1t \u0111\u01b0\u1ee3c 2 huy hi\u1ec7u"};
    private short[][] P = new short[][]{{621, 622}, {623, 624}, {625, 626}, {627, 628}, {629, 630, 631, 632}};
    private short[][] Q = new short[][]{{5, 2, 112, 224, 2, 2, 5, 6, 1, 6, 0, 112, 224, 2, 0, 1, 0, 10}, {4, 0, 48, 176, 2, 2, 3, 6, 3, 6, 0, 112, 224, 2, 2, 1, 0, 10}, {3, 6, 288, 224, 3, 0, 3, 6, 3, 6, 0, 112, 224, 2, 2, 1, 0, 10}, {1, 5, 272, 128, 3, 0, 5, 6, 1, 6, 0, 112, 224, 2, 0, 1, 0, 10}, {1, 5, 272, 128, 3, 2, 0, 0, 0, 3, 6, 288, 224, 3, 0, 0, 0, 0, 4, 0, 48, 176, 2, 0, 0, 0, 0, 5, 2, 112, 224, 2, 2, 0, 0, 0}};
    private byte R;
    private byte S;
    private String[] T = new String[]{"D\u1eabn th\u01b0\u1edfng", "Ti\u1ebfn h\u00f3a", "D\u1ecb h\u00f3a", "T\u00e0i li\u1ec7u", "C\u00e1ch m\u1edf"};

    public static h a() {
        if (n == null) {
            n = new h();
        }
        return n;
    }

    public h() {
        if (this.q == null) {
            this.q = game.g.o();
        }
    }

    public final void b() {
        n = null;
        this.q = null;
    }

    public final void a(an v1) {
        if (this.o != null) {
            this.o = null;
        }
        this.o = v1;
        this.g = true;
    }

    public final void c() {
        this.p.a("/data/ui/world.ui", 257, this);
        this.u = 0;
    }

    public final void d() {
        this.p.a.a(5).a(true);
        this.p.a.a(7).a(true);
    }

    private void aS() {
        if (this.p.b("/data/ui/world.ui")) {
            for (int i1 = 1; i1 <= 7; ++i1) {
                if (i1 == 2 || i1 == 3 || i1 == 4) continue;
                this.p.a.a(i1).a(false);
            }
        }
    }

    public final void e() {
        if (this.u < 2 && !game.c.f && game.k.J && this.p.c("/data/ui/world.ui")) {
            if (ab.a(this.p.d("/data/ui/world.ui"), 4)) {
                ((af)this.p.d((String)"/data/ui/world.ui").a((int)6)).h().a = ((k)this.o).k;
                this.u = 1;
            } else if (this.u == 1 && ((af)this.p.d((String)"/data/ui/world.ui").a((int)1)).h().m.a().h() >= 5) {
                ((af)this.p.d((String)"/data/ui/world.ui").a((int)6)).h().a = "";
                this.u = (byte)2;
                game.k.J = false;
            }
        }
        this.f();
    }

    public final boolean f() {
        if (this.v < 2 && this.p.b("/data/ui/openbox.ui")) {
            if (ab.a(this.p.a, 3) && this.p.a.a((int)1).h().m.a().g() == 9) {
                this.p.a.a((int)2).h().a = this.t;
                if (this.p.a.a(2).h().b()) {
                    if (this.p.d()) {
                        this.p.a.a((int)1).h().m.a((byte)12, (byte)-1);
                        this.v = 1;
                    }
                    if (this.o.k(196640)) {
                        this.p.a.a((int)2).h().a = "";
                        this.v = (byte)2;
                        this.g = true;
                        this.aw();
                        return true;
                    }
                }
            } else if (this.v == 1) {
                this.p.a.a((int)2).h().a = "";
                if (this.p.d()) {
                    this.v = (byte)2;
                    this.g = true;
                    this.aw();
                    return true;
                }
            }
        }
        return this.g();
    }

    public final boolean g() {
        if (this.v < 2) {
            if (this.p.b("/data/ui/taskTip.ui")) {
                if (ab.a(this.p.a, 4) && this.p.a.a((int)1).h().m.a().g() == 10) {
                    this.p.a.a((int)2).h().a = this.t;
                    if (this.p.a.a(2).h().b()) {
                        if (this.p.d()) {
                            this.p.a.a((int)1).h().m.a((byte)13, (byte)-1);
                            this.v = 1;
                        }
                        if (this.o.k(196640)) {
                            this.v = (byte)2;
                            this.g = true;
                            this.br();
                            return true;
                        }
                    }
                } else if (this.v == 1) {
                    this.p.a.a((int)2).h().a = "";
                    if (this.p.d()) {
                        this.g = true;
                        this.v = (byte)2;
                        this.br();
                        return true;
                    }
                }
            }
            this.g = true;
        }
        return false;
    }

    public final void h() {
        this.p.a("/data/ui/transmit.ui", 257, this);
        ((al)this.p.a.a((int)0)).a.a = A.length;
        ((al)this.p.a.a((int)0)).a.a(1);
        this.aT();
    }

    private void aT() {
        this.w = ((al)this.p.a.a((int)0)).a.e;
        this.h = ((al)this.p.a.a((int)0)).a.f;
        for (int i1 = 0; i1 < 5; ++i1) {
            this.p.a.a((int)(i1 + 5)).h().a = A[i1 + this.w];
        }
        this.p.a.a(13).b(109 + this.h * 88 / A.length, this.p.a.a());
    }

    public final void i() {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            this.aT();
            return;
        }
        if (this.o.k(8448)) {
            this.p.a.b(1);
            this.aT();
            return;
        }
        if (this.o.k(196640)) {
            game.k.a().f = B[this.h * 5];
            game.k.a().g = B[this.h * 5 + 1];
            game.k.a().h = B[this.h * 5 + 2];
            game.k.a().i = B[this.h * 5 + 3];
            game.k.w = (byte)B[this.h * 5 + 4];
            game.k.a().j = -1;
            game.i.a().a((byte)9);
            return;
        }
        if (this.o.k(262144)) {
            this.o.a((byte)8);
            this.p.a("/data/ui/transmit.ui");
        }
    }

    public final boolean j() {
        return this.p.b("/data/ui/openbox.ui") || this.p.b("/data/ui/taskTip.ui");
    }

    public final void k() {
        String[] v1 = new String[]{"T\u00f9y th\u00e2n c\u1eeda h\u00e0ng", "S\u1ee7ng v\u1eadt", "L\u01b0ng bao", "\u0110\u1ed3 gi\u00e1m", "Nhi\u1ec7m v\u1ee5", "L\u01b0u d\u1eef li\u1ec7u"};
        this.aS();
        this.p.a("/data/ui/gamemenu.ui", 257, this);
        ((al)this.p.a.a((int)0)).a.a = 6;
        this.p.a.a((int)14).h().a = an.f(605 + this.b);
        this.p.a.a((int)15).h().a = v1[0];
        for (int i2 = 0; i2 < 5; ++i2) {
            this.p.a.a((int)(i2 + 5)).h().a = v1[i2 + 1];
        }
        ((al)this.p.a.a((int)0)).a.f = this.b;
        this.p.a.a((int)18).h().a = "" + this.q.G();
        this.p.a.a((int)19).h().a = "" + this.q.E();
        this.f = 0;
    }

    public final void l() {
        this.o.l();
        if (!an.b(this.b, 0) && !this.j() && this.o.k(4100)) {
            this.p.a.b(0);
        } else if (!an.b(this.b, 0) && !this.j() && this.o.k(8448)) {
            this.p.a.b(1);
        } else if (!this.j() && an.I() && this.o.k(196640)) {
            if (an.H() && !an.b(this.b, 0)) {
                return;
            }
            switch (this.b) {
                case 0: {
                    this.o.a((byte)14);
                    this.p.a("/data/ui/gamemenu.ui");
                    break;
                }
                case 1: {
                    this.c = 0;
                    this.o.m();
                    this.o.a((byte)7);
                    this.p.a("/data/ui/gamemenu.ui");
                    break;
                }
                case 2: {
                    this.o.m();
                    this.o.a((byte)8);
                    this.p.a("/data/ui/gamemenu.ui");
                    break;
                }
                case 3: {
                    this.c = 0;
                    this.o.a((byte)9);
                    this.p.a("/data/ui/gamemenu.ui");
                    break;
                }
                case 4: {
                    this.b = 0;
                    this.o.a((byte)10);
                    this.p.a("/data/ui/gamemenu.ui");
                    break;
                }
                case 5: {
                    this.p.a.a(11).a(false);
                    this.p.a.a(12).a(false);
                    this.o.a((byte)22);
                }
            }
        } else if (an.J() && this.o.k(262144)) {
            this.p.a("/data/ui/gamemenu.ui");
            this.o.a((byte)0);
        }
        this.g();
    }

    public final void m() {
        this.aS();
        this.p.a("/data/ui/gamesystem.ui", 257, this);
        ((al)this.p.a.a((int)0)).a.f = this.b;
        this.f = 0;
    }

    public final void n() {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            return;
        }
        if (this.o.k(8448)) {
            this.p.a.b(1);
            return;
        }
        if (this.o.k(196640)) {
            switch (this.b) {
                case 0: {
                    this.p.a("/data/ui/gamesystem.ui");
                    this.o.a((byte)0);
                    return;
                }
                case 1: {
                    this.o.a((byte)20);
                    this.p.a("/data/ui/gamesystem.ui");
                    return;
                }
                case 2: {
                    this.o.a((byte)21);
                    this.p.a("/data/ui/gamesystem.ui");
                    return;
                }
                case 3: {
                    if (this.f == 0) {
                        this.p.a("/data/ui/option.ui", 257, this);
                        ((al)this.p.a.a((int)0)).a.f = this.c = 1;
                        this.p.a.a((int)12).h().a = "";
                        this.p.a.a((int)13).h().a = "Kh\u00f4ng";
                        this.f = 1;
                        return;
                    }
                    switch (this.c) {
                        case 1: {
                            this.p.a("/data/ui/option.ui");
                            this.f = 0;
                            this.g = true;
                            return;
                        }
                        case 0: {
                            game.i.a().b = 0L;
                            game.i.a().a = 0L;
                            game.g.o().y = false;
                            game.i.a().a((byte)7);
                            this.p.a("/data/ui/gamesystem.ui");
                        }
                    }
                }
            }
            return;
        }
        if (this.o.k(262144)) {
            if (this.f == 0) {
                this.p.a("/data/ui/gamesystem.ui");
                this.o.a((byte)0);
                return;
            }
            if (this.f == 1) {
                this.g = true;
                this.p.a("/data/ui/option.ui");
                this.f = 0;
            }
        }
    }

    public final void o() {
        this.p.a("/data/ui/help1.ui", 257, this);
        this.b = 0;
        this.p.a.a(6).a(true);
        this.p.a.a(7).a(false);
        this.d(this.b);
    }

    private void d(int i1) {
        if (i1 == 0) {
            this.p.a.a((int)5).h().a = "Tr\u1ee3 gi\u00fap";
            this.p.a.a((int)8).h().a = "Nh\u1ea5n n\u00fat 2, 4, 6, 8 \u0111\u1ec3 di chuy\u1ec3n#nN\u00fat 5: c\u00f4ng k\u00edch, \u0111\u1ed1i tho\u1ea1i, x\u00e1c nh\u1eadn#nN\u00fat 1, 3: Xem nhi\u1ec7m v\u1ee5#nN\u00fat 9: l\u1ef1a ch\u1ecdn s\u1ee7ng v\u1eadt c\u01b0\u1ee1i#nN\u00fat 0: Xem b\u1ea3n \u0111\u1ed3#nN\u00fat m\u1ec1m tr\u00e1i: menu h\u1ec7 th\u1ed1ng#nN\u00fat m\u1ec1m ph\u1ea3i: menu tr\u00f2 ch\u01a1i";
            for (int i2 = 0; i2 < 28; ++i2) {
                this.p.a.a(i2 + 9).a(false);
            }
        } else if (i1 > 0) {
            this.p.a.a((int)8).h().a = "";
            for (int i2 = 0; i2 < 14; ++i2) {
                this.p.a.a(9 + (i2 << 1)).a(true);
                this.p.a.a(9 + (i2 << 1) + 1).a(true);
                if ((i1 - 1) * 14 + i2 < 26) {
                    this.p.a.a((int)(9 + (i2 << 1))).h().m = new m();
                    this.p.a.a((int)(9 + (i2 << 1))).h().m.a(0);
                    this.p.a.a((int)(9 + (i2 << 1))).h().m.a = (byte)2;
                    this.p.a.a((int)(9 + (i2 << 1))).h().m.a(325, false, (byte)-2);
                    this.p.a.a((int)(9 + (i2 << 1))).h().m.a((i1 - 1) * 14 + i2 + 1);
                    if ((i1 - 1) * 14 + i2 <= 10) {
                        this.p.a.a((int)(9 + (i2 << 1) + 1)).h().a = an.f(i2 + 311);
                        continue;
                    }
                    this.p.a.a((int)(9 + (i2 << 1) + 1)).h().a = an.f(333 + ((i1 - 1) * 14 + i2 - 11));
                    continue;
                }
                this.p.a.a(9 + (i2 << 1)).a(false);
                this.p.a.a(9 + (i2 << 1) + 1).a(false);
            }
        }
        this.p.a.a((int)39).h().a = i1 + 1 + "/3";
    }

    public final void p() {
        if (this.o.k(16400)) {
            --this.b;
            if (this.b <= 0) {
                this.b = 0;
            }
            this.d(this.b);
            return;
        }
        if (this.o.k(32832)) {
            ++this.b;
            if (this.b >= 2) {
                this.b = 2;
            }
            this.d(this.b);
            return;
        }
        if (this.o.k(262144)) {
            this.o.a((byte)0);
            this.p.a("/data/ui/help1.ui");
        }
    }

    public final void q() {
        this.p.a("/data/ui/help.ui", 257, this);
        this.p.a.a((int)5).h().a = "Quan t\u1ea1i";
        this.p.a.a((int)8).h().a = "T\u00ean tr\u00f2 ch\u01a1i: S\u1ee7ng v\u1eadt V\u01b0\u01a1ng qu\u1ed1c - Li\u1ec7t h\u1ecfa#nVi\u1ec7t h\u00f3a: BIGAME";
        this.p.a.a(6).a(true);
        this.p.a.a(7).a(false);
        for (int i1 = 9; i1 < 13; ++i1) {
            this.p.a.a(i1).a(false);
        }
    }

    public final void r() {
        if (this.o.k(262144)) {
            this.o.a((byte)0);
            this.p.a("/data/ui/help.ui");
        }
    }

    public final void s() {
        this.p.a("/data/ui/help.ui", 257, this);
        this.p.a.a((int)5).h().a = "T\u00f9y ch\u1ecdn";
        this.p.a.a((int)8).h().a = "";
        this.p.a.a(6).a(false);
        this.p.a.a(7).a(true);
        for (int i1 = 9; i1 < 13; ++i1) {
            this.p.a.a(i1).a(true);
        }
        this.aU();
    }

    private void aU() {
        for (int i1 = 1; i1 < 4; ++i1) {
            this.p.a.a((int)(i1 + 9)).h().j = i1 <= game.i.a().g ? -2148 : -8540732;
        }
    }

    public final void t() {
        if (this.o.k(16400)) {
            game.i.a().i();
            this.aU();
            return;
        }
        if (this.o.k(32832)) {
            game.i.a().h();
            this.aU();
            return;
        }
        if (this.o.k(131072)) {
            this.o.a((byte)0);
            this.p.a("/data/ui/help.ui");
        }
    }

    public final void u() {
        this.p.a("/data/ui/help1.ui", 257, this);
        this.p.a("/data/ui/gamesystem.ui");
        this.r = 0;
        this.p.a.a(6).a(true);
        this.p.a.a(7).a(false);
        this.d(this.r);
    }

    public final void v() {
        if (this.o.k(16400)) {
            --this.r;
            if (this.r <= 0) {
                this.r = 0;
            }
            this.d(this.r);
            return;
        }
        if (this.o.k(32832)) {
            ++this.r;
            if (this.r >= 2) {
                this.r = 2;
            }
            this.d(this.r);
            return;
        }
        if (this.o.k(262144)) {
            this.o.a((byte)13);
            this.p.a("/data/ui/help1.ui");
        }
    }

    public final void w() {
        this.s();
        this.p.a("/data/ui/gamesystem.ui");
    }

    public final void x() {
        if (this.o.k(16400)) {
            this.p.a.b(2);
            game.i.a().i();
            this.aU();
            return;
        }
        if (this.o.k(32832)) {
            this.p.a.b(3);
            game.i.a().h();
            this.aU();
            return;
        }
        if (this.o.k(131072)) {
            this.o.a((byte)13);
            this.p.a("/data/ui/help.ui");
        }
    }

    public final void y() {
        this.p.a("/data/ui/petstate.ui", 257, this);
        this.f = 0;
        if (this.q.O.size() > 6) {
            ((al)this.p.a.a((int)0)).a.a(1);
        } else {
            ((al)this.p.a.a((int)0)).a.a(-1);
        }
        this.p.a.a((int)2).h().a = "Ng\u00e2n h\u00e0ng S\u1ee7ng v\u1eadt";
        this.p.a.a(75).a(false);
        this.p.a.a(76).a(false);
        this.aV();
    }

    private void aV() {
        int i1;
        ((al)this.p.a.a((int)0)).a.a = this.q.O.size();
        this.w = ((al)this.p.a.a((int)0)).a.e;
        this.h = ((al)this.p.a.a((int)0)).a.f;
        ((al)this.p.a.a((int)0)).a.d = this.q.O.size() >= 6 ? 6 : this.q.O.size();
        if (this.h >= this.q.O.size()) {
            ((al)this.p.a.a((int)0)).a.f = this.h = this.q.O.size() - 1;
        }
        if (this.w > 0 && this.h - this.w < 5) {
            --this.w;
            ((al)this.p.a.a((int)0)).a.e = this.w;
        }
        for (i1 = 0; i1 < 6; ++i1) {
            if (this.w + i1 < this.q.O.size()) {
                int[] v2 = (int[])this.q.O.elementAt(this.w + i1);
                if (i1 == 0) {
                    this.p.a.a((int)(14 + i1 * 6)).h().a = "" + (this.w + i1 + 1);
                } else {
                    this.p.a.a((int)(15 + i1 * 6)).h().a = "" + (this.w + i1 + 1);
                }
                this.p.a.a((int)(16 + i1 * 6)).h().a = "#P" + v2[6] * 100 / game.b.a(v2[0], v2[1], v2[4], 1);
                this.p.a.a((int)(17 + i1 * 6)).h().a = "#P" + game.b.a((short)v2[7], (short)v2[1]);
                continue;
            }
            this.p.a.a((int)(16 + i1 * 6)).h().a = "#P0";
            this.p.a.a((int)(17 + i1 * 6)).h().a = "#P0";
        }
        int[] v1 = null;
        if (this.q.O.size() > 0) {
            v1 = (int[])this.q.O.elementAt(this.h);
        }
        if (v1 != null) {
            if (this.p.a.a((int)48).h().m != null) {
                this.p.a.a((int)48).h().m.d();
            } else {
                this.p.a.a((int)48).h().m = new m();
                this.p.a.a((int)48).h().m.a(0);
                this.p.a.a((int)48).h().m.a = (byte)3;
            }
            this.p.a.a((int)48).h().m.a(aq.a((byte)0, (short)v1[0], (byte)17), false, (byte)-1);
            this.p.a.a((int)51).h().a = an.f(aq.a((byte)0, (short)v1[0], (byte)0));
            this.p.a.a((int)52).h().a = an.f(365 + aq.a((byte)0, (short)v1[0], (byte)1));
            if (aq.a((byte)0, (short)v1[0], (byte)19) == -1) {
                this.p.a.a((int)62).h().a = "";
            } else if (aq.c[0][aq.a((byte)0, (short)v1[0], (byte)19)][2] == 1 || aq.c[0][aq.a((byte)0, (short)v1[0], (byte)19)][2] == 2) {
                this.p.a.a((int)62).h().a = "C\u00f3 th\u1ec3 ti\u1ebfn h\u00f3a";
            } else if (aq.c[0][aq.a((byte)0, (short)v1[0], (byte)19)][2] == 3) {
                this.p.a.a((int)62).h().a = "C\u00f3 th\u1ec3 d\u1ecb ho\u00e1";
            }
            this.p.a.a((int)61).h().a = game.b.y(v1[0]);
            if (this.p.a.a((int)59).h().m == null) {
                this.p.a.a((int)59).h().m = new m();
                this.p.a.a((int)59).h().m.a(0);
                this.p.a.a((int)59).h().m.a = (byte)2;
                this.p.a.a((int)59).h().m.a(258, false, (byte)-1);
            }
            if (v1[2] != -1) {
                this.p.a.a((int)59).h().m.a(aq.c[3][v1[2]][1]);
                this.p.a.a((int)60).h().a = an.f(aq.c[3][v1[2]][0]);
            } else {
                this.p.a.a((int)59).h().m.a(0);
                this.p.a.a((int)60).h().a = "";
            }
            this.p.a.a((int)65).h().a = "" + v1[1];
            this.p.a.a((int)66).h().a = "" + game.b.a(v1[0], v1[1], v1[4], 2);
            this.p.a.a((int)67).h().a = "" + game.b.a(v1[0], v1[1], v1[4], 3);
            this.p.a.a((int)68).h().a = "" + game.b.a(v1[0], v1[1], v1[4], 4);
            int i2 = v1[4];
            i1 = aq.a((byte)0, (short)v1[0], (byte)4) - 1;
            for (int i3 = 0; i3 < 5; ++i3) {
                this.p.a.a(74 - i3).a(true);
                this.p.a.a((int)(74 - i3)).h().m.a = (byte)3;
                if (i3 > i1) {
                    this.p.a.a(74 - i3).a(false);
                    continue;
                }
                if (i2 > 0) {
                    this.p.a.a((int)(74 - i3)).h().m.a((byte)14, (byte)-1);
                    --i2;
                    continue;
                }
                this.p.a.a((int)(74 - i3)).h().m.a((byte)16, (byte)-1);
            }
            if (this.b == 1) {
                this.p.a.a((int)64).h().a = "L\u1ea5y ra";
                return;
            }
            if (this.b == 2) {
                this.p.a.a((int)64).h().a = "Ph\u00f3ng sinh";
            }
        }
    }

    public final void z() {
        if (this.f == 0 && this.o.k(4100)) {
            this.p.a.b(0);
            this.aV();
        } else if (this.f == 0 && this.o.k(8448)) {
            this.p.a.b(1);
            this.aV();
        }
        if (this.f == 0) {
            if (this.o.k(196640)) {
                if (this.b == 1) {
                    if (this.q.A >= 6) {
                        this.p.a("/data/ui/msgwarm.ui", 257, this);
                        this.a("Ba l\u00f4 S\u1ee7ng v\u1eadt \u0111\u00e3 \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                        this.f = 1;
                    } else {
                        if (this.q.O.size() <= 0) {
                            return;
                        }
                        this.q.r(this.h);
                        if (this.q.O.size() <= 0) {
                            this.o.a((byte)16);
                            this.p.a("/data/ui/petstate.ui");
                        } else {
                            this.aV();
                        }
                    }
                } else if (this.b == 2) {
                    if (this.q.O.size() <= 0) {
                        return;
                    }
                    int[] v1 = (int[])this.q.O.elementAt(this.h);
                    if (aq.a((byte)0, (short)v1[0], (byte)22) == 2) {
                        this.f = 2;
                        this.E();
                        this.a("Th\u1ea7n th\u00fa kh\u00f4ng th\u1ec3 ph\u00f3ng sinh", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    } else {
                        this.f = 1;
                        this.p.a("/data/ui/msgconfirm.ui", 257, this);
                        this.b("B\u1ea1n mu\u1ed1n ph\u00f3ng sinh s\u1ee7ng v\u1eadt n\u00e0y?", "X\u00e1c nh\u1eadn");
                    }
                }
            } else if (this.o.k(786432)) {
                this.o.a((byte)16);
                this.p.a("/data/ui/petstate.ui");
            }
        } else if (this.f > 0) {
            if (this.o.k(196640) && this.b == 1 || this.o.k(131072) && this.b == 2) {
                if (this.b == 1) {
                    this.p.a("/data/ui/msgwarm.ui");
                    this.f = 0;
                } else if (this.b == 2) {
                    if (this.f == 1) {
                        this.p.a("/data/ui/msgconfirm.ui");
                        int[] v1 = (int[])this.q.O.elementAt(this.h);
                        this.q.l(v1[2]);
                        this.q.q(this.h);
                        this.aV();
                    } else if (this.f == 2) {
                        this.F();
                    }
                    this.f = 0;
                }
            } else if (this.o.k(786432)) {
                if (this.b == 1) {
                    return;
                }
                this.p.a("/data/ui/msgconfirm.ui");
                this.f = 0;
            }
        }
        this.g = true;
    }

    public final void A() {
        this.aS();
        this.p.a("/data/ui/shop.ui", 257, this);
        this.b = 0;
        this.p.a.a((int)5).h().a = "Ng\u00e2n h\u00e0ng S\u1ee7ng v\u1eadt";
        this.p.a.a((int)6).h().a = "G\u1edfi l\u1ea1i";
        this.p.a.a((int)7).h().a = "L\u1ea5y ra";
        this.p.a.a((int)9).h().a = "Ph\u00f3ng sinh";
    }

    public final void B() {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            return;
        }
        if (this.o.k(8448)) {
            this.p.a.b(1);
            return;
        }
        if (this.o.k(196640)) {
            switch (this.b) {
                case 0: {
                    this.c = 0;
                    this.o.a((byte)7);
                    this.p.a("/data/ui/shop.ui");
                    return;
                }
                case 1: {
                    this.o.a((byte)15);
                    this.p.a("/data/ui/shop.ui");
                    return;
                }
                case 2: {
                    this.o.a((byte)15);
                    this.p.a("/data/ui/shop.ui");
                    return;
                }
                case 3: {
                    game.c.e = true;
                    this.p.a("/data/ui/shop.ui");
                    this.o.a((byte)0);
                }
            }
            return;
        }
        if (this.o.k(262144)) {
            game.c.e = true;
            this.p.a("/data/ui/shop.ui");
            this.o.a((byte)0);
        }
    }

    public final void C() {
        this.aS();
        this.p.a("/data/ui/shop.ui", 257, this);
        this.b = 0;
    }

    public final void D() {
        this.o.l();
        if (!an.b(this.b, 0) && !this.j() && this.f == 0 && this.o.k(4100) && this.aW()) {
            this.p.a.b(0);
        } else if (!an.b(this.b, 0) && !this.j() && this.f == 0 && this.o.k(8448) && this.aW()) {
            this.p.a.b(1);
        } else if (this.aW() && !this.j() && an.I() && this.o.k(196640)) {
            if (an.H() && !an.b(this.b, 0)) {
                return;
            }
            switch (this.b) {
                case 0: {
                    this.o.m();
                    this.o.a((byte)2);
                    this.p.a("/data/ui/shop.ui");
                    break;
                }
                case 1: {
                    this.o.a((byte)3);
                    this.p.a("/data/ui/shop.ui");
                    break;
                }
                case 2: {
                    if (this.f == 0) {
                        if (game.g.o().A() == -1) {
                            this.f = 6;
                            this.E();
                            this.a("To\u00e0n b\u1ed9 tr\u1ea1ng th\u00e1i \u0111\u00e3 \u0111\u1ea7y, kh\u00f4ng c\u1ea7n kh\u00f4i ph\u1ee5c", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                            break;
                        }
                        this.f = 3;
                        for (int i1 = 0; i1 < this.q.A; ++i1) {
                            this.q.z[i1].I();
                        }
                        this.b("Ba l\u00f4 s\u1ee7ng v\u1eadt tr\u1ea1ng th\u00e1i to\u00e0n b\u1ed9 kh\u00f4i ph\u1ee5c");
                        break;
                    }
                    if (this.f == 1) {
                        int i1 = game.g.o().A();
                        if (game.g.o().t(i1)) {
                            this.f = 3;
                            game.g.o().s(-i1);
                            for (i1 = 0; i1 < this.q.A; ++i1) {
                                this.q.z[i1].I();
                            }
                            this.b("Ba l\u00f4 s\u1ee7ng v\u1eadt tr\u1ea1ng th\u00e1i to\u00e0n b\u1ed9 kh\u00f4i ph\u1ee5c");
                        } else {
                            this.f = 2;
                            this.E();
                            this.a("Kim ti\u1ec1n ch\u01b0a \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                        }
                        this.p.a("/data/ui/msgRecover.ui");
                        break;
                    }
                    if (this.f == 2) {
                        this.o.a((byte)102);
                    }
                    this.f = 0;
                    this.F();
                    break;
                }
                case 3: {
                    game.c.e = true;
                    this.p.a("/data/ui/shop.ui");
                    this.o.a((byte)0);
                }
            }
        } else if (!this.j() && this.o.k(262144) && an.J() && this.aW()) {
            if (this.f == 1) {
                this.p.a("/data/ui/shop.ui", 257, this);
                this.p.a("/data/ui/msgRecover.ui");
                this.f = 0;
                this.b = 0;
            } else if (this.f == 0) {
                game.c.e = true;
                this.p.a("/data/ui/shop.ui");
                this.o.a((byte)0);
            }
        }
        if (this.f == 3 && this.ax()) {
            this.f = 4;
            this.p.a("/data/ui/shop.ui", 257, this);
            this.H();
            this.a("\u0110ang l\u01b0u...");
            this.J();
        } else if (this.f == 4 && ((k)this.o).k()) {
            this.a("L\u01b0u th\u00e0nh c\u00f4ng");
            this.f = 5;
        } else if (this.f == 5) {
            this.I();
            this.b = 0;
            this.f = 0;
        }
        this.f();
        this.g = true;
    }

    public final void a(int i1, byte i2) {
        this.p.a("/data/ui/shopbuy.ui", 257, this);
        this.b = 0;
        this.f = 0;
        ((al)this.p.a.a((int)0)).a.a = aq.c[i1].length;
        ((al)this.p.a.a((int)0)).a.a(1);
        this.p.a.a((int)5).h().a = "Mua";
        if (this.o instanceof k) {
            this.p.a.a(57).a(true);
            this.p.a.a(58).a(true);
            this.p.a.a((int)57).h().a = "Mua s\u1eafm";
            this.p.a.a((int)58).h().a = "Ph\u1ea3n h\u1ed3i";
            this.p.a.a(39).a(false);
            this.p.a.a(40).a(false);
        } else if (this.o instanceof d) {
            this.p.a.a(57).a(false);
            this.p.a.a(58).a(false);
            this.p.a.a(39).a(true);
            this.p.a.a(40).a(true);
            this.p.a.a((int)39).h().a = "Mua s\u1eafm";
            this.p.a.a((int)40).h().a = "Ph\u1ea3n h\u1ed3i";
        }
        this.b(i1, i2);
    }

    private void b(int i1, byte i2) {
        this.w = ((al)this.p.a.a((int)0)).a.e;
        this.h = ((al)this.p.a.a((int)0)).a.f;
        for (int i3 = 0; i3 < 5; ++i3) {
            if (this.p.a.a((int)(i3 + 51)).h().m == null) {
                this.p.a.a((int)(i3 + 51)).h().m = new m();
                this.p.a.a((int)(i3 + 51)).h().m.a(0);
                this.p.a.a((int)(i3 + 51)).h().m.a = (byte)2;
                this.p.a.a((int)(i3 + 51)).h().m.a(258, false, (byte)-1);
            }
            this.p.a.a((int)(i3 + 51)).h().m.a(aq.c[i1][this.w + i3][1]);
            this.p.a.a((int)(14 + i3 * 5)).h().a = an.f(aq.c[i1][this.w + i3][0]);
            if (this.o instanceof k) {
                if (this.j == 1 || this.j == 3) {
                    this.p.a.a((int)(15 + i3 * 5)).h().a = "" + aq.c[i1][this.w + i3][3];
                } else if (this.j == 2) {
                    this.p.a.a((int)(15 + i3 * 5)).h().a = aq.c[i1][this.w + i3][4] == 0 ? "" + aq.c[i1][this.w + i3][3] * 3 / 2 : "" + aq.c[i1][this.w + i3][3];
                }
            } else {
                this.p.a.a((int)(15 + i3 * 5)).h().a = i2 == 0 && i1 == 4 && this.w + i3 == 0 ? "" + aq.c[i1][this.w + i3][3] : "" + (aq.c[i1][this.w + i3][3] << 1);
            }
            if (aq.c[i1][this.w + i3][4] == 0) {
                this.p.a.a((int)(i3 + 45)).h().m.a(84);
                continue;
            }
            if (aq.c[i1][this.w + i3][4] == 1) {
                this.p.a.a((int)(i3 + 45)).h().m.a(83);
                continue;
            }
            if (aq.c[i1][this.w + i3][4] != 2) continue;
            this.p.a.a((int)(i3 + 45)).h().m.a(74);
        }
        this.p.a.a((int)56).h().a = an.f(aq.c[i1][this.h][2]);
        this.p.a.a((int)43).h().a = "" + this.q.G();
        this.p.a.a((int)44).h().a = "" + this.q.E();
        this.p.a.a(38).b(102 + this.h * 84 / aq.c[i1].length, this.p.a.a());
    }

    public final void a(byte i1, byte i2) {
        this.o.l();
        if (!an.b(this.b, 0) && this.f <= 1 && this.o.k(4100) && !this.j()) {
            this.p.a.b(0);
            if (this.f == 0) {
                this.b((int)i1, i2);
            }
        } else if (!an.b(this.b, 0) && this.f <= 1 && this.o.k(8448) && !this.j()) {
            this.p.a.b(1);
            if (this.f == 0) {
                this.b((int)i1, i2);
            }
        } else if (this.f == 1 && this.o.k(16400) && this.c > 0 && !this.j()) {
            --this.c;
            if (this.c <= 0) {
                this.c = 99 - this.q.a(this.h, i2);
            }
            this.a(this.c, this.c * aq.c[i1][this.h][3], (int)aq.c[i1][this.h][4], (int)i1);
        } else if (this.f == 1 && this.o.k(32832) && !this.j()) {
            ++this.c;
            if (this.c > 99 - this.q.a(this.h, i2)) {
                this.c = 1;
            }
            this.a(this.c, this.c * aq.c[i1][this.h][3], (int)aq.c[i1][this.h][4], (int)i1);
        } else if (an.I() && this.o.k(196640) && !this.j()) {
            if (an.H() && !an.b(this.b, 0)) {
                return;
            }
            if (aq.c[i1][this.h][4] == 2) {
                if (this.f == 0) {
                    if (!this.q.a(this.h, 1, (byte)0)) {
                        this.f = 3;
                        this.E();
                        this.a("\u0110\u1ea1o c\u1ee5 \u0111\u00e3 \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    } else {
                        this.o.a((byte)101);
                    }
                } else {
                    this.f = 0;
                    this.p.a("/data/ui/msgwarm.ui");
                }
            } else if (i2 == 2 && this.h < 12) {
                this.c = 1;
                if (this.q.a(this.h, this.c, i2)) {
                    if (this.f == 0) {
                        this.r = 0;
                        this.b(i1, i2);
                    } else if (this.f > 0) {
                        if (this.f == 4) {
                            this.o.a((byte)104);
                        } else if (this.f == 3) {
                            this.o.a((byte)102);
                        }
                        this.f = 0;
                        this.p.a("/data/ui/msgwarm.ui");
                    }
                } else if (this.f == 0) {
                    this.f = 2;
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("\u0110\u1ea1o c\u1ee5 n\u00e0y \u0111\u00e3 \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                } else {
                    this.o.m();
                    this.f = 0;
                    this.p.a("/data/ui/msgwarm.ui");
                    this.b((int)i1, i2);
                }
            } else if (this.q.a(this.h, this.c, i2)) {
                if (this.f == 0) {
                    this.f = 1;
                    this.p.a("/data/ui/msgyn.ui", 257, this);
                    this.c = 1;
                    this.r = 0;
                    this.a(this.c, this.c * aq.c[i1][this.h][3], (int)aq.c[i1][this.h][4], (int)i1);
                } else if (this.f == 1) {
                    this.b(i1, i2);
                } else if (this.f == 2) {
                    game.k.a().M.i();
                    this.f = 0;
                    this.c = 0;
                    this.p.a("/data/ui/msgwarm.ui");
                    this.b((int)i1, i2);
                } else {
                    if (this.f == 4) {
                        this.p.a("/data/ui/msgyn.ui");
                        this.o.a((byte)104);
                    } else if (this.f == 3) {
                        this.p.a("/data/ui/msgyn.ui");
                        this.o.a((byte)102);
                    }
                    this.f = 0;
                    this.c = 0;
                    this.p.a("/data/ui/msgwarm.ui");
                }
            } else if (this.f == 0) {
                this.f = 2;
                this.p.a("/data/ui/msgwarm.ui", 257, this);
                this.a("\u0110\u1ea1o c\u1ee5 n\u00e0y \u0111\u00e3 \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
            } else {
                this.f = 0;
                this.p.a("/data/ui/msgwarm.ui");
            }
        } else if (this.o.k(262144) && !this.j() && an.J()) {
            if (this.f == 0) {
                if (this.o instanceof k) {
                    if (this.j == 1) {
                        this.o.a((byte)1);
                    } else if (this.j == 2) {
                        this.o.a((byte)14);
                    } else if (this.j == 3) {
                        this.o.a((byte)27);
                    }
                    this.p.a("/data/ui/shopbuy.ui");
                } else {
                    this.p.a("/data/ui/shopbuy.ui");
                    this.o.a((byte)20);
                }
            } else if (this.f == 1) {
                this.f = 0;
                this.c = 0;
                this.p.a("/data/ui/msgyn.ui");
            }
        }
        this.f();
    }

    private void b(byte i1, byte i2) {
        block29: {
            block25: {
                block26: {
                    int i3;
                    block28: {
                        block27: {
                            if (this.o instanceof k && (this.j == 1 || this.j == 3) && this.q.b(this.h, this.c * aq.c[i1][this.h][3], (int)i1) || this.j == 2 && (aq.c[i1][this.h][4] == 0 && this.q.b(this.h, this.c * aq.c[i1][this.h][3] * 3 / 2, (int)i1) || aq.c[i1][this.h][4] != 0 && this.q.b(this.h, this.c * aq.c[i1][this.h][3], (int)i1))) {
                                if (this.r == 0) {
                                    this.q.c(this.h, this.c, i2);
                                    if (aq.c[i1][this.h][4] == 0) {
                                        if (this.j == 1 || this.j == 3) {
                                            this.q.s(-this.c * aq.c[i1][this.h][3]);
                                        } else if (this.j == 2) {
                                            this.q.s(-this.c * aq.c[i1][this.h][3] * 3 / 2);
                                        }
                                    } else if (this.j == 1 || this.j == 3) {
                                        this.q.u(-this.c * aq.c[i1][this.h][3]);
                                    } else if (this.j == 2) {
                                        this.q.u(-this.c * aq.c[i1][this.h][3]);
                                    }
                                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                                    if (i1 == 3 && this.h == 17) {
                                        this.a("\u0110\u00e3 th\u00e0nh c\u00f4ng mua s\u1eafm #2" + an.f(aq.c[i1][this.h][0]) + " * " + 5 * this.c, "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                    } else {
                                        this.a("\u0110\u00e3 th\u00e0nh c\u00f4ng mua s\u1eafm #2" + an.f(aq.c[i1][this.h][0]) + " * " + this.c, "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                    }
                                    this.f = 2;
                                    this.c = 1;
                                } else {
                                    this.f = 0;
                                }
                                this.p.a("/data/ui/msgyn.ui");
                                return;
                            }
                            if (this.o instanceof d && this.q.b(this.h, this.c * aq.c[i1][this.h][3] << 1, (int)i1)) {
                                if (this.r == 0) {
                                    this.q.c(this.h, this.c, i2);
                                    if (aq.c[i1][this.h][4] == 0) {
                                        this.q.s(-this.c * aq.c[i1][this.h][3] << 1);
                                    } else {
                                        this.q.u(-this.c * aq.c[i1][this.h][3] << 1);
                                    }
                                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                                    if (i1 == 3 && this.h == 17) {
                                        this.a("\u0110\u00e3 th\u00e0nh c\u00f4ng mua s\u1eafm #2" + an.f(aq.c[i1][this.h][0]) + " * " + 5 * this.c, "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                    } else {
                                        this.a("\u0110\u00e3 th\u00e0nh c\u00f4ng mua s\u1eafm #2" + an.f(aq.c[i1][this.h][0]) + " * " + this.c, "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                    }
                                    this.f = 2;
                                    this.c = 1;
                                } else {
                                    this.f = 0;
                                }
                                this.p.a("/data/ui/msgyn.ui");
                                return;
                            }
                            if (this.r != 0) break block25;
                            this.p.a("/data/ui/msgwarm.ui", 257, this);
                            if (aq.c[i1][this.h][4] == 0) {
                                this.f = 3;
                                this.a("Kim ti\u1ec1n ch\u01b0a \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                            } else {
                                this.f = 4;
                                this.a("S\u1ed1 l\u01b0\u1ee3ng Huy ch\u01b0\u01a1ng ch\u01b0a \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                            }
                            if (!(this.o instanceof k)) break block26;
                            if (this.j != 1 && this.j != 3) break block27;
                            i3 = this.c * aq.c[i1][this.h][3];
                            break block28;
                        }
                        if (this.j != 2) break block29;
                        i3 = aq.c[i1][this.h][4] == 0 ? this.c * aq.c[i1][this.h][3] * 3 / 2 : this.c * aq.c[i1][this.h][3];
                    }
                    this.b(new int[]{i1, i2, this.h, aq.c[i1][this.h][4], i3, this.c});
                    break block29;
                }
                if (this.o instanceof d) {
                    int i3 = this.c * aq.c[i1][this.h][3] << 1;
                    this.b(new int[]{i1, i2, this.h, aq.c[i1][this.h][4], i3, this.c});
                }
                break block29;
            }
            this.f = 0;
        }
        this.p.a("/data/ui/msgyn.ui");
    }

    private void b(int[] v1) {
        if (this.C == null) {
            this.C = new Vector();
        } else {
            this.C.removeAllElements();
        }
        this.C.addElement(v1);
    }

    private void a(int i1, int i2, int i3, int i4) {
        this.p.a.a((int)9).h().a = i4 == 3 && this.h == 17 ? "" + i1 * 5 : "" + i1;
        if (this.o instanceof k) {
            if (this.j == 1 || this.j == 3) {
                this.p.a.a((int)11).h().a = "" + i2;
            } else if (this.j == 2) {
                this.p.a.a((int)11).h().a = i3 == 0 ? "" + i2 * 3 / 2 : "" + i2;
            }
        } else {
            this.p.a.a((int)11).h().a = "" + (i2 << 1);
        }
        if (i3 == 0) {
            this.p.a.a((int)12).h().m.a(84);
            return;
        }
        if (i3 == 1) {
            this.p.a.a((int)12).h().m.a(83);
        }
    }

    public final void E() {
        this.p.a("/data/ui/msgwarm.ui", 257, this);
    }

    public final void F() {
        this.p.a("/data/ui/msgwarm.ui");
    }

    public final boolean G() {
        return !this.p.b("/data/ui/msgwarm.ui");
    }

    public final void a(String v1, String v2) {
        this.p.a.a((int)6).h().a = v2;
        this.p.a.a((int)7).h().a = v1;
    }

    public final void H() {
        this.p.a("/data/ui/msgtip.ui", 257, this);
    }

    public final void I() {
        this.p.a("/data/ui/msgtip.ui");
    }

    private boolean aW() {
        return !this.p.b("/data/ui/msgtip.ui");
    }

    public final void a(String v1) {
        this.p.a.a((int)2).h().a = v1;
    }

    public final void J() {
        this.p.a.a(3).a(false);
        this.p.a.a(4).a(false);
    }

    public final void K() {
        if (this.f == 0) {
            if (this.o.k(196640)) {
                this.f = 1;
                this.a("\u0110ang l\u01b0u...");
                this.J();
                return;
            }
            if (this.o.k(262144)) {
                this.b = 5;
                this.o.a((byte)6);
                this.p.a("/data/ui/msgtip.ui");
                this.f = 0;
                return;
            }
        } else if (this.f == 1) {
            if (((k)this.o).k()) {
                this.a("L\u01b0u th\u00e0nh c\u00f4ng");
                this.f = 2;
                return;
            }
        } else if (this.f == 2) {
            this.p.a("/data/ui/msgtip.ui");
            this.p.a("/data/ui/gamemenu.ui");
            this.o.a((byte)0);
            this.f = 0;
        }
    }

    private void b(String v1, String v2) {
        this.p.a.a((int)2).h().a = v2;
        this.p.a.a((int)4).h().a = v1;
    }

    public final void L() {
        this.p.a("/data/ui/shopbuy.ui", 257, this);
        this.b = 0;
        this.f = 0;
        this.p.a.a((int)5).h().a = "B\u00e1n ra";
        this.p.a.a((int)39).h().a = "";
        this.p.a.a((int)40).h().a = "";
        this.p.a.a((int)57).h().a = "B\u00e1n \u0111i";
        this.p.a.a((int)58).h().a = "Ph\u1ea3n h\u1ed3i";
        this.q.x();
        this.aX();
    }

    private void aX() {
        if (this.q.S.size() > 5) {
            ((al)this.p.a.a((int)0)).a.a(1);
        } else {
            ((al)this.p.a.a((int)0)).a.a(0);
        }
        ((al)this.p.a.a((int)0)).a.a = this.q.S.size();
        this.w = ((al)this.p.a.a((int)0)).a.e;
        this.h = ((al)this.p.a.a((int)0)).a.f;
        if (this.h >= this.q.S.size()) {
            ((al)this.p.a.a((int)0)).a.f = this.h = this.q.S.size() - 1;
        }
        if (this.w > 0 && this.h - this.w < 4) {
            --this.w;
            ((al)this.p.a.a((int)0)).a.e = this.w;
        }
        for (int i1 = 0; i1 < 5; ++i1) {
            if (this.w + i1 < this.q.S.size()) {
                int i2 = ((int[])this.q.S.elementAt(this.w + i1))[0];
                if (this.p.a.a((int)(i1 + 51)).h().m == null) {
                    this.p.a.a((int)(i1 + 51)).h().m = new m();
                    this.p.a.a((int)(i1 + 51)).h().m.a(0);
                    this.p.a.a((int)(i1 + 51)).h().m.a = (byte)2;
                    this.p.a.a((int)(i1 + 51)).h().m.a(258, false, (byte)-1);
                }
                this.p.a.a((int)(i1 + 51)).h().m.a(aq.c[4][i2][1]);
                this.p.a.a((int)(14 + i1 * 5)).h().a = an.f(aq.c[4][i2][0]);
                this.p.a.a((int)(15 + i1 * 5)).h().a = "" + aq.c[4][i2][3] / 2;
                if (aq.c[4][i2][4] == 0) {
                    this.p.a.a((int)(i1 + 45)).h().m.a(84);
                    continue;
                }
                if (aq.c[4][i2][4] == 1) {
                    this.p.a.a((int)(i1 + 45)).h().m.a(83);
                    continue;
                }
                if (aq.c[4][i2][4] != 2) continue;
                this.p.a.a((int)(i1 + 45)).h().m.a(74);
                continue;
            }
            if (this.p.a.a((int)(i1 + 51)).h().m != null) {
                this.p.a.a((int)(i1 + 51)).h().m.d();
            }
            this.p.a.a((int)(14 + i1 * 5)).h().a = "";
            this.p.a.a((int)(15 + i1 * 5)).h().a = "";
            this.p.a.a((int)(i1 + 45)).h().m.a(86);
        }
        this.p.a.a((int)56).h().a = this.q.S.size() > 0 ? an.f(aq.c[4][((int[])this.q.S.elementAt(this.h))[0]][2]) : "";
        if (this.q.S.size() <= 0) {
            return;
        }
        this.p.a.a((int)43).h().a = "" + this.q.G();
        this.p.a.a((int)44).h().a = "" + this.q.E();
        this.p.a.a(38).b(102 + this.h * 84 / this.q.S.size(), this.p.a.a());
    }

    /*
     * Enabled aggressive block sorting
     */
    public final void M() {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            return;
        }
        if (this.o.k(8448)) {
            this.p.a.b(1);
            return;
        }
        if (this.f == 1 && this.o.k(16400) && this.c > 0) {
            int[] v1 = (int[])this.q.S.elementAt(this.h);
            --this.c;
            if (this.c <= 0) {
                this.c = this.q.a(v1[0], (byte)0);
            }
            this.a(this.c, this.c * aq.c[4][v1[0]][3] / 2, (int)aq.c[4][v1[0]][4], 4);
            return;
        }
        if (this.f == 1 && this.o.k(32832)) {
            int[] v1 = (int[])this.q.S.elementAt(this.h);
            ++this.c;
            if (this.c > this.q.a(v1[0], (byte)0)) {
                this.c = 1;
            }
            this.a(this.c, this.c * aq.c[4][v1[0]][3] / 2, (int)aq.c[4][v1[0]][4], 4);
            return;
        }
        if (this.o.k(196640) && this.q.S.size() > 0) {
            int[] v1 = (int[])this.q.S.elementAt(this.h);
            if (this.f == 0) {
                this.f = 1;
                this.p.a("/data/ui/msgyn.ui", 257, this);
                this.c = 1;
                this.r = 0;
                this.a(this.c, this.c * aq.c[4][v1[0]][3] / 2, (int)aq.c[4][v1[0]][4], 4);
                return;
            }
            if (this.r != 0) {
                this.p.a("/data/ui/msgyn.ui");
                this.f = 0;
                return;
            }
            this.q.d(v1[0], this.c, (byte)0);
            this.q.s(this.c * aq.c[4][v1[0]][3] / 2);
        } else {
            if (!this.o.k(262144)) return;
            if (this.f == 0) {
                this.o.a((byte)1);
                this.p.a("/data/ui/shopbuy.ui");
                ((al)this.p.a.a((int)0)).a.f = this.b = 1;
                return;
            }
        }
        this.f = 0;
        this.p.a("/data/ui/msgyn.ui");
        this.q.x();
        this.aX();
    }

    public final void N() {
        this.p.a("/data/ui/record.ui", 257, this);
        this.p.a("/data/ui/gamemenu.ui");
        this.p.a.a((int)14).h().a = "" + (this.q.A + this.q.O.size());
        this.p.a.a((int)17).h().a = "" + this.q.F;
        this.p.a.a((int)20).h().a = "" + this.q.H;
        this.p.a.a((int)26).h().a = "" + this.q.G;
        int i1 = 0;
        for (byte i2 = 0; i2 < this.q.B.length; i2 = (byte)(i2 + 1)) {
            if (this.q.c(i2, (byte)0) != 2) continue;
            ++i1;
        }
        this.p.a.a((int)29).h().a = "" + i1;
        long j2 = game.i.a().d + game.i.a().e - game.i.a().f;
        k k2 = this.p.a.a(31).h();
        game.k.a();
        k2.a = game.k.a(j2)[1];
        ((al)this.p.a.a((int)0)).b.f = this.c;
        this.b = 0;
        this.f = 0;
        this.g = true;
    }

    public final void O() {
        if (this.o.k(16400)) {
            this.p.a.b(2);
            this.g = true;
        } else if (this.o.k(32832)) {
            this.p.a.b(3);
            this.g = true;
        } else if (this.o.k(196640)) {
            if (this.f == 0) {
                switch (this.c) {
                    case 0: {
                        if (game.g.o().k(5)) {
                            this.o.a((byte)11);
                            break;
                        }
                        this.E();
                        this.a("Kh\u00f4ng \u0111\u1ea1t \u0111\u01b0\u1ee3c s\u1ee7ng v\u1eadt s\u00e1ch tranh \u0111\u1ea1o c\u1ee5", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                        this.f = 1;
                        break;
                    }
                    case 1: {
                        this.o.a((byte)12);
                    }
                }
            } else {
                this.f = 0;
                this.F();
            }
        } else if (this.o.k(262144) && this.f == 0) {
            this.b = 3;
            this.o.a((byte)6);
            this.p.a("/data/ui/record.ui");
        }
        this.g = true;
    }

    public final void P() {
        this.p.a("/data/ui/petmap.ui", 257, this);
        this.p.a("/data/ui/record.ui");
        this.b = 0;
        this.c = 0;
        this.f = 0;
        ((al)this.p.a.a((int)0)).a.a(1);
        this.aZ();
        this.g = true;
    }

    private void aY() {
        ((al)this.p.a.a((int)0)).a.e = 0;
        ((al)this.p.a.a((int)0)).a.f = 0;
    }

    private void aZ() {
        int i2;
        ((al)this.p.a.a((int)0)).a.a = this.q.X[this.b];
        this.w = ((al)this.p.a.a((int)0)).a.e;
        this.h = ((al)this.p.a.a((int)0)).a.f;
        int i1 = aq.c[0][this.q.W[this.b] + this.h][17];
        if (this.q.a((byte)this.b, this.h + this.q.W[this.b]) > 0) {
            this.p.a.a(21).a(true);
            if (this.p.a.a((int)21).h().m != null) {
                this.p.a.a((int)21).h().m.d();
            } else {
                this.p.a.a((int)21).h().m = new m();
                this.p.a.a((int)21).h().m.a(0);
                this.p.a.a((int)21).h().m.a = (byte)3;
            }
            this.p.a.a((int)21).h().m.a(i1, false, (byte)-1);
            this.p.a.a((int)21).h().m.b();
        } else {
            this.p.a.a(21).a(false);
        }
        i1 = 0;
        for (i2 = 0; i2 < this.q.X[this.b]; ++i2) {
            if (this.q.a((byte)this.b, this.q.W[this.b] + i2) != 2) continue;
            ++i1;
        }
        for (i2 = 0; i2 < 5; ++i2) {
            if (this.p.a.a((int)(i2 + 44)).h().m == null) {
                this.p.a.a((int)(i2 + 44)).h().m = new m();
                this.p.a.a((int)(i2 + 44)).h().m.a(102);
                this.p.a.a((int)(i2 + 44)).h().m.a = (byte)2;
                this.p.a.a((int)(i2 + 44)).h().m.a(257, false, (byte)-1);
            }
            if (this.q.a((byte)this.b, i2 + this.w + this.q.W[this.b]) == 2) {
                this.p.a.a((int)(i2 + 44)).h().m.a(101);
            } else {
                this.p.a.a((int)(i2 + 44)).h().m.a(102);
            }
            this.p.a.a((int)(24 + (i2 << 2) + 3)).h().a = an.f(aq.c[0][this.q.W[this.b] + i2 + this.w][0]);
        }
        this.p.a.a((int)20).h().a = an.f(365 + this.b) + i1 + "/" + this.q.X[this.b];
        this.p.a.a(23).b(99 + (this.h << 6) / this.q.X[this.b], this.p.a.a());
    }

    public final void Q() {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            this.aZ();
        } else if (this.o.k(8448)) {
            this.p.a.b(1);
            this.aZ();
        } else if (this.o.k(16400)) {
            this.p.a.b(2);
            this.aY();
            this.aZ();
        } else if (this.o.k(32832)) {
            this.p.a.b(3);
            this.aY();
            this.aZ();
        } else if (!this.o.k(196640) && this.o.k(786432)) {
            if (this.o.Q == 8) {
                this.o.a((byte)8);
            } else {
                this.c = 0;
                this.o.a((byte)9);
            }
            this.p.a("/data/ui/petmap.ui");
        }
        this.g = true;
    }

    public final void R() {
        this.p.a("/data/ui/task.ui", 257, this);
        this.p.a("/data/ui/gamemenu.ui");
        ((al)this.p.a.a((int)0)).b.f = this.b;
        this.c = 0;
        this.r = 0;
        this.ba();
        this.bb();
    }

    private void ba() {
        switch (this.b) {
            case 0: {
                if (game.c.t >= game.c.r.length / 2 - 1) {
                    ((al)this.p.a.a((int)0)).a.a = game.c.r.length / 2;
                    ((al)this.p.a.a((int)0)).a.f = game.c.r.length / 2 - 1;
                } else {
                    ((al)this.p.a.a((int)0)).a.a = game.c.t + 1;
                    ((al)this.p.a.a((int)0)).a.f = game.c.t;
                }
                this.p.a.a((int)36).h().a = "";
                this.h = game.c.t;
                this.w = game.c.t - 4;
                if (this.h <= 0) {
                    this.h = 0;
                }
                if (this.w <= 0) {
                    this.w = 0;
                }
                ((al)this.p.a.a((int)0)).a.e = this.w;
                this.p.a.a((int)37).h().a = "\u0110\u1ea7u m\u1ed1i ch\u00ednh ho\u00e0n th\u00e0nh \u0111\u1ed9: ";
                this.p.a.a((int)38).h().a = game.c.t >= game.c.r.length / 2 ? game.c.r[game.c.r.length - 1] : game.c.r[game.c.r.length / 2 + game.c.t];
                int i1 = game.c.t * 1000 / (game.c.r.length / 2);
                int i2 = i1 % 10;
                if (i2 == 0) {
                    i2 = 1;
                }
                this.p.a.a((int)38).h().a = i1 / 50 + "." + i2 + "%";
                if (game.c.t > 4) {
                    ((al)this.p.a.a((int)0)).a.a(1);
                } else {
                    ((al)this.p.a.a((int)0)).a.a(0);
                }
                this.p.a.a((int)8).h().g = 11290624;
                break;
            }
            case 1: {
                int i2;
                ((al)this.p.a.a((int)0)).a.a = game.c.u;
                ((al)this.p.a.a((int)0)).a.f = 0;
                ((al)this.p.a.a((int)0)).a.e = 0;
                this.p.a.a((int)36).h().a = "";
                this.p.a.a((int)37).h().a = "Chi nh\u00e1nh ho\u00e0n th\u00e0nh \u0111\u1ed9: ";
                int i1 = 0;
                for (i2 = 0; i2 < game.c.s.length; ++i2) {
                    if (game.c.s[i2][1] != 3) continue;
                    ++i1;
                }
                System.out.println(" Nhi\u1ec7m v\u1ee5 ph\u1ee5 " + game.c.q.length);
                i2 = i1 * 1000 / (game.c.q.length / 2);
                this.p.a.a((int)38).h().a = i2 / 10 + "." + i2 % 10 + "%";
                if (game.c.u > 5) {
                    ((al)this.p.a.a((int)0)).a.a(1);
                } else {
                    ((al)this.p.a.a((int)0)).a.a(0);
                }
                this.p.a.a((int)9).h().g = 11290624;
            }
        }
        this.bb();
    }

    private void bb() {
        this.w = ((al)this.p.a.a((int)0)).a.e;
        this.h = ((al)this.p.a.a((int)0)).a.f;
        for (int i1 = 0; i1 < 5; ++i1) {
            if (this.b == 0) {
                if (game.c.t > 0) {
                    if (this.w + i1 < game.c.t) {
                        this.p.a.a((int)(10 + i1 * 5 + 2)).h().a = "" + (i1 + this.w + 1);
                        this.p.a.a((int)(10 + i1 * 5 + 3)).h().a = game.c.r[this.w + i1];
                        this.p.a.a((int)(10 + i1 * 5 + 4)).h().a = "Ho\u00e0n th\u00e0nh";
                        continue;
                    }
                    if (this.w + i1 == game.c.t && this.w + i1 <= game.c.r.length / 2 - 1) {
                        this.p.a.a((int)(10 + i1 * 5 + 2)).h().a = "" + (i1 + this.w + 1);
                        this.p.a.a((int)(10 + i1 * 5 + 3)).h().a = game.c.r[this.w + i1];
                        this.p.a.a((int)(10 + i1 * 5 + 4)).h().a = "";
                        continue;
                    }
                    this.p.a.a((int)(10 + i1 * 5 + 2)).h().a = "";
                    this.p.a.a((int)(10 + i1 * 5 + 3)).h().a = "";
                    this.p.a.a((int)(10 + i1 * 5 + 4)).h().a = "";
                    this.p.a.a((int)36).h().a = "";
                    continue;
                }
                this.p.a.a((int)12).h().a = "1";
                this.p.a.a((int)13).h().a = game.c.r[0];
                this.p.a.a((int)14).h().a = "";
                continue;
            }
            if (this.b != 1) continue;
            if (this.w + i1 < game.c.u) {
                this.p.a.a((int)(10 + i1 * 5 + 2)).h().a = "" + (i1 + this.w + 1);
                this.p.a.a((int)(10 + i1 * 5 + 3)).h().a = game.c.q[game.c.s[this.w + i1][0]];
                if (game.c.s[this.w + i1][1] == 3) {
                    this.p.a.a((int)(10 + i1 * 5 + 4)).h().a = "Ho\u00e0n th\u00e0nh";
                    continue;
                }
                this.p.a.a((int)(10 + i1 * 5 + 4)).h().a = "";
                continue;
            }
            this.p.a.a((int)(10 + i1 * 5 + 2)).h().a = "";
            this.p.a.a((int)(10 + i1 * 5 + 3)).h().a = "";
            this.p.a.a((int)(10 + i1 * 5 + 4)).h().a = "";
        }
        switch (this.b) {
            case 0: {
                this.p.a.a((int)36).h().a = game.c.r[game.c.r.length / 2 + this.h];
                break;
            }
            case 1: {
                if (game.c.u <= 0) break;
                this.p.a.a((int)36).h().a = game.c.q[game.c.q.length / 2 + game.c.s[this.h][0]];
            }
        }
        if (((al)this.p.a.a((int)0)).a.a > 0) {
            this.p.a.a(40).b(104 + (this.h << 6) / ((al)this.p.a.a((int)0)).a.a, this.p.a.a());
        }
    }

    public final void S() {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            this.bb();
            return;
        }
        if (this.o.k(8448)) {
            this.p.a.b(1);
            this.bb();
            return;
        }
        if (this.o.k(16400)) {
            this.p.a.b(2);
            this.ba();
            return;
        }
        if (this.o.k(32832)) {
            this.p.a.b(3);
            this.ba();
            return;
        }
        if (this.o.k(983072)) {
            this.p.a("/data/ui/task.ui");
            this.b = 4;
            if (this.o.Q == 0) {
                this.b = 0;
                this.o.a((byte)0);
                return;
            }
            this.o.a((byte)6);
            return;
        }
        if (this.o.k(10)) {
            this.p.a("/data/ui/task.ui");
            this.o.a((byte)0);
        }
    }

    public final void T() {
        this.p.a("/data/ui/badge.ui", 257, this);
        this.p.a("/data/ui/record.ui");
        this.b = 0;
        this.f = 0;
        for (int i1 = 0; i1 < 8; ++i1) {
            if (this.q.B[i1][0] == 0) continue;
            this.p.a.a((int)(i1 + 25)).h().m.a(i1 + 46);
        }
        this.bc();
    }

    private void bc() {
        this.p.a.a((int)13).h().a = an.f(aq.c[2][this.b][0]);
        this.p.a.a((int)14).h().a = an.f(aq.c[2][this.b][2 + this.q.c((byte)this.b, (byte)1)]);
        if (this.q.c((byte)this.b, (byte)0) == 0) {
            this.p.a.a((int)16).h().a = "Ch\u01b0a \u0111\u1ea1t";
            return;
        }
        this.p.a.a((int)16).h().a = "\u0110\u00e3 \u0111\u1ea1t \u0111\u01b0\u1ee3c";
        this.q.c((byte)this.b, (byte)1);
        this.p.a.a((int)33).h().a = "";
    }

    public final void U() {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            this.bc();
            return;
        }
        if (this.o.k(8448)) {
            this.p.a.b(1);
            this.bc();
            return;
        }
        if (this.o.k(16400)) {
            this.p.a.b(2);
            this.bc();
            return;
        }
        if (this.o.k(32832)) {
            this.p.a.b(3);
            this.bc();
            return;
        }
        if (this.o.k(786432)) {
            if (this.o.Q == 8) {
                this.o.a((byte)8);
            } else {
                this.c = 1;
                this.o.a((byte)9);
            }
            this.p.a("/data/ui/badge.ui");
        }
    }

    public final void a(int i1) {
        this.p.a("/data/ui/smsTip.ui", 257, this);
        if (this.p.a.a((int)6).h().m == null) {
            this.p.a.a((int)6).h().m = new m();
            this.p.a.a((int)6).h().m.a = (byte)2;
            this.p.a.a((int)6).h().m.a(-1);
            this.p.a.a((int)6).h().m.a(257, false, (byte)-1);
            this.p.a.a((int)6).h().m.a(i1 + 46);
        }
        this.p.a.a((int)7).h().a = an.f(i1 + 187) + ":" + an.f(i1 + 195);
        this.p.a.a((int)8).h().a = an.f(377);
    }

    public final void V() {
        this.p.a("/data/ui/smsTip.ui");
    }

    public final void W() {
        this.b = 0;
        this.e(this.c);
    }

    private void e(int i1) {
        int i3 = i1;
        b[] v2 = this.q.z;
        h v1 = this;
        v1.p.a("/data/ui/petstate.ui", 257, v1);
        v1.f(i3);
        v1.f = 0;
        if (v1.o instanceof k) {
            for (int i4 = 0; i4 < 6; ++i4) {
                if (v2[i4] != null) {
                    v1.p.a.a((int)(16 + i4 * 6)).h().a = "#P" + v2[i4].L();
                    v1.p.a.a((int)(17 + i4 * 6)).h().a = "#P" + v2[i4].O();
                    continue;
                }
                v1.p.a.a((int)(16 + i4 * 6)).h().a = "#P0";
                v1.p.a.a((int)(17 + i4 * 6)).h().a = "#P0";
            }
            if (v1.o.Q == 16) {
                v1.p.a.a((int)64).h().a = "G\u1edfi l\u1ea1i";
            }
            v1.p.a.a(75).a(false);
            v1.p.a.a(76).a(false);
        } else if (v1.o instanceof d) {
            for (int i4 = 0; i4 < 6; ++i4) {
                if (i4 < ((d)v1.o).f.length && v2[((d)v1.o).f[i4]] != null) {
                    v1.p.a.a((int)(16 + i4 * 6)).h().a = "#P" + v2[((d)v1.o).f[i4]].L();
                    v1.p.a.a((int)(17 + i4 * 6)).h().a = "#P" + v2[((d)v1.o).f[i4]].O();
                    continue;
                }
                v1.p.a.a((int)(16 + i4 * 6)).h().a = "#P0";
                v1.p.a.a((int)(17 + i4 * 6)).h().a = "#P0";
            }
            v1.p.a.a(63).a(false);
            v1.p.a.a(64).a(false);
            if (v1.o.Q == 4) {
                v1.p.a.a((int)75).h().a = "S\u1eed d\u1ee5ng";
            } else if (v1.o.P == 5) {
                v1.p.a.a((int)75).h().a = "Xu\u1ea5t chi\u1ebfn";
            }
        }
        ((al)v1.p.a.a((int)0)).a.a = v1.q.A;
        ((al)v1.p.a.a((int)0)).a.d = v1.q.A;
        ((al)v1.p.a.a((int)0)).a.f = i3;
        v1.g = true;
    }

    private void a(b[] v1, int i2) {
        if (v1[i2] != null) {
            if (this.p.a.a((int)48).h().m != null) {
                this.p.a.a((int)48).h().m.d();
            } else {
                this.p.a.a((int)48).h().m = new m();
                this.p.a.a((int)48).h().m.a(0);
                this.p.a.a((int)48).h().m.a = (byte)3;
            }
            this.p.a.a((int)48).h().m.a(v1[i2].C, false, (byte)-1);
            this.p.a.a((int)51).h().a = an.f(v1[i2].j((byte)0));
            this.p.a.a((int)52).h().a = an.f(365 + v1[i2].j((byte)1));
            if (v1[i2].j((byte)19) == -1) {
                this.p.a.a((int)62).h().a = "";
            } else if (aq.c[0][v1[i2].j((byte)19)][2] == 1 || aq.c[0][v1[i2].j((byte)19)][2] == 2) {
                this.p.a.a((int)62).h().a = "C\u00f3 th\u1ec3 ti\u1ebfn h\u00f3a";
            } else if (aq.c[0][v1[i2].j((byte)19)][2] == 3) {
                this.p.a.a((int)62).h().a = "C\u00f3 th\u1ec3 d\u1ecb ho\u00e1";
            }
            this.p.a.a((int)61).h().a = v1[i2].T();
            if (this.o instanceof d) {
                this.p.a.a((int)64).h().a = "Xu\u1ea5t chi\u1ebfn";
            } else if (this.o instanceof k) {
                this.p.a.a((int)64).h().a = "X\u00e1c nh\u1eadn";
            }
            if (this.p.a.a((int)59).h().m == null) {
                this.p.a.a((int)59).h().m = new m();
                this.p.a.a((int)59).h().m.a(0);
                this.p.a.a((int)59).h().m.a = (byte)2;
                this.p.a.a((int)59).h().m.a(258, false, (byte)-1);
            }
            if (v1[i2].c[5] != -1) {
                this.p.a.a((int)59).h().m.a(aq.c[3][v1[i2].c[5]][1]);
                this.p.a.a((int)60).h().a = an.f(aq.c[3][v1[i2].c[5]][0]);
            } else {
                this.p.a.a((int)59).h().m.a(0);
                this.p.a.a((int)60).h().a = "";
            }
            this.p.a.a((int)65).h().a = "" + v1[i2].s();
            this.p.a.a((int)66).h().a = "" + v1[i2].e((byte)2);
            this.p.a.a((int)67).h().a = "" + v1[i2].e((byte)3);
            this.p.a.a((int)68).h().a = "" + v1[i2].e((byte)4);
            int i3 = v1[i2].d[0];
            int i1 = aq.a((byte)0, (short)v1[i2].q(), (byte)4) - 1;
            for (i2 = 0; i2 < 5; ++i2) {
                this.p.a.a(74 - i2).a(true);
                this.p.a.a((int)(74 - i2)).h().m.a(257, false, (byte)-1);
                this.p.a.a((int)(74 - i2)).h().m.a = (byte)3;
                if (i2 > i1) {
                    this.p.a.a(74 - i2).a(false);
                    continue;
                }
                if (i3 > 0) {
                    this.p.a.a((int)(74 - i2)).h().m.a((byte)14, (byte)-1);
                    --i3;
                    continue;
                }
                this.p.a.a((int)(74 - i2)).h().m.a((byte)16, (byte)-1);
            }
        }
    }

    private void f(int i1) {
        if (this.o instanceof k) {
            this.a(this.q.z, i1);
            return;
        }
        if (this.o instanceof d) {
            this.a(this.q.z, (int)((d)this.o).f[i1]);
        }
    }

    public final void X() {
        block132: {
            block135: {
                block134: {
                    block133: {
                        block131: {
                            if (this.f != 0) break block131;
                            if (!an.b(this.b, 0) && !this.j() && this.o.k(4100)) {
                                this.p.a.b(0);
                            } else if (!an.b(this.b, 0) && !this.j() && this.o.k(8448)) {
                                this.p.a.b(1);
                            } else if (an.I() && !this.j() && this.o.k(196640)) {
                                if (an.H() && !an.b(this.b, 0)) {
                                    return;
                                }
                                if (this.o instanceof d) {
                                    int i1 = ((d)this.o).a(this.b);
                                    if (i1 == 0) {
                                        this.f = 2;
                                        this.p.a("/data/ui/msgwarm.ui", 257, this);
                                        this.a("S\u1ee7ng v\u1eadt n\u00e0y kh\u00f4ng th\u1ec3 tham chi\u1ebfn", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                        this.p.a("/data/ui/petsetting.ui");
                                    } else if (i1 == 1) {
                                        this.f = 2;
                                        this.p.a("/data/ui/msgwarm.ui", 257, this);
                                        this.a("S\u1ee7ng v\u1eadt n\u00e0y \u0111\u00e3 \u0111\u1eb7t \u1edf v\u1ecb tr\u00ed chi\u1ebfn \u0111\u1ea5u", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                        this.p.a("/data/ui/petsetting.ui");
                                    } else if (i1 == -1) {
                                        ((d)this.o).a((int)((d)this.o).g, 0);
                                        this.a = 0;
                                        this.o.a((byte)15);
                                        this.p.a("/data/ui/petsetting.ui");
                                        this.p.a("/data/ui/petstate.ui");
                                    }
                                } else if (this.o instanceof k) {
                                    if (this.o.Q == 16) {
                                        if (this.q.z()) {
                                            if (this.q.o(this.b)) {
                                                this.q.l(this.q.z[this.b].c[5]);
                                                this.q.z[this.b].c[5] = -1;
                                                this.q.b(this.q.z[this.b].P());
                                                this.q.m(this.b);
                                                if (this.b >= this.q.A) {
                                                    --this.b;
                                                }
                                                this.e(this.b);
                                            } else {
                                                this.f = 1;
                                                this.p.a("/data/ui/msgwarm.ui", 257, this);
                                                this.a("Ba l\u00f4 ph\u1ea3i l\u01b0u \u00edt nh\u1ea5t 1 s\u1ee7ng v\u1eadt", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            }
                                        } else {
                                            this.f = 1;
                                            this.p.a("/data/ui/msgwarm.ui", 257, this);
                                            this.a("Ng\u00e2n h\u00e0ng \u0111\u00e3 \u0111\u1ea7y, kh\u00f4ng th\u1ec3 g\u1edfi l\u1ea1i", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                        }
                                    } else if (this.o.Q == 6 || this.o.Q == 0) {
                                        this.c = 0;
                                        this.o.m();
                                        this.f = 1;
                                        this.p.a("/data/ui/petsetting.ui", 257, this);
                                        ((al)this.p.a.a((int)0)).a.f = this.c;
                                        if (this.q.z[this.b].R() == 2) {
                                            this.p.a.a((int)9).h().a = "D\u1ecb ho\u00e1";
                                            ((al)this.p.a.a((int)0)).a.a = 6;
                                            ((al)this.p.a.a((int)0)).a.d = 6;
                                        } else if (this.q.z[this.b].R() == 1) {
                                            this.p.a.a((int)9).h().a = "Ti\u1ebfn h\u00f3a";
                                            ((al)this.p.a.a((int)0)).a.a = 6;
                                            ((al)this.p.a.a((int)0)).a.d = 6;
                                        } else {
                                            this.p.a.a((int)9).h().a = "";
                                            ((al)this.p.a.a((int)0)).a.a = 5;
                                            ((al)this.p.a.a((int)0)).a.d = 5;
                                        }
                                    } else if (this.o.Q == 27) {
                                        if (this.d == 1 && this.q.z[this.b].R() == 1 || this.d == 2 && this.q.z[this.b].R() == 2) {
                                            this.bg();
                                        } else {
                                            this.f = 4;
                                            this.E();
                                            if (this.d == 1) {
                                                this.a("S\u1ee7ng v\u1eadt n\u00e0y kh\u00f4ng th\u1ec3 ti\u1ebfn h\u00f3a", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            } else if (this.d == 2) {
                                                this.a("S\u1ee7ng v\u1eadt n\u00e0y kh\u00f4ng th\u1ec3 d\u1ecb ho\u00e1", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            } else {
                                                this.a("Kh\u00f4ng th\u1ec3 v\u00e0o h\u00f3a c\u00f9ng d\u1ecb ho\u00e1", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            }
                                        }
                                    }
                                }
                            } else if (game.c.J() && !this.j() && this.o.k(262144)) {
                                if (this.o instanceof k) {
                                    if (this.o.Q == 16) {
                                        this.o.a((byte)16);
                                    } else if (this.o.Q == 6) {
                                        this.b = 1;
                                        this.o.a((byte)6);
                                    } else if (this.o.Q == 27) {
                                        this.o.a((byte)27);
                                    } else if (this.o.Q == 0) {
                                        this.o.a((byte)23);
                                    }
                                    this.p.a("/data/ui/petstate.ui");
                                } else if (this.o instanceof d) {
                                    if (((d)this.o).Q == 7 || ((d)this.o).Q == 13) {
                                        return;
                                    }
                                    this.p.a("/data/ui/petstate.ui");
                                    game.d.a().k = false;
                                    this.a = 0;
                                    this.o.a((byte)20);
                                }
                            }
                            break block132;
                        }
                        if (this.f != 1) break block133;
                        if (!an.b(this.c, 0) && !this.j() && this.o.k(4100)) {
                            this.p.a.b(0);
                        } else if (!an.b(this.c, 0) && !this.j() && this.o.k(8448)) {
                            this.p.a.b(1);
                        } else if (an.I() && !this.j() && this.o.k(196640)) {
                            if (an.H() && !an.b(this.c, 0)) {
                                return;
                            }
                            if (this.o.Q == 16) {
                                this.o.a((byte)16);
                                this.p.a("/data/ui/msgwarm.ui");
                                this.p.a("/data/ui/petstate.ui");
                                this.f = 0;
                            } else if (this.o.Q == 6 || this.o.Q == 0) {
                                switch (this.c) {
                                    case 0: {
                                        h v1 = this;
                                        this.f = 2;
                                        v1.r = 0;
                                        v1.p.a("/data/ui/choice.ui", 257, v1);
                                        v1.p.a("/data/ui/petsetting.ui");
                                        v1.p.a("/data/ui/petstate.ui");
                                        v1.p.a.a((int)8).h().a = "\u0110\u1ea1o c\u1ee5";
                                        v1.p.a.a((int)9).h().a = "S\u1ed1 l\u01b0\u1ee3ng";
                                        if (v1.o instanceof k) {
                                            v1.p.a.a(5).a(false);
                                            v1.p.a.a(6).a(false);
                                            v1.p.a.a(59).a(true);
                                            v1.p.a.a(60).a(true);
                                            v1.p.a.a((int)59).h().a = "S\u1eed d\u1ee5ng";
                                        } else {
                                            v1.p.a.a(5).a(true);
                                            v1.p.a.a(6).a(true);
                                            v1.p.a.a(59).a(false);
                                            v1.p.a.a(60).a(false);
                                            v1.p.a.a((int)5).h().a = "S\u1eed d\u1ee5ng";
                                        }
                                        v1.be();
                                        v1.g = true;
                                        break;
                                    }
                                    case 1: {
                                        if (!this.q.z[this.b].S()) {
                                            this.f = 2;
                                            this.p.a("/data/ui/msgwarm.ui", 257, this);
                                            this.a("S\u1ee7ng v\u1eadt n\u00e0y kh\u00f4ng th\u1ec3 tham chi\u1ebfn", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            this.p.a("/data/ui/petsetting.ui");
                                            this.b = 0;
                                            break;
                                        }
                                        if (this.b == 0) {
                                            this.f = 2;
                                            this.b = 0;
                                            this.p.a("/data/ui/msgwarm.ui", 257, this);
                                            this.a("S\u1ee7ng v\u1eadt n\u00e0y \u0111\u00e3 xu\u1ea5t chi\u1ebfn", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            this.p.a("/data/ui/petsetting.ui");
                                            break;
                                        }
                                        this.q.p(this.b);
                                        this.f = 0;
                                        this.b = 0;
                                        this.e(this.b);
                                        this.p.a("/data/ui/petsetting.ui");
                                        ((al)this.p.a.a((int)0)).a.f = 0;
                                        ((al)this.p.a.a((int)0)).a.e = 0;
                                        break;
                                    }
                                    case 2: {
                                        this.o.m();
                                        h v1 = this;
                                        this.f = 2;
                                        v1.r = 0;
                                        v1.p.a("/data/ui/choice.ui", 257, v1);
                                        v1.p.a("/data/ui/petsetting.ui");
                                        v1.p.a("/data/ui/petstate.ui");
                                        v1.p.a.a((int)8).h().a = "V\u1eadt ph\u1ea9m trang s\u1ee9c";
                                        v1.p.a.a((int)9).h().a = "Tr\u1ea1ng th\u00e1i";
                                        if (v1.o instanceof k) {
                                            v1.p.a.a(5).a(false);
                                            v1.p.a.a(6).a(false);
                                            v1.p.a.a(59).a(true);
                                            v1.p.a.a(60).a(true);
                                            v1.p.a.a((int)59).h().a = "Mang theo";
                                        } else {
                                            v1.p.a.a(5).a(true);
                                            v1.p.a.a(6).a(true);
                                            v1.p.a.a(59).a(false);
                                            v1.p.a.a(60).a(false);
                                            v1.p.a.a((int)5).h().a = "Mang theo";
                                        }
                                        v1.bd();
                                        v1.g = true;
                                        break;
                                    }
                                    case 3: {
                                        if (aq.a((byte)0, (short)this.q.z[this.b].q(), (byte)22) == 2) {
                                            this.f = 3;
                                            this.E();
                                            this.p.a("/data/ui/petsetting.ui");
                                            this.a("Th\u1ea7n th\u00fa kh\u00f4ng th\u1ec3 ph\u00f3ng sinh", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            break;
                                        }
                                        this.f = 2;
                                        this.p.a("/data/ui/msgconfirm.ui", 257, this);
                                        this.p.a("/data/ui/petsetting.ui");
                                        this.b("B\u1ea1n mu\u1ed1n ph\u00f3ng sinh s\u1ee7ng v\u1eadt n\u00e0y?", "X\u00e1c nh\u1eadn");
                                        break;
                                    }
                                    case 4: {
                                        h v1 = this;
                                        this.f = 2;
                                        v1.r = 0;
                                        v1.p.a("/data/ui/skill.ui", 257, v1);
                                        v1.p.a("/data/ui/petsetting.ui");
                                        v1.p.a("/data/ui/petstate.ui");
                                        v1.p.a.a((int)12).h().a = an.f(v1.q.z[v1.b].j((byte)0));
                                        v1.p.a.a((int)14).h().a = "" + v1.q.z[v1.b].s();
                                        if (v1.p.a.a((int)16).h().m != null) {
                                            v1.p.a.a((int)16).h().m.d();
                                        } else {
                                            v1.p.a.a((int)16).h().m = new m();
                                            v1.p.a.a((int)16).h().m.a(0);
                                            v1.p.a.a((int)16).h().m.a = (byte)3;
                                        }
                                        v1.p.a.a((int)16).h().m.a(v1.q.z[v1.b].C, false, (byte)-1);
                                        int i2 = v1.q.z[v1.b].E();
                                        for (int i3 = 0; i3 < i2; ++i3) {
                                            v1.p.a.a((int)(i3 + 18)).h().a = an.f(aq.c[1][v1.q.z[v1.b].t(i3)][1]);
                                        }
                                        v1.bf();
                                        v1.g = true;
                                        break;
                                    }
                                    case 5: {
                                        this.o.m();
                                        this.bg();
                                    }
                                    default: {
                                        break;
                                    }
                                }
                            }
                        } else if (game.c.J() && !this.j() && this.o.k(262144)) {
                            if (this.o.Q == 16) {
                                return;
                            }
                            this.f = 0;
                            this.p.a("/data/ui/petsetting.ui");
                        }
                        break block132;
                    }
                    if (this.f < 2) break block132;
                    if (!(this.o instanceof d)) break block134;
                    if (this.o.k(196640)) {
                        this.f = 0;
                        this.p.a("/data/ui/msgwarm.ui");
                    }
                    break block132;
                }
                if (this.o.Q != 6 && this.o.Q != 0) break block135;
                block8 : switch (this.c) {
                    case 1: {
                        if (this.o.k(196640)) {
                            this.f = 0;
                            this.p.a("/data/ui/msgwarm.ui");
                            break;
                        }
                        break block132;
                    }
                    case 2: {
                        h v1 = this;
                        if (!an.b(v1.b, 0) && !v1.j() && v1.f == 2 && v1.o.k(4100)) {
                            v1.p.a.b(0);
                            v1.bd();
                            break;
                        }
                        if (!an.b(v1.b, 0) && !v1.j() && v1.f == 2 && v1.o.k(8448)) {
                            v1.p.a.b(1);
                            v1.bd();
                            break;
                        }
                        if (an.I() && !v1.j() && v1.o.k(196640) && v1.q.L.size() > 0) {
                            if (!an.H() || an.b(v1.b, 0)) {
                                if (v1.f == 2) {
                                    int[] v2 = (int[])v1.q.L.elementAt(v1.h);
                                    if (v1.q.z[v1.b].c[5] == v2[0]) {
                                        v1.q.l(v1.q.z[v1.b].c[5]);
                                        v1.q.z[v1.b].c[5] = -1;
                                        v1.bd();
                                        v1.E();
                                        v1.a("Th\u00e0nh c\u00f4ng d\u1ee1 xu\u1ed1ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                    } else {
                                        v1.q.f(v2[0], v1.b);
                                        v1.bd();
                                        v1.E();
                                        v1.a("Th\u00e0nh c\u00f4ng mang theo", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                    }
                                    v1.f = 3;
                                    break;
                                }
                                v1.f = 2;
                                v1.o.m();
                                v1.e(v1.b);
                                v1.F();
                                v1.p.a("/data/ui/choice.ui");
                                break;
                            }
                        } else if (game.c.J() && !v1.j() && v1.f == 2 && v1.o.k(262144)) {
                            v1.e(v1.b);
                            v1.p.a("/data/ui/choice.ui");
                            break;
                        }
                        break block132;
                    }
                    case 0: {
                        h v1 = this;
                        if (v1.f == 2 && v1.o.k(4100)) {
                            v1.p.a.b(0);
                            break;
                        }
                        if (v1.f == 2 && v1.o.k(8448)) {
                            v1.p.a.b(1);
                            break;
                        }
                        if (v1.o.k(196640)) {
                            if (v1.q.J.size() > 0) {
                                if (v1.f == 2) {
                                    v1.f = 3;
                                    int[] v2 = (int[])v1.q.J.elementAt(v1.r);
                                    switch (v2[0]) {
                                        case 13: 
                                        case 14: {
                                            v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                                            v1.a("\u0110\u1ea1o c\u1ee5 n\u00e0y kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            break block8;
                                        }
                                    }
                                    switch (v1.q.z[v1.b].x(v2[0])) {
                                        case 0: {
                                            v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                                            v1.a("S\u1ee7ng v\u1eadt n\u00e0y \u0111\u00e3 t\u1eed vong, kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            break block8;
                                        }
                                        case 1: {
                                            v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                                            v1.a("S\u1ee7ng v\u1eadt n\u00e0y kh\u00f4ng c\u00f3, kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            break block8;
                                        }
                                        case 2: {
                                            v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                                            v1.a("M\u00e1u \u0111\u1ea7y, kh\u00f4ng c\u1ea7n s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            break block8;
                                        }
                                        case 3: {
                                            v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                                            v1.a("K\u1ef9 n\u0103ng gi\u00e1 tr\u1ecb \u0111\u00e3 \u0111\u1ea7y, kh\u00f4ng c\u1ea7n s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            break block8;
                                        }
                                        case 4: {
                                            v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                                            v1.a("Tr\u00ean ng\u01b0\u1eddi \u0111\u1ec1u b\u1ecb l\u1ee3i hi\u1ec7u qu\u1ea3", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            break block8;
                                        }
                                        case 5: {
                                            v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                                            v1.a("Trong h\u01b0ng ph\u1ea5n, kh\u00f4ng th\u1ec3 d\u00f9ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            break block8;
                                        }
                                        case 7: {
                                            v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                                            v1.a("M\u00e1u v\u00e0 k\u1ef9 n\u0103ng \u0111\u1ec1u \u0111\u00e3 \u0111\u1ea7y, kh\u00f4ng c\u1ea7n s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            break block8;
                                        }
                                        case 8: {
                                            v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                                            v1.a("S\u1ee7ng v\u1eadt \u0111\u00e3 ch\u1ebft, kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                            break block8;
                                        }
                                    }
                                    v1.q.z[v1.b].w(v2[0]);
                                    v1.e(v1.b);
                                    v1.f = 4;
                                    v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                                    v1.a("Th\u00e0nh c\u00f4ng s\u1eed d\u1ee5ng \u0111\u1ea1o c\u1ee5", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                    v1.p.a("/data/ui/choice.ui");
                                    break;
                                }
                                if (v1.f == 3) {
                                    v1.f = 2;
                                    v1.p.a("/data/ui/msgwarm.ui");
                                    break;
                                }
                                if (v1.f == 4) {
                                    v1.f = 0;
                                    v1.p.a("/data/ui/msgwarm.ui");
                                    break;
                                }
                            }
                        } else if (v1.f == 2 && v1.o.k(262144)) {
                            v1.e(v1.b);
                            v1.p.a("/data/ui/choice.ui");
                            break;
                        }
                        break block132;
                    }
                    case 4: {
                        h v1 = this;
                        if (v1.o.k(4100)) {
                            v1.p.a.b(0);
                            v1.bf();
                            break;
                        }
                        if (v1.o.k(8448)) {
                            v1.p.a.b(1);
                            v1.bf();
                            break;
                        }
                        if (v1.o.k(16400)) {
                            v1.p.a.b(2);
                            v1.bf();
                            break;
                        }
                        if (v1.o.k(32832)) {
                            v1.p.a.b(3);
                            v1.bf();
                            break;
                        }
                        if (v1.o.k(262144)) {
                            v1.e(v1.b);
                            v1.p.a("/data/ui/skill.ui");
                            break;
                        }
                        break block132;
                    }
                    case 3: {
                        if (this.o.k(131072) && this.f == 2 || this.o.k(131104) && this.f == 3) {
                            if (this.f == 2) {
                                if (this.q.o(this.b)) {
                                    this.q.l(this.q.z[this.b].c[5]);
                                    this.q.z[this.b].c[5] = -1;
                                    this.q.m(this.b);
                                    if (this.b >= this.q.A) {
                                        --this.b;
                                    }
                                    ((k)this.o).M.i();
                                    this.e(this.b);
                                    this.p.a("/data/ui/msgconfirm.ui");
                                    this.f = 0;
                                    break;
                                }
                                this.f = 3;
                                this.p.a("/data/ui/msgwarm.ui", 257, this);
                                this.a("Ba l\u00f4 ph\u1ea3i l\u01b0u \u00edt nh\u1ea5t 1 s\u1ee7ng v\u1eadt", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                this.p.a("/data/ui/msgconfirm.ui");
                                break;
                            }
                            this.f = 0;
                            this.p.a("/data/ui/msgwarm.ui");
                            break;
                        }
                        if (this.o.k(786432) && this.f <= 2) {
                            this.f = 0;
                            this.p.a("/data/ui/msgconfirm.ui");
                            break;
                        }
                        break block132;
                    }
                    case 5: {
                        this.bh();
                    }
                }
                break block132;
            }
            if (this.f <= 3) {
                this.bh();
            } else if (this.f == 4 && this.o.k(196640)) {
                this.f = 0;
                this.p.a("/data/ui/msgwarm.ui");
            }
        }
        this.g = true;
        this.g();
    }

    private void bd() {
        if (this.q.L.size() > 5) {
            ((al)this.p.a.a((int)0)).a.a(1);
        } else {
            ((al)this.p.a.a((int)0)).a.a(0);
        }
        ((al)this.p.a.a((int)0)).a.a = this.q.L.size();
        this.w = ((al)this.p.a.a((int)0)).a.e;
        this.h = ((al)this.p.a.a((int)0)).a.f;
        if (this.h >= this.q.L.size()) {
            ((al)this.p.a.a((int)0)).a.f = this.h = this.q.L.size() - 1;
        }
        if (this.w > 0 && this.h - this.w < 4) {
            --this.w;
            ((al)this.p.a.a((int)0)).a.e = this.w;
        }
        if (this.q.L.size() <= 0) {
            return;
        }
        if (this.q.z[this.b].c[5] == ((int[])this.q.L.elementAt(this.h))[0]) {
            if (this.o instanceof k) {
                this.p.a.a((int)59).h().a = "D\u1ee1 xu\u1ed1ng";
            } else {
                this.p.a.a((int)5).h().a = "D\u1ee1 xu\u1ed1ng";
            }
        } else if (this.o instanceof k) {
            this.p.a.a((int)59).h().a = "Mang theo";
        } else {
            this.p.a.a((int)5).h().a = "Mang theo";
        }
        for (int i1 = 0; i1 < 5; ++i1) {
            if (this.w + i1 < this.q.L.size()) {
                int[] v2 = (int[])this.q.L.elementAt(this.w + i1);
                if (this.p.a.a((int)(i1 + 54)).h().m == null) {
                    this.p.a.a((int)(i1 + 54)).h().m = new m();
                    this.p.a.a((int)(i1 + 54)).h().m.a(0);
                    this.p.a.a((int)(i1 + 54)).h().m.a = (byte)2;
                    this.p.a.a((int)(i1 + 54)).h().m.a(258, false, (byte)-1);
                }
                this.p.a.a((int)(i1 + 54)).h().m.a(aq.c[3][v2[0]][1]);
                this.p.a.a((int)(13 + i1 * 5)).h().a = an.f(aq.c[3][v2[0]][0]);
                if (game.g.o().z[this.b].c[5] == v2[0]) {
                    this.p.a.a((int)(14 + i1 * 5)).h().a = "\u0110\u00e3 mang theo";
                    continue;
                }
                if (v2[1] == 1) {
                    this.p.a.a((int)(14 + i1 * 5)).h().a = "B\u1ecb mang theo";
                    continue;
                }
                this.p.a.a((int)(14 + i1 * 5)).h().a = "";
                continue;
            }
            if (this.p.a.a((int)(i1 + 54)).h().m != null) {
                this.p.a.a((int)(i1 + 54)).h().m.d();
            }
            this.p.a.a((int)(13 + i1 * 5)).h().a = "";
            this.p.a.a((int)(14 + i1 * 5)).h().a = "";
        }
        this.p.a.a((int)53).h().a = this.q.L.size() > 0 ? an.f(aq.c[3][((int[])this.q.L.elementAt(this.h))[0]][2]) : "";
        if (this.q.L.size() > 0) {
            this.p.a.a(51).b(98 + this.h * 62 / this.q.L.size(), this.p.a.a());
            return;
        }
        this.p.a.a(51).b(98, this.p.a.a());
    }

    private void be() {
        if (this.q.J.size() > 5) {
            ((al)this.p.a.a((int)0)).a.a(1);
        } else {
            ((al)this.p.a.a((int)0)).a.a(0);
        }
        ((al)this.p.a.a((int)0)).a.a = this.q.J.size();
        this.w = ((al)this.p.a.a((int)0)).a.e;
        this.h = ((al)this.p.a.a((int)0)).a.f;
        if (this.h >= this.q.J.size()) {
            ((al)this.p.a.a((int)0)).a.f = this.h = this.q.J.size() - 1;
        }
        if (this.w > 0 && this.h - this.w < 4) {
            --this.w;
            ((al)this.p.a.a((int)0)).a.e = this.w;
        }
        for (int i1 = 0; i1 < 5; ++i1) {
            if (this.w + i1 < this.q.J.size()) {
                int[] v2 = (int[])this.q.J.elementAt(this.w + i1);
                if (this.p.a.a((int)(i1 + 54)).h().m == null) {
                    this.p.a.a((int)(i1 + 54)).h().m = new m();
                    this.p.a.a((int)(i1 + 54)).h().m.a(0);
                    this.p.a.a((int)(i1 + 54)).h().m.a = (byte)2;
                    this.p.a.a((int)(i1 + 54)).h().m.a(258, false, (byte)-1);
                }
                this.p.a.a((int)(i1 + 54)).h().m.a(aq.c[4][v2[0]][1]);
                this.p.a.a((int)(13 + i1 * 5)).h().a = an.f(aq.c[4][v2[0]][0]);
                this.p.a.a((int)(14 + i1 * 5)).h().a = "" + v2[1];
                continue;
            }
            if (this.p.a.a((int)(i1 + 54)).h().m != null) {
                this.p.a.a((int)(i1 + 54)).h().m.d();
            }
            this.p.a.a((int)(13 + i1 * 5)).h().a = "";
            this.p.a.a((int)(14 + i1 * 5)).h().a = "";
        }
        this.p.a.a((int)53).h().a = this.q.J.size() > 0 ? an.f(aq.c[4][((int[])this.q.J.elementAt(this.h))[0]][2]) : "";
        if (this.q.J.size() > 0) {
            this.p.a.a(51).b(98 + this.h * 72 / this.q.J.size(), this.p.a.a());
            return;
        }
        this.p.a.a(51).b(98, this.p.a.a());
    }

    private void bf() {
        if (this.q.z[this.b].t(this.r) != -1) {
            String[] v1 = new String[]{"Nh\u1ea5t \u0111\u1ecbnh", "Nh\u1ea5t \u0111\u1ecbnh"};
            this.p.a.a((int)9).h().a = an.a((int)aq.c[1][this.q.z[this.b].t(this.r)][2], v1);
            return;
        }
        this.p.a.a((int)9).h().a = "";
    }

    private void bg() {
        this.f = 2;
        this.r = 0;
        this.p.a("/data/ui/evolve.ui", 257, this);
        this.p.a("/data/ui/petsetting.ui");
        this.p.a("/data/ui/petstate.ui");
        if (this.p.a.a((int)10).h().m == null) {
            this.p.a.a((int)10).h().m = new m();
            this.p.a.a((int)10).h().m.a(0);
            this.p.a.a((int)10).h().m.a = (byte)3;
        }
        this.p.a.a((int)10).h().m.a(this.q.z[this.b].C, false, (byte)-1);
        int i1 = aq.a((byte)0, (byte)this.q.z[this.b].q(), (byte)20) + 12;
        short i2 = aq.a((byte)0, (byte)this.q.z[this.b].q(), (byte)21);
        this.p.a.a((int)38).h().a = an.f(aq.a((byte)0, (byte)this.q.z[this.b].q(), (byte)0));
        this.p.a.a((int)40).h().a = "" + this.q.z[this.b].s();
        this.p.a.a((int)45).h().a = an.f(aq.a((byte)3, (short)i1, (byte)0));
        this.p.a.a((int)46).h().a = this.q.a(i1, (byte)2) + "/" + i2;
        i1 = aq.a((byte)0, (byte)this.q.z[this.b].q(), (byte)19);
        b v2 = new b();
        v2.a(i1, (byte)this.q.z[this.b].s(), (short)-1, (byte)-1, (short)-1, (byte)-1);
        for (i1 = 0; i1 < 4; ++i1) {
            byte i3 = (byte)(i1 + 1);
            this.p.a.a((int)(i1 + 19)).h().a = "" + this.q.z[this.b].c[i3];
            i3 = (byte)(i1 + 1);
            this.p.a.a((int)(i1 + 31)).h().a = "" + v2.c[i3];
        }
        this.g = true;
    }

    private void bh() {
        if (game.k.n != null) {
            if (!game.k.n.i()) {
                short i1 = aq.a((byte)0, (byte)this.q.z[this.b].q(), (byte)19);
                String v2 = an.f(aq.a((byte)0, i1, (byte)0));
                h v1 = this;
                short i3 = aq.a((byte)0, (byte)v1.q.z[v1.b].q(), (byte)19);
                short i4 = aq.a((byte)0, i3, (byte)17);
                v1.p.a.a(10).a(true);
                v1.p.a.a((int)10).h().m.a(i4, false, (byte)-1);
                v1.p.a.a((int)38).h().a = an.f(aq.a((byte)0, i3, (byte)0));
                b v4 = new b();
                short i5 = aq.a((byte)0, i3, (byte)3);
                int i6 = -1;
                if (v1.q.z[v1.b].c[0] >= i5) {
                    i6 = (byte)v1.q.z[v1.b].c[0];
                }
                v4.a(i3, v1.q.z[v1.b].s(), v1.q.z[v1.b].c[5], (byte)v1.q.z[v1.b].d[6], (short)i6, (byte)-1);
                v4.a(v4.c[1], v1.q.z[v1.b].z(), (int)v1.q.z[v1.b].E);
                v4.b(v1.q.z[v1.b].Q());
                v1.q.a((byte)v1.q.z[v1.b].j((byte)1), (int)i3, (byte)2);
                v1.q.z[v1.b].a(v4.P());
                i5 = (short)(aq.a((byte)0, (byte)v1.q.z[v1.b].q(), (byte)20) + 12);
                i4 = aq.a((byte)0, (byte)v1.q.z[v1.b].q(), (byte)21);
                i3 = aq.a((byte)0, (byte)v1.q.z[v1.b].q(), (byte)19);
                i6 = v1.q.a((int)i5, (byte)2);
                if (i3 == -1) {
                    v1.p.a.a((int)42).h().a = "";
                    v1.p.a.a((int)45).h().a = "";
                    v1.p.a.a((int)46).h().a = "";
                } else {
                    v1.p.a.a((int)45).h().a = an.f(aq.a((byte)3, i5, (byte)0));
                    v1.p.a.a((int)46).h().a = i6 + "/" + i4;
                }
                if (this.q.z[this.b].R() == 2) {
                    this.f = 3;
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("D\u1ecb ho\u00e1 th\u00e0nh #2" + v2, "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                } else {
                    this.f = 3;
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("Ti\u1ebfn h\u00f3a th\u00e0nh #2" + v2, "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                }
                game.k.n = null;
            }
            return;
        }
        if (an.I() && !this.j() && this.o.k(196640)) {
            if (this.f == 2) {
                short i1 = (short)(aq.a((byte)0, (byte)this.q.z[this.b].q(), (byte)20) + 12);
                short i2 = aq.a((byte)0, (byte)this.q.z[this.b].q(), (byte)21);
                short i3 = aq.a((byte)0, (byte)this.q.z[this.b].q(), (byte)19);
                if (i3 == -1) {
                    this.f = 3;
                    this.E();
                    this.a("Kh\u00f4ng th\u1ec3 l\u1ea1i ti\u1ebfn h\u00f3a ho\u1eb7c d\u1ecb ho\u00e1", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    return;
                }
                short i4 = aq.a((byte)0, i3, (byte)17);
                if (this.q.z[this.b].s() >= game.b.t[aq.a((byte)0, i3, (byte)2) - 1]) {
                    if (this.q.a((int)i1, (byte)2) >= i2) {
                        this.p.a.a(10).a(false);
                        game.k.n = new ah();
                        short[] v3 = new short[]{0, 0, 10, 0, 0, this.q.z[this.b].C, 0, 0, i4, 0, 0};
                        game.k.n.a(v3);
                        game.k.n.c(true);
                        game.k.n.a();
                        this.q.d(i1, i2, (byte)2);
                        return;
                    }
                    this.f = 3;
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    if (this.q.z[this.b].R() == 2) {
                        this.a("T\u00e0i li\u1ec7u ch\u01b0a \u0111\u1ee7, kh\u00f4ng th\u1ec3 d\u1ecb ho\u00e1", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                        return;
                    }
                    this.a("T\u00e0i li\u1ec7u ch\u01b0a \u0111\u1ee7, kh\u00f4ng th\u1ec3 ti\u1ebfn h\u00f3a", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    return;
                }
                this.f = 3;
                this.p.a("/data/ui/msgwarm.ui", 257, this);
                this.a("C\u00f2n ch\u01b0a t\u1edbi" + game.b.t[aq.a((byte)0, i3, (byte)2) - 1] + " c\u1ea5p, kh\u00f4ng th\u1ec3 v\u00e0o h\u00f3a", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                return;
            }
            if (this.f == 3) {
                if (this.o.Q == 6 || this.o.Q == 0) {
                    this.f = 2;
                    this.p.a("/data/ui/msgwarm.ui");
                    this.o.m();
                    return;
                }
                if (this.o.Q == 27) {
                    this.e(this.b);
                    this.f = 0;
                    this.c = 0;
                    this.p.a("/data/ui/msgwarm.ui");
                    this.p.a("/data/ui/evolve.ui");
                    return;
                }
            }
        } else if (this.f < 3 && this.o.k(262144) && !this.j() && an.J()) {
            this.f = 0;
            this.e(this.b);
            this.p.a("/data/ui/evolve.ui");
        }
    }

    public final void Y() {
        this.p.a("/data/ui/bag.ui", 257, this);
        this.b = 0;
        this.bi();
        this.p.a.b(5);
        this.p.a.a((int)14).h().a = "V\u1eadt ph\u1ea9m";
        this.b = 0;
    }

    private void bi() {
        ((al)this.p.a.a((int)(8 + this.b * 39))).a.e = 0;
        ((al)this.p.a.a((int)(8 + this.b * 39))).a.f = 0;
        this.bj();
    }

    private void bj() {
        block0 : switch (this.b) {
            case 0: {
                this.bk();
                break;
            }
            case 1: {
                h v1 = this;
                if (v1.q.L.size() > 5) {
                    ((al)v1.p.a.a((int)47)).a.a(1);
                } else {
                    ((al)v1.p.a.a((int)47)).a.a(0);
                }
                ((al)v1.p.a.a((int)47)).a.a = v1.q.L.size();
                v1.w = ((al)v1.p.a.a((int)47)).a.e;
                v1.h = ((al)v1.p.a.a((int)47)).a.f;
                v1.p.a.a(7).a(false);
                for (int i2 = 0; i2 < 5; ++i2) {
                    if (v1.w + i2 < v1.q.L.size()) {
                        int[] v3 = (int[])v1.q.L.elementAt(v1.w + i2);
                        if (v1.p.a.a((int)(59 + i2 * 5)).h().m == null) {
                            v1.p.a.a((int)(59 + i2 * 5)).h().m = new m();
                            v1.p.a.a((int)(59 + i2 * 5)).h().m.a(0);
                            v1.p.a.a((int)(59 + i2 * 5)).h().m.a = (byte)2;
                            v1.p.a.a((int)(59 + i2 * 5)).h().m.a(258, false, (byte)-1);
                        }
                        if (v1.p.a.a((int)(59 + i2 * 5)).h().i == null) {
                            v1.p.a.a((int)(59 + i2 * 5)).h().i = new m();
                            v1.p.a.a((int)(59 + i2 * 5)).h().i.a(0);
                            v1.p.a.a((int)(59 + i2 * 5)).h().i.a = (byte)2;
                            v1.p.a.a((int)(59 + i2 * 5)).h().i.a(258, false, (byte)-1);
                        }
                        v1.p.a.a((int)(59 + i2 * 5)).h().m.a(aq.c[3][v3[0]][1]);
                        v1.p.a.a((int)(59 + i2 * 5)).h().i.a(aq.c[3][v3[0]][1]);
                        v1.p.a.a((int)(60 + i2 * 5)).h().a = an.f(aq.c[3][v3[0]][0]);
                        if (v3[1] == 1) {
                            v1.p.a.a((int)(61 + i2 * 5)).h().a = "\u0110\u00e3 mang theo";
                            continue;
                        }
                        v1.p.a.a((int)(61 + i2 * 5)).h().a = "";
                        continue;
                    }
                    if (v1.p.a.a((int)(59 + i2 * 5)).h().m != null) {
                        v1.p.a.a((int)(59 + i2 * 5)).h().m.d();
                    }
                    v1.p.a.a((int)(60 + i2 * 5)).h().a = "";
                    v1.p.a.a((int)(61 + i2 * 5)).h().a = "";
                }
                v1.p.a.a((int)85).h().a = v1.q.L.size() > 0 ? an.f(aq.c[3][((int[])v1.q.L.elementAt(v1.h))[0]][2]) : "";
                if (v1.q.L.size() > 0) {
                    v1.p.a.a(84).b(127 + v1.h * 72 / v1.q.L.size(), v1.p.a.a());
                    break;
                }
                v1.p.a.a(84).b(127, v1.p.a.a());
                break;
            }
            case 2: {
                h v1 = this;
                if (v1.q.M.size() > 5) {
                    ((al)v1.p.a.a((int)86)).a.a(1);
                } else {
                    ((al)v1.p.a.a((int)86)).a.a(0);
                }
                ((al)v1.p.a.a((int)86)).a.a = v1.q.M.size();
                v1.w = ((al)v1.p.a.a((int)86)).a.e;
                v1.h = ((al)v1.p.a.a((int)86)).a.f;
                v1.p.a.a(7).a(false);
                for (int i2 = 0; i2 < 5; ++i2) {
                    if (v1.w + i2 < v1.q.M.size()) {
                        int[] v3 = (int[])v1.q.M.elementAt(v1.w + i2);
                        if (v1.p.a.a((int)(98 + i2 * 5)).h().m == null) {
                            v1.p.a.a((int)(98 + i2 * 5)).h().m = new m();
                            v1.p.a.a((int)(98 + i2 * 5)).h().m.a(0);
                            v1.p.a.a((int)(98 + i2 * 5)).h().m.a = (byte)2;
                            v1.p.a.a((int)(98 + i2 * 5)).h().m.a(258, false, (byte)-1);
                        }
                        if (v1.p.a.a((int)(98 + i2 * 5)).h().i == null) {
                            v1.p.a.a((int)(98 + i2 * 5)).h().i = new m();
                            v1.p.a.a((int)(98 + i2 * 5)).h().i.a(0);
                            v1.p.a.a((int)(98 + i2 * 5)).h().i.a = (byte)2;
                            v1.p.a.a((int)(98 + i2 * 5)).h().i.a(258, false, (byte)-1);
                        }
                        v1.p.a.a((int)(98 + i2 * 5)).h().m.a(aq.c[3][v3[0]][1]);
                        v1.p.a.a((int)(98 + i2 * 5)).h().i.a(aq.c[3][v3[0]][1]);
                        v1.p.a.a((int)(99 + i2 * 5)).h().a = v3[0] == 17 ? "Ch\u00eca kh\u00f3a v\u00e0ng" : an.f(aq.c[3][v3[0]][0]);
                        v1.p.a.a((int)(100 + i2 * 5)).h().a = "" + v3[1];
                        continue;
                    }
                    if (v1.p.a.a((int)(98 + i2 * 5)).h().m != null) {
                        v1.p.a.a((int)(98 + i2 * 5)).h().m.d();
                    }
                    v1.p.a.a((int)(99 + i2 * 5)).h().a = "";
                    v1.p.a.a((int)(100 + i2 * 5)).h().a = "";
                }
                v1.p.a.a((int)124).h().a = v1.q.M.size() > 0 ? an.f(aq.c[3][((int[])v1.q.M.elementAt(v1.h))[0]][2]) : "";
                if (v1.q.M.size() > 0) {
                    v1.p.a.a(123).b(127 + v1.h * 72 / v1.q.M.size(), v1.p.a.a());
                    break;
                }
                v1.p.a.a(123).b(127, v1.p.a.a());
                break;
            }
            case 3: {
                this.bl();
                if (this.h < 0 || this.q.N.size() <= 0) {
                    return;
                }
                int[] v1 = (int[])this.q.N.elementAt(this.h);
                this.p.a.a(164).a(false);
                this.p.a.a(165).a(false);
                switch (v1[0]) {
                    case 0: {
                        if (this.q.k(v1[0])) {
                            this.p.a.a(7).a(true);
                            this.p.a.a((int)7).h().a = "\u1ea4p tr\u1ee9ng";
                            this.p.a.a(164).a(true);
                            this.p.a.a(165).a(true);
                            if (this.q.I == 0) {
                                this.p.a.a((int)164).h().a = "#P" + game.k.q * 100 / 10;
                                this.p.a.a((int)165).h().a = game.k.q + "/10";
                                break block0;
                            }
                            this.p.a.a((int)164).h().a = "#P" + game.k.q * 100 / 30;
                            this.p.a.a((int)165).h().a = game.k.q + "/30";
                            break block0;
                        }
                        this.p.a.a(7).a(false);
                        break block0;
                    }
                    case 1: 
                    case 2: 
                    case 3: 
                    case 4: {
                        this.p.a.a(7).a(false);
                        break block0;
                    }
                    case 5: 
                    case 6: 
                    case 10: {
                        this.p.a.a((int)7).h().a = "M\u1edf ra";
                        break block0;
                    }
                    case 7: 
                    case 8: 
                    case 9: {
                        this.p.a.a((int)7).h().a = "S\u1eed d\u1ee5ng";
                    }
                }
            }
        }
        this.g = true;
    }

    private void bk() {
        int i1 = this.q.K.size() + this.q.J.size();
        if (i1 > 5) {
            ((al)this.p.a.a((int)8)).a.a(1);
        } else {
            ((al)this.p.a.a((int)8)).a.a(0);
        }
        ((al)this.p.a.a((int)8)).a.a = i1;
        this.w = ((al)this.p.a.a((int)8)).a.e;
        this.h = ((al)this.p.a.a((int)8)).a.f;
        this.p.a.a(7).a(true);
        this.p.a.a((int)7).h().a = "S\u1eed d\u1ee5ng";
        for (int i2 = 0; i2 < 5; ++i2) {
            if (this.w + i2 < i1) {
                int[] v3 = this.w + i2 < this.q.K.size() ? (int[])this.q.K.elementAt(this.w + i2) : (int[])this.q.J.elementAt(this.w + i2 - this.q.K.size());
                if (this.p.a.a((int)(18 + i2 * 5)).h().m == null) {
                    this.p.a.a((int)(18 + i2 * 5)).h().m = new m();
                    this.p.a.a((int)(18 + i2 * 5)).h().m.a(0);
                    this.p.a.a((int)(18 + i2 * 5)).h().m.a = (byte)2;
                    this.p.a.a((int)(18 + i2 * 5)).h().m.a(258, false, (byte)-1);
                }
                if (this.p.a.a((int)(18 + i2 * 5)).h().i == null) {
                    this.p.a.a((int)(18 + i2 * 5)).h().i = new m();
                    this.p.a.a((int)(18 + i2 * 5)).h().i.a(0);
                    this.p.a.a((int)(18 + i2 * 5)).h().i.a = (byte)2;
                    this.p.a.a((int)(18 + i2 * 5)).h().i.a(258, false, (byte)-1);
                }
                this.p.a.a((int)(18 + i2 * 5)).h().m.a(aq.c[4][v3[0]][1]);
                this.p.a.a((int)(18 + i2 * 5)).h().i.a(aq.c[4][v3[0]][1]);
                this.p.a.a((int)(19 + i2 * 5)).h().a = an.f(aq.c[4][v3[0]][0]);
                this.p.a.a((int)(20 + i2 * 5)).h().a = "" + v3[1];
                continue;
            }
            if (this.p.a.a((int)(18 + i2 * 5)).h().m != null) {
                this.p.a.a((int)(18 + i2 * 5)).h().m.d();
            }
            this.p.a.a((int)(19 + i2 * 5)).h().a = "";
            this.p.a.a((int)(20 + i2 * 5)).h().a = "";
        }
        this.p.a.a((int)46).h().a = i1 > 0 ? (this.h < this.q.K.size() ? an.f(aq.c[4][((int[])this.q.K.elementAt(this.h))[0]][2]) : an.f(aq.c[4][((int[])this.q.J.elementAt(this.h - this.q.K.size()))[0]][2])) : "";
        if (i1 > 0) {
            this.p.a.a(43).b(127 + this.h * 72 / i1, this.p.a.a());
            return;
        }
        this.p.a.a(43).b(127, this.p.a.a());
    }

    private void bl() {
        int i1;
        if (this.q.N.size() > 5) {
            ((al)this.p.a.a((int)125)).a.a(1);
        } else {
            ((al)this.p.a.a((int)125)).a.a(0);
        }
        ((al)this.p.a.a((int)125)).a.a = this.q.N.size();
        this.w = ((al)this.p.a.a((int)125)).a.e;
        this.h = ((al)this.p.a.a((int)125)).a.f;
        for (i1 = 0; i1 < 5; ++i1) {
            if (this.w + i1 < this.q.N.size()) {
                int[] v2 = (int[])this.q.N.elementAt(this.w + i1);
                if (this.p.a.a((int)(137 + i1 * 5)).h().m == null) {
                    this.p.a.a((int)(137 + i1 * 5)).h().m = new m();
                    this.p.a.a((int)(137 + i1 * 5)).h().m.a(0);
                    this.p.a.a((int)(137 + i1 * 5)).h().m.a = (byte)2;
                    this.p.a.a((int)(137 + i1 * 5)).h().m.a(258, false, (byte)-1);
                }
                if (this.p.a.a((int)(137 + i1 * 5)).h().i == null) {
                    this.p.a.a((int)(137 + i1 * 5)).h().i = new m();
                    this.p.a.a((int)(137 + i1 * 5)).h().i.a(0);
                    this.p.a.a((int)(137 + i1 * 5)).h().i.a = (byte)2;
                    this.p.a.a((int)(137 + i1 * 5)).h().i.a(258, false, (byte)-1);
                }
                this.p.a.a((int)(137 + i1 * 5)).h().m.a(aq.c[5][v2[0]][1]);
                this.p.a.a((int)(137 + i1 * 5)).h().i.a(aq.c[5][v2[0]][1]);
                this.p.a.a((int)(138 + i1 * 5)).h().a = an.f(aq.c[5][v2[0]][0]);
                switch (v2[0]) {
                    case 0: {
                        if (this.q.k(v2[0])) {
                            this.p.a.a((int)163).h().a = an.f(aq.c[5][v2[0]][2]);
                            if (game.k.a().r()) {
                                this.p.a.a((int)(139 + i1 * 5)).h().a = "Ho\u00e0n th\u00e0nh";
                                break;
                            }
                            this.p.a.a((int)(139 + i1 * 5)).h().a = "1 c\u00e1i";
                            break;
                        }
                        this.p.a.a((int)163).h().a = an.f(634);
                        this.p.a.a((int)(139 + i1 * 5)).h().a = "0 c\u00e1i";
                        break;
                    }
                    default: {
                        this.p.a.a((int)(139 + i1 * 5)).h().a = "";
                        break;
                    }
                }
                continue;
            }
            if (this.p.a.a((int)(137 + i1 * 5)).h().m != null) {
                this.p.a.a((int)(137 + i1 * 5)).h().m.d();
            }
            this.p.a.a((int)(138 + i1 * 5)).h().a = "";
            this.p.a.a((int)(139 + i1 * 5)).h().a = "";
        }
        if (this.q.N.size() > 0) {
            i1 = ((int[])this.q.N.elementAt(this.h))[0];
            if (i1 != 0) {
                this.p.a.a((int)163).h().a = an.f(aq.c[5][i1][2]);
                this.p.a.a(7).a(true);
            }
            this.p.a.a((int)7).h().a = i1 == 0 ? (((int[])this.q.N.elementAt(this.h))[1] == 1 ? "\u0110\u00f3ng c\u1eeda" : "M\u1edf ra") : (i1 > 0 || i1 <= 4 ? (this.q.t == i1 - 1 ? "Tri\u1ec7u h\u1ed3i" : "Tri\u1ec7u ho\u00e1n") : (i1 == 10 ? "Gia t\u1ed1c" : "S\u1eed d\u1ee5ng"));
        } else {
            this.p.a.a((int)163).h().a = "";
            this.p.a.a(7).a(false);
        }
        if (this.q.N.size() > 0) {
            this.p.a.a(162).b(127 + this.h * 72 / this.q.N.size(), this.p.a.a());
            return;
        }
        this.p.a.a(162).b(127, this.p.a.a());
    }

    public final void Z() {
        if (this.f == 0 && this.o.k(4100)) {
            this.p.a.b(0);
            this.f(this.c);
            return;
        }
        if (this.f == 0 && this.o.k(8448)) {
            this.p.a.b(1);
            this.f(this.c);
            return;
        }
        if (this.o.k(196640)) {
            this.bo();
            return;
        }
        if (this.f == 0 && this.o.k(262144)) {
            this.o.a((byte)8);
            this.p.a("/data/ui/petstate.ui");
        }
    }

    public final void aa() {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            this.f(this.b);
            return;
        }
        if (this.o.k(8448)) {
            this.p.a.b(1);
            this.f(this.b);
            return;
        }
        if (this.o.k(196640)) {
            this.q.f(this.s, this.b);
            this.o.a((byte)8);
            return;
        }
        if (this.o.k(262144)) {
            this.o.a((byte)8);
            this.p.a("/data/ui/petstate.ui");
        }
    }

    public final void ab() {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            this.f(this.b);
            return;
        }
        if (this.o.k(8448)) {
            this.p.a.b(1);
            this.f(this.b);
            return;
        }
        if (this.o.k(196640)) {
            if (this.f == 0) {
                if (this.q.z[this.b].s() < 50) {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("Ch\u1ec9 c\u00f3 th\u1ec3 cho 50 c\u1ea5p s\u1ee7ng v\u1eadt s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    this.f = 2;
                    return;
                }
                if (this.q.e(this.s, this.b)) {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("S\u1eed d\u1ee5ng th\u00e0nh c\u00f4ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    this.f = 1;
                    return;
                }
            } else {
                if (this.f == 1) {
                    this.f = 0;
                    this.o.a((byte)8);
                    this.p.a("/data/ui/msgwarm.ui");
                    this.p.a("/data/ui/petstate.ui");
                    return;
                }
                if (this.f == 2) {
                    this.f = 0;
                    this.p.a("/data/ui/msgwarm.ui");
                    return;
                }
            }
        } else if (this.o.k(262144) && this.f == 0) {
            this.o.a((byte)8);
            this.p.a("/data/ui/petstate.ui");
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    public final void ac() {
        block46: {
            block50: {
                block54: {
                    block52: {
                        block44: {
                            int i4;
                            block53: {
                                block51: {
                                    block49: {
                                        block48: {
                                            block47: {
                                                block45: {
                                                    this.o.l();
                                                    if (this.f != 0 || !this.o.k(16400) || this.j() || an.b(this.b, 1)) break block45;
                                                    this.p.a.b(7);
                                                    this.p.a.b(2);
                                                    this.p.a.b(5);
                                                    this.bi();
                                                    this.o.m();
                                                    break block46;
                                                }
                                                if (this.f != 0 || !this.o.k(32832) || this.j() || an.b(this.b, 1)) break block47;
                                                this.p.a.b(7);
                                                this.p.a.b(3);
                                                this.p.a.b(5);
                                                this.bi();
                                                this.o.m();
                                                break block46;
                                            }
                                            if (this.f != 0 || !this.o.k(4100) || this.j() || an.b(this.h, 0)) break block48;
                                            this.p.a.b(0);
                                            break block46;
                                        }
                                        if (this.f != 0 || !this.o.k(8448) || this.j() || an.b(this.h, 0)) break block49;
                                        this.p.a.b(1);
                                        break block46;
                                    }
                                    if (!this.o.k(196640) || this.j() || !an.I()) break block50;
                                    if (this.f != 0) break block51;
                                    if (an.H() && !an.b(this.h, 0)) {
                                        return;
                                    }
                                    block0 : switch (this.b) {
                                        case 0: {
                                            int[] v1;
                                            if (this.h >= this.q.K.size()) {
                                                if (this.q.J.size() <= 0) {
                                                    return;
                                                }
                                                v1 = (int[])this.q.J.elementAt(this.h - this.q.K.size());
                                            } else {
                                                v1 = (int[])this.q.K.elementAt(this.h);
                                            }
                                            switch (v1[0]) {
                                                case 0: 
                                                case 1: 
                                                case 2: 
                                                case 3: {
                                                    if (this.f == 0) {
                                                        this.p.a("/data/ui/msgwarm.ui", 257, this);
                                                        this.a("Kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                                        this.f = 1;
                                                        break;
                                                    }
                                                    this.p.a("/data/ui/msgwarm.ui");
                                                    this.f = 0;
                                                    break;
                                                }
                                                case 14: {
                                                    if (this.f != 0) break;
                                                    if (this.q.k(0) && (this.q.I == 0 && game.k.q < 10 || this.q.I > 0 && game.k.q < 30)) {
                                                        if (!this.q.b(v1[0], 1, (byte)0)) break;
                                                        game.k.q = this.q.I == 0 ? 10 : 30;
                                                        this.q.d(v1[0], 1, (byte)0);
                                                        int i1 = this.q.K.size() + this.q.J.size();
                                                        if (this.h >= i1) {
                                                            ((al)this.p.a.a((int)8)).a.f = this.h = i1 - 1;
                                                        }
                                                        if (this.w > 0 && this.h - this.w < 4) {
                                                            --this.w;
                                                            ((al)this.p.a.a((int)8)).a.e = this.w;
                                                        }
                                                        this.bk();
                                                        this.p.a("/data/ui/msgwarm.ui", 257, this);
                                                        this.a("Th\u00e0nh c\u00f4ng s\u1eed d\u1ee5ng, tranh th\u1ee7 th\u1eddi gian \u0111i \u1ea5p tr\u1ee9ng tr\u01b0\u0301ng su\u0309ng v\u00e2\u0323t a!", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                                        this.f = 1;
                                                        break;
                                                    }
                                                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                                                    this.a("Kh\u00f4ng c\u00f3 tr\u1ee9ng c\u00f3 th\u1ec3 \u1ea5p tr\u1ee9ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                                    this.f = 1;
                                                    break;
                                                }
                                                case 13: {
                                                    if (this.f != 0) break;
                                                    if (this.q.x <= 0) {
                                                        if (game.k.a().f == 3 && game.k.a().g == 7) {
                                                            this.E();
                                                            this.a("N\u01a1i n\u00e0y kh\u00f4ng c\u00e1ch n\u00e0o s\u1eed d\u1ee5ng tr\u00e1nh qu\u00e1i ho\u00e0n", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                                            this.f = 1;
                                                            break;
                                                        }
                                                        if (!this.q.b(v1[0], 1, (byte)0)) break;
                                                        this.q.d(v1[0], 1, (byte)0);
                                                        this.q.x = aq.c[4][v1[0]][6];
                                                        this.q.w = 0;
                                                        int i1 = this.q.K.size() + this.q.J.size();
                                                        if (this.h >= i1) {
                                                            ((al)this.p.a.a((int)8)).a.f = this.h = i1 - 1;
                                                        }
                                                        if (this.w > 0 && this.h - this.w < 4) {
                                                            --this.w;
                                                            ((al)this.p.a.a((int)8)).a.e = this.w;
                                                        }
                                                        this.bk();
                                                        this.E();
                                                        this.q.c(1);
                                                        this.a("Th\u00e0nh c\u00f4ng s\u1eed d\u1ee5ng \u0111\u1ea1o c\u1ee5, c\u0169ng c\u00f3 th\u1eddi gian ng\u1eafn tr\u00e1nh qu\u00e1i hi\u1ec7u qu\u1ea3", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                                        this.f = 1;
                                                        break;
                                                    }
                                                    this.E();
                                                    this.a("\u0110\u00e3 c\u00f3 \u0111\u01b0\u1ee3c th\u1eddi gian ng\u1eafn tr\u00e1nh qu\u00e1i hi\u1ec7u qu\u1ea3", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                                    this.f = 1;
                                                    break;
                                                }
                                                default: {
                                                    this.s = v1[0];
                                                    this.o.a((byte)17);
                                                    this.p.a("/data/ui/bag.ui");
                                                    break;
                                                }
                                            }
                                            break;
                                        }
                                        case 3: {
                                            int[] v1 = (int[])this.q.N.elementAt(this.h);
                                            switch (v1[0]) {
                                                case 0: {
                                                    if (!this.q.k(v1[0])) break block0;
                                                    if (game.k.a().r()) {
                                                        if (this.q.y() == 2) {
                                                            this.E();
                                                            this.a("Kh\u00f4ng gian kh\u00f4ng \u0111\u1ee7, th\u1ec9nh thanh l\u00fd kh\u00f4ng gian l\u1ea1i \u1ea5p tr\u1ee9ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                                            this.f = 1;
                                                            break block0;
                                                        }
                                                        game.k.q = 0;
                                                        if (game.k.a().M.b[game.k.a(4, 5)] != null) {
                                                            game.k.a().M.b[game.k.a((int)4, (int)5)][15] = 4;
                                                            if (game.k.a().f == 4 && game.k.a().g == 5) {
                                                                game.k.a().M.a[15].a((byte)4);
                                                            }
                                                        }
                                                        this.q.j(v1[0]);
                                                        this.bl();
                                                        this.E();
                                                        this.a("\u1ea4p tr\u1ee9ng th\u00e0nh c\u00f4ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                                        this.f = 2;
                                                        break block0;
                                                    }
                                                    this.E();
                                                    this.a("V\u1eabn ch\u01b0a th\u1ec3 \u1ea5p tr\u1ee9ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                                    this.f = 1;
                                                    break block0;
                                                }
                                                case 5: {
                                                    this.o.a((byte)11);
                                                    this.p.a("/data/ui/bag.ui");
                                                    break block0;
                                                }
                                                case 10: {
                                                    this.o.a((byte)24);
                                                    this.p.a("/data/ui/bag.ui");
                                                    break block0;
                                                }
                                                case 6: {
                                                    this.o.a((byte)12);
                                                    this.p.a("/data/ui/bag.ui");
                                                    break block0;
                                                }
                                                case 7: 
                                                case 8: 
                                                case 9: {
                                                    this.s = v1[0];
                                                    this.o.a((byte)19);
                                                    this.p.a("/data/ui/bag.ui");
                                                    break block0;
                                                }
                                            }
                                        }
                                    }
                                    break block46;
                                }
                                if (this.f != 1 && this.f != 2) break block46;
                                if (this.f != 2) break block52;
                                if (this.q.I != 0) break block53;
                                byte i1 = this.g(58);
                                this.q.a((short)58);
                                if (i1 == 0) {
                                    this.c("\u1ea4p tr\u1ee9ng t\u00ecm \u0111\u01b0\u1ee3c #2" + an.f(aq.c[0][58][0]) + "#0 \u0111\u1ec3 v\u00e0o ba l\u00f4");
                                    break block44;
                                } else if (i1 == 1) {
                                    this.c("\u1ea4p tr\u1ee9ng t\u00ecm \u0111\u01b0\u1ee3c #2" + an.f(aq.c[0][58][0]) + "#0 \u0111\u1ec3 v\u00e0o ng\u00e2n h\u00e0ng");
                                    break block44;
                                } else {
                                    this.c("Kh\u00f4ng c\u00f3 kh\u00f4ng gian, \u0111\u00e3 ph\u00f3ng sinh");
                                }
                                break block44;
                            }
                            int i1 = ae.a(new int[]{76, 52, 28, 4, 0}, ae.a(100));
                            short[] v2 = new short[]{0, 56, 58, 95, 72};
                            byte i3 = this.g(v2[i1]);
                            for (i4 = 0; i4 < this.q.I && this.q.R[i4] != v2[i1]; ++i4) {
                            }
                            if (i4 >= this.q.I) {
                                this.q.a(v2[i1]);
                            }
                            if (i3 == 0) {
                                this.c("\u1ea4p tr\u1ee9ng t\u00ecm \u0111\u01b0\u1ee3c #2" + an.f(aq.c[0][v2[i1]][0]) + "#0 \u0111\u1ec3 v\u00e0o ba l\u00f4");
                            } else if (i3 == 1) {
                                this.c("\u1ea4p tr\u1ee9ng t\u00ecm \u0111\u01b0\u1ee3c #2" + an.f(aq.c[0][v2[i1]][0]) + "#0 \u0111\u1ec3 v\u00e0o ng\u00e2n h\u00e0ng");
                            } else {
                                this.c("Kh\u00f4ng c\u00f3 kh\u00f4ng gian, \u0111\u00e3 ph\u00f3ng sinh");
                            }
                        }
                        this.f = 3;
                        break block54;
                    }
                    this.o.m();
                    this.f = 0;
                }
                this.F();
                break block46;
            }
            if (this.f == 0 && this.o.k(262144) && !this.j() && an.J()) {
                this.b = 2;
                this.o.a((byte)6);
                this.p.a("/data/ui/bag.ui");
            }
        }
        if (this.f == 3 && !this.j()) {
            this.o.m();
            this.bj();
            this.f = 0;
        }
        this.f();
        this.g = true;
    }

    private byte g(int i1) {
        int[][] v2 = new int[][]{{60, 20, 0}, {75, 50, 20, 0}};
        int i3 = -1;
        int i4 = 0;
        if (aq.c[0][i1][4] == 5) {
            if (aq.c[0][i1][3] == 2) {
                i3 = 1;
                i4 = 2;
            } else if (aq.c[0][i1][3] == 3) {
                i3 = 0;
                i4 = 3;
            }
        }
        int i5 = aq.c[0][i1][1] * 10;
        int i6 = aq.c[1][i5][5];
        byte i7 = this.q.y();
        if (i3 == -1) {
            if (i7 == 0) {
                this.q.a(i1, 5, (byte)2, (short)-1, new int[]{1, i5, i6});
            } else if (i7 == 1) {
                int i2 = ae.b(aq.c[0][i1][3], (int)aq.c[0][i1][3]);
                this.q.a(i1, 5, (byte)2, (byte)i2, game.b.b(i1, 5, i2), -1, new int[]{1, i5, i6});
            }
        } else {
            i4 = (byte)(i4 + (byte)ae.a(v2[i3], ae.a(100)));
            if (i7 == 0) {
                this.q.a(i1, 5, (byte)2, (short)i4, new int[]{1, i5, i6});
            } else if (i7 == 1) {
                this.q.a(i1, 5, (byte)2, (short)i4, game.b.b(i1, 5, i4), -1, new int[]{1, i5, i6});
            }
        }
        return i7;
    }

    public final void ad() {
        this.aS();
        this.p.a("/data/ui/ride.ui", 257, this);
        this.b = 0;
        this.bm();
    }

    private void bm() {
        for (int i1 = 0; i1 < 4; ++i1) {
            if (this.p.a.a((int)(i1 + 4)).h().m == null) {
                this.p.a.a((int)(i1 + 4)).h().m = new m();
                this.p.a.a((int)(i1 + 4)).h().m.a(0);
                this.p.a.a((int)(i1 + 4)).h().m.a = (byte)3;
                this.p.a.a((int)(i1 + 4)).h().m.a(260, false, (byte)-1);
            }
            if (this.p.a.a((int)(i1 + 16)).h().m == null) {
                this.p.a.a((int)(i1 + 16)).h().m = new m();
                this.p.a.a((int)(i1 + 16)).h().m.a(131);
                this.p.a.a((int)(i1 + 16)).h().m.a = (byte)2;
                this.p.a.a((int)(i1 + 16)).h().m.a(257, false, (byte)0);
            }
            if (this.q.f(i1)) {
                if (this.b == i1) {
                    this.p.a.a((int)(i1 + 4)).h().m.a((byte)i1, (byte)-1);
                    if (this.b == 0) {
                        this.p.a.a((int)(i1 + 8)).h().a = "L\u1ee5c \u0111i \u0111i\u1ec3u";
                    } else if (this.b == 1) {
                        this.p.a.a((int)(i1 + 8)).h().a = "H\u01b0 kh\u00f4ng h\u00e0nh gi\u1ea3";
                    } else if (this.b == 2) {
                        this.p.a.a((int)(i1 + 8)).h().a = "H\u1ea3i \u00e2u";
                    } else if (this.b == 3) {
                        this.p.a.a((int)(i1 + 8)).h().a = "Nham s\u01a1n long";
                    }
                } else {
                    this.p.a.a((int)(i1 + 4)).h().m.a((byte)(i1 + 8), (byte)-1);
                    this.p.a.a((int)(i1 + 8)).h().a = "";
                }
                if (!this.q.g(i1)) {
                    this.p.a.a(i1 + 16).a(true);
                    continue;
                }
                this.p.a.a(i1 + 16).a(false);
                continue;
            }
            this.p.a.a(i1 + 16).a(false);
            this.p.a.a((int)(i1 + 4)).h().m.a((byte)(i1 + 4), (byte)-1);
            this.p.a.a((int)(i1 + 8)).h().a = "";
        }
    }

    public final void ae() {
        if (!this.j() && this.o.k(16400)) {
            this.p.a.b(2);
        } else if (!this.j() && this.o.k(32832)) {
            this.p.a.b(3);
        } else if (!this.j() && this.o.k(512)) {
            this.p.a("/data/ui/ride.ui");
            this.o.a((byte)0);
        } else if (!this.j() && this.o.k(196640)) {
            if (this.q.f(this.b)) {
                if (this.q.g(this.b)) {
                    this.q.h(this.b);
                    this.p.a("/data/ui/ride.ui");
                    this.o.a((byte)0);
                } else {
                    this.b("N\u01a1i n\u00e0y kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng s\u1ee7ng v\u1eadt c\u01b0\u1ee1i");
                }
            } else {
                this.b("Ch\u01b0a c\u00f3 s\u1ee7ng v\u1eadt c\u01b0\u1ee1i n\u00e0y");
            }
        } else if (!this.j() && this.o.k(262144)) {
            this.p.a("/data/ui/ride.ui");
            this.o.a((byte)0);
        }
        this.f();
        this.g = true;
    }

    public final void a(b v1, b v2) {
        this.p.a("/data/ui/battle.ui", 257, this);
        this.a = 0;
        this.e = 0;
        this.a((b)v1, false);
        this.b(v2, false);
        v1 = this;
        this.p.a.a((int)59).h().a = "100%";
        ((h)v1).p.a.a((int)58).h().a = "100%";
        ((d)((h)v1).o).e();
        this.p.a("/data/ui/world.ui");
    }

    public final void b(b v1, b v2) {
        if (v1.a(v2) == 0) {
            if (v1.r() == 0) {
                this.p.a.a((int)59).h().a = "300%";
                this.p.a.a((int)58).h().a = "60%";
                return;
            }
            this.p.a.a((int)59).h().a = "60%";
            this.p.a.a((int)58).h().a = "300%";
            return;
        }
        if (v1.a(v2) == 1) {
            if (v1.r() == 0) {
                this.p.a.a((int)59).h().a = "60%";
                this.p.a.a((int)58).h().a = "300%";
                return;
            }
            this.p.a.a((int)59).h().a = "300%";
            this.p.a.a((int)58).h().a = "60%";
            return;
        }
        this.p.a.a((int)59).h().a = "100%";
        this.p.a.a((int)58).h().a = "100%";
    }

    public final void a(b v1, b v2, b v3, int i4, int i5) {
        if (v1.a(v2) == 0) {
            if (v3.r() == 0) {
                if ((i4 *= 200 / i5) == i5 && i4 != 200) {
                    i4 = 200;
                }
                this.p.a.a((int)59).h().a = i4 + 100 + "%";
                return;
            }
            if (v3.r() == 1) {
                if ((i4 *= 40 / i5) == i5 && i4 != 40) {
                    i4 = 40;
                }
                this.p.a.a((int)58).h().a = 100 - i4 + "%";
                return;
            }
        } else if (v1.a(v2) == 1) {
            if (v3.r() == 0) {
                if ((i4 *= 40 / i5) == i5 && i4 != 40) {
                    i4 = 40;
                }
                this.p.a.a((int)59).h().a = 100 - i4 + "%";
                return;
            }
            if (v3.r() == 1) {
                if ((i4 *= 200 / i5) == i5 && i4 != 200) {
                    i4 = 200;
                }
                this.p.a.a((int)58).h().a = i4 + 100 + "%";
                return;
            }
        } else {
            this.p.a.a((int)59).h().a = "100%";
            this.p.a.a((int)58).h().a = "100%";
        }
    }

    public final void a(b v1, b v2, int i3, int i4) {
        this.E = 0;
        this.D = 0;
        if (v1.a(v2) == 0) {
            this.D += i3 * (200 / i4);
            if (this.D == i4 && this.D != 200) {
                this.D = 200;
            }
            this.p.a.a((int)59).h().a = 100 + this.D + "%";
            this.E += i3 * (40 / i4);
            if (this.E == i4 && this.E != 40) {
                this.E = 40;
            }
            this.p.a.a((int)58).h().a = 100 - this.E + "%";
            return;
        }
        if (v1.a(v2) == 1) {
            this.D += i3 * (40 / i4);
            if (this.D == i4 && this.D != 40) {
                this.D = 40;
            }
            this.p.a.a((int)59).h().a = 100 - this.D + "%";
            this.E += i3 * (200 / i4);
            if (this.E == i4 && this.E != 200) {
                this.E = 200;
            }
            this.p.a.a((int)58).h().a = 100 + this.E + "%";
            return;
        }
        this.p.a.a((int)59).h().a = "100%";
        this.p.a.a((int)58).h().a = "100%";
    }

    public final boolean a(b v1, boolean i2) {
        int i5;
        int i4;
        int i3 = 0;
        if (this.F == 0 && (i3 = Math.abs(v1.N() - v1.d[1]) / 11) <= 1) {
            i3 = 1;
        }
        if ((i4 = v1.N()) != (i5 = v1.d[1])) {
            ++this.G;
            if (this.G < 4) {
                if (i2) {
                    this.p.a.a((int)55).h().a = "#P" + v1.L();
                    this.p.a.a((int)11).h().a = "#P" + v1.M();
                } else {
                    this.p.a.a((int)55).h().a = "#P" + v1.M();
                    this.p.a.a((int)11).h().a = "#P" + v1.L();
                }
                return false;
            }
        }
        this.F += i3;
        if (i2) {
            if ((i4 += this.F) >= i5) {
                i4 = i5;
            }
            v1.u(i4);
            this.p.a.a((int)41).h().a = "#P" + v1.L();
            this.p.a.a((int)11).h().a = "#P" + v1.M();
            this.p.a.a((int)55).h().a = "#P" + v1.M();
        } else {
            if ((i4 -= this.F) <= i5) {
                i4 = i5;
            }
            v1.u(i4);
            this.p.a.a((int)41).h().a = "#P" + v1.M();
            this.p.a.a((int)55).h().a = "#P" + v1.L();
            this.p.a.a((int)11).h().a = "#P" + v1.L();
        }
        this.p.a.a((int)38).h().a = v1.N() + "/" + v1.c[1];
        this.p.a.a((int)9).h().a = "#P" + v1.O();
        this.p.a.a((int)40).h().a = v1.z() + "/" + v1.u();
        this.p.a.a((int)12).h().a = an.f(v1.j((byte)0));
        this.p.a.a((int)13).h().a = "lv" + v1.s();
        this.p.a.a((int)17).h().m.a(94 + v1.j((byte)1));
        if (i4 == i5) {
            this.F = 0;
            this.G = 0;
            this.k = 0;
            return true;
        }
        return false;
    }

    public final void a(b v1) {
        int i2;
        for (i2 = 0; i2 < 6; ++i2) {
            if (this.p.a.a((int)(i2 + 26)).h().m == null) {
                this.p.a.a((int)(i2 + 26)).h().m = new m();
                this.p.a.a((int)(i2 + 26)).h().m.a = (byte)2;
                this.p.a.a((int)(i2 + 26)).h().m.a(0);
                this.p.a.a((int)(i2 + 26)).h().m.a(325, false, (byte)0);
            }
            if (this.p.a.a((int)(i2 + 43)).h().m == null) {
                this.p.a.a((int)(i2 + 43)).h().m = new m();
                this.p.a.a((int)(i2 + 43)).h().m.a = (byte)2;
                this.p.a.a((int)(i2 + 43)).h().m.a(145);
                this.p.a.a((int)(i2 + 43)).h().m.a(257, false, (byte)0);
            }
            this.p.a.a((int)(i2 + 43)).h().m.a(145);
            this.p.a.a((int)(i2 + 26)).h().m.a(0);
        }
        for (i2 = 0; i2 < 3; ++i2) {
            if (v1.x[0][i2] != -1 && v1.v[v1.x[0][i2]][0] > 0) {
                this.p.a.a((int)(43 + this.k)).h().m.a(134 + v1.v[v1.x[0][i2]][0]);
                this.p.a.a((int)(26 + this.k)).h().m.a(v1.x[0][i2] + 12);
                ++this.k;
            }
            if (v1.x[1][i2] == -1 || v1.w[v1.x[1][i2]][0] <= 0) continue;
            this.p.a.a((int)(43 + this.k)).h().m.a(134 + v1.w[v1.x[1][i2]][0]);
            this.p.a.a((int)(26 + this.k)).h().m.a(v1.x[1][i2] + 1);
            ++this.k;
        }
    }

    private void g(b v1) {
        this.p.a.a((int)11).h().a = "#P" + v1.L();
        this.p.a.a((int)38).h().a = v1.N() + "/" + v1.c[1];
        this.p.a.a((int)16).h().a = "lv" + v1.s();
    }

    public final boolean b(b v1, boolean i2) {
        int i5;
        int i4;
        int i3 = 0;
        if (this.F == 0 && (i3 = Math.abs(v1.N() - v1.d[1]) / 11) <= 1) {
            i3 = 1;
        }
        if ((i4 = v1.N()) != (i5 = v1.d[1])) {
            ++this.G;
            if (this.G < 4) {
                if (i2) {
                    this.p.a.a((int)56).h().a = "#P" + v1.L();
                    this.p.a.a((int)14).h().a = "#P" + v1.M();
                } else {
                    this.p.a.a((int)56).h().a = "#P" + v1.M();
                    this.p.a.a((int)14).h().a = "#P" + v1.L();
                }
                return false;
            }
        }
        this.F += i3;
        if (i2) {
            if ((i4 += this.F) >= i5) {
                i4 = i5;
            }
            v1.u(i4);
            this.p.a.a((int)42).h().a = "#P" + v1.L();
            this.p.a.a((int)14).h().a = "#P" + v1.M();
            this.p.a.a((int)56).h().a = "#P" + v1.M();
        } else {
            if ((i4 -= this.F) <= i5) {
                i4 = i5;
            }
            v1.u(i4);
            this.p.a.a((int)42).h().a = "#P" + v1.M();
            this.p.a.a((int)14).h().a = "#P" + v1.L();
            this.p.a.a((int)56).h().a = "#P" + v1.L();
        }
        this.p.a.a((int)39).h().a = v1.N() + "/" + v1.c[1];
        if (this.q.a((byte)v1.j((byte)1), v1.q()) == 2) {
            this.p.a.a((int)19).h().m.a(101);
        } else {
            this.p.a.a((int)19).h().m.a(102);
        }
        this.p.a.a((int)15).h().a = an.f(v1.j((byte)0));
        this.p.a.a((int)16).h().a = "lv" + v1.s();
        this.p.a.a((int)18).h().m.a(94 + v1.j((byte)1));
        if (i4 == i5) {
            this.F = 0;
            this.G = 0;
            this.k = 0;
            return true;
        }
        return false;
    }

    public final void b(b v1) {
        int i2;
        for (i2 = 0; i2 < 6; ++i2) {
            if (this.p.a.a((int)(i2 + 32)).h().m == null) {
                this.p.a.a((int)(i2 + 32)).h().m = new m();
                this.p.a.a((int)(i2 + 32)).h().m.a = (byte)2;
                this.p.a.a((int)(i2 + 32)).h().m.a(0);
                this.p.a.a((int)(i2 + 32)).h().m.a(325, false, (byte)0);
            }
            if (this.p.a.a((int)(i2 + 49)).h().m == null) {
                this.p.a.a((int)(i2 + 49)).h().m = new m();
                this.p.a.a((int)(i2 + 49)).h().m.a = (byte)2;
                this.p.a.a((int)(i2 + 49)).h().m.a(145);
                this.p.a.a((int)(i2 + 49)).h().m.a(257, false, (byte)0);
            }
            this.p.a.a((int)(i2 + 49)).h().m.a(145);
            this.p.a.a((int)(i2 + 32)).h().m.a(0);
        }
        for (i2 = 0; i2 < 3; ++i2) {
            if (v1.x[0][i2] != -1 && v1.v[v1.x[0][i2]][0] > 0) {
                this.p.a.a((int)(49 + this.k)).h().m.a(134 + v1.v[v1.x[0][i2]][0]);
                this.p.a.a((int)(32 + this.k)).h().m.a(v1.x[0][i2] + 12);
                ++this.k;
            }
            if (v1.x[1][i2] == -1 || v1.w[v1.x[1][i2]][0] <= 0) continue;
            this.p.a.a((int)(49 + this.k)).h().m.a(134 + v1.w[v1.x[1][i2]][0]);
            this.p.a.a((int)(32 + this.k)).h().m.a(v1.x[1][i2] + 1);
            ++this.k;
        }
    }

    public final void af() {
        this.a = 0;
        this.y = null;
        this.p.a("/data/ui/battle.ui");
    }

    private void a(boolean i1) {
        this.p.a.a(20 + this.a).a(i1);
    }

    public final void ag() {
        ((al)this.p.a.a((int)0)).b.f = this.a;
        this.a(true);
    }

    public final void c(b v1) {
        this.f = 0;
        this.a(v1, false);
        this.ag();
    }

    public final void d(b v1) {
        ((d)this.o).l();
        if (!an.b(this.a, 1) && this.f == 0 && !this.j() && this.o.k(16400)) {
            this.p.a.b(2);
        } else if (!an.b(this.a, 1) && this.f == 0 && !this.j() && this.o.k(32832)) {
            this.p.a.b(3);
        } else if (!this.j() && this.o.k(196640)) {
            switch (this.a) {
                case 0: {
                    this.a(false);
                    this.o.a((byte)3);
                    break;
                }
                case 2: {
                    if (this.f == 0) {
                        if (v1.p(2)) {
                            this.p.a("/data/ui/msgwarm.ui", 257, this);
                            this.a("Tr\u1ea1ng th\u00e1i b\u1ecb qu\u1ea5n, kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng \u0111\u1ea1o c\u1ee5", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                            this.f = 1;
                            break;
                        }
                        this.a(false);
                        this.o.a((byte)4);
                        break;
                    }
                    this.p.a("/data/ui/msgwarm.ui");
                    this.f = 0;
                    break;
                }
                case 3: {
                    if (this.f == 0) {
                        if (v1.p(2)) {
                            this.p.a("/data/ui/msgwarm.ui", 257, this);
                            this.a("Tr\u1ea1ng th\u00e1i b\u1ecb qu\u1ea5n, kh\u00f4ng th\u1ec3 \u0111\u1ed5i s\u1ee7ng v\u1eadt", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                            this.f = 1;
                            break;
                        }
                        this.a(false);
                        ((d)this.o).g = ((d)this.o).e[((d)this.o).i];
                        game.d.a().k = true;
                        this.o.a((byte)5);
                        break;
                    }
                    this.p.a("/data/ui/msgwarm.ui");
                    this.f = 0;
                    break;
                }
                case 1: {
                    if (((d)this.o).b == 2) {
                        this.b("Tr\u1eadn chi\u1ebfn n\u00e0y kh\u00f4ng cho b\u1eaft s\u1ee7ng v\u1eadt");
                        break;
                    }
                    if (this.q.y() == 2) {
                        this.b("Kh\u00f4ng gian kh\u00f4ng \u0111\u1ee7, kh\u00f4ng c\u00e1ch n\u00e0o b\u1eaft \u0111\u01b0\u1ee3c");
                        break;
                    }
                    this.b = 0;
                    this.a(false);
                    ((d)this.o).m();
                    this.o.a((byte)21);
                    break;
                }
                case 4: {
                    this.a(false);
                    this.o.a((byte)11);
                    break;
                }
                case 5: {
                    if (this.f == 0) {
                        if (v1.p(2)) {
                            this.p.a("/data/ui/msgwarm.ui", 257, this);
                            this.a("Tr\u1ea1ng th\u00e1i b\u1ecb qu\u1ea5n, kh\u00f4ng th\u1ec3 ch\u1ea1y tr\u1ed1n", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                            this.f = 1;
                            break;
                        }
                        if (((d)this.o).b > 0 || !game.c.j) {
                            this.a(false);
                            this.f = 3;
                            this.b("Tr\u1eadn chi\u1ebfn n\u00e0y kh\u00f4ng th\u1ec3 tr\u1ed1n ch\u1ea1y");
                            break;
                        }
                        boolean i1 = false;
                        if (((d)this.o).h.s() > ((d)this.o).d[0].s()) {
                            i1 = true;
                        } else if (((d)this.o).h.s() == ((d)this.o).d[0].s()) {
                            if (ae.a(100) <= 95) {
                                i1 = true;
                            }
                        } else {
                            int i2 = ((d)this.o).d[0].s() - ((d)this.o).h.s();
                            if ((i2 = 95 - i2 * 10) <= 15) {
                                i2 = 15;
                            }
                            if (ae.a(100) < i2) {
                                i1 = true;
                            }
                        }
                        if (i1) {
                            this.a(false);
                            game.i.a().a((byte)10);
                            break;
                        }
                        this.f = 2;
                        this.b("Ch\u1ea1y tr\u1ed1n th\u1ea5t b\u1ea1i");
                        break;
                    }
                    this.p.a("/data/ui/msgwarm.ui");
                    this.f = 0;
                }
            }
        }
        this.f();
        if (this.f >= 2 && this.ax()) {
            if (this.f == 2) {
                ((d)this.o).h.J = true;
                ((d)this.o).i = (byte)(((d)this.o).i + 1);
                this.o.a((byte)1);
            } else {
                this.a(true);
            }
            this.f = 0;
        }
    }

    public final void e(b v1) {
        this.p.a("/data/ui/choiceskill.ui", 257, this);
        ((al)this.p.a.a((int)0)).a.a = v1.E();
        if (this.e >= v1.E()) {
            this.e = v1.E() - 1;
        }
        if (v1.E() > 5) {
            ((al)this.p.a.a((int)0)).a.a(1);
        } else {
            ((al)this.p.a.a((int)0)).a.a(-1);
        }
        this.p.a.a((int)5).h().a = "S\u1eed d\u1ee5ng";
        ((al)this.p.a.a((int)0)).a.f = this.e;
        this.h(v1);
        this.f = 0;
    }

    private void h(b v1) {
        this.w = ((al)this.p.a.a((int)0)).a.e;
        this.h = ((al)this.p.a.a((int)0)).a.f;
        int i2 = v1.E();
        for (int i3 = 0; i3 < 5; ++i3) {
            if (i3 >= i2) {
                this.p.a.a((int)(13 + i3 * 5)).h().a = "";
                this.p.a.a((int)(14 + i3 * 5)).h().a = "";
                continue;
            }
            this.p.a.a((int)(13 + i3 * 5)).h().a = an.f(aq.c[1][v1.t(this.w + i3)][1]);
            this.p.a.a((int)(14 + i3 * 5)).h().a = v1.y[this.w + i3] + "/" + aq.c[1][v1.t(this.w + i3)][5];
        }
        this.h(v1.z[this.e]);
        this.p.a.a(51).b(98 + this.h * 72 / i2, this.p.a.a());
    }

    private void h(int i1) {
        this.p.a.a((int)53).h().a = an.f(aq.c[1][i1][2]);
    }

    public final void f(b v1) {
        if (this.ay()) {
            if (this.f == 0 && this.o.k(4100)) {
                this.p.a.b(0);
                this.h(v1);
            } else if (this.f == 0 && this.o.k(8448)) {
                this.p.a.b(1);
                this.h(v1);
            } else if (this.o.k(196640)) {
                if (this.f == 0) {
                    if (v1.s(this.e)) {
                        this.p.a("/data/ui/choiceskill.ui");
                        ((d)this.o).b(v1.z[this.e]);
                        int n2 = ((d)this.o).a;
                        ((d)this.o).getClass();
                        if (n2 == 0) {
                            ((d)this.o).i();
                        } else {
                            this.o.a((byte)6);
                        }
                    } else {
                        this.f = 1;
                        this.p.a("/data/ui/msgwarm.ui", 257, this);
                        this.a("K\u1ef9 n\u0103ng gi\u00e1 tr\u1ecb ch\u01b0a \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    }
                } else {
                    this.f = 0;
                    this.p.a("/data/ui/msgwarm.ui");
                    if (v1.p(2) && v1.r() == 0) {
                        boolean i2 = false;
                        for (int i3 = 0; i3 < v1.y.length; ++i3) {
                            if (v1.y[i3] == 0) continue;
                            i2 = true;
                        }
                        if (!i2) {
                            this.p.a("/data/ui/choiceskill.ui");
                            this.c("Kh\u00f4ng c\u00f3 k\u1ef9 n\u0103ng gi\u00e1 tr\u1ecb, kh\u00f4ng c\u00e1ch n\u00e0o chi\u1ebfn \u0111\u1ea5u");
                            ((d)this.o).h();
                        }
                    }
                }
            } else if (this.o.k(262144) && this.f == 0) {
                this.p.a("/data/ui/choiceskill.ui");
                this.o.a((byte)20);
            }
        }
        this.g();
    }

    public final void ah() {
        this.f = 0;
        this.p.a("/data/ui/choice.ui", 257, this);
        this.p.a.a((int)8).h().a = "Pokemon ball";
        this.p.a.a((int)9).h().a = "T\u1ec9 l\u1ec7 b\u1eaft";
        this.p.a.a((int)5).h().a = "S\u1eed d\u1ee5ng";
        ((al)this.p.a.a((int)0)).a.f = this.b;
        ((al)this.p.a.a((int)0)).a.a(0);
        ((al)this.p.a.a((int)0)).a.a = this.q.K.size();
        for (int i1 = 0; i1 < this.q.K.size(); ++i1) {
            int[] v2 = (int[])this.q.K.elementAt(i1);
            if (this.p.a.a((int)(i1 + 54)).h().m == null) {
                this.p.a.a((int)(i1 + 54)).h().m = new m();
                this.p.a.a((int)(i1 + 54)).h().m.a(0);
                this.p.a.a((int)(i1 + 54)).h().m.a = (byte)2;
                this.p.a.a((int)(i1 + 54)).h().m.a(258, false, (byte)-1);
            }
            this.p.a.a((int)(i1 + 54)).h().m.a(aq.c[4][v2[0]][1]);
            this.p.a.a((int)(13 + i1 * 5)).h().a = an.f(aq.c[4][v2[0]][0]);
            this.p.a.a((int)(14 + i1 * 5)).h().a = ((d)this.o).b(v2[0]) + "%";
        }
        this.p.a.a(59).a(false);
        this.p.a.a(60).a(false);
        this.bn();
    }

    private void bn() {
        int[] v1 = (int[])this.q.K.elementAt(this.b);
        this.p.a.a((int)53).h().a = "S\u1ed1 l\u01b0\u1ee3ng: " + v1[1] + " c\u00e1i ";
    }

    public final void ai() {
        this.o.l();
        if (!an.b(this.b, 0) && this.f == 0 && this.o.k(4100) && !this.j()) {
            this.p.a.b(0);
            this.bn();
        } else if (!an.b(this.b, 0) && this.f == 0 && this.o.k(8448) && !this.j()) {
            this.p.a.b(1);
            this.bn();
        } else if (this.o.k(196640) && !this.j() && an.I()) {
            if (an.H() && !an.b(this.b, 0)) {
                return;
            }
            if (this.f == 0) {
                int[] v1 = (int[])this.q.K.elementAt(this.b);
                if (!this.q.b(v1[0], 1, (byte)0)) {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("S\u1ed1 l\u01b0\u1ee3ng Pokemon ball kh\u00f4ng \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    this.f = 1;
                } else {
                    this.f = 0;
                    game.d.l = (byte)v1[0];
                    this.o.m();
                    this.q.d(v1[0], 1, (byte)0);
                    this.o.a((byte)17);
                    this.p.a("/data/ui/choice.ui");
                }
            } else if (this.f == 1) {
                if (((int[])this.q.K.elementAt(this.b))[0] == 0) {
                    this.p.a("/data/ui/choice.ui");
                    this.o.a((byte)101);
                }
                this.f = 0;
                this.p.a("/data/ui/msgwarm.ui");
            }
        } else if (game.c.J() && this.f == 0 && this.o.k(262144) && !this.j()) {
            this.p.a("/data/ui/choice.ui");
            this.o.a((byte)20);
        }
        this.g();
    }

    public final void aj() {
        this.s = 0;
        this.f = 0;
        this.b = 0;
        this.p.a("/data/ui/choice.ui", 257, this);
        this.p.a.a((int)8).h().a = "\u0110\u1ea1o c\u1ee5";
        this.p.a.a((int)9).h().a = "S\u1ed1 l\u01b0\u1ee3ng";
        this.p.a.a((int)5).h().a = "S\u1eed d\u1ee5ng";
        this.p.a.a(59).a(false);
        this.p.a.a(60).a(false);
        this.be();
    }

    public final void ak() {
        if (this.f == 0 && this.o.k(4100)) {
            this.p.a.b(0);
            return;
        }
        if (this.f == 0 && this.o.k(8448)) {
            this.p.a.b(1);
            return;
        }
        if (this.o.k(196640)) {
            if (this.q.J.size() <= 0) {
                return;
            }
            this.s = ((int[])this.q.J.elementAt(this.h))[0];
            if (this.f == 0) {
                switch (aq.c[4][this.s][5]) {
                    case 7: 
                    case 8: 
                    case 9: 
                    case 10: {
                        this.p.a("/data/ui/msgwarm.ui", 257, this);
                        this.a("Trong chi\u1ebfn \u0111\u1ea5u kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                        this.f = 1;
                        return;
                    }
                }
                this.o.a((byte)16);
                this.p.a("/data/ui/choice.ui");
                return;
            }
            if (this.f == 1) {
                this.p.a("/data/ui/msgwarm.ui");
                this.f = 0;
                return;
            }
        } else if (this.f == 0 && this.o.k(262144)) {
            this.p.a("/data/ui/choice.ui");
            this.o.a((byte)20);
        }
    }

    private void bo() {
        if (this.f == 0) {
            this.f = 1;
            int i1 = this.o instanceof k ? this.q.z[this.c].x(this.s) : this.q.z[((d)this.o).f[this.c]].x(this.s);
            switch (i1) {
                case 0: {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("S\u1ee7ng v\u1eadt n\u00e0y \u0111\u00e3 t\u1eed vong, kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    return;
                }
                case 1: {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("S\u1ee7ng v\u1eadt n\u00e0y kh\u00f4ng c\u00f3, kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    return;
                }
                case 2: {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("M\u00e1u \u0111\u1ea7y, kh\u00f4ng c\u1ea7n s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    return;
                }
                case 3: {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("K\u1ef9 n\u0103ng gi\u00e1 tr\u1ecb \u0111\u00e3 \u0111\u1ea7y, kh\u00f4ng c\u1ea7n s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    return;
                }
                case 4: {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("Tr\u00ean ng\u01b0\u1eddi \u0111\u1ec1u b\u1ecb l\u1ee3i hi\u1ec7u qu\u1ea3", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    return;
                }
                case 5: {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("Trong h\u01b0ng ph\u1ea5n, kh\u00f4ng th\u1ec3 d\u00f9ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    return;
                }
                case 7: {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("M\u00e1u v\u00e0 k\u1ef9 n\u0103ng \u0111\u1ec1u \u0111\u00e3 \u0111\u1ea7y, kh\u00f4ng c\u1ea7n s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    return;
                }
                case 8: {
                    this.p.a("/data/ui/msgwarm.ui", 257, this);
                    this.a("S\u1ee7ng v\u1eadt \u0111\u00e3 ch\u1ebft, kh\u00f4ng th\u1ec3 s\u1eed d\u1ee5ng", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    return;
                }
            }
            if (this.q.b(this.s, 1, (byte)0)) {
                if (this.o instanceof k) {
                    this.q.z[this.c].w(this.s);
                } else {
                    ((d)this.o).h.J = true;
                    this.q.z[((d)this.o).f[this.c]].w(this.s);
                }
                this.e(this.c);
                this.f = 1;
                this.l = true;
                this.p.a("/data/ui/msgwarm.ui", 257, this);
                this.a("Th\u00e0nh c\u00f4ng s\u1eed d\u1ee5ng \u0111\u1ea1o c\u1ee5", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                return;
            }
            this.f = 2;
            this.E();
            this.a("\u0110\u00e3 kh\u00f4ng c\u00f3 \u0111\u1ea1o n\u00e0y c\u1ee5, th\u1ec9nh mua s\u1eafm", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
            return;
        }
        if (this.f == 1) {
            this.f = 0;
            this.p.a("/data/ui/msgwarm.ui");
            return;
        }
        if (this.f == 2) {
            this.f = 0;
            this.p.a("/data/ui/msgwarm.ui");
            this.p.a("/data/ui/petstate.ui");
            if (this.o instanceof k) {
                this.o.a((byte)8);
                return;
            }
            if (game.d.a().h.equals(((d)this.o).c(this.c))) {
                this.g(((d)this.o).d(this.c));
            }
            if (((d)this.o).h.J) {
                ((d)this.o).i = (byte)(((d)this.o).i + 1);
                ((d)this.o).a((byte)1);
                return;
            }
            ((d)this.o).a((byte)4);
        }
    }

    public final void al() {
        if (this.f == 0 && this.o.k(4100)) {
            this.p.a.b(0);
            return;
        }
        if (this.f == 0 && this.o.k(8448)) {
            this.p.a.b(1);
            return;
        }
        if (this.o.k(196640)) {
            this.bo();
            return;
        }
        if (this.f == 0 && this.o.k(262144)) {
            if (this.l) {
                if (game.d.a().h.equals(((d)this.o).c(this.c))) {
                    this.g(((d)this.o).d(this.c));
                }
                if (((d)this.o).h.J) {
                    ((d)this.o).i = (byte)(((d)this.o).i + 1);
                    ((d)this.o).a((byte)1);
                } else {
                    ((d)this.o).a((byte)4);
                }
                this.p.a("/data/ui/petstate.ui");
                return;
            }
            ((d)this.o).a((byte)4);
            this.p.a("/data/ui/petstate.ui");
        }
    }

    public final void a(int i1, int i2) {
        b v3;
        if (this.i >= game.d.j.size()) {
            this.i = 0;
            game.i.a().a((byte)10);
            return;
        }
        block0: while (true) {
            v3 = (b)game.d.j.elementAt(this.i);
            while (this.i < game.d.j.size() && v3.t()) {
                ++this.i;
                if (this.i >= game.d.j.size()) continue;
                continue block0;
            }
            break;
        }
        this.H = i1;
        this.I = i2;
        v3.c();
        v3.b(i1, i2);
        this.x = 0;
    }

    public final void am() {
        block20: {
            block19: {
                int i4;
                int i2;
                b v1;
                block18: {
                    if (this.i >= game.d.j.size()) {
                        this.i = 0;
                        game.i.a().a((byte)10);
                        return;
                    }
                    if (this.x <= 0) {
                        this.J += 8;
                    }
                    v1 = (b)game.d.j.elementAt(this.i);
                    i2 = v1.A() + this.J;
                    int i3 = v1.u();
                    i4 = v1.z();
                    if (i2 >= i3) {
                        i2 = i3;
                    } else if (i2 >= i4) {
                        i2 = i4;
                    }
                    if (this.o.k(196640)) {
                        if (i4 >= i3) {
                            i2 = i3;
                            this.p.a.a((int)40).h().a = i2 + "/" + i2;
                            this.p.a.a((int)9).h().a = "#P" + v1.v(i2);
                            v1.j(0);
                            this.x = 0;
                            ((d)this.o).a((byte)22);
                            return;
                        }
                        if (i2 >= i4) {
                            this.p.a.a((int)40).h().a = i4 + "/" + v1.u();
                            this.p.a.a((int)9).h().a = "#P" + v1.v(i4);
                            v1.j(i2);
                            ++this.i;
                            while (this.i < game.d.j.size() && ((b)game.d.j.elementAt(this.i)).t()) {
                                ++this.i;
                            }
                            if (this.i >= game.d.j.size()) {
                                this.i = 0;
                                game.i.a().a((byte)10);
                            } else {
                                ((b)game.d.j.elementAt(this.i)).b(this.H, this.I);
                            }
                            this.x = 0;
                            this.J = 0;
                            return;
                        }
                        this.J = 0;
                        i2 = i4;
                        v1.j(i2);
                        this.p.a.a((int)40).h().a = i2 + "/" + v1.u();
                        this.p.a.a((int)9).h().a = "#P" + v1.v(i2);
                        return;
                    }
                    this.p.a.a((int)40).h().a = i2 + "/" + v1.u();
                    this.p.a.a((int)9).h().a = "#P" + v1.v(i2);
                    b v6 = v1;
                    h v5 = this;
                    this.p.a.a((int)12).h().a = an.f(v6.j((byte)0));
                    v5.p.a.a((int)13).h().a = "lv" + v6.s();
                    v5.p.a.a((int)17).h().m.a(94 + v6.j((byte)1));
                    if (i2 < i3) break block18;
                    v1.j(0);
                    ((d)this.o).a((byte)22);
                    break block19;
                }
                if (i2 < i4) break block20;
                ++this.x;
                v1.j(i2);
                if (this.x >= 10) {
                    ++this.i;
                    while (this.i < game.d.j.size() && ((b)game.d.j.elementAt(this.i)).t()) {
                        ++this.i;
                    }
                    if (this.i >= game.d.j.size()) {
                        this.i = 0;
                        game.i.a().a((byte)10);
                    } else {
                        ((b)game.d.j.elementAt(this.i)).b(this.H, this.I);
                    }
                    this.x = 0;
                }
            }
            this.J = 0;
        }
    }

    public final void an() {
        byte i4;
        int i3;
        b v1 = (b)game.d.j.elementAt(this.i);
        String[] v2 = new String[4];
        for (i3 = 0; i3 < 4; ++i3) {
            i4 = (byte)(i3 + 1);
            v2[i3] = "" + v1.c[i4];
        }
        v1.v();
        this.g(v1);
        this.p.a("/data/ui/levelUp.ui", 257, this);
        for (i3 = 0; i3 < 4; ++i3) {
            this.p.a.a((int)(i3 + 19)).h().a = v2[i3];
        }
        if (v1.E() < 5 && v1.E() < v1.s() / 10 + 1) {
            this.y = v1.F();
            this.p.a.a((int)51).h().a = "C\u00f3 th\u1ec3 h\u1ecdc t\u1eadp k\u1ef9 n\u0103ng m\u1edbi";
        } else {
            this.p.a.a((int)51).h().a = "";
        }
        this.p.a.a((int)38).h().a = an.f(aq.c[0][v1.q()][0]);
        this.p.a.a((int)40).h().a = "" + v1.s();
        if (this.p.a.a((int)10).h().m == null) {
            this.p.a.a((int)10).h().m = new m();
            this.p.a.a((int)10).h().m.a = (byte)3;
            this.p.a.a((int)10).h().m.a(0);
            this.p.a.a((int)10).h().m.a(v1.C, false, (byte)-1);
        }
        for (i3 = 0; i3 < 4; ++i3) {
            i4 = (byte)(i3 + 1);
            this.p.a.a((int)(i3 + 31)).h().a = "" + v1.c[i4];
        }
    }

    public final void ao() {
        ++this.K;
        if (this.K > 40) {
            this.K = 0;
            if (this.y != null) {
                ((d)this.o).a((byte)23);
            } else if (this.i + 1 >= game.d.j.size()) {
                if (((b)game.d.j.elementAt(this.i)).z() > 0) {
                    this.o.a((byte)8);
                } else {
                    this.i = 0;
                    game.i.a().a((byte)10);
                }
                this.p.a("/data/ui/levelUp.ui");
            } else {
                this.o.a((byte)8);
                this.p.a("/data/ui/levelUp.ui");
            }
        }
        if (this.o.k(196640)) {
            this.K = 0;
            if (this.y != null) {
                this.o.a((byte)23);
                return;
            }
            if (this.i + 1 >= game.d.j.size()) {
                if (((b)game.d.j.elementAt(this.i)).z() > 0) {
                    this.o.a((byte)8);
                } else {
                    this.i = 0;
                    game.i.a().a((byte)10);
                }
                this.p.a("/data/ui/levelUp.ui");
                return;
            }
            this.o.a((byte)8);
            this.p.a("/data/ui/levelUp.ui");
        }
    }

    public final void ap() {
        this.p.a("/data/ui/choiceskill.ui", 257, this);
        this.p.a("/data/ui/levelUp.ui");
        this.b = 0;
        this.f = 0;
        ((al)this.p.a.a((int)0)).a.a = this.y.length;
        if (this.y.length > 5) {
            ((al)this.p.a.a((int)0)).a.a(1);
        } else {
            ((al)this.p.a.a((int)0)).a.a(-1);
        }
        if (this.p.a.a((int)5).h().m == null) {
            this.p.a.a((int)5).h().m = new m();
            this.p.a.a((int)5).h().m.a = (byte)3;
            this.p.a.a((int)5).h().m.a(0);
            this.p.a.a((int)5).h().m.a(257, false, (byte)-1);
        }
        this.p.a.a((int)5).h().m.a((byte)11, (byte)-1);
        this.p.a.a(6).a(false);
        this.bp();
        if (!game.k.D) {
            this.b("C\u00f3 th\u1ec3 nh\u1ea5n #1n\u00fat m\u1ec1m tr\u00e1i#0 \u0111\u1ec3 h\u1ecdc t\u1eadp k\u1ef9 n\u0103ng");
            game.k.D = true;
        }
    }

    private void bp() {
        this.w = ((al)this.p.a.a((int)0)).a.e;
        this.h = ((al)this.p.a.a((int)0)).a.f;
        for (int i1 = 0; i1 < 5; ++i1) {
            if (i1 >= this.y.length) {
                this.p.a.a((int)(13 + i1 * 5)).h().a = "";
                this.p.a.a((int)(14 + i1 * 5)).h().a = "";
                continue;
            }
            this.p.a.a((int)(13 + i1 * 5)).h().a = an.f(aq.c[1][this.y[this.w + i1]][1]);
            this.p.a.a((int)(14 + i1 * 5)).h().a = "" + aq.c[1][this.y[this.w + i1]][5];
        }
        this.h(this.y[this.h]);
        this.p.a.a(51).b(98 + this.h * 62 / this.y.length, this.p.a.a());
    }

    public final void aq() {
        if (!this.j() && this.o.k(4100) && this.f == 0) {
            this.p.a.b(0);
            this.bp();
        } else if (!this.j() && this.o.k(8448) && this.f == 0) {
            this.p.a.b(1);
            this.bp();
        } else if (!this.j() && this.f == 0 && (this.o.k(131072) || this.o.Q()) || this.f == 1 && this.o.k(196640)) {
            if (this.f == 0) {
                this.f = 1;
                this.p.a("/data/ui/msgwarm.ui", 257, this);
                this.a("H\u1ecdc t\u1eadp" + an.f(aq.c[1][this.y[this.b]][1]), "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
            } else if (this.f == 1) {
                b v1 = (b)game.d.j.elementAt(this.i);
                v1.g((byte)this.y[this.h]);
                this.y = null;
                if (this.i + 1 >= game.d.j.size() && v1.z() <= 0) {
                    this.i = 0;
                    game.i.a().a((byte)10);
                } else {
                    this.o.a((byte)8);
                }
                this.p.a("/data/ui/msgwarm.ui");
                this.p.a("/data/ui/choiceskill.ui");
            }
        }
        this.f();
    }

    public final void ar() {
        this.f = 0;
        this.b("Ba l\u00f4 s\u1ee7ng v\u1eadt \u0111\u1ec1u th\u0103ng 5 c\u1ea5p");
    }

    /*
     * Enabled aggressive block sorting
     */
    public final void as() {
        block14: {
            h v1;
            block17: {
                block22: {
                    block21: {
                        block19: {
                            block20: {
                                block18: {
                                    block16: {
                                        block15: {
                                            if (this.f != 0) break block15;
                                            if (this.ax()) {
                                                this.f = 1;
                                                if (game.k.E.size() <= 0) {
                                                    this.o.a((byte)14);
                                                }
                                            }
                                            break block14;
                                        }
                                        if (this.f != 1) break block16;
                                        this.p.a("/data/ui/bodyShop.ui");
                                        this.bq();
                                        this.g = true;
                                        break block14;
                                    }
                                    v1 = this;
                                    if (v1.f < 3) break block17;
                                    if (v1.f != 5) break block18;
                                    v1.f = 6;
                                    v1.H();
                                    v1.a("\u0110ang l\u01b0u...");
                                    v1.J();
                                    break block19;
                                }
                                if (v1.f != 6) break block20;
                                game.k.G = (byte)2;
                                game.k.h();
                                if (((k)v1.o).j()) {
                                    v1.a("L\u01b0u th\u00e0nh c\u00f4ng");
                                    v1.f = 7;
                                }
                                break block19;
                            }
                            if (v1.f != 7) break block19;
                            v1.p.a("/data/ui/msgtip.ui");
                            v1.f = 0;
                            if (v1.o.Q == 14) {
                                v1.o.a((byte)14);
                                break block14;
                            } else {
                                v1.o.a((byte)0);
                            }
                            break block14;
                        }
                        if (v1.j() || !v1.o.k(4100) || v1.f != 3) break block21;
                        v1.p.a.b(0);
                        v1.bp();
                        break block14;
                    }
                    if (v1.j() || !v1.o.k(8448) || v1.f != 3) break block22;
                    v1.p.a.b(1);
                    v1.bp();
                    break block14;
                }
                if ((v1.j() || v1.f != 3 || !v1.o.k(131072) && !v1.o.Q()) && (v1.f != 4 || !v1.o.k(196640))) break block14;
                if (v1.f == 3) {
                    v1.f = 4;
                    v1.p.a("/data/ui/msgwarm.ui", 257, v1);
                    v1.a("H\u1ecdc t\u1eadp" + an.f(aq.c[1][v1.y[v1.h]][1]), "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    break block14;
                } else if (v1.f == 4) {
                    ((b)game.k.E.elementAt(v1.i)).g((byte)v1.y[v1.h]);
                    v1.y = null;
                    ++v1.i;
                    if (v1.i >= game.k.E.size()) {
                        v1.i = 0;
                        v1.f = 5;
                    } else {
                        v1.bq();
                    }
                    v1.p.a("/data/ui/msgwarm.ui");
                    v1.p.a("/data/ui/choiceskill.ui");
                }
                break block14;
            }
            if (v1.o.k(196640)) {
                h v2 = v1;
                v2.p.a("/data/ui/choiceskill.ui", 257, v2);
                v2.p.a("/data/ui/levelUp.ui");
                v2.b = 0;
                v2.f = 3;
                ((al)v2.p.a.a((int)0)).a.a = v2.y.length;
                if (v2.y.length > 5) {
                    ((al)v2.p.a.a((int)0)).a.a(1);
                } else {
                    ((al)v2.p.a.a((int)0)).a.a(-1);
                }
                if (v2.p.a.a((int)5).h().m == null) {
                    v2.p.a.a((int)5).h().m = new m();
                    v2.p.a.a((int)5).h().m.a = (byte)3;
                    v2.p.a.a((int)5).h().m.a(0);
                    v2.p.a.a((int)5).h().m.a(257, false, (byte)-1);
                }
                v2.p.a.a((int)5).h().m.a((byte)11, (byte)-1);
                v2.p.a.a(6).a(false);
                v2.bp();
                v1.g = true;
            }
        }
        this.f();
    }

    private void bq() {
        int i2;
        this.f = 2;
        b v1 = (b)game.k.E.elementAt(this.i);
        this.p.a("/data/ui/levelUp.ui", 257, this);
        for (i2 = 0; i2 < 4; ++i2) {
            this.p.a.a((int)(i2 + 19)).h().a = "" + v1.i((int)((byte)(i2 + 1 - 1)));
        }
        if (v1.E() < 5 && v1.E() < v1.s() / 10 + 1) {
            this.y = v1.F();
            this.p.a.a((int)51).h().a = "Nh\u1ea5n n\u00fat 5 h\u1ecdc t\u1eadp k\u1ef9 n\u0103ng m\u1edbi";
        } else {
            this.p.a.a((int)51).h().a = "";
        }
        this.p.a.a((int)38).h().a = an.f(aq.c[0][v1.q()][0]);
        this.p.a.a((int)40).h().a = "" + v1.s();
        if (this.p.a.a((int)10).h().m == null) {
            this.p.a.a((int)10).h().m = new m();
            this.p.a.a((int)10).h().m.a = (byte)3;
            this.p.a.a((int)10).h().m.a(0);
            this.p.a.a((int)10).h().m.a(v1.C, false, (byte)-1);
        }
        for (i2 = 0; i2 < 4; ++i2) {
            byte i3 = (byte)(i2 + 1);
            this.p.a.a((int)(i2 + 31)).h().a = "" + v1.c[i3];
        }
    }

    public final void at() {
        this.p.a("/data/ui/npcEnemy.ui", 296, this);
        if (this.p.a.a((int)1).h().m == null) {
            this.p.a.a((int)1).h().m = new m();
            this.p.a.a((int)1).h().m.a = (byte)2;
            this.p.a.a((int)1).h().m.a(296, false, (byte)0);
            this.p.a.a((int)1).h().m.a(0);
        }
        this.p.a.a(36).a(false);
    }

    private void a(int i1, int i2, int i3) {
        if (i3 != -1 && this.p.a.a((int)i3).h().m != null) {
            this.p.a.a(i3).a(false);
        }
        if (this.p.a.a((int)i1).h().m == null) {
            this.p.a.a((int)i1).h().m = new m();
            this.p.a.a((int)i1).h().m.a = (byte)2;
            this.p.a.a((int)i1).h().m.a(296, false, (byte)0);
            this.p.a.a((int)i1).h().m.a(0);
        }
        this.p.a.a((int)i1).h().m.a(i2);
    }

    public final void b(int i1, int i2) {
        switch (i1) {
            case 0: {
                this.p.a.a((int)1).h().m.a(i2);
                return;
            }
            case 1: {
                for (i1 = 2; i1 < 4; ++i1) {
                    if (this.p.a.a((int)i1).h().m == null) {
                        this.p.a.a((int)i1).h().m = new m();
                        this.p.a.a((int)i1).h().m.a = (byte)2;
                        this.p.a.a((int)i1).h().m.a(0);
                    }
                    if (i1 % 2 == 1) {
                        this.p.a.a((int)i1).h().m.a(0, false, (byte)-1);
                    } else if (game.k.u == -1) {
                        if (game.k.v == -1) {
                            this.p.a.a((int)i1).h().m.a(game.k.a().d[8].a.a, false, (byte)-1);
                        } else {
                            this.p.a.a((int)i1).h().m.a(game.k.a().d[game.k.v].a.a, false, (byte)-1);
                        }
                    } else {
                        this.p.a.a((int)i1).h().m.a(game.k.a().d[game.k.u].a.a, false, (byte)-1);
                    }
                    this.p.a.a((int)i1).h().m.a(1);
                }
                this.p.a.a((int)1).h().m.a(i2);
                return;
            }
            case 2: {
                for (i1 = 2; i1 < 4; ++i1) {
                    if (this.p.a.a((int)i1).h().m != null) {
                        this.p.a.a(i1).a(false);
                    }
                    if (this.p.a.a((int)(i1 + 32)).h().m == null) {
                        this.p.a.a((int)(i1 + 32)).h().m = new m();
                        this.p.a.a((int)(i1 + 32)).h().m.a = (byte)2;
                        this.p.a.a((int)(i1 + 32)).h().m.a(0);
                    }
                    if (i1 % 2 == 1) {
                        this.p.a.a((int)(i1 + 32)).h().m.a(0, false, (byte)-1);
                    } else if (game.k.u == -1) {
                        if (game.k.v == -1) {
                            this.p.a.a((int)(i1 + 32)).h().m.a(game.k.a().d[8].a.a, false, (byte)-1);
                        } else {
                            this.p.a.a((int)(i1 + 32)).h().m.a(game.k.a().d[game.k.v].a.a, false, (byte)-1);
                        }
                    } else {
                        this.p.a.a((int)(i1 + 32)).h().m.a(game.k.a().d[game.k.u].a.a, false, (byte)-1);
                    }
                    this.p.a.a((int)(i1 + 32)).h().m.a(1);
                }
                this.p.a.a((int)1).h().m.a(i2);
                return;
            }
            case 3: {
                for (i1 = 2; i1 < 4; ++i1) {
                    if (this.p.a.a((int)(i1 + 32)).h().m != null) {
                        this.p.a.a(i1 + 32).a(false);
                    }
                    if (this.p.a.a((int)(i1 + 2)).h().m == null) {
                        this.p.a.a((int)(i1 + 2)).h().m = new m();
                        this.p.a.a((int)(i1 + 2)).h().m.a = (byte)2;
                        this.p.a.a((int)(i1 + 2)).h().m.a(0);
                    }
                    if (i1 % 2 == 1) {
                        this.p.a.a((int)(i1 + 2)).h().m.a(0, false, (byte)-1);
                    } else if (game.k.u == -1) {
                        if (game.k.v == -1) {
                            this.p.a.a((int)(i1 + 2)).h().m.a(game.k.a().d[8].a.a, false, (byte)-1);
                        } else {
                            this.p.a.a((int)(i1 + 2)).h().m.a(game.k.a().d[game.k.v].a.a, false, (byte)-1);
                        }
                    } else {
                        this.p.a.a((int)(i1 + 2)).h().m.a(game.k.a().d[game.k.u].a.a, false, (byte)-1);
                    }
                    this.p.a.a((int)(i1 + 2)).h().m.a(1);
                }
                this.p.a.a((int)1).h().m.a(i2);
                this.L = game.d.a().j();
                this.M = this.q.A;
                if (i2 - 3 < this.L) {
                    this.a(6, 6, -1);
                }
                if (i2 - 3 >= this.M) break;
                this.a(18, 6, -1);
                return;
            }
            case 4: {
                if (i2 - 3 < this.L) {
                    this.a(6 + (i2 - 3 << 1), 6, 6 + (i2 - 4 << 1));
                } else {
                    this.a(6 + (i2 - 3 << 1), 5, 6 + (i2 - 4 << 1));
                }
                if (i2 - 4 < this.L) {
                    this.a(7 + (i2 - 4 << 1), 6, 6 + (i2 - 4 << 1));
                } else {
                    this.a(7 + (i2 - 4 << 1), 5, 6 + (i2 - 4 << 1));
                }
                if (i2 - 4 < this.M) {
                    this.a(19 + (i2 - 4 << 1), 6, 18 + (i2 - 4 << 1));
                } else {
                    this.a(19 + (i2 - 4 << 1), 5, 18 + (i2 - 4 << 1));
                }
                if (i2 - 3 < this.M) {
                    this.a(18 + (i2 - 3 << 1), 6, 18 + (i2 - 4 << 1));
                    return;
                }
                this.a(18 + (i2 - 3 << 1), 5, 18 + (i2 - 4 << 1));
                return;
            }
            case 5: {
                if (i2 - 4 < this.L) {
                    this.a(7 + (i2 - 4 << 1), 6, 6 + (i2 - 4 << 1));
                } else {
                    this.a(7 + (i2 - 4 << 1), 5, 6 + (i2 - 4 << 1));
                }
                if (i2 - 4 < this.M) {
                    this.a(19 + (i2 - 4 << 1), 6, 18 + (i2 - 4 << 1));
                    return;
                }
                this.a(19 + (i2 - 4 << 1), 5, 18 + (i2 - 4 << 1));
                return;
            }
            case 6: {
                this.a(30, 8, -1);
                this.a(31, 7, -1);
                return;
            }
            case 7: {
                this.a(32, 8, 30);
                this.a(33, 7, 31);
                return;
            }
            case 8: {
                this.p.a.a(36).a(true);
                return;
            }
            case 9: {
                this.p.a.a(36).a(false);
                return;
            }
            case 10: {
                this.a(1, 4, 32);
                this.a(1, 4, 33);
                for (i1 = 4; i1 < 6; ++i1) {
                    this.p.a.a(i1).a(false);
                }
                for (i1 = 7; i1 < 19; i1 += 2) {
                    this.p.a.a(i1).a(172 + 17 * (i1 - 7) / 2, this.p.a.a());
                    this.p.a.a(i1 + 12).a(-30 + 17 * (i1 - 7) / 2, this.p.a.a());
                }
                return;
            }
            case 11: {
                for (i1 = 4; i1 < 6; ++i1) {
                    this.p.a.a(i1).a(false);
                }
                for (i1 = 7; i1 < 19; i1 += 2) {
                    this.p.a.a(i1).a(false);
                    this.p.a.a(i1 + 12).a(false);
                }
                this.a(1, 0, -1);
            }
        }
    }

    private void e(String v1) {
        this.t = v1;
        if (this.p.a.a((int)1).h().m == null) {
            this.p.a.a((int)1).h().m = new m();
            this.p.a.a((int)1).h().m.a(0);
            this.p.a.a((int)1).h().m.a = (byte)3;
            this.p.a.a((int)1).h().m.a(257, false, (byte)-2);
        }
        this.p.a.a((int)1).h().m.a((byte)9, (byte)-2);
        this.v = 0;
    }

    public final void au() {
        this.p.a("/data/ui/openbox.ui", 257, this);
        this.e("Kh\u00f4ng c\u00f3 c\u00e1i ch\u00eca kh\u00f3a, c\u00f3 th\u1ec3 \u0111\u1ebfn t\u00e0i li\u1ec7u c\u1eeda h\u00e0ng mua s\u1eafm");
    }

    public final void av() {
        this.p.a("/data/ui/openbox.ui", 257, this);
        this.e("\u0110\u1ea1o c\u1ee5 \u0111\u00e3 \u0111\u1ee7");
    }

    public final void a(String v1, int i2) {
        this.p.a("/data/ui/openbox.ui", 257, this);
        this.e(v1 + " x " + i2);
    }

    public final void b(String v1) {
        this.p.a("/data/ui/openbox.ui", 257, this);
        this.e(v1);
    }

    public final void aw() {
        if (this.p.b("/data/ui/openbox.ui")) {
            this.p.a("/data/ui/openbox.ui");
        }
    }

    public final boolean ax() {
        return !this.p.b("/data/ui/openbox.ui");
    }

    public final void c(String v1) {
        this.p.a("/data/ui/taskTip.ui", 257, this);
        String v2 = v1;
        v1 = this;
        this.t = v2;
        if (((h)v1).p.a.a((int)1).h().m == null) {
            ((h)v1).p.a.a((int)1).h().m = new m();
            ((h)v1).p.a.a((int)1).h().m.a(0);
            ((h)v1).p.a.a((int)1).h().m.a = (byte)3;
            ((h)v1).p.a.a((int)1).h().m.a(257, false, (byte)-2);
        }
        ((h)v1).p.a.a((int)1).h().m.a((byte)10, (byte)-2);
        ((h)v1).v = 0;
    }

    private void br() {
        if (this.p.b("/data/ui/taskTip.ui")) {
            this.p.a("/data/ui/taskTip.ui");
        }
    }

    public final boolean ay() {
        return !this.p.b("/data/ui/taskTip.ui");
    }

    public final void az() {
        this.c = 0;
        this.f = 0;
        this.p.a("/data/ui/bodyShop.ui", 257, this);
        this.bs();
    }

    private void bs() {
        Object v1 = "";
        switch (this.c) {
            case 0: {
                v1 = "T\u00f9y th\u1eddi mua s\u1eafm c\u00e1c lo\u1ea1i \u0111\u1ea1o c\u1ee5, gi\u00e0 tr\u1ebb kh\u00f4ng g\u1ea1t.";
                break;
            }
            case 1: {
                v1 = new int[]{2, 1, 2};
                v1 = an.f(602) + an.a(604, (int[])v1);
                break;
            }
            case 2: {
                v1 = new int[]{2, 1, 2};
                v1 = an.f(603) + an.a(604, (int[])v1);
                break;
            }
            case 3: {
                v1 = new int[]{2, 1, 2};
                v1 = an.f(601) + an.a(604, (int[])v1);
            }
        }
        this.p.a.a((int)11).h().a = (String)v1;
        if (this.c > 0) {
            this.o.d((byte)0);
            this.bt();
        }
    }

    private void bt() {
        switch (this.c) {
            case 1: {
                this.o.c((byte)3);
                return;
            }
            case 2: {
                this.o.c((byte)4);
                return;
            }
            case 3: {
                this.o.c((byte)2);
            }
        }
    }

    public final void aA() {
        block0 : switch (this.c) {
            case 0: {
                if (this.o.k(4100) && this.f == 0) {
                    this.p.a.b(0);
                    this.bs();
                    return;
                }
                if (this.o.k(8448) && this.f == 0) {
                    this.p.a.b(1);
                    this.bs();
                    return;
                }
                if (this.o.k(131072)) {
                    this.o.a((byte)26);
                    this.p.a("/data/ui/bodyShop.ui");
                    return;
                }
                if (!this.o.k(786432)) break;
                this.b = 0;
                this.o.a((byte)6);
                this.p.a("/data/ui/bodyShop.ui");
                return;
            }
            default: {
                switch (this.o.N()) {
                    case 0: {
                        if (this.o.k(4100) && this.f == 0) {
                            this.p.a.b(0);
                            this.bs();
                            return;
                        }
                        if (this.o.k(8448) && this.f == 0) {
                            this.p.a.b(1);
                            this.bs();
                            return;
                        }
                        if (this.f == 0 && this.o.k(131072) || this.f == 1 && this.o.k(65568)) {
                            if (this.f == 0) {
                                this.bt();
                                if (this.o.L() == 3) {
                                    int i1;
                                    if (game.k.H != null) {
                                        game.k.H.removeAllElements();
                                    }
                                    for (i1 = 0; i1 < game.g.o().A && game.g.o().z[i1].s() >= 50; ++i1) {
                                    }
                                    if (i1 >= game.g.o().A) {
                                        this.f = 1;
                                        this.p.a("/data/ui/msgwarm.ui", 257, this);
                                        this.a("Trong ba l\u00f4 s\u1ee7ng v\u1eadt \u0111\u1ec1u \u0111\u00e3 max level", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                                        return;
                                    }
                                }
                                if (this.o.O() > 1) {
                                    this.o.d((byte)1);
                                    return;
                                }
                                this.o.g(1);
                                return;
                            }
                            this.f = 0;
                            this.p.a("/data/ui/msgwarm.ui");
                            return;
                        }
                        if (!this.o.k(786432) || this.f != 0) break block0;
                        this.b = 0;
                        this.o.a((byte)6);
                        this.p.a("/data/ui/bodyShop.ui");
                        return;
                    }
                    case 1: {
                        if (this.o.k(131072)) {
                            this.o.h(1);
                            return;
                        }
                        if (!this.o.k(262144)) break block0;
                        this.o.h(2);
                        return;
                    }
                    case 2: {
                        if (!this.bu() || !this.o.k(917504)) break block0;
                        if (this.o.M()) {
                            if (this.o.L() == 3) {
                                this.o.a((byte)25);
                            }
                            this.o.d((byte)5);
                        } else {
                            this.o.d((byte)1);
                        }
                        this.f = 0;
                        return;
                    }
                    case 3: {
                        if (!this.o.k(393216)) break block0;
                        this.o.h(1);
                    }
                }
            }
        }
    }

    private boolean bu() {
        if (this.f == 0) {
            this.f = 1;
            this.H();
            this.a("\u0110ang l\u01b0u...");
            this.J();
        } else if (this.f == 1) {
            if (this.o.L() == 3) {
                if (game.k.a().k()) {
                    this.a("L\u01b0u th\u00e0nh c\u00f4ng");
                    this.f = 2;
                }
            } else if (game.k.a().n()) {
                this.a("L\u01b0u th\u00e0nh c\u00f4ng");
                this.f = 2;
            }
        } else if (this.f == 2) {
            this.p.a("/data/ui/msgtip.ui");
            this.f = 3;
        } else if (this.f == 3) {
            return true;
        }
        return false;
    }

    public final void aB() {
        this.p.a("/data/ui/dialog.ui", 257, this);
        this.p.a.a(12).a(false);
        this.p.a.a(13).a(false);
    }

    public final void a(String v1, String v2, int i3) {
        this.p.a("/data/ui/dialog.ui", 257, this);
        ae.a(v2, an.G(), this.p.a.a(14).d(), an.D(), this.o.R.b);
        ae.c(this.p.a.a(14).e());
        this.p.a.a((int)14).h().a = ae.d(1);
        game.k.t = (byte)i3;
        game.k.s = (byte)-1;
        this.p.a.a(8).a(false);
        this.p.a.a(11).a(false);
        this.p.a.a(12).a(true);
        this.p.a.a(13).a(true);
        if (i3 == -1) {
            this.p.a.a(12).a(false);
            this.p.a.a(13).a(false);
        }
        switch (i3) {
            case 0: {
                this.p.a.a(13).a(false);
                this.p.a.a((int)12).h().a = v1;
                return;
            }
            case 1: {
                this.p.a.a(12).a(false);
                this.p.a.a((int)13).h().a = v1;
            }
        }
    }

    public final void b(int i1) {
        this.p.a.a((int)14).h().a = ae.d(i1);
    }

    public final void aC() {
        this.p.a("/data/ui/dialog.ui");
    }

    public final boolean c(int i1, int i2) {
        if (i2 == -1) {
            return true;
        }
        switch (i1) {
            case 0: {
                if (!this.p.a.a((int)11).h().m.a().e()) break;
                return true;
            }
            case 1: {
                if (!this.p.a.a((int)8).h().m.a().e()) break;
                return true;
            }
        }
        this.g = true;
        return false;
    }

    public final void a(int i1, int i2, String[] v3, String v4) {
        this.b = 0;
        this.p.a(this.N[i1], 257, this);
        ((al)this.p.a.a((int)0)).a.a = i2;
        switch (i1) {
            case 0: {
                for (i1 = 0; i1 < v3.length; ++i1) {
                    this.p.a.a((int)(i1 + 12)).h().a = v3[i1];
                }
                return;
            }
            case 1: {
                this.p.a.a((int)5).h().a = v4;
                for (i1 = 0; i1 < v3.length; ++i1) {
                    this.p.a.a((int)(9 + (i1 << 2))).h().a = v3[i1];
                }
                return;
            }
            case 2: {
                this.p.a.a(10).a(false);
                this.p.a.a((int)8).h().a = "Tr\u00f2 ch\u01a1i";
                this.p.a.a((int)9).h().a = "X\u00e1c nh\u1eadn";
                for (i1 = 0; i1 < v3.length; ++i1) {
                    this.p.a.a((int)(i1 + 5)).h().a = v3[i1];
                }
                break;
            }
        }
    }

    public final int c(int i1) {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            this.b = this.z[0];
        } else if (this.o.k(8448)) {
            this.p.a.b(1);
            this.b = this.z[0];
        } else if (this.o.k(196640)) {
            int i2 = i1;
            h v1 = this;
            v1.p.a(v1.N[i2]);
            return this.b;
        }
        return -1;
    }

    public final void a(int[] v1, int[] v2, String[] v3, String[] v4) {
        int i5;
        this.b = 0;
        this.p.a("/data/ui/taskOption.ui", 257, this);
        for (i5 = 0; i5 < v4.length; ++i5) {
            this.p.a.a((int)(i5 + 17)).h().a = v4[i5];
        }
        block10: for (i5 = 0; i5 < v1.length; ++i5) {
            if (this.p.a.a((int)((i5 << 1) + 13)).h().m == null) {
                this.p.a.a((int)((i5 << 1) + 13)).h().m = new m();
                this.p.a.a((int)((i5 << 1) + 13)).h().m.a = (byte)2;
                if (v1[i5] < 3 || v1[i5] >= 5) {
                    this.p.a.a((int)((i5 << 1) + 13)).h().m.a(0);
                    this.p.a.a((int)((i5 << 1) + 13)).h().m.a(258, false, (byte)0);
                } else {
                    this.p.a.a((int)((i5 << 1) + 13)).h().m.a(-1);
                    this.p.a.a((int)((i5 << 1) + 13)).h().m.a(257, false, (byte)0);
                }
            }
            switch (v1[i5]) {
                case 0: {
                    this.p.a.a((int)((i5 << 1) + 13)).h().m.a(aq.c[4][v2[i5]][1]);
                    this.p.a.a((int)((i5 << 1) + 14)).h().a = v3[i5];
                    continue block10;
                }
                case 1: {
                    this.p.a.a((int)((i5 << 1) + 13)).h().m.a(aq.c[3][v2[i5]][1]);
                    this.p.a.a((int)((i5 << 1) + 14)).h().a = v3[i5];
                    continue block10;
                }
                case 2: {
                    this.p.a.a((int)((i5 << 1) + 13)).h().m.a(aq.c[5][v2[i5]][1]);
                    this.p.a.a((int)((i5 << 1) + 14)).h().a = v3[i5];
                    continue block10;
                }
                case 3: {
                    this.p.a.a((int)((i5 << 1) + 13)).h().m.a(84);
                    this.p.a.a((int)((i5 << 1) + 14)).h().a = v3[i5];
                    continue block10;
                }
                case 4: {
                    this.p.a.a((int)((i5 << 1) + 13)).h().m.a(83);
                    this.p.a.a((int)((i5 << 1) + 14)).h().a = v3[i5];
                    continue block10;
                }
                case 5: {
                    continue block10;
                }
                case 6: {
                    this.p.a.a((int)21).h().a = "#2" + an.f(aq.a((byte)0, (short)v2[i5], (byte)0)) + " #0" + v3[i5];
                }
            }
        }
    }

    public final int aD() {
        if (this.o.k(4100)) {
            this.p.a.b(0);
            this.b = this.z[0];
        } else if (this.o.k(8448)) {
            this.p.a.b(1);
            this.b = this.z[0];
        } else {
            if (this.o.k(196640)) {
                this.p.a("/data/ui/taskOption.ui");
                return this.b;
            }
            if (this.o.k(262144)) {
                this.p.a("/data/ui/taskOption.ui");
                return 1;
            }
        }
        return -1;
    }

    public final void aE() {
        this.bw();
        this.c("C\u00f3 d\u00f9ng 10000 kim ti\u1ec1n \u0111\u1ec3 kh\u00f4i ph\u1ee5c tr\u1ea1ng th\u00e1i c\u1ee7a t\u1ea5t c\u1ea3 s\u1ee7ng v\u1eadt trong ba l\u00f4 kh\u00f4ng?", "T\u1ea1i ch\u1ed7 s\u1ed1ng l\u1ea1i");
    }

    public final void aF() {
        if (this.o.k(196640)) {
            if (this.f == 0) {
                if (this.q.t(10000)) {
                    this.q.s(-10000);
                    for (int i1 = 0; i1 < this.q.A; ++i1) {
                        this.q.z[i1].I();
                        this.q.z[i1].u(this.q.z[i1].d[1]);
                    }
                    game.d.a().c();
                    this.o.a((byte)0);
                    this.bx();
                    return;
                }
                this.E();
                this.a("Kim ti\u1ec1n ch\u01b0a \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                this.f = 1;
                return;
            }
            for (int i1 = 0; i1 < this.q.A; ++i1) {
                this.q.z[i1].l(1);
                this.q.z[i1].u(1);
                this.q.z[i1].c();
            }
            this.o.a((byte)102);
            this.F();
            return;
        }
        if (this.f == 0 && this.o.k(786432)) {
            this.bv();
            this.bx();
        }
    }

    private void bv() {
        int i1 = -1;
        if (game.k.a().f == 9 && game.k.a().g != 0) {
            i1 = (byte)game.k.a().g;
        }
        if (i1 == -1) {
            game.k.a();
            if (game.k.i()) {
                game.k.a().f();
                this.q.y = false;
                game.i.a().a((byte)9);
                return;
            }
            game.i.a().a((byte)7);
            return;
        }
        if (game.k.a().g == 0) {
            short[] v2 = new short[]{15, 194, 433, 16, 142, 357, 17, 97, 268, 18, 183, 224};
            for (int i3 = 0; i3 < game.k.a().d.length; ++i3) {
                for (int i4 = 0; i4 < v2.length / 3; ++i4) {
                    if (game.k.a().d[i3].I != v2[i4 * 3]) continue;
                    game.k.a().d[i3].b(v2[i4 * 3 + 1], v2[i4 * 3 + 2]);
                }
            }
        }
        game.k.u = (short)-1;
        for (int i2 = 0; i2 < this.q.A; ++i2) {
            this.q.z[i2].l(1);
            this.q.z[i2].u(1);
            this.q.z[i2].c();
        }
        game.k.a().f = this.m[i1 << 2];
        game.k.a().g = this.m[(i1 << 2) + 1];
        game.g.o().b(this.m[(i1 << 2) + 2], this.m[(i1 << 2) + 3]);
        game.g.o().b.b(this.m[(i1 << 2) + 2], this.m[(i1 << 2) + 3]);
        game.g.o().n = (byte)2;
        game.i.a().a((byte)10);
    }

    public final void aG() {
        this.o.c((byte)0);
        this.o.d((byte)0);
        this.bw();
        Object v1 = new int[]{4, 1, 4};
        v1 = an.f(599) + an.a(604, v1);
        this.c((String)v1, "K\u00edch ho\u1ea1t");
    }

    public final void aH() {
        this.o.c((byte)1);
        this.o.d((byte)0);
        this.bw();
        Object v1 = new int[]{2, 1, 2};
        v1 = an.f(600) + an.a(604, v1);
        this.c((String)v1, "Mua s\u1eafm t\u1ea5t tr\u00fang c\u1ea7u");
    }

    public final void aI() {
        this.f = 0;
        this.o.c((byte)4);
        this.o.d((byte)0);
        this.bw();
        Object v1 = new int[]{2, 1, 2};
        v1 = an.f(603) + an.a(604, v1);
        this.c((String)v1, "Mua s\u1eafm huy hi\u1ec7u");
    }

    public final void aJ() {
        this.f = 0;
        this.o.c((byte)2);
        this.o.d((byte)0);
        this.bw();
        Object v1 = new int[]{2, 1, 2};
        v1 = an.f(601) + an.a(604, v1);
        this.c((String)v1, "Mua s\u1eafm kim ti\u1ec1n");
    }

    private void bw() {
        this.p.a("/data/ui/smsInfo.ui", 257, this);
        if (this.o instanceof k) {
            this.p.a.a(6).a(true);
            this.p.a.a(7).a(true);
            this.p.a.a(10).a(false);
            this.p.a.a(11).a(false);
            return;
        }
        this.p.a.a(6).a(false);
        this.p.a.a(7).a(false);
        this.p.a.a(10).a(true);
        this.p.a.a(11).a(true);
        this.p.a.a((int)10).h().a = "X\u00e1c nh\u1eadn";
        this.p.a.a((int)11).h().a = "Ph\u1ea3n h\u1ed3i";
    }

    private void c(String v1, String v2) {
        this.p.a.a((int)8).h().a = v1;
        this.p.a.a((int)5).h().a = v2;
    }

    private void bx() {
        this.p.a("/data/ui/smsInfo.ui");
    }

    public final void aK() {
        if (!this.p.c("/data/ui/smsTip.ui")) {
            this.p.a("/data/ui/smsTip.ui", 257, this);
        }
        for (int i1 = 0; i1 < 3; ++i1) {
            this.p.a.a(i1 + 6).a(false);
        }
        this.g = true;
    }

    public final void d(String v1) {
        this.g = true;
        this.p.a.a((int)5).h().a = v1;
    }

    public final void aL() {
        this.p.a("/data/ui/smsTip.ui");
    }

    public final void aM() {
        switch (this.o.N()) {
            case 0: {
                if (this.o.k(16400) || this.o.k(32832)) break;
                if (this.o.k(131072)) {
                    if (this.o.O() > 1) {
                        this.o.d((byte)1);
                        return;
                    }
                    this.o.g(1);
                    return;
                }
                if (!this.o.k(786432)) break;
                this.bx();
                this.o.d((byte)5);
                this.o.a(this.o.Q);
                return;
            }
            case 1: {
                if (this.o.k(131072)) {
                    this.o.h(1);
                    return;
                }
                if (!this.o.k(262144)) break;
                this.o.h(2);
                return;
            }
            case 2: {
                boolean i1 = false;
                if (this.o.P == 100) {
                    if (this.x >= this.O.length && this.ax()) {
                        i1 = true;
                    } else if (this.ax()) {
                        this.b(this.O[this.x]);
                        ++this.x;
                    }
                    this.f();
                } else {
                    i1 = true;
                }
                if (!i1 || !this.bu() || !this.o.k(917504)) break;
                this.x = 0;
                if (this.o.M()) {
                    this.bx();
                    this.aL();
                    this.o.a(this.o.Q);
                } else {
                    this.o.d((byte)5);
                }
                this.f = 0;
                return;
            }
            case 3: {
                if (!this.o.k(393216)) break;
                this.o.h(1);
            }
        }
    }

    public final void a(byte i1, int i2, int i3) {
        this.c = 0;
        this.R = i1;
        this.S = (byte)i2;
        switch (i2) {
            case 0: {
                this.p.a("/data/ui/wharf1.ui", 257, this);
                this.p.a.a((int)8).h().a = an.f(i3);
                for (i2 = 0; i2 < this.P[i1].length; ++i2) {
                    this.p.a.a((int)(i2 + 5)).h().a = an.f(this.P[i1][i2]);
                }
                break;
            }
            case 1: {
                this.p.a("/data/ui/wharf2.ui", 257, this);
                this.p.a.a((int)10).h().a = an.f(i3);
                for (i2 = 0; i2 < this.P[i1].length; ++i2) {
                    this.p.a.a((int)(i2 + 5)).h().a = an.f(this.P[i1][i2]);
                }
                break;
            }
        }
        this.p.a.a((int)(5 + this.P[i1].length)).h().a = "Kh\u00f4ng ra h\u00e0ng";
    }

    public final void aN() {
        if (this.o.k(4100) && !this.j()) {
            this.p.a.b(0);
        } else if (this.o.k(8448) && !this.j()) {
            this.p.a.b(1);
        } else if (this.o.k(196640) && !this.j()) {
            if (this.c == this.Q[this.R].length / 9) {
                switch (this.S) {
                    case 0: {
                        this.p.a("/data/ui/wharf1.ui");
                        break;
                    }
                    case 1: {
                        this.p.a("/data/ui/wharf2.ui");
                    }
                }
                this.o.a((byte)0);
                if (game.k.u != -1 && game.k.a().d[game.k.u].u() == 0) {
                    game.k.a().a(game.k.a().d[game.k.u].i, game.k.a().d[game.k.u].j - 40, game.k.a().d[game.k.u]);
                }
            } else if (game.k.a().M.b[game.k.a((int)this.Q[this.R][this.c * 9 + 6], (int)this.Q[this.R][this.c * 9 + 7])] != null && game.k.a().M.b[game.k.a((int)this.Q[this.R][this.c * 9 + 6], (int)this.Q[this.R][this.c * 9 + 7])][this.Q[this.R][this.c * 9 + 8]] == 3) {
                game.k.a().f = this.Q[this.R][this.c * 9];
                game.k.a().g = this.Q[this.R][this.c * 9 + 1];
                game.k.a().h = this.Q[this.R][this.c * 9 + 2];
                game.k.a().i = this.Q[this.R][this.c * 9 + 3];
                game.k.w = (byte)this.Q[this.R][this.c * 9 + 4];
                game.k.a().j = -1;
                this.o.a((byte)29);
                switch (this.S) {
                    case 0: {
                        this.p.a("/data/ui/wharf1.ui");
                        break;
                    }
                    case 1: {
                        this.p.a("/data/ui/wharf2.ui");
                    }
                }
            } else {
                this.b("\u0110\u01b0\u1eddng th\u1ee7y ch\u01b0a m\u1edf");
            }
        } else if (this.o.k(262144) && !this.j()) {
            if (game.k.u != -1 && game.k.a().d[game.k.u].u() == 0) {
                game.k.a().a(game.k.a().d[game.k.u].i, game.k.a().d[game.k.u].j - 40, game.k.a().d[game.k.u]);
            }
            switch (this.S) {
                case 0: {
                    this.p.a("/data/ui/wharf1.ui");
                    break;
                }
                case 1: {
                    this.p.a("/data/ui/wharf2.ui");
                }
            }
            this.o.a((byte)0);
        }
        this.f();
    }

    public final void aO() {
        this.b = 0;
        this.p.a("/data/ui/shopbuy.ui", 257, this);
        this.b = 0;
        this.f = 0;
        ((al)this.p.a.a((int)0)).a.a = 1;
        ((al)this.p.a.a((int)0)).a.a(0);
        this.p.a.a(41).a(false);
        this.p.a.a(43).a(false);
        this.p.a.a((int)5).h().a = "Mua";
        this.p.a.a(57).a(true);
        this.p.a.a(58).a(true);
        this.p.a.a((int)57).h().a = "Mua s\u1eafm";
        this.p.a.a((int)58).h().a = "Ph\u1ea3n h\u1ed3i";
        this.p.a.a(39).a(false);
        this.p.a.a(40).a(false);
        h v1 = this;
        v1.w = ((al)v1.p.a.a((int)0)).a.e;
        v1.h = ((al)v1.p.a.a((int)0)).a.f;
        if (v1.p.a.a((int)51).h().m == null) {
            v1.p.a.a((int)51).h().m = new m();
            v1.p.a.a((int)51).h().m.a(0);
            v1.p.a.a((int)51).h().m.a = (byte)2;
            v1.p.a.a((int)51).h().m.a(258, false, (byte)-1);
        }
        v1.p.a.a((int)51).h().m.a(aq.c[5][0][1]);
        v1.p.a.a((int)14).h().a = an.f(aq.c[5][0][0]);
        v1.p.a.a((int)15).h().a = "5000";
        v1.p.a.a((int)45).h().m.a(84);
        v1.p.a.a((int)56).h().a = "\u1ea4p tr\u1ee9ng ra s\u1ee7ng v\u1eadt";
        v1.p.a.a((int)44).h().a = "" + v1.q.E();
        v1.p.a.a(38).b(102 + v1.h * 84 / aq.c[5].length, v1.p.a.a());
    }

    private void by() {
        this.p.a("/data/ui/shopbuy.ui");
    }

    public final int aP() {
        if (this.o.k(196640)) {
            if (this.f == 0) {
                if (this.q.t(5000)) {
                    if (this.q.k(0)) {
                        this.E();
                        this.a("\u0110\u00e3 c\u00f3 tr\u01b0\u0301ng su\u0309ng v\u00e2\u0323t, kh\u00f4ng c\u1ea7n mua s\u1eafm", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                        this.f = 2;
                    } else {
                        this.q.e(0, -1);
                        this.E();
                        this.a("\u0110\u00e3 th\u00e0nh c\u00f4ng mua s\u1eafm #2 tr\u01b0\u0301ng su\u0309ng v\u00e2\u0323t", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                        this.f = 2;
                    }
                } else {
                    this.E();
                    this.a("Kim ti\u1ec1n ch\u01b0a \u0111\u1ee7", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                    this.f = 1;
                }
            } else if (this.f > 0) {
                this.F();
                if (this.f == 1) {
                    this.o.a((byte)102);
                } else if (this.f == 2) {
                    game.c.m = 0;
                    this.by();
                    this.o.a((byte)0);
                }
            }
        } else if (this.o.k(262144) && this.f == 0) {
            game.c.m = 1;
            this.by();
            this.o.a((byte)0);
        }
        return -1;
    }

    public final void aQ() {
        this.p.a("/data/ui/wharf2.ui", 257, this);
        ((al)this.p.a.a((int)0)).a.f = this.d;
        this.f = 0;
        this.p.a.a((int)10).h().a = "Ti\u1ec7n l\u1ee3i \u0111i\u1ebfm";
        this.p.a.a((int)12).h().a = "Ti\u1ebfn v\u00e0o";
        for (int i1 = 0; i1 < this.T.length; ++i1) {
            this.p.a.a((int)(i1 + 5)).h().a = this.T[i1];
        }
    }

    public final void aR() {
        if (this.o.k(4100) && !this.j()) {
            this.p.a.b(0);
            return;
        }
        if (this.o.k(8448) && !this.j()) {
            this.p.a.b(1);
            return;
        }
        if (this.o.k(196640) && !this.j()) {
            if (this.f == 0) {
                switch (this.d) {
                    case 0: {
                        this.p.a("/data/ui/wharf2.ui");
                        this.o.a((byte)31);
                        return;
                    }
                    case 1: 
                    case 2: {
                        game.k.a();
                        if (game.k.K) {
                            this.p.a("/data/ui/wharf2.ui");
                            this.c = 0;
                            this.o.a((byte)7);
                            return;
                        }
                        this.E();
                        this.f = 1;
                        this.a("C\u00f4ng n\u0103ng theo \u0111\u1ea1o h\u1ecdc sau m\u1edf ra", "Nh\u1ea5n n\u00fat 5 \u0111\u1ec3 ti\u1ebfp t\u1ee5c");
                        return;
                    }
                    case 3: {
                        this.p.a("/data/ui/wharf2.ui");
                        this.o.a((byte)32);
                        return;
                    }
                    case 4: {
                        this.p.a("/data/ui/wharf2.ui");
                        this.o.a((byte)0);
                    }
                }
                return;
            }
            this.f = 0;
            this.F();
            this.g = true;
            return;
        }
        if (this.f == 0 && this.o.k(262144) && !this.j()) {
            this.p.a("/data/ui/wharf2.ui");
            this.o.a((byte)0);
        }
    }

    public final void a(int[] v1) {
        this.z = v1;
        if (this.o instanceof k) {
            switch (((k)this.o).P) {
                case 0: {
                    return;
                }
                case 1: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.b = v3[0];
                    return;
                }
                case 2: 
                case 26: 
                case 32: {
                    this.d(v1);
                    return;
                }
                case 3: {
                    int[] v3 = v1;
                    h v2 = this;
                    if (v2.f == 0) {
                        v2.b = v3[0];
                        v2.aX();
                        return;
                    }
                    v2.r = v3[0];
                    return;
                }
                case 4: {
                    return;
                }
                case 5: {
                    int[] v3 = v1;
                    h v2 = this;
                    this.b = v3[1];
                    v2.bm();
                    return;
                }
                case 6: {
                    int[] v3 = v1;
                    h v2 = this;
                    this.b = v3[0];
                    if (an.X) {
                        v2.p.a.a((int)14).h().a = an.f(605 + v2.b);
                        return;
                    }
                    v2.p.a.a((int)14).h().a = an.f(606 + v2.b);
                    return;
                }
                case 7: {
                    this.c(v1);
                    return;
                }
                case 8: {
                    int[] v3 = v1;
                    h v2 = this;
                    if (v3[0] >= 0) {
                        v2.c = v3[0];
                    }
                    if (v3[1] >= 0) {
                        v2.b = v3[1];
                    }
                    v2.bj();
                    return;
                }
                case 9: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.c = v3[1];
                    return;
                }
                case 10: {
                    int[] v3 = v1;
                    h v2 = this;
                    this.b = v3[1];
                    switch (v2.b) {
                        case 0: {
                            v2.c = v3[0];
                            return;
                        }
                        case 1: {
                            v2.r = v3[0];
                        }
                    }
                    return;
                }
                case 11: {
                    int[] v3 = v1;
                    h v2 = this;
                    this.c = v3[0];
                    v2.b = v3[1];
                    return;
                }
                case 12: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.b = v3[1];
                    return;
                }
                case 13: {
                    if (this.f == 0) {
                        this.b = v1[0];
                        return;
                    }
                    this.c = v1[0];
                    return;
                }
                case 14: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.c = v3[0];
                    return;
                }
                case 15: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.c = v3[0];
                    return;
                }
                case 16: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.b = v3[0];
                    return;
                }
                case 17: 
                case 18: 
                case 19: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.c = v3[0];
                    return;
                }
                case 20: {
                    this.c = v1[1];
                    return;
                }
                case 24: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.c = v3[0];
                    return;
                }
                case 28: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.c = v3[0];
                    return;
                }
                case 27: {
                    this.d = v1[0];
                }
            }
            return;
        }
        if (this.o instanceof d) {
            switch (((d)this.o).P) {
                case 2: {
                    return;
                }
                case 3: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.e = v3[0];
                    return;
                }
                case 4: {
                    int[] v3 = v1;
                    h v2 = this;
                    this.b = v3[0];
                    v2.be();
                    return;
                }
                case 5: {
                    this.c(v1);
                    return;
                }
                case 6: {
                    return;
                }
                case 7: {
                    return;
                }
                case 8: {
                    return;
                }
                case 9: {
                    return;
                }
                case 10: {
                    return;
                }
                case 11: {
                    this.d(v1);
                    return;
                }
                case 12: {
                    return;
                }
                case 13: {
                    return;
                }
                case 14: {
                    return;
                }
                case 15: {
                    return;
                }
                case 16: {
                    int[] v3 = v1;
                    h v2 = this;
                    this.c = v3[0];
                    v2.f(v2.c);
                    return;
                }
                case 17: {
                    return;
                }
                case 18: {
                    return;
                }
                case 19: {
                    return;
                }
                case 20: {
                    int[] v3 = v1;
                    h v2 = this;
                    this.a = v3[1];
                    v2.a(true);
                    return;
                }
                case 21: {
                    int[] v3 = v1;
                    Object v2 = null;
                    this.b = v3[0];
                }
                case 23: {
                    int[] v3 = v1;
                    this.b = v1[0];
                }
            }
        }
    }

    private void c(int[] v1) {
        if (this.f == 0) {
            this.b = v1[0];
            this.f(this.b);
            return;
        }
        if (this.f == 1) {
            this.c = v1[0];
            return;
        }
        if (this.f == 2) {
            this.r = v1[0];
            switch (this.c) {
                case 0: {
                    this.be();
                }
            }
        }
    }

    private void d(int[] v1) {
        if (this.f == 0) {
            this.b = v1[0];
            return;
        }
        this.r = v1[0];
    }

    static {
        A = new String[]{"Th\u1ee7y Kimura", "B\u00edch Th\u1ee7y th\u00e0nh", "Nguy\u00ean M\u1ed9c Th\u00e0nh", "Ni\u00eam Th\u1ed5 Th\u00e0nh", "H\u1eafc Th\u1ea1ch th\u00e0nh", "Thi\u00ean kh\u00f4ng", "Xa c\u1ed5"};
        B = new short[]{1, 0, 196, 208, 0, 2, 1, 196, 208, 0, 3, 3, 196, 208, 0, 4, 5, 320, 352, 0, 5, 3, 320, 196, 0, 7, 2, 288, 112, 0, 8, 0, 160, 144, 0};
    }
}

