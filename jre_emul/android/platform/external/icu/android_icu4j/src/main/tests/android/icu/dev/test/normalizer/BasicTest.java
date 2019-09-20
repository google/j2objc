/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.dev.test.normalizer;

import java.text.StringCharacterIterator;
import java.util.Random;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.USerializedSet;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterCategory;
import android.icu.lang.UProperty;
import android.icu.text.FilteredNormalizer2;
import android.icu.text.Normalizer;
import android.icu.text.Normalizer2;
import android.icu.text.UCharacterIterator;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;


public class BasicTest extends TestFmwk {
    String[][] canonTests = {
        // Input                Decomposed              Composed
        { "cat",                "cat",                  "cat"               },
        { "\u00e0ardvark",      "a\u0300ardvark",       "\u00e0ardvark",    },

        { "\u1e0a",             "D\u0307",              "\u1e0a"            }, // D-dot_above
        { "D\u0307",            "D\u0307",              "\u1e0a"            }, // D dot_above

        { "\u1e0c\u0307",       "D\u0323\u0307",        "\u1e0c\u0307"      }, // D-dot_below dot_above
        { "\u1e0a\u0323",       "D\u0323\u0307",        "\u1e0c\u0307"      }, // D-dot_above dot_below
        { "D\u0307\u0323",      "D\u0323\u0307",        "\u1e0c\u0307"      }, // D dot_below dot_above

        { "\u1e10\u0307\u0323", "D\u0327\u0323\u0307",  "\u1e10\u0323\u0307"}, // D dot_below cedilla dot_above
        { "D\u0307\u0328\u0323","D\u0328\u0323\u0307",  "\u1e0c\u0328\u0307"}, // D dot_above ogonek dot_below

        { "\u1E14",             "E\u0304\u0300",        "\u1E14"            }, // E-macron-grave
        { "\u0112\u0300",       "E\u0304\u0300",        "\u1E14"            }, // E-macron + grave
        { "\u00c8\u0304",       "E\u0300\u0304",        "\u00c8\u0304"      }, // E-grave + macron

        { "\u212b",             "A\u030a",              "\u00c5"            }, // angstrom_sign
        { "\u00c5",             "A\u030a",              "\u00c5"            }, // A-ring

        { "\u00c4ffin",         "A\u0308ffin",          "\u00c4ffin"        },
        { "\u00c4\uFB03n",      "A\u0308\uFB03n",       "\u00c4\uFB03n"     },

        { "\u00fdffin",         "y\u0301ffin",          "\u00fdffin"        }, //updated with 3.0
        { "\u00fd\uFB03n",      "y\u0301\uFB03n",       "\u00fd\uFB03n"     }, //updated with 3.0

        { "Henry IV",           "Henry IV",             "Henry IV"          },
        { "Henry \u2163",       "Henry \u2163",         "Henry \u2163"      },

        { "\u30AC",             "\u30AB\u3099",         "\u30AC"            }, // ga (Katakana)
        { "\u30AB\u3099",       "\u30AB\u3099",         "\u30AC"            }, // ka + ten
        { "\uFF76\uFF9E",       "\uFF76\uFF9E",         "\uFF76\uFF9E"      }, // hw_ka + hw_ten
        { "\u30AB\uFF9E",       "\u30AB\uFF9E",         "\u30AB\uFF9E"      }, // ka + hw_ten
        { "\uFF76\u3099",       "\uFF76\u3099",         "\uFF76\u3099"      }, // hw_ka + ten

        { "A\u0300\u0316", "A\u0316\u0300", "\u00C0\u0316" },
        {"\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e","\\U0001D157\\U0001D165\\U0001D157\\U0001D165\\U0001D157\\U0001D165", "\\U0001D157\\U0001D165\\U0001D157\\U0001D165\\U0001D157\\U0001D165"},
    };

    String[][] compatTests = {
            // Input                Decomposed              Composed
        { "cat",                 "cat",                     "cat"           },
        { "\uFB4f",             "\u05D0\u05DC",         "\u05D0\u05DC",     }, // Alef-Lamed vs. Alef, Lamed

        { "\u00C4ffin",         "A\u0308ffin",          "\u00C4ffin"        },
        { "\u00C4\uFB03n",      "A\u0308ffin",          "\u00C4ffin"        }, // ffi ligature -> f + f + i

        { "\u00fdffin",         "y\u0301ffin",          "\u00fdffin"        },        //updated for 3.0
        { "\u00fd\uFB03n",      "y\u0301ffin",          "\u00fdffin"        }, // ffi ligature -> f + f + i

        { "Henry IV",           "Henry IV",             "Henry IV"          },
        { "Henry \u2163",       "Henry IV",             "Henry IV"          },

        { "\u30AC",             "\u30AB\u3099",         "\u30AC"            }, // ga (Katakana)
        { "\u30AB\u3099",       "\u30AB\u3099",         "\u30AC"            }, // ka + ten

        { "\uFF76\u3099",       "\u30AB\u3099",         "\u30AC"            }, // hw_ka + ten

        /* These two are broken in Unicode 2.1.2 but fixed in 2.1.5 and later*/
        { "\uFF76\uFF9E",       "\u30AB\u3099",         "\u30AC"            }, // hw_ka + hw_ten
        { "\u30AB\uFF9E",       "\u30AB\u3099",         "\u30AC"            }, // ka + hw_ten

    };

    // With Canonical decomposition, Hangul syllables should get decomposed
    // into Jamo, but Jamo characters should not be decomposed into
    // conjoining Jamo
    String[][] hangulCanon = {
        // Input                Decomposed              Composed
        { "\ud4db",             "\u1111\u1171\u11b6",   "\ud4db"        },
        { "\u1111\u1171\u11b6", "\u1111\u1171\u11b6",   "\ud4db"        },
    };

    // With compatibility decomposition turned on,
    // it should go all the way down to conjoining Jamo characters.
    // THIS IS NO LONGER TRUE IN UNICODE v2.1.8, SO THIS TEST IS OBSOLETE
    String[][] hangulCompat = {
        // Input        Decomposed                          Composed
        // { "\ud4db",     "\u1111\u116e\u1175\u11af\u11c2",   "\ud478\u1175\u11af\u11c2"  },
    };

    @Test
    public void TestHangulCompose()
                throws Exception{
        // Make sure that the static composition methods work
        logln("Canonical composition...");
        staticTest(Normalizer.NFC, hangulCanon,  2);
        logln("Compatibility composition...");
        staticTest(Normalizer.NFKC, hangulCompat, 2);
        // Now try iterative composition....
        logln("Iterative composition...");
        Normalizer norm = new Normalizer("", Normalizer.NFC,0);
        iterateTest(norm, hangulCanon, 2);

        norm.setMode(Normalizer.NFKD);
        iterateTest(norm, hangulCompat, 2);

        // And finally, make sure you can do it in reverse too
        logln("Reverse iteration...");
        norm.setMode(Normalizer.NFC);
        backAndForth(norm, hangulCanon);
     }

    @Test
    public void TestHangulDecomp() throws Exception{
        // Make sure that the static decomposition methods work
        logln("Canonical decomposition...");
        staticTest(Normalizer.NFD, hangulCanon,  1);
        logln("Compatibility decomposition...");
        staticTest(Normalizer.NFKD, hangulCompat, 1);

         // Now the iterative decomposition methods...
        logln("Iterative decomposition...");
        Normalizer norm = new Normalizer("", Normalizer.NFD,0);
        iterateTest(norm, hangulCanon, 1);

        norm.setMode(Normalizer.NFKD);
        iterateTest(norm, hangulCompat, 1);

        // And finally, make sure you can do it in reverse too
        logln("Reverse iteration...");
        norm.setMode(Normalizer.NFD);
        backAndForth(norm, hangulCanon);
    }
    @Test
    public void TestNone() throws Exception{
        Normalizer norm = new Normalizer("", Normalizer.NONE,0);
        iterateTest(norm, canonTests, 0);
        staticTest(Normalizer.NONE, canonTests, 0);
    }
    @Test
    public void TestDecomp() throws Exception{
        Normalizer norm = new Normalizer("", Normalizer.NFD,0);
        iterateTest(norm, canonTests, 1);
        staticTest(Normalizer.NFD, canonTests, 1);
        decomposeTest(Normalizer.NFD, canonTests, 1);
    }

    @Test
    public void TestCompatDecomp() throws Exception{
        Normalizer norm = new Normalizer("", Normalizer.NFKD,0);
        iterateTest(norm, compatTests, 1);
        staticTest(Normalizer.NFKD,compatTests, 1);
        decomposeTest(Normalizer.NFKD,compatTests, 1);
    }

    @Test
    public void TestCanonCompose() throws Exception{
        Normalizer norm = new Normalizer("", Normalizer.NFC,0);
        iterateTest(norm, canonTests, 2);
        staticTest(Normalizer.NFC, canonTests, 2);
        composeTest(Normalizer.NFC, canonTests, 2);
    }

    @Test
    public void TestCompatCompose() throws Exception{
        Normalizer norm = new Normalizer("", Normalizer.NFKC,0);
        iterateTest(norm, compatTests, 2);
        staticTest(Normalizer.NFKC,compatTests, 2);
        composeTest(Normalizer.NFKC,compatTests, 2);
    }

    @Test
    public void TestExplodingBase() throws Exception{
        // \u017f - Latin small letter long s
        // \u0307 - combining dot above
        // \u1e61 - Latin small letter s with dot above
        // \u1e9b - Latin small letter long s with dot above
        String[][] canon = {
            // Input                Decomposed              Composed
            { "Tschu\u017f",        "Tschu\u017f",          "Tschu\u017f"    },
            { "Tschu\u1e9b",        "Tschu\u017f\u0307",    "Tschu\u1e9b"    },
        };
        String[][] compat = {
            // Input                Decomposed              Composed
            { "\u017f",        "s",              "s"           },
            { "\u1e9b",        "s\u0307",        "\u1e61"      },
        };

        staticTest(Normalizer.NFD, canon,  1);
        staticTest(Normalizer.NFC, canon,  2);

        staticTest(Normalizer.NFKD, compat, 1);
        staticTest(Normalizer.NFKC, compat, 2);

    }

    /**
     * The Tibetan vowel sign AA, 0f71, was messed up prior to
     * Unicode version 2.1.9.
     * Once 2.1.9 or 3.0 is released, uncomment this test.
     */
    @Test
    public void TestTibetan() throws Exception{
        String[][] decomp = {
            { "\u0f77", "\u0f77", "\u0fb2\u0f71\u0f80" }
        };
        String[][] compose = {
            { "\u0fb2\u0f71\u0f80", "\u0fb2\u0f71\u0f80", "\u0fb2\u0f71\u0f80" }
        };

        staticTest(Normalizer.NFD, decomp, 1);
        staticTest(Normalizer.NFKD,decomp, 2);
        staticTest(Normalizer.NFC, compose, 1);
        staticTest(Normalizer.NFKC,compose, 2);
    }

    /**
     * Make sure characters in the CompositionExclusion.txt list do not get
     * composed to.
     */
    @Test
    public void TestCompositionExclusion()
                throws Exception{
        // This list is generated from CompositionExclusion.txt.
        // Update whenever the normalizer tables are updated.  Note
        // that we test all characters listed, even those that can be
        // derived from the Unicode DB and are therefore commented
        // out.
        String EXCLUDED =
            "\u0340\u0341\u0343\u0344\u0374\u037E\u0387\u0958" +
            "\u0959\u095A\u095B\u095C\u095D\u095E\u095F\u09DC" +
            "\u09DD\u09DF\u0A33\u0A36\u0A59\u0A5A\u0A5B\u0A5E" +
            "\u0B5C\u0B5D\u0F43\u0F4D\u0F52\u0F57\u0F5C\u0F69" +
            "\u0F73\u0F75\u0F76\u0F78\u0F81\u0F93\u0F9D\u0FA2" +
            "\u0FA7\u0FAC\u0FB9\u1F71\u1F73\u1F75\u1F77\u1F79" +
            "\u1F7B\u1F7D\u1FBB\u1FBE\u1FC9\u1FCB\u1FD3\u1FDB" +
            "\u1FE3\u1FEB\u1FEE\u1FEF\u1FF9\u1FFB\u1FFD\u2000" +
            "\u2001\u2126\u212A\u212B\u2329\u232A\uF900\uFA10" +
            "\uFA12\uFA15\uFA20\uFA22\uFA25\uFA26\uFA2A\uFB1F" +
            "\uFB2A\uFB2B\uFB2C\uFB2D\uFB2E\uFB2F\uFB30\uFB31" +
            "\uFB32\uFB33\uFB34\uFB35\uFB36\uFB38\uFB39\uFB3A" +
            "\uFB3B\uFB3C\uFB3E\uFB40\uFB41\uFB43\uFB44\uFB46" +
            "\uFB47\uFB48\uFB49\uFB4A\uFB4B\uFB4C\uFB4D\uFB4E";
        for (int i=0; i<EXCLUDED.length(); ++i) {
            String a = String.valueOf(EXCLUDED.charAt(i));
            String b = Normalizer.normalize(a, Normalizer.NFKD);
            String c = Normalizer.normalize(b, Normalizer.NFC);
            if (c.equals(a)) {
                errln("FAIL: " + hex(a) + " x DECOMP_COMPAT => " +
                      hex(b) + " x COMPOSE => " +
                      hex(c));
            } else if (isVerbose()) {
                logln("Ok: " + hex(a) + " x DECOMP_COMPAT => " +
                      hex(b) + " x COMPOSE => " +
                      hex(c));
            }
        }
        // The following method works too, but it is somewhat
        // incestuous.  It uses UInfo, which is the same database that
        // NormalizerBuilder uses, so if something is wrong with
        // UInfo, the following test won't show it.  All it will show
        // is that NormalizerBuilder has been run with whatever the
        // current UInfo is.
        //
        // We comment this out in favor of the test above, which
        // provides independent verification (but also requires
        // independent updating).
//      logln("---");
//      UInfo uinfo = new UInfo();
//      for (int i=0; i<=0xFFFF; ++i) {
//          if (!uinfo.isExcludedComposition((char)i) ||
//              (!uinfo.hasCanonicalDecomposition((char)i) &&
//               !uinfo.hasCompatibilityDecomposition((char)i))) continue;
//          String a = String.valueOf((char)i);
//          String b = Normalizer.normalize(a,Normalizer.DECOMP_COMPAT,0);
//          String c = Normalizer.normalize(b,Normalizer.COMPOSE,0);
//          if (c.equals(a)) {
//              errln("FAIL: " + hex(a) + " x DECOMP_COMPAT => " +
//                    hex(b) + " x COMPOSE => " +
//                    hex(c));
//          } else if (isVerbose()) {
//              logln("Ok: " + hex(a) + " x DECOMP_COMPAT => " +
//                    hex(b) + " x COMPOSE => " +
//                    hex(c));
//          }
//      }
    }

