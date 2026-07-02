/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Graphics
 */
package game;

import game.c;
import game.k;
import javax.microedition.lcdui.Graphics;

public final class j {
    private static int c = 0;
    private char[] d;
    private int e;
    private int f;
    private int g;
    private int h;
    private int i;
    private int[] j;
    private int k;
    private int l;
    private int m;
    private int n;
    private int o;
    private int p;
    private int q;
    private int r;
    public static boolean a = false;
    private int s;
    public static boolean b = false;
    private byte t;
    private static j u;
    private boolean v;
    private int w;
    private int[][] x;
    private int y;
    private char[] z;
    private char[] A;
    private String[] B;
    private int C;
    private long D;
    private int E;

    public j() {
        an.G();
        this.g = 0;
        this.h = 0;
        this.i = 0;
        this.j = new int[10];
        this.k = 0;
        this.l = 0;
        this.m = 2;
        this.n = 0;
        this.q = 0;
        this.r = 2;
        this.s = 0;
        this.t = 0;
        this.w = 0;
        this.x = null;
        this.y = 0;
        this.z = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        this.A = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        this.B = new String[]{"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
    }

    public static j a() {
        if (u == null) {
            u = new j();
        }
        return u;
    }

    public final void b() {
        char[] cArray = this.d;
        switch (c) {
            case 0: {
                int n2 = cArray.length;
                int n3 = this.E - 10;
                int n4 = n3 - s.b;
                int n5 = 1;
                int n6 = 0;
                while (n6 < 2) {
                    if (this.h < n2) {
                        char c2;
                        this.D = 0L;
                        if (this.h == 0) {
                            this.C = 0;
                        }
                        if ((c2 = cArray[this.h]) == '#') {
                            this.i += 7;
                            this.h += 7;
                        } else {
                            int n7 = s.a(c2);
                            int n8 = this.C + n7;
                            if (n8 > n3 || c2 == ' ' && n8 > n4) {
                                n8 = 0;
                                this.C = 0;
                                if (c2 != ' ') {
                                    n8 = n7 + 0;
                                }
                                if (++n5 > this.q) {
                                    n5 = 0;
                                    this.g = this.h;
                                }
                            }
                            this.C = n8;
                            ++this.g;
                        }
                    } else if (this.D == 0L) {
                        this.D = System.currentTimeMillis() + 2500L;
                    } else if (System.currentTimeMillis() > this.D) {
                        j j2 = this;
                        if (!j2.v) {
                            this.c();
                            b = false;
                            if (game.k.a().R.b("/data/ui/dialog.ui")) {
                                game.k.a().S.aC();
                            }
                        }
                        a = true;
                        ++this.r;
                    }
                    ++n6;
                }
                game.c.o = null;
                this.j[0] = this.j[this.l];
                b = false;
                this.g = 0;
                this.h = 0;
                this.i = 0;
                a = false;
                return;
            }
            case 1: {
                b = false;
                this.g = 0;
                this.h = 0;
                this.i = 0;
                return;
            }
            case 2: {
                return;
            }
            case 3: {
                if (this.s >= this.r * (this.y + 1)) break;
                if (this.r * (this.y + 1) > (null).length) {
                    this.s = (null).length;
                    return;
                }
                this.s = this.r * (this.y + 1);
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    public final void a(Graphics var1_1) {
        block28: {
            if (!game.j.b) break block28;
            block0 : switch (game.j.c) {
                case 0: {
                    switch (this.t) {
                        case 0: {
                            var1_1.setColor(0xFFFFFF);
                            this.a(var1_1, this.o, this.p);
                            break;
                        }
                        case 1: {
                            this.a(var1_1, this.o, this.p - (an.G() >> 1));
                        }
                    }
                    break;
                }
                case 1: {
                    switch (this.t) {
                        case 0: {
                            var2_2 = 0;
                            while (var2_2 < ae.a.length) {
                                var1_1.setColor(0xFFFFFF);
                                var1_1.drawString(ae.a[var2_2], this.o, this.p + var2_2 * (an.G() + an.G() / 2), 20);
                                ++var2_2;
                            }
                            break block0;
                        }
                        case 1: {
                            var2_3 = 0;
                            while (var2_3 < ae.a.length) {
                                var1_1.setColor(0xFFFFFF);
                                var1_1.drawString(ae.a[var2_3], this.o, (this.p - (an.G() + an.G() / 2) * ae.a.length >> 1) + var2_3 * (an.G() + an.G() / 2), 17);
                                ++var2_3;
                            }
                            break block10;
                        }
                    }
                    break;
                }
                case 2: {
                    switch (this.t) {
                        case 0: {
                            var2_4 = 0;
                            while (var2_4 < ae.a.length) {
                                var1_1.setColor(0xFFFFFF);
                                var1_1.drawString(ae.a[var2_4], this.o, this.p + var2_4 * (an.G() + an.G() / 2) + this.n, 20);
                                ++var2_4;
                            }
                            break block0;
                        }
                        case 1: {
                            var2_5 = 0;
                            while (var2_5 < ae.a.length) {
                                var1_1.setColor(0xFFFFFF);
                                var1_1.drawString(ae.a[var2_5], this.o, (this.p - (an.G() + an.G() / 2) * ae.a.length >> 1) + var2_5 * (an.G() + an.G() / 2) + this.n, 17);
                                ++var2_5;
                            }
                            break block14;
                        }
                    }
                    break;
                }
                case 3: {
                    var2_6 = this.p;
                    var3_7 = this.o;
                    var4_8 = this.d.length;
                    var5_9 = this.y * this.r;
                    while (var5_9 < this.s) {
                        var6_10 = s.a * (20 - this.y * this.r) + var2_6;
                        var7_11 = this.f + var3_7;
                        var8_12 = null[0];
                        ** GOTO lbl78
                        {
                            var9_13 = "";
                            var10_15 = 0;
                            while (var10_15 < 7) {
                                var9_13 = String.valueOf(var9_13) + this.d[var8_12 + var10_15];
                                ++var10_15;
                            }
                            this.k = ++this.k >= this.j.length ? 0 : this.k;
                            this.j[this.k] = this.a(var9_13);
                            var1_1.setColor(this.j[this.k]);
                            var8_12 += 7;
                            do {
                                if (var8_12 < var4_8 && this.d[var8_12] == '#') continue block23;
                                var1_1.setColor(this.j[this.k]);
                                if (var8_12 < var4_8) {
                                    var9_14 = this.d[var8_12];
                                    s.a(var1_1, var9_14, var7_11, var6_10);
                                    var7_11 += s.a(var9_14);
                                }
                                ++var8_12;
lbl78:
                                // 2 sources

                            } while (var8_12 < null[1]);
                        }
                        ++var5_9;
                    }
                    this.l = this.k;
                    this.k = 0;
                }
            }
            if (this.v && game.j.a && this.s % 10 < 5) {
                var1_1.setColor(0xFFFFFF);
                var1_1.drawString("Nh\u1ea5n n\u00fat 0 \u0111\u1ec3 ti\u1ebfp t\u1ee5c", an.w() >> 1, an.x() - 8, 33);
            }
        }
    }

    public final void c() {
        this.d = null;
        ae.a = null;
        this.s = 0;
    }

    public final void a(byte by, String object, int n2) {
        this.d = ((String)object).toCharArray();
        c = by;
        switch (by) {
            case 0: {
                j j2 = this;
                object = j2.d;
                int n3 = j2.d.length;
                int n4 = 0;
                int n5 = 0;
                int n6 = j2.E - 10;
                int n7 = n6 - s.b;
                StringBuffer stringBuffer = new StringBuffer();
                while (j2.h < n3) {
                    Object object2 = object[j2.g];
                    if (object2 == 35) {
                        j2.i += 7;
                        j2.h += 7;
                        continue;
                    }
                    ++j2.h;
                    int n8 = s.a((char)object2);
                    int n9 = n5 + n8;
                    stringBuffer.append((char)object2);
                    if (n5 > n6 || object2 == 32 && n5 > n7) {
                        n9 = 0;
                        if (object2 != 32) {
                            n9 = n8 + 0;
                        }
                        ++n4;
                        stringBuffer = new StringBuffer();
                    }
                    n5 = n9;
                }
                if (n5 > 0) {
                    ++n4;
                }
                this.r = n4;
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                break;
            }
            case 3: {
                this.y = 0;
                c = 3;
                this.g = 0;
                this.h = 0;
                this.i = 0;
                char[] cArray = this.d;
                int n10 = this.d.length;
                int[][] nArray = new int[50][2];
                int n11 = 0;
                int n12 = this.E - 10;
                int n13 = n12 - s.b;
                int n14 = 0;
                int n15 = 0;
                int n16 = 0;
                while (this.h < n10) {
                    char c2 = cArray[n16];
                    if (c2 == '#') {
                        n16 += 7;
                    } else {
                        int n17 = s.a(c2);
                        int n18 = n11 + n17;
                        if (n11 > n12 || c2 == ' ' && n11 > n13) {
                            n18 = 0;
                            if (c2 != ' ') {
                                n18 = n17 + 0;
                            }
                            nArray[n15][0] = n14;
                            nArray[n15][1] = n16;
                            n14 = n16;
                            ++n15;
                        }
                        n11 = n18;
                        ++n16;
                    }
                    ++n16;
                }
                this.a(0, 80);
            }
        }
        this.g = 0;
        this.h = 0;
        this.i = 0;
        b = true;
        a = false;
        this.v = false;
        this.t = (byte)n2;
    }

    public final void a(int n2, int n3) {
        this.o = n2;
        this.p = n3;
        this.E = an.w() - 2 * this.o;
        this.f = 0;
    }

    public final void b(int n2, int n3) {
        this.e = n2 / an.F();
        this.f = n2 - this.e * an.F() >> 1;
        this.r = n3 / an.G();
    }

    /*
     * Unable to fully structure code
     */
    private void a(Graphics var1_1, int var2_2, int var3_3) {
        this.q = this.g;
        var4_4 = this.d;
        var5_5 = this.d.length;
        var6_6 = var2_2;
        var7_7 = this.E + var2_2 - 10;
        var8_8 = var7_7 - s.b;
        var9_9 = this.g;
        ** GOTO lbl36
        {
            var10_10 = "";
            var11_12 = 0;
            while (var11_12 < 7) {
                var10_10 = String.valueOf(var10_10) + var4_4[var9_9 + var11_12];
                ++var11_12;
            }
            this.k = ++this.k >= this.j.length ? 0 : this.k;
            this.j[this.k] = this.a(var10_10);
            var1_1.setColor(this.j[this.k]);
            var9_9 += 7;
            do {
                if (var9_9 < var5_5 && var4_4[var9_9] == '#') continue block0;
                var1_1.setColor(this.j[this.k]);
                if (var9_9 < var5_5) {
                    var10_11 = var4_4[var9_9];
                    var11_12 = s.a(var10_11);
                    var12_13 = var6_6 + var11_12;
                    if (var12_13 > var7_7 || var10_11 == ' ' && var12_13 > var8_8) {
                        var6_6 = var12_13 = var2_2;
                        if (var10_11 != ' ') {
                            var12_13 = var6_6 + var11_12;
                        }
                        var3_3 += s.a + 1;
                    }
                    s.a(var1_1, var10_11, var6_6, var3_3);
                    var6_6 = var12_13;
                }
                ++this.q;
                ++var9_9;
lbl36:
                // 2 sources

            } while (var9_9 < this.h);
        }
        this.l = this.k;
        this.k = 0;
    }

    private int a(String object) {
        object = ((String)object).toCharArray();
        StringBuffer stringBuffer = new StringBuffer();
        int n2 = 0;
        while (n2 < ((Object)object).length) {
            stringBuffer.append(this.a((char)object[n2]));
            ++n2;
        }
        object = stringBuffer.toString().toCharArray();
        n2 = 0;
        int n3 = 0;
        while (n3 < ((Object)object).length) {
            if (object[n3] == 49) {
                n2 += 1 << ((Object)object).length - n3 - 1;
            }
            ++n3;
        }
        return n2;
    }

    private String a(char c2) {
        int n2 = 0;
        while (n2 < this.z.length) {
            if (c2 == this.z[n2] || c2 == this.A[n2]) {
                return this.B[n2];
            }
            ++n2;
        }
        return "0000";
    }

    public final void d() {
        char[] cArray = this.d;
        if (b) {
            switch (c) {
                case 0: {
                    int n2 = cArray.length;
                    int n3 = this.E - 10;
                    int n4 = n3 - s.b;
                    int n5 = 1;
                    int n6 = 0;
                    while (n6 < 2) {
                        if (this.h < n2) {
                            char c2;
                            this.D = 0L;
                            if (this.w == 0) {
                                this.C = 0;
                            }
                            if ((c2 = cArray[this.w]) == '#') {
                                this.i += 7;
                                this.h += 7;
                            } else {
                                int n7 = s.a(c2);
                                int n8 = this.C + n7;
                                if (n8 > n3 || c2 == ' ' && n8 > n4) {
                                    n8 = 0;
                                    this.C = 0;
                                    if (c2 != ' ') {
                                        n8 = n7 + 0;
                                    }
                                    if (++n5 > this.r) {
                                        n5 = 0;
                                        this.g = this.h;
                                    }
                                }
                                this.C = n8;
                                ++this.w;
                            }
                        } else if (this.D == 0L) {
                            this.D = System.currentTimeMillis() + 2500L;
                        } else if (System.currentTimeMillis() > this.D) {
                            j j2 = this;
                            if (!j2.v) {
                                this.c();
                                b = false;
                                if (game.k.a().R.b("/data/ui/dialog.ui")) {
                                    game.k.a().S.aC();
                                }
                            }
                            a = true;
                            ++this.s;
                        }
                        ++n6;
                    }
                    return;
                }
                case 1: {
                    a = true;
                    ++this.s;
                    return;
                }
                case 2: {
                    this.n += this.m;
                    if (this.n <= ae.a.length * (an.x() + an.x() / 2)) break;
                    this.n = 0;
                    a = true;
                    b = false;
                    ++this.s;
                    return;
                }
                case 3: {
                    if (this.s >= (null).length) break;
                    ++this.w;
                    if (this.w < 20) break;
                    ++this.s;
                    if (this.s > this.r * (this.y + 1)) {
                        ++this.y;
                        this.j[0] = this.j[this.l];
                    }
                    this.w = 0;
                }
            }
        }
    }

    public final void a(boolean bl) {
        this.v = true;
    }

    public final boolean e() {
        return this.v;
    }
}

