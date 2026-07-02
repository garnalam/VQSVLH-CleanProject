/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Graphics
 *  javax.microedition.lcdui.Image
 */
package game;

import game.a;
import game.d;
import game.g;
import game.i;
import game.j;
import game.k;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public final class c
extends an {
    private static c v = null;
    private k w;
    private g x;
    private an y;
    public p[] a;
    private Vector z;
    private byte A = (byte)-1;
    public byte[][] b;
    private int B = 0;
    public int c = -1;
    private static Vector C;
    public static Vector d;
    private j D = game.j.a();
    public static boolean e;
    public static boolean f;
    public static boolean g;
    public static boolean h;
    public boolean i = true;
    public static boolean j;
    public boolean k = true;
    public byte l = 0;
    public static byte m;
    private short[] E;
    private byte[] F;
    private short[] G;
    private short[] H;
    private short[][] I;
    private short[][] J;
    private short[] K;
    private short[] L;
    public byte n = 0;
    public static Image o;
    private String[] M = new String[]{"ikon_1", "ikon_2", "ikon_3", "ikon_4", "ikon_5"};
    private int N;
    private int O;
    private int Z = 0;
    private int aa = 0;
    private byte[] ab;
    private String[] ac;
    private int[] ad;
    private int[] ae;
    private String[] af = null;
    private f ag = null;
    private int ah = -1;
    private Calendar ai = null;
    private int[] aj;
    public byte p = 0;
    public static String[] q;
    public static String[] r;
    public static short[][] s;
    public static byte t;
    public static byte u;
    private static byte[][] ak;
    private static byte[][] al;

    public c() {
        if (this.w == null) {
            this.w = game.k.a();
        }
        if (this.x == null) {
            this.x = game.g.o();
        }
        if (this.b == null) {
            this.b = new byte[127][];
        }
        if (s == null) {
            s = new short[200][2];
        }
        if (this.ai == null) {
            this.ai = Calendar.getInstance(TimeZone.getDefault());
        }
        try {
            int n2;
            Object object = ae.c(ae.a("/data/script/bTask.mid"));
            q = new String[((String[][])object).length];
            for (n2 = 0; n2 < ((String[][])object).length; ++n2) {
                System.arraycopy(object[n2], 0, q, n2, object[n2].length);
            }
            object = ae.c(ae.a("/data/script/mTask.mid"));
            r = new String[((String[][])object).length];
            for (n2 = 0; n2 < ((String[][])object).length; ++n2) {
                System.arraycopy(object[n2], 0, r, n2, object[n2].length);
            }
            object = ae.a("/data/script/bqTask.mid");
            ak = ae.b((InputStream)object);
            al = ae.b((InputStream)object);
            ((InputStream)object).close();
            return;
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
            return;
        }
    }

    public static c a() {
        if (v == null) {
            v = new c();
        }
        return v;
    }

    public final void a(an an2) {
        if (this.y != null) {
            this.y = null;
        }
        this.y = an2;
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public final void b() {
        if (this.a == null) {
            return;
        }
        b.a().b();
        this.D.d();
        var1_1 = this;
        for (var2_2 = 0; var2_2 < var1_1.a.length; ++var2_2) {
            if (var1_1.a[var2_2].a() != 0 && var1_1.a[var2_2].a() != 4) continue;
            var3_3 = var1_1.a[var2_2].d();
            var4_5 = false;
            switch (var3_3.a()) {
                case 43: {
                    if (game.k.a((int)var3_3.b()[2], (int)var3_3.b()[3]) != game.k.a(var1_1.w.f, var1_1.w.g) || var3_3.b()[4] != game.k.u || !var1_1.a((ad)var3_3)) break;
                    game.c.g = true;
                    if (!game.c.h || var1_1.a(-1) != -1) break;
                    var6_7 = var2_2;
                    var5_6 = var1_1;
                    for (var7_12 = 0; var7_12 < var5_6.a.length; ++var7_12) {
                        if (var5_6.a[var7_12].a() == 3 || var6_7 == var7_12 || (var8_13 = var5_6.a[var7_12].d()).a() != 43 || game.k.a((int)var8_13.b()[2], (int)var8_13.b()[3]) != game.k.a(var5_6.w.f, var5_6.w.g) || var8_13.b()[4] != game.k.u || !super.a(var8_13)) continue;
                        v0 = (byte)var7_12;
                        ** GOTO lbl22
                    }
                    v0 = -1;
lbl22:
                    // 2 sources

                    if (var2_2 < v0) break;
                    var4_5 = true;
                    game.c.g = false;
                    var1_1.x.b((byte)0, var1_1.x.n);
                    if (var3_3.b()[1] != 1) break;
                    game.c.s[game.c.u][0] = var3_3.b()[0];
                    break;
                }
                case 44: {
                    if (game.k.a((int)var3_3.b()[2], (int)var3_3.b()[3]) != game.k.a(var1_1.w.f, var1_1.w.g) || var3_3.b()[4] != game.k.u || !var1_1.b((ad)var3_3)) break;
                    game.c.g = true;
                    if (!game.c.h || var2_2 < var1_1.a(var2_2)) break;
                    var1_1.x.b((byte)0, var1_1.x.n);
                    game.c.g = false;
                    ** GOTO lbl109
                }
                case 13: {
                    if (!ae.a(var3_3.b()[0], (int)var3_3.b()[1], (int)var3_3.b()[2], (int)var3_3.b()[3], var1_1.x.i, var1_1.x.j, var1_1.x.a.k())) break;
                    var1_1.x.b((byte)0, var1_1.x.n);
                    ** GOTO lbl109
                }
                case 15: {
                    if (var1_1.b[game.k.a((int)var3_3.b()[0], (int)var3_3.b()[1])] == null || var1_1.b[game.k.a((int)var3_3.b()[0], (int)var3_3.b()[1])][var3_3.b()[2]] != 3 && var1_1.b[game.k.a((int)var3_3.b()[0], (int)var3_3.b()[1])][var3_3.b()[2]] != 4) break;
                    var4_5 = true;
                    break;
                }
                case 16: {
                    if (var3_3.b()[0] != game.k.u) break;
                    game.c.g = true;
                    if (!game.c.h) break;
                    var1_1.x.b((byte)0, var1_1.x.n);
                    game.c.h = false;
                    ** GOTO lbl109
                }
                case 69: {
                    if (var3_3.b()[0] != game.k.u) break;
                    ** GOTO lbl109
                }
                case 57: {
                    if (var1_1.x.p == null || ((a)var1_1.x.p).t != 0 || ((a)var1_1.x.p).v != 11 || ((a)var1_1.x.p).I != var3_3.b()[3] || !game.c.h) break;
                    if (var1_1.w.d[var3_3.b()[0]].i == var3_3.b()[1] && var1_1.w.d[var3_3.b()[0]].j == var3_3.b()[2]) {
                        ((a)var1_1.x.p).d((byte)0);
                    } else {
                        var4_5 = true;
                    }
                    game.c.h = false;
                    break;
                }
                case 59: {
                    var5_6 = new int[ae.a(var3_3.c()[0], ',').length];
                    for (var6_8 = 0; var6_8 < ((byte[])var5_6).length; ++var6_8) {
                        var5_6[var6_8] = ae.b(ae.a(var3_3.c()[0], ',')[var6_8]);
                        if (var1_1.w.d[var5_6[var6_8]].h() == 0) break;
                    }
                    if (var6_8 < ((byte[])var5_6).length) break;
                    var4_5 = true;
                    break;
                }
                case 61: {
                    var5_6 = new int[ae.a(var3_3.c()[0], ',').length];
                    for (var6_9 = 0; var6_9 < ((byte[])var5_6).length; ++var6_9) {
                        var5_6[var6_9] = ae.b(ae.a(var3_3.c()[0], ',')[var6_9]);
                        if (var1_1.w.d[var5_6[var6_9]].h() == 0) break;
                    }
                    if (var6_9 < ((byte[])var5_6).length) break;
                    var1_1.x.b((byte)0, var1_1.x.n);
                    var4_5 = true;
                    break;
                }
                case 73: {
                    var5_6 = ae.a(var3_3.c()[1], ',');
                    var6_10 = ae.a(var3_3.c()[0], ',');
                    for (var3_4 = 0; var3_4 < ((byte[])var5_6).length && var1_1.x.a(ae.d(var6_10[var3_4]), (int)ae.d((String)var5_6[var3_4])) >= 2; ++var3_4) {
                    }
                    if (var3_4 < ((byte[])var5_6).length) break;
                    var4_5 = true;
                    break;
                }
                case 75: {
                    if (var1_1.x.L.size() <= 0) break;
                    ** GOTO lbl109
                }
                case 78: {
                    var5_6 = ae.e(var3_3.c()[0]);
                    var6_11 = ae.e(var3_3.c()[1]);
                    var3_3 = ae.e(var3_3.c()[2]);
                    for (var7_12 = 0; var7_12 < ((Object)var3_3).length && var1_1.b[game.k.a((int)var5_6[var7_12], (int)var6_11[var7_12])] != null && (var1_1.b[game.k.a((int)var5_6[var7_12], (int)var6_11[var7_12])][var3_3[var7_12]] == 3 || var1_1.b[game.k.a((int)var5_6[var7_12], (int)var6_11[var7_12])][var3_3[var7_12]] == 4); ++var7_12) {
                    }
                    if (var7_12 < ((Object)var3_3).length) break;
                    var4_5 = true;
                    break;
                }
                case 79: {
                    if (var1_1.b[game.k.a((int)var3_3.b()[0], (int)var3_3.b()[1])] == null || var1_1.b[game.k.a((int)var3_3.b()[0], (int)var3_3.b()[1])][var3_3.b()[2]] != 3 || var1_1.x.k(0) || game.k.u != var3_3.b()[3]) break;
                    game.c.g = true;
                    if (!game.c.h) break;
                    game.c.h = false;
                    ** GOTO lbl109
                }
                case 86: {
                    if (var1_1.b[game.k.a((int)var3_3.b()[0], (int)var3_3.b()[1])] == null || var1_1.b[game.k.a((int)var3_3.b()[0], (int)var3_3.b()[1])][var3_3.b()[2]] != 3) break;
                }
lbl109:
                // 8 sources

                default: {
                    var4_5 = true;
                }
            }
            if (!var4_5) continue;
            var1_1.A = (byte)var2_2;
            var1_1.a[var2_2].b((byte)0);
            var1_1.z.addElement(var1_1.a[var2_2]);
            var1_1.a[var2_2].a((byte)1);
        }
        this.n();
    }

    public static void a(Graphics graphics) {
        int n2;
        if (C != null) {
            for (n2 = 0; n2 < C.size(); ++n2) {
                f f2 = (f)C.elementAt(n2);
                f2.b(f2.p.i, f2.p.j - 40);
                f2.a(graphics, j.a().a, j.a().b);
            }
        }
        if (d != null) {
            for (n2 = 0; n2 < d.size(); ++n2) {
                ((f)d.elementAt(n2)).a(graphics, j.a().a, j.a().b);
            }
        }
    }

    public final void c() {
        int n2;
        if (C != null) {
            for (n2 = 0; n2 < C.size(); ++n2) {
                f f2 = (f)C.elementAt(n2);
                f2.a();
                if (!f2.a.e()) continue;
                f2.d();
                C.removeElementAt(n2);
                --n2;
            }
        }
        if (d != null) {
            for (n2 = 0; n2 < d.size(); ++n2) {
                ((f)d.elementAt(n2)).a();
            }
        }
        if (this.ag != null) {
            this.ag.a();
        }
    }

    public final void b(Graphics graphics) {
        b.a().a(graphics);
        Graphics graphics2 = graphics;
        c c2 = this;
        if (c2.ag != null && c2.ah == game.k.a(c2.w.f, c2.w.g)) {
            c2.ag.a(graphics2, j.a().a, j.a().b);
        }
        this.D.a(graphics);
    }

    public final boolean d() {
        return true;
    }

    public final boolean a(DataInputStream dataInputStream, int n2, int n3, int n4, String[] stringArray) {
        try {
            this.a = new p[n4];
            this.z = new Vector();
            C = new Vector();
            int n5 = n2 << 8 | n3;
            if (this.b[game.k.l[n2] + n3] == null) {
                this.b[game.k.l[n2] + n3] = new byte[n4];
            }
            for (byte by = 0; by < n4; by = (byte)(by + 1)) {
                this.a[by] = new p();
                this.a[by].a(dataInputStream, by, n5, stringArray);
                this.a[by].a(this.b[game.k.l[n2] + n3][by]);
            }
        }
        catch (IOException iOException) {
            System.out.println(" ex = " + iOException.toString() + " event init ");
        }
        this.i();
        return false;
    }

    public final void e() {
        this.x = null;
        this.w = null;
        this.b = null;
        s = null;
        this.ai = null;
        v = null;
    }

    public final void f() {
        if (C != null) {
            C.removeAllElements();
            C = null;
        }
        if (d != null) {
            d.removeAllElements();
            d = null;
        }
        this.a = null;
        this.E = null;
        this.F = null;
        this.G = null;
        this.H = null;
        this.I = null;
        this.J = null;
        this.K = null;
        this.L = null;
        this.ab = null;
        this.ac = null;
    }

    public final void a(byte by) {
    }

    private byte a(int n2) {
        for (int i2 = 0; i2 < this.a.length; ++i2) {
            ad ad2;
            if (this.a[i2].a() == 3 || n2 == i2 || (ad2 = this.a[i2].d()).a() != 44 || game.k.a((int)ad2.b()[2], (int)ad2.b()[3]) != game.k.a(this.w.f, this.w.g) || ad2.b()[4] != game.k.u || !this.b(ad2)) continue;
            return (byte)i2;
        }
        return -1;
    }

    private static void b(int n2) {
        for (int i2 = 0; i2 < u; ++i2) {
            if (s[i2][0] != n2) continue;
            game.c.s[i2][1] = 3;
            return;
        }
    }

    private static int c(int n2) {
        for (int i2 = 0; i2 < u; ++i2) {
            if (s[i2][0] != n2) continue;
            return i2;
        }
        return -1;
    }

    /*
     * Unable to fully structure code
     */
    private void n() {
        var1_1 = 0;
        while (var1_1 < this.z.size()) {
            var2_2 = (p)this.z.elementAt(var1_1);
            var3_3 = var2_2.c();
            switch (var3_3.a()) {
                case 0: {
                    break;
                }
                case 1: {
                    if (var2_2.a() != 5) {
                        b.a().c(0, 9);
                        this.D.a(var3_3.b()[1], var3_3.b()[2]);
                        this.D.a((byte)(var3_3.b()[0] / 10 - 1), var3_3.c()[0], var3_3.b()[0] % 10);
                        this.D.a(true);
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!game.j.a || !this.y.k(1)) break;
                    this.D.b();
                    if (game.j.b) break;
                    b.a().a = -1;
                    this.D.c();
                    ** GOTO lbl1044
                }
                case 2: {
                    if (var3_3.b()[0] == -1) {
                        this.x.c();
                        this.x.b((byte)0, ae.d(ae.a(var3_3.c()[1], ',')[0]));
                        break;
                    }
                    for (var4_4 = 0; var4_4 < var3_3.b()[0]; ++var4_4) {
                        this.w.d[ae.c(ae.a(var3_3.c()[0], ',')[var4_4])].b(ae.d(ae.a(var3_3.c()[1], ',')[var4_4]));
                        if (this.w.d[ae.c((String)ae.a((String)var3_3.c()[0], (char)',')[var4_4])].v == 1) {
                            this.w.d[ae.c(ae.a(var3_3.c()[0], ',')[var4_4])].d((byte)0);
                        }
                        this.w.d[ae.c(ae.a(var3_3.c()[0], ',')[var4_4])].c();
                    }
                    break;
                }
                case 3: {
                    if (var3_3.b()[0] == -1) {
                        this.x.d();
                        break;
                    }
                    for (var5_29 = 0; var5_29 < var3_3.b()[0]; ++var5_29) {
                        var4_5 = ae.c(ae.a(var3_3.c()[0], ',')[var5_29]);
                        this.w.d[var4_5].d();
                    }
                    break;
                }
                case 4: {
                    if (var2_2.a() != 5) {
                        this.x.b((byte)0, this.x.n);
                        this.w.S.a(var3_3.c()[0], var3_3.c()[1], (int)var3_3.b()[1]);
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.w.S.c(var3_3.b()[1], -1) || !this.y.k(196640)) break;
                    game.k.a().e();
                    if (ae.b < ae.b()) {
                        ae.c();
                        this.w.S.b(ae.b);
                        break;
                    }
                    if (game.k.u != -1 && this.w.d[game.k.u].a.a <= 85 && this.w.d[game.k.u].u() == 0) {
                        game.k.a().a(game.k.a().d[game.k.u].i, game.k.a().d[game.k.u].j - 40, game.k.a().d[game.k.u]);
                    }
                    game.c.g = false;
                    game.c.h = false;
                    this.w.S.aC();
                    var2_2.a((byte)1);
                    break;
                }
                case 5: {
                    var4_6 = new f();
                    var4_6.a(259, false);
                    var4_6.a((byte)var3_3.b()[2], (byte)-1, true);
                    if (var3_3.b()[0] == 0) {
                        var4_6.b(this.x.l(), this.x.m() - this.x.a.b(this.x.h(), this.x.n)[3]);
                        var4_6.a(this.x);
                    } else if (var3_3.b()[0] == 1) {
                        if (var3_3.b()[3] != 0 || var3_3.b()[4] != 0) {
                            var4_6.b(var3_3.b()[3], var3_3.b()[4]);
                        } else {
                            var4_6.b(this.w.d[var3_3.b()[1]].l(), this.w.d[var3_3.b()[1]].m());
                            var4_6.a(this.w.d[var3_3.b()[1]]);
                        }
                    }
                    var4_6.c();
                    game.c.C.addElement(var4_6);
                    break;
                }
                case 6: {
                    this.b[game.k.l[this.w.f] + this.w.g][var2_2.b()] = 3;
                    game.k.a().f = var3_3.b()[0];
                    game.k.a().g = var3_3.b()[1];
                    this.w.j = var3_3.b()[3] == 1 ? var3_3.b()[2] : -1;
                    game.i.a().a((byte)22);
                    break;
                }
                case 7: {
                    if (var2_2.a() != 5) {
                        this.E = new short[var3_3.b()[0]];
                        for (var4_7 = 0; var4_7 < this.E.length; ++var4_7) {
                            this.E[var4_7] = ae.c(ae.a(var3_3.c()[0], ',')[var4_7]);
                            var5_30 = ae.d(ae.a(var3_3.c()[2], ',')[var4_7]);
                            if (this.E[var4_7] == -1) {
                                this.x.b(ae.d(ae.a(var3_3.c()[1], ',')[0]), var5_30);
                                continue;
                            }
                            this.w.d[this.E[var4_7]].b(var5_30);
                            this.w.d[this.E[var4_7]].d(ae.d(ae.a(var3_3.c()[1], ',')[var4_7]));
                        }
                        this.B = 0;
                        var2_2.a((byte)5);
                        break;
                    }
                    for (var4_8 = 0; var4_8 < this.E.length; ++var4_8) {
                        if (this.E[var4_8] == -1) {
                            if (!this.x.b()) continue;
                            this.x.b((byte)0, this.x.n);
                            ++this.B;
                            continue;
                        }
                        if (!this.w.d[this.E[var4_8]].b()) continue;
                        this.w.d[this.E[var4_8]].d((byte)0);
                        ++this.B;
                    }
                    if (this.B < this.E.length) break;
                    this.B = 0;
                    ** GOTO lbl1044
                }
                case 8: {
                    this.x.c();
                    game.k.u = (short)-1;
                    this.x.b(var3_3.b()[0], var3_3.b()[1]);
                    this.x.b.b(var3_3.b()[0], var3_3.b()[1]);
                    this.x.b((byte)0, this.x.n);
                    break;
                }
                case 9: {
                    if (var2_2.a() != 5) {
                        var4_9 = false;
                        if (var3_3.b()[0] == 12 || var3_3.b()[0] == 13) {
                            b.a().c(0, var3_3.b()[0]);
                            b.a().a(var3_3.b()[1], var3_3.b()[2], var3_3.b()[3], var3_3.b()[4], var3_3.b()[5]);
                        } else if (var3_3.b()[0] == 10) {
                            b.a().c(0, var3_3.b()[0]);
                            b.a().d(var3_3.b()[1], var3_3.b()[2]);
                        } else if (var3_3.b()[0] == 15 || var3_3.b()[0] == 14) {
                            b.a().c(0, var3_3.b()[0]);
                            b.a().a(this.M[var3_3.b()[1]], (int)var3_3.b()[2], (int)var3_3.b()[3], (int)var3_3.b()[4]);
                        } else if (var3_3.b()[0] == 16) {
                            if (var3_3.b()[1] == 0) {
                                var5_31 = new String[]{"star0", "star1", "star2", "star3"};
                                b.a().a(16, (byte)var3_3.b()[2], (byte)7, var5_31);
                            } else if (var3_3.b()[1] == 1) {
                                var5_32 = new String[]{"fire0", "fire1", "fire2"};
                                b.a().a(16, (byte)var3_3.b()[2], (byte)0, var5_32);
                            } else if (var3_3.b()[1] == 2) {
                                var5_33 = new String[]{"fire0", "fire1", "fire2"};
                                b.a().a(17, (byte)var3_3.b()[2], (byte)0, var5_33);
                            } else {
                                var4_9 = true;
                                b.a().a(-1, (byte)var3_3.b()[2], (byte)0, null);
                                var2_2.a((byte)1);
                            }
                        } else if (var3_3.b()[0] == 17) {
                            b.a().c(var3_3.b()[1], var3_3.b()[0]);
                            b.a().a((int)var3_3.b()[2], (int)var3_3.b()[3], (int)var3_3.b()[4], (int)var3_3.b()[5]);
                        } else {
                            var5_34 = var3_3.b()[1] << 24 | var3_3.b()[2] << 16 | var3_3.b()[3] << 8 | var3_3.b()[4];
                            b.a().c(var5_34, var3_3.b()[0]);
                        }
                        if (var4_9) break;
                        var2_2.a((byte)5);
                        break;
                    }
                    if (b.a().c && (var3_3.b()[0] == 12 || var3_3.b()[0] == 13)) {
                        var2_2.a((byte)1);
                        break;
                    }
                    if (var3_3.b()[0] != 16 && !b.a().b) break;
                    ** GOTO lbl1044
                }
                case 10: {
                    if (var2_2.a() != 5) {
                        if (var3_3.b()[0] == -1) {
                            this.F = new byte[1];
                            this.x.b(ae.d(ae.a(var3_3.c()[1], ',')[0]));
                            this.x.b((byte)0, this.x.n);
                            this.x.a((byte)0, (short)ae.d(ae.a(var3_3.c()[2], ',')[0]));
                            this.F[0] = ae.d(ae.a(var3_3.c()[3], ',')[0]);
                        } else {
                            this.E = new short[var3_3.b()[0]];
                            this.F = new byte[var3_3.b()[0]];
                            for (var4_10 = 0; var4_10 < this.E.length; ++var4_10) {
                                this.E[var4_10] = ae.c(ae.a(var3_3.c()[0], ',')[var4_10]);
                                if (this.E[var4_10] != -1) {
                                    this.w.d[this.E[var4_10]].b(ae.d(ae.a(var3_3.c()[1], ',')[var4_10]));
                                    this.w.d[this.E[var4_10]].a((byte)0, (short)ae.d(ae.a(var3_3.c()[2], ',')[var4_10]));
                                    this.w.d[this.E[var4_10]].d((byte)0);
                                } else {
                                    this.x.b(ae.d(ae.a(var3_3.c()[1], ',')[var4_10]));
                                    this.x.a((byte)0, (short)ae.d(ae.a(var3_3.c()[2], ',')[var4_10]));
                                    this.x.b((byte)0, this.x.n);
                                }
                                this.F[var4_10] = ae.d(ae.a(var3_3.c()[3], ',')[var4_10]);
                            }
                        }
                        this.B = 0;
                        var2_2.a((byte)5);
                        break;
                    }
                    if (var3_3.b()[0] == -1) {
                        if (this.x.h() == 0) {
                            this.x.b((byte)1, this.x.n);
                            break;
                        }
                        this.F[0] = (byte)(this.F[0] - 1);
                        if (this.F[0] > 0) break;
                        this.x.b((byte)0, this.x.n);
                        if (this.x.P[0] == 2 || this.x.P[1] == 2) {
                            this.x.a((byte)0, (short)8);
                        } else {
                            this.x.a((byte)0, (short)4);
                        }
                        var2_2.a((byte)1);
                        break;
                    }
                    for (var4_11 = 0; var4_11 < this.E.length; ++var4_11) {
                        if (this.E[var4_11] != -1 && this.w.d[this.E[var4_11]].h() == 0 || this.E[var4_11] == -1 && this.x.h() == 0) {
                            if (this.F[var4_11] <= 0) continue;
                            if (this.E[var4_11] != -1) {
                                this.w.d[this.E[var4_11]].d((byte)3);
                                continue;
                            }
                            this.x.b((byte)1, this.x.n);
                            continue;
                        }
                        v0 = var4_11;
                        this.F[v0] = (byte)(this.F[v0] - 1);
                        if (this.F[var4_11] > 0) continue;
                        ++this.B;
                        this.F[var4_11] = 0;
                        if (this.E[var4_11] != -1) {
                            this.w.d[this.E[var4_11]].d((byte)0);
                            this.w.d[this.E[var4_11]].a((byte)0, (short)4);
                            continue;
                        }
                        this.x.b((byte)0, this.x.n);
                        if (this.x.P[0] == 2 || this.x.P[1] == 2) {
                            this.x.a((byte)0, (short)8);
                            continue;
                        }
                        this.x.a((byte)0, (short)4);
                    }
                    if (this.B < this.E.length) break;
                    ** GOTO lbl1044
                }
                case 11: {
                    if (var2_2.a() != 5) {
                        var4_12 = false;
                        if (var3_3.b()[6] == 0) {
                            var4_12 = true;
                        }
                        ai.a().a(var3_3.b()[7]);
                        if (var3_3.b()[2] == 1) {
                            v1 = ai.a();
                            v2 = var3_3.b()[4];
                            v3 = var3_3.b()[5];
                            var3_3.b();
                            var3_3.b();
                            v1.a((int)v2, (int)v3, var4_12);
                        } else if (var3_3.b()[2] == 0) {
                            if (var3_3.b()[3] == -1) {
                                v4 = ai.a();
                                var3_3.b();
                                var3_3.b();
                                v4.a(this.x, var4_12);
                            } else {
                                v5 = ai.a();
                                v6 = this.w.d[var3_3.b()[3]];
                                var3_3.b();
                                var3_3.b();
                                v5.a(v6, var4_12);
                            }
                        }
                        this.x.b((byte)0, this.x.n);
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!ai.a().c()) break;
                    ** GOTO lbl1044
                }
                case 12: {
                    ++this.B;
                    if (var2_2.a() != 5) {
                        var2_2.a((byte)5);
                        break;
                    }
                    if (this.B < var3_3.b()[0]) break;
                    this.B = 0;
                    ** GOTO lbl1044
                }
                case 13: {
                    if (ae.a(var3_3.b()[0], (int)var3_3.b()[1], (int)var3_3.b()[2], (int)var3_3.b()[3], this.x.i, this.x.j, this.x.a.k())) {
                        var2_2.a((byte)1);
                        this.x.b((byte)0, this.x.n);
                        break;
                    }
                    var2_2.a((byte)6);
                    break;
                }
                case 14: {
                    var2_2.a((byte)3);
                    break;
                }
                case 16: {
                    if (var3_3.b()[0] == game.k.u) {
                        game.c.g = true;
                        if (!game.c.h) break;
                        game.c.h = false;
                        var2_2.a((byte)2);
                        break;
                    }
                    var2_2.a((byte)6);
                    break;
                }
                case 17: {
                    if (var2_2.a() != 5) {
                        if (var3_3.b()[0] == 0) {
                            if (this.x.a((int)var3_3.b()[1], (int)var3_3.b()[2], (byte)0)) {
                                var4_13 = aq.c[4][var3_3.b()[1]][0];
                                this.y.S.a("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + an.f(var4_13), (int)var3_3.b()[2]);
                                this.x.c(var3_3.b()[1], var3_3.b()[2], (byte)0);
                            } else {
                                this.y.S.b("Ba l\u00f4 \u0111\u00e3 \u0111\u1ee7 \u0111\u1ea1o c\u1ee5 n\u00e0y");
                            }
                        } else if (this.x.b((int)var3_3.b()[1], (int)var3_3.b()[2], (byte)0)) {
                            var4_14 = aq.c[4][var3_3.b()[1]][0];
                            this.y.S.a("M\u1ea5t: " + an.f(var4_14), (int)var3_3.b()[2]);
                            this.x.d(var3_3.b()[1], var3_3.b()[2], (byte)0);
                        }
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.y.S.ax()) break;
                    ** GOTO lbl1044
                }
                case 18: {
                    if (var2_2.a() != 5) {
                        if (var3_3.b()[0] == 0) {
                            if (this.x.a((int)var3_3.b()[1], (int)var3_3.b()[2], (byte)2)) {
                                var4_15 = aq.c[3][var3_3.b()[1]][0];
                                this.y.S.a("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + an.f(var4_15), (int)var3_3.b()[2]);
                                this.x.c(var3_3.b()[1], var3_3.b()[2], (byte)2);
                            } else {
                                this.y.S.b("Ba l\u00f4 \u0111\u00e3 \u0111\u1ee7 \u0111\u1ea1o c\u1ee5 n\u00e0y");
                            }
                        } else if (var3_3.b()[0] == 1) {
                            var4_16 = aq.c[3][var3_3.b()[1]][0];
                            this.y.S.a("M\u1ea5t: " + an.f(var4_16), (int)var3_3.b()[2]);
                            this.x.d(var3_3.b()[1], var3_3.b()[2], (byte)2);
                        }
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.y.S.ax()) break;
                    ** GOTO lbl1044
                }
                case 19: {
                    if (var2_2.a() != 5) {
                        var4_17 = aq.c[5][var3_3.b()[0]][0];
                        this.y.S.a("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + an.f(var4_17), (int)var3_3.b()[1]);
                        var5_35 = this.x.d(var3_3.b()[0], var3_3.b()[1]);
                        if (var5_35 != -1) {
                            if (var5_35 == 1) {
                                this.y.S.b("Ba l\u00f4 \u0111\u00e3 \u0111\u1ee7 lo\u1ea1i \u0111\u1ea1o c\u1ee5 n\u00e0y");
                            } else {
                                this.x.c(var3_3.b()[0], var3_3.b()[1]);
                            }
                        } else if (var3_3.b()[0] == 0) {
                            this.x.e(var3_3.b()[0], -1);
                        } else {
                            this.x.i(var3_3.b()[0]);
                        }
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.y.S.ax()) break;
                    ** GOTO lbl1044
                }
                case 20: {
                    if (var2_2.a() != 5) {
                        if (var3_3.b()[0] == 1) {
                            this.y.S.b("M\u1ea5t: " + var3_3.c()[0]);
                            this.x.T[var3_3.b()[1]] = false;
                        } else {
                            this.y.S.b("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + var3_3.c()[0]);
                            this.x.T[var3_3.b()[1]] = true;
                        }
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.y.S.ax()) break;
                    ** GOTO lbl1044
                }
                case 21: {
                    game.k.x = false;
                    game.k.y = var3_3.b()[2];
                    if (var3_3.b()[1] != 1) break;
                    game.k.z = var3_3.b()[3];
                    game.k.A = var3_3.b()[4];
                    game.k.B = var3_3.b()[5];
                    game.k.C = var3_3.b()[6];
                    break;
                }
                case 22: {
                    game.k.x = true;
                    game.k.w = (byte)var3_3.b()[1];
                    game.k.a().h = var3_3.b()[2];
                    game.k.a().i = var3_3.b()[3];
                    game.k.B = var3_3.b()[4];
                    game.k.C = var3_3.b()[5];
                    game.k.a().j = -1;
                    break;
                }
                case 23: {
                    this.b[game.k.a((int)var3_3.b()[0], (int)var3_3.b()[1])][var3_3.b()[2]] = 3;
                    if (var3_3.b()[0] != this.w.f || var3_3.b()[1] != this.w.g) break;
                    this.a[var3_3.b()[2]].a((byte)3);
                    if (this.z.size() <= 0) break;
                    this.z.removeElement(this.a[var3_3.b()[2]]);
                    --var1_1;
                    break;
                }
                case 24: {
                    if (var2_2.a() != 5) {
                        b.a().c(0, 11);
                        b.a().a(var3_3.b()[0], var3_3.b()[1], var3_3.b()[2]);
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!b.a().b) break;
                    ** GOTO lbl1044
                }
                case 25: {
                    game.c.f = var3_3.b()[0] == 0;
                    break;
                }
                case 29: {
                    if (var2_2.a() != 5) {
                        var4_18 = var3_3.b()[0];
                        if (var3_3.b()[0] == -1) {
                            var4_18 = 1;
                        }
                        this.E = new short[var4_18];
                        this.K = new short[var4_18];
                        this.L = new short[var4_18];
                        this.G = new short[var4_18];
                        this.H = new short[var4_18];
                        for (var5_36 = 0; var5_36 < this.E.length; ++var5_36) {
                            this.E[var5_36] = ae.c(ae.a(var3_3.c()[0], ',')[var5_36]);
                            this.K[var5_36] = ae.c(ae.a(var3_3.c()[1], ',')[var5_36]);
                            this.L[var5_36] = ae.c(ae.a(var3_3.c()[2], ',')[var5_36]);
                            this.G[var5_36] = ae.c(ae.a(var3_3.c()[3], ',')[var5_36]);
                            this.H[var5_36] = ae.c(ae.a(var3_3.c()[4], ',')[var5_36]);
                        }
                        var2_2.a((byte)5);
                        break;
                    }
                    var4_19 = true;
                    for (var7_59 = 0; var7_59 < this.E.length; ++var7_59) {
                        if (this.G[var7_59] <= 0 && this.H[var7_59] <= 0) continue;
                        var4_19 = false;
                        v7 = var7_59;
                        this.G[v7] = (short)(this.G[v7] - 1);
                        v8 = var7_59;
                        this.H[v8] = (short)(this.H[v8] - 1);
                        if (var3_3.b()[0] == -1) {
                            var5_37 = this.x.l() + this.K[var7_59];
                            var6_44 = this.x.m() + this.L[var7_59];
                            this.x.b(var5_37, var6_44);
                            if (this.x.b == null) continue;
                            this.x.b.b(var5_37, var6_44);
                            continue;
                        }
                        var5_37 = this.w.d[this.E[var7_59]].l() + this.K[var7_59];
                        var6_44 = this.w.d[this.E[var7_59]].m() + this.L[var7_59];
                        this.w.d[this.E[var7_59]].b(var5_37, var6_44);
                        if (this.w.d[this.E[var7_59]].b == null) continue;
                        this.w.d[this.E[var7_59]].b.b(var5_37, var6_44);
                    }
                    if (!var4_19) break;
                    var2_2.a((byte)1);
                    break;
                }
                case 30: {
                    if (var2_2.a() != 5) {
                        this.E = new short[var3_3.b()[0]];
                        var4_20 = ae.a(var3_3.c()[2], ',');
                        for (var5_38 = 0; var5_38 < this.E.length; ++var5_38) {
                            this.E[var5_38] = ae.c(var4_20[var5_38]);
                        }
                        var5_39 = new String[this.E.length][];
                        var6_45 = new String[this.E.length][];
                        for (var7_60 = 0; var7_60 < var6_45.length; ++var7_60) {
                            var5_39[var7_60] = ae.a(ae.a(var3_3.c()[0], '#')[var7_60], ',');
                            var6_45[var7_60] = ae.a(ae.a(var3_3.c()[1], '#')[var7_60], ',');
                        }
                        this.I = new short[this.E.length][];
                        this.J = new short[this.E.length][];
                        for (var7_60 = 0; var7_60 < this.E.length; ++var7_60) {
                            this.I[var7_60] = new short[var5_39[var7_60].length];
                            this.J[var7_60] = new short[var6_45[var7_60].length];
                            for (var8_68 = 0; var8_68 < this.I[var7_60].length; ++var8_68) {
                                this.I[var7_60][var8_68] = ae.c(var5_39[var7_60][var8_68]);
                                this.J[var7_60][var8_68] = ae.c(var6_45[var7_60][var8_68]);
                            }
                        }
                        this.B = 0;
                        var2_2.a((byte)5);
                        break;
                    }
                    for (var4_21 = 0; var4_21 < this.E.length; ++var4_21) {
                        this.w.d[this.E[var4_21]].b(this.I[var4_21][this.B], this.J[var4_21][this.B]);
                    }
                    ++this.B;
                    if (this.B < this.I[0].length) break;
                    ** GOTO lbl1044
                }
                case 31: {
                    if (var2_2.a() != 5) {
                        if (var3_3.b()[0] == 0) {
                            if (var3_3.b()[1] == 0) {
                                this.x.s(var3_3.b()[2]);
                                this.y.S.b("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + var3_3.b()[2] + " kim ti\u1ec1n");
                            } else if (var3_3.b()[1] == 1) {
                                this.x.u(var3_3.b()[2]);
                                this.y.S.b("\u0110\u1ea1t \u0111\u01b0\u1ee3c: " + var3_3.b()[2] + "Huy hi\u1ec7u");
                            }
                        } else if (var3_3.b()[0] == 1) {
                            if (var3_3.b()[1] == 0) {
                                this.x.s(-var3_3.b()[2]);
                                this.y.S.b("M\u1ea5t: " + var3_3.b()[2] + " kim ti\u1ec1n");
                            } else if (var3_3.b()[1] == 1) {
                                this.x.u(-var3_3.b()[2]);
                                this.y.S.b("M\u1ea5t: " + var3_3.b()[2] + " huy hi\u1ec7u");
                            }
                        }
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.w.S.ax()) break;
                    ** GOTO lbl1044
                }
                case 32: {
                    this.w.e();
                    game.d.a().a = var3_3.b()[0];
                    game.d.a().b = (byte)var3_3.b()[1];
                    game.d.a().c = Image.createImage((int)an.w(), (int)an.x());
                    var4_22 = game.d.a().c.getGraphics();
                    this.w.b.b(var4_22);
                    this.x.b((byte)0, this.x.n);
                    var2_2.a((byte)1);
                    game.i.a().a((byte)12);
                    break;
                }
                case 33: {
                    break;
                }
                case 34: {
                    if (var2_2.a() != 5) {
                        var3_3.b();
                        this.N = var3_3.b()[2];
                        this.O = var3_3.b()[3];
                        this.B = var3_3.b()[4];
                        var2_2.a((byte)5);
                        break;
                    }
                    --this.B;
                    this.N -= this.O;
                    if (this.B > 0) break;
                    this.B = 0;
                    ** GOTO lbl1044
                }
                case 35: {
                    if (var2_2.a() != 5) {
                        this.Z = var3_3.b()[0];
                        this.aa = var3_3.b()[1];
                        this.ac = ae.a(var3_3.c()[0], ',');
                        this.ab = new byte[ae.a(var3_3.c()[1], ',').length];
                        var5_40 = var3_3.c()[2];
                        for (var6_46 = 0; var6_46 < this.ab.length; ++var6_46) {
                            this.ab[var6_46] = ae.d(ae.a(var3_3.c()[1], ',')[var6_46]);
                        }
                        this.y.S.a(this.aa, this.Z, this.ac, var5_40);
                        var2_2.a((byte)5);
                        break;
                    }
                    var5_41 = this.y.S.c(this.aa);
                    if (var5_41 == -1) break;
                    var2_2.b((byte)(this.ab[var5_41] - 2));
                    var2_2.a((byte)1);
                    break;
                }
                case 36: {
                    if (var2_2.a() != 5) {
                        var5_42 = this.x.y();
                        if (var3_3.b()[0] == 0) {
                            if (var5_42 == 0) {
                                this.x.a(var3_3.b()[1], var3_3.b()[2], (byte)var3_3.b()[4], (byte)var3_3.b()[3], new int[]{1, var3_3.b()[5], var3_3.b()[6]});
                            } else if (var5_42 == 1) {
                                this.y.S.b("Ba l\u00f4 \u0111\u00e3 \u0111\u1ee7, \u0111\u00e3 \u0111\u1ec3 v\u00e0o ng\u00e2n h\u00e0ng");
                                var6_47 = game.b.b(var3_3.b()[1], var3_3.b()[2], var3_3.b()[3]);
                                this.x.a(var3_3.b()[1], var3_3.b()[2], (byte)var3_3.b()[4], (byte)var3_3.b()[3], var6_47, 0, new int[]{1, var3_3.b()[5], var3_3.b()[6]});
                            } else {
                                this.y.S.b("Kh\u00f4ng c\u00f3 kh\u00f4ng gian, \u0111\u00e3 ph\u00f3ng sinh");
                            }
                        } else if (var3_3.b()[0] == 1) {
                            this.x.n(var3_3.b()[1]);
                        }
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.y.S.ax()) break;
                    ** GOTO lbl1044
                }
                case 37: {
                    game.d.a().a(new int[][]{{var3_3.b()[0], var3_3.b()[1], var3_3.b()[2]}});
                    break;
                }
                case 38: {
                    game.c.g = false;
                    for (var6_48 = 0; var6_48 < ae.a(var3_3.c()[0], ',').length; ++var6_48) {
                        if (ae.d(ae.a(var3_3.c()[0], ',')[var6_48]) != game.k.u) continue;
                        game.c.g = true;
                        if (!game.c.h) break;
                        game.k.u = (short)-1;
                        var5_43 = ae.d(ae.a(var3_3.c()[1], ',')[var6_48]);
                        var2_2.b((byte)(var5_43 - 1));
                        game.k.a().e();
                        game.c.g = false;
                        game.c.h = false;
                        break;
                    }
                    var2_2.a((byte)6);
                    break;
                }
                case 39: {
                    for (var6_49 = 0; var6_49 < this.x.A; ++var6_49) {
                        this.x.z[var6_49].I();
                    }
                    break;
                }
                case 40: {
                    if (var2_2.a() != 5) {
                        this.y.S.c(var3_3.c()[0]);
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.y.S.ay()) break;
                    ** GOTO lbl1044
                }
                case 41: {
                    var2_2.b((byte)(var3_3.b()[0] - 2));
                    break;
                }
                case 42: {
                    var2_2.a((byte)4);
                    break;
                }
                case 45: {
                    if (var2_2.a() != 5) {
                        this.y.S.c(var3_3.c()[0]);
                        game.c.t = (byte)var3_3.b()[0];
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.y.S.ay()) break;
                    ** GOTO lbl1044
                }
                case 46: {
                    if (var2_2.a() != 5) {
                        this.y.S.H();
                        this.y.S.a(var3_3.c()[0]);
                        var2_2.a((byte)5);
                        break;
                    }
                    if (this.y.S.f == 0) {
                        if (this.y.k(196640)) {
                            this.y.S.f = 1;
                            this.y.S.a("\u0110ang l\u01b0u...");
                            this.y.S.J();
                            break;
                        }
                        if (!this.y.k(262144)) break;
                        var2_2.a((byte)1);
                        this.y.S.I();
                        this.y.S.f = 0;
                        break;
                    }
                    if (this.y.S.f == 1) {
                        this.b[game.k.a((int)this.w.f, (int)this.w.g)][var2_2.b()] = 3;
                        if (!((k)this.y).k()) break;
                        this.y.S.a("L\u01b0u th\u00e0nh c\u00f4ng");
                        this.y.S.f = 2;
                        break;
                    }
                    if (this.y.S.f != 2) break;
                    this.y.S.I();
                    this.y.S.f = 0;
                    ** GOTO lbl1044
                }
                case 47: {
                    if (this.l == -1) break;
                    var2_2.b((byte)(var3_3.b()[this.l] - 2));
                    break;
                }
                case 48: {
                    if (var2_2.a() != 5) {
                        this.D.a(var3_3.b()[1], var3_3.b()[2]);
                        this.D.a((byte)(var3_3.b()[0] / 10 - 1), var3_3.c()[0], var3_3.b()[0] % 10);
                        if (var3_3.b()[5] == 1) {
                            this.D.a(true);
                        }
                        this.D.b(var3_3.b()[3], var3_3.b()[4]);
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.D.e()) ** GOTO lbl663
                    if (!game.j.a || !this.y.k(1)) break;
                    this.D.b();
                    if (game.j.b) break;
                    b.a().a = -1;
                    this.D.c();
                    ** GOTO lbl1044
lbl663:
                    // 1 sources

                    var2_2.a((byte)1);
                    break;
                }
                case 49: {
                    if (var2_2.a() != 5) {
                        this.ad = new int[2];
                        this.ae = new int[2];
                        this.af = new String[2];
                        this.ac = new String[2];
                        for (var6_50 = 0; var6_50 < 2; ++var6_50) {
                            this.ad[var6_50] = var3_3.b()[var6_50 << 1];
                            this.ae[var6_50] = var3_3.b()[(var6_50 << 1) + 1];
                            this.af[var6_50] = var3_3.c()[var6_50];
                        }
                        this.ab = new byte[ae.a(var3_3.c()[2], ',').length];
                        for (var6_50 = 0; var6_50 < this.ab.length; ++var6_50) {
                            this.ab[var6_50] = ae.d(ae.a(var3_3.c()[2], ',')[var6_50]);
                            this.ac[var6_50] = ae.a(var3_3.c()[3], ',')[var6_50];
                        }
                        this.y.S.a(this.ad, this.ae, this.af, this.ac);
                        var2_2.a((byte)5);
                        break;
                    }
                    var6_51 = this.y.S.aD();
                    if (var6_51 == -1) break;
                    if (var6_51 == 0 && var2_2.d().b()[1] == 1) {
                        game.c.s[game.c.u][1] = 1;
                        game.c.u = (byte)(game.c.u + 1);
                    }
                    var2_2.b((byte)(this.ab[var6_51] - 2));
                    var2_2.a((byte)1);
                    break;
                }
                case 50: {
                    if (var3_3.b()[0] == 0) {
                        this.x.u();
                        break;
                    }
                    this.x.t();
                    break;
                }
                case 51: {
                    this.w.S.aB();
                    this.D.a(var3_3.b()[1], var3_3.b()[2]);
                    this.D.a((byte)(var3_3.b()[0] / 10 - 1), var3_3.c()[0], var3_3.b()[0] % 10);
                    this.D.b(var3_3.b()[3], var3_3.b()[4]);
                    break;
                }
                case 52: {
                    this.i = var3_3.b()[0] == 0;
                    if (var3_3.b()[1] == 0) {
                        game.c.j = true;
                        break;
                    }
                    game.c.j = false;
                    break;
                }
                case 53: {
                    if (var2_2.a() != 5) {
                        if (var3_3.b()[1] == 0) {
                            this.x.a((byte)var3_3.b()[0], (byte)var3_3.b()[1], (byte)2);
                            for (var6_52 = 0; var6_52 < game.k.a().d.length; ++var6_52) {
                                if (game.k.a().d[var6_52].t != 0 || game.k.a().d[var6_52].v != 1) continue;
                                game.k.a().d[var6_52].v();
                            }
                        } else if (var3_3.b()[1] == 1) {
                            this.x.a((byte)var3_3.b()[0], (byte)var3_3.b()[1], (byte)1);
                        }
                        this.y.S.a(var3_3.b()[0]);
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.w.k(1)) break;
                    this.y.S.V();
                    ** GOTO lbl1044
                }
                case 54: {
                    var6_53 = var3_3.b()[0];
                    var7_61 = new int[var6_53][3];
                    for (var8_69 = 0; var8_69 < var6_53; ++var8_69) {
                        var7_61[var8_69][0] = ae.b(ae.a(var3_3.c()[0], ',')[var8_69]);
                        var7_61[var8_69][1] = ae.b(ae.a(var3_3.c()[1], ',')[var8_69]);
                        var7_61[var8_69][2] = ae.b(ae.a(var3_3.c()[2], ',')[var8_69]);
                    }
                    game.d.a().a(var7_61);
                    break;
                }
                case 55: {
                    if (var3_3.b()[0] == 0) {
                        if (this.ag == null) {
                            this.ag = new f();
                            this.ag.a(340, false);
                            this.ag.c();
                            this.ah = game.k.a((int)var3_3.b()[3], (int)var3_3.b()[4]);
                        }
                        this.ag.b(var3_3.b()[1], var3_3.b()[2]);
                        break;
                    }
                    if (var3_3.b()[0] != 1 || this.ag == null) break;
                    this.ag.d();
                    this.ag = null;
                    this.ah = -1;
                    break;
                }
                case 56: {
                    var6_54 = var3_3.b()[1];
                    if (var3_3.b()[0] == 0) {
                        for (var7_62 = 0; var7_62 < var6_54; ++var7_62) {
                            var8_70 = ae.c(ae.a(var3_3.c()[0], ',')[var7_62]);
                            var4_23 = ae.d(ae.a(var3_3.c()[1], ',')[var7_62]);
                            this.w.d[var8_70].b(var4_23);
                            if (this.w.d[var8_70].v == 1) {
                                this.w.d[var8_70].d((byte)0);
                            }
                            this.w.d[var8_70].c();
                            this.w.a(var8_70, 1, (byte)1, true);
                            this.w.a(var8_70, 2, var4_23, true);
                            this.w.d[var8_70].r();
                        }
                    } else {
                        if (var3_3.b()[0] != 1) break;
                        for (var7_63 = 0; var7_63 < var6_54; ++var7_63) {
                            var8_71 = ae.c(ae.a(var3_3.c()[0], ',')[var7_63]);
                            if (this.w.d[var8_71].v == 1) {
                                this.w.d[var8_71].d((byte)0);
                            }
                            this.w.d[var8_71].d();
                            this.w.a(var8_71, 1, (byte)0, true);
                            this.w.d[var8_71].r();
                        }
                    }
                    break;
                }
                case 58: {
                    if (((a)this.x.p).h() != 1 || !((a)this.x.p).a.e()) break;
                    ((a)this.x.p).d((byte)0);
                    this.w.d[var3_3.b()[0]].b(var3_3.b()[1], var3_3.b()[2]);
                    if ((a)this.w.d[var3_3.b()[0]].p == null) break;
                    ((a)this.w.d[var3_3.b()[0]].p).q();
                    this.w.d[var3_3.b()[0]].a(null);
                    break;
                }
                case 60: {
                    if (var2_2.a() != 5) {
                        this.E = new short[var3_3.b()[0]];
                        for (var6_55 = 0; var6_55 < this.E.length; ++var6_55) {
                            this.E[var6_55] = ae.c(ae.a(var3_3.c()[0], ',')[var6_55]);
                            this.w.d[this.E[var6_55]].d(ae.d(ae.a(var3_3.c()[1], ',')[var6_55]));
                            if (this.w.d[this.E[var6_55]].t != 0 || this.w.d[this.E[var6_55]].v != 6 || this.w.d[this.E[var6_55]].h() != 2) continue;
                            game.k.a().b.c(game.k.a().d[this.E[var6_55]]);
                        }
                        this.B = 0;
                        var2_2.a((byte)5);
                        break;
                    }
                    for (var6_56 = 0; var6_56 < this.E.length; ++var6_56) {
                        if (!this.w.d[this.E[var6_56]].b()) continue;
                        ++this.B;
                    }
                    if (this.B < this.E.length) break;
                    this.B = 0;
                    ** GOTO lbl1044
                }
                case 62: {
                    var6_57 = new int[ae.a(var3_3.c()[0], ',').length];
                    var8_72 = -1;
                    var2_2.a((byte)6);
                    for (var7_64 = 0; var7_64 < var6_57.length; ++var7_64) {
                        var6_57[var7_64] = ae.b(ae.a(var3_3.c()[0], ',')[var7_64]);
                        if (this.w.d[var6_57[var7_64]].h() != 2) continue;
                        var8_72 = var6_57[var7_64];
                        break;
                    }
                    if (var8_72 < 0) break;
                    if (var8_72 == var3_3.b()[0]) ** GOTO lbl818
                    var2_2.b((byte)(var3_3.b()[2] - 2));
                    ** GOTO lbl1044
lbl818:
                    // 1 sources

                    var2_2.b((byte)(var3_3.b()[1] - 2));
                    var2_2.a((byte)1);
                    break;
                }
                case 63: {
                    if (var3_3.b()[0] == 0) {
                        this.x.h(var3_3.b()[1]);
                    } else {
                        this.x.s();
                    }
                    this.k = var3_3.b()[2] != 0;
                    break;
                }
                case 64: {
                    if (var3_3.b()[0] == 0) {
                        this.w.a(var3_3.b()[1]);
                        if (var3_3.b()[2] == -1) {
                            this.w.a((f)this.x);
                            break;
                        }
                        this.w.a(this.w.d[var3_3.b()[2]]);
                        break;
                    }
                    this.w.g();
                    break;
                }
                case 65: {
                    if (var2_2.a() != 5 && !game.c.X) {
                        this.y.a((byte)100);
                        var2_2.a((byte)5);
                        break;
                    }
                    if (game.c.X) {
                        var2_2.b((byte)(var3_3.b()[0] - 2));
                    } else {
                        var2_2.b((byte)(var3_3.b()[1] - 2));
                    }
                    ** GOTO lbl1044
                }
                case 66: {
                    an.U = (byte)var3_3.b()[0];
                    an.c(0, 3);
                    break;
                }
                case 67: {
                    game.k.v = var3_3.b()[0];
                    break;
                }
                case 70: {
                    if (var2_2.a() != 5) {
                        game.c.e = false;
                        this.c = var3_3.b()[0];
                        switch (var3_3.b()[0]) {
                            case 0: 
                            case 1: {
                                this.w.a((byte)1);
                                break;
                            }
                            case 2: {
                                this.w.a((byte)16);
                            }
                        }
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!game.c.e) break;
                    this.c = -1;
                    ** GOTO lbl1044
                }
                case 71: {
                    if (this.x.F >= var3_3.b()[0]) {
                        var2_2.b((byte)(var3_3.b()[1] - 2));
                        break;
                    }
                    var2_2.b((byte)(var3_3.b()[2] - 2));
                    break;
                }
                case 72: {
                    var6_58 = ae.a(var3_3.c()[0], ',');
                    var7_65 = ae.a(var3_3.c()[1], ',');
                    var8_73 = new f[var6_58.length];
                    for (var4_24 = 0; var4_24 < var6_58.length; ++var4_24) {
                        var8_73[var4_24] = new f();
                        var8_73[var4_24].a(259, false);
                        if (ae.b(var6_58[var4_24]) == -1) {
                            var8_73[var4_24].a(ae.d(var7_65[var4_24]), (byte)-1, true);
                            var8_73[var4_24].c();
                            var8_73[var4_24].b(this.x.l(), this.x.m() - 40);
                            var8_73[var4_24].a(this.x);
                        } else {
                            var8_73[var4_24].a(ae.d(var7_65[var4_24]), (byte)-1, true);
                            var8_73[var4_24].c();
                            var8_73[var4_24].b(this.w.d[ae.b(var6_58[var4_24])].l(), this.w.d[ae.b(var6_58[var4_24])].m() - 40);
                            var8_73[var4_24].a(this.w.d[ae.b(var6_58[var4_24])]);
                        }
                        game.c.C.addElement(var8_73[var4_24]);
                    }
                    break;
                }
                case 74: {
                    if (((int[])this.x.K.elementAt(0))[1] > 0) {
                        var2_2.b((byte)(var3_3.b()[0] - 2));
                        break;
                    }
                    var2_2.b((byte)(var3_3.b()[1] - 2));
                    break;
                }
                case 76: {
                    this.b[game.k.l[this.w.f] + this.w.g][var2_2.b()] = 3;
                    game.k.a().f = var3_3.b()[0];
                    game.k.a().g = var3_3.b()[1];
                    game.k.a().j = -1;
                    this.w.a((byte)29);
                    break;
                }
                case 77: {
                    this.b[game.k.a((int)var3_3.b()[0], (int)var3_3.b()[1])][var3_3.b()[2]] = 4;
                    if (var3_3.b()[0] != this.w.f || var3_3.b()[1] != this.w.g) break;
                    this.a[var3_3.b()[2]].a((byte)4);
                    break;
                }
                case 80: {
                    if (var2_2.a() != 5) {
                        if (var3_3.b()[0] == 0) {
                            this.B = 0;
                            this.n = (byte)4;
                        } else if (var3_3.b()[0] == 1) {
                            game.i.a().c = game.i.a().b;
                            var7_66 = game.i.a().c - game.i.a().a;
                            if (ae.a(var7_66)[2] <= 70L) {
                                var4_25 = this.x.y();
                                if (var4_25 == 0) {
                                    this.y.S.b("\u0110\u1ea1t \u0111\u01b0\u1ee3c #2L\u1ee5c h\u00e0nh \u0111i\u1ec3u");
                                    this.x.a(54, 5, (byte)2, (short)-1, new int[]{1, 30, 45});
                                } else if (var4_25 == 1) {
                                    this.y.S.b("\u0110\u1ea1t \u0111\u01b0\u1ee3c #2L\u1ee5c h\u00e0nh \u0111i\u1ec3u#0 ba l\u00f4 \u0111\u00e3 \u0111\u1ee7, \u0111\u00e3 \u0111\u1ec3 v\u00e0o ng\u00e2n h\u00e0ng");
                                    var4_25 = ae.b(aq.c[0][54][3], (int)aq.c[0][54][3]);
                                    this.x.a(54, 5, (byte)2, (byte)var4_25, 0, 0, new int[]{1, 30, 45});
                                } else {
                                    this.y.S.b("Kh\u00f4ng c\u00f3 kh\u00f4ng gian, \u0111\u00e3 ph\u00f3ng sinh");
                                }
                            } else if (ae.a(var7_66)[2] <= 80L) {
                                this.x.s(1000);
                                this.y.S.b("Th\u01b0\u1edfng 1000 kim");
                            } else if (ae.a(var7_66)[2] <= 130L) {
                                this.x.s(750);
                                this.y.S.b("Th\u01b0\u1edfng 750 kim");
                            } else if (ae.a(var7_66)[2] <= 200L) {
                                this.x.s(600);
                                this.y.S.b("Th\u01b0\u1edfng 600 kim");
                            }
                            game.i.a().b = 0L;
                            game.i.a().a = 0L;
                        }
                        var2_2.a((byte)5);
                        break;
                    }
                    if (var3_3.b()[0] == 0) {
                        ++this.B;
                        if (this.n > 0) {
                            if (this.B / 10 == 0 || this.B % 10 != 0) break;
                            this.n = (byte)(this.n - 1);
                            break;
                        }
                        this.n = 0;
                        game.i.a().b = game.i.a().a = System.currentTimeMillis();
                        game.i.a().c = 0L;
                    } else {
                        if (var3_3.b()[0] != 1 || !this.y.S.ax()) break;
                        if (this.p == 0) {
                            this.aj = this.j();
                        }
                        this.p = (byte)(this.p + 1);
                    }
                    ** GOTO lbl1044
                }
                case 81: {
                    if (var3_3.b()[0] == 0) {
                        if (this.x.t(var3_3.b()[1])) {
                            var2_2.b((byte)(var3_3.b()[2] - 2));
                            break;
                        }
                        var2_2.b((byte)(var3_3.b()[3] - 2));
                        break;
                    }
                    if (var3_3.b()[0] != 1) break;
                    if (this.x.v(var3_3.b()[1])) {
                        var2_2.b((byte)(var3_3.b()[2] - 2));
                        break;
                    }
                    var2_2.b((byte)(var3_3.b()[3] - 2));
                    break;
                }
                case 82: {
                    var7_67 = var3_3.b()[0];
                    var8_74 = ae.e(var3_3.c()[0]);
                    for (var4_26 = 0; var4_26 < var7_67; ++var4_26) {
                        this.w.d[var8_74[var4_26]].r();
                        this.w.d[var8_74[var4_26]].s();
                    }
                    break;
                }
                case 83: {
                    if (var2_2.a() != 5) {
                        this.y.a((byte)30);
                        var2_2.a((byte)5);
                        break;
                    }
                    var2_2.b((byte)(var3_3.b()[game.c.m] - 2));
                    ** GOTO lbl1044
                }
                case 84: {
                    if (var2_2.a() != 5) {
                        var4_27 = null;
                        if (var3_3.b()[2] == 1) {
                            var4_27 = new int[]{this.p, 5 - this.p};
                        } else if (var3_3.b()[2] == 0) {
                            var4_27 = new int[]{this.x.I, this.x.R.length - this.x.I};
                        }
                        var4_27 = game.c.a(var3_3.c()[1], var4_27);
                        this.w.S.a(var3_3.c()[0], (String)var4_27, (int)var3_3.b()[1]);
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.w.S.c(var3_3.b()[1], -1) || !this.y.k(196640)) break;
                    game.k.a().e();
                    if (ae.b < ae.b()) {
                        ae.c();
                        this.w.S.b(ae.b);
                        break;
                    }
                    if (game.k.u != -1 && this.w.d[game.k.u].a.a <= 85 && this.w.d[game.k.u].u() == 0) {
                        game.k.a().a(game.k.a().d[game.k.u].i, game.k.a().d[game.k.u].j - 40, game.k.a().d[game.k.u]);
                    }
                    game.c.g = false;
                    game.c.h = false;
                    this.w.S.aC();
                    var2_2.a((byte)1);
                    break;
                }
                case 85: {
                    if (this.p >= 0 && this.p < 5) {
                        var2_2.b((byte)(var3_3.b()[0] - 2));
                        break;
                    }
                    var2_2.b((byte)(var3_3.b()[1] - 2));
                    break;
                }
                case 87: {
                    if (var2_2.a() != 5) {
                        if (var3_3.b()[0] == 0) {
                            this.x.a(var3_3.b()[7], var3_3.b()[1], var3_3.b()[2], (byte)var3_3.b()[4], (byte)var3_3.b()[3], new int[]{1, var3_3.b()[5], var3_3.b()[6]});
                        } else if (var3_3.b()[0] == 1) {
                            this.x.n(var3_3.b()[1]);
                        }
                        var2_2.a((byte)5);
                        break;
                    }
                    if (!this.y.S.ax()) break;
                    ** GOTO lbl1044
                }
                case 88: {
                    if (this.x.y() == 2) {
                        var2_2.b((byte)(var3_3.b()[0] - 2));
                    } else {
                        var2_2.b((byte)(var3_3.b()[1] - 2));
                    }
lbl1044:
                    // 31 sources

                    var2_2.a((byte)1);
                }
            }
            if (var2_2.a() != 5 && var2_2.a() != 6) {
                var2_2.e();
            }
            if (var2_2.a() == 3 || var2_2.a() == 4) {
                game.c.h = false;
                this.z.removeElement(var2_2);
                var4_28 = game.k.a(var2_2.f()[0], var2_2.f()[1]);
                if (this.b[var4_28] != null) {
                    this.b[var4_28][var2_2.b()] = var2_2.a();
                }
                if (var2_2.a() == 3 && var2_2.d().a() == 44 && var2_2.d().b()[1] == 1) {
                    game.c.b(var2_2.d().b()[0]);
                }
                this.i();
                continue;
            }
            ++var1_1;
        }
    }

    public final byte g() {
        return this.A;
    }

    public final boolean h() {
        if (this.a == null) {
            return false;
        }
        for (int i2 = 0; i2 < this.z.size(); ++i2) {
            p p2 = (p)this.z.elementAt(i2);
            if (p2.a() == 2 || p2.a() == 6) continue;
            return true;
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     */
    private boolean a(ad ad2) {
        boolean bl = false;
        if (ad2.b()[7] != -1) {
            if (ad2.b()[7] == -1) return bl;
            if (this.b[game.k.a((int)ad2.b()[5], (int)ad2.b()[6])] == null) return bl;
            if (this.b[game.k.a((int)ad2.b()[5], (int)ad2.b()[6])][ad2.b()[7]] != 3) return bl;
        }
        switch (ad2.b()[8]) {
            case 0: {
                if (!this.x.T[ad2.b()[9]]) return bl;
                break;
            }
            case 1: {
                return true;
            }
            case 2: {
                int n2;
                if (this.x.O.size() + this.x.A < ad2.b()[9]) return bl;
                for (n2 = 0; n2 < this.x.A; ++n2) {
                    if (this.x.z[n2].s() != ad2.b()[10]) continue;
                    bl = true;
                    break;
                }
                if (bl) return bl;
                n2 = 0;
                while (n2 < this.x.O.size()) {
                    if (((int[])this.x.O.elementAt(n2))[1] == ad2.b()[10]) return true;
                    ++n2;
                }
                return bl;
            }
            case 3: {
                if (this.x.F < ad2.b()[9]) return bl;
                break;
            }
            case 4: {
                if (this.x.a((byte)ad2.b()[9], (int)ad2.b()[10]) != 2) return bl;
                break;
            }
            case 5: {
                if (t <= ad2.b()[9]) return bl;
                break;
            }
            case 6: {
                if (t != ad2.b()[9]) return bl;
            }
        }
        return true;
    }

    private boolean b(ad object) {
        boolean bl = false;
        if (((ad)object).b()[7] == -1 || ((ad)object).b()[7] != -1 && this.b[game.k.a((int)((ad)object).b()[5], (int)((ad)object).b()[6])] != null && this.b[game.k.a((int)((ad)object).b()[5], (int)((ad)object).b()[6])][((ad)object).b()[7]] == 3) {
            switch (((ad)object).b()[8]) {
                case 0: {
                    if (this.x.a((byte)((ad)object).b()[9], (int)((ad)object).b()[10]) != 2) break;
                    bl = true;
                    break;
                }
                case 1: {
                    if (!this.x.T[((ad)object).b()[9]]) break;
                    bl = true;
                    break;
                }
                case 2: 
                case 4: {
                    if (this.b[game.k.a((int)((ad)object).b()[5], (int)((ad)object).b()[6])] == null || this.b[game.k.a((int)((ad)object).b()[5], (int)((ad)object).b()[6])][((ad)object).b()[7]] != 3) break;
                    bl = true;
                    break;
                }
                case 3: {
                    if (!this.x.b((int)((ad)object).b()[9], (int)((ad)object).b()[10], (byte)0)) break;
                    bl = true;
                    break;
                }
                case 5: {
                    if (this.x.F < ((ad)object).b()[9]) break;
                    bl = true;
                    break;
                }
                case 6: {
                    int n2;
                    object = new byte[]{0, 1, 2, 3};
                    block8: for (n2 = 0; n2 < this.x.A; ++n2) {
                        for (int i2 = 0; i2 < ((Object)object).length; ++i2) {
                            if (object[i2] == -1 || object[i2] != aq.a((byte)0, (short)this.x.z[n2].q(), (byte)1)) continue;
                            object[i2] = -1;
                            continue block8;
                        }
                    }
                    for (n2 = 0; n2 < ((Object)object).length && object[n2] == -1; ++n2) {
                    }
                    if (n2 < ((Object)object).length) break;
                    bl = true;
                }
            }
        }
        return bl;
    }

    public final void i() {
        a a2;
        f f2;
        ad ad2;
        int n2;
        if (d == null) {
            d = new Vector();
        }
        d.removeAllElements();
        Vector<String> vector = new Vector<String>();
        for (n2 = 0; n2 < al.length; ++n2) {
            if (game.k.a((int)al[n2][0], (int)al[n2][1]) != game.k.a(game.k.a().f, game.k.a().g) || this.a[al[n2][2]].a() != 0 && this.a[al[n2][2]].a() != 4 || vector.contains("" + (ad2 = this.a[al[n2][2]].d()).b()[4])) continue;
            if (this.b(ad2)) {
                f2 = new f();
                f2.a(259, false);
                f2.a((byte)1, (byte)-1, true);
                f2.b(this.w.d[ad2.b()[4]].i, this.w.d[ad2.b()[4]].j - 40);
                this.w.d[ad2.b()[4]].t();
                a2 = this.w.d[ad2.b()[4]];
                f2.p = a2;
                f2.c();
                d.addElement(f2);
                vector.addElement("" + ad2.b()[4]);
                continue;
            }
            int n3 = game.c.c(ad2.b()[0]);
            if ((ad2.b()[1] != 0 || this.b[game.k.a((int)ak[n2][0], (int)ak[n2][1])][ak[n2][2]] != 3 || this.b[game.k.a((int)al[n2][0], (int)al[n2][1])][al[n2][2]] == 3) && (ad2.b()[1] != 1 || n3 == -1 || s[n3][1] != 1)) continue;
            f f3 = new f();
            f3.a(259, false);
            f3.a((byte)15, (byte)-1, true);
            f3.b(this.w.d[ad2.b()[4]].i, this.w.d[ad2.b()[4]].j - 40);
            this.w.d[ad2.b()[4]].t();
            a2 = this.w.d[ad2.b()[4]];
            f3.p = a2;
            f3.c();
            d.addElement(f3);
            vector.addElement("" + ad2.b()[4]);
        }
        for (n2 = 0; n2 < ak.length; ++n2) {
            if (game.k.a((int)ak[n2][0], (int)ak[n2][1]) != game.k.a(game.k.a().f, game.k.a().g) || this.a[ak[n2][2]].a() != 0 && this.a[ak[n2][2]].a() != 4 || vector.contains("" + (ad2 = this.a[ak[n2][2]].d()).b()[4]) || !this.a(ad2)) continue;
            f2 = new f();
            f2.a(259, false);
            f2.a((byte)7, (byte)-1, true);
            f2.b(this.w.d[ad2.b()[4]].i, this.w.d[ad2.b()[4]].j - 40);
            this.w.d[ad2.b()[4]].t();
            a2 = this.w.d[ad2.b()[4]];
            f2.p = a2;
            f2.c();
            d.addElement(f2);
        }
    }

    public final int[] j() {
        int[] nArray = new int[4];
        int[] nArray2 = nArray;
        nArray[0] = this.ai.get(1);
        nArray2[1] = this.ai.get(2);
        nArray2[2] = this.ai.get(5);
        nArray2[3] = this.ai.get(11);
        return this.aj;
    }

    public final int[] k() {
        return this.aj;
    }

    public final void a(int[] nArray) {
        this.aj = nArray;
    }

    static {
        e = false;
        f = true;
        g = false;
        h = false;
        j = true;
        m = (byte)-1;
        s = null;
        t = 0;
        u = 0;
    }
}