    /**
     * Test for a problem that showed up just before ICU 1.6 release
     * having to do with combining characters with an index of zero.
     * Such characters do not participate in any canonical
     * decompositions.  However, having an index of zero means that
     * they all share one typeMask[] entry, that is, they all have to
     * map to the same canonical class, which is not the case, in
     * reality.
     */
    @Test
    public void TestZeroIndex()
                throws Exception{
        String[] DATA = {
            // Expect col1 x COMPOSE_COMPAT => col2
            // Expect col2 x DECOMP => col3
            "A\u0316\u0300", "\u00C0\u0316", "A\u0316\u0300",
            "A\u0300\u0316", "\u00C0\u0316", "A\u0316\u0300",
            "A\u0327\u0300", "\u00C0\u0327", "A\u0327\u0300",
            "c\u0321\u0327", "c\u0321\u0327", "c\u0321\u0327",
            "c\u0327\u0321", "\u00E7\u0321", "c\u0327\u0321",
        };

        for (int i=0; i<DATA.length; i+=3) {
            String a = DATA[i];
            String b = Normalizer.normalize(a, Normalizer.NFKC);
            String exp = DATA[i+1];
            if (b.equals(exp)) {
                logln("Ok: " + hex(a) + " x COMPOSE_COMPAT => " + hex(b));
            } else {
                errln("FAIL: " + hex(a) + " x COMPOSE_COMPAT => " + hex(b) +
                      ", expect " + hex(exp));
            }
            a = Normalizer.normalize(b, Normalizer.NFD);
            exp = DATA[i+2];
            if (a.equals(exp)) {
                logln("Ok: " + hex(b) + " x DECOMP => " + hex(a));
            } else {
                errln("FAIL: " + hex(b) + " x DECOMP => " + hex(a) +
                      ", expect " + hex(exp));
            }
        }
    }

    /**
     * Test for a problem found by Verisign.  Problem is that
     * characters at the start of a string are not put in canonical
     * order correctly by compose() if there is no starter.
     */
    @Test
    public void TestVerisign()
                throws Exception{
        String[] inputs = {
            "\u05b8\u05b9\u05b1\u0591\u05c3\u05b0\u05ac\u059f",
            "\u0592\u05b7\u05bc\u05a5\u05b0\u05c0\u05c4\u05ad"
        };
        String[] outputs = {
            "\u05b1\u05b8\u05b9\u0591\u05c3\u05b0\u05ac\u059f",
            "\u05b0\u05b7\u05bc\u05a5\u0592\u05c0\u05ad\u05c4"
        };

        for (int i = 0; i < inputs.length; ++i) {
            String input = inputs[i];
            String output = outputs[i];
            String result = Normalizer.decompose(input, false);
            if (!result.equals(output)) {
                errln("FAIL input: " + hex(input));
                errln(" decompose: " + hex(result));
                errln("  expected: " + hex(output));
            }
            result = Normalizer.compose(input, false);
            if (!result.equals(output)) {
                errln("FAIL input: " + hex(input));
                errln("   compose: " + hex(result));
                errln("  expected: " + hex(output));
            }
        }

    }
    @Test
    public void  TestQuickCheckResultNO()
                 throws Exception{
        final char CPNFD[] = {0x00C5, 0x0407, 0x1E00, 0x1F57, 0x220C,
                                0x30AE, 0xAC00, 0xD7A3, 0xFB36, 0xFB4E};
        final char CPNFC[] = {0x0340, 0x0F93, 0x1F77, 0x1FBB, 0x1FEB,
                                0x2000, 0x232A, 0xF900, 0xFA1E, 0xFB4E};
        final char CPNFKD[] = {0x00A0, 0x02E4, 0x1FDB, 0x24EA, 0x32FE,
                                0xAC00, 0xFB4E, 0xFA10, 0xFF3F, 0xFA2D};
        final char CPNFKC[] = {0x00A0, 0x017F, 0x2000, 0x24EA, 0x32FE,
                                0x33FE, 0xFB4E, 0xFA10, 0xFF3F, 0xFA2D};


        final int SIZE = 10;

        int count = 0;
        for (; count < SIZE; count ++)
        {
            if (Normalizer.quickCheck(String.valueOf(CPNFD[count]),
                    Normalizer.NFD,0) != Normalizer.NO)
            {
                errln("ERROR in NFD quick check at U+" +
                       Integer.toHexString(CPNFD[count]));
                return;
            }
            if (Normalizer.quickCheck(String.valueOf(CPNFC[count]),
                        Normalizer.NFC,0) !=Normalizer.NO)
            {
                errln("ERROR in NFC quick check at U+"+
                       Integer.toHexString(CPNFC[count]));
                return;
            }
            if (Normalizer.quickCheck(String.valueOf(CPNFKD[count]),
                                Normalizer.NFKD,0) != Normalizer.NO)
            {
                errln("ERROR in NFKD quick check at U+"+
                       Integer.toHexString(CPNFKD[count]));
                return;
            }
            if (Normalizer.quickCheck(String.valueOf(CPNFKC[count]),
                                         Normalizer.NFKC,0) !=Normalizer.NO)
            {
                errln("ERROR in NFKC quick check at U+"+
                       Integer.toHexString(CPNFKC[count]));
                return;
            }
            // for improving coverage
            if (Normalizer.quickCheck(String.valueOf(CPNFKC[count]),
                                         Normalizer.NFKC) !=Normalizer.NO)
            {
                errln("ERROR in NFKC quick check at U+"+
                       Integer.toHexString(CPNFKC[count]));
                return;
            }
        }
    }


    @Test
    public void TestQuickCheckResultYES()
                throws Exception{
        final char CPNFD[] = {0x00C6, 0x017F, 0x0F74, 0x1000, 0x1E9A,
                                0x2261, 0x3075, 0x4000, 0x5000, 0xF000};
        final char CPNFC[] = {0x0400, 0x0540, 0x0901, 0x1000, 0x1500,
                                0x1E9A, 0x3000, 0x4000, 0x5000, 0xF000};
        final char CPNFKD[] = {0x00AB, 0x02A0, 0x1000, 0x1027, 0x2FFB,
                                0x3FFF, 0x4FFF, 0xA000, 0xF000, 0xFA27};
        final char CPNFKC[] = {0x00B0, 0x0100, 0x0200, 0x0A02, 0x1000,
                                0x2010, 0x3030, 0x4000, 0xA000, 0xFA0E};

        final int SIZE = 10;
        int count = 0;

        char cp = 0;
        while (cp < 0xA0)
        {
            if (Normalizer.quickCheck(String.valueOf(cp), Normalizer.NFD,0)
                                            != Normalizer.YES)
            {
                errln("ERROR in NFD quick check at U+"+
                                                      Integer.toHexString(cp));
                return;
            }
            if (Normalizer.quickCheck(String.valueOf(cp), Normalizer.NFC,0)
                                             != Normalizer.YES)
            {
                errln("ERROR in NFC quick check at U+"+
                                                      Integer.toHexString(cp));
                return;
            }
            if (Normalizer.quickCheck(String.valueOf(cp), Normalizer.NFKD,0)
                                             != Normalizer.YES)
            {
                errln("ERROR in NFKD quick check at U+" +
                                                      Integer.toHexString(cp));
                return;
            }
            if (Normalizer.quickCheck(String.valueOf(cp), Normalizer.NFKC,0)
                                             != Normalizer.YES)
            {
                errln("ERROR in NFKC quick check at U+"+
                                                       Integer.toHexString(cp));
                return;
            }
            // improve the coverage
            if (Normalizer.quickCheck(String.valueOf(cp), Normalizer.NFKC)
                                             != Normalizer.YES)
            {
                errln("ERROR in NFKC quick check at U+"+
                                                       Integer.toHexString(cp));
                return;
            }
            cp++;
        }

        for (; count < SIZE; count ++)
        {
            if (Normalizer.quickCheck(String.valueOf(CPNFD[count]),
                                         Normalizer.NFD,0)!=Normalizer.YES)
            {
                errln("ERROR in NFD quick check at U+"+
                                             Integer.toHexString(CPNFD[count]));
                return;
            }
            if (Normalizer.quickCheck(String.valueOf(CPNFC[count]),
                                         Normalizer.NFC,0)!=Normalizer.YES)
            {
                errln("ERROR in NFC quick check at U+"+
                                             Integer.toHexString(CPNFC[count]));
                return;
            }
            if (Normalizer.quickCheck(String.valueOf(CPNFKD[count]),
                                         Normalizer.NFKD,0)!=Normalizer.YES)
            {
                errln("ERROR in NFKD quick check at U+"+
                                    Integer.toHexString(CPNFKD[count]));
                return;
            }
            if (Normalizer.quickCheck(String.valueOf(CPNFKC[count]),
                                         Normalizer.NFKC,0)!=Normalizer.YES)
            {
                errln("ERROR in NFKC quick check at U+"+
                        Integer.toHexString(CPNFKC[count]));
                return;
            }
            // improve the coverage
            if (Normalizer.quickCheck(String.valueOf(CPNFKC[count]),
                                         Normalizer.NFKC)!=Normalizer.YES)
            {
                errln("ERROR in NFKC quick check at U+"+
                        Integer.toHexString(CPNFKC[count]));
                return;
            }
        }
    }
    @Test
    public void TestBengali() throws Exception{
        String input = "\u09bc\u09be\u09cd\u09be";
        String output=Normalizer.normalize(input,Normalizer.NFC);
        if(!input.equals(output)){
             errln("ERROR in NFC of string");
        }
    }
    @Test
    public void TestQuickCheckResultMAYBE()
                throws Exception{

        final char[] CPNFC = {0x0306, 0x0654, 0x0BBE, 0x102E, 0x1161,
                                0x116A, 0x1173, 0x1175, 0x3099, 0x309A};
        final char[] CPNFKC = {0x0300, 0x0654, 0x0655, 0x09D7, 0x0B3E,
                                0x0DCF, 0xDDF, 0x102E, 0x11A8, 0x3099};


        final int SIZE = 10;

        int count = 0;

        /* NFD and NFKD does not have any MAYBE codepoints */
        for (; count < SIZE; count ++)
        {
            if (Normalizer.quickCheck(String.valueOf(CPNFC[count]),
                                        Normalizer.NFC,0)!=Normalizer.MAYBE)
            {
                errln("ERROR in NFC quick check at U+"+
                                            Integer.toHexString(CPNFC[count]));
                return;
            }
            if (Normalizer.quickCheck(String.valueOf(CPNFKC[count]),
                                       Normalizer.NFKC,0)!=Normalizer.MAYBE)
            {
                errln("ERROR in NFKC quick check at U+"+
                                            Integer.toHexString(CPNFKC[count]));
                return;
            }
            if (Normalizer.quickCheck(new char[]{CPNFC[count]},
                                        Normalizer.NFC,0)!=Normalizer.MAYBE)
            {
                errln("ERROR in NFC quick check at U+"+
                                            Integer.toHexString(CPNFC[count]));
                return;
            }
            if (Normalizer.quickCheck(new char[]{CPNFKC[count]},
                                       Normalizer.NFKC,0)!=Normalizer.MAYBE)
            {
                errln("ERROR in NFKC quick check at U+"+
                                            Integer.toHexString(CPNFKC[count]));
                return;
            }
            if (Normalizer.quickCheck(new char[]{CPNFKC[count]},
                                       Normalizer.NONE,0)!=Normalizer.YES)
            {
                errln("ERROR in NONE quick check at U+"+
                                            Integer.toHexString(CPNFKC[count]));
                return;
            }
        }
    }

    @Test
    public void TestQuickCheckStringResult()
                throws Exception{
        int count;
        String d;
        String c;

        for (count = 0; count < canonTests.length; count ++)
        {
            d = canonTests[count][1];
            c = canonTests[count][2];
            if (Normalizer.quickCheck(d,Normalizer.NFD,0)
                                            != Normalizer.YES)
            {
                errln("ERROR in NFD quick check for string at count " + count);
                return;
            }

            if (Normalizer.quickCheck(c, Normalizer.NFC,0)
                                            == Normalizer.NO)
            {
                errln("ERROR in NFC quick check for string at count " + count);
                return;
            }
        }

        for (count = 0; count < compatTests.length; count ++)
        {
            d = compatTests[count][1];
            c = compatTests[count][2];
            if (Normalizer.quickCheck(d, Normalizer.NFKD,0)
                                            != Normalizer.YES)
            {
                errln("ERROR in NFKD quick check for string at count " + count);
                return;
            }

            if (Normalizer.quickCheck(c,  Normalizer.NFKC,0)
                                            != Normalizer.YES)
            {
                errln("ERROR in NFKC quick check for string at count " + count);
                return;
            }
        }
    }

    static final int qcToInt(Normalizer.QuickCheckResult qc) {
        if(qc==Normalizer.NO) {
            return 0;
        } else if(qc==Normalizer.YES) {
            return 1;
        } else /* Normalizer.MAYBE */ {
            return 2;
        }
    }

