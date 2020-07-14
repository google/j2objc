/*
 * Copyright (C) 2009 The Android Open Source Project
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

package libcore.libcore.icu;

//import java.text.BreakIterator;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;
import libcore.icu.ICU;

public class ICUTest extends junit.framework.TestCase {
  public void test_getISOLanguages() throws Exception {
    // Check that corrupting our array doesn't affect other callers.
    assertNotNull(ICU.getISOLanguages()[0]);
    ICU.getISOLanguages()[0] = null;
    assertNotNull(ICU.getISOLanguages()[0]);
  }

  public void test_getISOCountries() throws Exception {
    // Check that corrupting our array doesn't affect other callers.
    assertNotNull(ICU.getISOCountries()[0]);
    ICU.getISOCountries()[0] = null;
    assertNotNull(ICU.getISOCountries()[0]);
  }

  public void test_getAvailableLocales() throws Exception {
    // Check that corrupting our array doesn't affect other callers.
    assertNotNull(ICU.getAvailableLocales()[0]);
    ICU.getAvailableLocales()[0] = null;
    assertNotNull(ICU.getAvailableLocales()[0]);
  }

  /* ICU.getBestDateTimePattern is currently unavailable in J2ObjC.
  public void test_getBestDateTimePattern() throws Exception {
    assertEquals("d MMMM", ICU.getBestDateTimePattern("MMMMd", new Locale("ca", "ES")));
    assertEquals("d 'de' MMMM", ICU.getBestDateTimePattern("MMMMd", new Locale("es", "ES")));
    assertEquals("d. MMMM", ICU.getBestDateTimePattern("MMMMd", new Locale("de", "CH")));
    assertEquals("MMMM d", ICU.getBestDateTimePattern("MMMMd", new Locale("en", "US")));
    assertEquals("d LLLL", ICU.getBestDateTimePattern("MMMMd", new Locale("fa", "IR")));
    assertEquals("M月d日", ICU.getBestDateTimePattern("MMMMd", new Locale("ja", "JP")));
  }*/

  public void test_localeFromString() throws Exception {
    // localeFromString is pretty lenient. Some of these can't be round-tripped
    // through Locale.toString.
    assertEquals(Locale.ENGLISH, ICU.localeFromIcuLocaleId("en"));
    assertEquals(Locale.ENGLISH, ICU.localeFromIcuLocaleId("en_"));
    assertEquals(Locale.ENGLISH, ICU.localeFromIcuLocaleId("en__"));
    assertEquals(Locale.US, ICU.localeFromIcuLocaleId("en_US"));
    assertEquals(Locale.US, ICU.localeFromIcuLocaleId("en_US_"));
    assertEquals(new Locale("", "US", ""), ICU.localeFromIcuLocaleId("_US"));
    assertEquals(new Locale("", "US", ""), ICU.localeFromIcuLocaleId("_US_"));
    assertEquals(new Locale("", "", "POSIX"), ICU.localeFromIcuLocaleId("__POSIX"));
    assertEquals(new Locale("aa", "BB", "CCCCC"), ICU.localeFromIcuLocaleId("aa_BB_CCCCC"));
  }

  /* ICU.addLikelySubtags is currently unavailable in J2ObjC.
  public void test_getScript_addLikelySubtags() throws Exception {
    assertEquals("Latn", ICU.getScript(ICU.addLikelySubtags("en_US")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("he")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("he_IL")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("iw")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("iw_IL")));
  }

  public void test_addLikelySubtags() throws Exception {
    assertEquals("Latn", ICU.addLikelySubtags(new Locale("en", "US")).getScript());
    assertEquals("Hebr", ICU.addLikelySubtags(new Locale("he")).getScript());
    assertEquals("Hebr", ICU.addLikelySubtags(new Locale("he", "IL")).getScript());
    assertEquals("Hebr", ICU.addLikelySubtags(new Locale("iw")).getScript());
    assertEquals("Hebr", ICU.addLikelySubtags(new Locale("iw", "IL")).getScript());
  }*/

  /* ICU.getBestDateTimePattern is currently unavailable in J2ObjC.
  private String best(Locale l, String skeleton) {
    return ICU.getBestDateTimePattern(skeleton, l);
  }

  public void test_getDateFormatOrder() throws Exception {
    // lv and fa use differing orders depending on whether you're using numeric or textual months.
    Locale lv = new Locale("lv");
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(lv, "yyyy-M-dd"))));
    assertEquals("[y, d, M]", Arrays.toString(ICU.getDateFormatOrder(best(lv, "yyyy-MMM-dd"))));
    assertEquals("[d, M, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(lv, "MMM-dd"))));
    Locale fa = new Locale("fa");
    assertEquals("[y, M, d]", Arrays.toString(ICU.getDateFormatOrder(best(fa, "yyyy-M-dd"))));
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(fa, "yyyy-MMM-dd"))));
    assertEquals("[d, M, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(fa, "MMM-dd"))));

    // English differs on each side of the Atlantic.
    Locale en_US = Locale.US;
    assertEquals("[M, d, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_US, "yyyy-M-dd"))));
    assertEquals("[M, d, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_US, "yyyy-MMM-dd"))));
    assertEquals("[M, d, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(en_US, "MMM-dd"))));
    Locale en_GB = Locale.UK;
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_GB, "yyyy-M-dd"))));
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_GB, "yyyy-MMM-dd"))));
    assertEquals("[d, M, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(en_GB, "MMM-dd"))));

    assertEquals("[y, M, d]", Arrays.toString(ICU.getDateFormatOrder("yyyy - 'why' '' 'ddd' MMM-dd")));

    try {
      ICU.getDateFormatOrder("the quick brown fox jumped over the lazy dog");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      ICU.getDateFormatOrder("'");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      ICU.getDateFormatOrder("yyyy'");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      ICU.getDateFormatOrder("yyyy'MMM");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }*/

  /* java.text.BreakIterator is unavailable in J2ObjC.
  public void testScriptsPassedToIcu() throws Exception {
    Locale sr_Cyrl_BA = Locale.forLanguageTag("sr-Cyrl-BA");
    Locale sr_Cyrl_ME = Locale.forLanguageTag("sr-Cyrl-ME");
    Locale sr_Latn_BA = Locale.forLanguageTag("sr-Latn-BA");
    Locale sr_Latn_ME = Locale.forLanguageTag("sr-Latn-ME");

    assertEquals("sr_BA_#Cyrl", sr_Cyrl_BA.toString());
    assertEquals("Cyrl",        sr_Cyrl_BA.getScript());

    assertEquals("sr_ME_#Cyrl", sr_Cyrl_ME.toString());
    assertEquals("Cyrl",        sr_Cyrl_ME.getScript());

    assertEquals("sr_BA_#Latn", sr_Latn_BA.toString());
    assertEquals("Latn",        sr_Latn_BA.getScript());

    assertEquals("sr_ME_#Latn", sr_Latn_ME.toString());
    assertEquals("Latn",        sr_Latn_ME.getScript());

    assertEquals("српски",              sr_Cyrl_BA.getDisplayLanguage(sr_Cyrl_BA));
    assertEquals("Босна и Херцеговина", sr_Cyrl_BA.getDisplayCountry(sr_Cyrl_BA));
    assertEquals("ћирилица",            sr_Cyrl_BA.getDisplayScript(sr_Cyrl_BA));
    assertEquals("",                    sr_Cyrl_BA.getDisplayVariant(sr_Cyrl_BA));

    assertEquals("српски",    sr_Cyrl_ME.getDisplayLanguage(sr_Cyrl_ME));
    assertEquals("Црна Гора", sr_Cyrl_ME.getDisplayCountry(sr_Cyrl_ME));
    assertEquals("ћирилица",  sr_Cyrl_ME.getDisplayScript(sr_Cyrl_ME));
    assertEquals("",          sr_Cyrl_ME.getDisplayVariant(sr_Cyrl_ME));

    assertEquals("srpski",              sr_Latn_BA.getDisplayLanguage(sr_Latn_BA));
    assertEquals("Bosna i Hercegovina", sr_Latn_BA.getDisplayCountry(sr_Latn_BA));
    assertEquals("latinica",            sr_Latn_BA.getDisplayScript(sr_Latn_BA));
    assertEquals("",                    sr_Latn_BA.getDisplayVariant(sr_Latn_BA));

    assertEquals("srpski",    sr_Latn_ME.getDisplayLanguage(sr_Latn_ME));
    assertEquals("Crna Gora", sr_Latn_ME.getDisplayCountry(sr_Latn_ME));
    assertEquals("latinica",  sr_Latn_ME.getDisplayScript(sr_Latn_ME));
    assertEquals("",          sr_Latn_ME.getDisplayVariant(sr_Latn_ME));

    assertEquals("BIH", sr_Cyrl_BA.getISO3Country());
    assertEquals("srp", sr_Cyrl_BA.getISO3Language());
    assertEquals("MNE", sr_Cyrl_ME.getISO3Country());
    assertEquals("srp", sr_Cyrl_ME.getISO3Language());
    assertEquals("BIH", sr_Latn_BA.getISO3Country());
    assertEquals("srp", sr_Latn_BA.getISO3Language());
    assertEquals("MNE", sr_Latn_ME.getISO3Country());
    assertEquals("srp", sr_Latn_ME.getISO3Language());

    BreakIterator.getCharacterInstance(sr_Cyrl_BA);
    BreakIterator.getCharacterInstance(sr_Cyrl_ME);
    BreakIterator.getCharacterInstance(sr_Latn_BA);
    BreakIterator.getCharacterInstance(sr_Latn_ME);

    BreakIterator.getLineInstance(sr_Cyrl_BA);
    BreakIterator.getLineInstance(sr_Cyrl_ME);
    BreakIterator.getLineInstance(sr_Latn_BA);
    BreakIterator.getLineInstance(sr_Latn_ME);

    BreakIterator.getSentenceInstance(sr_Cyrl_BA);
    BreakIterator.getSentenceInstance(sr_Cyrl_ME);
    BreakIterator.getSentenceInstance(sr_Latn_BA);
    BreakIterator.getSentenceInstance(sr_Latn_ME);

    BreakIterator.getWordInstance(sr_Cyrl_BA);
    BreakIterator.getWordInstance(sr_Cyrl_ME);
    BreakIterator.getWordInstance(sr_Latn_BA);
    BreakIterator.getWordInstance(sr_Latn_ME);

    Collator.getInstance(sr_Cyrl_BA);
    Collator.getInstance(sr_Cyrl_ME);
    Collator.getInstance(sr_Latn_BA);
    Collator.getInstance(sr_Latn_ME);

    Locale l = Locale.forLanguageTag("de-u-co-phonebk-kf-upper-kn");
    assertEquals("de__#u-co-phonebk-kf-upper-kn", l.toString());
    assertEquals("de-u-co-phonebk-kf-upper-kn", l.toLanguageTag());

    Collator c = Collator.getInstance(l);
    assertTrue(c.compare("2", "11") < 0);
    assertTrue(c.compare("11", "ae") < 0);
    assertTrue(c.compare("ae", "Ä") < 0);
    assertTrue(c.compare("Ä", "ä") < 0);
    assertTrue(c.compare("ä", "AF") < 0);
    assertTrue(c.compare("AF", "af") < 0);
  }*/

  // Test for the behavior of currency symbol lookup when an unrecognized locale has been set as the
  // default.
  /* ICU.getDefaultLocale is currently unavailable in J2ObjC.
  public void testIcuDefaultAffectsCurrencySymbol() {
    // A locale that is not going to be recognized by ICU and should fallback to "root" for the
    // currency symbol.
    final Locale unrecognizedLocale = new Locale("xy", "KR");

    // A known locale with a relatively stable representation for its currency symbol.
    final Locale enUsLocale = new Locale("en", "US");
    final String usDollar = "USD";

    String initialDefaultLocale = ICU.getDefaultLocale();
    try {
      // Confirm the "$" symbol for USD in en-US.
      assertEquals("$", ICU.getCurrencySymbol(enUsLocale, usDollar));

      // Set the default so this will be used as fallback for the unrecognized locale symbol lookup.
      ICU.setDefaultLocale(enUsLocale.toLanguageTag());

      // Demonstrate the USD symbol is reported as "$" for the unrecognized locale (which is using
      // the default).
      assertEquals("$", ICU.getCurrencySymbol(unrecognizedLocale, usDollar));

      // Change the default.
      ICU.setDefaultLocale(unrecognizedLocale.toLanguageTag());

      String currencySymbolAfterDefaultChange = ICU.getCurrencySymbol(unrecognizedLocale, usDollar);
      // "$US" is the value from root. With an unrecognized locale argument, and an unrecognized
      // locale as the default, ICU has returns the value in root.
      assertEquals("US$", currencySymbolAfterDefaultChange);
    } finally {
      ICU.setDefaultLocale(initialDefaultLocale);
    }
  }*/

  /** Confirms that ICU agrees with the rest of libcore about the version of the TZ data in use. */
  /* ICU.getTZDataVersion is currently unavailable in J2ObjC.
  public void testTimeZoneDataVersion() {
    String icu4cTzVersion = ICU.getTZDataVersion();
    String zoneInfoTzVersion = ZoneInfoDB.getInstance().getVersion();
    assertEquals(icu4cTzVersion, zoneInfoTzVersion);

    String icu4jTzVersion = android.icu.util.TimeZone.getTZDataVersion();
    assertEquals(icu4jTzVersion, zoneInfoTzVersion);
  }*/
}
