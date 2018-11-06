/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.dev.test.collator;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import android.icu.dev.test.TestFmwk;
import android.icu.text.Collator;

public class CollationThreadTest extends TestFmwk {
    private static final String[] threadTestData;
    static {
        final Collator collator = Collator.getInstance(new Locale("pl", "", ""));
        final String[] temporaryData = {
                "Banc Se\u00F3kdyaaouq Pfuymjec",
                "BSH \u00F3y",
                "ABB - \u00F3g",
                "G\u00F3kpo Adhdoetpwtx Twxma, qm. Ilnudx",
                "G\u00F3bjh Zcgopqmjidw Dyhlu, ky. Npyamr",
                "G\u00F3dxb Slfduvgdwfi Qhreu, ao. Adyfqx",
                "G\u00F3ten Emrmbmttgne Rtpir, rx. Mgmpjy",
                "G\u00F3kjo Hciqkymfcds Jpudo, ti. Ueceedbm (tkvyj vplrnpoq)",
                "Przjrpnbhrflnoo Dbiccp Lnmikfhsuo\u00F3s Tgfhlpqoso / ZAD ENR",
                "Bang Nbygmoyc Nd\u00F3nipcryjtzm",
                "Citjk\u00EBd Qgmgvr Er. w u.x.",
                "Dyrscywp Kvoifmyxo Ivv\u00F3r Lbyxtrwnzp",
                "G\u00E9awk Ssqenl Pk. c r.g.",
                "Nesdo\u00E9 Ilwbay Z.U.",
                "Poczsb Lrdtqg",
                "Pocafu Tgbmpn - wwg zo Mpespnzdllqk",
                "Polyvmg Z.C.",
                "POLUHONANQ FO",
                "Polrpycn",
                "Poleeaw-Rqzghgnnj R.W.",
                "Polyto Sgrgcvncz",
                "Polixj Tyfc\u00F3vcga Gbkjxf\u00F3f Tuogcybbbkyd C.U.",
                "Poltmzzlrkwt",
                "Polefgb Oiqefrkq",
                "Polrfdk K\u00F3nvyrfot Xuzbzzn f Ujmfwkdbnzh E.U. Wxkfiwss",
                "Polxtcf Hfowus Zzobblfm N.I.",
                "POLJNXO ZVYU L.A.",
                "PP Lowyr Rmknyoew",
                "Pralpe",
                "Preyojy Qnrxr",
                "PRK -5",
                "PRONENC U.P.",
                "Prowwyq & Relnda Hxkvauksnn Znyord Tz. w t.o.",
                "Propydv Afobbmhpg",
                "Proimpoupvp",
                "Probfo Hfttyr",
                "Propgi Lutgumnj X.W. BL",
                "Prozkch K.E.",
                "Progiyvzr Erejqk T.W.",
                "Prooxwq-Ydglovgk J.J.",
                "PTU Ntcw Lwkxjk S.M. UYF",
                "PWN",
                "PWP",
                "PZU I.D. Tlpzmhax",
                "PZU ioii A.T. Yqkknryu - bipdq badtg 500/9",
                "Qumnl-Udffq",
                "Radmvv",
                "Railoggeqd Aewy Fwlmsp K.S. Ybrqjgyr",
                "Remhmxkx Ewuhxbg",
                "Renafwp Sapnqr io v z.n.",
                "Repqbpuuo",
                "Resflig",
                "Rocqz Mvwftutxozs VQ",
                "Rohkui",
                "RRC",
                "Samgtzg Fkbulcjaaqv Ollllq Ad. l l.v.",
                "Schelrlw Fu. t z.x.",
                "Schemxgoc Axvufoeuh",
                "Siezsxz Eb. n r.h",
                "Sikj Wyvuog",
                "Sobcwssf Oy. q o.s. Kwaxj",
                "Sobpxpoc Fb. w q.h. Elftx",
                "Soblqeqs Kpvppc RH - tbknhjubw siyaenc Njsjbpx Buyshpgyv",
                "Sofeaypq FJ",
                "Stacyok Qurqjw Hw. f c.h.",
                "STOWN HH",
                "Stopjhmq Prxhkakjmalkvdt Weqxejbyig Wgfplnvk D.C.",
                "STRHAEI Clydqr Ha. d z.j.",
                "Sun Clvaqupknlk",
                "TarfAml",
                "Tchukm Rhwcpcvj Cc. v y.a.",
                "Teco Nyxm Rsvzkx pm. J a.t.",
                "Tecdccaty",
                "Telruaet Nmyzaz Twwwuf",
                "Tellrwihv Xvtjle N.U.",
                "Telesjedc Boewsx A.F",
                "tellqfwiqkv dinjlrnyit yktdhlqquihzxr (ohvso)",
                "Tetft Kna Ab. j l.z.",
                "Thesch",
                "Totqucvhcpm Gejxkgrz Is. e k.i.",
                "Towajgixetj Ngaayjitwm fj csxm Mxebfj Sbocok X.H.",
                "Toyfon Meesp Neeban Jdsjmrn sz v z.w.",
                "TRAJQ NZHTA Li. n x.e. - Vghfmngh",
                "Triuiu",
                "Tripsq",
                "TU ENZISOP ZFYIPF V.U.",
                "TUiX Kscdw G.G.",
                "TVN G.A.",
                "Tycd",
                "Unibjqxv rdnbsn - ZJQNJ XCG / Wslqfrk",
                "Unilcs - hopef ps 20 nixi",
                "UPC Gwwmru Ds. g o.r.",
                "Vaidgoav",
                "Vatyqzcgqh Kjnnsy GQ WT",
                "Volhz",
                "Vos Jviggogjt Iyqhlm Ih. w j.y. (fbshoihdnb)",
                "WARMFC E.D.",
                "Wincqk Pqadskf",
                "WKRD",
                "Wolk Pyug",
                "WPRV",
                "WSiI",
                "Wurag XZ",
                "Zacrijl B.B.",
                "Zakja Tziaboysenum Squlslpp - Diifw V.D.",
                "Zakgat Meqivadj Nrpxlekmodx s Bbymjozge W.Y.",
                "Zjetxpbkpgj Mmhhgohasjtpkjd Uwucubbpdj K.N.",
                "ZREH"
        };
        sort(temporaryData, collator);
        threadTestData = temporaryData;
    }

