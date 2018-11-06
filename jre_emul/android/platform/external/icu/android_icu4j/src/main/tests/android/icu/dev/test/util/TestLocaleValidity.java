/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2015-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.ValidIdentifiers;
import android.icu.impl.ValidIdentifiers.Datasubtype;
import android.icu.impl.ValidIdentifiers.Datatype;
import android.icu.impl.ValidIdentifiers.ValiditySet;
import android.icu.impl.locale.KeyTypeData;
import android.icu.impl.locale.LocaleValidityChecker;
import android.icu.impl.locale.LocaleValidityChecker.Where;
import android.icu.util.ULocale;

/**
 * @author markdavis
 *
 */
public class TestLocaleValidity extends TestFmwk {
    @Test
    public void testBasic() {
        String[][] tests = {
                {"OK", "eng-us"},
                {"OK", "en-u-ca-chinese"},
                {"OK", "en-x-abcdefg"},
                {"OK", "x-abcdefg"},
                {"OK", "en-u-sd-usca"},
                {"OK", "en-US-u-sd-usca"},
                {"OK", "en-t-it"},
                {"OK", "und-Cyrl-t-und-latn"},
                {"OK", "und"},
                {"OK", "en"},
                {"OK", "en-Hant"},
                {"OK", "zh-Hant-1606nict-1694acad"},
                {"OK", "zh-Hant"},
                {"OK", "zh-Hant-AQ"},
                {"OK", "x-abcdefg-g-foobar"},

                {"OK", "en-u-ca-buddhist"},
                {"OK", "en-u-ca-islamic-umalqura"}, // additive
                {"OK", "en-u-cf-account"},
                {"OK", "en-u-co-big5han"},
                {"OK", "en-u-cu-adp"},
                {"OK", "en-u-fw-fri"},
                {"OK", "en-u-hc-h11"},
                {"OK", "en-u-ka-noignore"},
                {"OK", "en-u-kb-false"},
                {"OK", "en-u-kc-false"},
                {"OK", "en-u-kf-false"},
                {"OK", "en-u-kk-false"},
                {"OK", "en-u-kn-false"},
                {"OK", "en-u-kr-latn-digit-symbol"}, // reorder codes, multiple
                {"OK", "en-u-kr-latn-digit-others-Cyrl"}, // reorder codes, duplicat
                {"OK", "en-u-ks-identic"},
                {"OK", "en-u-kv-currency"},
                {"OK", "en-u-nu-ahom"},
                {"OK", "en-u-sd-usny"},
                {"OK", "en-u-tz-adalv"},
                {"OK", "en-u-va-posix"},

                {"OK", "en-t-d0-accents"},
                {"OK", "en-u-em-default"},
                {"OK", "en-t-i0-handwrit"},
                {"OK", "en-t-k0-101key"},
                {"OK", "en-u-lb-loose"},
                {"OK", "en-u-lw-breakall"},
                {"OK", "en-t-m0-alaloc"},
                {"OK", "en-u-ms-uksystem"},
                {"OK", "en-t-s0-accents"},
                {"OK", "en-u-ss-none"},
                {"OK", "en-t-t0-und"},
                {"OK", "en-t-x0-12345678"},

                {"OK", "en-u-rg-uszzzz"},
                {"OK", "en-u-rg-USZZZZ"},
                {"{region, 001}", "en-u-rg-001zzzz"}, // well-formed but invalid

                {"OK", "en-u-sd-uszzzz"},

                // really long case

                {"OK", "en-u-ca-buddhist-ca-islamic-umalqura-cf-account-co-big5han-cu-adp-fw-fri-hc-h11-ka-noignore-kb-false-kc-false-kf-false-kk-false-kn-false-kr-latn-digit-symbol-ks-identic-kv-currency-nu-ahom-sd-usny-tz-adalv-va-posix"},

                // bad case (for language tag)
                {"{language, root}", "root"},

                // deprecated, but turned into valid by ULocale.Builder()
                {"OK", "en-u-ca-islamicc"}, // deprecated
                {"OK", "en-u-tz-aqams"}, // deprecated

                // Bad syntax (caught by ULocale.Builder())
                {"Invalid subtag: t [at index 0]", "t-it"},
                {"Invalid subtag: u [at index 0]", "u-it"},

                {"Incomplete extension 'u' [at index 3]", "en-u"},
                {"Incomplete extension 't' [at index 3]", "en-t"},
                {"Empty subtag [at index 0]", ""},
                {"Incomplete privateuse [at index 0]", "x-abc$defg"},
                {"Invalid subtag: $ [at index 3]", "EN-$"},
                {"Invalid subtag: $ [at index 0]", "$"},

                // bad extension

                {"{illegal, q}", "en-q-abcdefg"},

                {"Incomplete privateuse [at index 3]", "en-x-123456789"},
                {"Empty subtag [at index 14]", "en-x-12345678--a"},

                // bad subtags

                {"{variant, FOOBAR}", "zh-Hant-1606nict-1694acad-foobar"},
                {"{region, AB}", "zh-Hant-AB"},
                {"{language, ex}", "ex"},
                {"{script, Hanx}", "zh-Hanx"},
                {"{language, qaa}", "qaa"},

                // bad types for keys

                {"{u, ca-chinesx}", "en-u-ca-chinesx"},
                {"{script, Latx}", "und-Cyrl-t-und-latx"},
                {"{u, sd-usca}", "en-AQ-u-sd-usca"},

                {"{u, ca-buddhisx}", "en-u-ca-buddhisx"},
                {"{u, ca-islamic-umalqurx}", "en-u-ca-islamic-umalqurx"}, // additive
                {"{u, cf-accounx}", "en-u-cf-accounx"},
                {"{u, co-big5hax}", "en-u-co-big5hax"},
                {"{u, cu-adx}", "en-u-cu-adx"},
                {"{u, fw-frx}", "en-u-fw-frx"},
                {"{u, hc-h1x}", "en-u-hc-h1x"},
                {"{u, ka-noignorx}", "en-u-ka-noignorx"},
                {"{u, kb-falsx}", "en-u-kb-falsx"},
                {"{u, kc-falsx}", "en-u-kc-falsx"},
                {"{u, kf-falsx}", "en-u-kf-falsx"},
                {"{u, kk-falsx}", "en-u-kk-falsx"},
                {"{u, kn-falsx}", "en-u-kn-falsx"},
                {"{u, kr-symbox}", "en-u-kr-latn-digit-symbox"}, // reorder codes, multiple
                {"{u, kr-latn}", "en-u-kr-latn-digit-latn"}, // reorder codes, duplicat
                {"{u, kr-zzzz}", "en-u-kr-latn-others-digit-Zzzz"}, // reorder codes, duplicat
                {"{u, kr-zsym}", "en-u-kr-Zsym"}, // reorder codes, duplicat
                {"{u, kr-qaai}", "en-u-kr-Qaai"}, // reorder codes, duplicat
                {"{u, ks-identix}", "en-u-ks-identix"},
                {"{u, kv-currencx}", "en-u-kv-currencx"},
                {"{u, nu-ahox}", "en-u-nu-ahox"},
                {"{u, sd-usnx}", "en-u-sd-usnx"},
                {"{u, tz-adalx}", "en-u-tz-adalx"},
                {"{u, va-posit}", "en-u-va-posit"},

                // too many items

                {"{u, cu-usd}", "en-u-cu-adp-usd"},

                // use deprecated subtags. testDeprecated checks if they work when Datasubtype.deprecated is added
                //{"{u, ca-civil}", "en-u-ca-islamicc"}, // deprecated, but turns into valid
                {"{u, co-direct}", "en-u-co-direct"}, // deprecated
                {"{u, kh}", "en-u-kh-false"}, // deprecated
                {"{u, tz-camtr}", "en-u-tz-camtr"}, // deprecated
                {"{u, vt}", "en-u-vt-0020-0041"}, // deprecated
        };
        final LinkedHashSet<String> foundKeys = new LinkedHashSet<String>();
        check(tests, foundKeys, Datasubtype.regular, Datasubtype.unknown);

        LinkedHashSet<String> missing = new LinkedHashSet(KeyTypeData.getBcp47Keys());
        missing.removeAll(foundKeys);
        if (!assertEquals("Missing keys", Collections.EMPTY_SET, missing)) {
            // print out template for missing cases for adding
            for (String key : missing) {
                char extension = key.charAt(key.length()-1) < 'A' ? 't' : 'u';
                String bestType = null;
                for (String type : KeyTypeData.getBcp47KeyTypes(key)) {
                    if (KeyTypeData.isDeprecated(key, type)) {
                        bestType = type;
                        continue;
                    }
                    bestType = type;
                    break;
                }
                System.out.println("{\"OK\", \"en-" + extension + "-" + key + "-" + bestType + "\"},");
            }
        }
    }