    @Test
    public void TestQuickCheckPerCP() {
        int c, lead, trail;
        String s, nfd;
        int lccc1, lccc2, tccc1, tccc2;
        int qc1, qc2;

        if(
            UCharacter.getIntPropertyMaxValue(UProperty.NFD_QUICK_CHECK)!=1 || // YES
            UCharacter.getIntPropertyMaxValue(UProperty.NFKD_QUICK_CHECK)!=1 ||
            UCharacter.getIntPropertyMaxValue(UProperty.NFC_QUICK_CHECK)!=2 || // MAYBE
            UCharacter.getIntPropertyMaxValue(UProperty.NFKC_QUICK_CHECK)!=2 ||
            UCharacter.getIntPropertyMaxValue(UProperty.LEAD_CANONICAL_COMBINING_CLASS)!=UCharacter.getIntPropertyMaxValue(UProperty.CANONICAL_COMBINING_CLASS) ||
            UCharacter.getIntPropertyMaxValue(UProperty.TRAIL_CANONICAL_COMBINING_CLASS)!=UCharacter.getIntPropertyMaxValue(UProperty.CANONICAL_COMBINING_CLASS)
        ) {
            errln("wrong result from one of the u_getIntPropertyMaxValue(UCHAR_NF*_QUICK_CHECK) or UCHAR_*_CANONICAL_COMBINING_CLASS");
        }

        /*
         * compare the quick check property values for some code points
         * to the quick check results for checking same-code point strings
         */
        c=0;
        while(c<0x110000) {
            s=UTF16.valueOf(c);

            qc1=UCharacter.getIntPropertyValue(c, UProperty.NFC_QUICK_CHECK);
            qc2=qcToInt(Normalizer.quickCheck(s, Normalizer.NFC));
            if(qc1!=qc2) {
                errln("getIntPropertyValue(NFC)="+qc1+" != "+qc2+"=quickCheck(NFC) for U+"+Integer.toHexString(c));
            }

            qc1=UCharacter.getIntPropertyValue(c, UProperty.NFD_QUICK_CHECK);
            qc2=qcToInt(Normalizer.quickCheck(s, Normalizer.NFD));
            if(qc1!=qc2) {
                errln("getIntPropertyValue(NFD)="+qc1+" != "+qc2+"=quickCheck(NFD) for U+"+Integer.toHexString(c));
            }

            qc1=UCharacter.getIntPropertyValue(c, UProperty.NFKC_QUICK_CHECK);
            qc2=qcToInt(Normalizer.quickCheck(s, Normalizer.NFKC));
            if(qc1!=qc2) {
                errln("getIntPropertyValue(NFKC)="+qc1+" != "+qc2+"=quickCheck(NFKC) for U+"+Integer.toHexString(c));
            }

            qc1=UCharacter.getIntPropertyValue(c, UProperty.NFKD_QUICK_CHECK);
            qc2=qcToInt(Normalizer.quickCheck(s, Normalizer.NFKD));
            if(qc1!=qc2) {
                errln("getIntPropertyValue(NFKD)="+qc1+" != "+qc2+"=quickCheck(NFKD) for U+"+Integer.toHexString(c));
            }

            nfd=Normalizer.normalize(s, Normalizer.NFD);
            lead=UTF16.charAt(nfd, 0);
            trail=UTF16.charAt(nfd, nfd.length()-1);

            lccc1=UCharacter.getIntPropertyValue(c, UProperty.LEAD_CANONICAL_COMBINING_CLASS);
            lccc2=UCharacter.getCombiningClass(lead);
            tccc1=UCharacter.getIntPropertyValue(c, UProperty.TRAIL_CANONICAL_COMBINING_CLASS);
            tccc2=UCharacter.getCombiningClass(trail);

            if(lccc1!=lccc2) {
                errln("getIntPropertyValue(lccc)="+lccc1+" != "+lccc2+"=getCombiningClass(lead) for U+"+Integer.toHexString(c));
            }
            if(tccc1!=tccc2) {
                errln("getIntPropertyValue(tccc)="+tccc1+" != "+tccc2+"=getCombiningClass(trail) for U+"+Integer.toHexString(c));
            }

            /* skip some code points */
            c=(20*c)/19+1;
        }
    }

    //------------------------------------------------------------------------
    // Internal utilities
    //
       //------------------------------------------------------------------------
    // Internal utilities
    //

/*    private void backAndForth(Normalizer iter, String input)
    {
        iter.setText(input);

        // Run through the iterator forwards and stick it into a StringBuffer
        StringBuffer forward =  new StringBuffer();
        for (int ch = iter.first(); ch != Normalizer.DONE; ch = iter.next()) {
            forward.append(ch);
        }

        // Now do it backwards
        StringBuffer reverse = new StringBuffer();
        for (int ch = iter.last(); ch != Normalizer.DONE; ch = iter.previous()) {
            reverse.insert(0, ch);
        }

        if (!forward.toString().equals(reverse.toString())) {
            errln("FAIL: Forward/reverse mismatch for input " + hex(input)
                  + ", forward: " + hex(forward) + ", backward: "+hex(reverse));
        } else if (isVerbose()) {
            logln("Ok: Forward/reverse for input " + hex(input)
                  + ", forward: " + hex(forward) + ", backward: "+hex(reverse));
        }
    }*/

    private void backAndForth(Normalizer iter, String[][] tests)
    {
        for (int i = 0; i < tests.length; i++)
        {
            iter.setText(tests[i][0]);

            // Run through the iterator forwards and stick it into a
            // StringBuffer
            StringBuffer forward =  new StringBuffer();
            for (int ch = iter.first(); ch != Normalizer.DONE; ch = iter.next()) {
                forward.append(ch);
            }

            // Now do it backwards
            StringBuffer reverse = new StringBuffer();
            for (int ch = iter.last(); ch != Normalizer.DONE; ch = iter.previous()) {
                reverse.insert(0, ch);
            }

            if (!forward.toString().equals(reverse.toString())) {
                errln("FAIL: Forward/reverse mismatch for input "
                    + hex(tests[i][0]) + ", forward: " + hex(forward)
                    + ", backward: " + hex(reverse));
            } else if (isVerbose()) {
                logln("Ok: Forward/reverse for input " + hex(tests[i][0])
                      + ", forward: " + hex(forward) + ", backward: "
                      + hex(reverse));
            }
        }
    }

    private void staticTest (Normalizer.Mode mode,
                             String[][] tests, int outCol) throws Exception{
        for (int i = 0; i < tests.length; i++)
        {
            String input = Utility.unescape(tests[i][0]);
            String expect = Utility.unescape(tests[i][outCol]);

            logln("Normalizing '" + input + "' (" + hex(input) + ")" );

            String output = Normalizer.normalize(input, mode);

            if (!output.equals(expect)) {
                errln("FAIL: case " + i
                    + " expected '" + expect + "' (" + hex(expect) + ")"
                    + " but got '" + output + "' (" + hex(output) + ")" );
            }
        }
        char[] output = new char[1];
        for (int i = 0; i < tests.length; i++)
        {
            char[] input = Utility.unescape(tests[i][0]).toCharArray();
            String expect =Utility.unescape( tests[i][outCol]);

            logln("Normalizing '" + new String(input) + "' (" +
                        hex(new String(input)) + ")" );
            int reqLength=0;
            while(true){
                try{
                    reqLength=Normalizer.normalize(input,output, mode,0);
                    if(reqLength<=output.length    ){
                        break;
                    }
                }catch(IndexOutOfBoundsException e){
                    output= new char[Integer.parseInt(e.getMessage())];
                    continue;
                }
            }
            if (!expect.equals(new String(output,0,reqLength))) {
                errln("FAIL: case " + i
                    + " expected '" + expect + "' (" + hex(expect) + ")"
                    + " but got '" + new String(output)
                    + "' ("  + hex(new String(output)) + ")" );
            }
        }
    }
    private void decomposeTest(Normalizer.Mode mode,
                             String[][] tests, int outCol) throws Exception{
        for (int i = 0; i < tests.length; i++)
        {
            String input = Utility.unescape(tests[i][0]);
            String expect = Utility.unescape(tests[i][outCol]);

            logln("Normalizing '" + input + "' (" + hex(input) + ")" );

            String output = Normalizer.decompose(input, mode==Normalizer.NFKD);

            if (!output.equals(expect)) {
                errln("FAIL: case " + i
                    + " expected '" + expect + "' (" + hex(expect) + ")"
                    + " but got '" + output + "' (" + hex(output) + ")" );
            }
        }
        char[] output = new char[1];
        for (int i = 0; i < tests.length; i++)
        {
            char[] input = Utility.unescape(tests[i][0]).toCharArray();
            String expect = Utility.unescape(tests[i][outCol]);

            logln("Normalizing '" + new String(input) + "' (" +
                        hex(new String(input)) + ")" );
            int reqLength=0;
            while(true){
                try{
                    reqLength=Normalizer.decompose(input,output, mode==Normalizer.NFKD,0);
                    if(reqLength<=output.length ){
                        break;
                    }
                }catch(IndexOutOfBoundsException e){
                    output= new char[Integer.parseInt(e.getMessage())];
                    continue;
                }
            }
            if (!expect.equals(new String(output,0,reqLength))) {
                errln("FAIL: case " + i
                    + " expected '" + expect + "' (" + hex(expect) + ")"
                    + " but got '" + new String(output)
                    + "' ("  + hex(new String(output)) + ")" );
            }
        }
        output = new char[1];
        for (int i = 0; i < tests.length; i++)
        {
           char[] input = Utility.unescape(tests[i][0]).toCharArray();
           String expect = Utility.unescape(tests[i][outCol]);

           logln("Normalizing '" + new String(input) + "' (" +
                       hex(new String(input)) + ")" );
           int reqLength=0;
           while(true){
               try{
                   reqLength=Normalizer.decompose(input,0,input.length,output,0,output.length, mode==Normalizer.NFKD,0);
                   if(reqLength<=output.length ){
                       break;
                   }
               }catch(IndexOutOfBoundsException e){
                   output= new char[Integer.parseInt(e.getMessage())];
                   continue;
               }
           }
           if (!expect.equals(new String(output,0,reqLength))) {
               errln("FAIL: case " + i
                   + " expected '" + expect + "' (" + hex(expect) + ")"
                   + " but got '" + new String(output)
                   + "' ("  + hex(new String(output)) + ")" );
           }
           char[] output2 = new char[reqLength * 2];
           System.arraycopy(output, 0, output2, 0, reqLength);
           int retLength = Normalizer.decompose(input,0,input.length, output2, reqLength, output2.length, mode==Normalizer.NFKC,0);
           if(retLength != reqLength){
               logln("FAIL: Normalizer.compose did not return the expected length. Expected: " +reqLength + " Got: " + retLength);
           }
        }
    }

    private void composeTest(Normalizer.Mode mode,
                             String[][] tests, int outCol) throws Exception{
        for (int i = 0; i < tests.length; i++)
        {
            String input = Utility.unescape(tests[i][0]);
            String expect = Utility.unescape(tests[i][outCol]);

            logln("Normalizing '" + input + "' (" + hex(input) + ")" );

            String output = Normalizer.compose(input, mode==Normalizer.NFKC);

            if (!output.equals(expect)) {
                errln("FAIL: case " + i
                    + " expected '" + expect + "' (" + hex(expect) + ")"
                    + " but got '" + output + "' (" + hex(output) + ")" );
            }
        }
        char[] output = new char[1];
        for (int i = 0; i < tests.length; i++)
        {
            char[] input = Utility.unescape(tests[i][0]).toCharArray();
            String expect = Utility.unescape(tests[i][outCol]);

            logln("Normalizing '" + new String(input) + "' (" +
                        hex(new String(input)) + ")" );
            int reqLength=0;
            while(true){
                try{
                    reqLength=Normalizer.compose(input,output, mode==Normalizer.NFKC,0);
                    if(reqLength<=output.length ){
                        break;
                    }
                }catch(IndexOutOfBoundsException e){
                    output= new char[Integer.parseInt(e.getMessage())];
                    continue;
                }
            }
            if (!expect.equals(new String(output,0,reqLength))) {
                errln("FAIL: case " + i
                    + " expected '" + expect + "' (" + hex(expect) + ")"
                    + " but got '" + new String(output)
                    + "' ("  + hex(new String(output)) + ")" );
            }
        }
        output = new char[1];
        for (int i = 0; i < tests.length; i++)
        {
            char[] input = Utility.unescape(tests[i][0]).toCharArray();
            String expect = Utility.unescape(tests[i][outCol]);

            logln("Normalizing '" + new String(input) + "' (" +
                        hex(new String(input)) + ")" );
            int reqLength=0;
            while(true){
                try{
                    reqLength=Normalizer.compose(input,0,input.length, output, 0, output.length, mode==Normalizer.NFKC,0);
                    if(reqLength<=output.length ){
                        break;
                    }
                }catch(IndexOutOfBoundsException e){
                    output= new char[Integer.parseInt(e.getMessage())];
                    continue;
                }
            }
            if (!expect.equals(new String(output,0,reqLength))) {
                errln("FAIL: case " + i
                    + " expected '" + expect + "' (" + hex(expect) + ")"
                    + " but got '" + new String(output)
                    + "' ("  + hex(new String(output)) + ")" );
            }

            char[] output2 = new char[reqLength * 2];
            System.arraycopy(output, 0, output2, 0, reqLength);
            int retLength = Normalizer.compose(input,0,input.length, output2, reqLength, output2.length, mode==Normalizer.NFKC,0);
            if(retLength != reqLength){
                logln("FAIL: Normalizer.compose did not return the expected length. Expected: " +reqLength + " Got: " + retLength);
            }
        }
    }
    private void iterateTest(Normalizer iter, String[][] tests, int outCol){
        for (int i = 0; i < tests.length; i++)
        {
            String input = Utility.unescape(tests[i][0]);
            String expect = Utility.unescape(tests[i][outCol]);

            logln("Normalizing '" + input + "' (" + hex(input) + ")" );

            iter.setText(input);
            assertEqual(expect, iter, "case " + i + " ");
        }
    }

    private void assertEqual(String expected, Normalizer iter, String msg)
    {
        int index = 0;
        int ch;
        UCharacterIterator cIter =  UCharacterIterator.getInstance(expected);

        while ((ch=iter.next())!= Normalizer.DONE){
            if (index >= expected.length()) {
                errln("FAIL: " + msg + "Unexpected character '" + (char)ch
                        + "' (" + hex(ch) + ")"
                        + " at index " + index);
                break;
            }
            int want = UTF16.charAt(expected,index);
            if (ch != want) {
                errln("FAIL: " + msg + "got '" + (char)ch
                        + "' (" + hex(ch) + ")"
                        + " but expected '" + want + "' (" + hex(want)+ ")"
                        + " at index " + index);
            }
            index+=  UTF16.getCharCount(ch);
        }
        if (index < expected.length()) {
            errln("FAIL: " + msg + "Only got " + index + " chars, expected "
            + expected.length());
        }

        cIter.setToLimit();
        while((ch=iter.previous())!=Normalizer.DONE){
            int want = cIter.previousCodePoint();
            if (ch != want ) {
                errln("FAIL: " + msg + "got '" + (char)ch
                        + "' (" + hex(ch) + ")"
                        + " but expected '" + want + "' (" + hex(want) + ")"
                        + " at index " + index);
            }
        }
    }
    //--------------------------------------------------------------------------

    // NOTE: These tests are used for quick debugging so are not ported
    // to ICU4C tsnorm.cpp in intltest
    //

