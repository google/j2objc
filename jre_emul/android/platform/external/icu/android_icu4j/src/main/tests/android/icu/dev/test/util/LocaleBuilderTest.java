/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2012, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.util;

import java.util.Arrays;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.util.IllformedLocaleException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Builder;

/**
 * Test cases for ULocale.LocaleBuilder
 */
public class LocaleBuilderTest extends TestFmwk {
    @Test
    public void TestLocaleBuilder() {
        // "L": +1 = language
        // "S": +1 = script
        // "R": +1 = region
        // "V": +1 = variant
        // "K": +1 = Unicode locale key / +2 = Unicode locale type
        // "A": +1 = Unicode locale attribute
        // "E": +1 = extension letter / +2 = extension value
        // "P": +1 = private use
        // "U": +1 = ULocale
        // "B": +1 = BCP47 language tag

        // "C": Clear all
        // "N": Clear extensions
        // "D": +1 = Unicode locale attribute to be removed

        // "X": indicates an exception must be thrown
        // "T": +1 = expected language tag / +2 = expected locale string
        String[][] TESTCASE = {
                {"L", "en", "R", "us", "T", "en-US", "en_US"},
                {"L", "en", "R", "CA", "L", null, "T", "und-CA", "_CA"},
                {"L", "en", "R", "CA", "L", "", "T", "und-CA", "_CA"},
                {"L", "en", "R", "FR", "L", "fr", "T", "fr-FR", "fr_FR"},
                {"L", "123", "X"},
                {"R", "us", "T", "und-US", "_US"},
                {"R", "usa", "X"},
                {"R", "123", "L", "it", "R", null, "T", "it", "it"},
                {"R", "123", "L", "it", "R", "", "T", "it", "it"},
                {"R", "123", "L", "en", "T", "en-123", "en_123"},
                {"S", "LATN", "L", "DE", "T", "de-Latn", "de_Latn"},
                {"L", "De", "S", "latn", "R", "de", "S", "", "T", "de-DE", "de_DE"},
                {"L", "De", "S", "latn", "R", "de", "S", null, "T", "de-DE", "de_DE"},
                {"S", "latin", "X"},
                {"V", "1234", "L", "en", "T", "en-1234", "en__1234"},
                {"V", "1234", "L", "en", "V", "5678", "T", "en-5678", "en__5678"},
                {"V", "1234", "L", "en", "V", null, "T", "en", "en"},
                {"V", "1234", "L", "en", "V", "", "T", "en", "en"},
                {"V", "123", "X"},
                {"U", "en_US", "T", "en-US", "en_US"},
                {"U", "en_US_WIN", "X"},
                {"B", "fr-FR-1606nict-u-ca-gregory-x-test", "T", "fr-FR-1606nict-u-ca-gregory-x-test", "fr_FR_1606NICT@calendar=gregorian;x=test"},
                {"B", "ab-cde-fghij", "T", "cde-fghij", "cde__FGHIJ"},
                {"B", "und-CA", "T", "und-CA", "_CA"},
                {"B", "en-US-x-test-lvariant-var", "T", "en-US-x-test-lvariant-var", "en_US_VAR@x=test"},
                {"B", "en-US-VAR", "X"},
                {"U", "ja_JP@calendar=japanese;currency=JPY", "L", "ko", "T", "ko-JP-u-ca-japanese-cu-jpy", "ko_JP@calendar=japanese;currency=jpy"},
                {"U", "ja_JP@calendar=japanese;currency=JPY", "K", "ca", null, "T", "ja-JP-u-cu-jpy", "ja_JP@currency=jpy"},
                {"U", "ja_JP@calendar=japanese;currency=JPY", "E", "u", "attr1-ca-gregory", "T", "ja-JP-u-attr1-ca-gregory", "ja_JP@attribute=attr1;calendar=gregorian"},
                {"U", "en@colnumeric=yes", "K", "kn", "", "T", "en-u-kn-true", "en@colnumeric=yes"},
                {"L", "th", "R", "th", "K", "nu", "thai", "T", "th-TH-u-nu-thai", "th_TH@numbers=thai"},
                {"U", "zh_Hans", "R", "sg", "K", "ca", "badcalendar", "X"},
                {"U", "zh_Hans", "R", "sg", "K", "cal", "gregory", "X"},
                {"E", "z", "ExtZ", "L", "en", "T", "en-z-extz", "en@z=extz"},
                {"E", "z", "ExtZ", "L", "en", "E", "z", "", "T", "en", "en"},
                {"E", "z", "ExtZ", "L", "en", "E", "z", null, "T", "en", "en"},
                {"E", "a", "x", "X"},
                {"E", "a", "abc_def", "T", "und-a-abc-def", "@a=abc-def"},
                // Design limitation - typeless u extension keyword 00 below is interpreted as a boolean value true/yes.
                // With the legacy keyword syntax, "yes" is used for such boolean value instead of "true".
                // However, once the legacy keyword is translated back to BCP 47 u extension, key "00" is unknown,
                // so "yes" is preserved - not mapped to "true". We could change the code to automatically transform
                // "yes" to "true", but it will break roundtrip conversion if BCP 47 u extension has "00-yes".
                {"L", "en", "E", "u", "bbb-aaa-00", "T", "en-u-aaa-bbb-00-yes", "en@00=yes;attribute=aaa-bbb"},
                {"L", "fr", "R", "FR", "P", "Yoshito-ICU", "T", "fr-FR-x-yoshito-icu", "fr_FR@x=yoshito-icu"},
                {"L", "ja", "R", "jp", "K", "ca", "japanese", "T", "ja-JP-u-ca-japanese", "ja_JP@calendar=japanese"},
                {"K", "co", "PHONEBK", "K", "ca", "gregory", "L", "De", "T", "de-u-ca-gregory-co-phonebk", "de@calendar=gregorian;collation=phonebook"},
                {"E", "o", "OPQR", "E", "a", "aBcD", "T", "und-a-abcd-o-opqr", "@a=abcd;o=opqr"},
                {"E", "u", "nu-thai-ca-gregory", "L", "TH", "T", "th-u-ca-gregory-nu-thai", "th@calendar=gregorian;numbers=thai"},
                {"L", "en", "K", "tz", "usnyc", "R", "US", "T", "en-US-u-tz-usnyc", "en_US@timezone=America/New_York"},
                {"L", "de", "K", "co", "phonebk", "K", "ks", "level1", "K", "kk", "true", "T", "de-u-co-phonebk-kk-true-ks-level1", "de@collation=phonebook;colnormalization=yes;colstrength=primary"},
                {"L", "en", "R", "US", "K", "ca", "gregory", "T", "en-US-u-ca-gregory", "en_US@calendar=gregorian"},
                {"L", "en", "R", "US", "K", "cal", "gregory", "X"},
                {"L", "en", "R", "US", "K", "ca", "gregorian", "X"},
                {"L", "en", "R", "US", "K", "kn", "", "T", "en-US-u-kn-true", "en_US@colnumeric=yes"},
                {"B", "de-DE-u-co-phonebk", "C", "L", "pt", "T", "pt", "pt"},
                {"B", "ja-jp-u-ca-japanese", "N", "T", "ja-JP", "ja_JP"},
                {"B", "es-u-def-abc-co-trad", "A", "hij", "D", "def", "T", "es-u-abc-hij-co-trad", "es@attribute=abc-hij;collation=traditional"},
                {"B", "es-u-def-abc-co-trad", "A", "hij", "D", "def", "D", "def", "T", "es-u-abc-hij-co-trad", "es@attribute=abc-hij;collation=traditional"},
                {"L", "en", "A", "aa", "X"},
                {"B", "fr-u-attr1-cu-eur", "D", "attribute1", "X"},
        };

        Builder bld_st = new Builder();

        for (int tidx = 0; tidx < TESTCASE.length; tidx++) {
            int i = 0;
            String[] expected = null;

            Builder bld = bld_st;

            bld.clear();

            while (true) {
                String method = TESTCASE[tidx][i++];
                try {
                    // setters
                    if (method.equals("L")) {
                        bld.setLanguage(TESTCASE[tidx][i++]);
                    } else if (method.equals("S")) {
                        bld.setScript(TESTCASE[tidx][i++]);
                    } else if (method.equals("R")) {
                        bld.setRegion(TESTCASE[tidx][i++]);
                    } else if (method.equals("V")) {
                        bld.setVariant(TESTCASE[tidx][i++]);
                    } else if (method.equals("K")) {
                        String key = TESTCASE[tidx][i++];
                        String type = TESTCASE[tidx][i++];
                        bld.setUnicodeLocaleKeyword(key, type);
                    } else if (method.equals("A")) {
                        bld.addUnicodeLocaleAttribute(TESTCASE[tidx][i++]);
                    } else if (method.equals("E")) {
                        String key = TESTCASE[tidx][i++];
                        String value = TESTCASE[tidx][i++];
                        bld.setExtension(key.charAt(0), value);
                    } else if (method.equals("P")) {
                        bld.setExtension(ULocale.PRIVATE_USE_EXTENSION, TESTCASE[tidx][i++]);
                    } else if (method.equals("U")) {
                        bld.setLocale(new ULocale(TESTCASE[tidx][i++]));
                    } else if (method.equals("B")) {
                        bld.setLanguageTag(TESTCASE[tidx][i++]);
                    }
                    // clear / remove
                    else if (method.equals("C")) {
                        bld.clear();
                    } else if (method.equals("N")) {
                        bld.clearExtensions();
                    } else if (method.equals("D")) {
                        bld.removeUnicodeLocaleAttribute(TESTCASE[tidx][i++]);
                    }
                    // result
                    else if (method.equals("X")) {
                        errln("FAIL: No excetion was thrown - test csae: "
                                + Arrays.toString(TESTCASE[tidx]));
                    } else if (method.equals("T")) {
                        expected = new String[2];
                        expected[0] = TESTCASE[tidx][i];
                        expected[1] = TESTCASE[tidx][i + 1];
                        break;
                    } else {
                        // Unknow test method
                        errln("Unknown test case method: There is an error in the test case data.");
                    }

                } catch (IllformedLocaleException e) {
                    if (TESTCASE[tidx][i].equals("X")) {
                        // This exception is expected
                        break;
                    } else {
                        errln("FAIL: IllformedLocaleException at offset " + i
                                + " in test case: " + Arrays.toString(TESTCASE[tidx]));
                    }
                }
            }
            if (expected != null) {
                ULocale loc = bld.build();
                if (!expected[1].equals(loc.toString())) {
                    errln("FAIL: Wrong locale ID - " + loc + 
                            " for test case: " + Arrays.toString(TESTCASE[tidx]));
                }
                String langtag = loc.toLanguageTag();
                if (!expected[0].equals(langtag)) {
                    errln("FAIL: Wrong language tag - " + langtag + 
                            " for test case: " + Arrays.toString(TESTCASE[tidx]));
                }
                ULocale loc1 = ULocale.forLanguageTag(langtag);
                if (!loc.equals(loc1)) {
                    errln("FAIL: Language tag round trip failed for " + loc);
                }
            }
        }
    }

    @Test
    public void TestSetLocale() {
        ULocale loc = new ULocale("th_TH@calendar=gregorian");
        Builder bld = new Builder();
        try {
            bld.setLocale(loc);
            ULocale loc1 = bld.build();
            if (!loc.equals(loc1)) {
                errln("FAIL: Locale loc1 " + loc1 + " was returned by the builder.  Expected " + loc);
            }
            bld.setLanguage("").setUnicodeLocaleKeyword("ca", "buddhist")
                .setLanguage("TH").setUnicodeLocaleKeyword("ca", "gregory");
            ULocale loc2 = bld.build();
            if (!loc.equals(loc2)) {
                errln("FAIL: Locale loc2 " + loc2 + " was returned by the builder.  Expected " + loc);
            }            
        } catch (IllformedLocaleException e) {
            errln("FAIL: IllformedLocaleException: " + e.getMessage());
        }
    }
}