    // TODO(user): turned off for failure - need to investigate
    @Ignore
    @Test
    public void testMissing() {
        String[][] tests = {
                {"OK", "en-u-lb-loose"},
                {"OK", "en-u-lw-breakall"},
                {"OK", "en-u-ms-metric"},
                {"OK", "en-u-ss-none"},
        };
        check(tests, null, Datasubtype.regular, Datasubtype.unknown);
    }

    @Test
    public void testTSubtags() {
        String[][] tests = {
                //                {"OK", "und-Cyrl-t-und-latn-m0-ungegn-2007"},
                //                {"{t, ungegg}", "und-Cyrl-t-und-latn-m0-ungegg-2007"},
                //                {"OK", "en-t-i0-handwrit"},
                //                {"OK", "en-t-k0-101key"},
                //                {"OK", "en-t-m0-alaloc"},
                //                {"OK", "en-t-t0-und"},
                //                {"OK", "en-t-x0-anythin"},
        };
        check(tests, null, Datasubtype.regular, Datasubtype.unknown);
    }

    @Test
    public void testDeprecated() {
        String[][] tests = {
                {"OK", "en-u-co-direct"}, // deprecated
                {"OK", "en-u-kh-false"}, // deprecated
                {"OK", "en-u-tz-camtr"}, // deprecated
                {"OK", "en-u-vt-0020"}, // deprecated
        };
        check(tests, null, Datasubtype.regular, Datasubtype.unknown, Datasubtype.deprecated);
    }