    @Test
    public void TestDebugStatic(){
        String in = Utility.unescape("\\U0001D157\\U0001D165");
        if(!Normalizer.isNormalized(in,Normalizer.NFC,0)){
            errln("isNormalized failed");
        }

        String input  =  "\uAD8B\uAD8B\uAD8B\uAD8B"+
            "\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
            "\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
            "\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
            "\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
            "\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
            "aaaaaaaaaaaaaaaaaazzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"+
            "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"+
            "ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"+
            "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"+
            "\uAD8B\uAD8B\uAD8B\uAD8B"+
            "d\u031B\u0307\u0323";
        String expect = "\u1100\u116F\u11AA\u1100\u116F\u11AA\u1100\u116F"+
                        "\u11AA\u1100\u116F\u11AA\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65\uD834\uDD57\uD834\uDD65"+
                        "\uD834\uDD57\uD834\uDD65aaaaaaaaaaaaaaaaaazzzzzz"+
                        "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"+
                        "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"+
                        "bbbbbbbbbbbbbbbbbbbbbbbbccccccccccccccccccccccccccccc"+
                        "cccccccccccccccccccccccccccccccccccccccccccccccc"+
                        "ddddddddddddddddddddddddddddddddddddddddddddddddddddd"+
                        "dddddddddddddddddddddddd"+
                        "\u1100\u116F\u11AA\u1100\u116F\u11AA\u1100\u116F"+
                        "\u11AA\u1100\u116F\u11AA\u0064\u031B\u0323\u0307";
            String output = Normalizer.normalize(Utility.unescape(input),
                            Normalizer.NFD);
            if(!expect.equals(output)){
                errln("FAIL expected: "+hex(expect) + " got: "+hex(output));
            }



    }
    @Test
    public void TestDebugIter(){
        String src = Utility.unescape("\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e");
        String expected = Utility.unescape("\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e");
        Normalizer iter = new Normalizer(new StringCharacterIterator(Utility.unescape(src)),
                                                Normalizer.NONE,0);
        int index = 0;
        int ch;
        UCharacterIterator cIter =  UCharacterIterator.getInstance(expected);

        while ((ch=iter.next())!= Normalizer.DONE){
            if (index >= expected.length()) {
                errln("FAIL: " +  "Unexpected character '" + (char)ch
                        + "' (" + hex(ch) + ")"
                        + " at index " + index);
                break;
            }
            int want = UTF16.charAt(expected,index);
            if (ch != want) {
                errln("FAIL: " +  "got '" + (char)ch
                        + "' (" + hex(ch) + ")"
                        + " but expected '" + want + "' (" + hex(want)+ ")"
                        + " at index " + index);
            }
            index+=  UTF16.getCharCount(ch);
        }
        if (index < expected.length()) {
            errln("FAIL: " +  "Only got " + index + " chars, expected "
            + expected.length());
        }

        cIter.setToLimit();
        while((ch=iter.previous())!=Normalizer.DONE){
            int want = cIter.previousCodePoint();
            if (ch != want ) {
                errln("FAIL: " + "got '" + (char)ch
                        + "' (" + hex(ch) + ")"
                        + " but expected '" + want + "' (" + hex(want) + ")"
                        + " at index " + index);
            }
        }
    }
    @Test
    public void TestDebugIterOld(){
        String input = "\\U0001D15E";
        String expected = "\uD834\uDD57\uD834\uDD65";
        String expectedReverse = "\uD834\uDD65\uD834\uDD57";
        int index = 0;
        int ch;
        Normalizer iter = new Normalizer(new StringCharacterIterator(Utility.unescape(input)),
                                                Normalizer.NFKC,0);
        StringBuffer got = new StringBuffer();
        for (ch = iter.first();ch!=Normalizer.DONE;ch=iter.next())
        {
            if (index >= expected.length()) {
                errln("FAIL: " +  "Unexpected character '" + (char)ch +
                       "' (" + hex(ch) + ")" + " at index " + index);
                break;
            }
            got.append(UCharacter.toString(ch));
            index++;
        }
        if (!expected.equals(got.toString())) {
                errln("FAIL: " +  "got '" +got+ "' (" + hex(got) + ")"
                        + " but expected '" + expected + "' ("
                        + hex(expected) + ")");
        }
        if (got.length() < expected.length()) {
            errln("FAIL: " +  "Only got " + index + " chars, expected "
                           + expected.length());
        }

        logln("Reverse Iteration\n");
        iter.setIndexOnly(iter.endIndex());
        got.setLength(0);
        for(ch=iter.previous();ch!=Normalizer.DONE;ch=iter.previous()){
            if (index >= expected.length()) {
                errln("FAIL: " +  "Unexpected character '" + (char)ch
                               + "' (" + hex(ch) + ")" + " at index " + index);
                break;
            }
            got.append(UCharacter.toString(ch));
        }
        if (!expectedReverse.equals(got.toString())) {
                errln("FAIL: " +  "got '" +got+ "' (" + hex(got) + ")"
                               + " but expected '" + expected
                               + "' (" + hex(expected) + ")");
        }
        if (got.length() < expected.length()) {
            errln("FAIL: " +  "Only got " + index + " chars, expected "
                      + expected.length());
        }

    }
    //--------------------------------------------------------------------------
    // helper class for TestPreviousNext()
    // simple UTF-32 character iterator
    class UCharIterator {

       public UCharIterator(int[] src, int len, int index){

            s=src;
            length=len;
            i=index;
       }

        public int current() {
            if(i<length) {
                return s[i];
            } else {
                return -1;
            }
        }

        public int next() {
            if(i<length) {
                return s[i++];
            } else {
                return -1;
            }
        }

        public int previous() {
            if(i>0) {
                return s[--i];
            } else {
                return -1;
            }
        }

        public int getIndex() {
            return i;
        }

        private int[] s;
        private int length, i;
    }
    @Test
    public void TestPreviousNext() {
        // src and expect strings
        char src[]={
            UTF16.getLeadSurrogate(0x2f999), UTF16.getTrailSurrogate(0x2f999),
            UTF16.getLeadSurrogate(0x1d15f), UTF16.getTrailSurrogate(0x1d15f),
            0xc4,
            0x1ed0
        };
        int expect[]={
            0x831d,
            0x1d158, 0x1d165,
            0x41, 0x308,
            0x4f, 0x302, 0x301
        };

        // expected src indexes corresponding to expect indexes
        int expectIndex[]={
            0,
            2, 2,
            4, 4,
            5, 5, 5,
            6 // behind last character
        };

        // initial indexes into the src and expect strings

        final int SRC_MIDDLE=4;
        final int EXPECT_MIDDLE=3;


        // movement vector
        // - for previous(), 0 for current(), + for next()
        // not const so that we can terminate it below for the error message
        String moves="0+0+0--0-0-+++0--+++++++0--------";

        // iterators
        Normalizer iter = new Normalizer(new String(src),
                                                Normalizer.NFD,0);
        UCharIterator iter32 = new UCharIterator(expect, expect.length,
                                                     EXPECT_MIDDLE);

        int c1, c2;
        char m;

        // initially set the indexes into the middle of the strings
        iter.setIndexOnly(SRC_MIDDLE);

        // move around and compare the iteration code points with
        // the expected ones
        int movesIndex =0;
        while(movesIndex<moves.length()) {
            m=moves.charAt(movesIndex++);
            if(m=='-') {
                c1=iter.previous();
                c2=iter32.previous();
            } else if(m=='0') {
                c1=iter.current();
                c2=iter32.current();
            } else /* m=='+' */ {
                c1=iter.next();
                c2=iter32.next();
            }

            // compare results
            if(c1!=c2) {
                // copy the moves until the current (m) move, and terminate
                String history = moves.substring(0,movesIndex);
                errln("error: mismatch in Normalizer iteration at "+history+": "
                      +"got c1= " + hex(c1) +" != expected c2= "+ hex(c2));
                break;
            }

            // compare indexes
            if(iter.getIndex()!=expectIndex[iter32.getIndex()]) {
                // copy the moves until the current (m) move, and terminate
                String history = moves.substring(0,movesIndex);
                errln("error: index mismatch in Normalizer iteration at "
                      +history+ " : "+ "Normalizer index " +iter.getIndex()
                      +" expected "+ expectIndex[iter32.getIndex()]);
                break;
            }
        }
    }
    // Only in ICU4j
    @Test
    public void TestPreviousNextJCI() {
        // src and expect strings
        char src[]={
            UTF16.getLeadSurrogate(0x2f999), UTF16.getTrailSurrogate(0x2f999),
            UTF16.getLeadSurrogate(0x1d15f), UTF16.getTrailSurrogate(0x1d15f),
            0xc4,
            0x1ed0
        };
        int expect[]={
            0x831d,
            0x1d158, 0x1d165,
            0x41, 0x308,
            0x4f, 0x302, 0x301
        };

        // expected src indexes corresponding to expect indexes
        int expectIndex[]={
            0,
            2, 2,
            4, 4,
            5, 5, 5,
            6 // behind last character
        };

        // initial indexes into the src and expect strings

        final int SRC_MIDDLE=4;
        final int EXPECT_MIDDLE=3;


        // movement vector
        // - for previous(), 0 for current(), + for next()
        // not const so that we can terminate it below for the error message
        String moves="0+0+0--0-0-+++0--+++++++0--------";

        // iterators
        StringCharacterIterator text = new StringCharacterIterator(new String(src));
        Normalizer iter = new Normalizer(text,Normalizer.NFD,0);
        UCharIterator iter32 = new UCharIterator(expect, expect.length,
                                                     EXPECT_MIDDLE);

        int c1, c2;
        char m;

        // initially set the indexes into the middle of the strings
        iter.setIndexOnly(SRC_MIDDLE);

        // move around and compare the iteration code points with
        // the expected ones
        int movesIndex =0;
        while(movesIndex<moves.length()) {
            m=moves.charAt(movesIndex++);
            if(m=='-') {
                c1=iter.previous();
                c2=iter32.previous();
            } else if(m=='0') {
                c1=iter.current();
                c2=iter32.current();
            } else /* m=='+' */ {
                c1=iter.next();
                c2=iter32.next();
            }

            // compare results
            if(c1!=c2) {
                // copy the moves until the current (m) move, and terminate
                String history = moves.substring(0,movesIndex);
                errln("error: mismatch in Normalizer iteration at "+history+": "
                      +"got c1= " + hex(c1) +" != expected c2= "+ hex(c2));
                break;
            }

            // compare indexes
            if(iter.getIndex()!=expectIndex[iter32.getIndex()]) {
                // copy the moves until the current (m) move, and terminate
                String history = moves.substring(0,movesIndex);
                errln("error: index mismatch in Normalizer iteration at "
                      +history+ " : "+ "Normalizer index " +iter.getIndex()
                      +" expected "+ expectIndex[iter32.getIndex()]);
                break;
            }
        }
    }

    // test APIs that are not otherwise used - improve test coverage
    @Test
    public void TestNormalizerAPI() throws Exception {
        try{
            // instantiate a Normalizer from a CharacterIterator
            String s=Utility.unescape("a\u0308\uac00\\U0002f800");
            // make s a bit longer and more interesting
            UCharacterIterator iter = UCharacterIterator.getInstance(s+s);
            Normalizer norm = new Normalizer(iter, Normalizer.NFC,0);
            if(norm.next()!=0xe4) {
                errln("error in Normalizer(CharacterIterator).next()");
            }

            // test clone(), ==, and hashCode()
            Normalizer clone=(Normalizer)norm.clone();
            if(clone.equals(norm)) {
                errln("error in Normalizer(Normalizer(CharacterIterator)).clone()!=norm");
            }

            if(clone.getLength()!= norm.getLength()){
               errln("error in Normalizer.getBeginIndex()");
            }
            // clone must have the same hashCode()
            //if(clone.hashCode()!=norm.hashCode()) {
            //    errln("error in Normalizer(Normalizer(CharacterIterator)).clone().hashCode()!=copy.hashCode()");
            //}
            if(clone.next()!=0xac00) {
                errln("error in Normalizer(Normalizer(CharacterIterator)).next()");
            }
            int ch = clone.next();
            if(ch!=0x4e3d) {
                errln("error in Normalizer(Normalizer(CharacterIterator)).clone().next()");
            }
            // position changed, must change hashCode()
            if(clone.hashCode()==norm.hashCode()) {
                errln("error in Normalizer(Normalizer(CharacterIterator)).clone().next().hashCode()==copy.hashCode()");
            }

            // test compose() and decompose()
            StringBuffer tel;
            String nfkc, nfkd;
            tel=new StringBuffer("\u2121\u2121\u2121\u2121\u2121\u2121\u2121\u2121\u2121\u2121");
            tel.insert(1,(char)0x0301);

            nfkc=Normalizer.compose(tel.toString(), true);
            nfkd=Normalizer.decompose(tel.toString(), true);
            if(
                !nfkc.equals(Utility.unescape("TE\u0139TELTELTELTELTELTELTELTELTEL"))||
                !nfkd.equals(Utility.unescape("TEL\u0301TELTELTELTELTELTELTELTELTEL"))
            ) {
                errln("error in Normalizer::(de)compose(): wrong result(s)");
            }

            // test setIndex()
            ch=norm.setIndex(3);
            if(ch!=0x4e3d) {
               errln("error in Normalizer(CharacterIterator).setIndex(3)");
            }

            // test setText(CharacterIterator) and getText()
            String out, out2;
            clone.setText(iter);

            out = clone.getText();
            out2 = iter.getText();
            if( !out.equals(out2) ||
                clone.startIndex()!=0||
                clone.endIndex()!=iter.getLength()
            ) {
                errln("error in Normalizer::setText() or Normalizer::getText()");
            }

            char[] fillIn1 = new char[clone.getLength()];
            char[] fillIn2 = new char[iter.getLength()];
            int len = clone.getText(fillIn1);
            iter.getText(fillIn2,0);
            if(!Utility.arrayRegionMatches(fillIn1,0,fillIn2,0,len)){
                errln("error in Normalizer.getText(). Normalizer: "+
                                Utility.hex(new String(fillIn1))+
                                " Iter: " + Utility.hex(new String(fillIn2)));
            }

            clone.setText(fillIn1);
            len = clone.getText(fillIn2);
            if(!Utility.arrayRegionMatches(fillIn1,0,fillIn2,0,len)){
                errln("error in Normalizer.setText() or Normalizer.getText()"+
                                Utility.hex(new String(fillIn1))+
                                " Iter: " + Utility.hex(new String(fillIn2)));
            }

            // test setText(UChar *), getUMode() and setMode()
            clone.setText(s);
            clone.setIndexOnly(1);
            clone.setMode(Normalizer.NFD);
            if(clone.getMode()!=Normalizer.NFD) {
                errln("error in Normalizer::setMode() or Normalizer::getMode()");
            }
            if(clone.next()!=0x308 || clone.next()!=0x1100) {
                errln("error in Normalizer::setText() or Normalizer::setMode()");
            }

            // test last()/previous() with an internal buffer overflow
            StringBuffer buf = new StringBuffer("aaaaaaaaaa");
            buf.setCharAt(10-1,'\u0308');
            clone.setText(buf);
            if(clone.last()!=0x308) {
                errln("error in Normalizer(10*U+0308).last()");
            }

            // test UNORM_NONE
            norm.setMode(Normalizer.NONE);
            if(norm.first()!=0x61 || norm.next()!=0x308 || norm.last()!=0x2f800) {
                errln("error in Normalizer(UNORM_NONE).first()/next()/last()");
            }
            out=Normalizer.normalize(s, Normalizer.NONE);
            if(!out.equals(s)) {
                errln("error in Normalizer::normalize(UNORM_NONE)");
            }
            ch = 0x1D15E;
            String exp = "\\U0001D157\\U0001D165";
            String ns = Normalizer.normalize(ch,Normalizer.NFC);
            if(!ns.equals(Utility.unescape(exp))){
                errln("error in Normalizer.normalize(int,Mode)");
            }
            ns = Normalizer.normalize(ch,Normalizer.NFC,0);
            if(!ns.equals(Utility.unescape(exp))){
                errln("error in Normalizer.normalize(int,Mode,int)");
            }
        }catch(Exception e){
            throw e;
        }
    }

