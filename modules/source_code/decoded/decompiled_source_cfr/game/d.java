/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Graphics
 *  javax.microedition.lcdui.Image
 */
package game;

import game.b;
import game.g;
import game.h;
import game.i;
import game.k;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public final class d
extends an {
    private final byte[] n = new byte[]{2, 4};
    private static d o;
    private static g p;
    private byte q = 0;
    public int a;
    public byte b;
    private byte r;
    public Image c;
    private int[][] s;
    public b[] d;
    private byte[] t;
    public byte[] e;
    public byte[] f;
    private byte[] u;
    public byte g = 0;
    public b h;
    private Vector v;
    public byte i;
    private boolean w;
    private static Vector x;
    public static Vector j;
    private boolean y;
    public boolean k;
    private boolean z;
    private boolean A;
    private boolean B;
    private byte C;
    private byte[] D;
    private byte[] E;
    private byte F;
    private int G;
    private ah H;
    private byte I = 0;
    private byte J = 0;
    private byte K = 0;
    private byte L = 0;
    private boolean M;
    private boolean N;
    private byte[] O;
    private int[] Z;
    private boolean aa;
    private byte ab;
    private byte ac;
    private byte ad;
    private byte[] ae;
    private byte[][] af;
    private byte[] ag;
    private byte[] ah;
    private byte[][] ai = new byte[][]{{3, 5, 13}, {0, 1, 2, 3, 8, 9, 10}};
    private f aj;
    private boolean ak;
    public static byte l;
    private f[] al;
    private static short[][] am;
    private static short[][][] an;
    private static byte[][] ao;
    public static short[][] m;
    private static byte[][] ap;
    private static byte[][] aq;
    private static byte[][] ar;
    private int as;
    private int at;
    private int au;
    private int av;
    private int aw;
    private int ax;
    private String ay = null;
    private static Image[] az;
    private static short[][] aA;
    private Vector aB = new Vector();
    private Vector aC = new Vector();
    private int aD;
    private boolean aE;
    private int aF;
    private byte[] aG;
    private byte[] aH;
    private byte[] aI;

    public static d a() {
        if (o == null) {
            o = new d();
        }
        return o;
    }

    public d() {
        new Vector();
        new Vector();
        this.aD = 0;
        this.aE = false;
        this.aF = 0;
        this.aG = new byte[]{10, 11, 12, 13, 15};
        this.aH = new byte[]{10, 12, 13, 14, 15, 16};
        this.aI = new byte[]{105, 100, 80, 60, 40, 20, 5};
        if (this.v == null) {
            this.v = new Vector();
        }
        if (x == null) {
            x = new Vector();
        }
        if (j == null) {
            j = new Vector();
        }
    }

    public final void f() {
        int n2;
        this.v.removeAllElements();
        for (n2 = 0; n2 < this.f.length; ++n2) {
            this.c(n2).C();
            this.c(n2).D();
            this.c((int)n2).F = 0;
            this.c(n2).d(false);
            this.c((int)n2).G.removeAllElements();
            this.c((int)n2).H.removeAllElements();
            this.c(n2).u(this.c((int)n2).d[1]);
        }
        x.removeAllElements();
        j.removeAllElements();
        for (n2 = 0; n2 < this.d.length; ++n2) {
            if (this.d[n2] == null) continue;
            this.d[n2].d();
            this.d[n2] = null;
        }
        for (n2 = 0; n2 < az.length; ++n2) {
            if (az[n2] == null) continue;
            game.d.az[n2] = null;
        }
        az = null;
        if (this.aj != null) {
            this.aj.a.b();
            this.aj = null;
        }
        if (this.al != null) {
            for (n2 = 0; n2 < this.al.length; ++n2) {
                this.al[n2].a.b();
                this.al[n2] = null;
            }
            this.al = null;
        }
        this.u = null;
        this.i = 0;
        this.G = 0;
        this.h = null;
        this.w = false;
        this.y = false;
        this.H = null;
        this.d = null;
        this.c = null;
        this.t = null;
        this.e = null;
        this.D = null;
        this.E = null;
        this.ae = null;
        this.O = null;
        ap = null;
        aq = null;
        game.k.r = 0;
        am = null;
        an = null;
        ao = null;
        m = null;
        aA = null;
        ap = null;
        aq = null;
        ar = null;
        this.S.af();
    }

    public final boolean d() {
        int n2;
        InputStream inputStream;
        int n3;
        this.s();
        this.u = new byte[2];
        p = game.g.o();
        this.f = new byte[game.d.p.A];
        int n4 = 0;
        for (n3 = 0; n3 < game.d.p.A; ++n3) {
            this.f[n3] = (byte)n3;
            if (this.c(n3) != null && this.c(n3).S()) {
                ++n4;
            }
            this.c(n3).j(this.c(n3).z());
        }
        this.d = n4 == 1 && this.a == 1 ? new b[3] : new b[this.n[this.a]];
        try {
            inputStream = null;
            am = ae.a(ae.a("/data/script/pos.mid"));
            inputStream = ae.a("/data/script/cpos.mid");
            an = new short[3][][];
            for (n3 = 0; n3 < 3; ++n3) {
                game.d.an[n3] = ae.a(inputStream);
            }
            ao = ae.b(ae.a("/data/script/effect.mid"));
            m = ae.a(ae.a("/data/script/speffect.mid"));
            aA = ae.a(ae.a("/data/script/blood.mid"));
            inputStream = ae.a("/data/script/bufDebuf.mid");
            ap = ae.b(inputStream);
            aq = ae.b(inputStream);
            ar = ae.b(inputStream);
            inputStream.close();
        }
        catch (IOException iOException) {
            inputStream = null;
            iOException.printStackTrace();
        }
        this.r = this.a == 0 ? (this.b == 1 ? (byte)2 : (byte)0) : (byte)1;
        this.al = new f[this.d.length + 2];
        for (n3 = 0; n3 < this.al.length; ++n3) {
            this.al[n3] = new f();
            this.al[n3].a(294, false);
            if (n3 == this.d.length + 1) {
                this.al[n3].a((byte)2, (byte)-1, false);
                if (this.a != 0) continue;
                this.al[n3].c();
                continue;
            }
            if (n3 == this.d.length) {
                this.al[n3].a((byte)1, (byte)-1, false);
                this.al[n3].c();
                this.al[n3].b(false);
                continue;
            }
            this.al[n3].a((byte)0, (byte)-1, false);
            this.al[n3].b(an[this.r][n3][2], an[this.r][n3][3]);
            this.al[n3].c();
        }
        this.D = new byte[this.d.length];
        this.E = new byte[this.d.length];
        this.n(this.d.length);
        n3 = 0;
        block13: for (n2 = 0; n2 < this.d.length; ++n2) {
            if (this.a == 0) {
                if (n2 > 0) {
                    while (!this.c((int)this.f[n3]).S()) {
                        ++n3;
                    }
                    this.a(n2, n3);
                    x.addElement(this.c((int)this.f[n3]));
                    this.e(0, n3);
                    continue;
                }
                switch (this.b) {
                    case 0: 
                    case 1: {
                        this.m(n2);
                        break;
                    }
                    case 2: {
                        this.m(n2);
                    }
                }
                continue;
            }
            if (n2 > 1) {
                while (!this.c((int)this.f[n3]).S()) {
                    ++n3;
                }
                this.a(n2, n3);
                x.addElement(this.c((int)this.f[n3]));
                this.e(n2 - 2, n3);
                ++n3;
                continue;
            }
            switch (this.b) {
                case 0: 
                case 1: {
                    this.m(n2);
                    continue block13;
                }
                case 2: {
                    this.m(n2);
                }
            }
        }
        this.T();
        az = new Image[3];
        for (n2 = 0; n2 < az.length; ++n2) {
            game.d.az[n2] = ae.b("/data/tex/", "blood_" + n2);
        }
        for (n2 = 0; n2 < game.d.p.A; ++n2) {
            game.d.p.z[n2].A = game.d.p.z[n2].d[1];
        }
        this.a((byte)0);
        game.d.t();
        return true;
    }

    public final void c() {
        int n2;
        int n3 = 0;
        for (int i2 = 0; i2 < game.d.p.A; ++i2) {
            if (this.c(i2) != null && this.c(i2).S()) {
                ++n3;
            }
            this.c(i2).j(this.c(i2).z());
        }
        b[] bArray = this.a == 0 ? new b[1] : new b[2];
        for (n2 = 0; n2 < this.d.length; n2 = (int)((byte)(n2 + 1))) {
            if (this.d[n2].r() != 1) continue;
            bArray[n2] = this.d[n2];
        }
        this.d = n3 == 1 && this.a == 1 ? new b[3] : new b[this.n[this.a]];
        this.al = new f[this.d.length + 2];
        for (n2 = 0; n2 < this.al.length; ++n2) {
            this.al[n2] = new f();
            this.al[n2].a(294, false);
            if (n2 == this.d.length + 1) {
                this.al[n2].a((byte)2, (byte)-1, false);
                if (this.a != 0) continue;
                this.al[n2].c();
                continue;
            }
            if (n2 == this.d.length) {
                this.al[n2].a((byte)1, (byte)-1, false);
                this.al[n2].c();
                this.al[n2].b(false);
                continue;
            }
            this.al[n2].a((byte)0, (byte)-1, false);
            this.al[n2].b(an[this.r][n2][2], an[this.r][n2][3]);
            this.al[n2].c();
        }
        this.D = new byte[this.d.length];
        this.E = new byte[this.d.length];
        this.G = 0;
        this.n(this.d.length);
        n2 = 0;
        for (n3 = 0; n3 < this.d.length; ++n3) {
            if (this.a == 0) {
                if (n3 > 0) {
                    while (!this.c((int)this.f[n2]).S()) {
                        ++n2;
                    }
                    this.a(n3, n2);
                    x.addElement(this.c((int)this.f[n2]));
                    this.e(0, n2);
                    continue;
                }
                this.d[n3] = bArray[n3];
                continue;
            }
            if (n3 > 1) {
                while (!this.c((int)this.f[n2]).S()) {
                    ++n2;
                }
                this.a(n3, n2);
                x.addElement(this.c((int)this.f[n2]));
                this.e(n3 - 2, n2);
                ++n2;
                continue;
            }
            this.d[n3] = bArray[n3];
        }
        this.T();
    }

    public final void e() {
        if (this.a == 1) {
            int n2 = 0;
            while (((b)this.v.elementAt(n2)).r() != 0 || ((b)this.v.elementAt(n2)).r() == 0 && !((b)this.v.elementAt(n2)).S()) {
                ++n2;
            }
            if (this.d[0].S()) {
                this.S.b(this.d[this.e[n2]], this.d[0]);
                return;
            }
            this.S.b(this.d[this.e[n2]], this.d[1]);
        }
    }

    public final void g() {
        this.S = game.h.a();
        this.S.a(this);
        this.R = ab.a();
        if (this.a == 0) {
            this.S.a(this.d[1], this.d[0]);
            return;
        }
        this.S.a(this.d[2], this.d[0]);
    }

    public final void a(int n2, int n3) {
        this.d[n2] = this.c((int)this.f[n3]);
        this.d[n2].d(true);
        this.d[n2].f(0);
        this.d[n2].n = 0;
        this.d[n2].b(an[this.r][n2][0], an[this.r][n2][1]);
        this.d[n2].c();
    }

    private void m(int n2) {
        this.d[n2] = new b();
        this.d[n2].a(this.s[this.u[0]][0], this.s[this.u[0]][1], (short)-1, (byte)2, (short)this.s[this.u[0]][2], (byte)-1);
        this.d[n2].f(1);
        this.d[n2].n = 1;
        this.d[n2].b(an[this.r][n2][0], an[this.r][n2][1]);
        short s2 = aq.c[0][this.s[this.u[0]][0]][1];
        this.d[n2].g((byte)(s2 * 10));
        this.d[n2].G();
        this.d[n2].c();
        p.a((byte)this.d[n2].j((byte)1), this.d[n2].q(), (byte)1);
        this.u[0] = (byte)(this.u[0] + 1);
    }

    private void n() {
        this.J = this.I;
        this.O = ao[this.h.D];
        if (this.O[this.J * 7 + 1] == 1) {
            short s2;
            short s3;
            short s4;
            short s5;
            short s6;
            this.H = new ah();
            if (this.O[this.J * 7] == 0) {
                s6 = (short)((b)this.h.p).i;
                s5 = (short)((b)this.h.p).j;
                s4 = (short)((b)this.h.p).q();
                s4 = aq.c[0][s4][17];
                s3 = ((b)this.h.p).p();
                s2 = ((b)this.h.p).n;
            } else {
                s6 = (short)this.h.i;
                s5 = (short)this.h.j;
                s4 = (short)this.h.q();
                s4 = aq.c[0][s4][17];
                s3 = this.h.p();
                s2 = this.h.n;
            }
            short[] sArray = m[this.O[this.J * 7 + 2]];
            short[] sArray2 = new short[sArray.length + 5];
            System.arraycopy(sArray, 1, sArray2, 6, sArray.length - 1);
            short[] sArray3 = new short[]{sArray[0], s6, s5, s4, s3, s2};
            System.arraycopy(sArray3, 0, sArray2, 0, sArray3.length);
            this.H.a(sArray2);
            this.H.c(true);
        } else if (this.O[this.J * 7] == 0) {
            ((b)this.h.p).a((short)this.O[this.J * 7 + 2], this.O[this.J * 7 + 3]);
        } else {
            this.h.a((short)this.O[this.J * 7 + 2], this.O[this.J * 7 + 3]);
        }
        this.I = (byte)(this.I + 1);
    }

    private boolean a(b b2) {
        while (!this.b(b2)) {
            for (int i2 = 0; i2 < this.ai[this.ag[this.ad << 1]].length; ++i2) {
                if (this.ai[this.ag[this.ad << 1]][i2] != this.ag[(this.ad << 1) + 1]) continue;
                return false;
            }
            if (this.ag[this.ad << 1] == 0) {
                b2.o(this.ag[(this.ad << 1) + 1]);
                b2.d(this.ag[(this.ad << 1) + 1], this.ah[this.ad]);
            } else if (this.ag[this.ad << 1] == 1) {
                b2.q(this.ag[(this.ad << 1) + 1]);
                b2.c(this.ag[(this.ad << 1) + 1], this.ah[this.ad]);
            }
            if (b2.r() == 0) {
                this.S.a(b2, false);
                this.S.a(b2);
            } else {
                this.S.b(b2, this.aE);
                this.S.b(b2);
            }
            this.ad = (byte)(this.ad + 1);
        }
        return true;
    }

    private void o() {
        b b2 = (b)this.v.elementAt(this.i);
        this.ac = this.ab;
        this.ae = this.af[this.ad];
        if (this.ae[this.ac << 2] == 1) {
            this.H = new ah();
            short s2 = (short)b2.i;
            short s3 = (short)b2.j;
            short s4 = (short)b2.q();
            s4 = aq.c[0][s4][17];
            short s5 = b2.p();
            short s6 = b2.n;
            short[] sArray = m[this.ae[(this.ac << 2) + 1]];
            short[] sArray2 = new short[sArray.length + 5];
            System.arraycopy(sArray, 1, sArray2, 6, sArray.length - 1);
            short[] sArray3 = new short[]{sArray[0], s2, s3, s4, s5, s6};
            System.arraycopy(sArray3, 0, sArray2, 0, sArray3.length);
            this.H.a(sArray2);
            this.H.c(true);
        } else {
            b2.a((short)this.ae[(this.ac << 2) + 1], this.ae[(this.ac << 2) + 2]);
        }
        this.ab = (byte)(this.ab + 1);
    }

    private void a(b b2, boolean n2) {
        if (!b2.S()) {
            b2.C();
            b2.D();
            this.S.b(b2);
            this.h(b2);
            this.u[1] = (byte)(this.u[1] + 1);
        }
        if (this.u[1] >= this.s.length) {
            game.d.X();
            this.a((byte)8);
            return;
        }
        if (!b2.S()) {
            for (n2 = 0; n2 < this.d.length; ++n2) {
                if (!this.d[n2].m(11) || !this.d[this.d[n2].v[11][1]].equals(b2)) continue;
                this.d[n2].n(11);
            }
            if (this.u[0] < this.s.length) {
                this.m(this.e[this.i]);
                this.g = this.e[this.i];
                this.a((byte)15);
                return;
            }
            this.i = (byte)(this.i + 1);
            this.p();
            return;
        }
        if (n2 != 0) {
            this.a((byte)2);
        }
    }

    private void b(b b2, boolean n2) {
        int n3;
        for (n3 = 0; n3 < this.f.length && !this.c((int)this.f[n3]).S(); ++n3) {
        }
        if (n3 >= this.f.length) {
            this.a((byte)9);
            return;
        }
        if (!b2.S()) {
            for (n2 = 0; n2 < this.d.length; ++n2) {
                if (!this.d[n2].m(11) || !this.d[this.d[n2].v[11][1]].equals(b2)) continue;
                this.d[n2].n(11);
            }
            b2.C();
            b2.D();
            this.S.a(b2);
            x.removeElement(b2);
            j.removeElement(b2);
            b2.B = 0;
            b2.d(false);
            b2.F = 0;
            if (this.r()) {
                this.g = this.e[this.i];
                this.a((byte)5);
                return;
            }
            this.i = (byte)(this.i + 1);
            this.p();
            return;
        }
        if (n2 != 0) {
            if (b2.p(9)) {
                this.a((byte)2);
                return;
            }
            this.a((byte)20);
        }
    }

    private boolean b(b b2) {
        if (this.ad >= this.af.length) {
            this.ad = 0;
            this.ab = 0;
            this.ac = 0;
            this.af = null;
            this.ag = null;
            this.ah = null;
            this.H = null;
            if (this.P == 12) {
                this.a(b2, true);
            } else if (this.P == 13) {
                this.b(b2, true);
            }
            return true;
        }
        return false;
    }

    private void c(b b2) {
        this.ad = (byte)(this.ad + 1);
        if (!this.b(b2)) {
            if (this.a(b2)) {
                return;
            }
            this.ab = 0;
            this.ac = 0;
            this.o();
            if (this.P == 12) {
                if (this.g(b2) == 2) {
                    this.ad = 0;
                    this.af = null;
                    this.ag = null;
                    this.ah = null;
                }
                this.a(b2, false);
                return;
            }
            if (this.P == 13) {
                if (this.g(b2) == 1) {
                    this.ad = 0;
                    this.af = null;
                    this.ag = null;
                    this.ah = null;
                }
                this.b(b2, false);
            }
        }
    }

    private void n(int n2) {
        this.t = new byte[n2];
        this.e = new byte[n2];
        for (n2 = 0; n2 < this.t.length; ++n2) {
            this.t[n2] = (byte)n2;
        }
    }

    private void e(byte by) {
        switch (by) {
            case 0: {
                this.aj.a(by, (byte)0, true);
                break;
            }
            case 1: {
                this.d[0].b(false);
                short s2 = aq.c[0][((b)this.h.p).q()][17];
                short[] sArray = new short[]{8, (short)((b)this.h.p).i, (short)((b)this.h.p).j, s2, 0, ((b)this.h.p).n, 0, 9, 1, 3, 0, 10, 0, 0, 7, 0, -10, 4, 0, -20};
                this.H = new ah();
                this.H.a(sArray);
                this.H.c(true);
                this.H.a();
                this.aj.a(by, (byte)-2, true);
                break;
            }
            case 2: {
                this.aj.a(by, (byte)0, true);
                break;
            }
            case 3: {
                this.aj.a(by, (byte)-2, true);
                break;
            }
            case 4: {
                short s3 = aq.c[0][((b)this.h.p).q()][17];
                short[] sArray = new short[]{8, (short)((b)this.h.p).i, (short)((b)this.h.p).j, s3, 0, ((b)this.h.p).n, 0, 8, 1, 4, 1, 4, 0, -20, 6, 0, -12, 8, 0, -4, 10, 0, 0};
                this.H = new ah();
                this.H.a(sArray);
                this.H.c(true);
                this.H.a();
                this.aj.a((byte)1, (byte)-2, true);
            }
        }
        this.q = by;
    }

    private void a(int n2, boolean bl) {
        this.al[this.d.length + 1].b(bl);
        this.al[this.d.length + 1].b(am[this.a][(n2 << 2) + 2], am[this.a][(n2 << 2) + 3]);
    }

    private void b(int n2, boolean bl) {
        this.al[this.d.length].b(bl);
        this.al[this.d.length].b(am[this.a][(n2 << 2) + 2], am[this.a][(n2 << 2) + 3]);
    }

    public final void a(byte by) {
        this.Q = this.P;
        this.P = by;
        switch (by) {
            case 0: {
                this.i = 0;
                while (((b)this.v.elementAt(this.i)).r() != 0) {
                    this.i = (byte)(this.i + 1);
                }
                break;
            }
            case 15: {
                this.S.a = 0;
                this.y = true;
                this.G = this.g;
                this.E[this.G] = 0;
                this.v.setElementAt(this.d[this.g], this.t[this.g]);
                this.d[this.g].J = true;
                this.b(this.g, false);
                this.i = (byte)(this.i + 1);
                return;
            }
            case 20: {
                this.h = (b)this.v.elementAt(this.i);
                for (by = 0; by < this.d.length; by = (byte)(by + 1)) {
                    if (this.d[by].r() != 1 || !this.d[by].S()) continue;
                    this.S.b(this.d[by], false);
                    this.S.b(this.d[by]);
                }
                this.b(this.e[this.i], true);
                this.S.c(this.h);
                if (this.d[0].S()) {
                    this.S.b(this.h, this.d[0]);
                    return;
                }
                this.S.b(this.h, this.d[1]);
                return;
            }
            case 1: {
                if (this.i >= this.v.size()) {
                    this.i = 0;
                }
                this.h = (b)this.v.elementAt(this.i);
                while (this.h.J || !this.h.S()) {
                    this.i = (byte)(this.i + 1);
                    if (this.h.J) {
                        this.h.J = false;
                    }
                    if (this.i >= this.v.size()) {
                        this.w = true;
                        this.i = 0;
                        break;
                    }
                    this.h = (b)this.v.elementAt(this.i);
                }
                if (!this.h.p(2) || this.h.r() != 0) break;
                by = 0;
                for (int i2 = 0; i2 < this.h.y.length; ++i2) {
                    if (this.h.y[i2] == 0) continue;
                    by = 1;
                }
                if (by == 0) {
                    this.S.c("Kh\u00f4ng c\u00f2n tinh l\u1ef1c, kh\u00f4ng c\u00e1ch n\u00e0o chi\u1ebfn \u0111\u1ea5u");
                    this.i = (byte)(this.i + 1);
                    if (this.i >= this.v.size()) {
                        this.w = true;
                        this.i = 0;
                        return;
                    }
                }
                return;
            }
            case 12: 
            case 13: {
                int n2;
                if (this.h.r() == 0) {
                    this.S.a(this.h, false);
                    this.S.a(this.h);
                } else {
                    this.S.b(this.h, false);
                    this.S.b(this.h);
                }
                b b2 = (b)this.v.elementAt(this.i);
                if (b2.m(13) || b2.m(14)) {
                    b2.C();
                }
                this.af = new byte[b2.r(0) + b2.r(1)][];
                this.ag = new byte[this.af.length << 1];
                this.ah = new byte[this.af.length];
                int n3 = 0;
                for (n2 = 0; n2 < 3; ++n2) {
                    if (b2.x[0][n2] == -1) continue;
                    this.ah[n3] = (byte)n2;
                    this.af[n3] = ap[ar[0][b2.x[0][n2]]];
                    this.ag[n3 << 1] = 0;
                    this.ag[(n3 << 1) + 1] = b2.x[0][n2];
                    ++n3;
                }
                for (n2 = 0; n2 < 3; ++n2) {
                    if (b2.x[1][n2] == -1) continue;
                    this.ah[n3] = (byte)n2;
                    this.af[n3] = aq[ar[1][b2.x[1][n2]]];
                    this.ag[n3 << 1] = 1;
                    this.ag[(n3 << 1) + 1] = b2.x[1][n2];
                    ++n3;
                }
                this.ab = 0;
                this.ac = 0;
                if (this.a(b2)) break;
                this.o();
                return;
            }
            case 2: {
                return;
            }
            case 7: {
                if (this.h.r() == 0) {
                    this.S.a(this.h, false);
                    this.S.a(this.h);
                } else {
                    this.S.b(this.h, false);
                    this.S.b(this.h);
                }
                if (((b)this.h.p).r() == 1) {
                    this.S.b((b)this.h.p, false);
                    this.S.b((b)this.h.p);
                } else {
                    this.S.a((b)this.h.p, false);
                    this.S.a((b)this.h.p);
                }
                this.z = false;
                this.A = false;
                this.n();
                d d2 = this;
                if (d2.h.r() != ((b)d2.h.p).r() || d2.h.p(8)) {
                    this.Z = this.h.b((b)this.h.p);
                }
                switch (this.h.D) {
                    case 52: 
                    case 58: {
                        if (ae.a(100) > 30) {
                            this.aa = false;
                            break;
                        }
                    }
                    default: {
                        this.aa = true;
                    }
                }
                if (this.O[this.J * 7] == 0) {
                    this.h.d((byte)1);
                    return;
                }
                this.h.d((byte)0);
                return;
            }
            case 3: {
                this.S.e((b)this.v.elementAt(this.i));
                return;
            }
            case 4: {
                this.S.aj();
                return;
            }
            case 6: {
                this.C = 0;
                this.S.b((b)this.h.G.elementAt(this.C), false);
                this.S.b(this.h, (b)this.h.G.elementAt(this.C));
                this.S.b((b)this.h.G.elementAt(this.C));
                this.a(Integer.parseInt((String)this.h.H.elementAt(this.C)), true);
                return;
            }
            case 16: {
                this.S.c = 0;
                this.S.l = false;
                this.S.W();
                return;
            }
            case 5: {
                this.S.c = 0;
                this.S.W();
                return;
            }
            case 17: {
                b b3 = this.d[0];
                this.h.p = b3;
                if (this.aj == null) {
                    this.aj = new f();
                    this.aj.a(269, false);
                }
                this.aj.b(this.h.i, this.h.j);
                this.aj.c();
                this.e((byte)0);
                this.ak = false;
                int n4 = this.b((int)l);
                this.ak = ae.a(100) < n4;
                if (U == 0 && V == 5) {
                    this.ak = false;
                }
                this.S.f = 0;
                return;
            }
            case 18: {
                return;
            }
            case 21: {
                b b4 = this.d[0];
                this.h.p = b4;
                this.S.ah();
                return;
            }
            case 10: {
                return;
            }
            case 8: {
                game.k.a().M.l = 0;
                for (int i3 = 0; i3 < x.size(); ++i3) {
                    by = (byte)(((b)game.d.x.elementAt((int)i3)).A - ((b)game.d.x.elementAt((int)i3)).d[1]);
                    by = X ? (byte)(by % 20 / 100) : (byte)(by % 50 / 100);
                    if (by <= 0) continue;
                    ((b)x.elementAt(i3)).l(by);
                    ((b)x.elementAt(i3)).u(((b)game.d.x.elementAt((int)i3)).d[1]);
                }
                this.al[0].b(am[0][6], am[0][7]);
                this.S.a((int)am[0][4], (int)am[0][5]);
                return;
            }
            case 22: {
                this.S.an();
                return;
            }
            case 9: {
                d d3 = this;
                if (game.k.a().M.i) {
                    d3.a((byte)24);
                } else {
                    for (int i4 = 0; i4 < game.d.p.A; ++i4) {
                        game.d.p.z[i4].l(1);
                        game.d.p.z[i4].u(1);
                        game.d.p.z[i4].c();
                    }
                    game.i.a().a((byte)10);
                }
                game.k.a().M.l = 1;
                game.k.a().M.i = true;
                return;
            }
            case 11: {
                this.S.a(4, (byte)0);
                return;
            }
            case 14: {
                return;
            }
            case 23: {
                this.S.ap();
                return;
            }
            case 24: {
                this.S.aE();
                return;
            }
            case 102: {
                this.S.aJ();
                return;
            }
            case 104: {
                this.S.aI();
                return;
            }
            case 101: {
                this.S.aH();
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    public final void b() {
        if (!this.Y) {
            return;
        }
        this.S();
        switch (this.P) {
            case 0: {
                this.F = (byte)(this.F + 1);
                this.D[this.G] = this.E[this.G];
                this.d[this.G].b(game.d.an[this.r][this.G][this.E[this.G] << 2], game.d.an[this.r][this.G][(this.E[this.G] << 2) + 1]);
                this.al[this.G].b(game.d.an[this.r][this.G][(this.E[this.G] << 2) + 2], game.d.an[this.r][this.G][(this.E[this.G] << 2) + 3]);
                if (this.a == 1 && this.E[this.G] > game.d.an[this.r][this.G].length / 4 - 3 && this.G % 2 == 0 && this.E.length > this.G + 1) {
                    v0 = this.G + 1;
                    this.E[v0] = (byte)(this.E[v0] + 1);
                    this.D[this.G + 1] = this.E[this.G + 1];
                    this.d[this.G + 1].b(game.d.an[this.r][this.G + 1][this.E[this.G + 1] << 2], game.d.an[this.r][this.G + 1][(this.E[this.G + 1] << 2) + 1]);
                    this.al[this.G + 1].b(game.d.an[this.r][this.G + 1][(this.E[this.G + 1] << 2) + 2], game.d.an[this.r][this.G + 1][(this.E[this.G + 1] << 2) + 3]);
                }
                if (this.a == 0) {
                    this.S.a(this.d[1], this.d[0], this.d[this.G], this.E[this.G] + 1, game.d.an[this.r][this.G].length / 4);
                }
                if (this.F > 1) {
                    v1 = this.G;
                    this.E[v1] = (byte)(this.E[v1] + 1);
                    this.F = 0;
                }
                if (this.E[this.G] <= game.d.an[this.r][this.G].length / 4 - 1) break;
                this.E[this.G] = (byte)(game.d.an[this.r][this.G].length / 4 - 1);
                this.D[this.G] = this.E[this.G];
                ++this.G;
                if (this.G <= this.d.length - 1) break;
                this.G = this.d.length - 1;
                this.a((byte)20);
                break;
            }
            case 15: {
                if (this.a == 0) {
                    this.S.a(this.d[1], this.d[0], this.E[this.G] + 1, game.d.an[this.r][this.G].length / 4);
                }
                if (this.F > 0) {
                    v2 = this.G;
                    this.E[v2] = (byte)(this.E[v2] + 1);
                    this.F = 0;
                } else {
                    this.F = (byte)(this.F + 1);
                    if (this.E[this.G] > game.d.an[this.r][this.G].length / 4 - 3) {
                        this.D[this.G] = this.E[this.G];
                    }
                    this.d[this.G].b(game.d.an[this.r][this.G][this.E[this.G] << 2], game.d.an[this.r][this.G][(this.E[this.G] << 2) + 1]);
                }
                if (this.E[this.G] <= game.d.an[this.r][this.G].length / 4 - 1) break;
                this.D[this.G] = this.E[this.G] = (byte)(game.d.an[this.r][this.G].length / 4 - 1);
                var1_1 = true;
                if (!this.k) ** GOTO lbl60
                while (!(this.i >= this.v.size() || ((b)this.v.elementAt(this.i)).S() && ((b)this.v.elementAt(this.i)).r() == 0)) {
                    this.i = (byte)(this.i + 1);
                }
                if (this.i >= this.v.size()) {
                    this.i = 0;
                    this.a((byte)1);
                } else if (game.d.d((b)this.v.elementAt(this.i))) {
                    this.a((byte)13);
                } else {
                    var1_1 = false;
                    this.a((byte)20);
                }
                this.k = false;
                ** GOTO lbl127
lbl60:
                // 1 sources

                if (this.a == 0) {
                    this.i = (byte)this.v.size();
                }
                if (this.i < this.v.size()) ** GOTO lbl107
                for (var2_9 = 0; var2_9 < this.d.length; ++var2_9) {
                    this.d[var2_9].J = false;
                }
                if (this.y) {
                    this.T();
                    this.y = false;
                }
                if (this.Q == 12 || this.Q == 13) {
                    this.i = 0;
                    while (((b)this.v.elementAt(this.i)).r() != 0 || ((b)this.v.elementAt(this.i)).r() == 0 && !((b)this.v.elementAt(this.i)).S()) {
                        this.i = (byte)(this.i + 1);
                    }
                    if (game.d.d((b)this.v.elementAt(this.i))) {
                        this.a((byte)13);
                    } else {
                        var1_1 = false;
                        this.a((byte)20);
                    }
                } else {
                    var2_9 = 0;
                    if (this.h.m(12) && this.h.K[12] == 2) {
                        this.h.K[12] = (short)(this.h.K[12] - 1);
                        if (!((b)this.h.p).S()) {
                            var2_9 = 1;
                            this.h.K[12] = (short)(this.h.K[12] - 1);
                        } else {
                            this.i = (byte)(this.i - 1);
                            this.a((byte)2);
                        }
                    } else {
                        var3_15 = ae.a(100);
                        if ((this.h.D == 63 || this.h.D == 69) && var3_15 <= aq.c[1][this.h.D][8] && ((b)this.h.p).S()) {
                            this.i = (byte)(this.i - 1);
                            this.a((byte)2);
                        } else {
                            var2_9 = 1;
                        }
                    }
                    if (var2_9 != 0) {
                        this.i = 0;
                        while (((b)this.v.elementAt(this.i)).r() != 0 || ((b)this.v.elementAt(this.i)).r() == 0 && !((b)this.v.elementAt(this.i)).S()) {
                            this.i = (byte)(this.i + 1);
                        }
                        if (game.d.d((b)this.v.elementAt(this.i))) {
                            this.a((byte)13);
                        } else {
                            var1_1 = false;
                            this.a((byte)20);
                        }
                    }
                }
                ** GOTO lbl127
lbl107:
                // 1 sources

                if (this.Q != 13 && this.Q != 12) ** GOTO lbl110
                this.a((byte)1);
                ** GOTO lbl127
lbl110:
                // 1 sources

                var2_9 = 0;
                if (!this.h.m(12) || this.h.K[12] != 2) ** GOTO lbl119
                this.h.K[12] = (short)(this.h.K[12] - 1);
                if (((b)this.h.p).S()) ** GOTO lbl116
                this.h.K[12] = (short)(this.h.K[12] - 1);
                ** GOTO lbl-1000
lbl116:
                // 1 sources

                this.i = (byte)(this.i - 1);
                this.a((byte)2);
                ** GOTO lbl125
lbl119:
                // 1 sources

                var3_15 = ae.a(100);
                if ((this.h.D == 63 || this.h.D == 69) && var3_15 <= aq.c[1][this.h.D][8] && ((b)this.h.p).S()) {
                    this.i = (byte)(this.i - 1);
                    this.a((byte)2);
                } else lbl-1000:
                // 2 sources

                {
                    var2_9 = 1;
                }
lbl125:
                // 3 sources

                if (var2_9 != 0) {
                    this.a((byte)1);
                }
lbl127:
                // 9 sources

                if (!var1_1) break;
                this.e();
                if (this.d[this.g].r() == 0) {
                    this.S.a(this.d[this.g], false);
                    this.S.a(this.d[this.g]);
                    break;
                }
                this.S.b(this.d[this.g], false);
                this.S.b(this.d[this.g]);
                break;
            }
            case 1: {
                if (this.S.ay()) {
                    if (this.h.r() == 1 && this.b == 0 && (this.h.q() == 33 || this.h.q() == 59) && this.h.d[1] < this.h.c[1]) {
                        this.a((byte)10);
                        return;
                    }
                    if (this.w) {
                        if (this.y) {
                            this.T();
                            this.i = 0;
                            this.y = false;
                        }
                        this.i = 0;
                        while (((b)this.v.elementAt(this.i)).r() != 0 || ((b)this.v.elementAt(this.i)).r() == 0 && !((b)this.v.elementAt(this.i)).S()) {
                            this.i = (byte)(this.i + 1);
                        }
                        if (game.d.d((b)this.v.elementAt(this.i))) {
                            this.a((byte)13);
                        } else {
                            this.a((byte)20);
                        }
                        this.w = false;
                    } else if (this.h.r() == 1) {
                        if (game.d.d(this.h)) {
                            this.a((byte)12);
                        } else {
                            this.a((byte)2);
                        }
                    } else {
                        this.a((byte)2);
                    }
                }
                this.S.g();
                this.al[this.d.length].a();
                break;
            }
            case 20: {
                this.al[this.d.length].a();
                this.S.d(this.h);
                break;
            }
            case 12: 
            case 13: {
                var1_2 = this;
                var2_10 = (b)var1_2.v.elementAt(var1_2.i);
                if (var2_10.u == null) ** GOTO lbl194
                if (!var2_10.u.i()) ** GOTO lbl193
                if (!var2_10.u.a.e()) ** GOTO lbl190
                var2_10.u.b();
                var2_10.u = null;
                if (var1_2.ab <= var1_2.ae.length / 4 - 1) ** GOTO lbl187
                if (var1_2.H == null) {
                    var1_2.A = true;
                }
                ** GOTO lbl194
lbl187:
                // 1 sources

                var1_2.o();
                if (var2_10.u == null) ** GOTO lbl194
                ** GOTO lbl193
lbl190:
                // 1 sources

                if (var1_2.ae[(var1_2.ac << 2) + 3] != -1 && var2_10.u.a((int)var1_2.ae[(var1_2.ac << 2) + 3]) && var1_2.ab < var1_2.ae.length / 4) {
                    var1_2.o();
                }
                ** GOTO lbl194
lbl193:
                // 2 sources

                var2_10.u.a();
lbl194:
                // 5 sources

                if (var1_2.H != null && !var1_2.H.i()) {
                    var1_2.H.a();
                    var2_10.b(false);
                }
                if (var1_2.H != null && var1_2.H.i() && !var1_2.H.e()) {
                    var1_2.H = null;
                    var2_10.b(true);
                    if (var1_2.ab > var1_2.ae.length / 4 - 1) {
                        var1_2.A = true;
                    } else {
                        var1_2.o();
                    }
                }
                if (!var1_2.A) break;
                var3_16 = var2_10;
                var2_10 = var1_2;
                var4_19 = false;
                var5_20 = var3_16.N();
                var6_21 = 0;
                if (!var2_10.aE) {
                    if (var2_10.ag[var2_10.ad << 1] == 0) {
                        var6_21 = var3_16.o(var2_10.ag[(var2_10.ad << 1) + 1]);
                        var3_16.d(var2_10.ag[(var2_10.ad << 1) + 1], var2_10.ah[var2_10.ad]);
                    } else if (var2_10.ag[var2_10.ad << 1] == 1) {
                        var3_16.q(var2_10.ag[(var2_10.ad << 1) + 1]);
                        var3_16.c(var2_10.ag[(var2_10.ad << 1) + 1], var2_10.ah[var2_10.ad]);
                    }
                    if (var3_16.d[1] < var5_20) {
                        super.a("" + (var3_16.d[1] - var5_20), (byte)0, 0, var3_16.r(), var3_16.i, var3_16.j, 9, 12);
                    }
                    if (var6_21 > 0) {
                        super.a("+" + var6_21, (byte)0, 2, var3_16.r(), var3_16.i, var3_16.j, 9, 12);
                    }
                    var2_10.aE = true;
                    var2_10.S.k = 0;
                    if (var3_16.r() == 0) {
                        var2_10.S.a(var3_16);
                    } else {
                        var2_10.S.b(var3_16);
                    }
                }
                var6_21 = super.V();
                if (var3_16.r() == 0) {
                    if (var5_20 < var3_16.d[1]) {
                        if (var2_10.S.a(var3_16, true) && var6_21 != 0) {
                            var2_10.aE = false;
                            var4_19 = true;
                            super.c(var3_16);
                        }
                    } else if (var2_10.S.a(var3_16, false) && var6_21 != 0) {
                        var2_10.aE = false;
                        var4_19 = true;
                        super.c(var3_16);
                    }
                } else if (var5_20 < var3_16.d[1]) {
                    if (var2_10.S.b(var3_16, true) && var6_21 != 0) {
                        var2_10.aE = false;
                        var4_19 = true;
                        super.c(var3_16);
                    }
                } else if (var2_10.S.b(var3_16, false) && var6_21 != 0) {
                    var2_10.aE = false;
                    var4_19 = true;
                    super.c(var3_16);
                }
                if (!var4_19) break;
                var1_2.A = false;
                break;
            }
            case 2: {
                if (this.h.r() == 1) {
                    var1_3 = (byte)game.d.e(this.h);
                    if (this.h.p(9)) {
                        this.f(this.h);
                    } else {
                        var2_11 = 1;
                        if (game.b.b(var1_3, (byte)9) == 0 && this.h.p(8) && ae.a(100) > aq.a((byte)1, var1_3, (byte)8)) {
                            this.f(this.h);
                            var2_11 = 0;
                        }
                        if (var2_11 != 0) {
                            this.b(var1_3);
                        }
                    }
                    var2_11 = ae.a(this.h.G.size());
                    var3_17 = (b)this.h.G.elementAt(var2_11);
                    this.h.I = Byte.parseByte((String)this.h.H.elementAt(var2_11));
                    this.S.b(this.h, var3_17);
                    this.h.a(var1_3, var3_17);
                    this.a((byte)7);
                    break;
                }
                if (this.h.p(9)) {
                    this.f(this.h);
                    var1_4 = ae.a(this.h.G.size());
                    var2_12 = (b)this.h.G.elementAt(var1_4);
                    this.h.I = Byte.parseByte((String)this.h.H.elementAt(var1_4));
                    var3_18 = (byte)game.d.e(this.h);
                    this.h.a(var3_18, var2_12);
                    this.S.b(this.h, var2_12);
                    this.a((byte)7);
                    break;
                }
                var1_5 = true;
                if (game.b.b(this.h.H(), (byte)9) == 0 && this.h.p(8) && ae.a(100) > aq.a((byte)1, this.h.H(), (byte)8)) {
                    this.f(this.h);
                    var1_5 = false;
                }
                if (var1_5) {
                    if ((b)this.h.p != null && !((b)this.h.p).S()) {
                        for (var2_13 = 0; var2_13 < this.h.G.size(); ++var2_13) {
                            if (!((b)this.h.G.elementAt(var2_13)).S()) continue;
                            this.h.I = Byte.parseByte((String)this.h.H.elementAt(var2_13));
                            this.h.a(this.h.H(), (b)this.h.G.elementAt(var2_13));
                        }
                    } else {
                        this.h.a(this.h.H(), (b)this.h.p);
                    }
                }
                this.S.b(this.h, (b)this.h.p);
                this.a((byte)7);
                break;
            }
            case 7: {
                if (this.h.u != null) {
                    if (this.h.p() == 0) {
                        if (this.h.u.i()) {
                            if (this.h.u.a.e()) {
                                this.h.u.b();
                                this.h.u = null;
                                if (this.I > this.O.length / 7 - 1 || this.W()) {
                                    this.A = true;
                                    this.B = true;
                                } else {
                                    this.n();
                                    if (this.h.u != null) {
                                        this.h.u.a();
                                    }
                                    if (this.H != null) {
                                        this.M = true;
                                    }
                                }
                            } else if (this.O[this.J * 7 + 4] != -1 && this.h.u.a((int)this.O[this.J * 7 + 4])) {
                                this.h.u.b();
                                if (this.I < this.O.length / 7 - 1 || this.W()) {
                                    this.n();
                                    if (this.H != null) {
                                        this.M = true;
                                    }
                                }
                            } else if (this.O[this.J * 7 + 5] != -1 && this.h.u.a((int)this.O[this.J * 7 + 5])) {
                                this.h.d(this.O[this.J * 7 + 6]);
                            }
                        } else {
                            this.N = false;
                            this.h.u.a();
                        }
                    } else if (this.h.p() == 1 && this.h.a.e()) {
                        this.h.d((byte)0);
                    }
                } else if (((b)this.h.p).u != null) {
                    if (((b)this.h.p).u.i()) {
                        if (((b)this.h.p).u.d()) {
                            ((b)this.h.p).u.b();
                            if (this.I > this.O.length / 7 - 1 || this.W()) {
                                ((b)this.h.p).d((byte)2);
                            } else if (this.O[this.I * 7] == 1) {
                                ((b)this.h.p).d((byte)2);
                            } else {
                                this.K = 0;
                                ((b)this.h.p).u = null;
                                this.n();
                                if (((b)this.h.p).u != null) {
                                    ((b)this.h.p).u.a();
                                }
                                if (this.H != null) {
                                    this.M = true;
                                }
                            }
                        } else {
                            if (this.O[this.J * 7 + 5] != -1) {
                                this.K = this.J;
                            }
                            if (this.O[this.K * 7 + 5] != -1 && ((b)this.h.p).u.a((int)this.O[this.K * 7 + 5])) {
                                ((b)this.h.p).d(this.O[this.K * 7 + 6]);
                                this.K = 0;
                            }
                            if (this.O[this.J * 7 + 4] != -1 && ((b)this.h.p).u.a((int)this.O[this.J * 7 + 4])) {
                                this.n();
                                if (this.H != null) {
                                    this.M = true;
                                }
                            }
                        }
                    } else if (this.h.p() == 1 && this.h.b() || this.N) {
                        this.h.d((byte)0);
                        ((b)this.h.p).u.a();
                        this.N = false;
                    } else if (((b)this.h.p).p() == 2 && ((b)this.h.p).b()) {
                        this.z = true;
                        ((b)this.h.p).u = null;
                        if (this.I > this.O.length / 7 - 1 || this.W()) {
                            this.A = true;
                        } else {
                            this.n();
                            if (this.H != null) {
                                this.M = true;
                            }
                        }
                    }
                }
                if (this.H != null && !this.H.i() && (this.h.p() == 1 && this.h.b() || this.M || this.h.p() == 0)) {
                    if (this.J == 0) {
                        this.N = true;
                    }
                    this.h.d((byte)0);
                    this.H.a();
                    this.L = this.J;
                    if (this.O[this.J * 7] == 0) {
                        ((b)this.h.p).b(false);
                    } else {
                        this.h.b(false);
                    }
                }
                if (this.H != null && this.H.i() && !this.H.e()) {
                    this.H = null;
                    this.M = false;
                    if (this.O[this.L * 7] == 0) {
                        ((b)this.h.p).b(true);
                    } else {
                        this.h.b(true);
                    }
                    if (((b)this.h.p).u == null && this.h.u == null) {
                        if (this.I > this.O.length / 7 - 1 || this.W()) {
                            if (this.O[this.J * 7] == 0) {
                                this.z = true;
                            }
                            this.B = true;
                            this.A = true;
                        } else {
                            if (this.O[this.I * 7] == 1) {
                                this.z = true;
                            }
                            this.n();
                            if (this.H != null) {
                                this.M = true;
                            }
                        }
                    }
                    this.L = 0;
                }
                if (!this.z) ** GOTO lbl465
                this.B = false;
                var1_6 = this;
                if (aq.c[1][var1_6.h.D][3] != 0) ** GOTO lbl416
                var1_6.S.k = 0;
                if (((b)var1_6.h.p).r() == 0) {
                    var1_6.S.a((b)var1_6.h.p);
                } else {
                    var1_6.S.b((b)var1_6.h.p);
                }
                ((b)var1_6.h.p).d((byte)0);
                v3 = true;
                ** GOTO lbl462
lbl416:
                // 1 sources

                if (((b)var1_6.h.p).p() != 3) ** GOTO lbl419
                v3 = true;
                ** GOTO lbl462
lbl419:
                // 1 sources

                if (!var1_6.aE) {
                    var2_14 = var1_6.h.s();
                    if (var1_6.h.r() == 0 && game.d.p.c((byte)4, (byte)0) == 2 && game.d.p.c((byte)4, (byte)1) == 1) {
                        var2_14 += aq.c[2][4][6];
                    }
                    var2_14 = var1_6.h.m(4) != false ? ((b)var1_6.h.p).s() - (var2_14 - var1_6.h.w[4][1]) << 1 : ((b)var1_6.h.p).s() - var2_14 << 1;
                    if (var1_6.h.f((byte)9)) {
                        var2_14 = 0;
                    }
                    if (var2_14 <= 0) {
                        var2_14 = 0;
                    } else if (var2_14 >= 20) {
                        var2_14 = 20;
                    }
                    if (ae.a(100) >= var2_14) {
                        ((b)var1_6.h.p).k(var1_6.Z[0]);
                        if (var1_6.h.f((byte)10) && ((b)var1_6.h.p).a((byte)1) <= aq.c[3][10][5]) {
                            ((b)var1_6.h.p).a((byte)1, aq.c[3][10][5]);
                        }
                        if (var1_6.Z[1] == 1) {
                            var1_6.a("-" + var1_6.Z[0], (byte)0, 1, ((b)var1_6.h.p).r(), ((b)var1_6.h.p).l(), ((b)var1_6.h.p).m(), 15, 19);
                        } else {
                            var1_6.a("-" + var1_6.Z[0], (byte)0, 0, ((b)var1_6.h.p).r(), ((b)var1_6.h.p).l(), ((b)var1_6.h.p).m(), 9, 12);
                        }
                        if (var1_6.Z[2] != -1) {
                            var1_6.a(an.f(aq.c[7][var1_6.Z[2]][0]), (byte)1, 0, ((b)var1_6.h.p).r(), ((b)var1_6.h.p).l(), ((b)var1_6.h.p).m(), 9, 12);
                        }
                    } else {
                        var1_6.a("N\u00e9 tr\u00e1nh", (byte)1, 0, ((b)var1_6.h.p).r(), ((b)var1_6.h.p).l(), ((b)var1_6.h.p).m(), 9, 12);
                    }
                    var1_6.aE = true;
                    var1_6.S.k = 0;
                    if (((b)var1_6.h.p).r() == 0) {
                        var1_6.S.a((b)var1_6.h.p);
                    } else {
                        var1_6.S.b((b)var1_6.h.p);
                    }
                }
                var2_14 = var1_6.V();
                if (((b)var1_6.h.p).r() != 0) ** GOTO lbl456
                if (!var1_6.S.a((b)var1_6.h.p, false) || var2_14 == 0) ** GOTO lbl-1000
                var1_6.U();
                var1_6.aE = false;
                v3 = true;
                ** GOTO lbl462
lbl456:
                // 1 sources

                if (var1_6.S.b((b)var1_6.h.p, false) && var2_14 != 0) {
                    var1_6.U();
                    var1_6.aE = false;
                    v3 = true;
                } else lbl-1000:
                // 2 sources

                {
                    v3 = false;
                }
lbl462:
                // 5 sources

                if (v3 && (((b)this.h.p).S() || this.c((b)this.h.p, true))) {
                    this.B = true;
                    this.z = false;
                }
lbl465:
                // 4 sources

                if (!this.B || !this.A || !this.q()) break;
                this.B = false;
                this.A = false;
                break;
            }
            case 3: {
                this.S.f((b)this.v.elementAt(this.i));
                break;
            }
            case 4: {
                this.S.ak();
                break;
            }
            case 17: {
                if (this.S.f == 0) {
                    if (this.q == 0 && this.aj.b()) {
                        this.e((byte)1);
                    } else if (this.q == 1 && this.aj.b()) {
                        if (!this.H.e()) {
                            this.e((byte)2);
                        }
                    } else if (this.q == 2 && this.aj.b()) {
                        if (this.ak) {
                            this.e((byte)3);
                        } else {
                            this.e((byte)4);
                        }
                    } else if (this.q == 3 && this.aj.b()) {
                        var1_7 = game.d.p.y();
                        if (var1_7 == 0) {
                            this.S.f = 1;
                            this.S.b("B\u1eaft th\u00e0nh c\u00f4ng #2" + an.f(aq.c[0][((b)this.h.p).q()][0]));
                            game.d.p.a(((b)this.h.p).P());
                        } else if (var1_7 == 1) {
                            this.S.f = 2;
                            this.S.b("B\u1eaft th\u00e0nh c\u00f4ng #2" + an.f(aq.c[0][((b)this.h.p).q()][0]));
                            game.d.p.b(((b)this.h.p).P());
                        } else {
                            this.S.f = 1;
                            this.S.b("Kh\u00f4ng c\u00f2n kh\u00f4ng gian, s\u1ee7ng v\u1eadt n\u00e0y \u0111\u00e3 ph\u00f3ng sinh");
                        }
                    } else if (this.q == 4 && this.aj.b() && !this.H.e()) {
                        this.H = null;
                        this.d[0].b(true);
                        this.aj.d();
                        this.h.J = true;
                        if (this.ak) {
                            this.S.b("Ng\u00e2n h\u00e0ng v\u00e0 Ba l\u00f4 \u0111\u1ec1u \u0111\u00e3 \u0111\u1ea7y");
                            this.S.f = 3;
                        } else {
                            this.i = (byte)(this.i + 1);
                            this.a((byte)1);
                        }
                    }
                    this.aj.a();
                    break;
                }
                if (this.S.ax()) {
                    if (this.S.f == 3) {
                        this.S.f = 0;
                        this.h.J = true;
                        this.i = (byte)(this.i + 1);
                        this.a((byte)1);
                    } else if (this.S.f == 2) {
                        this.S.b("S\u1ee7ng v\u1eadt ba l\u00f4 \u0111\u00e3 \u0111\u1ee7, \u0111\u00e3 \u0111\u1ec3 v\u00e0o ng\u00e2n h\u00e0ng");
                        this.S.f = 4;
                    } else if (this.S.f == 4 || this.S.f == 1) {
                        this.S.f = 0;
                        game.k.a().M.l = (byte)-1;
                        this.l();
                        game.i.a().a((byte)10);
                    }
                }
                this.S.f();
                break;
            }
            case 16: {
                this.S.al();
                break;
            }
            case 5: {
                this.S.X();
                break;
            }
            case 18: {
                break;
            }
            case 19: {
                if (!this.k(786432)) break;
                this.a((byte)18);
                break;
            }
            case 6: {
                if (this.k(4100)) {
                    if (this.a == 1) {
                        this.C = (byte)(this.C - 1);
                        if (this.C <= 0) {
                            this.C = 0;
                        }
                        this.a(Integer.parseInt((String)this.h.H.elementAt(this.C)), true);
                        this.S.b((b)this.h.G.elementAt(this.C), false);
                        this.S.b(this.h, (b)this.h.G.elementAt(this.C));
                        this.S.b((b)this.h.G.elementAt(this.C));
                    }
                } else if (this.k(8448)) {
                    if (this.a == 1) {
                        this.C = (byte)(this.C + 1);
                        if (this.C >= this.h.G.size() - 1) {
                            this.C = (byte)(this.h.G.size() - 1);
                        }
                        this.a(Integer.parseInt((String)this.h.H.elementAt(this.C)), true);
                        this.S.b((b)this.h.G.elementAt(this.C), false);
                        this.S.b(this.h, (b)this.h.G.elementAt(this.C));
                        this.S.b((b)this.h.G.elementAt(this.C));
                    }
                } else if (this.k(16400)) {
                    if (this.a == 1) {
                        this.C = (byte)(this.C - 1);
                        if (this.C <= 0) {
                            this.C = 0;
                        }
                        this.a(Integer.parseInt((String)this.h.H.elementAt(this.C)), true);
                        this.S.b((b)this.h.G.elementAt(this.C), false);
                        this.S.b(this.h, (b)this.h.G.elementAt(this.C));
                        this.S.b((b)this.h.G.elementAt(this.C));
                    }
                } else if (this.k(32832)) {
                    if (this.a == 1) {
                        this.C = (byte)(this.C + 1);
                        if (this.C >= this.h.G.size() - 1) {
                            this.C = (byte)(this.h.G.size() - 1);
                        }
                        this.a(Integer.parseInt((String)this.h.H.elementAt(this.C)), true);
                        this.S.b((b)this.h.G.elementAt(this.C), false);
                        this.S.b(this.h, (b)this.h.G.elementAt(this.C));
                        this.S.b((b)this.h.G.elementAt(this.C));
                    }
                } else if (this.k(196640)) {
                    this.i();
                } else if (this.k(786432)) {
                    this.a(Integer.parseInt((String)this.h.H.elementAt(this.C)), false);
                    this.a((byte)3);
                }
                this.al[this.d.length].a();
                this.al[this.d.length + 1].a();
                break;
            }
            case 21: {
                this.S.ai();
                break;
            }
            case 10: {
                if (!this.S.j() && this.c(this.h, false)) {
                    this.S.c(game.d.f(aq.c[0][this.h.q()][0]) + "Ch\u1ea1y tr\u1ed1n");
                }
                if (!this.S.g()) break;
                game.i.a().a((byte)10);
                break;
            }
            case 8: {
                this.S.am();
                break;
            }
            case 22: {
                this.S.ao();
                break;
            }
            case 9: {
                break;
            }
            case 11: {
                this.S.a((byte)4, (byte)0);
                break;
            }
            case 14: {
                break;
            }
            case 23: {
                this.S.aq();
                break;
            }
            case 24: {
                this.S.aF();
                break;
            }
            case 101: 
            case 102: 
            case 104: {
                this.S.aM();
            }
        }
        for (var1_8 = 0; var1_8 < this.d.length; ++var1_8) {
            this.d[var1_8].o();
        }
        this.R.c();
    }

    private void a(Graphics graphics) {
        graphics.setColor(0xFFFFFF);
        for (int i2 = 0; i2 < this.d.length; ++i2) {
            this.d[i2].a(graphics);
        }
    }

    private void a(Graphics graphics, boolean bl) {
        for (int i2 = 0; i2 < this.d.length; ++i2) {
            this.al[i2].a(graphics, 0, 0);
        }
        if (bl) {
            this.al[this.d.length].a(graphics, 0, 0);
            if (this.a == 1) {
                this.al[this.d.length + 1].a(graphics, 0, 0);
            }
        }
    }

    private void a(String string, byte by, int n2, int n3, int n4, int n5, int n6, int n7) {
        this.ay = string;
        this.as = n4;
        this.at = n5;
        this.aw = n2;
        this.ax = n3;
        this.au = n6;
        this.av = n7;
        this.aC.addElement(this.ay);
        this.aB.addElement(new int[]{by, this.aw, this.ax, -1, this.au, this.av});
    }

    private void c(Graphics graphics) {
        block4: for (int i2 = 0; i2 < this.aB.size(); ++i2) {
            int[] nArray = (int[])this.aB.elementAt(i2);
            String string = (String)this.aC.elementAt(i2);
            switch (nArray[0]) {
                case 0: {
                    if (nArray[2] == 0) {
                        game.d.a(graphics, az[nArray[1]], string, this.as + aA[nArray[0]][nArray[3] << 1] + 30, this.at + aA[nArray[0]][(nArray[3] << 1) + 1] - 30, nArray[4], nArray[5]);
                        continue block4;
                    }
                    game.d.a(graphics, az[nArray[1]], string, this.as - aA[nArray[0]][nArray[3] << 1] - 30, this.at + aA[nArray[0]][(nArray[3] << 1) + 1] - 30, nArray[4], nArray[5]);
                    continue block4;
                }
                case 1: {
                    if (nArray[2] == 0) {
                        ae.a(graphics, string, 16704699, this.as - 10, this.at + aA[nArray[0]][(nArray[3] << 1) + 1] - 30, this.R.b, 2);
                        continue block4;
                    }
                    ae.a(graphics, string, 16704699, this.as + 10, this.at + aA[nArray[0]][(nArray[3] << 1) + 1] - 30, this.R.b, 2);
                }
            }
        }
    }

    public final void b(Graphics graphics) {
        if (!this.Y) {
            return;
        }
        if (this.c != null) {
            graphics.drawImage(this.c, 0, 0, 20);
        } else {
            graphics.setColor(0);
            graphics.fillRect(0, 0, (int)an.w(), (int)an.x());
        }
        switch (this.P) {
            case 0: {
                this.a(graphics, false);
                this.a(graphics);
                break;
            }
            case 12: 
            case 13: {
                this.a(graphics, false);
                if (this.H != null) {
                    this.H.a(graphics);
                }
                this.a(graphics);
                this.c(graphics);
                break;
            }
            case 1: 
            case 10: {
                this.a(graphics, false);
                this.a(graphics);
                break;
            }
            case 20: {
                this.a(graphics, true);
                this.a(graphics);
                break;
            }
            case 2: {
                this.a(graphics, false);
                this.a(graphics);
                break;
            }
            case 7: {
                this.a(graphics, false);
                if (this.H != null) {
                    this.H.a(graphics);
                }
                this.a(graphics);
                this.c(graphics);
                break;
            }
            case 15: {
                this.a(graphics, false);
                this.a(graphics);
                break;
            }
            case 3: {
                graphics.setColor(0xFFFFFF);
                break;
            }
            case 4: {
                graphics.setColor(0xFFFFFF);
                break;
            }
            case 5: {
                graphics.setColor(0xFFFFFF);
                graphics.drawString(game.d.f(aq.c[0][this.c((int)this.f[0]).q()][0]), game.d.w() >> 1, 200, 17);
                break;
            }
            case 17: {
                this.a(graphics, false);
                this.a(graphics);
                if (this.H != null && this.H.c()) {
                    this.H.a(graphics);
                    this.aj.a(graphics, 0, 0);
                    break;
                }
                this.aj.a(graphics, 0, 0);
                break;
            }
            case 18: {
                break;
            }
            case 21: {
                break;
            }
            case 6: {
                if (this.a == 1) {
                    this.a(graphics, true);
                } else {
                    this.a(graphics, false);
                }
                this.a(graphics);
                break;
            }
            case 8: {
                this.al[0].a(graphics, 0, 0);
                if (this.S.i >= j.size()) break;
                ((b)j.elementAt(this.S.i)).a(graphics);
            }
        }
        this.R.a(graphics);
    }

    public final void h() {
        this.i = (byte)(this.i + 1);
        if (this.i < this.v.size()) {
            while (((b)this.v.elementAt(this.i)).r() != 0 || ((b)this.v.elementAt(this.i)).r() == 0 && !((b)this.v.elementAt(this.i)).S()) {
                this.i = (byte)(this.i + 1);
                if (this.i < this.v.size()) continue;
            }
        }
        if (this.i >= this.v.size()) {
            this.a((byte)1);
            return;
        }
        if (game.d.d((b)this.v.elementAt(this.i))) {
            this.a((byte)13);
            return;
        }
        this.a((byte)20);
    }

    private void p() {
        if (this.i >= this.v.size()) {
            if (this.y) {
                this.T();
                this.y = false;
            }
            for (int i2 = 0; i2 < this.v.size(); ++i2) {
                ((b)this.v.elementAt((int)i2)).J = false;
            }
            this.i = 0;
            while (((b)this.v.elementAt(this.i)).r() != 0 || ((b)this.v.elementAt(this.i)).r() == 0 && !((b)this.v.elementAt(this.i)).S()) {
                this.i = (byte)(this.i + 1);
            }
            if (game.d.d((b)this.v.elementAt(this.i))) {
                this.a((byte)13);
                return;
            }
            this.a((byte)20);
            return;
        }
        this.a((byte)1);
    }

    private boolean q() {
        int n2;
        int n3;
        d d2 = this;
        if (!d2.aE) {
            int n4;
            n3 = 0;
            d2.aF = aq.c[1][d2.h.D][9] == 0 ? d2.h.N() : ((b)d2.h.p).N();
            n2 = d2.h.D;
            switch (n2) {
                case 11: 
                case 17: {
                    n3 = (short)(d2.h.B() * aq.c[1][n2][8] / 100);
                    d2.h.u(d2.h.d[1]);
                    if (n3 <= 0) {
                        n3 = 1;
                    }
                    d2.h.l(n3);
                    break;
                }
                case 21: 
                case 27: 
                case 42: 
                case 48: 
                case 62: 
                case 68: {
                    d2.h.a((byte)aq.c[1][n2][7], -1, n2);
                    break;
                }
                case 52: 
                case 58: {
                    if (d2.aa) {
                        n3 = d2.Z[0] * aq.c[1][n2][8] / 100;
                        d2.h.u(d2.h.d[1]);
                        d2.h.l(d2.Z[0] * aq.c[1][n2][8] / 100);
                    }
                }
                case 64: {
                    d2.h.a((byte)aq.c[1][n2][7], (int)d2.h.I, n2);
                    break;
                }
                default: {
                    if (aq.c[1][n2][6] != 1) break;
                    n3 = ((b)d2.h.p).a((byte)aq.c[1][n2][7], -1, n2);
                }
            }
            byte by = (byte)aq.c[1][n2][6];
            if (aq.c[1][d2.h.D][9] == 0) {
                if (d2.h.f((byte)8) && ae.a(100) <= aq.c[3][8][5]) {
                    d2.h.u(d2.h.d[1]);
                    d2.h.l((short)(d2.Z[0] * aq.c[3][8][6] / 100));
                }
                if (((b)d2.h.p).m(2)) {
                    n4 = d2.Z[0] * ((b)d2.h.p).v[2][2] / 100;
                    d2.h.k(n4);
                }
                if (((b)d2.h.p).m(5) && (n4 = d2.h.K[5]) > 0) {
                    d2.h.k(n4);
                    d2.h.K[5] = 0;
                }
                if (d2.h.d[1] < d2.aF) {
                    d2.a("" + (d2.h.d[1] - d2.aF), (byte)0, 0, d2.h.r(), d2.h.i, d2.h.j, 9, 12);
                } else if (n3 > 0) {
                    d2.a("+" + n3, (byte)0, 2, d2.h.r(), d2.h.i, d2.h.j, 9, 12);
                }
            } else if (n3 > 0) {
                d2.a("+" + n3, (byte)0, 2, ((b)d2.h.p).r(), ((b)d2.h.p).i, ((b)d2.h.p).j, 9, 12);
            }
            if (by == 1) {
                n4 = aq.c[1][n2][7];
                switch (n2) {
                    case 21: 
                    case 27: 
                    case 42: 
                    case 48: 
                    case 62: 
                    case 64: 
                    case 68: {
                        d2.a(an.f(aq.c[6][n4][0]), (byte)1, 2, d2.h.r(), d2.h.i, d2.h.j, 9, 12);
                        break;
                    }
                    default: {
                        d2.a(an.f(aq.c[6][n4][0]), (byte)1, 2, ((b)d2.h.p).r(), ((b)d2.h.p).i, ((b)d2.h.p).j, 9, 12);
                    }
                }
            }
            if (aq.c[1][d2.h.D][9] == 0) {
                d2.S.k = 0;
                if (d2.h.r() == 0) {
                    d2.S.a(d2.h);
                } else {
                    d2.S.b(d2.h);
                }
            } else {
                d2.S.k = 0;
                if (((b)d2.h.p).r() == 0) {
                    d2.S.a((b)d2.h.p);
                } else {
                    d2.S.b((b)d2.h.p);
                }
            }
            d2.aE = true;
        }
        n3 = d2.V();
        if (((b)d2.h.p).r() != d2.h.r() || d2.h.p(9) ? d2.a(d2.h, n3 != 0, d2.aF) : d2.a((b)d2.h.p, n3 != 0, d2.aF)) {
            this.I = 0;
            this.J = 0;
            for (n3 = 0; n3 < this.f.length && !this.c((int)this.f[n3]).S(); ++n3) {
            }
            if (((b)this.h.p).r() == 1 && !((b)this.h.p).S() || this.h.r() == 1 && !this.h.S()) {
                ((b)this.h.p).C();
                ((b)this.h.p).D();
                this.S.b((b)this.h.p);
                this.h((b)this.h.p);
                this.u[1] = (byte)(this.u[1] + 1);
            } else if (((b)this.h.p).r() == 0 && !((b)this.h.p).S() || this.h.r() == 0 && !this.h.S()) {
                ((b)this.h.p).C();
                ((b)this.h.p).D();
                this.S.a((b)this.h.p);
                x.removeElement((b)this.h.p);
                j.removeElement((b)this.h.p);
                ((b)this.h.p).B = 0;
                ((b)this.h.p).d(false);
                ((b)this.h.p).F = 0;
            }
            int n5 = n3 >= this.f.length ? 2 : (this.u[1] >= this.s.length ? 1 : 0);
            switch (n5) {
                case 0: {
                    n5 = 0;
                    if (!((b)this.h.p).S() || !this.h.S()) {
                        for (n2 = 0; n2 < this.d.length; ++n2) {
                            if (!this.d[n2].m(11) || !this.d[this.d[n2].v[11][1]].equals((b)this.h.p)) continue;
                            this.d[n2].n(11);
                        }
                        if (((b)this.h.p).r() == 1 && !((b)this.h.p).S() || this.h.r() == 1 && !this.h.S()) {
                            if (this.u[0] < this.s.length) {
                                this.g = this.h.r() == 1 && !this.h.S() ? this.e[this.i] : this.h.I;
                                this.m(this.g);
                                this.a((byte)15);
                            } else {
                                n5 = 1;
                            }
                        } else if (this.r()) {
                            this.g = this.h.r() == 0 && !this.h.S() ? this.e[this.i] : this.h.I;
                            this.a((byte)5);
                        } else {
                            n5 = 1;
                        }
                    } else {
                        n5 = 1;
                    }
                    if (n5 == 0) break;
                    if (this.h.m(12) && this.h.K[12] == 2) {
                        this.h.K[12] = (short)(this.h.K[12] - 1);
                        if (!((b)this.h.p).S()) {
                            this.h.K[12] = (short)(this.h.K[12] - 1);
                            this.i = (byte)(this.i + 1);
                            this.p();
                            break;
                        }
                        this.a((byte)2);
                        break;
                    }
                    n2 = ae.a(100);
                    if ((this.h.D == 63 || this.h.D == 69) && n2 <= aq.c[1][this.h.D][8]) {
                        if (!((b)this.h.p).S()) {
                            this.h.K[12] = (short)(this.h.K[12] - 1);
                        } else {
                            this.a((byte)2);
                            break;
                        }
                    }
                    this.i = (byte)(this.i + 1);
                    this.p();
                    break;
                }
                case 1: {
                    game.d.X();
                    this.a((byte)8);
                    break;
                }
                case 2: {
                    this.a((byte)9);
                }
            }
            if (p.c((byte)5, (byte)0) == 2 && p.c((byte)5, (byte)1) == 1) {
                for (n5 = 0; n5 < this.d.length; ++n5) {
                    if (this.d[n3].r() != 0 || !this.d[n3].S()) continue;
                    this.d[n3].y();
                }
            }
            return true;
        }
        return false;
    }

    private static boolean d(b b2) {
        return b2.r(0) > 0 || b2.r(1) > 0;
    }

    private static int e(b b2) {
        byte by = b2.z[0];
        int[] nArray = new int[]{50, 20, 15, 10, 5, 5, 5, 5, 5, 5};
        int n2 = ae.a(100);
        for (int i2 = 0; i2 < b2.z.length; ++i2) {
            if (b2.z[i2] == -1 || b2.y[i2] <= 0 || n2 >= nArray[i2]) continue;
            by = b2.z[i2];
        }
        return by;
    }

    public final void b(byte by) {
        this.h.G.removeAllElements();
        this.h.H.removeAllElements();
        switch (game.b.b(by, (byte)9)) {
            case 1: {
                for (by = 0; by < this.d.length; by = (byte)(by + 1)) {
                    if (this.d[by].r() != this.h.r() || !this.d[by].S()) continue;
                    this.h.G.addElement(this.d[by]);
                    this.h.H.addElement("" + by);
                }
                return;
            }
            case 0: {
                for (by = 0; by < this.d.length; by = (byte)(by + 1)) {
                    if (this.d[by].r() == this.h.r() || !this.d[by].S()) continue;
                    this.h.G.addElement(this.d[by]);
                    this.h.H.addElement("" + by);
                }
                break;
            }
        }
    }

    private void f(b b2) {
        b2.G.removeAllElements();
        b2.H.removeAllElements();
        for (int i2 = 0; i2 < this.d.length; ++i2) {
            if (!this.d[i2].S() || this.d[i2].equals(b2)) continue;
            b2.G.addElement(this.d[i2]);
            b2.H.addElement("" + i2);
        }
    }

    public final void i() {
        b b2 = (b)this.h.G.elementAt(this.C);
        this.h.p = b2;
        this.h.I = Byte.parseByte((String)this.h.H.elementAt(this.C));
        this.h.h(((b)this.v.elementAt((int)this.i)).z[this.S.e]);
        this.a(Integer.parseInt((String)this.h.H.elementAt(this.C)), false);
        this.h();
    }

    private int g(b b2) {
        if (!b2.S()) {
            if (b2.r() == 0) {
                if (this.r()) {
                    return 1;
                }
            } else if (this.u[0] < this.s.length) {
                return 2;
            }
        }
        return 0;
    }

    private boolean r() {
        int n2 = 0;
        for (int i2 = 0; i2 < game.d.p.A; ++i2) {
            if (!this.c(i2).S() || this.c(i2).K()) continue;
            ++n2;
        }
        return n2 > 0;
    }

    private void e(int n2, int n3) {
        byte by = this.f[n3];
        this.f[n3] = this.f[n2];
        this.f[n2] = by;
    }

    public final int a(int n2) {
        if (!this.c((int)this.f[n2]).S()) {
            return 0;
        }
        if (this.c((int)this.f[n2]).K()) {
            return 1;
        }
        byte by = this.f[n2];
        --n2;
        while (n2 >= 0) {
            this.f[n2 + 1] = this.f[n2];
            --n2;
        }
        this.f[0] = by;
        for (n2 = 0; n2 < x.size() && !x.elementAt(n2).equals(this.c((int)this.f[0])); ++n2) {
        }
        if (n2 >= x.size()) {
            x.addElement(this.c((int)this.f[0]));
        }
        this.c((int)this.f[0]).J = true;
        this.c((int)this.f[0]).d(true);
        this.h.d(false);
        this.h.F = 0;
        for (n2 = 0; n2 < this.d.length; ++n2) {
            if (!this.d[n2].m(11) || !this.d[this.d[n2].v[11][1]].equals(this.h)) continue;
            this.d[n2].n(11);
        }
        return -1;
    }

    private void T() {
        byte by;
        int n2;
        this.v.removeAllElements();
        int n3 = -1;
        for (n2 = 0; n2 < this.t.length - 1; ++n2) {
            for (int i2 = n2 + 1; i2 < this.t.length; ++i2) {
                if (this.d[n2].c[4] >= this.d[i2].c[4]) continue;
                by = this.t[n2];
                this.t[n2] = this.t[i2];
                this.t[i2] = by;
            }
        }
        for (n2 = 0; n2 < this.t.length; ++n2) {
            if (!this.d[n2].f((byte)7)) continue;
            n3 = n2;
            this.t[n2] = 0;
            break;
        }
        if (n3 != -1) {
            int n4;
            n2 = 1;
            int[] nArray = new int[this.t.length - 1];
            for (n4 = 0; n4 < this.t.length; ++n4) {
                if (n4 == n3) continue;
                nArray[n2 - 1] = n4;
                this.t[n4] = (byte)n2;
                ++n2;
            }
            for (n4 = 0; n4 < nArray.length - 1; ++n4) {
                for (n3 = n4 + 1; n3 < nArray.length; ++n3) {
                    if (this.d[nArray[n4]].c[4] >= this.d[nArray[n3]].c[4]) continue;
                    by = this.t[nArray[n4]];
                    this.t[nArray[n4]] = this.t[nArray[n3]];
                    this.t[nArray[n3]] = by;
                }
            }
        }
        for (n2 = 0; n2 < this.t.length; ++n2) {
            this.e[this.t[n2]] = (byte)n2;
        }
        for (n2 = 0; n2 < this.e.length; ++n2) {
            this.v.addElement(this.d[this.e[n2]]);
        }
    }

    private void U() {
        if (this.h.f((byte)10) && ((b)this.h.p).d[1] <= aq.c[3][10][5]) {
            short s2;
            ((b)this.h.p).d[1] = s2 = aq.c[3][10][5];
        }
        if (((b)this.h.p).d[1] <= 0) {
            ((b)this.h.p).d((byte)3);
            return;
        }
        ((b)this.h.p).d((byte)0);
    }

    private boolean c(b b2, boolean bl) {
        if (this.b == 0 && bl) {
            if (b2.L != null && !b2.L.i()) {
                return true;
            }
        } else {
            int n2 = b2.i;
            this.aD = b2.r() == 0 ? (this.aD -= 10) : (this.aD += 10);
            int n3 = n2 + this.aD;
            if (Math.abs(this.aD) >= 100) {
                b2.d();
                this.aD = 0;
                return true;
            }
            b2.b(n3, b2.j);
        }
        return false;
    }

    private boolean V() {
        int n2;
        for (n2 = 0; n2 < this.aB.size(); ++n2) {
            int[] nArray = (int[])this.aB.elementAt(n2);
            int[] nArray2 = nArray;
            nArray[3] = nArray[3] + 1;
            if (nArray2[3] < aA[nArray2[0]].length / 2) continue;
            this.aB.removeElementAt(n2);
            this.aC.removeElementAt(n2);
            --n2;
        }
        return n2 <= 0;
    }

    private boolean W() {
        if (this.Z != null) {
            if (this.Z[2] != -1) {
                return false;
            }
            return this.O[this.O.length - 1] == this.I;
        }
        return !this.aa;
    }

    private boolean a(b b2, boolean bl, int n2) {
        if (b2.r() == 0) {
            if (n2 < b2.d[1]) {
                if (this.S.a(b2, true) && bl) {
                    this.aE = false;
                    return true;
                }
            } else if (this.S.a(b2, false) && bl) {
                this.aE = false;
                return true;
            }
        } else if (n2 < b2.d[1]) {
            if (this.S.b(b2, true) && bl) {
                this.aE = false;
                return true;
            }
        } else if (this.S.b(b2, false) && bl) {
            this.aE = false;
            return true;
        }
        return false;
    }

    private static void X() {
        int n2;
        for (n2 = 0; n2 < j.size(); ++n2) {
            if (((b)j.elementAt(n2)).S()) {
                ((b)j.elementAt(n2)).g(((b)game.d.j.elementAt((int)n2)).B);
                ((b)game.d.j.elementAt((int)n2)).B = 0;
                ((b)j.elementAt(n2)).d(false);
                continue;
            }
            j.removeElementAt(n2);
            --n2;
        }
        if (p.c((byte)0, (byte)0) == 2 && p.c((byte)0, (byte)1) == 1) {
            for (n2 = 0; n2 < game.d.p.A; ++n2) {
                if (!game.d.p.z[n2].S()) continue;
                game.d.p.z[n2].u(game.d.p.z[n2].d[1] + aq.c[0][game.d.p.z[n2].q()][5] * aq.c[2][0][6] / 100);
                game.d.p.z[n2].l(aq.c[0][game.d.p.z[n2].q()][5] * aq.c[2][0][6] / 100);
            }
        }
    }

    private void h(b b2) {
        int n2;
        int n3;
        int n4 = b2.s();
        int n5 = ((n4 << 1) * n4 + 50) * this.aG[b2.c[0] - 1] / 10 + 400;
        int n6 = x.size();
        int[] nArray = new int[n6];
        b b3 = null;
        byte by = 0;
        for (n3 = 0; n3 < n6; ++n3) {
            b3 = (b)x.elementAt(n3);
            if (b3.s() - n4 >= 6) {
                by = this.aI[6];
            } else if (b3.s() - n4 > 0) {
                by = this.aI[b3.s() - n4];
            } else if (b3.s() == n4) {
                by = this.aI[1];
            } else if (b3.s() < n4) {
                by = this.aI[0];
            }
            n2 = n5 / n6 * this.aH[n6 - 1] * by / 1000;
            if (b3.f((byte)5)) {
                n2 = n2 * (aq.c[3][5][5] + 100) / 100;
            }
            b3.B += n2;
            nArray[n3] = n2;
            if (j.contains(b3)) continue;
            j.addElement(b3);
        }
        for (n3 = 0; n3 < game.d.p.A; ++n3) {
            if (!this.c(n3).S() || x.contains(this.c(n3))) continue;
            if (p.c((byte)7, (byte)0) == 2) {
                if (b3.s() - n4 >= 6) {
                    by = this.aI[6];
                } else if (b3.s() - n4 > 0) {
                    by = this.aI[b3.s() - n4];
                } else if (b3.s() == n4) {
                    by = this.aI[1];
                } else if (b3.s() < n4) {
                    by = this.aI[0];
                }
                n2 = n5 / n6 * this.aH[n6 - 1] * by / 3000;
                this.c((int)n3).B += n2;
                this.c(n3).c();
                if (j.contains(this.c(n3))) continue;
                j.addElement(this.c(n3));
                continue;
            }
            if (!this.c(n3).f((byte)6)) continue;
            if (b3.s() - n4 >= 6) {
                by = this.aI[6];
            } else if (b3.s() - n4 > 0) {
                by = this.aI[b3.s() - n4];
            } else if (b3.s() == n4) {
                by = this.aI[1];
            } else if (b3.s() < n4) {
                by = this.aI[0];
            }
            n2 = n5 / n6 * this.aH[n6 - 1] * by / 1000;
            this.c((int)n3).B += n2;
            this.c(n3).c();
            if (j.contains(this.c(n3))) continue;
            j.addElement(this.c(n3));
        }
        for (n5 = 0; n5 < x.size(); ++n5) {
            b b4 = (b)x.elementAt(n5);
            if (b4.K()) continue;
            x.removeElement(b4);
        }
        if (p.k(0)) {
            if (game.d.p.I == 0) {
                if (b3.s() >= 30 && ++game.k.q >= 10) {
                    game.k.q = 10;
                    return;
                }
            } else if (b3.s() >= 40 && ++game.k.q >= 30) {
                game.k.q = 30;
            }
        }
    }

    public final int b(int n2) {
        if (n2 == 0) {
            return 100;
        }
        int n3 = 0;
        if (((b)this.h.p).m(1)) {
            n3 = 1;
        }
        if (((b)this.h.p).m(2)) {
            n3 = 2;
        }
        if (((b)this.h.p).m(10)) {
            n3 = 3;
        }
        if (this.h.f((byte)11)) {
            n3 = 4;
        }
        int n4 = 1;
        short s2 = ((b)this.h.p).d[1];
        short s3 = ((b)this.h.p).c[1];
        if (s2 <= s3 * 15 / 100) {
            n4 = 85;
        } else if (s2 <= s3 * 50 / 100) {
            n4 = 45;
        } else if (s2 <= s3) {
            n4 = 20;
        }
        n4 = n4 * aq.c[4][n2][6] / 100;
        int[] nArray = new int[]{110, 100, 95, 80, 70};
        n4 = n4 * nArray[((b)this.h.p).c[0] - 1] / 100;
        nArray = new int[]{10, 11, 12, 12, 12};
        n4 = n4 * nArray[n3] / 10;
        if (this.h.f((byte)11)) {
            n4 = n4 * (100 + aq.c[3][11][5]) / 100;
        }
        Object[] objectArray = new int[]{1000, 500, 1, 1000};
        n4 = n4 * objectArray[aq.c[0][((b)this.h.p).q()][22]] / 1000;
        if (((b)this.h.p).s() >= 20 && n4 >= (objectArray = (Object[])new byte[]{0, 15, 35, 65})[n2]) {
            n4 = objectArray[n2];
        }
        if (n4 >= 100) {
            n4 = 100;
        } else if (n4 <= 0) {
            n4 = 1;
        }
        return n4;
    }

    public final b c(int n2) {
        if (n2 > this.f.length - 1) {
            return null;
        }
        return game.d.p.z[n2];
    }

    public final b d(int n2) {
        if (n2 > this.f.length - 1) {
            return null;
        }
        return game.d.p.z[this.f[n2]];
    }

    public final void a(int[][] nArray) {
        this.s = nArray;
    }

    public final int j() {
        return this.s.length;
    }

    public final int k() {
        return this.s[0][0];
    }

    public final void l() {
        switch (U) {
            case 0: {
                if (V == 0) {
                    if (this.h == null) break;
                    int n2 = this.d[0].c[1] * 50 / 100;
                    if (this.d[0].d[1] <= n2) {
                        game.d.c(0, 1);
                        V = (byte)(V + 1);
                        this.S.c("Di L\u1eb7c th\u1ecf th\u1ecf \u0111\u00e3 b\u1ecb th\u01b0\u01a1ng, nhanh s\u1eed d\u1ee5ng #2 phong \u1ea5n c\u1ea7u #1 ti\u1ebfn h\u00e0nh b\u1eaft \u0111\u01b0\u1ee3c a");
                    }
                    return;
                }
                if (V == 1) {
                    if (!this.S.ay()) break;
                    V = (byte)(V + 1);
                    this.S.a = 1;
                    game.d.c(2, 1);
                    game.d.c(1, 1);
                    this.S.ag();
                    this.S.c("H\u00e3y nh\u1ea5n #2n\u00fat 5");
                    return;
                }
                if (V == 3) {
                    V = (byte)(V + 1);
                    game.d.c(2, 0);
                    game.d.c(1, 1);
                    this.S.c("H\u00e3y l\u1ef1a ch\u1ecdn phong \u1ea5n c\u1ea7u");
                    return;
                }
                if (V == 5) {
                    V = (byte)(V + 1);
                    this.S.c("\u0110\u00e1ng ti\u1ebfc \u0111\u00e3 b\u1eaft tr\u01b0\u1ee3t, th\u1eed d\u00f9ng lo\u1ea1i x\u1ecbn #2T\u1ea5t tr\u00fang c\u1ea7u#1 xem sao!");
                    return;
                }
                if (V == 6) {
                    if (!this.S.ay()) break;
                    game.d.c(1, 0);
                    this.S.a = 1;
                    this.S.ag();
                    this.S.b = 0;
                    V = (byte)(V + 1);
                    this.a((byte)21);
                    return;
                }
                if (V != 8) break;
                game.d.c(1, -1);
                game.d.c(0, 0);
                U = (byte)-1;
                V = 0;
                return;
            }
            case 2: {
                int n3;
                if (V == 0) {
                    game.d.c(0, 0);
                    V = (byte)(V + 1);
                    this.S.c("Tranh th\u1ee7 th\u1eddi gian l\u1ef1a ch\u1ecdn #2T\u1ea5t tr\u00fang c\u1ea7u#1 \u0111\u1ec3 b\u1eaft s\u1ee7ng v\u1eadt");
                    return;
                }
                if (V != 2) break;
                if (this.h != null && this.d[0].d[1] <= (n3 = this.d[0].c[1] * 50 / 100 + 2)) {
                    V = (byte)(V - 1);
                    this.S.c("L\u1ef1a ch\u1ecdn #2T\u1ea5t tr\u00fang c\u1ea7u#1 \u0111\u1ec3 b\u1eaft s\u1ee7ng v\u1eadt");
                }
                if (p.a((byte)1, 29) != 2) break;
                U = (byte)-1;
                V = 0;
                return;
            }
            case 5: {
                if (V != 0) break;
                this.S.a = 1;
                V = (byte)(V + 1);
                this.S.ag();
                this.S.c("Tranh th\u1ee7 th\u1eddi gian l\u1ef1a ch\u1ecdn #2T\u1ea5t tr\u00fang c\u1ea7u#1 \u0111\u1ec3 b\u1eaft s\u1ee7ng v\u1eadt");
            }
        }
    }

    public final void m() {
        switch (U) {
            case 0: {
                if (V != 2 && V != 4 && V != 7) break;
                V = (byte)(V + 1);
                return;
            }
            case 5: {
                if (V == 1) {
                    V = (byte)(V + 1);
                    game.d.c(1, 0);
                    game.d.c(0, 1);
                    return;
                }
                if (V != 2) break;
                game.d.c(0, 0);
                U = (byte)-1;
                V = 0;
            }
        }
    }

    static {
        l = 0;
    }
}

