/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test;

import static android.icu.impl.LocaleDisplayNamesImpl.DataTableType.LANG;
import static android.icu.impl.LocaleDisplayNamesImpl.DataTableType.REGION;

import java.util.Locale;

import org.junit.Test;

import android.icu.impl.LocaleDisplayNamesImpl;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.LocaleDisplayNames.DialectHandling;
import android.icu.util.ULocale;

public class TestLocaleNamePackaging extends TestFmwk {
    public TestLocaleNamePackaging() {
    }

    public boolean validate() {
        logln("language data: " + LocaleDisplayNamesImpl.haveData(LANG));
        logln("  region data: " + LocaleDisplayNamesImpl.haveData(REGION));
        return true;
    }

    private static ULocale[] locales = {
        ULocale.ROOT, ULocale.US, new ULocale("es_ES"), ULocale.GERMANY,
        new ULocale("und_TH")
    };

    // Java Locales equivalent to above
    private static Locale[] javaLocales = {
        new Locale(""), Locale.US, new Locale("es", "ES"), Locale.GERMANY,
        new Locale("und", "TH")
    };

    @Test
    public void testRegionDisplayNames() {
        String[] expectedWithRegionData = {
            "",
            "US",
            "ES",
            "DE",
            "TH",
            "",
            "United States",
            "Spain",
            "Germany",
            "Thailand",
            "",
            "Estados Unidos",
            "Espa\u00f1a",
            "Alemania",
            "Tailandia",
            "",
            "Vereinigte Staaten",
            "Spanien",
            "Deutschland",
            "Thailand",
            "",
            "United States",
            "Spain",
            "Germany",
            "Thailand",
        };
        String[] expectedWithoutRegionData = {
            "",
            "US",
            "ES",
            "DE",
            "TH",
        };
        String[] expected = LocaleDisplayNamesImpl.haveData(REGION) ? 
            expectedWithRegionData : expectedWithoutRegionData;

        int n = 0;
        for (ULocale displayLocale : locales) {
            LocaleDisplayNames dn = LocaleDisplayNames.getInstance(displayLocale);
            for (ULocale targetLocale : locales) {
                String result = dn.regionDisplayName(targetLocale.getCountry());
                assertEquals(targetLocale + " in " + displayLocale, expected[n++], result);
                if (n == expected.length) {
                    n = 0;
                }
            }
        }

        // Same test with Java Locale
        n = 0;
        for (Locale displayJavaLocale : javaLocales) {
            LocaleDisplayNames dn = LocaleDisplayNames.getInstance(displayJavaLocale);
            for (Locale targetLocale : javaLocales) {
                String result = dn.regionDisplayName(targetLocale.getCountry());
                assertEquals(targetLocale + " in " + displayJavaLocale, expected[n++], result);
                if (n == expected.length) {
                    n = 0;
                }
            }
        }

    }

    @Test
    public void testLanguageDisplayNames() {
        String[] expectedWithLanguageData = {
            "",
            "en",
            "es",
            "de",
            "und",
            "",
            "English",
            "Spanish",
            "German",
            "Unknown Language",
            "",
            "ingl\u00E9s",
            "espa\u00F1ol",
            "alem\u00E1n",
            "lengua desconocida",
            "",
            "Englisch",
            "Spanisch",
            "Deutsch",
            "Unbestimmte Sprache",
            "",
            "English",
            "Spanish",
            "German",
            "Unknown Language",
        };
        String[] expectedWithoutLanguageData = {
            "",
            "en",
            "es",
            "de",
            "und"
        };
        String[] expected = LocaleDisplayNamesImpl.haveData(LANG) ?
            expectedWithLanguageData : expectedWithoutLanguageData;

        int n = 0;
        for (ULocale displayLocale : locales) {
            LocaleDisplayNames dn = LocaleDisplayNames.getInstance(displayLocale);
            for (ULocale targetLocale : locales) {
                String result = dn.languageDisplayName(targetLocale.getLanguage());
                assertEquals(targetLocale + " in " + displayLocale, expected[n++], result);
                if (n == expected.length) {
                    n = 0;
                }
            }
        }

        // Same test with Java Locale
        n = 0;
        for (Locale displayJavaLocale : javaLocales) {
            LocaleDisplayNames dn = LocaleDisplayNames.getInstance(displayJavaLocale);
            for (Locale targetLocale : javaLocales) {
                String result = dn.languageDisplayName(targetLocale.getLanguage());
                assertEquals(targetLocale + " in " + displayJavaLocale, expected[n++], result);
                if (n == expected.length) {
                    n = 0;
                }
            }
        }

    }

    // test a 'root' locale, with keywords
    @Test
    public void testLocaleDisplayNameWithKeywords() {
        String[] expectedWithLanguageData = {
            "root (collation=phonebook)",
            "Root (Phonebook Sort Order)",
            "ra\u00EDz (orden de list\u00EDn telef\u00F3nico)",
            "Root (Telefonbuch-Sortierung)",
            "Root (Phonebook Sort Order)",
        };
        String[] expectedWithoutLanguageData = {
            "root (collation=phonebook)",
        };
        String[] expected = LocaleDisplayNamesImpl.haveData(LANG) ?
            expectedWithLanguageData : expectedWithoutLanguageData;

        ULocale kl = new ULocale("@collation=phonebook");

        int n = 0;
        for (ULocale displayLocale : locales) {
            LocaleDisplayNames dn = LocaleDisplayNames.getInstance(displayLocale);
            String result = dn.localeDisplayName(kl);
            assertEquals(kl + " in " + displayLocale, expected[n++], result);
            if (n == expected.length) {
                n = 0;
            }
        }
    }

    @Test
    public void testLanguageDisplayNameDoesNotTranslateRoot() {
        // "root" is not a language code-- the fact that we have our data organized this
        // way is immaterial.  "root" remains untranslated whether we have data or not.
        LocaleDisplayNames dn = LocaleDisplayNames.getInstance(ULocale.US);
        assertEquals("root", "root", dn.languageDisplayName("root"));
    }

    @Test
    public void testLanguageDisplayNameDoesNotTranslateDialects() {
        // Dialect ids are also not language codes.
        LocaleDisplayNames dn = LocaleDisplayNames.getInstance(ULocale.US,
                                                               DialectHandling.DIALECT_NAMES);
        assertEquals("dialect", "en_GB", dn.languageDisplayName("en_GB"));

        String target = LocaleDisplayNamesImpl.haveData(LANG)
            ? "British English"
            : (LocaleDisplayNamesImpl.haveData(REGION)
               ? "en (United Kingdom)"
               : "en (GB)");
        assertEquals("dialect 2", target, dn.localeDisplayName("en_GB"));
    }
    
    @Test
    public void testLocaleKeywords() {
        LocaleDisplayNames dn = LocaleDisplayNames.getInstance(ULocale.US,
                DialectHandling.DIALECT_NAMES);
        String name = dn.localeDisplayName("de@collation=phonebook");
        String target = LocaleDisplayNamesImpl.haveData(LANG) ? 
                "German (Phonebook Sort Order)" : "de (collation=phonebook)";
        assertEquals("collation", target, name);
        
    }
}