    @Test
    public void TestConcatenate() {

        Object[][]cases=new Object[][]{
            /* mode, left, right, result */
            {
                Normalizer.NFC,
                "re",
                "\u0301sum\u00e9",
                "r\u00e9sum\u00e9"
            },
            {
                Normalizer.NFC,
                "a\u1100",
                "\u1161bcdefghijk",
                "a\uac00bcdefghijk"
            },
            /* ### TODO: add more interesting cases */
            {
                Normalizer.NFD,
                "\u03B1\u0345",
                "\u0C4D\uD804\uDCBA\uD834\uDD69",  // 0C4D 110BA 1D169
                "\u03B1\uD834\uDD69\uD804\uDCBA\u0C4D\u0345"  // 03B1 1D169 110BA 0C4D 0345
            }
        };

        String left, right, expect, result;
        Normalizer.Mode mode;
        int i;

        /* test concatenation */
        for(i=0; i<cases.length; ++i) {
            mode = (Normalizer.Mode)cases[i][0];

            left=(String)cases[i][1];
            right=(String)cases[i][2];
            expect=(String)cases[i][3];
            {
                result=Normalizer.concatenate(left, right, mode,0);
                if(!result.equals(expect)) {
                    errln("error in Normalizer.concatenate(), cases[] failed"
                          +", result==expect: expected: "
                          + hex(expect)+" =========> got: " + hex(result));
                }
            }
            {
                result=Normalizer.concatenate(left.toCharArray(), right.toCharArray(), mode,0);
                if(!result.equals(expect)) {
                    errln("error in Normalizer.concatenate(), cases[] failed"
                          +", result==expect: expected: "
                          + hex(expect)+" =========> got: " + hex(result));
                }
            }
        }

        mode= Normalizer.NFC; // (Normalizer.Mode)cases2[0][0];
        char[] destination = "My resume is here".toCharArray();
        left = "resume";
        right = "re\u0301sum\u00e9 is HERE";
        expect = "My r\u00e9sum\u00e9 is HERE";

        // Concatenates 're' with '\u0301sum\u00e9 is HERE' and places the result at
        // position 3 of string 'My resume is here'.
        Normalizer.concatenate(left.toCharArray(), 0, 2, right.toCharArray(), 2, 15,
                                         destination, 3, 17, mode, 0);
        if(!String.valueOf(destination).equals(expect)) {
            errln("error in Normalizer.concatenate(), cases2[] failed"
                  +", result==expect: expected: "
                  + hex(expect) + " =========> got: " + hex(destination));
        }

        // Error case when result of concatenation won't fit into destination array.
        try {
            Normalizer.concatenate(left.toCharArray(), 0, 2, right.toCharArray(), 2, 15,
                                         destination, 3, 16, mode, 0);
        } catch (IndexOutOfBoundsException e) {
            assertTrue("Normalizer.concatenate() failed", e.getMessage().equals("14"));
            return;
        }
        fail("Normalizer.concatenate() tested for failure but passed");
    }

    private final int RAND_MAX = 0x7fff;

    @Test
    public void TestCheckFCD()
    {
      char[] FAST = {0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007,
                     0x0008, 0x0009, 0x000A};

      char[] FALSE = {0x0001, 0x0002, 0x02EA, 0x03EB, 0x0300, 0x0301,
                      0x02B9, 0x0314, 0x0315, 0x0316};

      char[] TRUE = {0x0030, 0x0040, 0x0440, 0x056D, 0x064F, 0x06E7,
                     0x0050, 0x0730, 0x09EE, 0x1E10};

      char[][] datastr= { {0x0061, 0x030A, 0x1E05, 0x0302, 0},
                          {0x0061, 0x030A, 0x00E2, 0x0323, 0},
                          {0x0061, 0x0323, 0x00E2, 0x0323, 0},
                          {0x0061, 0x0323, 0x1E05, 0x0302, 0}
                        };
      Normalizer.QuickCheckResult result[] = {Normalizer.YES, Normalizer.NO, Normalizer.NO, Normalizer.YES};

      char[] datachar= {        0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69,
                                0x6a,
                                0xe0, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9,
                                0xea,
                                0x0300, 0x0301, 0x0302, 0x0303, 0x0304, 0x0305, 0x0306,
                                0x0307, 0x0308, 0x0309, 0x030a,
                                0x0320, 0x0321, 0x0322, 0x0323, 0x0324, 0x0325, 0x0326,
                                0x0327, 0x0328, 0x0329, 0x032a,
                                0x1e00, 0x1e01, 0x1e02, 0x1e03, 0x1e04, 0x1e05, 0x1e06,
                                0x1e07, 0x1e08, 0x1e09, 0x1e0a
                       };

      int count = 0;

      if (Normalizer.quickCheck(FAST,0,FAST.length, Normalizer.FCD,0) != Normalizer.YES)
        errln("Normalizer.quickCheck(FCD) failed: expected value for fast Normalizer.quickCheck is Normalizer.YES\n");
      if (Normalizer.quickCheck(FALSE,0, FALSE.length,Normalizer.FCD,0) != Normalizer.NO)
        errln("Normalizer.quickCheck(FCD) failed: expected value for error Normalizer.quickCheck is Normalizer.NO\n");
      if (Normalizer.quickCheck(TRUE,0,TRUE.length,Normalizer.FCD,0) != Normalizer.YES)
        errln("Normalizer.quickCheck(FCD) failed: expected value for correct Normalizer.quickCheck is Normalizer.YES\n");


      while (count < 4)
      {
        Normalizer.QuickCheckResult fcdresult = Normalizer.quickCheck(datastr[count],0,datastr[count].length, Normalizer.FCD,0);
        if (result[count] != fcdresult) {
            errln("Normalizer.quickCheck(FCD) failed: Data set "+ count
                    + " expected value "+ result[count]);
        }
        count ++;
      }

      /* random checks of long strings */
      //srand((unsigned)time( NULL ));
      Random rand = createRandom(); // use test framework's random

      for (count = 0; count < 50; count ++)
      {
        int size = 0;
        Normalizer.QuickCheckResult testresult = Normalizer.YES;
        char[] data= new char[20];
        char[] norm= new char[100];
        char[] nfd = new char[100];
        int normStart = 0;
        int nfdsize = 0;
        while (size != 19) {
          data[size] = datachar[rand.nextInt(RAND_MAX)*50/RAND_MAX];
          logln("0x"+data[size]);
          normStart += Normalizer.normalize(data,size,size+1,
                                              norm,normStart,100,
                                              Normalizer.NFD,0);
          size ++;
        }
        logln("\n");

        nfdsize = Normalizer.normalize(data,0,size, nfd,0,nfd.length,Normalizer.NFD,0);
        //    nfdsize = unorm_normalize(data, size, UNORM_NFD, UCOL_IGNORE_HANGUL,
        //                      nfd, 100, &status);
        if (nfdsize != normStart || Utility.arrayRegionMatches(nfd,0, norm,0,nfdsize) ==false) {
          testresult = Normalizer.NO;
        }
        if (testresult == Normalizer.YES) {
          logln("result Normalizer.YES\n");
        }
        else {
          logln("result Normalizer.NO\n");
        }

        if (Normalizer.quickCheck(data,0,data.length, Normalizer.FCD,0) != testresult) {
          errln("Normalizer.quickCheck(FCD) failed: expected "+ testresult +" for random data: "+hex(new String(data)) );
        }
      }
    }


    // reference implementation of Normalizer::compare
    private int ref_norm_compare(String s1, String s2, int options) {
        String t1, t2,r1,r2;

        int normOptions=options>>Normalizer.COMPARE_NORM_OPTIONS_SHIFT;

        if((options&Normalizer.COMPARE_IGNORE_CASE)!=0) {
            // NFD(toCasefold(NFD(X))) = NFD(toCasefold(NFD(Y)))
            r1 = Normalizer.decompose(s1,false,normOptions);
            r2 = Normalizer.decompose(s2,false,normOptions);
            r1 = UCharacter.foldCase(r1,options);
            r2 = UCharacter.foldCase(r2,options);
        }else{
            r1 = s1;
            r2 = s2;
        }

        t1 = Normalizer.decompose(r1, false, normOptions);
        t2 = Normalizer.decompose(r2, false, normOptions);

        if((options&Normalizer.COMPARE_CODE_POINT_ORDER)!=0) {
            UTF16.StringComparator comp
                    = new UTF16.StringComparator(true, false,
                                     UTF16.StringComparator.FOLD_CASE_DEFAULT);
            return comp.compare(t1,t2);
        } else {
            return t1.compareTo(t2);
        }

    }

    // test wrapper for Normalizer::compare, sets UNORM_INPUT_IS_FCD appropriately
    private int norm_compare(String s1, String s2, int options) {
        int normOptions=options>>Normalizer.COMPARE_NORM_OPTIONS_SHIFT;

        if( Normalizer.YES==Normalizer.quickCheck(s1,Normalizer.FCD,normOptions) &&
            Normalizer.YES==Normalizer.quickCheck(s2,Normalizer.FCD,normOptions)) {
            options|=Normalizer.INPUT_IS_FCD;
        }

        int cmpStrings = Normalizer.compare(s1, s2, options);
        int cmpArrays = Normalizer.compare(
                s1.toCharArray(), 0, s1.length(),
                s2.toCharArray(), 0, s2.length(), options);
        assertEquals("compare strings == compare char arrays", cmpStrings, cmpArrays);
        return cmpStrings;
    }

    // reference implementation of UnicodeString::caseCompare
    private int ref_case_compare(String s1, String s2, int options) {
        String t1, t2;

        t1=s1;
        t2=s2;

        t1 = UCharacter.foldCase(t1,((options&Normalizer.FOLD_CASE_EXCLUDE_SPECIAL_I)==0));
        t2 = UCharacter.foldCase(t2,((options&Normalizer.FOLD_CASE_EXCLUDE_SPECIAL_I)==0));

        if((options&Normalizer.COMPARE_CODE_POINT_ORDER)!=0) {
            UTF16.StringComparator comp
                    = new UTF16.StringComparator(true, false,
                                    UTF16.StringComparator.FOLD_CASE_DEFAULT);
            return comp.compare(t1,t2);
        } else {
            return t1.compareTo(t2);
        }

    }

    // reduce an integer to -1/0/1
    private static int sign(int value) {
        if(value==0) {
            return 0;
        } else {
            return (value>>31)|1;
        }
    }
    private static String signString(int value) {
        if(value<0) {
            return "<0";
        } else if(value==0) {
            return "=0";
        } else /* value>0 */ {
            return ">0";
        }
    }
    // test Normalizer::compare and unorm_compare (thinly wrapped by the former)
    // by comparing it with its semantic equivalent
    // since we trust the pieces, this is sufficient

    // test each string with itself and each other
    // each time with all options
    private  String strings[]=new String[]{
                // some cases from NormalizationTest.txt
                // 0..3
                "D\u031B\u0307\u0323",
                "\u1E0C\u031B\u0307",
                "D\u031B\u0323\u0307",
                "d\u031B\u0323\u0307",

                // 4..6
                "\u00E4",
                "a\u0308",
                "A\u0308",

                // Angstrom sign = A ring
                // 7..10
                "\u212B",
                "\u00C5",
                "A\u030A",
                "a\u030A",

                // 11.14
                "a\u059A\u0316\u302A\u032Fb",
                "a\u302A\u0316\u032F\u059Ab",
                "a\u302A\u0316\u032F\u059Ab",
                "A\u059A\u0316\u302A\u032Fb",

                // from ICU case folding tests
                // 15..20
                "A\u00df\u00b5\ufb03\\U0001040c\u0131",
                "ass\u03bcffi\\U00010434i",
                "\u0061\u0042\u0131\u03a3\u00df\ufb03\ud93f\udfff",
                "\u0041\u0062\u0069\u03c3\u0073\u0053\u0046\u0066\u0049\ud93f\udfff",
                "\u0041\u0062\u0131\u03c3\u0053\u0073\u0066\u0046\u0069\ud93f\udfff",
                "\u0041\u0062\u0069\u03c3\u0073\u0053\u0046\u0066\u0049\ud93f\udffd",

                //     U+d800 U+10001   see implementation comment in unorm_cmpEquivFold
                // vs. U+10000          at bottom - code point order
                // 21..22
                "\ud800\ud800\udc01",
                "\ud800\udc00",

                // other code point order tests from ustrtest.cpp
                // 23..31
                "\u20ac\ud801",
                "\u20ac\ud800\udc00",
                "\ud800",
                "\ud800\uff61",
                "\udfff",
                "\uff61\udfff",
                "\uff61\ud800\udc02",
                "\ud800\udc02",
                "\ud84d\udc56",

                // long strings, see cnormtst.c/TestNormCoverage()
                // equivalent if case-insensitive
                // 32..33
                "\uAD8B\uAD8B\uAD8B\uAD8B"+
                "\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
                "\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
                "\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
                "\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
                "\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
                "aaaaaaaaaaaaaaaaaazzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"+
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"+
                "ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"+
                "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"+
                "\uAD8B\uAD8B\uAD8B\uAD8B"+
                "d\u031B\u0307\u0323",

                "\u1100\u116f\u11aa\uAD8B\uAD8B\u1100\u116f\u11aa"+
                "\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
                "\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
                "\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
                "\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
                "\\U0001d15e\\U0001d157\\U0001d165\\U0001d15e\\U0001d15e\\U0001d15e\\U0001d15e"+
                "aaaaaaaaaaAAAAAAAAZZZZZZZZZZZZZZZZzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"+
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"+
                "ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"+
                "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"+
                "\u1100\u116f\u11aa\uAD8B\uAD8B\u1100\u116f\u11aa"+
                "\u1E0C\u031B\u0307",

                // some strings that may make a difference whether the compare function
                // case-folds or decomposes first
                // 34..41
                "\u0360\u0345\u0334",
                "\u0360\u03b9\u0334",

                "\u0360\u1f80\u0334",
                "\u0360\u03b1\u0313\u03b9\u0334",

                "\u0360\u1ffc\u0334",
                "\u0360\u03c9\u03b9\u0334",

                "a\u0360\u0345\u0360\u0345b",
                "a\u0345\u0360\u0345\u0360b",

                // interesting cases for canonical caseless match with turkic i handling
                // 42..43
                "\u00cc",
                "\u0069\u0300",

                // strings with post-Unicode 3.2 normalization or normalization corrections
                // 44..45
                "\u00e4\u193b\\U0002f868",
                "\u0061\u193b\u0308\u36fc",


    };

