/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Graphics
 */
package game;

import game.c;
import game.g;
import game.i;
import game.k;
import javax.microedition.lcdui.Graphics;

public final class a
extends f {
    public byte t;
    public boolean u;
    public byte v;
    public byte w;
    public short x;
    public short y;
    private int J;
    private int K;
    private int L;
    private int M;
    public byte z;
    public int A;
    public int B;
    public byte C;
    private short N;
    private short O;
    private short P;
    public short D;
    public short E;
    public short F;
    private boolean Q;
    private byte[] R = new byte[]{2, 3, 0, 1};
    private byte S = 0;
    public f G;
    public f H;
    public short I = (short)-1;
    private short[] T = new short[]{8, 9, 2, 96, 320, 0};

    public final void a(short[] sArray, int n2) {
        this.I = (short)n2;
        this.t = (byte)sArray[0];
        this.a.a((int)sArray[1], false);
        a a2 = this;
        a2.a.c();
        this.v = (byte)sArray[6];
        if (this.t == 0 && (this.v == 1 || this.v == 18)) {
            byte by;
            this.n = by = (byte)(sArray[2] % 3);
        }
        this.d((byte)sArray[2]);
        this.i = sArray[3];
        this.j = sArray[4];
        if (sArray[5] == 1) {
            this.b(true);
        } else {
            this.b(false);
        }
        switch (this.t) {
            case 0: {
                this.s = (byte)sArray[7];
                this.w = (byte)sArray[8];
                a a3 = this;
                if (a3.w != 0 && a3.G == null && a3.j()) {
                    a3.G = new f();
                    a3.G.a(259, false);
                    a3.G.a(a3.w, (byte)-1, true);
                    a3.G.b(a3.i, a3.j - 40);
                    a a4 = a3;
                    a3.G.p = a4;
                }
                a3.z();
                this.v();
                this.u = sArray[9] != 0;
                this.x = sArray[11];
                this.y = sArray[12];
                this.J = 0;
                this.L = ae.b(20, 40);
                this.z = 0;
                if (this.v == 12) {
                    this.n = 0;
                } else if (this.v == 13) {
                    this.n = 1;
                }
                if (this.v == 3) {
                    if (this.h == 4) {
                        this.n = 1;
                    }
                } else if (this.v == 2) {
                    if (this.h == 5) {
                        this.n = (byte)2;
                    } else if (this.h == 3) {
                        this.n = 0;
                    }
                }
                if ((this.v != 1 || this.a.a == 226) && this.v != 2 && this.v != 3 && this.v != 17) break;
                if (this.b == null) {
                    this.b = new f();
                    this.b.a(337, false);
                }
                this.b.b(this.i, this.j);
                if (this.a.a == 4) {
                    this.b.a((byte)0, (byte)0, this.Q);
                } else {
                    this.b.a((byte)1, (byte)0, this.Q);
                }
                this.b.c();
                break;
            }
            case 1: {
                this.s = sArray[1] == 320 ? 2 : 1;
                if (this.j() && sArray[0] > 0 && sArray[0] <= 3) {
                    game.k.a().e.addElement(this);
                }
                if (this.v == 3) {
                    this.u = true;
                }
                this.C = (byte)sArray[7];
                this.N = sArray[8];
                this.O = sArray[9];
                this.P = sArray[10];
                break;
            }
            case 2: {
                if (sArray[7] == 0) {
                    this.Q = false;
                    break;
                }
                this.Q = true;
                break;
            }
            case 3: {
                this.s = 1;
                this.D = sArray[7];
                this.E = sArray[8];
                switch (this.E) {
                    case 9: {
                        this.E = 1;
                        break;
                    }
                    case 10: {
                        this.E = 0;
                        break;
                    }
                    case 11: {
                        this.E = (short)2;
                        break;
                    }
                    case 12: {
                        this.E = (short)3;
                    }
                }
                this.F = sArray[9];
                this.u = true;
            }
        }
        this.c = new short[3];
        this.d = new short[3];
    }

    public final void d(byte by) {
        switch (this.t) {
            case 0: {
                if (this.v == 8) {
                    this.a((byte)0, (byte)-1, false);
                    this.J = 0;
                    this.h = by;
                    return;
                }
                if (this.v == 1 || this.v == 18) {
                    this.h = (byte)(by / 3);
                    if (this.h == 0) {
                        if (this.n == 3) {
                            this.a((byte)1, (byte)-1, false);
                            return;
                        }
                        this.a(this.n, (byte)-1, false);
                        return;
                    }
                    if (this.h != 1) break;
                    if (this.n == 3) {
                        this.a((byte)(this.h * 3 + 1), (byte)-1, false);
                        return;
                    }
                    this.a((byte)(this.h * 3 + this.n), (byte)-1, false);
                    return;
                }
                this.a(by, (byte)-1, false);
                this.h = by;
                game.k.a().a(this.I, 0, this.h, true);
                return;
            }
            case 1: {
                if (this.v == 0) {
                    switch (by) {
                        case 0: {
                            this.a(by, (byte)-1, false);
                            break;
                        }
                        case 1: {
                            this.a(by, (byte)-2, false);
                            break;
                        }
                        case 2: {
                            this.a(by, (byte)-1, false);
                            break;
                        }
                        case 3: {
                            this.a(by, (byte)-2, false);
                        }
                    }
                } else {
                    this.a(by, (byte)-1, false);
                    if (this.v == 3) {
                        game.k.a().a(this.I, 0, by, true);
                    }
                }
                this.h = by;
                return;
            }
            case 2: {
                this.h = by;
                return;
            }
            case 3: {
                this.a(by, (byte)-2, false);
                this.h = by;
                game.k.a().a(this.I, 0, this.h, true);
            }
        }
    }

    public final void o() {
        switch (this.t) {
            case 0: {
                Object object = this;
                block4 : switch (((a)object).v) {
                    case 2: {
                        if (((a)object).y()) {
                            byte[] byArray = new byte[]{0, 1, 2, 3, 5};
                            ((a)object).d(byArray[ae.a(5)]);
                            ((n)object).n = ((a)object).h == 3 || ((a)object).h == 0 ? (byte)0 : (((a)object).h == 5 || ((a)object).h == 2 ? (byte)2 : (ae.a(2) == 0 ? (byte)3 : (byte)1));
                        }
                        if (((a)object).h == 3) {
                            if (((a)object).J >= 64) {
                                ((a)object).d((byte)0);
                                break;
                            }
                            super.a(4);
                            ((a)object).J += 4;
                            break;
                        }
                        if (((a)object).h != 5) break;
                        if (((a)object).J <= 0) {
                            ((a)object).d((byte)2);
                            break;
                        }
                        super.a(4);
                        ((a)object).J -= 4;
                        break;
                    }
                    case 3: {
                        if (((a)object).y()) {
                            byte[] byArray = new byte[]{0, 1, 2, 4};
                            ((a)object).d(byArray[ae.a(4)]);
                            ((n)object).n = ((a)object).h == 0 ? (byte)0 : (((a)object).h == 2 ? (byte)2 : (ae.a(2) == 0 ? (byte)3 : (byte)1));
                        }
                        if (((a)object).h != 4) break;
                        if (((a)object).n == 1) {
                            if (((a)object).J >= 64) {
                                ((a)object).d((byte)0);
                                break;
                            }
                            super.a(4);
                            ((a)object).J += 4;
                            break;
                        }
                        if (((a)object).n != 3) break;
                        if (((a)object).J <= 0) {
                            ((a)object).d((byte)2);
                            break;
                        }
                        super.a(4);
                        ((a)object).J -= 4;
                        break;
                    }
                    case 11: {
                        break;
                    }
                    case 4: 
                    case 5: 
                    case 6: 
                    case 7: 
                    case 15: {
                        int n2;
                        if (((a)object).H != null && ((n)object).j() && ((a)object).H.p.equals(object) && (((a)object).h != 0 || !game.g.o().a((a)object, game.g.o().a.k(), ((a)object).a.k()))) {
                            ((a)object).x();
                        }
                        if (((a)object).h != 1 || !((a)object).a.e()) break;
                        ((a)object).d((byte)2);
                        if (((a)object).v == 6 && ((a)object).v == 7) {
                            game.k.a().a(((a)object).I, 0, ((a)object).h, false);
                        } else {
                            game.k.a().a(((a)object).I, 0, ((a)object).h, true);
                        }
                        if ((((a)object).v == 7 || ((a)object).v == 6) && (n2 = ae.a(2)) > 0) {
                            game.g.o().s(n2);
                            object = new int[]{n2, ((n)object).i, ((n)object).j - 20, 0};
                            game.g.o().V.addElement(object);
                        }
                        game.c.h = false;
                        break;
                    }
                    case 8: {
                        if (((a)object).h != 1) break;
                        if (((a)object).J < 2 && ((a)object).a(((a)object).n, 8, (byte)0)) {
                            ++((a)object).J;
                            super.a(8);
                            ((a)object).r();
                            break;
                        }
                        ((a)object).d((byte)0);
                        break;
                    }
                    case 9: 
                    case 10: {
                        if (((a)object).z != 1) break;
                        if ((((a)object).a.a == 302 || ((a)object).a.a == 298) && ((a)object).a(((a)object).n, 4, (byte)1)) {
                            super.a(4);
                            ((f)((a)object).p).a(4);
                            break;
                        }
                        if (((a)object).a(((a)object).n, 4, (byte)2)) {
                            super.a(4);
                            ((f)((a)object).p).a(4);
                            break;
                        }
                        ((a)object).r();
                        ((a)object).z = (byte)2;
                        ((a)object).d((byte)0);
                        break;
                    }
                    case 12: {
                        if (!((n)object).j()) break;
                        if (ae.a(game.g.o().i, game.g.o().j, ((a)object).i, ((a)object).j, ((a)object).a.k())) {
                            if (game.g.o().h() == 8) break;
                            game.g.o().b((byte)8, ((a)object).n);
                            break;
                        }
                        if (((a)object).a(((a)object).n, 4, (byte)0)) {
                            super.a(4);
                            break;
                        }
                        if (((a)object).n == 2) {
                            ((a)object).n = 0;
                            break;
                        }
                        ((a)object).n = (byte)2;
                        break;
                    }
                    case 13: {
                        if (!((n)object).j()) break;
                        if (ae.a(game.g.o().i, game.g.o().j, ((a)object).i, ((a)object).j, ((a)object).a.k())) {
                            if (game.g.o().h() == 8) break;
                            game.g.o().b((byte)8, ((a)object).n);
                            break;
                        }
                        if (((a)object).a(((a)object).n, 4, (byte)0)) {
                            super.a(4);
                            break;
                        }
                        if (((a)object).n == 3) {
                            ((a)object).n = 1;
                            break;
                        }
                        ((a)object).n = (byte)3;
                        break;
                    }
                    case 16: {
                        if (!ae.a(game.g.o().i, game.g.o().j, ((a)object).i, ((a)object).j, ((a)object).a.k()) || game.g.o().h() == 5) break;
                        game.g.o().b((byte)5, game.g.o().n);
                        break;
                    }
                    case 14: {
                        ((a)object).q();
                        if (((a)object).M < 4) {
                            ++((a)object).M;
                            break;
                        }
                        ((a)object).M = 0;
                        break;
                    }
                    case 1: {
                        int n3;
                        if (((a)object).h == 1) {
                            n3 = ((n)object).d[0];
                            super.a(n3);
                            if (game.k.a().p != null && game.k.a().p.p != null && game.k.a().p.p.equals(object) && game.g.o().a != null && !game.g.o().a((a)object, game.g.o().a.k(), ((a)object).a.k())) {
                                game.k.a().e();
                            }
                        }
                        if (!(game.k.a().p == null || game.k.a().p.p == null || !game.k.a().p.p.equals(object) || game.g.o().a == null || ((n)object).j() && game.g.o().a((a)object, game.g.o().a.k(), ((a)object).a.k()))) {
                            game.k.u = (short)-1;
                            game.k.a().e();
                        }
                        if (((a)object).G != null && ((n)object).j() && ((a)object).G.p.equals(object) && game.g.o().a != null && !game.g.o().a((a)object, game.g.o().a.k(), ((a)object).a.k())) {
                            ((a)object).z();
                        }
                        if (game.c.d == null || game.c.d.size() <= 0 || ((a)object).S != 1 || game.g.o().a == null || game.g.o().a((a)object, game.g.o().a.k(), ((a)object).a.k())) break;
                        for (n3 = 0; n3 < game.c.d.size(); ++n3) {
                            if (!((f)game.c.d.elementAt((int)n3)).p.equals(object)) continue;
                            ((f)game.c.d.elementAt(n3)).c();
                            break block4;
                        }
                        break;
                    }
                }
                break;
            }
            case 1: {
                if (this.v == 0) {
                    if (this.h == 0 && ae.a(game.g.o().i, game.g.o().j, this.i, this.j, game.g.o().a.k(), this.a.k())) {
                        this.d((byte)1);
                    } else if (this.h == 2 && !ae.a(game.g.o().i, game.g.o().j, this.i, this.j, game.g.o().a.k(), this.a.k())) {
                        this.d((byte)3);
                    } else if (this.h == 1 && this.a.e()) {
                        this.d((byte)2);
                    } else if (this.h == 3 && this.a.e()) {
                        this.d((byte)0);
                    }
                }
                if (this.v == 0 && this.a.e() || this.v == 1 || this.v == 3 && this.h() == 2) {
                    if (this.a.a == 320 && !this.j()) {
                        return;
                    }
                    if ((game.g.o().o != this.R[this.C] || this.a.a == 320 || this.a.a == 310) && this.a.a != 320 && this.a.a != 310 || !ae.a(game.g.o().i, game.g.o().j, this.i, this.j, game.g.o().a.k(), this.a.j())) break;
                    game.k.a().f = this.N;
                    game.k.a().g = this.O;
                    game.k.a().j = this.P;
                    game.i.a().a((byte)9);
                    break;
                }
                if (this.v == 2) {
                    if ((game.g.o().o != this.R[this.C] || this.a.a == 320) && this.a.a != 320 || !ae.a(game.g.o().i, game.g.o().j, this.i, this.j, game.g.o().a.k(), this.a.j())) break;
                    for (int i2 = 0; i2 < this.T.length / 6; ++i2) {
                        if (this.T[i2 * 6] != this.I || this.T[i2 * 6 + 1] != game.k.a().f || this.T[i2 * 6 + 2] != game.k.a().g) continue;
                        game.k.a().h = this.T[i2 * 6 + 3];
                        game.k.a().i = this.T[i2 * 6 + 4];
                        game.k.w = (byte)this.T[i2 * 6 + 5];
                        break;
                    }
                    game.k.a().f = this.N;
                    game.k.a().g = this.O;
                    game.k.a().j = -1;
                    game.i.a().a((byte)9);
                    break;
                }
                if (this.v != 4 || game.g.o().h() == 9 || game.g.o().h() == 10 || !ae.a(game.g.o().i, game.g.o().j, this.i, this.j, game.g.o().a.k(), this.a.j())) break;
                game.g.o().b(this.i, this.j);
                game.g.o().b.b(this.i, this.j);
                game.g.o().b((byte)9, this.n);
                game.k.a().j = this.P;
            }
        }
        this.f();
    }

    public final void p() {
        if (this.v == 9) {
            this.d((byte)1);
            this.n = (this.a.a == 302 || this.a.a == 298) && this.a((byte)1, 4, (byte)1) ? (byte)1 : (this.a((byte)1, 4, (byte)2) ? (byte)1 : (byte)3);
        } else if ((this.a.a == 302 || this.a.a == 298) && this.a((byte)2, 4, (byte)1)) {
            this.d((byte)2);
            this.n = (byte)2;
        } else if (this.a((byte)2, 4, (byte)2)) {
            this.d((byte)2);
            this.n = (byte)2;
        } else {
            this.d((byte)1);
            this.n = 0;
        }
        this.z = 1;
    }

    /*
     * Enabled aggressive block sorting
     */
    public final void q() {
        this.A = 0;
        while (true) {
            a a2 = null;
            boolean bl = false;
            int n2 = 16 * (this.A + 1);
            int n3 = this.a.g();
            a2 = this;
            byte by = 0;
            switch (n3) {
                case 2: {
                    by = j.a().b(a2.i, a2.j - n2);
                    for (n3 = 0; n3 < game.k.a().d.length; ++n3) {
                        if (game.k.a().d[n3].v == a2.v || game.k.a().d[n3].a.k() == null || !ae.a(a2.i, a2.j - n2, game.k.a().d[n3].i, game.k.a().d[n3].j, game.k.a().d[n3].a.k())) continue;
                        a a3 = a2;
                        game.k.a().d[n3].p = a3;
                        return;
                    }
                    break;
                }
                case 0: {
                    by = j.a().b(a2.i, a2.j + n2);
                    for (n3 = 0; n3 < game.k.a().d.length; ++n3) {
                        if (game.k.a().d[n3].v == a2.v || game.k.a().d[n3].a.k() == null || !ae.a(a2.i, a2.j + n2, game.k.a().d[n3].i, game.k.a().d[n3].j, game.k.a().d[n3].a.k())) continue;
                        a a4 = a2;
                        game.k.a().d[n3].p = a4;
                        return;
                    }
                    break;
                }
                case 3: {
                    by = j.a().b(a2.i - n2, a2.j);
                    for (n3 = 0; n3 < game.k.a().d.length; ++n3) {
                        if (game.k.a().d[n3].v == a2.v || game.k.a().d[n3].a.k() == null || !ae.a(a2.i - n2, a2.j, game.k.a().d[n3].i, game.k.a().d[n3].j, game.k.a().d[n3].a.k())) continue;
                        a a5 = a2;
                        game.k.a().d[n3].p = a5;
                        return;
                    }
                    break;
                }
                case 1: {
                    by = j.a().b(a2.i + n2, a2.j);
                    for (n3 = 0; n3 < game.k.a().d.length; ++n3) {
                        if (game.k.a().d[n3].v == a2.v || game.k.a().d[n3].a.k() == null || !ae.a(a2.i + n2, a2.j, game.k.a().d[n3].i, game.k.a().d[n3].j, game.k.a().d[n3].a.k())) continue;
                        a a6 = a2;
                        game.k.a().d[n3].p = a6;
                        return;
                    }
                }
            }
            if (by != 0) {
                return;
            }
            boolean bl2 = true;
            if (!bl2) {
                return;
            }
            ++this.A;
        }
    }

    private boolean y() {
        ++this.K;
        if (this.K >= this.L) {
            this.K = 0;
            this.L = ae.b(20, 40);
            return true;
        }
        return false;
    }

    private boolean a(byte by, int n2, byte by2) {
        byte by3 = 0;
        switch (by) {
            case 2: {
                by3 = j.a().b(this.i, this.j - this.a.k()[3] - n2);
                break;
            }
            case 0: {
                by3 = j.a().b(this.i, this.j + n2);
                break;
            }
            case 3: {
                by3 = j.a().b(this.i - n2 - this.a.k()[2] / 2, this.j);
                break;
            }
            case 1: {
                by3 = j.a().b(this.i + n2 + this.a.k()[2] / 2, this.j);
            }
        }
        return by3 == by2;
    }

    public final void b(Graphics graphics, int n2, int n3) {
        switch (this.a.g()) {
            case 2: {
                graphics.setColor(65280);
                graphics.fillRect(this.i - n2 - (this.M + 5) / 2, this.j - this.a.j()[3] - n3 - (this.A << 4) + 8, this.M + 5, this.A << 4);
                graphics.setColor(0xFFFFFF);
                graphics.fillRect(this.i - n2 - (this.M + 3) / 2, this.j - this.a.j()[3] - n3 - (this.A << 4) + 8, this.M + 3, this.A << 4);
                return;
            }
            case 0: {
                graphics.setColor(65280);
                graphics.fillRect(this.i - n2 - (this.M + 5) / 2, this.j - this.a.j()[3] - n3 + 20, this.M + 5, this.A + 1 << 4);
                graphics.setColor(0xFFFFFF);
                graphics.fillRect(this.i - n2 - (this.M + 3) / 2, this.j - this.a.j()[3] - n3 + 20, this.M + 3, this.A + 1 << 4);
                return;
            }
            case 3: {
                graphics.setColor(65280);
                graphics.fillRect(this.i - n2 - 8 - (this.A << 4), this.j - this.a.j()[3] - n3 - (this.M + 5) / 2 + 13, this.A << 4, this.M + 5);
                graphics.setColor(0xFFFFFF);
                graphics.fillRect(this.i - n2 - 8 - (this.A << 4), this.j - this.a.j()[3] - n3 - (this.M + 3) / 2 + 13, this.A << 4, this.M + 3);
                return;
            }
            case 1: {
                graphics.setColor(65280);
                graphics.fillRect(this.i - n2 + 7, this.j - this.a.j()[3] - n3 - (this.M + 5) / 2 + 13, this.A << 4, this.M + 5);
                graphics.setColor(0xFFFFFF);
                graphics.fillRect(this.i - n2 + 7, this.j - this.a.j()[3] - n3 - (this.M + 3) / 2 + 13, this.A << 4, this.M + 3);
            }
        }
    }

    public final void r() {
        game.k.a().a((int)this.I, 0, this.i);
        game.k.a().a((int)this.I, 1, this.j);
    }

    public final void s() {
        byte by = 0;
        if (this.j()) {
            by = 1;
        }
        game.k.a().a(this.I, 1, by, true);
        game.k.a().a(this.I, 0, this.h, true);
        game.k.a().a(this.I, 2, this.n, true);
    }

    public final void a(int n2) {
        super.a(n2);
    }

    public final void t() {
        this.S = 1;
    }

    public final byte u() {
        return this.S;
    }

    public final void f(int n2) {
        if (this.H == null && this.j()) {
            this.H = new f();
            this.H.a(259, false);
            this.H.a((byte)7, (byte)-1, true);
            this.H.b(this.i, this.j - n2);
            a a2 = this;
            this.H.p = a2;
        }
        a a3 = this;
        if (a3.H != null) {
            a3.H.c();
        }
    }

    public final void v() {
        if (game.k.a().b(this.I)) {
            this.w();
        }
    }

    private void z() {
        if (this.G != null) {
            this.G.c();
        }
    }

    public final void d() {
        super.d();
        this.w();
        this.e();
        this.x();
    }

    public final void w() {
        if (this.G != null) {
            this.G.d();
        }
    }

    public final void x() {
        if (this.H != null) {
            this.H.d();
        }
    }

    public final void b(int n2, int n3) {
        super.b(n2, n3);
        if (this.b != null && this.b.j()) {
            this.b.b(n2, n3);
        }
    }
}

