/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util;

import static java.util.Locale.FilteringMode.AUTOSELECT_FILTERING;
import static java.util.Locale.FilteringMode.EXTENDED_FILTERING;
import static java.util.Locale.FilteringMode.IGNORE_EXTENDED_RANGES;
import static java.util.Locale.FilteringMode.MAP_EXTENDED_RANGES;
import static java.util.Locale.FilteringMode.REJECT_EXTENDED_RANGES;

import com.google.j2objc.EnvironmentUtil;
import java.io.ObjectInputStream;
import java.text.BreakIterator;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.stream.Collectors;

public class LocaleTest extends junit.framework.TestCase {

    public void test_extension_absent() throws Exception {
        Locale locale = Locale.forLanguageTag("en-US");
        assertFalse(locale.hasExtensions());
        assertEquals(locale, locale.stripExtensions());
    }

    public void test_extension_builder() throws Exception {
        Locale.Builder b = new Locale.Builder();
        Locale localeWithoutExtension = b.build();
        b.setExtension('g', "FO_ba-BR_bg");
        Locale locale = b.build();
        assertTrue(locale.hasExtensions());
        assertFalse(locale.stripExtensions().hasExtensions());
        assertEquals(localeWithoutExtension, locale.stripExtensions());
    }

    public void test_extension_languageTag() throws Exception {
        Locale lA = Locale.forLanguageTag("en-Latn-US-x-foo");
        Locale lB = Locale.forLanguageTag("en-Latn-US");
        assertTrue(lA.hasExtensions());
        assertFalse(lB.hasExtensions());
        assertEquals(lB, lA.stripExtensions());
    }

    // http://b/2611311; if there's no display language/country/variant, use the raw codes.
    /* TODO(kstanger): enable when display names are supported.
    public void test_getDisplayName_invalid() throws Exception {
        Locale invalid = new Locale("AaBbCc", "DdEeFf", "GgHhIi");

        assertEquals("aabbcc", invalid.getLanguage());
        assertEquals("DDEEFF", invalid.getCountry());
        assertEquals("GgHhIi", invalid.getVariant());

        // Android using icu4c < 49.2 returned empty strings for display language, country,
        // and variant, but a display name made up of the raw strings.
        // Newer releases return slightly different results, but no less unreasonable.
        assertEquals("aabbcc", invalid.getDisplayLanguage());
        assertEquals("DDEEFF", invalid.getDisplayCountry());
        assertEquals("GGHHII", invalid.getDisplayVariant());
        assertEquals("aabbcc (DDEEFF,GGHHII)", invalid.getDisplayName());
    }*/

    public void test_getDisplayName_emptyCodes() {
        Locale emptyLanguage = new Locale("", "DdeEFf");
        assertEquals("", emptyLanguage.getDisplayLanguage());

        Locale emptyCountry = new Locale("AaBbCc", "");
        assertEquals("", emptyCountry.getDisplayCountry());

        Locale emptyCountryAndLanguage = new Locale("", "", "Farl");
        assertEquals("", emptyCountryAndLanguage.getDisplayLanguage());
        assertEquals("", emptyCountryAndLanguage.getDisplayCountry());
        assertEquals("Farl", emptyCountryAndLanguage.getDisplayVariant());
    }

    // http://b/2611311; if there's no display language/country/variant, use the raw codes.
    /* TODO(kstanger): enable when display names are supported.
    public void test_getDisplayName_unknown() throws Exception {
        Locale unknown = new Locale("xx", "YY", "Traditional");
        assertEquals("xx", unknown.getLanguage());
        assertEquals("YY", unknown.getCountry());
        assertEquals("Traditional", unknown.getVariant());

        assertEquals("xx", unknown.getDisplayLanguage());
        assertEquals("YY", unknown.getDisplayCountry());
        assertEquals("Traditional", unknown.getDisplayVariant());
        assertEquals("xx (YY,Traditional)", unknown.getDisplayName());
    }*/