    // all combinations of options
    // UNORM_INPUT_IS_FCD is set automatically if both input strings fulfill FCD conditions
    final class Temp {
        int options;
        String name;
        public Temp(int opt,String str){
            options =opt;
            name = str;
        }

    }
    // set UNORM_UNICODE_3_2 in one additional combination

    private Temp[] opt = new Temp[]{
                    new Temp(0,"default"),
                    new Temp(Normalizer.COMPARE_CODE_POINT_ORDER, "code point order" ),
                    new Temp(Normalizer.COMPARE_IGNORE_CASE, "ignore case" ),
                    new Temp(Normalizer.COMPARE_CODE_POINT_ORDER|Normalizer.COMPARE_IGNORE_CASE, "code point order & ignore case" ),
                    new Temp(Normalizer.COMPARE_IGNORE_CASE|Normalizer.FOLD_CASE_EXCLUDE_SPECIAL_I, "ignore case & special i"),
                    new Temp(Normalizer.COMPARE_CODE_POINT_ORDER|Normalizer.COMPARE_IGNORE_CASE|Normalizer.FOLD_CASE_EXCLUDE_SPECIAL_I, "code point order & ignore case & special i"),
                    new Temp(Normalizer.UNICODE_3_2 << Normalizer.COMPARE_NORM_OPTIONS_SHIFT, "Unicode 3.2")
            };


    @Test
    public void TestCompareDebug(){

        String[] s = new String[100]; // at least as many items as in strings[] !


        int i, j, k, count=strings.length;
        int result, refResult;

        // create the UnicodeStrings
        for(i=0; i<count; ++i) {
            s[i]=Utility.unescape(strings[i]);
        }
        UTF16.StringComparator comp = new UTF16.StringComparator(true, false,
                                     UTF16.StringComparator.FOLD_CASE_DEFAULT);
        // test them each with each other

        i = 42;
        j = 43;
        k = 2;
        // test Normalizer::compare
        result=norm_compare(s[i], s[j], opt[k].options);
        refResult=ref_norm_compare(s[i], s[j], opt[k].options);
        if(sign(result)!=sign(refResult)) {
            errln("Normalizer::compare( " + i +", "+j + ", " +k+"( " +opt[k].name+"))=" + result +" should be same sign as " + refResult);
        }

        // test UnicodeString::caseCompare - same internal implementation function
         if(0!=(opt[k].options&Normalizer.COMPARE_IGNORE_CASE)) {
        //    result=s[i]. (s[j], opt[k].options);
            if ((opt[k].options & Normalizer.FOLD_CASE_EXCLUDE_SPECIAL_I) == 0)
            {
                comp.setIgnoreCase(true, UTF16.StringComparator.FOLD_CASE_DEFAULT);
            }
            else {
                comp.setIgnoreCase(true, UTF16.StringComparator.FOLD_CASE_EXCLUDE_SPECIAL_I);
            }

            result=comp.compare(s[i],s[j]);
            refResult=ref_case_compare(s[i], s[j], opt[k].options);
            if(sign(result)!=sign(refResult)) {
                      errln("Normalizer::compare( " + i +", "+j + ", "+k+"( " +opt[k].name+"))=" + result +" should be same sign as " + refResult);
                            }
        }
        String value1 = "\u00dater\u00fd";
        String value2 = "\u00fater\u00fd";
        if(Normalizer.compare(value1,value2,0)!=0){
            if(Normalizer.compare(value1,value2,Normalizer.COMPARE_IGNORE_CASE)==0){

            }
        }
    }

    @Test
    public void TestCompare() {

        String[] s = new String[100]; // at least as many items as in strings[] !

        int i, j, k, count=strings.length;
        int result, refResult;

        // create the UnicodeStrings
        for(i=0; i<count; ++i) {
            s[i]=Utility.unescape(strings[i]);
        }
        UTF16.StringComparator comp = new UTF16.StringComparator();
        // test them each with each other
        for(i=0; i<count; ++i) {
            for(j=i; j<count; ++j) {
                for(k=0; k<opt.length; ++k) {
                    // test Normalizer::compare
                    result=norm_compare(s[i], s[j], opt[k].options);
                    refResult=ref_norm_compare(s[i], s[j], opt[k].options);
                    if(sign(result)!=sign(refResult)) {
                        errln("Normalizer::compare( " + i +", "+j + ", " +k+"( " +opt[k].name+"))=" + result +" should be same sign as " + refResult);
                    }

                    // test UnicodeString::caseCompare - same internal implementation function
                     if(0!=(opt[k].options&Normalizer.COMPARE_IGNORE_CASE)) {
                        //    result=s[i]. (s[j], opt[k].options);
                        if ((opt[k].options & Normalizer.FOLD_CASE_EXCLUDE_SPECIAL_I) == 0)
                        {
                            comp.setIgnoreCase(true, UTF16.StringComparator.FOLD_CASE_DEFAULT);
                        }
                        else {
                            comp.setIgnoreCase(true, UTF16.StringComparator.FOLD_CASE_EXCLUDE_SPECIAL_I);
                        }

                        comp.setCodePointCompare((opt[k].options & Normalizer.COMPARE_CODE_POINT_ORDER) != 0);
                        // result=comp.caseCompare(s[i],s[j], opt[k].options);
                        result=comp.compare(s[i],s[j]);
                        refResult=ref_case_compare(s[i], s[j], opt[k].options);
                        if(sign(result)!=sign(refResult)) {
                                  errln("Normalizer::compare( " + i +", "+j + ", "+k+"( " +opt[k].name+"))=" + result +" should be same sign as " + refResult);
                                         }
                    }
                }
            }
        }

        // test cases with i and I to make sure Turkic works
        char[] iI= new char[]{ 0x49, 0x69, 0x130, 0x131 };
        UnicodeSet set = new UnicodeSet(), iSet = new UnicodeSet();
        Normalizer2Impl nfcImpl = Norm2AllModes.getNFCInstance().impl;
        nfcImpl.ensureCanonIterData();

        String s1, s2;

        // collect all sets into one for contiguous output
        for(i=0; i<iI.length; ++i) {
            if(nfcImpl.getCanonStartSet(iI[i], iSet)) {
                set.addAll(iSet);
            }
        }

        // test all of these precomposed characters
        Normalizer2 nfcNorm2 = Normalizer2.getNFCInstance();
        UnicodeSetIterator it = new UnicodeSetIterator(set);
        int c;
        while(it.next() && (c=it.codepoint)!=UnicodeSetIterator.IS_STRING) {
            s1 = UTF16.valueOf(c);
            s2 = nfcNorm2.getDecomposition(c);
            for(k=0; k<opt.length; ++k) {
                // test Normalizer::compare

                result= norm_compare(s1, s2, opt[k].options);
                refResult=ref_norm_compare(s1, s2, opt[k].options);
                if(sign(result)!=sign(refResult)) {
                    errln("Normalizer.compare(U+"+hex(c)+" with its NFD, "+opt[k].name+")"
                          + signString(result)+" should be "+signString(refResult));
                }

                // test UnicodeString::caseCompare - same internal implementation function
                if((opt[k].options & Normalizer.COMPARE_IGNORE_CASE)>0) {
                     if ((opt[k].options & Normalizer.FOLD_CASE_EXCLUDE_SPECIAL_I) == 0)
                    {
                        comp.setIgnoreCase(true, UTF16.StringComparator.FOLD_CASE_DEFAULT);
                    }
                    else {
                        comp.setIgnoreCase(true, UTF16.StringComparator.FOLD_CASE_EXCLUDE_SPECIAL_I);
                    }

                    comp.setCodePointCompare((opt[k].options & Normalizer.COMPARE_CODE_POINT_ORDER) != 0);

                    result=comp.compare(s1,s2);
                    refResult=ref_case_compare(s1, s2, opt[k].options);
                    if(sign(result)!=sign(refResult)) {
                        errln("UTF16.compare(U+"+hex(c)+" with its NFD, "
                              +opt[k].name+")"+signString(result) +" should be "+signString(refResult));
                    }
                }
            }
        }

        // test getDecomposition() for some characters that do not decompose
        if( nfcNorm2.getDecomposition(0x20)!=null ||
            nfcNorm2.getDecomposition(0x4e00)!=null ||
            nfcNorm2.getDecomposition(0x20002)!=null
        ) {
            errln("NFC.getDecomposition() returns TRUE for characters which do not have decompositions");
        }

        // test getRawDecomposition() for some characters that do not decompose
        if( nfcNorm2.getRawDecomposition(0x20)!=null ||
            nfcNorm2.getRawDecomposition(0x4e00)!=null ||
            nfcNorm2.getRawDecomposition(0x20002)!=null
        ) {
            errln("getRawDecomposition() returns TRUE for characters which do not have decompositions");
        }

        // test composePair() for some pairs of characters that do not compose
        if( nfcNorm2.composePair(0x20, 0x301)>=0 ||
            nfcNorm2.composePair(0x61, 0x305)>=0 ||
            nfcNorm2.composePair(0x1100, 0x1160)>=0 ||
            nfcNorm2.composePair(0xac00, 0x11a7)>=0
        ) {
            errln("NFC.composePair() incorrectly composes some pairs of characters");
        }

        // test FilteredNormalizer2.getDecomposition()
        UnicodeSet filter=new UnicodeSet("[^\u00a0-\u00ff]");
        FilteredNormalizer2 fn2=new FilteredNormalizer2(nfcNorm2, filter);
        if(fn2.getDecomposition(0xe4)!=null || !"A\u0304".equals(fn2.getDecomposition(0x100))) {
            errln("FilteredNormalizer2(NFC, ^A0-FF).getDecomposition() failed");
        }

        // test FilteredNormalizer2.getRawDecomposition()
        if(fn2.getRawDecomposition(0xe4)!=null || !"A\u0304".equals(fn2.getRawDecomposition(0x100))) {
            errln("FilteredNormalizer2(NFC, ^A0-FF).getRawDecomposition() failed");
        }

        // test FilteredNormalizer2::composePair()
        if( 0x100!=fn2.composePair(0x41, 0x304) ||
            fn2.composePair(0xc7, 0x301)>=0 // unfiltered result: U+1E08
        ) {
            errln("FilteredNormalizer2(NFC, ^A0-FF).composePair() failed");
        }
    }

    // verify that case-folding does not un-FCD strings
    int countFoldFCDExceptions(int foldingOptions) {
        String s, d;
        int c;
        int count;
        int/*unsigned*/ cc, trailCC, foldCC, foldTrailCC;
        Normalizer.QuickCheckResult qcResult;
        int category;
        boolean isNFD;


        logln("Test if case folding may un-FCD a string (folding options 0x)"+hex(foldingOptions));

        count=0;
        for(c=0; c<=0x10ffff; ++c) {
            category=UCharacter.getType(c);
            if(category==UCharacterCategory.UNASSIGNED) {
                continue; // skip unassigned code points
            }
            if(c==0xac00) {
                c=0xd7a3; // skip Hangul - no case folding there
                continue;
            }
            // skip Han blocks - no case folding there either
            if(c==0x3400) {
                c=0x4db5;
                continue;
            }
            if(c==0x4e00) {
                c=0x9fa5;
                continue;
            }
            if(c==0x20000) {
                c=0x2a6d6;
                continue;
            }

            s= UTF16.valueOf(c);

            // get leading and trailing cc for c
            d= Normalizer.decompose(s,false);
            isNFD= s==d;
            cc=UCharacter.getCombiningClass(UTF16.charAt(d,0));
            trailCC=UCharacter.getCombiningClass(UTF16.charAt(d,d.length()-1));

            // get leading and trailing cc for the case-folding of c
            UCharacter.foldCase(s,(foldingOptions==0));
            d = Normalizer.decompose(s, false);
            foldCC=UCharacter.getCombiningClass(UTF16.charAt(d,0));
            foldTrailCC=UCharacter.getCombiningClass(UTF16.charAt(d,d.length()-1));

            qcResult=Normalizer.quickCheck(s, Normalizer.FCD,0);


            // bad:
            // - character maps to empty string: adjacent characters may then need reordering
            // - folding has different leading/trailing cc's, and they don't become just 0
            // - folding itself is not FCD
            if( qcResult!=Normalizer.YES ||
                s.length()==0 ||
                (cc!=foldCC && foldCC!=0) || (trailCC!=foldTrailCC && foldTrailCC!=0)
            ) {
                ++count;
                errln("U+"+hex(c)+": case-folding may un-FCD a string (folding options 0x"+hex(foldingOptions)+")");
                //errln("  cc %02x trailCC %02x    foldCC(U+%04lx) %02x foldTrailCC(U+%04lx) %02x   quickCheck(folded)=%d", cc, trailCC, UTF16.charAt(d,0), foldCC, UTF16.charAt(d,d.length()-1), foldTrailCC, qcResult);
                continue;
            }

            // also bad:
            // if a code point is in NFD but its case folding is not, then
            // unorm_compare will also fail
            if(isNFD && Normalizer.YES!=Normalizer.quickCheck(s, Normalizer.NFD,0)) {
                ++count;
                errln("U+"+hex(c)+": case-folding may un-FCD a string (folding options 0x"+hex(foldingOptions)+")");
            }
        }

        logln("There are "+hex(count)+" code points for which case-folding may un-FCD a string (folding options"+foldingOptions+"x)" );
        return count;
    }

    @Test
    public void TestFindFoldFCDExceptions() {
        int count;

        count=countFoldFCDExceptions(0);
        count+=countFoldFCDExceptions(Normalizer.FOLD_CASE_EXCLUDE_SPECIAL_I);
        if(count>0) {
            //*
            //* If case-folding un-FCDs any strings, then unorm_compare() must be
            //* re-implemented.
            //* It currently assumes that one can check for FCD then case-fold
            //* and then still have FCD strings for raw decomposition without reordering.
            //*
            errln("error: There are "+count+" code points for which case-folding"+
                  " may un-FCD a string for all folding options.\n See comment"+
                  " in BasicNormalizerTest::FindFoldFCDExceptions()!");
        }
    }

    @Test
    public void TestCombiningMarks(){
        String src = "\u0f71\u0f72\u0f73\u0f74\u0f75";
        String expected = "\u0F71\u0F71\u0F71\u0F72\u0F72\u0F74\u0F74";
        String result = Normalizer.decompose(src,false);
        if(!expected.equals(result)){
            errln("Reordering of combining marks failed. Expected: "+Utility.hex(expected)+" Got: "+ Utility.hex(result));
        }
    }

