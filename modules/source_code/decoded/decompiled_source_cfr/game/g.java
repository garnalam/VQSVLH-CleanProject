/*
 * Decompiled with CFR 0.152.
 */
package game;

import game.a;
import game.c;
import game.i;
import game.k;
import java.util.Vector;

public final class g
extends f {
    private static g Y;
    public int t;
    private int Z;
    private int aa;
    public byte u;
    private int ab;
    private boolean ac;
    public int v;
    private int ad;
    private int ae;
    public int w;
    public int x;
    public boolean y;
    public game.b[] z;
    public int A;
    public byte[][] B;
    public byte[][] C;
    public byte[][] D;
    public byte[] E;
    public byte F;
    public byte G;
    public byte H;
    public byte I;
    public Vector J;
    public Vector K;
    public Vector L;
    public Vector M;
    public Vector N;
    public Vector O;
    public byte[] P;
    public byte[] Q;
    public short[] R;
    public Vector S;
    public boolean[] T;
    public static boolean U;
    public Vector V;
    private int af;
    private int ag;
    public int[] W = new int[]{0, 16, 32, 48, 64, 76, 88};
    public int[] X = new int[]{16, 16, 16, 16, 12, 12, 12};
    private static byte[][] ah;
    private static short[][] ai;
    private Vector aj = null;

    public static g o() {
        if (Y == null) {
            Y = new g();
        }
        return Y;
    }

    public final void p() {
        this.A = 0;
        Y = null;
    }

    public g() {
        int n2;
        this.c = new short[3];
        this.d = new short[3];
        this.z = new game.b[6];
        this.B = new byte[8][2];
        this.T = new boolean[21];
        this.J = new Vector();
        this.K = new Vector();
        int[] nArray = new int[]{0, 0, 1};
        this.K.addElement(nArray);
        this.L = new Vector();
        this.M = new Vector();
        this.N = new Vector();
        this.O = new Vector();
        this.V = new Vector();
        this.C = new byte[7][];
        this.E = new byte[7];
        this.D = new byte[7][];
        this.R = new short[]{-1, -1, -1, -1, -1};
        for (n2 = 0; n2 < this.D.length; ++n2) {
            this.D[n2] = new byte[this.X[n2]];
            for (int i2 = 0; i2 < this.D[n2].length; ++i2) {
                this.D[n2][i2] = -1;
            }
        }
        this.C[0] = new byte[16];
        this.C[1] = new byte[16];
        this.C[2] = new byte[16];
        this.C[3] = new byte[16];
        this.C[4] = new byte[12];
        this.C[5] = new byte[12];
        this.C[6] = new byte[12];
        this.P = new byte[4];
        this.Q = new byte[4];
        this.u = 0;
        for (n2 = 0; n2 < 8; ++n2) {
            this.B[n2][0] = 0;
            this.B[n2][1] = 0;
        }
        for (n2 = 0; n2 < 4; ++n2) {
            this.P[n2] = 0;
        }
        this.af = 1000;
        this.ag = 0;
        this.t = -1;
        this.y = false;
    }

    public final void a(short[] sArray) {
        short s2;
        if (this.t == -1) {
            this.a(0, false);
        }
        this.h(this.t);
        this.i = sArray[0];
        this.j = sArray[1];
        this.b((byte)0, (byte)sArray[2]);
        this.c[0] = s2 = sArray[3];
        this.c[1] = s2 = sArray[4];
        this.c[2] = s2 = sArray[5];
        if (this.t == -1) {
            this.g();
        }
        this.s = 1;
        this.ad = sArray[6];
        this.ae = sArray[7];
        this.v = this.C();
        if (this.u == 1) {
            this.a(0, 107);
        }
        if (this.b == null) {
            this.b = new f();
            this.b.a(337, false);
        }
        this.b.b(this.i, this.j);
        if (this.a.a == 4) {
            this.b.a((byte)0, (byte)0, false);
        } else {
            this.b.a((byte)1, (byte)0, false);
        }
        this.b.c();
        this.p = null;
        this.y = true;
    }

    public final void q() {
        if (this.n() && this.p.h() != 0) {
            this.a(((f)this.p).a, this.a);
            return;
        }
        switch (this.h) {
            case 0: {
                if (this.P[2] == 2 || !this.N()) break;
                this.b((byte)3, this.n);
                return;
            }
            case 1: {
                if (this.P[2] == 2) {
                    boolean bl;
                    int n2 = this.j;
                    int n3 = this.i;
                    g g2 = this;
                    switch (g2.n) {
                        case 2: {
                            bl = j.a().c(n3, n2 - 25 - g2.Z);
                            break;
                        }
                        case 0: {
                            bl = j.a().c(n3, n2 - 25 + g2.Z);
                            break;
                        }
                        case 3: {
                            bl = j.a().c(n3 - g2.Z, n2 - 25);
                            break;
                        }
                        case 1: {
                            bl = j.a().c(n3 + g2.Z, n2 - 25);
                            break;
                        }
                        default: {
                            bl = false;
                        }
                    }
                    if (bl) break;
                    game.k.u = this.K();
                    this.p = null;
                    for (int i2 = 0; i2 < game.k.a().d.length; ++i2) {
                        this.w(i2);
                    }
                    this.a((int)this.d[0]);
                    this.aa = this.aa < 8 ? (this.aa += this.d[0]) : 4;
                    this.O();
                    return;
                }
                game.k.u = this.K();
                if (this.L() && this.M()) {
                    this.a(this.Z);
                    this.aa = this.aa < 8 ? (this.aa += this.Z) : 4;
                    this.O();
                    return;
                }
                this.aa = 0;
                return;
            }
            case 2: {
                if (this.L() && this.M()) {
                    this.a(this.Z);
                    this.aa = this.aa < 8 ? (this.aa += this.Z) : 4;
                    this.O();
                    return;
                }
                this.aa = 0;
                return;
            }
            case 4: {
                return;
            }
            case 5: {
                int n4;
                if (this.ab < 16) {
                    int n5;
                    switch (this.m) {
                        case 1: 
                        case 3: {
                            if (this.m == 3) {
                                if (this.i > this.p.i) {
                                    this.b((int)this.m);
                                }
                            } else if (this.i < this.p.i) {
                                this.b((int)this.m);
                            }
                            if (this.l > this.p.j - 16) {
                                if (this.j <= this.p.j - 16) {
                                    this.j = n5 = this.p.j - 16;
                                    break;
                                }
                                this.b(2);
                                break;
                            }
                            if (this.l >= this.p.j - 16) break;
                            if (this.j >= this.p.j - 16) {
                                this.j = n5 = this.p.j - 16;
                                break;
                            }
                            this.b(0);
                            break;
                        }
                        case 0: 
                        case 2: {
                            if (this.m == 2) {
                                if (this.j > this.p.j - 16) {
                                    this.b((int)this.m);
                                }
                            } else if (this.j < this.p.j - 16) {
                                this.b((int)this.m);
                            }
                            if (this.k > this.p.i) {
                                if (this.i <= this.p.i) {
                                    this.i = n5 = this.p.i;
                                    break;
                                }
                                this.b(3);
                                break;
                            }
                            if (this.k >= this.p.i) break;
                            if (this.i >= this.p.i) {
                                this.i = n5 = this.p.i;
                                break;
                            }
                            this.b(1);
                        }
                    }
                    if (this.ab % 4 == 3) {
                        this.a((byte)1, (byte)-1, false);
                    } else {
                        this.a((byte)(this.ab % 4), (byte)-1, false);
                    }
                    this.n = n5 = (int)(this.ab % 4);
                    ++this.ab;
                    return;
                }
                int n6 = 0;
                for (n4 = 0; n4 < ah.length; ++n4) {
                    if (game.k.a().f != ah[n4][0] || game.k.a().g != ah[n4][1]) continue;
                    game.k.a().f = ah[n4][2];
                    game.k.a().g = ah[n4][3];
                    n6 = ah[n4][4];
                    break;
                }
                for (n4 = 0; n4 < ai[n6].length / 4; ++n4) {
                    if (((a)this.p).I < ai[n6][n4 << 2] || ((a)this.p).I > ai[n6][(n4 << 2) + 1]) continue;
                    game.k.a().h = ai[n6][(n4 << 2) + 2];
                    game.k.a().i = ai[n6][(n4 << 2) + 3];
                    break;
                }
                U = true;
                game.k.a().j = -1;
                game.i.a().a((byte)9);
                return;
            }
            case 6: {
                g g3 = this;
                boolean bl = true;
                switch (g3.n) {
                    case 3: {
                        bl = g3.d(j.a().b(g3.i - 16, g3.j));
                        break;
                    }
                    case 1: {
                        bl = g3.d(j.a().b(g3.i + 16, g3.j));
                        break;
                    }
                    case 2: {
                        bl = g3.d(j.a().b(g3.i, g3.j - 16));
                        break;
                    }
                    case 0: {
                        bl = g3.d(j.a().b(g3.i, g3.j + 16));
                    }
                }
                if (bl && j.a().b(g3.i, g3.j) != 3 ? false : bl) {
                    this.a((int)this.c[2]);
                    return;
                }
                this.a((int)this.d[1]);
                this.b((byte)0, this.n);
                return;
            }
            case 7: {
                if (((a)this.p).z == 0) {
                    byte by;
                    if (this.ab < 7) {
                        this.a(4);
                        ++this.ab;
                        return;
                    }
                    if (this.ab != 7) break;
                    if (this.n == 3) {
                        this.a((byte)1, (byte)-1, false);
                    } else {
                        this.a(this.n, (byte)-1, false);
                    }
                    this.n = by = this.n;
                    ((a)this.p).p();
                    ++this.ab;
                    return;
                }
                if (((a)this.p).z != 2) break;
                if (this.ab < 8 && this.ab > 0) {
                    this.a(4);
                    --this.ab;
                    return;
                }
                if (this.ab == 8) {
                    this.b((byte)7, this.n);
                    --this.ab;
                    return;
                }
                ((a)this.p).z = 0;
                ((a)this.p).p = null;
                this.p = null;
                this.b((byte)0, this.n);
                return;
            }
            case 8: {
                if (this.ab < 16) {
                    byte by;
                    if (this.ab % 4 == 3) {
                        this.a((byte)1, (byte)-1, false);
                    } else {
                        this.a((byte)(this.ab % 4), (byte)-1, false);
                    }
                    this.n = by = (byte)(this.ab % 4);
                    ++this.ab;
                    return;
                }
                int n7 = game.k.a().d[game.k.a().j].i - game.k.a().d[game.k.a().j].i % this.c[2];
                int n8 = game.k.a().d[game.k.a().j].j - game.k.a().d[game.k.a().j].j % this.c[2];
                this.b(n7, n8);
                this.b.b(n7, n8);
                this.b((byte)0, game.k.a().d[game.k.a().j].C);
                this.a(32);
                ai.a().a(8);
                ai.a().b();
                return;
            }
            case 9: {
                if (this.ab < 16) {
                    byte by;
                    if (this.ab % 4 == 3) {
                        this.a((byte)1, (byte)-1, false);
                    } else {
                        this.a((byte)(this.ab % 4), (byte)-1, false);
                    }
                    this.n = by = (byte)(this.ab % 4);
                    ++this.ab;
                    return;
                }
                int n9 = game.k.a().d[game.k.a().j].i - game.k.a().d[game.k.a().j].i % this.c[2];
                int n10 = game.k.a().d[game.k.a().j].j - game.k.a().d[game.k.a().j].j % this.c[2];
                this.b(n9, n10);
                this.b.b(n9, n10);
                this.b((byte)10, this.n);
                ai.a().a(8);
                ai.a().b();
                return;
            }
            case 10: {
                if (this.ab > 0) {
                    byte by;
                    if (this.ab % 4 == 3) {
                        this.a((byte)1, (byte)-1, false);
                    } else {
                        this.a((byte)(this.ab % 4), (byte)-1, false);
                    }
                    this.n = by = (byte)(this.ab % 4);
                    --this.ab;
                    return;
                }
                this.b((byte)0, game.k.a().d[game.k.a().j].C);
                this.a(32);
            }
        }
    }

    public final void b(byte by, byte by2) {
        block11: while (true) {
            switch (by) {
                case 0: {
                    if (this.P[2] != 2 && this.N()) {
                        by = (byte)3;
                        continue block11;
                    }
                    if (by2 == 3) {
                        this.a((byte)1, (byte)-1, false);
                    } else {
                        this.a(by2, (byte)-1, false);
                    }
                    this.n = by2;
                    break block11;
                }
                case 1: {
                    if (this.P[2] != 2 && this.N()) {
                        by = (byte)2;
                        continue block11;
                    }
                    if (this.a.g() < 6) {
                        if (by2 == 3) {
                            this.a((byte)4, by2);
                            break block11;
                        }
                        this.a((byte)(by2 + 3), by2);
                        break block11;
                    }
                    if (by2 == 3) {
                        this.a((byte)(by * 3 + 1), (byte)-1, false);
                    } else {
                        this.a((byte)(by * 3 + by2), (byte)-1, false);
                    }
                    this.n = by2;
                    break block11;
                }
                case 2: {
                    if (this.N()) {
                        if (this.a.g() < 9) {
                            if (by2 == 3) {
                                this.a((byte)7, by2);
                            } else {
                                this.a((byte)(by2 + 6), by2);
                            }
                        } else if (by2 == 3) {
                            this.a((byte)(by * 3 + 1), (byte)-1, false);
                        } else {
                            this.a((byte)(by * 3 + by2), (byte)-1, false);
                        }
                    } else if (by2 == 3) {
                        this.a((byte)(this.h * 3 + 1), (byte)-1, false);
                    } else {
                        this.a((byte)(this.h * 3 + by2), (byte)-1, false);
                    }
                    this.n = by2;
                    break block11;
                }
                case 3: {
                    if (by2 == 3) {
                        this.a((byte)(by * 3 + 1), (byte)-1, false);
                    } else {
                        this.a((byte)(by * 3 + by2), (byte)-1, false);
                    }
                    this.n = by2;
                    break block11;
                }
                case 4: {
                    this.a((byte)(by * 3), (byte)-2, false);
                    this.n = by2;
                    this.aa = 0;
                    break block11;
                }
                case 5: {
                    this.ab = 0;
                    this.m = by2;
                    int n2 = this.j;
                    int n3 = this.i;
                    g g2 = this;
                    this.k = n3;
                    g2.l = n2;
                    if (by2 == 3) {
                        this.a((byte)1, (byte)-1, false);
                    } else {
                        this.a(by2, (byte)-1, false);
                    }
                    this.n = by2;
                    break block11;
                }
                case 6: {
                    if (by2 == 3) {
                        this.a((byte)1, (byte)-1, false);
                    } else {
                        this.a(by2, (byte)-1, false);
                    }
                    this.n = by2;
                    break block11;
                }
                case 7: {
                    if (by2 == 3) {
                        this.a((byte)4, (byte)-1, false);
                    } else {
                        this.a((byte)(by2 + 3), (byte)-1, false);
                    }
                    this.n = by2;
                    break block11;
                }
                case 8: 
                case 9: {
                    this.ab = 0;
                    this.n = by2;
                }
            }
            break;
        }
        this.h = by;
        if (this.h == 0 || this.h == 1) {
            if (game.k.a().o != null) {
                game.k.a().o.a(true);
                return;
            }
        } else if (game.k.a().o != null) {
            game.k.a().o.a(false);
        }
    }

    public final boolean f(int n2) {
        return this.P[n2] != 0;
    }

    public final boolean g(int n2) {
        return this.Q[n2] != 1;
    }

    public final boolean r() {
        return this.t != 2 || j.a().b(this.i + 7, this.j + 7) == 0 && j.a().b(this.i - 8, this.j - 8) == 0;
    }

    public final void h(int n2) {
        if (n2 == -1) {
            return;
        }
        this.P[n2] = 2;
        this.a.b();
        this.a(n2 + 1, false);
        if (this.u == 1) {
            this.a(1, 107);
        }
        this.b((byte)0, this.n);
        this.d[0] = this.P[n2] == 2 && n2 == 0 || this.P[n2] == 2 && n2 == 1 ? 8 : 4;
        if (this.P[2] == 2 && game.k.a().o != null) {
            game.k.a().o.d();
        }
        this.Z = this.d[0];
        this.t = n2;
    }

    public final void s() {
        int n2;
        this.a.b();
        this.a(0, false);
        for (n2 = 0; n2 < 4; ++n2) {
            if (this.P[n2] != 2) continue;
            this.P[n2] = 1;
        }
        if (this.u == 1) {
            this.a(0, 107);
        }
        if (game.k.a().o != null) {
            game.k.a().o.c();
        }
        this.d[0] = n2 = this.c[0];
        this.t = -1;
    }

    public final void t() {
        this.u = 1;
        boolean bl = false;
        for (int i2 = 0; i2 < 4; ++i2) {
            if (this.P[i2] != 2) continue;
            bl = true;
            break;
        }
        if (bl) {
            this.a(1, 107);
            return;
        }
        this.a(0, 107);
    }

    public final void u() {
        this.u = 0;
        boolean bl = false;
        for (int i2 = 0; i2 < 4; ++i2) {
            if (this.P[i2] != 2) continue;
            bl = true;
            break;
        }
        if (bl) {
            this.a(1, 100);
            return;
        }
        this.a(0, 100);
    }

    private short K() {
        for (short s2 = 0; s2 < game.k.a().d.length; s2 = (short)((short)(s2 + 1))) {
            if (!game.k.a().d[s2].j() || game.k.a().d[s2].a.a > 85 && game.k.a().d[s2].a.a != 226 && game.k.a().d[s2].a.a != 92 && game.k.a().d[s2].a.a != 102 && game.k.a().d[s2].a.a != 137 || game.k.a().d[s2].t != 0 || game.k.a().d[s2].v != 1 && game.k.a().d[s2].v != 18 || !this.a(game.k.a().d[s2], this.a.k(), game.k.a().d[s2].a.k())) continue;
            if (game.k.a().d[s2].u() == 0) {
                game.k.a().a(game.k.a().d[s2].i, game.k.a().d[s2].j - 40, game.k.a().d[s2]);
                if (game.k.a().d[s2].G != null) {
                    game.k.a().d[s2].G.d();
                }
            } else if (game.k.a().d[s2].u() == 1) {
                game.k.a().a(game.k.a().d[s2].i, game.k.a().d[s2].j - 40, game.k.a().d[s2]);
                if (game.c.d != null && game.c.d.size() > 0) {
                    for (int i2 = 0; i2 < game.c.d.size(); ++i2) {
                        if (!((f)game.c.d.elementAt((int)i2)).p.equals(game.k.a().d[s2])) continue;
                        ((f)game.c.d.elementAt(i2)).d();
                        break;
                    }
                }
            } else {
                game.k.a().a(game.k.a().d[s2].i, game.k.a().d[s2].j - 40, game.k.a().d[s2]);
                if (game.k.a().d[s2].w != 0) {
                    game.k.a().d[s2].w();
                }
            }
            return s2;
        }
        game.k.a().e();
        game.c.g = false;
        return -1;
    }

    private boolean w(int n2) {
        switch (game.k.a().d[n2].t) {
            case 3: {
                short[] sArray = game.k.a().d[n2].a.k();
                short s2 = sArray[0];
                short s3 = sArray[1];
                short s4 = (short)(sArray[2] + 16);
                short s5 = (short)(sArray[3] + 16);
                if (!game.k.a().d[n2].u || !this.a(game.k.a().d[n2], this.a.k(), new short[]{s2, s3, s4, s5})) break;
                a a2 = game.k.a().d[n2];
                this.p = a2;
            }
        }
        return true;
    }

    private boolean L() {
        f f2 = null;
        this.p = null;
        int n2 = 1;
        if (this.aj != null) {
            this.aj.removeAllElements();
        }
        block24: for (int i2 = 0; i2 < game.k.a().d.length; ++i2) {
            this.w(i2);
            if (!game.k.a().d[i2].u || !this.a(game.k.a().d[i2], this.a.k(), game.k.a().d[i2].a.k())) continue;
            switch (game.k.a().d[i2].t) {
                case 0: {
                    switch (game.k.a().d[i2].v) {
                        case 0: {
                            return false;
                        }
                        case 4: 
                        case 11: {
                            if (game.k.a().d[i2].h() == 2 || !game.k.a().d[i2].j()) break;
                            f2 = game.k.a().d[i2];
                            this.p = f2;
                            return false;
                        }
                        case 5: {
                            if (game.k.a().d[i2].h() != 2) {
                                if (this.B[5][0] == 2) {
                                    f2 = game.k.a().d[i2];
                                    this.p = f2;
                                }
                                return false;
                            }
                        }
                        case 6: {
                            if (game.k.a().d[i2].h() == 2) break;
                            if (this.P[3] != 2) {
                                if (this.B[2][0] == 2) {
                                    f2 = game.k.a().d[i2];
                                    this.p = f2;
                                    if (this.aj == null) {
                                        this.aj = new Vector();
                                    }
                                    this.aj.addElement(game.k.a().d[i2]);
                                    game.k.a().d[i2].f(20);
                                }
                                n2 = 0;
                                break;
                            }
                            game.k.a().d[i2].d((byte)1);
                            game.k.a().b.c(game.k.a().d[i2]);
                            break;
                        }
                        case 15: {
                            if (game.k.a().d[i2].h() == 2) break;
                            if (this.T[6]) {
                                f2 = game.k.a().d[i2];
                                this.p = f2;
                                return false;
                            }
                            game.k.a().d[i2].d((byte)1);
                            game.k.a().b.c(game.k.a().d[i2]);
                            break;
                        }
                        case 7: {
                            if (game.k.a().d[i2].h() == 2) break;
                            if (this.P[3] != 2) {
                                if (this.B[1][0] == 2) {
                                    f2 = game.k.a().d[i2];
                                    this.p = f2;
                                    if (this.aj == null) {
                                        this.aj = new Vector();
                                    }
                                    this.aj.addElement(game.k.a().d[i2]);
                                    game.k.a().d[i2].f(30);
                                }
                                n2 = 0;
                                break;
                            }
                            game.k.a().d[i2].d((byte)1);
                            game.k.a().b.c(game.k.a().d[i2]);
                            break;
                        }
                        case 8: {
                            byte by;
                            if (!game.k.a().d[i2].j()) break;
                            if ((a)game.k.a().d[i2].p != null && ((a)game.k.a().d[i2].p).B > ((a)game.k.a().d[i2].p).A) {
                                return false;
                            }
                            block25: for (n2 = 0; n2 < game.k.a().d.length; ++n2) {
                                if (!game.k.a().d[n2].u || game.k.a().d[n2].equals(game.k.a().d[i2]) || game.k.a().d[n2].t != 0 || game.k.a().d[n2].v != 8 && game.k.a().d[n2].v != 11) continue;
                                switch (this.n) {
                                    case 2: {
                                        if (!ae.a(game.k.a().d[i2].i, game.k.a().d[i2].j - 8, game.k.a().d[n2].i, game.k.a().d[n2].j, game.k.a().d[i2].a.k(), game.k.a().d[n2].a.k())) continue block25;
                                        return false;
                                    }
                                    case 0: {
                                        if (!ae.a(game.k.a().d[i2].i, game.k.a().d[i2].j + 8, game.k.a().d[n2].i, game.k.a().d[n2].j, game.k.a().d[i2].a.k(), game.k.a().d[n2].a.k())) continue block25;
                                        return false;
                                    }
                                    case 3: {
                                        if (!ae.a(game.k.a().d[i2].i - 8, game.k.a().d[i2].j, game.k.a().d[n2].i, game.k.a().d[n2].j, game.k.a().d[i2].a.k(), game.k.a().d[n2].a.k())) continue block25;
                                        return false;
                                    }
                                    case 1: {
                                        if (!ae.a(game.k.a().d[i2].i + 8, game.k.a().d[i2].j, game.k.a().d[n2].i, game.k.a().d[n2].j, game.k.a().d[i2].a.k(), game.k.a().d[n2].a.k())) continue block25;
                                        return false;
                                    }
                                }
                            }
                            game.k.a().d[i2].d((byte)1);
                            game.k.a().d[i2].n = by = this.n;
                            return false;
                        }
                        case 9: {
                            if (j.a().b(this.i, this.j) == 2 || j.a().b(this.i, this.j) == 1) {
                                return false;
                            }
                            if (this.n != 3 && this.n != 1) break;
                            this.ab = 0;
                            this.b((byte)7, this.n);
                            f2 = this;
                            game.k.a().d[i2].p = f2;
                            f2 = game.k.a().d[i2];
                            this.p = f2;
                            return false;
                        }
                        case 10: {
                            if (j.a().b(this.i, this.j) == 2 || j.a().b(this.i, this.j) == 1) {
                                return false;
                            }
                            if (this.n != 0 && this.n != 2) break;
                            this.ab = 0;
                            this.b((byte)7, this.n);
                            f2 = this;
                            game.k.a().d[i2].p = f2;
                            f2 = game.k.a().d[i2];
                            this.p = f2;
                            return false;
                        }
                        case 14: {
                            return false;
                        }
                        case 16: {
                            f2 = game.k.a().d[i2];
                            this.p = f2;
                        }
                    }
                    continue block24;
                }
                case 3: {
                    return false;
                }
                case 1: {
                    if (game.k.a().d[i2].v != 3) continue block24;
                    return false;
                }
            }
        }
        return n2 != 0;
    }

    private boolean M() {
        this.Z = this.d[0];
        int n2 = this.i - 8;
        int n3 = this.j - 8;
        int n4 = this.i + 7;
        int n5 = this.j + 7;
        byte[] byArray = new byte[]{-1, -1, -1, -1, -1};
        switch (this.n) {
            case 2: {
                if (j.a().c(this.i, n3 - this.Z)) {
                    return false;
                }
                byArray[0] = j.a().b(n2, n3 - this.Z);
                byArray[1] = j.a().b(n4, n3 - this.Z);
                byArray[2] = j.a().b(this.i, n3 - this.Z);
                if (!this.d(byArray[0]) && !this.d(byArray[1])) {
                    if (j.a().b(this.i, this.j) == 3) {
                        return false;
                    }
                    byArray[0] = j.a().b(this.i - 16, n3 - this.Z);
                    byArray[1] = j.a().b(this.i + 16, n3 - this.Z);
                    byArray[3] = j.a().b(this.i - 16, this.j);
                    byArray[4] = j.a().b(this.i + 16, this.j);
                    this.Z = this.d[1];
                    if (!this.d(byArray[0])) {
                        return this.d(byArray[1], (byte)1);
                    }
                    if (!this.d(byArray[1])) {
                        return this.d(byArray[0], (byte)3);
                    }
                    if (this.d(byArray[4])) {
                        return this.d(byArray[1], (byte)1);
                    }
                    if (this.d(byArray[3])) {
                        return this.d(byArray[0], (byte)3);
                    }
                } else {
                    if (!this.d(byArray[0])) {
                        this.Z = this.d[1];
                        return this.d(byArray[1], (byte)1);
                    }
                    if (!this.d(byArray[1])) {
                        this.Z = this.d[1];
                        return this.d(byArray[0], (byte)3);
                    }
                    return this.d(byArray[2], (byte)2);
                }
            }
            case 0: {
                if (j.a().c(this.i, n5 + this.Z)) {
                    return false;
                }
                byArray[0] = j.a().b(n2, n5 + this.Z);
                byArray[1] = j.a().b(n4, n5 + this.Z);
                byArray[2] = j.a().b(this.i, n5 + this.Z);
                if (!this.d(byArray[0]) && !this.d(byArray[1])) {
                    if (j.a().b(this.i, this.j) == 3) {
                        return false;
                    }
                    byArray[0] = j.a().b(n2 - 16, n5 + this.Z);
                    byArray[1] = j.a().b(n4 + 16, n5 + this.Z);
                    byArray[3] = j.a().b(this.i - 16, this.j);
                    byArray[4] = j.a().b(this.i + 16, this.j);
                    this.Z = this.d[1];
                    if (!this.d(byArray[0])) {
                        return this.d(byArray[1], (byte)1);
                    }
                    if (!this.d(byArray[1])) {
                        return this.d(byArray[0], (byte)3);
                    }
                    if (this.d(byArray[4])) {
                        return this.d(byArray[1], (byte)1);
                    }
                    if (this.d(byArray[3])) {
                        return this.d(byArray[0], (byte)3);
                    }
                } else {
                    if (!this.d(byArray[0])) {
                        this.Z = this.d[1];
                        return this.d(byArray[1], (byte)1);
                    }
                    if (!this.d(byArray[1])) {
                        this.Z = this.d[1];
                        return this.d(byArray[0], (byte)3);
                    }
                    return this.d(byArray[2], (byte)0);
                }
            }
            case 3: {
                if (j.a().c(n2 - this.Z, this.j)) {
                    return false;
                }
                byArray[0] = j.a().b(n2 - this.Z, n3);
                byArray[1] = j.a().b(n2 - this.Z, n5);
                byArray[2] = j.a().b(n2 - this.Z, this.j);
                if (!this.d(byArray[0]) && !this.d(byArray[1])) {
                    if (j.a().b(this.i, this.j) == 3) {
                        return false;
                    }
                    byArray[0] = j.a().b(n2 - this.Z, n3 - 16);
                    byArray[1] = j.a().b(n2 - this.Z, n5 + 16);
                    byArray[3] = j.a().b(this.i, this.j - 16);
                    byArray[4] = j.a().b(this.i, this.j + 16);
                    this.Z = this.d[1];
                    if (!this.d(byArray[0])) {
                        return this.d(byArray[1], (byte)0);
                    }
                    if (!this.d(byArray[1])) {
                        return this.d(byArray[0], (byte)2);
                    }
                    if (this.d(byArray[4])) {
                        return this.d(byArray[1], (byte)0);
                    }
                    if (this.d(byArray[3])) {
                        return this.d(byArray[0], (byte)2);
                    }
                } else {
                    if (!this.d(byArray[0])) {
                        this.Z = this.d[1];
                        return this.d(byArray[1], (byte)0);
                    }
                    if (!this.d(byArray[1])) {
                        this.Z = this.d[1];
                        return this.d(byArray[0], (byte)2);
                    }
                    return this.d(byArray[2], (byte)3);
                }
            }
            case 1: {
                if (j.a().c(n4 + this.Z, this.j)) {
                    return false;
                }
                byArray[0] = j.a().b(n4 + this.Z, n3);
                byArray[1] = j.a().b(n4 + this.Z, n5);
                byArray[2] = j.a().b(n4 + this.Z, this.j);
                if (!this.d(byArray[0]) && !this.d(byArray[1])) {
                    if (j.a().b(this.i, this.j) == 3) {
                        return false;
                    }
                    byArray[0] = j.a().b(n4 + this.Z, n3 - 16);
                    byArray[1] = j.a().b(n4 + this.Z, n5 + 16);
                    byArray[3] = j.a().b(this.i, this.j - 16);
                    byArray[4] = j.a().b(this.i, this.j + 16);
                    this.Z = this.d[1];
                    if (!this.d(byArray[0])) {
                        return this.d(byArray[1], (byte)0);
                    }
                    if (!this.d(byArray[1])) {
                        return this.d(byArray[0], (byte)2);
                    }
                    if (this.d(byArray[4])) {
                        return this.d(byArray[1], (byte)0);
                    }
                    if (!this.d(byArray[3])) break;
                    return this.d(byArray[0], (byte)2);
                }
                if (!this.d(byArray[0])) {
                    this.Z = this.d[1];
                    return this.d(byArray[1], (byte)0);
                }
                if (!this.d(byArray[1])) {
                    this.Z = this.d[1];
                    return this.d(byArray[0], (byte)2);
                }
                return this.d(byArray[2], (byte)1);
            }
        }
        return true;
    }

    private boolean d(byte by) {
        switch (by) {
            case 1: {
                return false;
            }
            case 2: {
                return this.B[3][0] == 2;
            }
        }
        return true;
    }

    public final boolean a(a a2, short[] sArray, short[] sArray2) {
        if (sArray2 == null) {
            return false;
        }
        switch (this.n) {
            case 3: {
                if (a2.v == 14) {
                    return game.g.a(a2, sArray2, sArray, this.i - this.d[0], this.j);
                }
                if (!ae.a(this.i - this.d[0], this.j, a2.i, a2.j, sArray, sArray2)) break;
                return true;
            }
            case 1: {
                if (a2.v == 14) {
                    return game.g.a(a2, sArray2, sArray, this.i + this.d[0], this.j);
                }
                if (!ae.a(this.i + this.d[0], this.j, a2.i, a2.j, sArray, sArray2)) break;
                return true;
            }
            case 2: {
                if (a2.v == 14) {
                    return game.g.a(a2, sArray2, sArray, this.i, this.j - this.d[0]);
                }
                if (!ae.a(this.i, this.j - this.d[0], a2.i, a2.j, sArray, sArray2)) break;
                return true;
            }
            case 0: {
                if (a2.v == 14) {
                    return game.g.a(a2, sArray2, sArray, this.i, this.j + this.d[0]);
                }
                if (!ae.a(this.i, this.j + this.d[0], a2.i, a2.j, sArray, sArray2)) break;
                return true;
            }
        }
        return false;
    }

    private static boolean a(a a2, short[] sArray, short[] sArray2, int n2, int n3) {
        switch (a2.a.g()) {
            case 1: {
                if (!ae.a(a2.i + sArray[0], a2.j + sArray[1], sArray[2] + (a2.A << 4), (int)sArray[3], n2, n3, sArray2)) break;
                return true;
            }
            case 3: {
                if (!ae.a(a2.i + sArray[0] - (a2.A << 4), a2.j + sArray[1], sArray[2] + (a2.A << 4), (int)sArray[3], n2, n3, sArray2)) break;
                return true;
            }
            case 2: {
                if (!ae.a(a2.i + sArray[0], a2.j + sArray[1] - (a2.A << 4), (int)sArray[2], sArray[3] + (a2.A << 4), n2, n3, sArray2)) break;
                return true;
            }
            case 0: {
                if (!ae.a(a2.i + sArray[0], a2.j + sArray[1], (int)sArray[2], sArray[3] + (a2.A << 4), n2, n3, sArray2)) break;
                return true;
            }
        }
        return false;
    }

    private boolean N() {
        boolean bl = true;
        switch (this.n) {
            case 3: {
                bl = this.d(j.a().b(this.i - this.d[0], this.j));
                break;
            }
            case 1: {
                bl = this.d(j.a().b(this.i + this.d[0], this.j));
                break;
            }
            case 2: {
                bl = this.d(j.a().b(this.i, this.j - this.d[0]));
                break;
            }
            case 0: {
                bl = this.d(j.a().b(this.i, this.j + this.d[0]));
            }
        }
        if (bl && j.a().b(this.i, this.j) != 2) {
            return false;
        }
        return bl;
    }

    private boolean d(byte by, byte by2) {
        switch (by) {
            case -1: 
            case 0: {
                this.b((byte)1, by2);
                break;
            }
            case 1: {
                if (j.a().b(this.i, this.j) == 2) {
                    this.b((byte)2, by2);
                }
                return false;
            }
            case 2: {
                if (this.d((byte)2)) {
                    this.b((byte)2, by2);
                    break;
                }
                return false;
            }
            case 3: {
                this.b((byte)6, by2);
            }
        }
        return true;
    }

    public final void v() {
        if (((a)this.p).h() == 1) {
            game.k.a().S.aw();
            game.k.a().S.b("B\u1ea3o r\u01b0\u01a1ng n\u00e0y \u0111\u00e3 tr\u1ed1ng");
            return;
        }
        if (((a)this.p).v == 0) {
            ((a)this.p).d((byte)1);
            if (this.a((int)((a)this.p).F, (int)((a)this.p).D, (byte)((a)this.p).E)) {
                this.c(((a)this.p).F, ((a)this.p).D, (byte)((a)this.p).E);
                String string = null;
                if (((a)this.p).E == 0) {
                    string = an.f(aq.c[4][((a)this.p).F][0]);
                } else if (((a)this.p).E == 2) {
                    string = an.f(aq.c[3][((a)this.p).F][0]);
                }
                game.k.a().S.a("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + string, (int)((a)this.p).D);
            } else {
                game.k.a().S.av();
            }
            this.b((byte)0, this.n);
            return;
        }
        if (((a)this.p).v == 1) {
            if (this.b(17, 1, (byte)2)) {
                ((a)this.p).d((byte)1);
                this.d(17, 1, (byte)2);
                if (this.a((int)((a)this.p).F, (int)((a)this.p).D, (byte)((a)this.p).E)) {
                    this.c(((a)this.p).F, ((a)this.p).D, (byte)((a)this.p).E);
                    String string = null;
                    if (((a)this.p).E == 0) {
                        string = an.f(aq.c[4][((a)this.p).F][0]);
                    } else if (((a)this.p).E == 2) {
                        string = an.f(aq.c[3][((a)this.p).F][0]);
                    }
                    game.k.a().S.a("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + string, (int)((a)this.p).D);
                } else {
                    game.k.a().S.av();
                }
            } else {
                game.k.a().S.au();
            }
            this.b((byte)0, this.n);
        }
    }

    public final boolean w() {
        if (this.p == null) {
            return false;
        }
        if (((a)this.p).h() == 0) {
            game.c.h = true;
            if (((a)this.p).v == 7 || ((a)this.p).v == 6) {
                for (int i2 = 0; i2 < this.aj.size(); ++i2) {
                    a a2 = (a)this.aj.elementAt(i2);
                    a2.d((byte)1);
                    a2.x();
                }
                this.aj.removeAllElements();
            } else if (((a)this.p).v != 16) {
                ((a)this.p).d((byte)1);
            }
            return true;
        }
        return false;
    }

    private static boolean a(int n2, int n3, Vector vector) {
        for (int i2 = 0; i2 < vector.size(); ++i2) {
            int[] nArray = (int[])vector.elementAt(i2);
            if (nArray[0] != n2) continue;
            return nArray[1] < 99;
        }
        return n3 <= 99;
    }

    private static boolean b(int n2, int n3, Vector vector) {
        for (int i2 = 0; i2 < vector.size(); ++i2) {
            int[] nArray = (int[])vector.elementAt(i2);
            if (nArray[0] != n2) continue;
            return nArray[1] - n3 >= 0;
        }
        return false;
    }

    private static boolean c(int n2, int n3, Vector vector) {
        int[] nArray;
        for (int i2 = 0; i2 < vector.size(); ++i2) {
            nArray = (int[])vector.elementAt(i2);
            if (nArray[0] != n2) continue;
            nArray[1] = nArray[1] + n3;
            if (nArray[1] >= 99) {
                nArray[1] = 99;
            }
            return true;
        }
        nArray = new int[]{n2, n3, 0};
        vector.addElement(nArray);
        return false;
    }

    private static boolean d(int n2, int n3, Vector vector) {
        for (int i2 = 0; i2 < vector.size(); ++i2) {
            int[] nArray = (int[])vector.elementAt(i2);
            if (nArray[0] != n2) continue;
            nArray[1] = nArray[1] - n3;
            if (nArray[1] <= 0 && nArray[2] == 0) {
                vector.removeElementAt(i2);
            }
            return true;
        }
        return false;
    }

    public final boolean a(int n2, int n3, byte by) {
        switch (by) {
            case 0: {
                if (aq.c[4][n2][5] == 0) {
                    return game.g.a(n2, n3, this.K);
                }
                return game.g.a(n2, n3, this.J);
            }
            case 2: {
                if (n2 >= 12) {
                    return game.g.a(n2, n3, this.M);
                }
                n3 = n2;
                g g2 = this;
                for (by = 0; by < g2.L.size(); by = (byte)(by + 1)) {
                    if (((int[])g2.L.elementAt(by))[0] != n3) continue;
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public final boolean b(int n2, int n3, byte by) {
        switch (by) {
            case 0: {
                if (aq.c[4][n2][5] == 0) {
                    return game.g.b(n2, n3, this.K);
                }
                return game.g.b(n2, n3, this.J);
            }
            case 2: {
                return game.g.b(n2, n3, this.M);
            }
        }
        return false;
    }

    public final boolean c(int n2, int n3, byte by) {
        switch (by) {
            case 0: {
                if (aq.c[4][n2][5] == 0) {
                    return game.g.c(n2, n3, this.K);
                }
                return game.g.c(n2, n3, this.J);
            }
            case 2: {
                if (n2 >= 12) {
                    if (n2 == 17) {
                        return game.g.c(n2, n3 * 5, this.M);
                    }
                    return game.g.c(n2, n3, this.M);
                }
                n3 = n2;
                g g2 = this;
                int[] nArray = new int[]{n3, 0, 0};
                g2.L.addElement(nArray);
                return true;
            }
        }
        return false;
    }

    public final boolean d(int n2, int n3, byte by) {
        switch (by) {
            case 0: {
                if (aq.c[4][n2][5] == 0) {
                    return game.g.d(n2, n3, this.K);
                }
                return game.g.d(n2, n3, this.J);
            }
            case 2: {
                return game.g.d(n2, n3, this.M);
            }
        }
        return false;
    }

    public final int a(int n2, byte by) {
        switch (by) {
            case 0: {
                if (aq.c[4][n2][5] == 0) {
                    for (int i2 = 0; i2 < this.K.size(); ++i2) {
                        int[] nArray = (int[])this.K.elementAt(i2);
                        if (nArray[0] != n2) continue;
                        return nArray[1];
                    }
                } else {
                    for (int i3 = 0; i3 < this.J.size(); ++i3) {
                        int[] nArray = (int[])this.J.elementAt(i3);
                        if (nArray[0] != n2) continue;
                        return nArray[1];
                    }
                }
                break;
            }
            case 2: {
                for (int i4 = 0; i4 < this.M.size(); ++i4) {
                    int[] nArray = (int[])this.M.elementAt(i4);
                    if (nArray[0] != n2) continue;
                    return nArray[1];
                }
                break;
            }
        }
        return 0;
    }

    public final void x() {
        int[] nArray;
        int n2;
        if (this.S == null) {
            this.S = new Vector();
        } else {
            this.S.removeAllElements();
        }
        for (n2 = 0; n2 < this.K.size(); ++n2) {
            nArray = (int[])this.K.elementAt(n2);
            if (aq.c[4][nArray[0]][4] != 0) continue;
            this.S.addElement(nArray);
        }
        for (n2 = 0; n2 < this.J.size(); ++n2) {
            nArray = (int[])this.J.elementAt(n2);
            if (aq.c[4][nArray[0]][4] != 0) continue;
            this.S.addElement(nArray);
        }
    }

    public final boolean i(int n2) {
        int[] nArray;
        if (n2 == 0) {
            nArray = new int[]{n2, 0, 0};
        } else {
            nArray = new int[]{n2, 1, 0};
            if (n2 == 1 || n2 == 2 || n2 == 3 || n2 == 4) {
                this.P[n2 - 1] = 1;
            }
        }
        this.N.addElement(nArray);
        return true;
    }

    public final void c(int n2, int n3) {
        int[] nArray;
        for (int i2 = 0; i2 < this.N.size(); ++i2) {
            nArray = (int[])this.N.elementAt(i2);
            if (nArray[0] != n2) continue;
            nArray[2] = nArray[2] + n3;
            if (nArray[2] >= 99) {
                nArray[2] = 99;
            }
            return;
        }
        nArray = new int[]{n2, 0, n3};
        this.N.addElement(nArray);
    }

    public final int d(int n2, int n3) {
        if (0 < this.N.size()) {
            int[] nArray = (int[])this.N.elementAt(0);
            if (nArray[0] == n2 && (n2 == 7 || n2 == 9 || n2 == 8)) {
                if (nArray[1] <= 99) {
                    return 0;
                }
                return 1;
            }
            return -1;
        }
        if (n3 > 99) {
            return 1;
        }
        return -1;
    }

    public final boolean e(int n2, int n3) {
        for (int i2 = 0; i2 < this.N.size(); ++i2) {
            int[] nArray = (int[])this.N.elementAt(i2);
            if (nArray[0] == n2 && (n2 == 7 || n2 == 9 || n2 == 8)) {
                this.z[n3].i((byte)n2);
                if (nArray[2] > 0) {
                    nArray[2] = nArray[2] - 1;
                    this.N.setElementAt(new int[]{nArray[0], 0, nArray[2]}, i2);
                } else if (nArray[2] <= 0) {
                    this.N.removeElementAt(i2);
                }
                return true;
            }
            if (nArray[0] != n2 || nArray[1] != 0) continue;
            this.N.setElementAt(new int[]{nArray[0], 1, nArray[2]}, i2);
            break;
        }
        return false;
    }

    public final void j(int n2) {
        for (int i2 = 0; i2 < this.N.size(); ++i2) {
            int[] nArray = (int[])this.N.elementAt(i2);
            if (nArray[0] != n2 || nArray[1] != 1) continue;
            this.N.setElementAt(new int[]{nArray[0], 0, nArray[2]}, i2);
            return;
        }
    }

    public final boolean k(int n2) {
        for (int i2 = 0; i2 < this.N.size(); ++i2) {
            int[] nArray = (int[])this.N.elementAt(i2);
            if (nArray[0] != n2 || nArray[1] != 1) continue;
            return true;
        }
        return false;
    }

    public final boolean l(int n2) {
        for (int i2 = 0; i2 < this.L.size(); ++i2) {
            int[] nArray = (int[])this.L.elementAt(i2);
            if (nArray[0] != n2) continue;
            nArray[1] = 0;
            return true;
        }
        return false;
    }

    public final void f(int n2, int n3) {
        short s2;
        boolean bl;
        int[] nArray;
        g g2;
        int n4;
        block7: {
            if (this.z[n3].c[5] >= 0) {
                this.l(this.z[n3].c[5]);
                this.z[n3].c[5] = -1;
            }
            n4 = 0;
            g2 = this;
            for (n4 = 0; n4 < g2.L.size(); ++n4) {
                nArray = (int[])g2.L.elementAt(n4);
                if (nArray[0] != n2 || nArray[1] != 1) continue;
                bl = true;
                break block7;
            }
            bl = false;
        }
        if (bl) {
            this.l(n2);
            boolean bl2 = false;
            for (n4 = 0; n4 < this.A; ++n4) {
                if (this.z[n4].c[5] != n2) continue;
                this.z[n4].c[5] = -1;
                bl2 = true;
                break;
            }
            if (!bl2) {
                for (n4 = 0; n4 < this.O.size(); ++n4) {
                    int[] nArray2 = (int[])this.O.elementAt(n3);
                    if (nArray2[2] != n2) continue;
                    nArray2[2] = -1;
                    break;
                }
            }
        }
        g2 = this;
        for (n4 = 0; n4 < g2.L.size(); ++n4) {
            nArray = (int[])g2.L.elementAt(n4);
            if (nArray[0] != n2) continue;
            nArray[1] = 1;
            break;
        }
        this.z[n3].c[5] = s2 = (short)n2;
    }

    public final byte y() {
        if (this.A < 6) {
            return 0;
        }
        if (this.O.size() < 100) {
            return 1;
        }
        return 2;
    }

    public final boolean z() {
        return this.O.size() < 100;
    }

    public final void a(int n2, int n3, byte by, short s2, int[] nArray) {
        this.z[this.A] = new game.b();
        this.z[this.A].a(n2, n3, (short)-1, by, s2, (byte)-1);
        this.z[this.A].b(nArray);
        this.a((byte)this.z[this.A].j((byte)1), n2, (byte)2);
        ++this.A;
    }

    public final void a(int n2, int n3, int n4, byte by, short s2, int[] nArray) {
        this.z[this.A] = new game.b();
        System.arraycopy(this.z, n2, this.z, n2 + 1, this.A - n2);
        this.z[n2] = null;
        this.z[n2] = new game.b();
        this.z[n2].a(n3, n4, (short)-1, by, s2, (byte)-1);
        this.z[n2].b(nArray);
        ++this.A;
    }

    public final void a(int[] nArray) {
        this.z[this.A] = new game.b();
        this.z[this.A].a(nArray[0], nArray[1], (short)nArray[2], (byte)nArray[3], (short)nArray[4], (byte)nArray[5]);
        this.z[this.A].a((short)nArray[6], nArray[7], nArray[8]);
        int[] nArray2 = new int[nArray.length - 9];
        for (int i2 = 0; i2 < nArray2.length; ++i2) {
            nArray2[i2] = nArray[i2 + 9];
        }
        this.z[this.A].b(nArray2);
        this.a((byte)this.z[this.A].j((byte)1), nArray[0], (byte)2);
        ++this.A;
    }

    public final void m(int n2) {
        this.z[n2] = null;
        while (n2 < this.A - 1) {
            this.z[n2] = this.z[n2 + 1];
            this.z[n2 + 1] = null;
            ++n2;
        }
        --this.A;
    }

    public final void n(int n2) {
        for (int i2 = 0; i2 < this.A; ++i2) {
            if (this.z[i2].q() != n2) continue;
            this.m(i2);
            return;
        }
    }

    public final boolean o(int n2) {
        int n3 = 0;
        for (int i2 = 0; i2 < this.A; ++i2) {
            if (i2 == n2 || !this.z[i2].S()) continue;
            ++n3;
        }
        return n3 > 0;
    }

    public final int A() {
        int n2;
        int[] nArray = new int[this.A];
        for (n2 = 0; n2 < this.A; ++n2) {
            nArray[n2] = this.z[n2].c[1] - this.z[n2].d[1];
        }
        n2 = nArray[0];
        for (int i2 = 1; i2 < nArray.length; ++i2) {
            if (n2 >= nArray[i2]) continue;
            n2 = nArray[i2];
        }
        if (n2 == 0) {
            return -1;
        }
        return n2;
    }

    public final void p(int n2) {
        game.b b2 = this.z[n2];
        while (n2 > 0) {
            this.z[n2] = this.z[n2 - 1];
            --n2;
        }
        this.z[0] = b2;
    }

    public final void a(int n2, int n3, byte by, short s2, int n4, int n5, int[] nArray) {
        int[] nArray2 = new int[9 + nArray.length];
        int[] nArray3 = nArray2;
        nArray2[0] = n2;
        nArray3[1] = n3;
        nArray3[2] = -1;
        nArray3[3] = by;
        nArray3[4] = s2;
        nArray3[5] = -1;
        nArray3[6] = n4;
        nArray3[7] = 0;
        nArray3[8] = n5;
        System.arraycopy(nArray, 0, nArray3, 9, nArray.length);
        this.O.addElement(nArray3);
        this.a((byte)aq.c[0][n2][1], n2, (byte)2);
    }

    public final void b(int[] nArray) {
        this.O.addElement(nArray);
        this.a((byte)aq.c[0][nArray[0]][1], nArray[0], (byte)2);
    }

    public final void q(int n2) {
        this.O.removeElementAt(n2);
    }

    public final void r(int n2) {
        Object object = this;
        game.b b2 = new game.b();
        object = (int[])((g)object).O.elementAt(n2);
        b2.a((int)object[0], (int)object[1], (short)object[2], (byte)object[3], (short)object[4], (byte)object[5]);
        b2.a((short)object[6], (int)object[7], (int)object[8]);
        int[] nArray = new int[((Object)object).length - 9];
        for (int i2 = 0; i2 < nArray.length; ++i2) {
            nArray[i2] = (int)object[i2 + 9];
        }
        b2.b(nArray);
        this.z[this.A] = b2;
        ++this.A;
        this.q(n2);
    }

    public final void a(short s2) {
        this.R[this.I] = s2;
        this.I = (byte)(this.I + 1);
    }

    public final void a(byte by, byte by2, byte by3) {
        this.B[by][by2] = by3;
        if (this.B[0][0] == 2) {
            b.a().a(aq.a((byte)2, (short)0, (byte)5) / 2, aq.a((byte)2, (short)0, (byte)5) / 2);
        }
    }

    public final byte c(byte by, byte by2) {
        return this.B[by][by2];
    }

    public final void a(byte by, int n2, byte by2) {
        boolean bl;
        block11: {
            g g2 = this;
            for (int i2 = 0; i2 < g2.E[by]; ++i2) {
                if (g2.D[by][i2] != n2) continue;
                bl = false;
                break block11;
            }
            bl = true;
        }
        if (bl) {
            this.D[by][this.E[by]] = (byte)n2;
            byte by3 = by;
            this.E[by3] = (byte)(this.E[by3] + 1);
            if (by2 == 2) {
                this.F = (byte)(this.F + 1);
                if (aq.c[0][n2][22] == 2) {
                    this.G = (byte)(this.G + 1);
                } else if (aq.c[0][n2][22] == 1) {
                    this.H = (byte)(this.H + 1);
                }
            }
            this.C[by][n2 - this.W[by]] = by2;
            return;
        }
        if (this.a(by, n2) <= 1) {
            if (by2 == 2) {
                this.F = (byte)(this.F + 1);
                if (aq.c[0][n2][22] == 2) {
                    this.G = (byte)(this.G + 1);
                } else if (aq.c[0][n2][22] == 1) {
                    this.H = (byte)(this.H + 1);
                }
            }
            this.C[by][n2 - this.W[by]] = by2;
        }
    }

    public final byte a(byte by, int n2) {
        return this.C[by][n2 - this.W[by]];
    }

    public final void B() {
        this.ac = true;
    }

    public final int C() {
        if (game.k.a().f == 4 && game.k.a().g == 1) {
            return ae.b(4, 8);
        }
        return ae.b(this.ad, this.ae);
    }

    private void O() {
        if (game.k.a().M.h() || game.c.t == 0) {
            return;
        }
        --this.w;
        if (this.w <= 0) {
            game.k.a().q();
            this.w = 0;
        }
        --this.x;
        if (this.x == 0) {
            this.a.a(0);
            this.x = -1;
        }
        if (!game.k.a().c() || this.x > 0) {
            return;
        }
        if (this.ac && this.v > 0 && this.P[1] != 2 && this.P[3] != 2) {
            --this.v;
        }
    }

    public final boolean D() {
        return this.v <= 0;
    }

    public final int E() {
        return this.af;
    }

    public final void s(int n2) {
        this.af += n2;
    }

    public final void F() {
        this.af = 0;
    }

    public final boolean t(int n2) {
        return this.af >= n2;
    }

    public final int G() {
        return this.ag;
    }

    public final void u(int n2) {
        this.ag += n2;
    }

    public final void H() {
        this.ag = 0;
    }

    public final boolean v(int n2) {
        return this.ag >= n2;
    }

    public final boolean b(int n2, int n3, int n4) {
        if (aq.c[n4][n2][4] == 0) {
            return this.t(n3);
        }
        return this.v(n3);
    }

    public final void I() {
        this.a(68, 7, (byte)2, (short)2, new int[]{1, 40, 45});
        this.i(0);
    }

    public final boolean a(int n2, boolean bl) {
        super.a(n2, bl);
        if (this.x > 0) {
            this.a.a(1);
        }
        return true;
    }

    public final void J() {
        this.b((byte)0, this.n);
    }

    static {
        ah = new byte[][]{{9, 2, 9, 3, 0}};
        ai = new short[][]{{0, 3, 112, 256}};
    }
}

