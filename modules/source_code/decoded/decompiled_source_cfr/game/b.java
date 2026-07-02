/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Graphics
 */
package game;

import game.g;
import game.k;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public final class b
extends f {
    private static short[] M = new short[]{90, 95, 100, 110, 125};
    public static final byte[] t = new byte[]{12, 30, 5};
    ah u;
    short[][] v;
    short[][] w;
    byte[][] x;
    private byte[] N;
    short[] y;
    byte[] z;
    private byte O;
    private short[] P;
    protected int A = 0;
    private int Q;
    private int R = 0;
    private int S = 0;
    protected int B = 0;
    private int T = 0;
    protected short C;
    private byte U = 0;
    private int V = 0;
    private byte W;
    protected byte D;
    private int X = 0;
    private boolean Y;
    protected short E;
    protected byte F;
    protected Vector G = new Vector();
    protected Vector H = new Vector();
    public byte I = 0;
    protected boolean J;
    protected short[] K;
    protected ah L = null;
    private byte Z = 0;

    public b() {
        this.c = new short[23];
        this.d = new short[23];
        this.y = new short[5];
        this.z = new byte[5];
        this.P = new short[4];
        for (int i2 = 0; i2 < this.z.length; ++i2) {
            this.z[i2] = -1;
        }
        this.K = new short[16];
        this.v = new short[16][5];
        this.w = new short[11][5];
        this.x = new byte[][]{{-1, -1, -1}, {-1, -1, -1}};
        this.N = new byte[2];
    }

    public final void a(int n2, int n3, short s2, byte by, short s3, byte by2) {
        this.V = n2;
        this.T = n3;
        this.c[0] = s3 == -1 ? (n2 = (int)((short)ae.b(aq.c[0][this.V][3], (int)aq.c[0][this.V][3]))) : (n2 = (int)s3);
        this.c[1] = n2 = (int)((short)((aq.c[0][this.V][5] + aq.c[0][this.V][6] * n3 + aq.c[0][this.V][7]) * M[this.c[0] - 1] / 100));
        this.c[2] = n2 = (int)((short)((aq.c[0][this.V][8] + aq.c[0][this.V][9] * n3 + aq.c[0][this.V][10]) * M[this.c[0] - 1] / 100));
        this.c[3] = n2 = (int)((short)((aq.c[0][this.V][11] + aq.c[0][this.V][12] * n3 / 10 + aq.c[0][this.V][13]) * M[this.c[0] - 1] / 100));
        this.c[4] = n2 = (int)((short)((aq.c[0][this.V][14] + aq.c[0][this.V][15] * n3 / 10 + aq.c[0][this.V][16]) * M[this.c[0] - 1] / 100));
        this.c[5] = s2;
        this.c[6] = n2 = (int)by;
        this.i(by2);
        this.g();
        this.C = aq.c[0][this.V][17];
        b b2 = this;
        b2.u(b2.d[1]);
    }

    public final void a(int[] nArray) {
        this.a(nArray[0], nArray[1], (short)nArray[2], (byte)nArray[3], (short)nArray[4], (byte)nArray[5]);
        this.U();
        this.a((short)nArray[6], nArray[7], nArray[8]);
        int[] nArray2 = new int[nArray.length - 9];
        for (int i2 = 0; i2 < nArray2.length; ++i2) {
            nArray2[i2] = nArray[i2 + 9];
        }
        this.b(nArray2);
    }

    public final void a(short s2, int n2, int n3) {
        this.U();
        this.d[1] = s2;
        this.u(this.d[1]);
        this.S = n2;
        this.E = (byte)n3;
    }

    public final void c() {
        super.c();
        if (this.a == null) {
            this.a = new d();
        }
        this.a.a((int)this.C, false);
        this.d((byte)0);
    }

    public final void d() {
        super.d();
        if (this.a != null) {
            this.a.a();
            this.a = null;
        }
    }

    public final void a(short s2, byte by) {
        byte by2 = this.n;
        this.u = null;
        this.u = new ah();
        this.u.a(new short[]{s2, by, by2});
        this.u.b(this.i, this.j);
        if (s2 == 20 && by == 3 || s2 == 22 && by == 4) {
            int[] nArray = this.a.b(0, by2);
            this.u.b(this.i, this.j - nArray[3]);
        }
        this.u.c(true);
    }

    private void z(int n2) {
        this.L = new ah();
        short[] sArray = game.d.m[n2];
        short[] sArray2 = new short[sArray.length + 5];
        System.arraycopy(sArray, 1, sArray2, 6, sArray.length - 1);
        sArray = new short[]{sArray[0], (short)this.i, (short)this.j, aq.c[0][this.V][17], 0, this.n};
        System.arraycopy(sArray, 0, sArray2, 0, sArray.length);
        this.L.a(sArray2);
        this.L.c(true);
    }

    public final void d(byte by) {
        switch (by) {
            case 0: {
                this.a.a(by, (byte)-1, true);
                break;
            }
            case 1: {
                this.a.a(by, (byte)0, true);
                switch (this.V) {
                    case 0: {
                        this.z(27);
                        break;
                    }
                    case 10: {
                        this.Z = 1;
                        this.z(28);
                        break;
                    }
                    case 91: {
                        this.z(26);
                        break;
                    }
                    case 92: {
                        this.z(25);
                        break;
                    }
                    case 97: 
                    case 98: {
                        this.z(23);
                        break;
                    }
                    case 62: {
                        this.z(24);
                        break;
                    }
                    case 75: {
                        this.z(20);
                        break;
                    }
                    case 87: {
                        this.z(21);
                    }
                }
                break;
            }
            case 2: {
                this.a.a(by, (byte)0, true);
                break;
            }
            case 3: {
                if (game.d.a().b != 0) break;
                this.d();
                b b2 = this;
                short[] sArray = new short[]{16, 0, 0, 4};
                b2.L = new ah();
                short[] sArray2 = new short[sArray.length + 5];
                System.arraycopy(sArray, 1, sArray2, 6, sArray.length - 1);
                sArray = new short[]{sArray[0], (short)b2.i, (short)b2.j, aq.c[0][b2.V][17], 0, b2.n};
                System.arraycopy(sArray, 0, sArray2, 0, sArray.length);
                b2.L.a(sArray2);
                b2.L.c(true);
                b2.L.a();
                break;
            }
            case 4: {
                this.a.a(by, (byte)-1, true);
            }
        }
        this.U = by;
    }

    public final void o() {
        this.a();
        if (this.u != null) {
            this.u.e();
        }
        if (this.L != null) {
            this.L.e();
        }
    }

    public final void a(Graphics graphics) {
        if (this.L != null && this.U == 1) {
            switch (this.V) {
                case 0: {
                    if (!this.a.b(1)) break;
                    this.L.a();
                    break;
                }
                case 10: {
                    if (!this.a.b(1)) break;
                    this.L.a();
                    break;
                }
                case 91: {
                    if (!this.a.b(1)) break;
                    this.L.a();
                    break;
                }
                case 92: {
                    if (!this.a.b(1)) break;
                    this.L.a();
                    break;
                }
                case 97: 
                case 98: {
                    if (!this.a.b(1)) break;
                    this.L.a();
                    break;
                }
                case 62: {
                    if (!this.a.b(1)) break;
                    this.L.a();
                    break;
                }
                case 75: {
                    if (!this.a.b(1)) break;
                    this.L.a();
                    break;
                }
                case 87: {
                    if (!this.a.b(1)) break;
                    this.L.a();
                }
            }
        }
        if (this.L != null && this.Z == 0) {
            this.L.a(graphics);
        }
        Graphics graphics2 = graphics;
        b b2 = this;
        if (b2.f) {
            b2.a.a(graphics2, b2.i, b2.j, b2.n);
        }
        if (this.L != null && this.Z == 1) {
            this.L.a(graphics);
        }
        if (this.u != null) {
            this.u.a(graphics);
        }
    }

    public final byte p() {
        return this.U;
    }

    public final int q() {
        return this.V;
    }

    public final void f(int n2) {
        this.X = n2;
    }

    public final int r() {
        return this.X;
    }

    public final int s() {
        return this.T;
    }

    public final boolean t() {
        return this.T == 50;
    }

    protected final void g(int n2) {
        if (this.T >= 50) {
            return;
        }
        this.S += n2;
        if (this.S < 0) {
            this.S = 0;
        }
    }

    public final int u() {
        if (this.T >= 50) {
            return game.b.A(50);
        }
        return game.b.A(this.T + 1);
    }

    public final void v() {
        ++this.T;
        this.g(-game.b.A(this.T));
        this.J();
        for (int i2 = 0; i2 < this.z.length; ++i2) {
            if (this.z[i2] == -1) continue;
            this.y[i2] = aq.c[1][this.z[i2]][5];
        }
        this.V();
    }

    public final void h(int n2) {
        this.T += n2;
        this.J();
        this.V();
    }

    private static int A(int n2) {
        return n2 * 15 * n2 - 200;
    }

    private void U() {
        short s2;
        if (game.g.o().c((byte)1, (byte)0) == 2 && game.g.o().c((byte)1, (byte)1) == 1) {
            s2 = (short)(this.c[1] * aq.c[2][1][6] / 100);
            this.c[1] = s2 = (short)(this.c[1] + s2);
        }
        if (game.g.o().c((byte)2, (byte)0) == 2 && game.g.o().c((byte)2, (byte)1) == 1) {
            s2 = (short)(this.c[3] * aq.c[2][2][6] / 100);
            this.c[3] = s2 = (short)(this.c[3] + s2);
        }
    }

    private void V() {
        short s2;
        this.c[1] = s2 = (short)((aq.c[0][this.V][5] + aq.c[0][this.V][6] * this.T + aq.c[0][this.V][7]) * M[this.c[0] - 1] / 100);
        this.c[2] = s2 = (short)((aq.c[0][this.V][8] + aq.c[0][this.V][9] * this.T + aq.c[0][this.V][10]) * M[this.c[0] - 1] / 100);
        this.c[3] = s2 = (short)((aq.c[0][this.V][11] + aq.c[0][this.V][12] * this.T / 10 + aq.c[0][this.V][13]) * M[this.c[0] - 1] / 100);
        this.c[4] = s2 = (short)((aq.c[0][this.V][14] + aq.c[0][this.V][15] * this.T / 10 + aq.c[0][this.V][16]) * M[this.c[0] - 1] / 100);
        this.g();
        this.u(this.d[1]);
    }

    public static short b(int n2, int n3, int n4) {
        return (short)((aq.c[0][n2][5] + aq.c[0][n2][6] * n3 + aq.c[0][n2][7]) * M[n4 - 1] / 100);
    }

    public final void w() {
        for (int i2 = 0; i2 < this.P.length; ++i2) {
            this.P[i2] = game.b.a(this.V, this.T - 5, this.c[0], i2 + 1);
        }
    }

    public final void x() {
        for (int i2 = 0; i2 < this.P.length; ++i2) {
            byte by = (byte)(i2 + 1);
            this.P[i2] = this.c[by];
        }
    }

    public final short i(int n2) {
        return this.P[n2];
    }

    public final void y() {
        if (this.F < 20) {
            short s2;
            this.F = (byte)(this.F + 1);
            this.d[2] = s2 = (short)(this.c[2] + this.c[2] * this.F / 100);
            this.d[3] = s2 = (short)(this.c[3] + this.c[3] * this.F / 100);
            this.d[4] = s2 = (short)(this.c[4] + this.c[4] * this.F / 100);
        }
    }

    public final int z() {
        return this.S;
    }

    public final void j(int n2) {
        this.R = n2;
    }

    public final int A() {
        return this.R;
    }

    public final int B() {
        int n2;
        if (((b)this.p).X == 0 && game.g.o().c((byte)4, (byte)0) == 2) {
            this.p.d[3] = n2 = (int)((short)(this.p.c[3] * (100 + aq.c[2][4][5]) / 100));
        }
        n2 = ((b)this.p).f((byte)2) ? this.d[2] - this.p.d[3] * (100 + aq.c[3][2][5]) / 100 : this.d[2] - this.p.d[3];
        if (this.f((byte)0)) {
            if (this.d[1] <= aq.c[3][0][5] * this.c[1] / 100) {
                n2 = this.d[2] * (100 + aq.c[3][0][6]) / 100 - this.p.d[3];
            }
        } else if (this.f((byte)1)) {
            n2 = this.d[2] * (100 + aq.c[3][1][5]) / 100 - this.p.d[3];
        }
        return n2;
    }

    public final int e(byte by) {
        int n2 = this.c[by];
        switch (by) {
            case 2: {
                if (this.f((byte)0)) {
                    if (this.d[1] > aq.c[3][0][5] * this.c[1] / 100) break;
                    n2 = this.d[2] * (100 + aq.c[3][0][6]) / 100;
                    break;
                }
                if (!this.f((byte)1)) break;
                n2 = this.c[2] * (100 + aq.c[3][1][5]) / 100;
                break;
            }
            case 3: {
                if (!this.f((byte)2)) break;
                n2 = this.d[3] * (100 + aq.c[3][2][5]) / 100;
            }
        }
        return n2;
    }

    public final void k(int n2) {
        int n3 = n2;
        if (n2 <= 0) {
            n3 = 1;
        }
        this.u(this.d[1]);
        this.d[1] = n3 = (int)((short)(this.d[1] - n3));
        if (this.d[1] <= 0) {
            this.d[1] = 0;
        }
    }

    public final void l(int n2) {
        this.d[1] = n2 = (int)((short)(this.d[1] + n2));
        if (this.d[1] >= this.c[1]) {
            this.d[1] = n2 = this.c[1];
        }
    }

    private void B(int n2) {
        for (int i2 = 0; i2 < this.z.length; ++i2) {
            if (this.z[i2] == -1) continue;
            int n3 = i2;
            this.y[n3] = (short)(this.y[n3] + n2);
            if (this.y[i2] < aq.c[1][this.z[i2]][5]) continue;
            this.y[i2] = aq.c[1][this.z[i2]][5];
        }
    }

    public final boolean m(int n2) {
        return this.v[n2][4] == 1;
    }

    public final int a(byte by, int n2, int n3) {
        int n4 = 0;
        if (by == -1) {
            return 0;
        }
        switch (by) {
            case 0: {
                this.v[by][1] = (short)(this.c[3] * aq.c[6][by][3] / 100);
                this.v[by][2] = (short)(aq.c[6][by][4] * this.B() / 100);
                this.d[3] = n2 = (int)((short)(this.c[3] + this.v[by][1]));
                break;
            }
            case 1: {
                this.v[by][1] = (short)(this.c[3] * aq.c[6][by][3] / 100);
                this.v[by][2] = aq.c[6][by][4];
                this.d[3] = n2 = (int)((short)(this.c[3] - this.v[by][1]));
                break;
            }
            case 2: {
                this.v[by][1] = (short)(this.c[3] * aq.c[6][by][3] / 100);
                this.v[by][2] = aq.c[6][by][4];
                this.d[3] = n2 = (int)((short)(this.c[3] + this.v[by][1]));
                break;
            }
            case 3: {
                this.v[by][1] = (short)(this.c[1] * aq.c[6][by][3] / 100);
                n4 = this.v[by][1];
                this.u(this.d[1]);
                this.l(this.v[by][1]);
                break;
            }
            case 4: {
                this.K[4] = (short)n3;
                this.v[by][1] = (short)(this.c[3] * aq.c[1][n3][8] / 100);
                this.d[3] = n2 = (int)((short)(this.c[3] + this.v[by][1]));
                break;
            }
            case 5: {
                this.v[by][1] = aq.c[6][by][3];
                break;
            }
            case 6: {
                this.v[by][1] = aq.c[6][by][3];
                this.v[by][2] = aq.c[6][by][4];
                break;
            }
            case 7: {
                this.K[7] = (short)n3;
                this.v[by][1] = (short)(this.c[4] * aq.c[1][n3][8] / 100);
                this.d[4] = n2 = (int)((short)(this.c[4] + this.v[by][1]));
                break;
            }
            case 8: {
                this.v[by][1] = aq.c[6][by][3];
                break;
            }
            case 9: {
                this.v[by][1] = (short)(this.c[4] * aq.c[6][by][3] / 100);
                this.v[by][2] = (short)(this.c[3] * aq.c[6][by][4] / 100);
                this.d[4] = n2 = (int)((short)(this.c[4] + this.v[by][1]));
                this.d[3] = n2 = (int)((short)(this.c[3] - this.v[by][2]));
                break;
            }
            case 10: {
                this.v[by][1] = (short)(this.c[2] * aq.c[6][by][3] / 100);
                this.d[2] = n2 = (int)((short)(this.c[2] + this.v[by][1]));
                break;
            }
            case 11: {
                this.v[by][1] = (short)n2;
                b b2 = game.d.a().d[n2];
                for (int i2 = 0; i2 < b2.N[0]; ++i2) {
                    this.a(b2.x[0][i2], (int)b2.v[b2.x[0][i2]][1], (int)game.d.a().d[n2].K[i2]);
                }
                b2.D();
                break;
            }
            case 12: {
                this.K[12] = 1;
                break;
            }
            case 13: {
                this.v[by][1] = (short)(this.c[1] * aq.c[6][by][3] / 100);
                n4 = this.v[by][1];
                this.u(this.d[1]);
                this.l(this.v[by][1]);
                this.C();
                break;
            }
            case 14: {
                this.C();
                break;
            }
            case 15: {
                this.v[by][1] = (short)(n2 * aq.c[6][by][3]);
            }
        }
        this.a(0, by);
        this.v[by][0] = aq.c[6][by][2];
        this.v[by][4] = 1;
        return n4;
    }

    public final void n(int n2) {
        this.v[n2][4] = 0;
        for (n2 = 2; n2 <= 4; n2 = (int)((byte)(n2 + 1))) {
            int n3 = n2;
            short s2 = this.c[n3];
            n3 = n2;
            this.d[n3] = s2;
        }
    }

    public final int o(int n2) {
        int n3 = 0;
        switch (n2) {
            case 0: {
                break;
            }
            case 1: {
                short s2;
                this.d[3] = s2 = (short)(this.c[3] - this.v[n2][1]);
                break;
            }
            case 2: {
                short s3;
                this.d[3] = s3 = (short)(this.c[3] + this.v[n2][1]);
                break;
            }
            case 3: {
                n3 = this.v[n2][1];
                this.u(this.d[1]);
                this.l(this.v[n2][1]);
                break;
            }
            case 4: {
                short s4;
                this.d[3] = s4 = (short)(this.d[3] + this.v[n2][1]);
                break;
            }
            case 5: {
                break;
            }
            case 6: {
                break;
            }
            case 7: {
                short s5;
                this.d[4] = s5 = (short)(this.c[4] + this.v[n2][1]);
                break;
            }
            case 8: {
                break;
            }
            case 9: {
                short s6;
                this.d[4] = s6 = (short)(this.c[4] + this.v[n2][1]);
                this.d[3] = s6 = (short)(this.c[3] - this.v[n2][2]);
                break;
            }
            case 10: {
                short s7;
                this.d[2] = s7 = (short)(this.c[2] + this.v[n2][1]);
                break;
            }
            case 11: {
                b b2 = game.d.a().d[this.v[11][1]];
                for (int i2 = 0; i2 < b2.N[0]; ++i2) {
                    this.a(b2.x[0][i2], (int)b2.v[b2.x[0][i2]][1], (int)game.d.a().d[this.v[11][1]].K[i2]);
                }
                b2.D();
                break;
            }
            case 12: {
                this.K[12] = 2;
                break;
            }
            case 13: {
                n3 = this.v[n2][1];
                this.u(this.d[1]);
                this.l(this.v[n2][1]);
            }
        }
        return n3;
    }

    public final boolean p(int n2) {
        return this.w[n2][4] == 1;
    }

    public final void C() {
        int n2;
        for (n2 = 0; n2 < 11; ++n2) {
            if (this.w[n2][4] != 1) continue;
            this.C(n2);
        }
        for (n2 = 0; n2 < 3; ++n2) {
            this.e(1, n2);
        }
    }

    public final void D() {
        int n2;
        for (n2 = 0; n2 < 16; ++n2) {
            if (this.v[n2][4] != 1) continue;
            this.n(n2);
        }
        for (n2 = 0; n2 < 3; ++n2) {
            this.e(0, n2);
        }
    }

    private void C(int n2) {
        this.w[n2][4] = 0;
        for (n2 = 2; n2 <= 4; n2 = (int)((byte)(n2 + 1))) {
            int n3 = n2;
            short s2 = this.c[n3];
            n3 = n2;
            this.d[n3] = s2;
        }
    }

    private void e(int n2, int n3) {
        this.x[n2][n3] = -1;
        if (this.N[n2] > 0) {
            int n4 = n2;
            this.N[n4] = (byte)(this.N[n4] - 1);
        }
    }

    public final void q(int n2) {
        switch (n2) {
            case 0: {
                short s2 = this.w[0][1];
                n2 = aq.c[1][this.w[0][3]][8];
                this.k(s2 / n2);
                if (!this.S()) {
                    this.d((byte)3);
                }
                return;
            }
            case 1: {
                return;
            }
            case 2: {
                return;
            }
            case 3: {
                if (this.w[n2][0] > 1) break;
                short s3 = this.w[n2][1];
                n2 = aq.c[1][this.w[n2][3]][8];
                this.k(s3 * n2 / 100);
                if (!this.S()) {
                    this.d((byte)3);
                }
                return;
            }
            case 4: {
                return;
            }
            case 5: {
                this.d[4] = n2 = (int)((short)(this.c[4] - this.w[n2][1]));
                return;
            }
            case 6: {
                return;
            }
            case 7: {
                this.d[3] = n2 = (int)((short)(this.c[3] - this.w[n2][1]));
            }
        }
    }

    public final void c(int n2, int n3) {
        if (this.p(n2)) {
            if (this.w[n2][0] > 0) {
                short[] sArray = this.w[n2];
                sArray[0] = (short)(sArray[0] - 1);
            }
            if (this.w[n2][0] <= 0) {
                this.C(n2);
                this.e(1, n3);
            }
        }
    }

    public final void d(int n2, int n3) {
        if (this.m(n2)) {
            if (this.v[n2][0] > 0) {
                short[] sArray = this.v[n2];
                sArray[0] = (short)(sArray[0] - 1);
            }
            if (this.v[n2][0] <= 0) {
                this.n(n2);
                this.e(0, n3);
            }
        }
    }

    private void a(int n2, byte by) {
        int n3;
        for (n3 = 0; n3 < 3; ++n3) {
            int n4;
            if (this.x[n2][n3] != -1) continue;
            for (n4 = 0; n4 < 3; ++n4) {
                if (this.x[n2][n4] != by) continue;
                return;
            }
            if (n4 < 3) continue;
            this.x[n2][n3] = by;
            if (this.N[n2] >= 3) break;
            int n5 = n2;
            this.N[n5] = (byte)(this.N[n5] + 1);
            break;
        }
        if (n3 >= 3) {
            this.x[n2][0] = by;
        }
    }

    public final byte r(int n2) {
        return this.N[n2];
    }

    public final boolean f(byte by) {
        return this.c[5] == by;
    }

    public final int E() {
        return this.O;
    }

    public final int[] F() {
        int n2;
        int[] nArray = null;
        Vector<String> vector = new Vector<String>();
        short s2 = aq.c[0][this.V][18];
        short s3 = aq.c[0][this.V][1];
        int n3 = this.W();
        for (n2 = s3 * 10; n2 < s3 * 10 + 10; ++n2) {
            int n4 = 0;
            if (aq.c[1][n2][4] > aq.c[8][s2][n3]) continue;
            for (n4 = 0; n4 < this.z.length && n2 != this.z[n4]; ++n4) {
            }
            if (n4 < this.z.length) continue;
            vector.addElement(String.valueOf(n2));
        }
        if (vector.size() > 0) {
            nArray = new int[vector.size()];
            for (n2 = 0; n2 < nArray.length; ++n2) {
                nArray[n2] = Integer.parseInt((String)vector.elementAt(n2));
            }
        }
        return nArray;
    }

    public final void g(byte by) {
        for (int i2 = 0; i2 < this.z.length; ++i2) {
            if (this.z[i2] != -1) continue;
            this.z[i2] = by;
            this.O = (byte)(this.O + 1);
            this.y[i2] = aq.c[1][by][5];
            return;
        }
    }

    public final void G() {
        int n2 = aq.c[0][this.V][1];
        if (this.T <= 5) {
            n2 *= 10;
            boolean bl = true;
            for (int i2 = 0; i2 < this.z.length; ++i2) {
                if (n2 != this.z[i2]) continue;
                bl = false;
                break;
            }
            if (bl) {
                this.g((byte)n2);
            }
            return;
        }
        if (this.O >= this.W() + 1) {
            return;
        }
        int[] nArray = this.F();
        int n3 = nArray.length;
        for (int i3 = 0; i3 < nArray.length; ++i3) {
            this.g((byte)nArray[n2]);
            if (this.O >= this.T / 10 + 1) break;
            for (n2 = ae.a(n3); n2 < n3 - 1; ++n2) {
                nArray[n2] = nArray[n2 + 1];
            }
            --n3;
        }
    }

    public final boolean s(int n2) {
        if (n2 == -1) {
            return false;
        }
        return this.y[n2] > 0;
    }

    public final void a(byte by, b b2) {
        this.p = b2;
        this.D = by;
        for (int i2 = 0; i2 < this.z.length; ++i2) {
            if (this.z[i2] != by) continue;
            int n2 = i2;
            this.y[n2] = (short)(this.y[n2] - 1);
            if (this.m(12) && this.K[12] == 1) {
                int n3 = i2;
                this.y[n3] = (short)(this.y[n3] + 1);
            }
            if (!this.m(8)) continue;
            int n4 = i2;
            this.y[n4] = (short)(this.y[n4] - 1);
        }
    }

    public final void b(int[] nArray) {
        this.O = (byte)nArray[0];
        for (int i2 = 0; i2 < nArray[0]; ++i2) {
            this.z[i2] = (byte)nArray[i2 + 1];
            this.y[i2] = (short)nArray[nArray[0] + 1 + i2];
        }
    }

    public final byte t(int n2) {
        if (n2 > this.z.length - 1 || n2 < 0) {
            return -1;
        }
        return this.z[n2];
    }

    public static short b(byte by, byte by2) {
        return aq.c[1][by][by2];
    }

    private int W() {
        int[] nArray = new int[]{5, 10, 20, 30, 40};
        int n2 = 0;
        for (int i2 = 0; i2 < nArray.length; ++i2) {
            if (this.T < nArray[i2]) continue;
            n2 = i2;
        }
        return n2;
    }

    public final void h(byte by) {
        this.D = by;
    }

    public final byte H() {
        return this.D;
    }

    public final void g() {
        this.U();
        super.g();
        this.u(this.c[1]);
    }

    public final void I() {
        for (int i2 = 0; i2 < this.z.length; ++i2) {
            if (this.z[i2] == -1) continue;
            this.y[i2] = aq.c[1][this.z[i2]][5];
        }
        this.g();
        this.c();
    }

    public final void J() {
        short s2;
        if (game.k.H == null) {
            game.k.H = new Vector();
        }
        if ((s2 = aq.a((byte)0, (short)this.V, (byte)19)) == -1) {
            return;
        }
        short s3 = aq.a((byte)0, (short)this.V, (byte)21);
        int n2 = aq.a((byte)0, (short)this.V, (byte)20) + 12;
        boolean bl = false;
        if (!game.k.K && this.R() > 0 && this.T >= t[aq.a((byte)0, s2, (byte)2) - 1] && game.g.o().a(n2, (byte)2) >= s3) {
            bl = true;
        } else if (this.R() > 0 && this.T >= t[aq.a((byte)0, s2, (byte)2) - 1]) {
            bl = true;
        }
        if (bl) {
            int[] nArray = new int[]{this.V, aq.c[0][this.V][0]};
            game.k.H.addElement(nArray);
            game.k.L[0] = (byte)this.T;
            game.k.L[1] = (byte)this.V;
            game.k.I = 0;
        }
    }

    public final void i(byte by) {
        this.W = by;
        switch (by) {
            case 7: {
                by = (byte)(this.c[2] * 90 / 100);
                this.c[2] = by;
                by = (byte)(this.c[4] + 7);
                this.c[4] = by;
                by = (byte)(this.c[1] * 80 / 100);
                this.c[1] = by;
                return;
            }
            case 8: {
                by = (byte)(this.c[2] * 130 / 100);
                this.c[2] = by;
                by = (byte)(this.c[4] + -2);
                this.c[4] = by;
                by = (byte)(this.c[1] * 80 / 100);
                this.c[1] = by;
                return;
            }
            case 9: {
                by = (byte)(this.c[2] * 90 / 100);
                this.c[2] = by;
                by = (byte)(this.c[4] + -2);
                this.c[4] = by;
                by = (byte)(this.c[1] * 130 / 100);
                this.c[1] = by;
            }
        }
    }

    public final boolean K() {
        return this.Y;
    }

    public final void d(boolean bl) {
        this.Y = bl;
    }

    public final int j(byte by) {
        return aq.c[0][this.V][by];
    }

    public static short a(int n2, int n3, int n4, int n5) {
        switch (n5) {
            case 1: {
                return (short)((aq.c[0][n2][5] + aq.c[0][n2][6] * n3 + aq.c[0][n2][7]) * M[n4 - 1] / 100);
            }
            case 2: {
                return (short)((aq.c[0][n2][8] + aq.c[0][n2][9] * n3 + aq.c[0][n2][10]) * M[n4 - 1] / 100);
            }
            case 3: {
                return (short)((aq.c[0][n2][11] + aq.c[0][n2][12] * n3 / 10 + aq.c[0][n2][13]) * M[n4 - 1] / 100);
            }
            case 4: {
                return (short)((aq.c[0][n2][14] + aq.c[0][n2][15] * n3 / 10 + aq.c[0][n2][16]) * M[n4 - 1] / 100);
            }
        }
        return 0;
    }

    public final int L() {
        return this.d[1] * 100 / this.c[1];
    }

    public final int M() {
        return this.Q * 100 / this.c[1];
    }

    public final int N() {
        return this.Q;
    }

    public final void u(int n2) {
        if (n2 >= this.c[1]) {
            this.Q = this.c[1];
            return;
        }
        this.Q = n2;
    }

    public final int O() {
        return this.S * 100 / this.u();
    }

    public final int v(int n2) {
        return n2 * 100 / this.u();
    }

    public static int a(short s2, short s3) {
        s3 = s3 >= 50 ? (short)37300 : (short)(s3 * 15 * s3 - 200);
        return s2 * 100 / s3;
    }

    public final int[] P() {
        int[] nArray = new int[9 + (this.O << 1) + 1];
        int[] nArray2 = nArray;
        nArray[0] = this.V;
        nArray2[1] = this.T;
        nArray2[2] = this.c[5];
        nArray2[3] = this.d[6];
        nArray2[4] = this.c[0];
        nArray2[5] = this.W;
        nArray2[6] = this.d[1];
        nArray2[7] = this.S;
        nArray2[8] = this.E;
        nArray2[9] = this.O;
        for (int i2 = 0; i2 < this.O; ++i2) {
            nArray2[i2 + 10] = this.z[i2];
            nArray2[10 + nArray2[9] + i2] = this.y[i2];
        }
        return nArray2;
    }

    public final int[] Q() {
        int[] nArray = new int[(this.O << 1) + 1];
        int[] nArray2 = nArray;
        nArray[0] = this.O;
        for (int i2 = 0; i2 < this.O; ++i2) {
            nArray2[i2 + 1] = this.z[i2];
            nArray2[nArray2[0] + i2 + 1] = this.y[i2];
        }
        return nArray2;
    }

    public final int R() {
        if (this.j((byte)19) == -1) {
            return 0;
        }
        if (aq.c[0][this.j((byte)19)][2] == 1) {
            return 1;
        }
        if (aq.c[0][this.j((byte)19)][2] == 2) {
            return 1;
        }
        if (aq.c[0][this.j((byte)19)][2] == 3) {
            return 2;
        }
        return 0;
    }

    public final void w(int n2) {
        switch (aq.c[4][n2][5]) {
            case 1: {
                short s2 = (short)(this.c[1] * aq.c[4][n2][6] / 100 + aq.c[4][n2][7]);
                this.u(this.d[1] + s2);
                this.l(s2);
                break;
            }
            case 2: {
                short s3 = aq.c[4][n2][6];
                this.B(s3);
                break;
            }
            case 3: {
                short s4 = (short)(this.c[1] * aq.c[4][n2][6] / 100 + aq.c[4][n2][7]);
                short s5 = aq.c[4][n2][8];
                this.u(this.d[1] + s4);
                this.l(s4);
                this.B(s5);
                break;
            }
            case 4: {
                this.c();
                short s6 = (short)(this.c[1] * aq.c[4][n2][6] / 100 + aq.c[4][n2][7]);
                short s7 = aq.c[4][n2][8];
                this.u(s6);
                this.l(s6);
                this.B(s7);
                break;
            }
            case 5: {
                this.C();
                break;
            }
            case 6: {
                this.d[6] = 2;
            }
        }
        game.g.o().d(n2, 1, (byte)0);
    }

    public final int x(int n2) {
        if (!this.S() && aq.c[4][n2][5] != 4) {
            return 8;
        }
        switch (aq.c[4][n2][5]) {
            case 0: {
                return 6;
            }
            case 1: {
                if (this.c[1] != this.d[1]) break;
                return 2;
            }
            case 2: {
                n2 = this.O;
                for (int i2 = 0; i2 < n2; ++i2) {
                    if (this.y[i2] >= game.b.b(this.z[i2], (byte)5)) continue;
                    return -1;
                }
                return 3;
            }
            case 3: {
                n2 = -1;
                if (this.c[1] == this.d[1] || !this.S()) {
                    n2 = 2;
                }
                int n3 = this.O;
                for (int i3 = 0; i3 < n3; ++i3) {
                    if (this.y[i3] >= game.b.b(this.z[i3], (byte)5)) continue;
                    return -1;
                }
                if (n2 != 2) break;
                return 7;
            }
            case 4: {
                if (!this.S()) break;
                return 1;
            }
            case 5: {
                for (n2 = 0; n2 < this.w.length; ++n2) {
                    if (!this.p(n2)) continue;
                    return -1;
                }
                return 4;
            }
            case 6: {
                if (this.d[6] < 2) break;
                return 5;
            }
        }
        return -1;
    }

    public final boolean S() {
        return this.d[1] > 0;
    }

    /*
     * Unable to fully structure code
     */
    public final byte a(b var1_1) {
        var2_3 = aq.c[0][this.V][1];
        var3_4 = aq.c[0][var1_1.V][1];
        var4_5 = aq.c[0][this.V][22];
        var1_2 = aq.c[0][var1_1.V][22];
        var5_6 = false;
        var6_7 = false;
        if (var4_5 == 2 && var1_2 == 2) ** GOTO lbl-1000
        if (var4_5 == 2 && var1_2 != 2) {
            var5_6 = true;
        } else if (var4_5 != 2 && var1_2 == 2) {
            var6_7 = true;
        } else lbl-1000:
        // 2 sources

        {
            var5_6 = true;
            var6_7 = true;
        }
        if (var5_6 && (var2_3 == 0 && var3_4 == 1 || var2_3 == 1 && var3_4 == 2 || var2_3 == 2 && var3_4 == 3 || var2_3 == 3 && var3_4 == 0 || var2_3 == 5 && var3_4 == 6 || var2_3 == 6 && var3_4 == 4 || var2_3 == 4 && var3_4 == 5)) {
            return 0;
        }
        if (var6_7 && (var3_4 == 0 && var2_3 == 1 || var3_4 == 1 && var2_3 == 2 || var3_4 == 2 && var2_3 == 3 || var3_4 == 3 && var2_3 == 0 || var3_4 == 5 && var2_3 == 6 || var3_4 == 6 && var2_3 == 4 || var3_4 == 4 && var2_3 == 5)) {
            return 1;
        }
        return -1;
    }

    /*
     * Unable to fully structure code
     */
    public final int[] b(b var1_1) {
        block48: {
            block49: {
                block47: {
                    var2_2 = 0;
                    var3_3 = 5;
                    var4_6 = this.B();
                    var5_7 = aq.c[0][this.V][1];
                    if (this.C == game.g.o().W[var5_7] + game.g.o().X[var5_7] - 1) {
                        var3_3 = 30;
                    }
                    var3_3 += this.d[4] / 2;
                    if (this.f((byte)4)) {
                        var3_3 += aq.c[3][4][5];
                    }
                    if (ae.a(100) <= var3_3) {
                        var4_6 = var4_6 * 3 / 2;
                        var2_2 = 1;
                    }
                    var5_7 = (byte)aq.c[1][this.D][7];
                    var3_3 = -1;
                    var6_8 = var4_6;
                    switch (this.D) {
                        case 0: 
                        case 6: 
                        case 10: 
                        case 11: 
                        case 12: 
                        case 13: 
                        case 16: 
                        case 17: 
                        case 18: 
                        case 19: 
                        case 20: 
                        case 26: 
                        case 30: 
                        case 31: 
                        case 32: 
                        case 33: 
                        case 36: 
                        case 37: 
                        case 38: 
                        case 39: 
                        case 40: 
                        case 46: 
                        case 50: 
                        case 51: 
                        case 52: 
                        case 54: 
                        case 55: 
                        case 56: 
                        case 57: 
                        case 58: 
                        case 60: 
                        case 61: 
                        case 63: 
                        case 66: 
                        case 68: 
                        case 69: {
                            var4_6 = var4_6 * aq.c[1][this.D][3] / 100;
                            break;
                        }
                        case 1: 
                        case 7: {
                            var4_6 = var4_6 * aq.c[1][this.D][3] / 100 + var4_6 / aq.c[1][this.D][8];
                            break;
                        }
                        case 2: 
                        case 8: 
                        case 22: 
                        case 28: 
                        case 41: 
                        case 47: {
                            var4_6 = var4_6 * aq.c[1][this.D][3] / 100;
                            var3_3 = aq.c[1][this.D][8];
                            break;
                        }
                        case 3: 
                        case 9: {
                            if (var1_1.p(0)) {
                                var4_6 = var4_6 * aq.c[1][this.D][8] / 100;
                                break;
                            }
                            var4_6 = var4_6 * aq.c[1][this.D][3] / 100;
                            break;
                        }
                        case 23: 
                        case 29: {
                            if (var1_1.p(1)) {
                                var4_6 = var4_6 * aq.c[1][this.D][8] / 100;
                                break;
                            }
                            var4_6 = var4_6 * aq.c[1][this.D][3] / 100;
                            break;
                        }
                        case 43: 
                        case 49: {
                            var4_6 = var4_6 * aq.c[1][this.D][3] / 100;
                            var1_1.D();
                            break;
                        }
                        case 53: 
                        case 59: {
                            var7_9 = this.d[1] * 100 / this.c[1];
                            var4_6 = var4_6 * (aq.c[1][this.D][8] - var7_9) / 100;
                            break;
                        }
                        default: {
                            var5_7 = -1;
                        }
                    }
                    if (var6_8 <= 0) {
                        var6_8 = 1;
                    }
                    var8_10 = var3_3;
                    var7_9 = this.D;
                    var6_8 = (short)var6_8;
                    var3_4 = var1_1;
                    if (var5_7 != -1) break block47;
                    v0 = -1;
                    break block48;
                }
                if (!var3_4.f((byte)3)) break block49;
                if (ae.a(100) <= var8_10 * (100 - aq.c[3][3][5]) / 100) ** GOTO lbl-1000
                v0 = -1;
                break block48;
            }
            if (var3_4.m(14)) {
                v0 = -1;
            } else if (var8_10 != -1 && ae.a(100) > var8_10) {
                v0 = -1;
            } else lbl-1000:
            // 2 sources

            {
                switch (var5_7) {
                    case 0: {
                        var3_4.w[var5_7][1] = var6_8;
                        break;
                    }
                    case 1: {
                        break;
                    }
                    case 2: {
                        break;
                    }
                    case 3: {
                        var3_4.w[var5_7][1] = var6_8;
                        break;
                    }
                    case 4: {
                        var3_4.w[var5_7][1] = aq.c[1][var7_9][8];
                        break;
                    }
                    case 5: {
                        var3_4.w[var5_7][1] = (short)(var3_4.c[4] * aq.c[1][var7_9][8] / 100);
                        var3_4.d[4] = var6_8 = (int)((short)(var3_4.c[4] - var3_4.w[var5_7][1]));
                        break;
                    }
                    case 6: {
                        var3_4.w[var5_7][1] = aq.c[1][var7_9][8];
                        break;
                    }
                    case 7: {
                        var3_4.w[var5_7][1] = (short)(var3_4.c[3] * aq.c[1][var7_9][8] / 100);
                        var3_4.d[3] = var6_8 = (int)((short)(var3_4.c[3] - var3_4.w[var5_7][1]));
                    }
                }
                var3_4.a(1, (byte)var5_7);
                var3_4.w[var5_7][0] = var3_4.X == 0 && game.g.o().c((byte)6, (byte)0) == 2 && game.g.o().c((byte)6, (byte)1) == 1 ? (short)(aq.c[7][var5_7][2] / 2) : aq.c[7][var5_7][2];
                var3_4.w[var5_7][3] = var7_9;
                var3_4.w[var5_7][4] = 1;
                v0 = var7_9 = var5_7;
            }
        }
        if (this.m(0) && this.v[0][0] == 0) {
            var4_6 += this.v[0][2];
        }
        if (this.m(1)) {
            var4_6 += var4_6 * this.v[1][2] / 100;
        }
        if (this.p(6)) {
            var4_6 -= var4_6 * this.w[6][1] / 100;
        }
        if (var1_1.m(6) && ae.a(100) <= this.v[6][1]) {
            var4_6 = var4_6 * this.v[6][2] / 100;
        }
        if (this.m(8)) {
            var4_6 += var4_6 * this.v[8][1] / 100;
        }
        if (this.X == 0 && game.g.o().c((byte)3, (byte)0) == 2 && game.g.o().c((byte)3, (byte)1) == 1 && game.k.O == 2) {
            var4_6 += var4_6 * aq.c[2][3][5] / 100;
        }
        if (this.X == 0 && game.g.o().c((byte)6, (byte)0) == 2) {
            var4_6 += var4_6 * aq.c[2][6][5] / 100;
        }
        if (this.a(var1_1) == 0) {
            var4_6 *= 3;
        } else if (this.a(var1_1) == 1) {
            var4_6 = var4_6 * 60 / 100;
        }
        if (var4_6 <= 0) {
            var4_6 = 1;
        } else {
            var3_5 = ae.a(100);
            var5_7 = (var4_6 << 1) / 100;
            if (var3_5 > 50) {
                if (var5_7 <= 0) {
                    ++var4_6;
                }
            } else if (var5_7 <= 0) {
                --var4_6;
            }
            if (var4_6 <= 0) {
                var4_6 = 1;
            }
        }
        if (var1_1.m(5) && ae.a(100) <= var1_1.v[5][1]) {
            this.K[5] = (short)var4_6;
            return new int[]{var4_6, var2_2, var7_9};
        }
        return new int[]{var4_6, var2_2, var7_9};
    }

    public final String T() {
        String[] stringArray = new String[]{"M\u1ed9c h\u1ec7", "Th\u1ed5 h\u1ec7", "Th\u1ee7y h\u1ec7", "H\u1ecfa h\u1ec7", "Qu\u1ef7 h\u1ec7", "Phong h\u1ec7", "\u0110i\u1ec7n h\u1ec7"};
        short s2 = aq.c[0][this.V][1];
        return stringArray[s2];
    }

    public static String y(int n2) {
        String[] stringArray = new String[]{"M\u1ed9c h\u1ec7", "Th\u1ed5 h\u1ec7", "Th\u1ee7y h\u1ec7", "H\u1ecfa h\u1ec7", "Qu\u1ef7 h\u1ec7", "Phong h\u1ec7", "\u0110i\u1ec7n h\u1ec7"};
        n2 = aq.c[0][n2][1];
        return stringArray[n2];
    }
}