    public void test_getDisplayName_easy() throws Exception {
        assertEquals("English", Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH));
        assertEquals("German", Locale.GERMAN.getDisplayLanguage(Locale.ENGLISH));
        assertEquals("Englisch", Locale.ENGLISH.getDisplayLanguage(Locale.GERMAN));
        assertEquals("Deutsch", Locale.GERMAN.getDisplayLanguage(Locale.GERMAN));
    }

    // https://b/issue?id=13790528
    /* TODO(kstanger): enable when display names are supported.
    public void test_getDisplayName_withScriptsAndVariants() throws Exception {
        // Script + Country.
        assertEquals("Chinese (Traditional Han,China)",
                Locale.forLanguageTag("zh-Hant-CN").getDisplayName(Locale.US));
        // Script + Variant.
        assertEquals("Chinese (Traditional Han,VARIANT)",
                Locale.forLanguageTag("zh-Hant-VARIANT").getDisplayName(Locale.US));
        // Country + Variant.
        assertEquals("Chinese (China,VARIANT)",
                Locale.forLanguageTag("zh-CN-VARIANT").getDisplayName(Locale.US));
        // Script + Country + variant.
        assertEquals("Chinese (Traditional Han,China,VARIANT)",
                Locale.forLanguageTag("zh-Hant-CN-VARIANT").getDisplayName(Locale.US));
    }*/

    public void testGetDisplayCountry_locale_null() {
        assertThrowsNpe(() -> {
            Locale.forLanguageTag("en-US").getDisplayCountry(null);
        });
    }

    public void testGetDisplayLanguage_locale_null() {
        assertThrowsNpe(() -> {
            Locale.forLanguageTag("en-US").getDisplayLanguage(null);
        });
    }

    public void testGetDisplayScript_locale_null() {
        assertThrowsNpe(() -> {
            Locale.forLanguageTag("sr-Cyrl-BA").getDisplayScript(null);
        });
    }

    public void testGetDisplayVariant_locale_null() {
        assertThrowsNpe(() -> {
            Locale.forLanguageTag("en-US-POSIX").getDisplayVariant(null);
        });
    }

    /* TODO(kstanger): enable when display names are supported.
    public void test_getDisplayCountry_8870289() throws Exception {
        assertEquals("Hong Kong", new Locale("", "HK").getDisplayCountry(Locale.US));
        assertEquals("Palestine", new Locale("", "PS").getDisplayCountry(Locale.US));

        assertEquals("Cocos (Keeling) Islands", new Locale("", "CC").getDisplayCountry(Locale.US));
        assertEquals("Falkland Islands (Islas Malvinas)", new Locale("", "FK").getDisplayCountry(Locale.US));
        assertEquals("Myanmar (Burma)", new Locale("", "MM").getDisplayCountry(Locale.US));
        assertEquals("Taiwan", new Locale("", "TW").getDisplayCountry(Locale.US));
    }*/

    public void test_tl_and_fil() throws Exception {
        // In jb-mr1, we had a last-minute hack to always return "Filipino" because
        // icu4c 4.8 didn't have any localizations for fil. (http://b/7291355).
        //
        // After the icu4c 4.9 upgrade, we could localize "fil" correctly, though we
        // needed another hack to supply "fil" instead of "tl" to icu4c. (http://b/8023288).
        //
        // These hacks have now been reverted, so "tl" really does represent
        // tagalog and not filipino.
        Locale tl = new Locale("tl");
        Locale tl_PH = new Locale("tl", "PH");
        assertEquals("Tagalog", tl.getDisplayLanguage(Locale.ENGLISH));
        assertEquals("Tagalog", tl_PH.getDisplayLanguage(Locale.ENGLISH));
        assertEquals("Tagalog", tl.getDisplayLanguage(tl));
        assertEquals("Tagalog", tl_PH.getDisplayLanguage(tl_PH));

        Locale es_MX = new Locale("es", "MX");
        assertEquals("tagalo", tl.getDisplayLanguage(es_MX));
        assertEquals("tagalo", tl_PH.getDisplayLanguage(es_MX));

        // Assert that we can deal with "fil" correctly, since we've switched
        // to using "fil" for Filipino, and not "tl". (http://b/15873165).
        Locale fil = new Locale("fil");
        Locale fil_PH = new Locale("fil", "PH");
        assertEquals("Filipino", fil.getDisplayLanguage(Locale.ENGLISH));
        assertEquals("Filipino", fil_PH.getDisplayLanguage(Locale.ENGLISH));
        assertEquals("Filipino", fil.getDisplayLanguage(fil));
        assertEquals("Filipino", fil_PH.getDisplayLanguage(fil_PH));

        assertEquals("filipino", fil.getDisplayLanguage(es_MX));
        assertEquals("filipino", fil_PH.getDisplayLanguage(es_MX));
    }

    // http://b/3452611; Locale.getDisplayLanguage fails for the obsolete language codes.
    public void test_getDisplayName_obsolete() throws Exception {
      // Locale strings updated in macOS 10.12 to match iOS.
      if (!EnvironmentUtil.onMacOSX() || EnvironmentUtil.onMinimumOSVersion("10.12")) {
        // he (new) -> iw (obsolete)
        assertObsolete("he", "iw", "עברית");
        // id (new) -> in (obsolete)
        assertObsolete("id", "in", "Indonesia");
      }
    }

    private static void assertObsolete(String newCode, String oldCode, String displayName) {
        // Either code should get you the same locale.
        Locale newLocale = new Locale(newCode);
        Locale oldLocale = new Locale(oldCode);
        assertEquals(newLocale, oldLocale);

        // No matter what code you used to create the locale, you should get the old code back.
        assertEquals(oldCode, newLocale.getLanguage());
        assertEquals(oldCode, oldLocale.getLanguage());

        // Check we get the right display name.
        assertEquals(displayName, newLocale.getDisplayLanguage(newLocale));
        assertEquals(displayName, oldLocale.getDisplayLanguage(newLocale));
        assertEquals(displayName, newLocale.getDisplayLanguage(oldLocale));
        assertEquals(displayName, oldLocale.getDisplayLanguage(oldLocale));

        // Check that none of the 'getAvailableLocales' methods are accidentally returning two
        // equal locales (because to ICU they're different, but we mangle one into the other).
        assertOnce(newLocale, BreakIterator.getAvailableLocales());
        assertOnce(newLocale, Calendar.getAvailableLocales());
        assertOnce(newLocale, Collator.getAvailableLocales());
        assertOnce(newLocale, DateFormat.getAvailableLocales());
        assertOnce(newLocale, DateFormatSymbols.getAvailableLocales());
        assertOnce(newLocale, NumberFormat.getAvailableLocales());
        assertOnce(newLocale, Locale.getAvailableLocales());
    }

    /* J2ObjC: ICU defines locales not supported on iOS.
    public void testGetAvailableLocales_icuConsistency() {
        Locale[] javaLocales = Locale.getAvailableLocales();
        ULocale[] icuLocales = ULocale.getAvailableLocales();
        Set<Locale> javaSet = new HashSet<>(Arrays.asList(javaLocales));
        Set<Locale> icuSet = Arrays.stream(icuLocales)
            .map(uLocale -> uLocale.toLocale())
            .collect(Collectors.toSet());
        assertEquals(javaSet, icuSet);
        // Assert no duplicated entries
        assertEquals(javaLocales.length, javaSet.size());
        assertEquals(icuLocales.length, icuSet.size());
    }
    */

    private static void assertOnce(Locale element, Locale[] array) {
        int count = 0;
        for (Locale l : array) {
            if (l.equals(element)) {
                ++count;
            }
        }
        assertEquals(1, count);
    }

    /**
     * Check that the straightforward ways to try to construct/obtain a Locale
     * with null country don't work.
     */
    public void test_nullCountry_fails() {
        try {
            new Locale(/* language */ "en", /* country */ null);
        } catch (NullPointerException expected) {
        }
        try {
            new Locale(/* language */ "en", /* country */ null, /* variant */ "EN");
        } catch (NullPointerException expected) {
        }
        assertNotNull(Locale.getDefault().getCountry());
        for (Locale locale : Locale.getAvailableLocales()) {
            assertNotNull(locale.getCountry());
        }
    }

    /* No iOS support for ISO 639-2 three-character country codes.
    public void test_getISO3Country() {
        // Empty country code.
        assertEquals("", new Locale("en", "").getISO3Country());

        // Invalid country code.
        try {
            assertEquals("", new Locale("en", "XX").getISO3Country());
            fail();
        } catch (MissingResourceException expected) {
            assertEquals("FormatData_en_XX", expected.getClassName());
            assertEquals("ShortCountry", expected.getKey());
        }

        // Valid country code.
        assertEquals("CAN", new Locale("", "CA").getISO3Country());
        assertEquals("CAN", new Locale("en", "CA").getISO3Country());
        assertEquals("CAN", new Locale("xx", "CA").getISO3Country());

        // 3 letter country codes.
        assertEquals("CAN", new Locale("en", "CAN").getISO3Country());
        assertEquals("CAN", new Locale("frankenderp", "CAN").getISO3Country());
    }*/

    /* No iOS support for ISO 639-2 three-character language codes.
    public void test_getISO3Language() {
        // Empty language code.
        assertEquals("", new Locale("", "US").getISO3Language());

        // Invalid language code.
        try {
            assertEquals("", new Locale("xx", "US").getISO3Language());
            fail();
        } catch (MissingResourceException expected) {
            assertEquals("FormatData_xx_US", expected.getClassName());
            assertEquals("ShortLanguage", expected.getKey());
        }

        // Valid language code.
        assertEquals("eng", new Locale("en", "").getISO3Language());
        assertEquals("eng", new Locale("en", "CA").getISO3Language());
        assertEquals("eng", new Locale("en", "XX").getISO3Language());

        // 3 letter language code.
        assertEquals("eng", new Locale("eng", "USA").getISO3Language());
        assertEquals("eng", new Locale("eng", "US").getISO3Language());
    }*/

    public void test_Builder_setLanguage() {
        Locale.Builder b = new Locale.Builder();

        // Should normalize to lower case.
        b.setLanguage("EN");
        assertEquals("en", b.build().getLanguage());

        b = new Locale.Builder();

        // Too short.
        try {
            b.setLanguage("e");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Too long
        try {
            // note: pre-openJdk Locale assumed that language will be between
            // 2-3 characters. openJdk accepts 2-8 character languages.
            b.setLanguage("foobarbar");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Contains non ASCII characters
        try {
            b.setLanguage("தமிழ்");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Null or empty languages must clear state.
        b = new Locale.Builder();
        b.setLanguage("en");
        b.setLanguage(null);
        assertEquals("", b.build().getLanguage());

        b = new Locale.Builder();
        b.setLanguage("en");
        b.setLanguage("");
        assertEquals("", b.build().getLanguage());
    }

    public void test_Builder_setRegion() {
        Locale.Builder b = new Locale.Builder();

        // Should normalize to upper case.
        b.setRegion("us");
        assertEquals("US", b.build().getCountry());

        b = new Locale.Builder();

        // Too short.
        try {
            b.setRegion("e");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Too long
        try {
            b.setRegion("USA");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Contains non ASCII characters
        try {
            b.setLanguage("திழ்");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Null or empty regions must clear state.
        b = new Locale.Builder();
        b.setRegion("US");
        b.setRegion(null);
        assertEquals("", b.build().getCountry());

        b = new Locale.Builder();
        b.setRegion("US");
        b.setRegion("");
        assertEquals("", b.build().getCountry());
    }

    public void test_Builder_setVariant() {
        Locale.Builder b = new Locale.Builder();

        // Should normalize "_" to "-"
        b = new Locale.Builder();
        b.setVariant("vArIaNt-VaRiAnT-VARIANT");
        assertEquals("vArIaNt_VaRiAnT_VARIANT", b.build().getVariant());

        b = new Locale.Builder();
        // Too short
        try {
            b.setVariant("shor");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Too long
        try {
            b.setVariant("waytoolong");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        try {
            b.setVariant("foooo-foooo-fo");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Special case. Variants of length 4 are allowed when the first
        // character is a digit.
        b.setVariant("0ABC");
        assertEquals("0ABC", b.build().getVariant());

        b = new Locale.Builder();
        b.setVariant("variant");
        b.setVariant(null);
        assertEquals("", b.build().getVariant());

        b = new Locale.Builder();
        b.setVariant("variant");
        b.setVariant("");
        assertEquals("", b.build().getVariant());
    }

    public void test_Builder_setLocale() {
        // Default case.
        Locale.Builder b = new Locale.Builder();
        b.setLocale(Locale.US);
        assertEquals("en", b.build().getLanguage());
        assertEquals("US", b.build().getCountry());

        // Should throw when locale is malformed.
        // - Bad language
        Locale bad = new Locale("e", "US");
        b = new Locale.Builder();
        try {
            b.setLocale(bad);
            fail();
        } catch (IllformedLocaleException expected) {
        }
        // - Bad country
        bad = new Locale("en", "USA");
        try {
            b.setLocale(bad);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // - Bad variant
        bad = new Locale("en", "US", "c");
        try {
            b.setLocale(bad);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Test values are normalized as they should be
        b = new Locale.Builder();
        Locale good = new Locale("EN", "us", "variant-VARIANT");
        b.setLocale(good);
        Locale l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());
        assertEquals("variant_VARIANT", l.getVariant());

        // Test that none of the existing fields are messed with
        // if the locale update fails.
        b = new Locale.Builder();
        b.setLanguage("fr").setRegion("FR");

        try {
            b.setLocale(bad);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        l = b.build();
        assertEquals("fr", l.getLanguage());
        assertEquals("FR", l.getCountry());
    }

    public void test_Builder_setScript() {
        Locale.Builder b = new Locale.Builder();

        // Should normalize variants to lower case.
        b.setScript("lAtN");
        assertEquals("Latn", b.build().getScript());

        b = new Locale.Builder();
        // Too short
        try {
            b.setScript("lat");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Too long
        try {
            b.setScript("latin");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        b = new Locale.Builder();
        b.setScript("Latn");
        b.setScript(null);
        assertEquals("", b.build().getScript());

        b = new Locale.Builder();
        b.setScript("Latn");
        b.setScript("");
        assertEquals("", b.build().getScript());
    }

    public void test_Builder_clear() {
        Locale.Builder b = new Locale.Builder();
        b.setLanguage("en").setScript("Latn").setRegion("US")
                .setVariant("POSIX").setExtension('g', "foo")
                .setUnicodeLocaleKeyword("fo", "baar")
                .addUnicodeLocaleAttribute("baaaaz");

        Locale l = b.clear().build();
        assertEquals("", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getVariant());
        assertEquals("", l.getScript());
        assertTrue(l.getExtensionKeys().isEmpty());
    }

    public void test_Builder_setExtension() {
        Locale.Builder b = new Locale.Builder();
        b.setExtension('g', "FO_ba-BR_bg");

        Locale l = b.build();
        assertEquals("fo-ba-br-bg", l.getExtension('g'));

        b = new Locale.Builder();

        // Too short
        try {
            b.setExtension('g', "fo-ba-br-x");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Too long
        try {
            b.setExtension('g', "fo-ba-br-extension");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Special case, the private use extension allows single char subtags.
        b.setExtension(Locale.PRIVATE_USE_EXTENSION, "fo-ba-br-m");
        l = b.build();
        assertEquals("fo-ba-br-m", l.getExtension('x'));

        // Special case, the unicode locale extension must be parsed into
        // its individual components. The correctness of the parse is tested
        // in test_parseUnicodeExtension.
        b.setExtension(Locale.UNICODE_LOCALE_EXTENSION, "foooo_BaaaR-BA_Baz-bI_BIZ");
        l = b.build();
        // Note that attributes and keywords are sorted alphabetically.
        assertEquals("baaar-foooo-ba-baz-bi-biz", l.getExtension('u'));

        assertTrue(l.getUnicodeLocaleAttributes().contains("foooo"));
        assertTrue(l.getUnicodeLocaleAttributes().contains("baaar"));
        assertEquals("baz", l.getUnicodeLocaleType("ba"));
        assertEquals("biz", l.getUnicodeLocaleType("bi"));
    }

    public void test_Builder_clearExtensions() {
        Locale.Builder b = new Locale.Builder();
        b.setExtension('g', "FO_ba-BR_bg");
        b.setExtension(Locale.PRIVATE_USE_EXTENSION, "fo-ba-br-m");
        b.clearExtensions();

        assertTrue(b.build().getExtensionKeys().isEmpty());
    }

    private static Locale fromLanguageTag(String languageTag, boolean useBuilder) {
        if (useBuilder) {
            return (new Locale.Builder().setLanguageTag(languageTag).build());
        } else {
            return Locale.forLanguageTag(languageTag);
        }
    }

    private void test_setLanguageTag_wellFormedsingleSubtag(boolean useBuilder) {
        Locale l = fromLanguageTag("en", useBuilder);
        assertEquals("en", l.getLanguage());

        l = fromLanguageTag("eng", useBuilder);
        assertEquals("eng", l.getLanguage());
    }

    private void test_setLanguageTag_twoWellFormedSubtags(boolean useBuilder) {
        Locale l =  fromLanguageTag("en-US", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());

        l =  fromLanguageTag("eng-419", useBuilder);
        assertEquals("eng", l.getLanguage());
        assertEquals("419", l.getCountry());

        // Script tags shouldn't be mis-recognized as regions.
        l =  fromLanguageTag("en-Latn", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("Latn", l.getScript());

        // Neither should variant tags.
        l =  fromLanguageTag("en-POSIX", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getScript());
        assertEquals("POSIX", l.getVariant());
    }

    public void test_Builder_setLanguageTag_malformedTags() {
        try {
            fromLanguageTag("a", true);
            fail();
        } catch (IllformedLocaleException ifle) {
        }

        // Three subtags
        // lang-region-illformedvariant
        try {
            fromLanguageTag("en-US-BA", true);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // lang-variant-illformedvariant
        try {
            fromLanguageTag("en-FOOOO-BA", true);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Four or more sub tags
        try {
            fromLanguageTag("en-US-POSIX-P2", true);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        try {
            fromLanguageTag("en-Latn-US-P2", true);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Extensions
        // Ill-formed empty extension.
        try {
            fromLanguageTag("en-f-f", true);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Ill-formed empty extension.
        try {
            fromLanguageTag("en-f", true);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Two extension keys in a row (i.e, another case of an ill-formed
        // empty exception).
        try {
            fromLanguageTag("en-f-g-fo-baar", true);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Dangling empty key after a well formed extension.
        try {
            fromLanguageTag("en-f-fo-baar-g", true);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Ill-formed extension with long subtag.
        try {
            fromLanguageTag("en-f-fooobaaaz", true);
            fail();
        } catch (IllformedLocaleException expected) {
        }
    }

    private void test_setLanguageTag_threeWellFormedSubtags(boolean useBuilder) {
        // lang-region-variant
        Locale l = fromLanguageTag("en-US-FOOOO", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());
        assertEquals("", l.getScript());
        assertEquals("FOOOO", l.getVariant());

        // lang-script-variant
        l = fromLanguageTag("en-Latn-FOOOO", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("Latn", l.getScript());
        assertEquals("FOOOO", l.getVariant());

        // lang-script-region
        l = fromLanguageTag("en-Latn-US", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());
        assertEquals("Latn", l.getScript());
        assertEquals("", l.getVariant());

        // lang-variant-variant
        l = fromLanguageTag("en-FOOOO-BAAAR", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getScript());
        assertEquals("FOOOO_BAAAR", l.getVariant());
    }

    private void test_setLanguageTag_fourOrMoreWellFormedSubtags(boolean useBuilder) {
        // lang-script-region-variant.
        Locale l = fromLanguageTag("en-Latn-US-foooo", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("US", l.getCountry());
        assertEquals("foooo", l.getVariant());

        // Variant with multiple subtags.
        l = fromLanguageTag("en-Latn-US-foooo-gfffh", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("US", l.getCountry());
        assertEquals("foooo_gfffh", l.getVariant());

        // Variant with 3 subtags. POSIX shouldn't be recognized
        // as a region or a script.
        l = fromLanguageTag("en-POSIX-P2003-P2004", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getScript());
        assertEquals("", l.getCountry());
        assertEquals("POSIX_P2003_P2004", l.getVariant());

        // lang-script-variant-variant.
        l = fromLanguageTag("en-Latn-POSIX-P2003", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("", l.getCountry());
        assertEquals("POSIX_P2003", l.getVariant());

        // lang-region-variant-variant
        l = fromLanguageTag("en-US-POSIX-P2003", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getScript());
        assertEquals("US", l.getCountry());
        assertEquals("POSIX_P2003", l.getVariant());
    }

    private void test_setLanguageTag_withWellFormedExtensions(boolean useBuilder) {
        Locale l = fromLanguageTag("en-Latn-GB-foooo-g-fo-bar-baaz", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("GB", l.getCountry());
        assertEquals("foooo", l.getVariant());
        assertEquals("fo-bar-baaz", l.getExtension('g'));

        // Multiple extensions
        l = fromLanguageTag("en-Latn-US-foooo-g-fo-bar-h-go-gaz", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("US", l.getCountry());
        assertEquals("foooo", l.getVariant());
        assertEquals("fo-bar", l.getExtension('g'));
        assertEquals("go-gaz", l.getExtension('h'));

        // Unicode locale extension.
        l = fromLanguageTag("en-Latn-US-foooo-u-koooo-fo-bar", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("US", l.getCountry());
        assertEquals("koooo-fo-bar", l.getExtension('u'));
        assertTrue(l.getUnicodeLocaleAttributes().contains("koooo"));
        assertEquals("bar", l.getUnicodeLocaleType("fo"));

        // Extensions without variants
        l = fromLanguageTag("en-Latn-US-f-fo", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("US", l.getCountry());
        assertEquals("fo", l.getExtension('f'));

        l = fromLanguageTag("en-Latn-f-fo", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("fo", l.getExtension('f'));

        l = fromLanguageTag("en-f-fo", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getScript());
        assertEquals("", l.getCountry());
        assertEquals("fo", l.getExtension('f'));

        l = fromLanguageTag("en-f-fo-x-a-b-c-d-e-fo", useBuilder);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getScript());
        assertEquals("", l.getCountry());
        assertEquals("fo", l.getExtension('f'));
        assertEquals("a-b-c-d-e-fo", l.getExtension('x'));
    }

    /**
     * Tests filtering locales using basic language ranges (without "*").
     */
    public void test_filter_basic() {
        List<String> tags = tagsOf(
            "en-US",
            "en-Latn-US",
            "zh-Hant-TW",
            "es-419",
            "fr-FR",
            "ja-JP"
        );
        List<LanguageRange> ranges = new ArrayList<>();
        ranges.add(new LanguageRange("en-US"));

        // By default, basic filtering is used for basic language ranges
        assertFilter(tagsOf("en-US"), ranges, tags);

        // Since no extended ranges are given, these should produce the same result
        assertFilter(tagsOf("en-US"), ranges, tags, AUTOSELECT_FILTERING);
        assertFilter(tagsOf("en-US"), ranges, tags, REJECT_EXTENDED_RANGES);
        assertFilter(tagsOf("en-US"), ranges, tags, IGNORE_EXTENDED_RANGES);

        // EXTENDED_FILTERING can be enabled explicitly even when the priority
        // list only contains basic; then, en-US also matches en-Latn-US.
        assertFilter(tagsOf("en-US", "en-Latn-US"), ranges, tags, EXTENDED_FILTERING);

        ranges.add(new LanguageRange("zh-Hant-TW"));
        assertFilter(tagsOf("en-US", "zh-Hant-TW"), ranges, tags);
    }

    /**
     * Tests that filtering is case insensitive.
     */
    public void test_filter_caseInsensitive() {
        List<String> tags = tagsOf("de-DE", "de-Latn-DE", "ja-jp");

        assertFilter(tagsOf("de-DE"), languageRangesOf("dE-De"), tags);
        assertFilter(tagsOf("ja-jp"), languageRangesOf("ja-JP"), tags);
        assertFilter(tagsOf("ja-jp"), languageRangesOf("JA-jp"), tags);
    }

    /**
     * Tests filtering locales using extended language ranges (with "*"), per
     * the example from RFC 4647 section 3.3.2
     */
    public void test_filter_extended() {
        List<LanguageRange> priorityList = languageRangesOf("de-DE", "de-*-DE");
        List<String> tags = tagsOf(
            "de", // not matched: missing 'DE'
            "de-DE", // German, as used in Germany
            "de-de", // German, as used in Germany
            "de-Latn-DE", // Latin script
            "de-Latf-DE", // Fraktur variant of Latin script
            "de-DE-x-goethe", // private-use subtag
            "de-Latn-DE-1996",
            "de-Deva", // not matched: 'Deva' not equal to 'DE'
            "de-Deva-DE", // Devanagari script
            "de-x-DE" // not matched: singleton 'x' occurs before 'DE'
        );

        List<String> filteredTags = tagsOf(
            "de-DE", // German, as used in Germany
            "de-Latn-DE", // Latin script
            "de-Latf-DE", // Fraktur variant of Latin script
            "de-DE-x-goethe", // private-use subtag
            "de-Latn-DE-1996",
            "de-Deva-DE" // Devanagari script
        );

        assertFilter(filteredTags, priorityList, tags, EXTENDED_FILTERING);

        // Because the priority list contains an extended language range, filtering
        // should default to extended, so default filtering should yield the same results:
        assertFilter(filteredTags, priorityList, tags);
        assertFilter(filteredTags, priorityList, tags, AUTOSELECT_FILTERING);

        // Ignoring the extended range (de-*-DE) matches only a single language tag, "de-DE"
        assertFilter(tagsOf("de-DE", "de-DE-x-goethe"), priorityList, tags, IGNORE_EXTENDED_RANGES);
    }

    /**
     * Tests that filtering with {@link Locale.FilteringMode#REJECT_EXTENDED_RANGES}
     * throws IllegalArgumentException if passed an extended tag / language range.
     */
    public void test_filter_extended_reject() {
        try {
            Locale.filter(
                languageRangesOf("de-DE", "de-*-DE"),
                localesOf("de-DE", "fr-FR"),
                REJECT_EXTENDED_RANGES);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            Locale.filterTags(
                languageRangesOf("de-DE", "de-*-DE"),
                tagsOf("de-DE", "fr-FR"),
                REJECT_EXTENDED_RANGES);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * Checks that a '*' occurring in a LanguageRange is interpreted in compliance
     * with RFC 4647 section 3.2: if the first subtag is a '*' then the entire range
     * is treated as "*", otherwise each wildcard subtag is removed.
     */
    public void test_filter_extended_wildcardInLanguageRange() {
        List<String> tags = tagsOf("en-US", "de-DE", "en-AU", "en-Latn-US");
        // en-*-US is treated as "en-US", so only en-US matches
        assertFilter(tagsOf("en-US"), languageRangesOf("en-*-US"), tags, MAP_EXTENDED_RANGES);

        // *-US is treated as "*", so all locales match
        assertFilter(tags, languageRangesOf("*-US"), tags, MAP_EXTENDED_RANGES);

        // Same behavior with just "*"
        assertFilter(tags, languageRangesOf("*"), tags, MAP_EXTENDED_RANGES);
    }

    /**
     * Tests that a '*' in a Locale in the priority list matches a subtag only
     * when extended filtering is used; note that this is different from a
     * '*' occuring in a LanguageRange, where it is ignored.
     */
    public void test_filter_extended_wildcardInPriorityList() {
        List<String> tags = tagsOf("de-DE", "de-Latn-DE", "ja-JP");
        assertFilter(tagsOf("de-DE", "de-Latn-DE"),
            languageRangesOf("dE-*-De"), tags);
        assertFilter(tagsOf("de-DE", "de-Latn-DE"),
            languageRangesOf("dE-De"), tags, EXTENDED_FILTERING);
    }

    public void test_filter_noMatch() {
        List<String> noTag = Collections.emptyList();

        List<String> tags = tagsOf("en-US", "fr-Fr", "de-DE");

        assertFilter(noTag, languageRangesOf("en-AU"), tags);
        assertFilter(noTag, languageRangesOf("es-419"), tags);
        assertFilter(noTag, languageRangesOf("zh-*-TW"), tags);
    }

    /**
     * Tests that various methods throw NullPointerException when given {@code null}
     * as an argument.
     */
    public void test_filter_nullArguments() {
        List<String> tags = tagsOf("de-DE", "de-Latn-DE");
        List<Locale> locales = localesOf(tags);
        List<LanguageRange> languageRanges = languageRangesOf("en-*-US", "de-DE");

        assertThrowsNpe(() -> { Locale.filter(null, locales); });
        assertThrowsNpe(() -> { Locale.filter(languageRanges, null); });

        assertThrowsNpe(() -> { Locale.filterTags(null, tags); });
        assertThrowsNpe(() -> { Locale.filterTags(languageRanges, null); });

        // The documentation doesn't say whether FilteringMode is allowed to be
        // null or what the sematnics of that null are; currently it is allowed.
        // This test ensures that we are aware if we change this behavior in future.
        List<Locale> filteredLocales = Locale.filter(languageRanges, locales, null);
        List<String> filteredTags = Locale.filterTags(languageRanges, tags, null);
        assertEquals(localesOf("de-DE"), filteredLocales);
        assertEquals(tagsOf("de-DE"), filteredTags);
    }

    /**
     * Tests that filtered locales are returned in priority order.
     */
    public void test_filter_priorityOrder() {
        List<LanguageRange> priorityList = languageRangesOf("zh-Hant-TW", "en-US");

        List<String> tags = tagsOf(
            "en-US",
            "zh-Hant-TW",
            "es-419",
            "fr-FR"
        );
        assertFilter(tagsOf("zh-Hant-TW", "en-US"), languageRangesOf("zh-Hant-TW", "en-US"), tags);
        assertFilter(tagsOf("en-US", "zh-Hant-TW"), languageRangesOf("en-US", "zh-Hant-TW"), tags);
    }

    /**
     * Tests that the List returned by the various {@code filter} methods is modifiable,
     * as specified by the documentation.
     */
    public void test_filter_resultIsModifiable_locales() {
        List<LanguageRange> priorityList = languageRangesOf("de-DE", "de-*-DE");
        List<Locale> locales = localesOf("de-DE", "de-Latn-DE", "ja-JP");

        Locale fake = Locale.FRANCE;
        // should not throw
        Locale.filter(priorityList, locales).add(fake);
        Locale.filter(priorityList, locales, AUTOSELECT_FILTERING).add(fake);
        Locale.filter(priorityList, locales, EXTENDED_FILTERING).add(fake);
        Locale.filter(priorityList, locales, IGNORE_EXTENDED_RANGES).add(fake);
        Locale.filter(priorityList, locales, MAP_EXTENDED_RANGES).add(fake);
        Locale.filter(languageRangesOf("de-DE"), locales, REJECT_EXTENDED_RANGES).add(fake);
    }

    public void test_filter_resultIsModifiable_tags() {
        List<LanguageRange> priorityList = languageRangesOf("de-DE", "de-*-DE");
        List<String> tags = tagsOf("de-DE", "de-Latn-DE", "ja-JP");

        String fake = "fr-FR";
        // should not throw
        Locale.filterTags(priorityList, tags).add(fake);
        Locale.filterTags(priorityList, tags, AUTOSELECT_FILTERING).add(fake);
        Locale.filterTags(priorityList, tags, EXTENDED_FILTERING).add(fake);
        Locale.filterTags(priorityList, tags, IGNORE_EXTENDED_RANGES).add(fake);
        Locale.filterTags(priorityList, tags, MAP_EXTENDED_RANGES).add(fake);
        Locale.filterTags(languageRangesOf("de-DE"), tags, REJECT_EXTENDED_RANGES).add(fake);
    }

    public void test_forLanguageTag() {
        test_setLanguageTag_wellFormedsingleSubtag(false);
        test_setLanguageTag_twoWellFormedSubtags(false);
        test_setLanguageTag_threeWellFormedSubtags(false);
        test_setLanguageTag_fourOrMoreWellFormedSubtags(false);
        test_setLanguageTag_withWellFormedExtensions(false);
    }

    public void test_Builder_setLanguageTag() {
        test_setLanguageTag_wellFormedsingleSubtag(true);
        test_setLanguageTag_twoWellFormedSubtags(true);
        test_setLanguageTag_threeWellFormedSubtags(true);
        test_setLanguageTag_fourOrMoreWellFormedSubtags(true);
        test_setLanguageTag_withWellFormedExtensions(true);
    }

    /* TODO(kstanger): enable when display names are supported.
    public void test_getDisplayScript() {
        Locale.Builder b = new Locale.Builder();
        b.setLanguage("en").setRegion("US").setScript("Latn");

        Locale l = b.build();

        // getAndSetDefaultForTest(uncategorizedLocale, displayLocale, formatLocale)
        Locales locales = Locales.getAndSetDefaultForTest(Locale.US, Locale.GERMANY, Locale.FRANCE);
        try {
            // Check that getDisplayScript() uses the default DISPLAY Locale.
            assertEquals("Lateinisch", l.getDisplayScript()); // the German word for "Latin"

            assertEquals("latino", l.getDisplayScript(Locale.ITALY));

            // Fallback for navajo, a language for which we don't have data.
            assertEquals("Latin", l.getDisplayScript(new Locale("nv", "US")));

            b = new Locale.Builder();
            b.setLanguage("en").setRegion("US").setScript("Fooo");

            // Will be equivalent to getScriptCode for scripts that aren't
            // registered with ISO-15429 (but are otherwise well formed).
            l = b.build();
            assertEquals("Fooo", l.getDisplayScript());
        } finally {
            locales.setAsDefault();
        }
    }*/

    public void test_setLanguageTag_malformedTags() {
        Locale l = fromLanguageTag("a", false);
        assertEquals("", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getVariant());
        assertEquals("", l.getScript());

        l = fromLanguageTag("en-US-BA", false);
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());
        assertEquals("", l.getVariant());
        assertEquals("", l.getScript());

        l = fromLanguageTag("en-FOOOO-BA", false);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("FOOOO", l.getVariant());
        assertEquals("", l.getScript());

        l = fromLanguageTag("en-US-POSIX-P2", false);
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());
        assertEquals("POSIX", l.getVariant());
        assertEquals("", l.getScript());

        l = fromLanguageTag("en-Latn-US-P2", false);
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());
        assertEquals("Latn", l.getScript());

        l = fromLanguageTag("en-f-f", false);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getVariant());
        assertEquals("", l.getScript());

        l = fromLanguageTag("en-f", false);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getVariant());
        assertEquals("", l.getScript());

        l = fromLanguageTag("en-f-fooobaaaz", false);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getVariant());
        assertEquals("", l.getScript());

        l = fromLanguageTag("en-9-baa", false);
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getVariant());
        assertEquals("", l.getScript());
    }

    public void test_Builder_unicodeAttributes() {
        // Adding and removing attributes
        Locale.Builder b = new Locale.Builder();
        b.setLanguage("en");

        // Well formed attribute.
        b.addUnicodeLocaleAttribute("foooo");

        try {
            b.addUnicodeLocaleAttribute("fo");
            fail();
        } catch (IllformedLocaleException ifle) {
        }

        try {
            b.removeUnicodeLocaleAttribute("fo");
            fail();
        } catch (IllformedLocaleException ifle) {
        }

        try {
            b.addUnicodeLocaleAttribute("greaterthaneightchars");
            fail();
        } catch (IllformedLocaleException ifle) {
        }

        try {
            b.removeUnicodeLocaleAttribute("greaterthaneightchars");
            fail();
        } catch (IllformedLocaleException ifle) {
        }

        try {
            b.addUnicodeLocaleAttribute(null);
            fail();
        } catch (NullPointerException npe) {
        }

        try {
            b.removeUnicodeLocaleAttribute(null);
            fail();
        } catch (NullPointerException npe) {
        }

        Locale l = b.build();
        assertEquals("en-u-foooo", l.toLanguageTag());
        assertTrue(l.getUnicodeLocaleAttributes().contains("foooo"));

        b.addUnicodeLocaleAttribute("dAtA");
        l = b.build();
        assertEquals("data-foooo", l.getExtension('u'));
        assertTrue(l.getUnicodeLocaleAttributes().contains("data"));
        assertTrue(l.getUnicodeLocaleAttributes().contains("foooo"));
    }

    public void test_Builder_unicodeKeywords() {
        // Adding and removing attributes
        Locale.Builder b = new Locale.Builder();
        b.setLanguage("en");

        // Key not of length 2.
        try {
            b.setUnicodeLocaleKeyword("k", "fooo");
            fail();
        } catch (IllformedLocaleException ifle) {
        }

        // Value too short
        try {
            b.setUnicodeLocaleKeyword("k", "fo");
            fail();
        } catch (IllformedLocaleException ifle) {
        }

        // Value too long
        try {
            b.setUnicodeLocaleKeyword("k", "foooooooo");
            fail();
        } catch (IllformedLocaleException ifle) {
        }


        // Null should clear the key.
        b.setUnicodeLocaleKeyword("bo", "baaz");
        Locale l = b.build();
        assertEquals("bo-baaz", l.getExtension('u'));
        assertEquals("baaz", l.getUnicodeLocaleType("bo"));

        b = new Locale.Builder();
        b.setUnicodeLocaleKeyword("bo", "baaz");
        b.setUnicodeLocaleKeyword("bo", null);
        l = b.build();
        assertNull(l.getExtension('u'));
        assertNull(l.getUnicodeLocaleType("bo"));

        // When we set attributes, they should show up before extensions.
        b = new Locale.Builder();
        b.addUnicodeLocaleAttribute("fooo");
        b.addUnicodeLocaleAttribute("gooo");
        b.setUnicodeLocaleKeyword("fo", "baz");
        b.setUnicodeLocaleKeyword("ka", "kaz");
        l = b.build();
        assertEquals("fooo-gooo-fo-baz-ka-kaz", l.getExtension('u'));
        assertEquals("baz", l.getUnicodeLocaleType("fo"));
        assertEquals("kaz", l.getUnicodeLocaleType("ka"));
        assertTrue(l.getUnicodeLocaleAttributes().contains("fooo"));
        assertTrue(l.getUnicodeLocaleAttributes().contains("gooo"));
    }

    public void test_multipleExtensions() {
        Locale.Builder b = new Locale.Builder();
        b.setLanguage("en");
        b.addUnicodeLocaleAttribute("attrib");
        b.addUnicodeLocaleAttribute("attrib2");
        b.setExtension('f', "fo-baaz-ga-gaaz");
        b.setExtension('x', "xo-baaz-ga-gaaz");
        b.setExtension('z', "zo-baaz-ga-gaaz");

        Locale l = b.build();
        // Implicitly added because we added unicode locale attributes.
        assertEquals("attrib-attrib2", l.getExtension('u'));
        assertEquals("fo-baaz-ga-gaaz", l.getExtension('f'));
        assertEquals("xo-baaz-ga-gaaz", l.getExtension('x'));
        assertEquals("zo-baaz-ga-gaaz", l.getExtension('z'));
    }

    public void test_immutability() {
        Locale.Builder b = new Locale.Builder();
        b.setExtension('g', "fooo-baaz-baar");
        b.setExtension('u', "foooo-baaar-ba-baaz-ka-kaaz");

        Locale l = b.build();
        try {
            l.getExtensionKeys().add('g');
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        try {
            l.getUnicodeLocaleAttributes().add("fooo");
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void test_lookup_noMatch() {
        // RFC 4647 section 3.4.
        List<LanguageRange> languageRanges = languageRangesOf(
            "zh-Hant-CN-x-private1-private2",
            "zh-Hant-CN-x-private1",
            "zh-Hant-CN",
            "zh-Hant",
            "zh"
        );
        assertNull(Locale.lookup(languageRanges, localesOf("de-DE", "fr-FR", "ja-JP")));
        assertNull(Locale.lookupTag(languageRanges, tagsOf("de-DE", "fr-FR", "ja-JP")));
    }

    /**
     * Tests that lookup returns the tag/locale that matches the highest priority
     * LanguageRange.
     */
    public void test_lookup_order() {
        // RFC 4647 section 3.4.
        List<LanguageRange> languageRanges = languageRangesOf(
            "de-Latn-DE-1996",
            "zh-Hant-CN",
            "de"
        );

        // de would also match, but de-Latn-DE-1997 occurs earlier in the
        // (sorted by descending priority) languageRanges
        assertLookup("de-Latn-DE-1996",
            languageRanges,
            tagsOf("de", "de-Latn-DE-1996"));

        // de-Latn-DE-1996 also includes de-Latn-DE, de-Latn, de; therefore
        // de-Latn-DE is preferred over zh-Hant-CN
        assertLookup("de-Latn-DE",
            languageRanges,
            tagsOf("de", "de-Latn-DE", "de-DE-1996", "zh-Hant-CN"));

        // After reversing the priority list of the LanguageRanges, "de" now has the
        // highest priority.
        assertLookup("de",
            languageRangesOf(
                "de",
                "zh-Hant-CN",
                "de-Latn-DE-1996"
            ),
            tagsOf("de", "de-Latn-DE", "de-DE-1996"));

        // Dropping "de" from the priority list of LanguageRanges false back to de-Latn-DE
        assertLookup("de-Latn-DE",
            languageRangesOf(
                "zh-Hant-CN",
                "de-Latn-DE-1996"
            ),
            tagsOf("de", "de-Latn-DE", "de-DE-1996"));
    }

    public void test_lookup_nullArguments() {
        List<String> tags = tagsOf("de-DE", "de-Latn-DE");
        List<Locale> locales = localesOf(tags);
        List<LanguageRange> languageRanges = languageRangesOf("en-*-US", "de-DE");

        assertThrowsNpe(() -> { Locale.lookup(null, locales); });
        assertThrowsNpe(() -> { Locale.lookup(languageRanges, null); });

        assertThrowsNpe(() -> { Locale.lookupTag(null, tags); });
        assertThrowsNpe(() -> { Locale.lookupTag(languageRanges, null); });
    }

    public void test_toLanguageTag() {
        Locale.Builder b = new Locale.Builder();

        // Empty builder.
        Locale l = b.build();
        assertEquals("und", l.toLanguageTag());

        // Only language.
        b = new Locale.Builder();
        b.setLanguage("en");
        assertEquals("en", b.build().toLanguageTag());

        // Language & Region
        b = new Locale.Builder();
        b.setLanguage("en").setRegion("US");
        assertEquals("en-US", b.build().toLanguageTag());

        // Language & Script
        b = new Locale.Builder();
        b.setLanguage("en").setScript("Latn");
        assertEquals("en-Latn", b.build().toLanguageTag());

        // Language & Variant
        b = new Locale.Builder();
        b.setLanguage("en").setVariant("foooo");
        assertEquals("en-foooo", b.build().toLanguageTag());

        // Language / script & country
        b = new Locale.Builder();
        b.setLanguage("en").setScript("Latn").setRegion("US");
        assertEquals("en-Latn-US", b.build().toLanguageTag());

        // Language / script & variant
        b = new Locale.Builder();
        b.setLanguage("en").setScript("Latn").setVariant("foooo");
        assertEquals("en-Latn-foooo", b.build().toLanguageTag());

        // Language / script / country / variant.
        b = new Locale.Builder();
        b.setLanguage("en").setScript("Latn").setVariant("foooo").setRegion("US");
        assertEquals("en-Latn-US-foooo", b.build().toLanguageTag());

        // Language / extension
        b = new Locale.Builder();
        b.setLanguage("en").setExtension('x', "fooo-baar");
        assertEquals("en-x-fooo-baar", b.build().toLanguageTag());

        // Language & multiple extensions (including unicode).
        b = new Locale.Builder();
        b.setLanguage("en");
        b.addUnicodeLocaleAttribute("attrib");
        b.addUnicodeLocaleAttribute("attrib2");
        b.setExtension('f', "fo-baaz-ga-gaaz");
        b.setExtension('x', "xo-baaz-ga-gaaz");
        b.setExtension('z', "zo-baaz-ga-gaaz");

        l = b.build();
        // Implicitly added because we added unicode locale attributes.
        assertEquals("attrib-attrib2", l.getExtension('u'));
        assertEquals("fo-baaz-ga-gaaz", l.getExtension('f'));
        assertEquals("xo-baaz-ga-gaaz", l.getExtension('x'));
        assertEquals("zo-baaz-ga-gaaz", l.getExtension('z'));

        assertEquals("en-" +
                "f-fo-baaz-ga-gaaz-" +   // extension tags in lexical order
                "u-attrib-attrib2-z-zo-baaz-ga-gaaz-" +  // unicode attribs & keywords in lex order
                "x-xo-baaz-ga-gaaz", // private use extension unmodified.
                l.toLanguageTag());
    }

    public void test_toString() {
        Locale.Builder b = new Locale.Builder();

        // Empty builder.
        Locale l = b.build();
        assertEquals("", l.toString());

        // Only language.
        b = new Locale.Builder();
        b.setLanguage("en");
        assertEquals("en", b.build().toString());

        // Only region
        b = new Locale.Builder();
        b.setRegion("US");
        assertEquals("_US", b.build().toString());

        // Language & Region
        b = new Locale.Builder();
        b.setLanguage("en").setRegion("US");
        assertEquals("en_US", b.build().toString());

        // Language & Script
        b = new Locale.Builder();
        b.setLanguage("en").setScript("Latn");
        assertEquals("en__#Latn", b.build().toString());

        // Language & Variant
        b = new Locale.Builder();
        b.setLanguage("en").setVariant("foooo");
        assertEquals("en__foooo", b.build().toString());

        // Language / script & country
        b = new Locale.Builder();
        b.setLanguage("en").setScript("Latn").setRegion("US");
        assertEquals("en_US_#Latn", b.build().toString());

        // Language / script & variant
        b = new Locale.Builder();
        b.setLanguage("en").setScript("Latn").setVariant("foooo");
        assertEquals("en__foooo_#Latn", b.build().toString());

        // Language / script / country / variant.
        b = new Locale.Builder();
        b.setLanguage("en").setScript("Latn").setVariant("foooo").setRegion("US");
        assertEquals("en_US_foooo_#Latn", b.build().toString());

        // Language / extension
        b = new Locale.Builder();
        b.setLanguage("en").setExtension('x', "fooo-baar");
        assertEquals("en__#x-fooo-baar", b.build().toString());
    }

    // Tests cases where our "guess" for the output size is incorrect.
    //
    // https://b.corp.google.com/issue?id=13414549
    public void test_toLanguageTag_largerTag() {
        Locale posix = new Locale.Builder()
                .setLanguage("en").setRegion("US").setVariant("POSIX")
                .build();
        assertEquals("en-US-POSIX", posix.toLanguageTag());
    }

    // Legacy locales in this test refer to "grandfathered" locales as defined in
    // https://tools.ietf.org/html/bcp47#section-2.2.8
    public void test_forLanguageTag_legacyLocale() {
        // Regular legacy locale.
        Locale gaulish = Locale.forLanguageTag("cel-gaulish");
        assertEquals("xtg", gaulish.getLanguage());
        assertEquals("cel-gaulish", gaulish.getExtension(Locale.PRIVATE_USE_EXTENSION));
        assertEquals("", gaulish.getCountry());
        assertEquals("", gaulish.getScript());
        assertEquals("", gaulish.getVariant());

        // Irregular legacy locale.
        Locale enochian = Locale.forLanguageTag("i-enochian");
        assertEquals("", enochian.getLanguage());
        assertEquals("i-enochian", enochian.getExtension(Locale.PRIVATE_USE_EXTENSION));
        assertEquals("", enochian.getCountry());
        assertEquals("", enochian.getScript());
        assertEquals("", enochian.getVariant());
    }

    // Test case from http://b/16811867
    public void testVariantsCaseSensitive() {
        final Locale locale = new Locale("en", "US", "variant");
        assertEquals("variant", locale.getVariant());
        assertEquals(locale, Locale.forLanguageTag(locale.toLanguageTag()));
    }

    /* Arabic is not supported in J2ObjC.
    public void testArabicDigits() throws Exception {
        // ar-DZ uses latn digits by default, but we can override that.
        Locale ar_DZ = Locale.forLanguageTag("ar-DZ");
        Locale ar_DZ_arab = Locale.forLanguageTag("ar-DZ-u-nu-arab");
        Locale ar_DZ_latn = Locale.forLanguageTag("ar-DZ-u-nu-latn");
        assertEquals('0', new DecimalFormatSymbols(ar_DZ).getZeroDigit());
        assertEquals('\u0660', new DecimalFormatSymbols(ar_DZ_arab).getZeroDigit());
        assertEquals('0', new DecimalFormatSymbols(ar_DZ_latn).getZeroDigit());

        // ar-EG uses arab digits by default, but we can override that.
        Locale ar_EG = Locale.forLanguageTag("ar-EG");
        Locale ar_EG_arab = Locale.forLanguageTag("ar-EG-u-nu-arab");
        Locale ar_EG_latn = Locale.forLanguageTag("ar-EG-u-nu-latn");
        assertEquals('\u0660', new DecimalFormatSymbols(ar_EG).getZeroDigit());
        assertEquals('\u0660', new DecimalFormatSymbols(ar_EG_arab).getZeroDigit());
        assertEquals('0', new DecimalFormatSymbols(ar_EG_latn).getZeroDigit());
    }*/

    /*public void testDefaultLocale() throws Exception {
        final String userLanguage = System.getProperty("user.language", "");
        final String userRegion = System.getProperty("user.region", "");
        final String userLocale = System.getProperty("user.locale", "");
        try {
            // Assert that user.locale gets priority.
            System.setUnchangeableSystemProperty("user.locale", "de-DE");
            System.setUnchangeableSystemProperty("user.language", "en");
            System.setUnchangeableSystemProperty("user.region", "US");

            Locale l = Locale.initDefault();
            assertEquals("de", l.getLanguage());
            assertEquals("DE", l.getCountry());

            // Assert that it's parsed as a full language tag.
            System.setUnchangeableSystemProperty("user.locale", "de-Latn-DE");
            System.setUnchangeableSystemProperty("user.language", "en");
            System.setUnchangeableSystemProperty("user.region", "US");

            l = Locale.initDefault();
            assertEquals("de", l.getLanguage());
            assertEquals("DE", l.getCountry());
            assertEquals("Latn", l.getScript());

            // Assert that we don't end up with a null default locale or an exception.
            System.setUnchangeableSystemProperty("user.locale", "toolonglang-Latn-DE");

            // Note: pre-enso Locale#fromLanguageTag parser was more error-tolerant
            // then the current one. Result of bad language part of tag from line above
            // will be an empty Locale object.
            l = Locale.initDefault();
            assertEquals("", l.getLanguage());
            assertEquals("", l.getCountry());
        } finally {
            System.setUnchangeableSystemProperty("user.language", userLanguage);
            System.setUnchangeableSystemProperty("user.region", userRegion);
            System.setUnchangeableSystemProperty("user.locale", userLocale);
        }
    }*/

    // http://b/20252611
    public void testLegacyLocalesWithExtensions() {
        Locale ja_JP_JP = new Locale("ja", "JP", "JP");
        assertEquals("ca-japanese", ja_JP_JP.getExtension(Locale.UNICODE_LOCALE_EXTENSION));
        assertEquals("japanese", ja_JP_JP.getUnicodeLocaleType("ca"));

        Locale th_TH_TH = new Locale("th", "TH", "TH");
        assertEquals("nu-thai", th_TH_TH.getExtension(Locale.UNICODE_LOCALE_EXTENSION));
        assertEquals("thai", th_TH_TH.getUnicodeLocaleType("nu"));
    }

    // http://b/20252611
    public void testLowerCaseExtensionKeys() {
        // We must lowercase extension keys in forLanguageTag..
        Locale ar_EG = Locale.forLanguageTag("ar-EG-U-nu-arab");
        assertEquals("nu-arab", ar_EG.getExtension(Locale.UNICODE_LOCALE_EXTENSION));
        assertEquals("ar-EG-u-nu-arab", ar_EG.toLanguageTag());

        // ... and in builders.
        Locale.Builder b = new Locale.Builder();
        b.setLanguage("ar");
        b.setRegion("EG");
        b.setExtension('U', "nu-arab");
        assertEquals("ar-EG-u-nu-arab", b.build().toLanguageTag());

        // Corollary : extension keys are case insensitive.
        b = new Locale.Builder();
        b.setLanguage("ar");
        b.setRegion("EG");
        b.setExtension('U', "nu-arab");
        b.setExtension('u', "nu-thai");
        assertEquals("ar-EG-u-nu-thai", b.build().toLanguageTag());
    }

    // http://b/26387905
    public void test_SerializationBug_26387905() throws Exception {
        try (ObjectInputStream oinput = new ObjectInputStream(getClass()
                .getResource("/serialization/org/apache/harmony/tests/java/util/Locale_Bug_26387905.ser")
                .openStream())) {
            Locale l = (Locale) oinput.readObject();
        }
    }

    public void test_setDefault_withCategory() {
        final Locale defaultLocale = Locale.getDefault();
        try {
            // Establish a baseline for the checks further down
            Locale.setDefault(Locale.US);
            assertEquals(Locale.US, Locale.getDefault(Locale.Category.FORMAT));
            assertEquals(Locale.US, Locale.getDefault(Locale.Category.DISPLAY));
            assertEquals(Locale.US, Locale.getDefault());

            Locale.setDefault(Locale.Category.FORMAT, Locale.UK);
            assertEquals(Locale.UK, Locale.getDefault(Locale.Category.FORMAT));
            assertEquals(Locale.US, Locale.getDefault(Locale.Category.DISPLAY));
            assertEquals(Locale.US, Locale.getDefault());

            Locale.setDefault(Locale.Category.DISPLAY, Locale.CANADA);
            assertEquals(Locale.UK, Locale.getDefault(Locale.Category.FORMAT));
            assertEquals(Locale.CANADA, Locale.getDefault(Locale.Category.DISPLAY));
            assertEquals(Locale.US, Locale.getDefault());

            Locale.setDefault(Locale.FRANCE);
            assertEquals(Locale.FRANCE, Locale.getDefault(Locale.Category.FORMAT));
            assertEquals(Locale.FRANCE, Locale.getDefault(Locale.Category.DISPLAY));
            assertEquals(Locale.FRANCE, Locale.getDefault());

            // Check that setDefault(Locale) sets all three defaults
            Locale.setDefault(Locale.US);
            assertEquals(Locale.US, Locale.getDefault(Locale.Category.FORMAT));
            assertEquals(Locale.US, Locale.getDefault(Locale.Category.DISPLAY));
            assertEquals(Locale.US, Locale.getDefault());
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    private static List<Locale> localesOf(String... languageTags) {
        return localesOf(tagsOf(languageTags));
    }

    private static List<Locale> localesOf(List<String> languageTags) {
        List<Locale> result = new ArrayList<>();
        for (String languageTag : languageTags) {
            result.add(Locale.forLanguageTag(languageTag));
        }
        return Collections.unmodifiableList(result);
    }

    private static List<String> tagsOf(String... tags) {
        List<String> result = new ArrayList<>();
        for (String tag : tags) {
            result.add(tag.toLowerCase());
        }
        return Collections.unmodifiableList(result);
    }

    private static List<LanguageRange> languageRangesOf(String... languageRanges) {
        List<LanguageRange> result = new ArrayList<>();
        for (String languageRange : languageRanges) {
            result.add(new LanguageRange(languageRange));
        }
        return Collections.unmodifiableList(result);
    }

    private static void assertFilter(List<String> filteredTags, List<LanguageRange> languageRanges,
        List<String> tags) {
        assertEquals(filteredTags, Locale.filterTags(languageRanges, tags));

        List<Locale> locales = localesOf(tags);
        List<Locale> filteredLocales = localesOf(filteredTags);
        assertEquals(filteredLocales, Locale.filter(languageRanges, locales));
    }

    private static void assertFilter(List<String> filteredTags, List<LanguageRange> languageRanges,
        List<String> tags, Locale.FilteringMode filteringMode) {
        assertEquals(filteredTags,
            Locale.filterTags(languageRanges, tags, filteringMode));

        List<Locale> locales = localesOf(tags);
        List<Locale> filteredLocales = localesOf(filteredTags);
        assertEquals(filteredLocales, Locale.filter(languageRanges, locales, filteringMode));
    }

    private static void assertThrowsNpe(Runnable runnable) {
        try {
            runnable.run();
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    private static void assertLookup(String expectedTag, List<LanguageRange> languageRanges,
        List<String> tags) {
        assertEquals(expectedTag.toLowerCase(), Locale.lookupTag(languageRanges, tags));

        assertEquals(Locale.forLanguageTag(expectedTag),
            Locale.lookup(languageRanges, localesOf(tags)));
    }

}