    /*
     * Re-enable this test when UTC fixes UAX 21
    @Test
    public void TestUAX21Failure(){
        final String[][] cases = new String[][]{
                {"\u0061\u0345\u0360\u0345\u0062", "\u0061\u0360\u0345\u0345\u0062"},
                {"\u0061\u0345\u0345\u0360\u0062", "\u0061\u0360\u0345\u0345\u0062"},
                {"\u0061\u0345\u0360\u0362\u0360\u0062", "\u0061\u0362\u0360\u0360\u0345\u0062"},
                {"\u0061\u0360\u0345\u0360\u0362\u0062", "\u0061\u0362\u0360\u0360\u0345\u0062"},
                {"\u0061\u0345\u0360\u0362\u0361\u0062", "\u0061\u0362\u0360\u0361\u0345\u0062"},
                {"\u0061\u0361\u0345\u0360\u0362\u0062", "\u0061\u0362\u0361\u0360\u0345\u0062"},
        };
        for(int i = 0; i< cases.length; i++){
            String s1 =cases[0][0];
            String s2 = cases[0][1];
            if( (Normalizer.compare(s1,s2,Normalizer.FOLD_CASE_DEFAULT ==0)//case sensitive compare
                &&
                (Normalizer.compare(s1,s2,Normalizer.COMPARE_IGNORE_CASE)!=0)){
                errln("Normalizer.compare() failed for s1: "
                        + Utility.hex(s1) +" s2: " + Utility.hex(s2));
            }
        }
    }
    */
    @Test
    public void TestFCNFKCClosure() {
        final class TestStruct{
            int c;
            String s;
            TestStruct(int cp, String src){
                c=cp;
                s=src;
            }
        }

        TestStruct[] tests= new TestStruct[]{
            new TestStruct( 0x00C4, "" ),
            new TestStruct( 0x00E4, "" ),
            new TestStruct( 0x037A, "\u0020\u03B9" ),
            new TestStruct( 0x03D2, "\u03C5" ),
            new TestStruct( 0x20A8, "\u0072\u0073" ) ,
            new TestStruct( 0x210B, "\u0068" ),
            new TestStruct( 0x210C, "\u0068" ),
            new TestStruct( 0x2121, "\u0074\u0065\u006C" ),
            new TestStruct( 0x2122, "\u0074\u006D" ),
            new TestStruct( 0x2128, "\u007A" ),
            new TestStruct( 0x1D5DB,"\u0068" ),
            new TestStruct( 0x1D5ED,"\u007A" ),
            new TestStruct( 0x0061, "" )
        };


        for(int i = 0; i < tests.length; ++ i) {
            String result=Normalizer.getFC_NFKC_Closure(tests[i].c);
            if(!result.equals(new String(tests[i].s))) {
                errln("getFC_NFKC_Closure(U+"+Integer.toHexString(tests[i].c)+") is wrong");
            }
        }

        /* error handling */

        int length=Normalizer.getFC_NFKC_Closure(0x5c, null);
        if(length!=0){
            errln("getFC_NFKC_Closure did not perform error handling correctly");
        }
    }
    @Test
    public void TestBugJ2324(){
       /* String[] input = new String[]{
                            //"\u30FD\u3099",
                            "\u30FA\u309A",
                            "\u30FB\u309A",
                            "\u30FC\u309A",
                            "\u30FE\u309A",
                            "\u30FD\u309A",

        };*/
        String troublesome = "\u309A";
        for(int i=0x3000; i<0x3100;i++){
            String input = ((char)i)+troublesome;
            try{
              /*  String result =*/ Normalizer.compose(input,false);
            }catch(IndexOutOfBoundsException e){
                errln("compose() failed for input: " + Utility.hex(input) + " Exception: " + e.toString());
            }
        }

    }

    static final int D = 0, C = 1, KD= 2, KC = 3, FCD=4, NONE=5;

    private static UnicodeSet[] initSkippables(UnicodeSet[] skipSets) {
        skipSets[D].applyPattern("[[:NFD_QC=Yes:]&[:ccc=0:]]", false);
        skipSets[C].applyPattern("[[:NFC_QC=Yes:]&[:ccc=0:]-[:HST=LV:]]", false);
        skipSets[KD].applyPattern("[[:NFKD_QC=Yes:]&[:ccc=0:]]", false);
        skipSets[KC].applyPattern("[[:NFKC_QC=Yes:]&[:ccc=0:]-[:HST=LV:]]", false);

        // Remove from the NFC and NFKC sets all those characters that change
        // when a back-combining character is added.
        // First, get all of the back-combining characters and their combining classes.
        UnicodeSet combineBack=new UnicodeSet("[:NFC_QC=Maybe:]");
        int numCombineBack=combineBack.size();
        int[] combineBackCharsAndCc=new int[numCombineBack*2];
        UnicodeSetIterator iter=new UnicodeSetIterator(combineBack);
        for(int i=0; i<numCombineBack; ++i) {
            iter.next();
            int c=iter.codepoint;
            combineBackCharsAndCc[2*i]=c;
            combineBackCharsAndCc[2*i+1]=UCharacter.getCombiningClass(c);
        }

        // We need not look at control codes, Han characters nor Hangul LVT syllables because they
        // do not combine forward. LV syllables are already removed.
        UnicodeSet notInteresting=new UnicodeSet("[[:C:][:Unified_Ideograph:][:HST=LVT:]]");
        UnicodeSet unsure=((UnicodeSet)(skipSets[C].clone())).removeAll(notInteresting);
        // System.out.format("unsure.size()=%d\n", unsure.size());

        // For each character about which we are unsure, see if it changes when we add
        // one of the back-combining characters.
        Normalizer2 norm2=Normalizer2.getNFCInstance();
        StringBuilder s=new StringBuilder();
        iter.reset(unsure);
        while(iter.next()) {
            int c=iter.codepoint;
            s.delete(0, 0x7fffffff).appendCodePoint(c);
            int cLength=s.length();
            int tccc=UCharacter.getIntPropertyValue(c, UProperty.TRAIL_CANONICAL_COMBINING_CLASS);
            for(int i=0; i<numCombineBack; ++i) {
                // If c's decomposition ends with a character with non-zero combining class, then
                // c can only change if it combines with a character with a non-zero combining class.
                int cc2=combineBackCharsAndCc[2*i+1];
                if(tccc==0 || cc2!=0) {
                    int c2=combineBackCharsAndCc[2*i];
                    s.appendCodePoint(c2);
                    if(!norm2.isNormalized(s)) {
                        // System.out.format("remove U+%04x (tccc=%d) + U+%04x (cc=%d)\n", c, tccc, c2, cc2);
                        skipSets[C].remove(c);
                        skipSets[KC].remove(c);
                        break;
                    }
                    s.delete(cLength, 0x7fffffff);
                }
            }
        }
        return skipSets;
    }

    @Test
    public void TestSkippable() {
        UnicodeSet[] skipSets = new UnicodeSet[] {
            new UnicodeSet(), //NFD
            new UnicodeSet(), //NFC
            new UnicodeSet(), //NFKD
            new UnicodeSet()  //NFKC
        };
        UnicodeSet[] expectSets = new UnicodeSet[] {
            new UnicodeSet(),
            new UnicodeSet(),
            new UnicodeSet(),
            new UnicodeSet()
        };
        StringBuilder s, pattern;

        // build NF*Skippable sets from runtime data
        skipSets[D].applyPattern("[:NFD_Inert:]");
        skipSets[C].applyPattern("[:NFC_Inert:]");
        skipSets[KD].applyPattern("[:NFKD_Inert:]");
        skipSets[KC].applyPattern("[:NFKC_Inert:]");

        expectSets = initSkippables(expectSets);
        if(expectSets[D].contains(0x0350)){
            errln("expectSets[D] contains 0x0350");
        }
        for(int i=0; i<expectSets.length; ++i) {
            if(!skipSets[i].equals(expectSets[i])) {
                errln("error: TestSkippable skipSets["+i+"]!=expectedSets["+i+"]\n");
                // Note: This used to depend on hardcoded UnicodeSet patterns generated by
                // Mark's unicodetools.com.ibm.text.UCD.NFSkippable, by
                // running com.ibm.text.UCD.Main with the option NFSkippable.
                // Since ICU 4.6/Unicode 6, we are generating the
                // expectSets ourselves in initSkippables().

                s=new StringBuilder();

                s.append("\n\nskip=       ");
                s.append(skipSets[i].toPattern(true));
                s.append("\n\n");

                s.append("skip-expect=");
                pattern = new StringBuilder(((UnicodeSet)skipSets[i].clone()).removeAll(expectSets[i]).toPattern(true));
                s.append(pattern);

                pattern.delete(0,pattern.length());
                s.append("\n\nexpect-skip=");
                pattern = new StringBuilder(((UnicodeSet)expectSets[i].clone()).removeAll(skipSets[i]).toPattern(true));
                s.append(pattern);
                s.append("\n\n");

                pattern.delete(0,pattern.length());
                s.append("\n\nintersection(expect,skip)=");
                UnicodeSet intersection  = ((UnicodeSet) expectSets[i].clone()).retainAll(skipSets[i]);
                pattern = new StringBuilder(intersection.toPattern(true));
                s.append(pattern);
                // Special: test coverage for append(char).
                s.append('\n');
                s.append('\n');

                errln(s.toString());
            }
        }
    }

    @Test
    public void TestBugJ2068(){
        String sample = "The quick brown fox jumped over the lazy dog";
        UCharacterIterator text = UCharacterIterator.getInstance(sample);
        Normalizer norm = new Normalizer(text,Normalizer.NFC,0);
        text.setIndex(4);
        if(text.current() == norm.current()){
            errln("Normalizer is not cloning the UCharacterIterator");
        }
     }
    @Test
     public void TestGetCombiningClass(){
        for(int i=0;i<0x10FFFF;i++){
            int cc = UCharacter.getCombiningClass(i);
            if(0xD800<= i && i<=0xDFFF && cc >0 ){
                cc = UCharacter.getCombiningClass(i);
                errln("CC: "+ cc + " for codepoint: " +Utility.hex(i,8));
            }
        }
    }

    @Test
    public void TestSerializedSet(){
        USerializedSet sset=new USerializedSet();
        UnicodeSet set = new UnicodeSet();
        int start, end;

        char[] serialized = {
            0x8007,  // length
            3,  // bmpLength
            0xc0, 0xfe, 0xfffc,
            1, 9, 0x10, 0xfffc
        };
        sset.getSet(serialized, 0);

        // collect all sets into one for contiguous output
        int[] startEnd = new int[2];
        int count=sset.countRanges();
        for(int j=0; j<count; ++j) {
            sset.getRange(j, startEnd);
            set.add(startEnd[0], startEnd[1]);
        }

        // test all of these characters
        UnicodeSetIterator it = new UnicodeSetIterator(set);
        while(it.nextRange() && it.codepoint!=UnicodeSetIterator.IS_STRING) {
            start=it.codepoint;
            end=it.codepointEnd;
            while(start<=end) {
                if(!sset.contains(start)){
                    errln("USerializedSet.contains failed for "+Utility.hex(start,8));
                }
                ++start;
            }
        }
    }

    @Test
    public void TestReturnFailure(){
        char[] term = {'r','\u00e9','s','u','m','\u00e9' };
        char[] decomposed_term = new char[10 + term.length + 2];
        int rc = Normalizer.decompose(term,0,term.length, decomposed_term,0,decomposed_term.length,true, 0);
        int rc1 = Normalizer.decompose(term,0,term.length, decomposed_term,10,decomposed_term.length,true, 0);
        if(rc!=rc1){
            errln("Normalizer decompose did not return correct length");
        }
    }

    private final static class TestCompositionCase {
        public Normalizer.Mode mode;
        public int options;
        public String input, expect;
        TestCompositionCase(Normalizer.Mode mode, int options, String input, String expect) {
            this.mode=mode;
            this.options=options;
            this.input=input;
            this.expect=expect;
        }
    }

    @Test
    public void TestComposition() {
        final TestCompositionCase cases[]=new TestCompositionCase[]{
            /*
             * special cases for UAX #15 bug
             * see Unicode Corrigendum #5: Normalization Idempotency
             * at http://unicode.org/versions/corrigendum5.html
             * (was Public Review Issue #29)
             */
            new TestCompositionCase(Normalizer.NFC, 0, "\u1100\u0300\u1161\u0327",      "\u1100\u0300\u1161\u0327"),
            new TestCompositionCase(Normalizer.NFC, 0, "\u1100\u0300\u1161\u0327\u11a8","\u1100\u0300\u1161\u0327\u11a8"),
            new TestCompositionCase(Normalizer.NFC, 0, "\uac00\u0300\u0327\u11a8",      "\uac00\u0327\u0300\u11a8"),
            new TestCompositionCase(Normalizer.NFC, 0, "\u0b47\u0300\u0b3e",            "\u0b47\u0300\u0b3e"),

            /* TODO: add test cases for UNORM_FCC here (j2151) */
        };

        String output;
        int i;

        for(i=0; i<cases.length; ++i) {
            output=Normalizer.normalize(cases[i].input, cases[i].mode, cases[i].options);
            if(!output.equals(cases[i].expect)) {
                errln("unexpected result for case "+i);
            }
        }
    }

    @Test
    public void TestGetDecomposition() {
        Normalizer2 n2=Normalizer2.getInstance(null, "nfc", Normalizer2.Mode.COMPOSE_CONTIGUOUS);
        String decomp=n2.getDecomposition(0x20);
        assertEquals("fcc.getDecomposition(space) failed", null, decomp);
        decomp=n2.getDecomposition(0xe4);
        assertEquals("fcc.getDecomposition(a-umlaut) failed", "a\u0308", decomp);
        decomp=n2.getDecomposition(0xac01);
        assertEquals("fcc.getDecomposition(Hangul syllable U+AC01) failed", "\u1100\u1161\u11a8", decomp);
    }