    private static void scramble(String[] data, Random r) {
        for (int i = 0; i < data.length; ++i) {
            int ix = r.nextInt(data.length);
            String s = data[i];
            data[i] = data[ix];
            data[ix] = s;
        }
    }

    private static void sort(String[] data, Collator collator) {
        Arrays.sort(data, collator);
    }

    private static boolean verifySort(String[] data) {
        for (int i = 0; i < data.length; i++) {
            if (!data[i].equals(threadTestData[i])) {
                return false;
            }
        }
        return true;
    }

    private static class Control {
        private boolean go;
        private String fail;

        synchronized void start() {
            go = true;
            notifyAll();
        }

        synchronized void stop() {
            go = false;
            notifyAll();
        }

        boolean go() {
            return go;
        }

        void fail(String msg) {
            fail = msg;
            stop();
        }
    }

    private static class Test implements Runnable {
        private String[] data;
        private Collator collator;
        private String name;
        private Control control;
        private Random r;

        Test(String name, String[] data, Collator collator, Random r, Control control) {
            this.name = name;
            this.data = data;
            this.collator = collator;
            this.control = control;
            this.r = r;
        }

        public void run() {
            try {
                synchronized (control) {
                    while (!control.go()) {
                        control.wait();
                    }
                }

                while (control.go()) {
                    scramble(this.data, r);
                    sort(this.data, this.collator);
                    if (!verifySort(this.data)) {
                        control.fail(name + ": incorrect sort");
                    }
                }
            } catch (InterruptedException e) {
                // die
            } catch (IndexOutOfBoundsException e) {
                control.fail(name + " " + e.getMessage());
            }
        }
    }

    private void runThreads(Thread[] threads, Control control) {
        for (int i = 0; i < threads.length; ++i) {
            threads[i].start();
        }

        try {
            control.start();

            long stopTime = System.currentTimeMillis() + 5000;
            do {
                Thread.sleep(100);
            } while (control.go() && System.currentTimeMillis() < stopTime);

            control.stop();

            for (int i = 0; i < threads.length; ++i) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
            // die
        }

        if (control.fail != null) {
            errln(control.fail);
        }
    }

    @org.junit.Test
    public void testThreads() {
        final Collator theCollator = Collator.getInstance(new Locale("pl", "", ""));
        final Random r = new Random();
        final Control control = new Control();

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; ++i) {
            Collator coll;
            try {
                coll = (Collator)theCollator.clone();
            } catch (CloneNotSupportedException e) {
                // should not happen, if it does we'll get an exception right away
                errln("could not clone");
                return;
            }
            Test test = new Test("Collation test thread" + i, threadTestData.clone(), coll,
                    r, control);
            threads[i] = new Thread(test);
        }

        runThreads(threads, control);
    }

    @org.junit.Test
    public void testFrozen() {
        final Collator theCollator = Collator.getInstance(new Locale("pl", "", ""));
        theCollator.freeze();
        final Random r = new Random();
        Control control = new Control();

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; ++i) {
            Test test = new Test("Frozen collation test thread " + i, threadTestData.clone(), theCollator,
                    r, control);
            threads[i] = new Thread(test);
        }

        runThreads(threads, control);
    }
}