    private void check(String[][] tests, Set<String> keys, Datasubtype... datasubtypes) {
        int count = 0;
        LocaleValidityChecker localeValidityChecker = new LocaleValidityChecker(datasubtypes);
        for (String[] test : tests) {
            if (test[1].endsWith("-va-posix") && logKnownIssue("12615","Validity check wrong for -va-posix?"))
                continue;
            check(++count, localeValidityChecker, test[0], test[1], keys);
        }
    }

    private void check(int count, LocaleValidityChecker all, String expected, String locale, Set<String> keys) {
        ULocale ulocale;
        try {
            ulocale = new ULocale.Builder().setLanguageTag(locale).build();
            if (keys != null) {
                addKeys(ulocale, keys);
            }
        } catch (Exception e) {
            assertEquals(count + ". " + locale, expected, e.getMessage());
            return;
        }
        Where where = new Where();
        all.isValid(ulocale, where);
        assertEquals(count + ". " + locale, expected, where.toString());

        //        ULocale ulocale2 = ULocale.forLanguageTag(locale);
        //        final String languageTag2 = ulocale2.toLanguageTag();
        //
        //        if (languageTag.equals(languageTag2)) {
        //            return;
        //        }
        //        all.isValid(ulocale2, where);
        //        assertEquals(ulocale2 + ", " + ulocale2.toLanguageTag(), expected, where.toString());

        // problem: ULocale("$").toLanguageTag() becomes valid
    }

    private void addKeys(ULocale ulocale, Set<String> keys) {
        for (char cp : ulocale.getExtensionKeys()) {
            switch (cp) {
            case 't':
            case 'u':
                String extensionString = ulocale.getExtension(cp);
                String[] parts = extensionString.split("-");
                for (String part : parts) {
                    if (part.length() == 2) { // key
                        keys.add(part);
                    }
                }
                break;
            }
        }
    }


    // Quick testing for now

    @Test
    public void testValidIdentifierData() {
        showValid(Datasubtype.unknown, Datatype.script, EnumSet.of(Datasubtype.regular, Datasubtype.unknown), "Zzzz");
        showValid(null, Datatype.script, EnumSet.of(Datasubtype.regular), "Zzzz");
        showValid(Datasubtype.regular, Datatype.subdivision, EnumSet.of(Datasubtype.regular), "US-CA");
        showValid(Datasubtype.regular, Datatype.subdivision, EnumSet.of(Datasubtype.regular), "US", "CA");
        showValid(null, Datatype.subdivision, EnumSet.of(Datasubtype.regular), "US-?");
        showValid(null, Datatype.subdivision, EnumSet.of(Datasubtype.regular), "US", "?");
        if (isVerbose()) {
            showAll();
        }
    }

    private static void showAll() {
        Map<Datatype, Map<Datasubtype, ValiditySet>> data = ValidIdentifiers.getData();
        for (Entry<Datatype, Map<Datasubtype, ValiditySet>> e1 : data.entrySet()) {
            System.out.println(e1.getKey());
            for (Entry<Datasubtype, ValiditySet> e2 : e1.getValue().entrySet()) {
                System.out.println("\t" + e2.getKey());
                System.out.println("\t\t" + e2.getValue());
            }
        }
    }

    private void showValid(Datasubtype expected, Datatype datatype, Set<Datasubtype> datasubtypes, String code) {
        Datasubtype value = ValidIdentifiers.isValid(datatype, datasubtypes, code);
        assertEquals(datatype + ", " + datasubtypes + ", " + code, expected, value);
    }
    private void showValid(Datasubtype expected, Datatype datatype, Set<Datasubtype> datasubtypes, String code, String code2) {
        Datasubtype value = ValidIdentifiers.isValid(datatype, datasubtypes, code, code2);
        assertEquals(datatype + ", " + datasubtypes + ", " + code + ", " + code2, expected, value);
    }
}