    @Test
    public void TestGetRawDecomposition() {
        Normalizer2 n2=Normalizer2.getNFKCInstance();
        /*
         * Raw decompositions from NFKC data are the Unicode Decomposition_Mapping values,
         * without recursive decomposition.
         */

        String decomp=n2.getRawDecomposition(0x20);
        assertEquals("nfkc.getRawDecomposition(space) failed", null, decomp);
        decomp=n2.getRawDecomposition(0xe4);
        assertEquals("nfkc.getRawDecomposition(a-umlaut) failed", "a\u0308", decomp);
        /* U+1E08 LATIN CAPITAL LETTER C WITH CEDILLA AND ACUTE */
        decomp=n2.getRawDecomposition(0x1e08);
        assertEquals("nfkc.getRawDecomposition(c-cedilla-acute) failed", "\u00c7\u0301", decomp);
        /* U+212B ANGSTROM SIGN */
        decomp=n2.getRawDecomposition(0x212b);
        assertEquals("nfkc.getRawDecomposition(angstrom sign) failed", "\u00c5", decomp);
        decomp=n2.getRawDecomposition(0xac00);
        assertEquals("nfkc.getRawDecomposition(Hangul syllable U+AC00) failed", "\u1100\u1161", decomp);
        /* A Hangul LVT syllable has a raw decomposition of an LV syllable + T. */
        decomp=n2.getRawDecomposition(0xac01);
        assertEquals("nfkc.getRawDecomposition(Hangul syllable U+AC01) failed", "\uac00\u11a8", decomp);
    }

    @Test
    public void TestCustomComp() {
        String [][] pairs={
            { "\\uD801\\uE000\\uDFFE", "" },
            { "\\uD800\\uD801\\uE000\\uDFFE\\uDFFF", "\\uD7FF\\uFFFF" },
            { "\\uD800\\uD801\\uDFFE\\uDFFF", "\\uD7FF\\U000107FE\\uFFFF" },
            { "\\uE001\\U000110B9\\u0345\\u0308\\u0327", "\\uE002\\U000110B9\\u0327\\u0345" },
            { "\\uE010\\U000F0011\\uE012", "\\uE011\\uE012" },
            { "\\uE010\\U000F0011\\U000F0011\\uE012", "\\uE011\\U000F0010" },
            { "\\uE111\\u1161\\uE112\\u1162", "\\uAE4C\\u1102\\u0062\\u1162" },
            { "\\uFFF3\\uFFF7\\U00010036\\U00010077", "\\U00010037\\U00010037\\uFFF6\\U00010037" }
        };
        Normalizer2 customNorm2;
        customNorm2=
            Normalizer2.getInstance(
                BasicTest.class.getResourceAsStream("/android/icu/dev/data/testdata/testnorm.nrm"),
                "testnorm",
                Normalizer2.Mode.COMPOSE);
        for(int i=0; i<pairs.length; ++i) {
            String[] pair=pairs[i];
            String input=Utility.unescape(pair[0]);
            String expected=Utility.unescape(pair[1]);
            String result=customNorm2.normalize(input);
            if(!result.equals(expected)) {
                errln("custom compose Normalizer2 did not normalize input "+i+" as expected");
            }
        }
    }

    @Test
    public void TestCustomFCC() {
        String[][] pairs={
            { "\\uD801\\uE000\\uDFFE", "" },
            { "\\uD800\\uD801\\uE000\\uDFFE\\uDFFF", "\\uD7FF\\uFFFF" },
            { "\\uD800\\uD801\\uDFFE\\uDFFF", "\\uD7FF\\U000107FE\\uFFFF" },
            // The following expected result is different from CustomComp
            // because of only-contiguous composition.
            { "\\uE001\\U000110B9\\u0345\\u0308\\u0327", "\\uE001\\U000110B9\\u0327\\u0308\\u0345" },
            { "\\uE010\\U000F0011\\uE012", "\\uE011\\uE012" },
            { "\\uE010\\U000F0011\\U000F0011\\uE012", "\\uE011\\U000F0010" },
            { "\\uE111\\u1161\\uE112\\u1162", "\\uAE4C\\u1102\\u0062\\u1162" },
            { "\\uFFF3\\uFFF7\\U00010036\\U00010077", "\\U00010037\\U00010037\\uFFF6\\U00010037" }
        };
        Normalizer2 customNorm2;
        customNorm2=
            Normalizer2.getInstance(
                BasicTest.class.getResourceAsStream("/android/icu/dev/data/testdata/testnorm.nrm"),
                "testnorm",
                Normalizer2.Mode.COMPOSE_CONTIGUOUS);
        for(int i=0; i<pairs.length; ++i) {
            String[] pair=pairs[i];
            String input=Utility.unescape(pair[0]);
            String expected=Utility.unescape(pair[1]);
            String result=customNorm2.normalize(input);
            if(!result.equals(expected)) {
                errln("custom FCC Normalizer2 did not normalize input "+i+" as expected");
            }
        }
    }

    @Test
    public void TestCanonIterData() {
        // For now, just a regression test.
        Normalizer2Impl impl=Norm2AllModes.getNFCInstance().impl.ensureCanonIterData();
        // U+0FB5 TIBETAN SUBJOINED LETTER SSA is the trailing character
        // in some decomposition mappings where there is a composition exclusion.
        // In fact, U+0FB5 is normalization-inert (NFC_QC=Yes, NFD_QC=Yes, ccc=0)
        // but it is not a segment starter because it occurs in a decomposition mapping.
        if(impl.isCanonSegmentStarter(0xfb5)) {
            errln("isCanonSegmentStarter(U+0fb5)=true is wrong");
        }
        // For [:Segment_Starter:] to work right, not just the property function has to work right,
        // UnicodeSet also needs a correct range starts set.
        UnicodeSet segStarters=new UnicodeSet("[:Segment_Starter:]").freeze();
        if(segStarters.contains(0xfb5)) {
            errln("[:Segment_Starter:].contains(U+0fb5)=true is wrong");
        }
        // Try characters up to Kana and miscellaneous CJK but below Han (for expediency).
        for(int c=0; c<=0x33ff; ++c) {
            boolean isStarter=impl.isCanonSegmentStarter(c);
            boolean isContained=segStarters.contains(c);
            if(isStarter!=isContained) {
                errln(String.format(
                        "discrepancy: isCanonSegmentStarter(U+%04x)=%5b != " +
                        "[:Segment_Starter:].contains(same)",
                        c, isStarter));
            }
        }
    }

    @Test
    public void TestFilteredNormalizer2() {
        Normalizer2 nfcNorm2=Normalizer2.getNFCInstance();
        UnicodeSet filter=new UnicodeSet("[^\u00a0-\u00ff\u0310-\u031f]");
        FilteredNormalizer2 fn2=new FilteredNormalizer2(nfcNorm2, filter);
        int c;
        for(c=0; c<=0x3ff; ++c) {
            int expectedCC= filter.contains(c) ? nfcNorm2.getCombiningClass(c) : 0;
            int cc=fn2.getCombiningClass(c);
            assertEquals(
                    "FilteredNormalizer2(NFC, ^A0-FF,310-31F).getCombiningClass(U+"+hex(c)+
                    ")==filtered NFC.getCC()",
                    expectedCC, cc);
        }

        // More coverage.
        StringBuilder sb=new StringBuilder();
        assertEquals("filtered normalize()", "ää\u0304",
                fn2.normalize("a\u0308ä\u0304", (Appendable)sb).toString());
        assertTrue("filtered hasBoundaryAfter()", fn2.hasBoundaryAfter('ä'));
        assertTrue("filtered isInert()", fn2.isInert(0x0313));
    }

    @Test
    public void TestFilteredAppend() {
        Normalizer2 nfcNorm2=Normalizer2.getNFCInstance();
        UnicodeSet filter=new UnicodeSet("[^\u00a0-\u00ff\u0310-\u031f]");
        FilteredNormalizer2 fn2=new FilteredNormalizer2(nfcNorm2, filter);

        // Append two strings that each contain a character outside the filter set.
        StringBuilder sb = new StringBuilder("a\u0313a");
        String second = "\u0301\u0313";
        assertEquals("append()", "a\u0313á\u0313", fn2.append(sb, second).toString());

        // Same, and also normalize the second string.
        sb.replace(0, 0x7fffffff, "a\u0313a");
        assertEquals(
            "normalizeSecondAndAppend()",
            "a\u0313á\u0313", fn2.normalizeSecondAndAppend(sb, second).toString());

        // Normalizer2.normalize(String) uses spanQuickCheckYes() and normalizeSecondAndAppend().
        assertEquals("normalize()", "a\u0313á\u0313", fn2.normalize("a\u0313a\u0301\u0313"));
    }

    @Test
    public void TestGetEasyToUseInstance() {
        // Test input string:
        // U+00A0 -> <noBreak> 0020
        // U+00C7 0301 = 1E08 = 0043 0327 0301
        String in="\u00A0\u00C7\u0301";
        Normalizer2 n2=Normalizer2.getNFCInstance();
        String out=n2.normalize(in);
        assertEquals(
                "getNFCInstance() did not return an NFC instance " +
                "(normalizes to " + prettify(out) + ')',
                "\u00A0\u1E08", out);

        n2=Normalizer2.getNFDInstance();
        out=n2.normalize(in);
        assertEquals(
                "getNFDInstance() did not return an NFD instance " +
                "(normalizes to " + prettify(out) + ')',
                "\u00A0C\u0327\u0301", out);

        n2=Normalizer2.getNFKCInstance();
        out=n2.normalize(in);
        assertEquals(
                "getNFKCInstance() did not return an NFKC instance " +
                "(normalizes to " + prettify(out) + ')',
                " \u1E08", out);

        n2=Normalizer2.getNFKDInstance();
        out=n2.normalize(in);
        assertEquals(
                "getNFKDInstance() did not return an NFKD instance " +
                "(normalizes to " + prettify(out) + ')',
                " C\u0327\u0301", out);

        n2=Normalizer2.getNFKCCasefoldInstance();
        out=n2.normalize(in);
        assertEquals(
                "getNFKCCasefoldInstance() did not return an NFKC_Casefold instance " +
                "(normalizes to " + prettify(out) + ')',
                " \u1E09", out);
    }

    @Test
    public void TestNFC() {
        // Coverage tests.
        Normalizer2 nfc = Normalizer2.getNFCInstance();
        assertTrue("nfc.hasBoundaryAfter(space)", nfc.hasBoundaryAfter(' '));
        assertFalse("nfc.hasBoundaryAfter(ä)", nfc.hasBoundaryAfter('ä'));
    }

    @Test
    public void TestNFD() {
        // Coverage tests.
        Normalizer2 nfd = Normalizer2.getNFDInstance();
        assertTrue("nfd.hasBoundaryAfter(space)", nfd.hasBoundaryAfter(' '));
        assertFalse("nfd.hasBoundaryAfter(ä)", nfd.hasBoundaryAfter('ä'));
    }

    @Test
    public void TestFCD() {
        // Coverage tests.
        Normalizer2 fcd = Normalizer2.getInstance(null, "nfc", Normalizer2.Mode.FCD);
        assertTrue("fcd.hasBoundaryAfter(space)", fcd.hasBoundaryAfter(' '));
        assertFalse("fcd.hasBoundaryAfter(ä)", fcd.hasBoundaryAfter('ä'));
        assertTrue("fcd.isInert(space)", fcd.isInert(' '));
        assertFalse("fcd.isInert(ä)", fcd.isInert('ä'));

        // This implementation method is unreachable via public API.
        Norm2AllModes.FCDNormalizer2 impl = (Norm2AllModes.FCDNormalizer2)fcd;
        assertEquals("fcd impl.getQuickCheck(space)", 1, impl.getQuickCheck(' '));
        assertEquals("fcd impl.getQuickCheck(ä)", 0, impl.getQuickCheck('ä'));
    }

    @Test
    public void TestNoneNormalizer() {
        // Use the deprecated Mode Normalizer.NONE for coverage of the internal NoopNormalizer2
        // as far as its methods are reachable that way.
        assertEquals("NONE.concatenate()", "ä\u0327",
                Normalizer.concatenate("ä", "\u0327", Normalizer.NONE, 0));
        assertTrue("NONE.isNormalized()", Normalizer.isNormalized("ä\u0327", Normalizer.NONE, 0));
    }

    @Test
    public void TestNoopNormalizer2() {
        // Use the internal class directly for coverage of methods that are not publicly reachable.
        Normalizer2 noop = Norm2AllModes.NOOP_NORMALIZER2;
        assertEquals("noop.normalizeSecondAndAppend()", "ä\u0327",
                noop.normalizeSecondAndAppend(new StringBuilder("ä"), "\u0327").toString());
        assertEquals("noop.getDecomposition()", null, noop.getDecomposition('ä'));
        assertTrue("noop.hasBoundaryAfter()", noop.hasBoundaryAfter(0x0308));
        assertTrue("noop.isInert()", noop.isInert(0x0308));
    }

    /*
     * This unit test covers two 'get' methods in class Normalizer2Impl. It only tests that
     * an object is returned.
     */
    @Test
    public void TestGetsFromImpl() {
       Normalizer2Impl nfcImpl = Norm2AllModes.getNFCInstance().impl;
       assertNotEquals("getNormTrie() returns null", null, nfcImpl.getNormTrie());
       assertNotEquals("getFCD16FromBelow180() returns null", null,
                       nfcImpl.getFCD16FromBelow180(0));
    }

    /*
     * Abstract class Normalizer2 has non-abstract methods which are overwritten by
     * its derived classes. To test these methods a derived class is defined here.
     */
    public class TestNormalizer2 extends Normalizer2 {

        public TestNormalizer2() {}
        @Override
        public StringBuilder normalize(CharSequence src, StringBuilder dest) { return null; }
        @Override
        public Appendable normalize(CharSequence src, Appendable dest) { return null; }
        @Override
        public StringBuilder normalizeSecondAndAppend(
            StringBuilder first, CharSequence second) { return null; }
        @Override
        public StringBuilder append(StringBuilder first, CharSequence second) { return null; }
        @Override
        public String getDecomposition(int c) { return null; }
        @Override
        public boolean isNormalized(CharSequence s) { return false; }
        @Override
        public Normalizer.QuickCheckResult quickCheck(CharSequence s) { return null; }
        @Override
        public int spanQuickCheckYes(CharSequence s) { return 0; }
        @Override
        public boolean hasBoundaryBefore(int c) { return false; }
        @Override
        public boolean hasBoundaryAfter(int c) { return false; }
        @Override
        public boolean isInert(int c) { return false; }
    }

    final TestNormalizer2 tnorm2 = new TestNormalizer2();
    @Test
    public void TestGetRawDecompositionBase() {
        int c = 'à';
        assertEquals("Unexpected value returned from Normalizer2.getRawDecomposition()",
                     null, tnorm2.getRawDecomposition(c));
    }

    @Test
    public void TestComposePairBase() {
        int a = 'a';
        int b = '\u0300';
        assertEquals("Unexpected value returned from Normalizer2.composePair()",
                     -1, tnorm2.composePair(a, b));
    }

    @Test
    public void TestGetCombiningClassBase() {
        int c = '\u00e0';
        assertEquals("Unexpected value returned from Normalizer2.getCombiningClass()",
                     0, tnorm2.getCombiningClass(c));
    }
}
