/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2004-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.dev.test.format;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.BreakIterator;
import android.icu.text.Collator;
import android.icu.text.DateFormat;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.BuddhistCalendar;
import android.icu.util.Calendar;
import android.icu.util.Currency;
import android.icu.util.GlobalizationPreferences;
import android.icu.util.GregorianCalendar;
import android.icu.util.IslamicCalendar;
import android.icu.util.JapaneseCalendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;



public class GlobalizationPreferencesTest extends TestFmwk {
    @Test
    public void TestDefault() {
        GlobalizationPreferences gp = new GlobalizationPreferences();
        ULocale defLocale = new ULocale("en_US");
        ULocale defFallbackLocale = new ULocale("en");

        if (!defLocale.equals(ULocale.getDefault())) {
            // Locale.US is always used as the default locale in the test environment
            // If not, some test cases will fail...
            errln("FAIL: The default locale of the test environment must be en_US");
        }
        
        logln("Default locale: " + defLocale.toString());

        // First locale is en_US
        ULocale gpLocale0 = gp.getLocale(0);
        logln("Primary locale: " + gpLocale0.toString());
        if (!gpLocale0.equals(defLocale)) {
            errln("FAIL: The primary locale is not en_US");
        }

        // Second locale is en
        ULocale gpLocale1 = gp.getLocale(1);
        logln("Secondary locale: " + gpLocale1.toString());
        if (!gpLocale1.equals(defFallbackLocale)) {
            errln("FAIL: The secondary locale is not en");
        }

        // Third locale is null
        ULocale gpLocale2 = gp.getLocale(2);
        if (gpLocale2 != null) {
            errln("FAIL: Number of locales must be 2");
        }
        
        // Calendar locale
        Calendar cal = gp.getCalendar();
        ULocale calLocale = cal.getLocale(ULocale.VALID_LOCALE);
        logln("Calendar locale: " + calLocale.toString());
        if (!calLocale.equals(defLocale)) {
            errln("FAIL: The calendar locale must match with the default JVM locale");
        }

        // Collator locale
        Collator coll = gp.getCollator();
        ULocale collLocale = coll.getLocale(ULocale.VALID_LOCALE);
        logln("Collator locale: " + collLocale.toString());
        if (!collLocale.equals(defLocale)) {
            errln("FAIL: The collator locale must match with the default JVM locale");
        }

        // BreakIterator locale
        BreakIterator brk = gp.getBreakIterator(GlobalizationPreferences.BI_CHARACTER);
        ULocale brkLocale = brk.getLocale(ULocale.VALID_LOCALE);
        logln("BreakIterator locale: " + brkLocale.toString());
        if (!brkLocale.equals(defLocale)) {
            errln("FAIL: The break iterator locale must match with the default JVM locale");
        }

        /* Skip - Bug#5209
        // DateFormat locale
        DateFormat df = gp.getDateFormat(GlobalizationPreferences.DF_FULL, GlobalizationPreferences.DF_NONE);
        ULocale dfLocale = df.getLocale(ULocale.VALID_LOCALE);
        logln("DateFormat locale: " + dfLocale.toString());
        if (!dfLocale.equals(defLocale)) {
            errln("FAIL: The date format locale must match with the default JVM locale");
        }
        */

        // NumberFormat locale
        NumberFormat nf = gp.getNumberFormat(GlobalizationPreferences.NF_NUMBER);
        ULocale nfLocale = nf.getLocale(ULocale.VALID_LOCALE);
        logln("NumberFormat locale: " + nfLocale.toString());
        if (!nfLocale.equals(defLocale)) {
            errln("FAIL: The number format locale must match with the default JVM locale");
        }
    }

    @Test
    public void TestFreezable() {
        logln("Create a new GlobalizationPreference object");
        GlobalizationPreferences gp = new GlobalizationPreferences();
        if (gp.isFrozen()) {
            errln("FAIL: This object is not yet frozen");
        }        

        logln("Call reset()");
        boolean bSet = true;
        try {
            gp.reset();
        } catch (UnsupportedOperationException uoe) {
            bSet = false;
        }
        if (!bSet) {
            errln("FAIL: reset() must not throw an exception before frozen");
        }

        // Freeze the object
        logln("Freeze the object");
        gp.freeze();
        if (!gp.isFrozen()) {
            errln("FAIL: This object is already fronzen");
        }

        // reset()
        logln("Call reset() after frozen");
        bSet = true;
        try {
            gp.reset();
        } catch (UnsupportedOperationException uoe) {
            bSet = false;
        }
        if (bSet) {
            errln("FAIL: reset() must be blocked after frozen");
        }

        // setLocales(ULocale[])
        logln("Call setLocales(ULocale[]) after frozen");
        bSet = true;
        try {
            gp.setLocales(new ULocale[] {new ULocale("fr_FR")});
        } catch (UnsupportedOperationException uoe) {
            bSet = false;
        }
        if (bSet) {
            errln("FAIL: setLocales(ULocale[]) must be blocked after frozen");
        }

        // setLocales(ULocale[])
        logln("Call setLocales(List) after frozen");
        bSet = true;
        ArrayList list = new ArrayList(1);
        list.add(new ULocale("fr_FR"));
        try {
            gp.setLocales(list);
        } catch (UnsupportedOperationException uoe) {
            bSet = false;
        }
        if (bSet) {
            errln("FAIL: setLocales(List) must be blocked after frozen");
        }

        // setLocales(String)
        logln("Call setLocales(String) after frozen");
        bSet = true;
        try {
            gp.setLocales("pt-BR,es;q=0.7");
        } catch (UnsupportedOperationException uoe) {
            bSet = false;
        }
        if (bSet) {
            errln("FAIL: setLocales(String) must be blocked after frozen");
        }

        // setLocale(ULocale)
        logln("Call setLocale(ULocale) after frozen");
        bSet = true;
        try {
            gp.setLocale(new ULocale("fi_FI"));
        } catch (UnsupportedOperationException uoe) {
            bSet = false;
        }
        if (bSet) {
            errln("FAIL: setLocale(ULocale) must be blocked after frozen");
        }
        
        // setTerritory(String)
        logln("Call setTerritory(String) after frozen");
        bSet = true;
        try {
            gp.setTerritory("AU");
        } catch (UnsupportedOperationException uoe) {
            bSet = false;
        }
        if (bSet) {
            errln("FAIL: setTerritory(String) must be blocked after frozen");
        }

        // Modifiable clone
        logln("Create a modifiable clone");
        GlobalizationPreferences gp1 = (GlobalizationPreferences)gp.cloneAsThawed();

        if (gp1.isFrozen()) {
            errln("FAIL: The object returned by cloneAsThawed() must not be frozen yet");
        }        

        // setLocale(ULocale)
        logln("Call setLocale(ULocale) of the modifiable clone");
        bSet = true;
        try {
            gp1.setLocale(new ULocale("fr_FR"));
        } catch (UnsupportedOperationException uoe) {
            bSet = false;
        }
        if (!bSet) {
            errln("FAIL: setLocales(ULocale) must not throw an exception before frozen");
        }        
    }

    static String[][] INPUT_LOCALEIDS = {
        {"en_US"},
        {"fr_CA", "fr"},
        {"fr", "fr_CA"},
        {"es", "fr", "en_US"},
        {"zh_CN", "zh_Hans", "zh_Hans_CN"},
        {"en_US_123"},
        {"es_US", "es"},
        {"de_DE", "es", "fr_FR"},
    };

    static String[] ACCEPT_LANGUAGES = {
        "en-US",
        "fr-CA,fr;q=0.5",
        "fr_CA;q=0.5,fr",
        "es,fr;q=0.76,en_US;q=0.75",
        "zh-CN,zh-Hans;q=0.5,zh-Hans-CN;q=0.1",
        "en-US-123",
        "  es\t; q   =0.5 \t, es-US ;q   =1",
        "fr-FR; q=0.5, de-DE, es",
    };

    static String[][] RESULTS_LOCALEIDS = {
        {"en_US", "en"},
        {"fr_CA", "fr"},
        {"fr_CA", "fr"},
        {"es", "fr", "en_US", "en"},
        {"zh_Hans_CN", "zh_CN", "zh_Hans", "zh"},
        {"en_US_123", "en_US", "en"},
        {"es_US", "es"},
        {"de_DE", "de", "es", "fr_FR", "fr"},
    };

    @Test
    public void TestSetLocales() {
        GlobalizationPreferences gp = new GlobalizationPreferences();

        // setLocales(List)
        for (int i = 0; i < INPUT_LOCALEIDS.length; i++) {
            String[] localeStrings = INPUT_LOCALEIDS[i];
            ArrayList locales = new ArrayList();
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < localeStrings.length; j++) {
                locales.add(new ULocale(localeStrings[j]));
                if (j != 0) {
                    sb.append(", ");
                }
                sb.append(localeStrings[j]);
            }
            logln("Input locales: " + sb.toString());
            
            gp.reset();
            gp.setLocales(locales);

            List resultLocales = gp.getLocales();
            if (resultLocales.size() != RESULTS_LOCALEIDS[i].length) {
                errln("FAIL: Number of locales mismatch - GP:" + resultLocales.size()
                        + " Expected:" + RESULTS_LOCALEIDS[i].length);
            } else {
                
                for (int j = 0; j < RESULTS_LOCALEIDS[i].length; j++) {
                    ULocale loc = gp.getLocale(j);
                    logln("Locale[" + j + "]: " + loc.toString());
                    if (!gp.getLocale(j).toString().equals(RESULTS_LOCALEIDS[i][j])) {
                        errln("FAIL: Locale index(" + j + ") does not match - GP:" + loc.toString()
                                + " Expected:" + RESULTS_LOCALEIDS[i][j]);
                    }
                }    
            }
        }
        
        // setLocales(ULocale[])
        for (int i = 0; i < INPUT_LOCALEIDS.length; i++) {
            String[] localeStrings = INPUT_LOCALEIDS[i];
            ULocale[] localeArray = new ULocale[INPUT_LOCALEIDS[i].length];
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < localeStrings.length; j++) {
                localeArray[j] = new ULocale(localeStrings[j]);
                if (j != 0) {
                    sb.append(", ");
                }
                sb.append(localeStrings[j]);
            }
            logln("Input locales: " + sb.toString());
            
            gp.reset();
            gp.setLocales(localeArray);

            List resultLocales = gp.getLocales();
            if (resultLocales.size() != RESULTS_LOCALEIDS[i].length) {
                errln("FAIL: Number of locales mismatch - GP:" + resultLocales.size()
                        + " Expected:" + RESULTS_LOCALEIDS[i].length);
            } else {
                
                for (int j = 0; j < RESULTS_LOCALEIDS[i].length; j++) {
                    ULocale loc = gp.getLocale(j);
                    logln("Locale[" + j + "]: " + loc.toString());
                    if (!gp.getLocale(j).toString().equals(RESULTS_LOCALEIDS[i][j])) {
                        errln("FAIL: Locale index(" + j + ") does not match - GP:" + loc.toString()
                                + " Expected:" + RESULTS_LOCALEIDS[i][j]);
                    }
                }    
            }
        }

        // setLocales(String)
        for (int i = 0; i < ACCEPT_LANGUAGES.length; i++) {
            String acceptLanguage = ACCEPT_LANGUAGES[i];
            logln("Accept language: " + acceptLanguage);
            
            gp.reset();
            gp.setLocales(acceptLanguage);

            List resultLocales = gp.getLocales();
            if (resultLocales.size() != RESULTS_LOCALEIDS[i].length) {
                errln("FAIL: Number of locales mismatch - GP:" + resultLocales.size()
                        + " Expected:" + RESULTS_LOCALEIDS[i].length);
            } else {
                
                for (int j = 0; j < RESULTS_LOCALEIDS[i].length; j++) {
                    ULocale loc = gp.getLocale(j);
                    logln("Locale[" + j + "]: " + loc.toString());
                    if (!gp.getLocale(j).toString().equals(RESULTS_LOCALEIDS[i][j])) {
                        errln("FAIL: Locale index(" + j + ") does not match - GP:" + loc.toString()
                                + " Expected:" + RESULTS_LOCALEIDS[i][j]);
                    }
                }    
            }
        }


        // accept-language without q-value
        logln("Set accept-language - de,de-AT");
        gp.setLocales("de,de-AT");
        if (!gp.getLocale(0).toString().equals("de_AT")) {
            errln("FAIL: getLocale(0) returns " + gp.getLocale(0).toString() + " Expected: de_AT");
        }
        
        // Invalid accept-language
        logln("Set locale - ko_KR");
        gp.setLocale(new ULocale("ko_KR"));
        boolean bException = false;
        try {
            logln("Set invlaid accept-language - ko=100");
            gp.setLocales("ko=100");
        } catch (IllegalArgumentException iae) {
            logln("IllegalArgumentException was thrown");
            bException = true;
        }
        if (!bException) {
            errln("FAIL: IllegalArgumentException was not thrown for illegal accept-language - ko=100");
        }
        if (!gp.getLocale(0).toString().equals("ko_KR")) {
            errln("FAIL: Previous valid locale list had gone");
        }
    }

    @Test
    public void TestResourceBundle() {
        String baseName = "android.icu.dev.data.resources.TestDataElements";
        ResourceBundle rb;

        logln("Get a resource bundle " + baseName + 
                " using GlobalizationPreferences initialized by locales - en_GB, en_US");
        GlobalizationPreferences gp = new GlobalizationPreferences();
        ULocale[] locales = new ULocale[2];
        locales[0] = new ULocale("en_GB");
        locales[1] = new ULocale("en_US");
        gp.setLocales(locales);

        try {
            rb = gp.getResourceBundle(baseName, Thread.currentThread().getContextClassLoader());
            String str = rb.getString("from_en_US");
            if (!str.equals("This data comes from en_US")) {
                errln("FAIL: from_en_US is not from en_US bundle");
            }
        } catch (MissingResourceException mre) {
            errln("FAIL: Missing resouces");
        }

        gp.reset();

        logln("Get a resource bundle " + baseName + 
        " using GlobalizationPreferences initialized by locales - ja, en_US_California");

        locales = new ULocale[2];
        locales[0] = new ULocale("ja");
        locales[1] = new ULocale("en_US_California");
        gp.setLocales(locales);
        
        try {
            rb = gp.getResourceBundle(baseName, Thread.currentThread().getContextClassLoader());
            String str = rb.getString("from_en_US");
            if (!str.equals("This data comes from en_US")) {
                errln("FAIL: from_en_US is not from en_US bundle");
            }
        } catch (MissingResourceException mre) {
            errln("FAIL: Missing resouces");
        }

        logln("Get a resource bundle which does not exist");
        boolean bException = false;
        try {
            rb = gp.getResourceBundle("foo.bar.XXX", Thread.currentThread().getContextClassLoader());
        } catch (MissingResourceException mre) {
            logln("Missing resource exception for getting resource bundle - foo.bar.XXX");
            bException = true;
        }
        if (!bException) {
            errln("FAIL: MissingResourceException must be thrown for RB - foo.bar.XXX");
        }
    }

    @Test
    public void TestTerritory() {
        GlobalizationPreferences gp = new GlobalizationPreferences();

        // Territory for unsupported language locale
        logln("Set locale - ang");
        gp.setLocale(new ULocale("ang"));
        String territory = gp.getTerritory();
        if (!territory.equals("US")) {
            errln("FAIL: Territory is " + territory + " - Expected: US");
        }

        // Territory for language only locale "fr"
        logln("Set locale - fr");
        gp.setLocale(new ULocale("fr"));
        territory = gp.getTerritory();
        if (!territory.equals("FR")) {
            errln("FAIL: Territory is " + territory + " - Expected: FR");
        }


        // Set explicity territory
        logln("Set explicit territory - CA");
        gp.setTerritory("CA");
        territory = gp.getTerritory();
        if (!territory.equals("CA")) {
            errln("FAIL: Territory is " + territory + " - Expected: CA");
        }

        // Freeze
        logln("Freeze this object");
        gp.freeze();
        
        boolean bFrozen = false;
        try {
            gp.setTerritory("FR");
        } catch (UnsupportedOperationException uoe) {
            logln("setTerritory is blocked");
            bFrozen = true;
        }
        if (!bFrozen) {
            errln("FAIL: setTerritory must be blocked after frozen");
        }
        territory = gp.getTerritory();
        if (!territory.equals("CA")) {
            errln("FAIL: Territory is not CA");
        }

        // Safe clone
        GlobalizationPreferences gp1 = (GlobalizationPreferences)gp.cloneAsThawed();
        territory = gp1.getTerritory();
        if (!territory.equals("CA")) {
            errln("FAIL: Territory is " + territory + " - Expected: CA");
        }

        gp1.reset();
        ULocale[] locales = new ULocale[2];
        locales[0] = new ULocale("ja");
        locales[1] = new ULocale("zh_Hant_TW");
 
        logln("Set locales - ja, zh_Hant_TW");
        gp1.setLocales(locales);

        territory = gp1.getTerritory();
        if (!territory.equals("TW")) {
            errln("FAIL: Territory is " + territory + " - Expected: TW");
        }
    }

    @Test
    public void TestCurrency() {
        GlobalizationPreferences gp = new GlobalizationPreferences();

        // Set language only locale - ja
        logln("Set locale - ja");
        gp.setLocale(new ULocale("ja"));
        Currency cur = gp.getCurrency();
        String code = cur.getCurrencyCode();
        if (!code.equals("JPY")) {
            errln("FAIL: Currency is " + code + " - Expected: JPY");
        }

        gp.reset();
        // Set locales with territory
        logln("Set locale - ja_US");
        gp.setLocale(new ULocale("ja_US"));
        cur = gp.getCurrency();
        code = cur.getCurrencyCode();
        if (!code.equals("USD")) {
            errln("FAIL: Currency is " + code + " - Expected: USD");
        }

        // Set locales with territory in the second locale
        logln("Set locales - it, en_US");
        ULocale[] locales = new ULocale[2];
        locales[0] = new ULocale("it");
        locales[1] = new ULocale("en_US");
        gp.setLocales(locales);
        cur = gp.getCurrency();
        code = cur.getCurrencyCode();
        if (!code.equals("USD")) {
            errln("FAIL: Currency is " + code + " - Expected: USD");
        }

        // Set explicit territory
        logln("Set territory - DE");
        gp.setTerritory("DE");
        cur = gp.getCurrency();
        code = cur.getCurrencyCode();
        if (!code.equals("EUR")) {
            errln("FAIL: Currency is " + code + " - Expected: EUR");
        }

        // Set explicit currency
        Currency ecur = Currency.getInstance("BRL");
        gp.setCurrency(ecur);
        logln("Set explicit currency - BRL");
        cur = gp.getCurrency();
        code = cur.getCurrencyCode();
        if (!code.equals("BRL")) {
            errln("FAIL: Currency is " + code + " - Expected: BRL");
        }

        // Set explicit territory again
        logln("Set territory - JP");
        cur = gp.getCurrency();
        code = cur.getCurrencyCode();
        if (!code.equals("BRL")) {
            errln("FAIL: Currency is " + code + " - Expected: BRL");
        }

        // Freeze
        logln("Freeze this object");
        Currency ecur2 = Currency.getInstance("CHF");
        boolean bFrozen = false;
        gp.freeze();
        try {
            gp.setCurrency(ecur2);
        } catch (UnsupportedOperationException uoe) {
            logln("setCurrency is blocked");
            bFrozen = true;
        }
        if (!bFrozen) {
            errln("FAIL: setCurrency must be blocked");
        }

        // Safe clone
        logln("cloneAsThawed");
        GlobalizationPreferences gp1 = (GlobalizationPreferences)gp.cloneAsThawed();
        cur = gp.getCurrency();
        code = cur.getCurrencyCode();
        if (!code.equals("BRL")) {
            errln("FAIL: Currency is " + code + " - Expected: BRL");
        }

        // Set ecplicit currency
        gp1.setCurrency(ecur2);
        cur = gp1.getCurrency();
        code = cur.getCurrencyCode();
        if (!code.equals("CHF")) {
            errln("FAIL: Currency is " + code + " - Expected: CHF");
        }
    }

    @Test
    public void TestCalendar() {
        GlobalizationPreferences gp = new GlobalizationPreferences();

        // Set locale - pt_BR
        logln("Set locale - pt");
        gp.setLocale(new ULocale("pt"));
        Calendar cal = gp.getCalendar();
        String calType = cal.getType();
        if (!calType.equals("gregorian")) {
            errln("FAIL: Calendar type is " + calType + " Expected: gregorian");
        }

        // Set a list of locales
        logln("Set locales - en, en_JP, en_GB");
        ULocale[] locales = new ULocale[3];
        locales[0] = new ULocale("en");
        locales[1] = new ULocale("en_JP");
        locales[2] = new ULocale("en_GB");
        gp.setLocales(locales);

        cal = gp.getCalendar();
        ULocale calLocale = cal.getLocale(ULocale.VALID_LOCALE);
        if (!calLocale.equals(locales[2])) {
            errln("FAIL: Calendar locale is " + calLocale.toString() + " - Expected: en_GB");
        }

        // Set ecplicit calendar
        logln("Set Japanese calendar to this object");
        JapaneseCalendar jcal = new JapaneseCalendar();
        gp.setCalendar(jcal);
        cal = gp.getCalendar();
        calType = cal.getType();
        if (!calType.equals("japanese")) {
            errln("FAIL: Calendar type is " + calType + " Expected: japanese");
        }

        jcal.setFirstDayOfWeek(3);
        if (cal.getFirstDayOfWeek() == jcal.getFirstDayOfWeek()) {
            errln("FAIL: Calendar returned by getCalendar must be a safe copy");
        }
        cal.setFirstDayOfWeek(3);
        Calendar cal1 = gp.getCalendar();
        if (cal1.getFirstDayOfWeek() == cal.getFirstDayOfWeek()) {
            errln("FAIL: Calendar returned by getCalendar must be a safe copy");
        }

        // Freeze
        logln("Freeze this object");
        IslamicCalendar ical = new IslamicCalendar();
        boolean bFrozen = false;
        gp.freeze();
        try {
            gp.setCalendar(ical);
        } catch (UnsupportedOperationException uoe) {
            logln("setCalendar is blocked");
            bFrozen = true;
        }
        if (!bFrozen) {
            errln("FAIL: setCalendar must be blocked");
        }

        // Safe clone
        logln("cloneAsThawed");
        GlobalizationPreferences gp1 = (GlobalizationPreferences)gp.cloneAsThawed();
        cal = gp.getCalendar();
        calType = cal.getType();
        if (!calType.equals("japanese")) {
            errln("FAIL: Calendar type afte clone is " + calType + " Expected: japanese");
        }

        logln("Set islamic calendar");
        gp1.setCalendar(ical);
        cal = gp1.getCalendar();
        calType = cal.getType();
        if (!calType.equals("islamic-civil")) { // default constructed IslamicCalendar is islamic-civil
            errln("FAIL: Calendar type afte clone is " + calType + " Expected: islamic-civil");
        }
    }

    @Test
    public void TestTimeZone() {
        GlobalizationPreferences gp = new GlobalizationPreferences();

        // Set locale - zh_CN 
        logln("Set locale - zh_CN");
        gp.setLocale(new ULocale("zh_CN"));
        TimeZone tz = gp.getTimeZone();
        String tzid = tz.getID();
        if (!tzid.equals("Asia/Shanghai")) {
            errln("FAIL: Time zone ID is " + tzid + " Expected: Asia/Shanghai");
        }

        // Set locale - en
        logln("Set locale - en");
        gp.setLocale(new ULocale("en"));
        tz = gp.getTimeZone();
        tzid = tz.getID();
        if (!tzid.equals("America/New_York")) {
            errln("FAIL: Time zone ID is " + tzid + " Expected: America/New_York");
        }

        // Set territory - GB
        logln("Set territory - GB");
        gp.setTerritory("GB");
        tz = gp.getTimeZone();
        tzid = tz.getID();
        if (!tzid.equals("Europe/London")) {
            errln("FAIL: Time zone ID is " + tzid + " Expected: Europe/London");
        }

        // Check if getTimeZone returns a safe clone
        tz.setID("Bad_ID");
        tz = gp.getTimeZone();
        tzid = tz.getID();
        if (!tzid.equals("Europe/London")) {
            errln("FAIL: Time zone ID is " + tzid + " Expected: Europe/London");
        }

        // Set explicit time zone
        TimeZone jst = TimeZone.getTimeZone("Asia/Tokyo");
        String customJstId = "Japan_Standard_Time";
        jst.setID(customJstId);
        gp.setTimeZone(jst);
        tz = gp.getTimeZone();
        tzid = tz.getID();
        if (!tzid.equals(customJstId)) {
            errln("FAIL: Time zone ID is " + tzid + " Expected: " + customJstId);
        }

        // Freeze
        logln("Freeze this object");
        TimeZone cst = TimeZone.getTimeZone("Europe/Paris");
        boolean bFrozen = false;
        gp.freeze();
        try {
            gp.setTimeZone(cst);
        } catch (UnsupportedOperationException uoe) {
            logln("setTimeZone is blocked");
            bFrozen = true;
        }
        if (!bFrozen) {
            errln("FAIL: setTimeZone must be blocked");
        }

        // Modifiable clone
        logln("cloneAsThawed");
        GlobalizationPreferences gp1 = (GlobalizationPreferences)gp.cloneAsThawed();
        tz = gp1.getTimeZone();
        tzid = tz.getID();
        if (!tzid.equals(customJstId)) {
            errln("FAIL: Time zone ID is " + tzid + " Expected: " + customJstId);
        }

        // Set explicit time zone
        gp1.setTimeZone(cst);
        tz = gp1.getTimeZone();
        tzid = tz.getID();
        if (!tzid.equals(cst.getID())) {
            errln("FAIL: Time zone ID is " + tzid + " Expected: " + cst.getID());
        }        
    }

    @Test
    public void TestCollator() {
        GlobalizationPreferences gp = new GlobalizationPreferences();

        // Set locale - tr
        logln("Set locale - tr");
        gp.setLocale(new ULocale("tr"));
        Collator coll = gp.getCollator();
        String locStr = coll.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("tr")) {
            errln("FAIL: Collator locale is " + locStr + " Expected: tr");
        }

        // Unsupported collator locale - zun
        logln("Set locale - zun");
        gp.setLocale(new ULocale("zun"));
        coll = gp.getCollator();
        locStr = coll.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("")) {
            errln("FAIL: Collator locale is \"" + locStr + "\" Expected: \"\"(empty)");
        }

        // Set locales - en_JP, fr, en_US, fr_FR
        logln("Set locale - en_JP, fr, en_US, fr_FR");
        ULocale[] locales = new ULocale[4];
        locales[0] = new ULocale("en_JP");
        locales[1] = new ULocale("fr");
        locales[2] = new ULocale("en_US");
        locales[3] = new ULocale("fr_FR");
        gp.setLocales(locales);
        coll = gp.getCollator();
        locStr = coll.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("fr")) {
            errln("FAIL: Collator locale is " + locStr + " Expected: fr");
        }

        // Set explicit Collator
        Collator coll1 = Collator.getInstance(new ULocale("it"));
        coll1.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        logln("Set collator for it in canonical deconposition mode");
        gp.setCollator(coll1);
        coll1.setStrength(Collator.IDENTICAL);
        coll = gp.getCollator();
        locStr = coll.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("it")) {
            errln("FAIL: Collator locale is " + locStr + " Expected: it");
        }
        if (coll1.equals(coll)) {
            errln("FAIL: setCollator must use a safe copy of a Collator");
        }

        // Freeze
        logln("Freeze this object");
        boolean isFrozen = false;
        gp.freeze();
        try {
            gp.setCollator(coll1);
        } catch (UnsupportedOperationException uoe) {
            logln("setCollator is blocked");
            isFrozen = true;
        }
        if (!isFrozen) {
            errln("FAIL: setCollator must be blocked after freeze");
        }

        // Modifiable clone
        logln("cloneAsThawed");
        GlobalizationPreferences gp1 = (GlobalizationPreferences)gp.cloneAsThawed();
        coll = gp1.getCollator();
        locStr = coll.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("it")) {
            errln("FAIL: Collator locale is " + locStr + " Expected: it");
        }
        if (coll.getDecomposition() != Collator.CANONICAL_DECOMPOSITION) {
            errln("FAIL: Decomposition mode is not CANONICAL_DECOMPOSITION");
        }

        // Set custom collator again
        gp1.setCollator(coll1);
        coll = gp1.getCollator();
        if (coll.getStrength() != Collator.IDENTICAL) {
            errln("FAIL: Strength is not IDENTICAL");
        }
    }

    @Test
    public void TestBreakIterator() {
        GlobalizationPreferences gp = new GlobalizationPreferences();

        // Unsupported break iterator locale - aar
        logln("Set locale - aar");
        gp.setLocale(new ULocale("aar"));
        BreakIterator brk = gp.getBreakIterator(GlobalizationPreferences.BI_LINE);
        String locStr = brk.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("root")) {
            errln("FAIL: Line break iterator locale is " + locStr + " Expected: root");
        }

        // Set locale - es
        logln("Set locale - es");
        gp.setLocale(new ULocale("es"));
        brk = gp.getBreakIterator(GlobalizationPreferences.BI_CHARACTER);
        locStr = brk.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("es")) {
            errln("FAIL: Character break iterator locale is " + locStr + " Expected: es");
        }

        // Set explicit break sentence iterator
        logln("Set break iterator for sentence using locale hu_HU");
        BreakIterator brk1 = BreakIterator.getSentenceInstance(new ULocale("hu_HU"));
        gp.setBreakIterator(GlobalizationPreferences.BI_SENTENCE, brk1);

        brk = gp.getBreakIterator(GlobalizationPreferences.BI_SENTENCE);
        /* TODO: JB#5210
        locStr = brk.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("hu_HU")) {
            errln("FAIL: Sentence break locale is " + locStr + " Expected: hu_HU");
        }
        */
        brk.setText("This is a test case.  Is this a new instance?");
        brk.next();
        if (brk1.current() == brk.current()) {
            errln("FAIL: getBreakIterator must return a new instance");
        }

        // Illegal argument
        logln("Get break iterator type 100");
        boolean illegalArg = false;
        try {
            brk = gp.getBreakIterator(100);
        } catch (IllegalArgumentException iae) {
            logln("Break iterator type 100 is illegal");
            illegalArg = true;
        }
        if (!illegalArg) {
            errln("FAIL: getBreakIterator must throw IllegalArgumentException for type 100");
        }
        logln("Set break iterator type -1");
        illegalArg = false;
        try {
            gp.setBreakIterator(-1, brk1);
        } catch (IllegalArgumentException iae) {
            logln("Break iterator type -1 is illegal");
            illegalArg = true;
        }
        if (!illegalArg) {
            errln("FAIL: getBreakIterator must throw IllegalArgumentException for type -1");
        }

        // Freeze
        logln("Freeze this object");
        BreakIterator brk2 = BreakIterator.getTitleInstance(new ULocale("es_MX"));
        boolean isFrozen = false;
        gp.freeze();
        try {
            gp.setBreakIterator(GlobalizationPreferences.BI_TITLE, brk2);
        } catch (UnsupportedOperationException uoe) {
            logln("setBreakIterator is blocked");
            isFrozen = true;
        }
        if (!isFrozen) {
            errln("FAIL: setBreakIterator must be blocked after frozen");
        }

        // Modifiable clone
        logln("cloneAsThawed");
        GlobalizationPreferences gp1 = (GlobalizationPreferences)gp.cloneAsThawed();
        brk = gp1.getBreakIterator(GlobalizationPreferences.BI_WORD);
        /* TODO: JB#5383
        locStr = brk.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("es")) {
            errln("FAIL: Word break iterator locale is " + locStr + " Expected: es");
        }
        */

        ULocale frFR = new ULocale("fr_FR");
        BreakIterator brkC = BreakIterator.getCharacterInstance(frFR);
        BreakIterator brkW = BreakIterator.getWordInstance(frFR);
        BreakIterator brkL = BreakIterator.getLineInstance(frFR);
        BreakIterator brkS = BreakIterator.getSentenceInstance(frFR);
        BreakIterator brkT = BreakIterator.getTitleInstance(frFR);

        gp1.setBreakIterator(GlobalizationPreferences.BI_CHARACTER, brkC);
        gp1.setBreakIterator(GlobalizationPreferences.BI_WORD, brkW);
        gp1.setBreakIterator(GlobalizationPreferences.BI_LINE, brkL);
        gp1.setBreakIterator(GlobalizationPreferences.BI_SENTENCE, brkS);
        gp1.setBreakIterator(GlobalizationPreferences.BI_TITLE, brkT);

        /* TODO: JB#5210
        locStr = brkC.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("ja_JP")) {
            errln("FAIL: Character break iterator locale is " + locStr + " Expected: fr_FR");
        }
        locStr = brkW.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("ja_JP")) {
            errln("FAIL: Word break iterator locale is " + locStr + " Expected: fr_FR");
        }
        locStr = brkL.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("ja_JP")) {
            errln("FAIL: Line break iterator locale is " + locStr + " Expected: fr_FR");
        }
        locStr = brkS.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("ja_JP")) {
            errln("FAIL: Sentence break iterator locale is " + locStr + " Expected: fr_FR");
        }
        locStr = brkT.getLocale(ULocale.VALID_LOCALE).toString();
        if (!locStr.equals("ja_JP")) {
            errln("FAIL: Title break iterator locale is " + locStr + " Expected: fr_FR");
        }
        */
    }

    @Test
    public void TestDisplayName() {
        GlobalizationPreferences gp = new GlobalizationPreferences();

        ULocale loc_fr_FR_Paris = new ULocale("fr_FR_Paris");
        ULocale loc_peo = new ULocale("peo");

        // Locale list - fr_FR_Paris
        ArrayList locales1 = new ArrayList(1);
        locales1.add(loc_fr_FR_Paris);

        // Locale list - ain, fr_FR_Paris
        ArrayList locales2 = new ArrayList(2);
        locales2.add(loc_peo);
        locales2.add(loc_fr_FR_Paris);

        logln("Locales: <default> | <fr_FR_Paris> | <ain, fr_FR_Paris>");

        // ID_LOCALE
        String id = "zh_Hant_HK";
        String name1 = gp.getDisplayName(id, GlobalizationPreferences.ID_LOCALE);
        gp.setLocales(locales1);
        String name2 = gp.getDisplayName(id, GlobalizationPreferences.ID_LOCALE);
        gp.setLocales(locales2);
        String name3 = gp.getDisplayName(id, GlobalizationPreferences.ID_LOCALE);

        logln("Locale[zh_Hant_HK]: " + name1 + " | " + name2 + " | " + name3);
        if (name1.equals(name2) || !name2.equals(name3)) {
            errln("FAIL: Locale ID");
        }

        // ID_LANGUAGE
        gp.reset();
        id = "fr";
        name1 = gp.getDisplayName(id, GlobalizationPreferences.ID_LANGUAGE);
        gp.setLocales(locales1);
        name2 = gp.getDisplayName(id, GlobalizationPreferences.ID_LANGUAGE);
        gp.setLocales(locales2);
        name3 = gp.getDisplayName(id, GlobalizationPreferences.ID_LANGUAGE);

        logln("Language[fr]: " + name1 + " | " + name2 + " | " + name3);
        if (name1.equals(name2) || !name2.equals(name3)) {
            errln("FAIL: Language ID");
        }

        // ID_SCRIPT
        gp.reset();
        id = "cyrl";
        name1 = gp.getDisplayName(id, GlobalizationPreferences.ID_SCRIPT);
        gp.setLocales(locales1);
        name2 = gp.getDisplayName(id, GlobalizationPreferences.ID_SCRIPT);
        gp.setLocales(locales2);
        name3 = gp.getDisplayName(id, GlobalizationPreferences.ID_SCRIPT);

        logln("Script[cyrl]: " + name1 + " | " + name2 + " | " + name3);
        if (name1.equals(name2) || !name2.equals(name3)) {
            errln("FAIL: Script ID");
        }

        // ID_TERRITORY
        gp.reset();
        id = "JP";
        name1 = gp.getDisplayName(id, GlobalizationPreferences.ID_TERRITORY);
        gp.setLocales(locales1);
        name2 = gp.getDisplayName(id, GlobalizationPreferences.ID_TERRITORY);
        gp.setLocales(locales2);
        name3 = gp.getDisplayName(id, GlobalizationPreferences.ID_TERRITORY);

        logln("Territory[JP]: " + name1 + " | " + name2 + " | " + name3);
        if (name1.equals(name2) || !name2.equals(name3)) {
            errln("FAIL: Territory ID");
        }

        // ID_VARIANT
        gp.reset();
        id = "NEDIS";
        name1 = gp.getDisplayName(id, GlobalizationPreferences.ID_VARIANT);
        gp.setLocales(locales1);
        name2 = gp.getDisplayName(id, GlobalizationPreferences.ID_VARIANT);
        gp.setLocales(locales2);
        name3 = gp.getDisplayName(id, GlobalizationPreferences.ID_VARIANT);

        logln("Variant[NEDIS]: " + name1 + " | " + name2 + " | " + name3);
        if (name1.equals(name2) || !name2.equals(name3)) {
            errln("FAIL: Variant ID");
        }

        // ID_KEYWORD
        gp.reset();
        id = "collation";
        name1 = gp.getDisplayName(id, GlobalizationPreferences.ID_KEYWORD);
        gp.setLocales(locales1);
        name2 = gp.getDisplayName(id, GlobalizationPreferences.ID_KEYWORD);
        gp.setLocales(locales2);
        name3 = gp.getDisplayName(id, GlobalizationPreferences.ID_KEYWORD);

        logln("Keyword[collation]: " + name1 + " | " + name2 + " | " + name3);
        if (name1.equals(name2) || !name2.equals(name3)) {
            errln("FAIL: Keyword ID");
        }

        // ID_KEYWORD_VALUE
        gp.reset();
        id = "collation=traditional";
        name1 = gp.getDisplayName(id, GlobalizationPreferences.ID_KEYWORD_VALUE);
        gp.setLocales(locales1);
        name2 = gp.getDisplayName(id, GlobalizationPreferences.ID_KEYWORD_VALUE);
        gp.setLocales(locales2);
        name3 = gp.getDisplayName(id, GlobalizationPreferences.ID_KEYWORD_VALUE);

        logln("Keyword value[traditional]: " + name1 + " | " + name2 + " | " + name3);
        if (name1.equals(name2) || !name2.equals(name3)) {
            errln("FAIL: Keyword value ID");
        }

        // ID_CURRENCY_SYMBOL
        gp.reset();
        id = "USD";
        name1 = gp.getDisplayName(id, GlobalizationPreferences.ID_CURRENCY_SYMBOL);
        gp.setLocales(locales1);
        name2 = gp.getDisplayName(id, GlobalizationPreferences.ID_CURRENCY_SYMBOL);
        gp.setLocales(locales2);
        name3 = gp.getDisplayName(id, GlobalizationPreferences.ID_CURRENCY_SYMBOL);

        logln("Currency symbol[USD]: " + name1 + " | " + name2 + " | " + name3);
        String dollar = "$";
        String us_dollar = "$US";
        if (!name1.equals(dollar) || !name2.equals(us_dollar) || !name3.equals(us_dollar)) {
            errln("FAIL: Currency symbol ID");
        }

        // ID_CURRENCY
        gp.reset();
        id = "USD";
        name1 = gp.getDisplayName(id, GlobalizationPreferences.ID_CURRENCY);
        gp.setLocales(locales1);
        name2 = gp.getDisplayName(id, GlobalizationPreferences.ID_CURRENCY);
        gp.setLocales(locales2);
        name3 = gp.getDisplayName(id, GlobalizationPreferences.ID_CURRENCY);

        logln("Currency[USD]: " + name1 + " | " + name2 + " | " + name3);
        if (name1.equals(name2) || !name2.equals(name3)) {
            errln("FAIL: Currency ID");
        }

        // ID_TIMEZONE
        gp.reset();
        id = "Europe/Paris";
        name1 = gp.getDisplayName(id, GlobalizationPreferences.ID_TIMEZONE);
        gp.setLocales(locales1);
        name2 = gp.getDisplayName(id, GlobalizationPreferences.ID_TIMEZONE);
        gp.setLocales(locales2);
        name3 = gp.getDisplayName(id, GlobalizationPreferences.ID_TIMEZONE);

        logln("Timezone[Europe/Paris]: " + name1 + " | " + name2 + " | " + name3);
        if (name1.equals(name2) || !name2.equals(name3)) {
            errln("FAIL: Timezone ID");
        }

        // Illegal ID
        gp.reset();
        boolean illegalArg = false;
        try {
            name1 = gp.getDisplayName(id, -1);
        } catch (IllegalArgumentException iae) {
            logln("Illegal type -1");
            illegalArg = true;
        }
        if (!illegalArg) {
            errln("FAIL: getDisplayName must throw IllegalArgumentException for type -1");
        }

        illegalArg = false;
        try {
            name1 = gp.getDisplayName(id, 100);
        } catch (IllegalArgumentException iae) {
            logln("Illegal type 100");
            illegalArg = true;
        }
        if (!illegalArg) {
            errln("FAIL: getDisplayName must throw IllegalArgumentException for type 100");
        }
    }

    @Test
    public void TestDateFormat() {
        GlobalizationPreferences gp = new GlobalizationPreferences();

        String pattern;
        DateFormat df;

        // Set unsupported locale - ach
        logln("Set locale - ach");
        gp.setLocale(new ULocale("ach"));

        // Date - short
        df = gp.getDateFormat(GlobalizationPreferences.DF_SHORT, GlobalizationPreferences.DF_NONE);
        pattern = ((SimpleDateFormat)df).toPattern();
        // root pattern must be used
        if (!pattern.equals("y-MM-dd")) {
            errln("FAIL: SHORT date pattern is " + pattern + " Expected: y-MM-dd");
        }

        // Set locale - fr, fr_CA, fr_FR
        ArrayList lcls = new ArrayList(3);
        lcls.add(new ULocale("fr"));
        lcls.add(new ULocale("fr_CA"));
        lcls.add(new ULocale("fr_FR"));
        logln("Set locales - fr, fr_CA, fr_FR");
        gp.setLocales(lcls);
        // Date - short
        df = gp.getDateFormat(GlobalizationPreferences.DF_SHORT, GlobalizationPreferences.DF_NONE);
        pattern = ((SimpleDateFormat)df).toPattern();
        // fr_CA pattern must be used
        if (!pattern.equals("yy-MM-dd")) {
            errln("FAIL: SHORT date pattern is " + pattern + " Expected: yy-MM-dd");
        }


        // Set locale - en_GB
        logln("Set locale - en_GB");
        gp.setLocale(new ULocale("en_GB"));
        
        // Date - full
        df = gp.getDateFormat(GlobalizationPreferences.DF_FULL, GlobalizationPreferences.DF_NONE);
        pattern = ((SimpleDateFormat)df).toPattern();
        if (!pattern.equals("EEEE, d MMMM y")) {
            errln("FAIL: FULL date pattern is " + pattern + " Expected: EEEE, d MMMM y");
        }

        // Date - long
        df = gp.getDateFormat(GlobalizationPreferences.DF_LONG, GlobalizationPreferences.DF_NONE);
        pattern = ((SimpleDateFormat)df).toPattern();
        if (!pattern.equals("d MMMM y")) {
            errln("FAIL: LONG date pattern is " + pattern + " Expected: d MMMM y");
        }

        // Date - medium
        df = gp.getDateFormat(GlobalizationPreferences.DF_MEDIUM, GlobalizationPreferences.DF_NONE);
        pattern = ((SimpleDateFormat)df).toPattern();
        if (!pattern.equals("d MMM y")) {
            errln("FAIL: MEDIUM date pattern is " + pattern + " Expected: d MMM y");
        }

        // Date - short
        df = gp.getDateFormat(GlobalizationPreferences.DF_SHORT, GlobalizationPreferences.DF_NONE);
        pattern = ((SimpleDateFormat)df).toPattern();
        if (!pattern.equals("dd/MM/y")) {
            errln("FAIL: SHORT date pattern is " + pattern + " Expected: dd/MM/y");
        }

        // Time - full
        df = gp.getDateFormat(GlobalizationPreferences.DF_NONE, GlobalizationPreferences.DF_FULL);
        pattern = ((SimpleDateFormat)df).toPattern();
        if (!pattern.equals("HH:mm:ss zzzz")) {
            errln("FAIL: FULL time pattern is " + pattern + " Expected: HH:mm:ss zzzz");
        }

        // Time - long
        df = gp.getDateFormat(GlobalizationPreferences.DF_NONE, GlobalizationPreferences.DF_LONG);
        pattern = ((SimpleDateFormat)df).toPattern();
        if (!pattern.equals("HH:mm:ss z")) {
            errln("FAIL: LONG time pattern is " + pattern + " Expected: HH:mm:ss z");
        }

        // Time - medium
        df = gp.getDateFormat(GlobalizationPreferences.DF_NONE, GlobalizationPreferences.DF_MEDIUM);
        pattern = ((SimpleDateFormat)df).toPattern();
        if (!pattern.equals("HH:mm:ss")) {
            errln("FAIL: MEDIUM time pattern is " + pattern + " Expected: HH:mm:ss");
        }

        // Time - short
        df = gp.getDateFormat(GlobalizationPreferences.DF_NONE, GlobalizationPreferences.DF_SHORT);
        pattern = ((SimpleDateFormat)df).toPattern();
        if (!pattern.equals("HH:mm")) {
            errln("FAIL: SHORT time pattern is " + pattern + " Expected: HH:mm");
        }

        // Date/Time - full
        df = gp.getDateFormat(GlobalizationPreferences.DF_FULL, GlobalizationPreferences.DF_FULL);
        pattern = ((SimpleDateFormat)df).toPattern();
        if (!pattern.equals("EEEE, d MMMM y 'at' HH:mm:ss zzzz")) {
            errln("FAIL: FULL date/time pattern is " + pattern + " Expected: EEEE, d MMMM y 'at' HH:mm:ss zzzz");
        }

        // Invalid style
        boolean illegalArg = false;
        try {
            df = gp.getDateFormat(-1, GlobalizationPreferences.DF_NONE);
        } catch (IllegalArgumentException iae) {
            logln("Illegal date style -1");
            illegalArg = true;
        }
        if (!illegalArg) {
            errln("FAIL: getDateFormat() must throw IllegalArgumentException for dateStyle -1");
        }

        illegalArg = false;
        try {
            df = gp.getDateFormat(GlobalizationPreferences.DF_NONE, GlobalizationPreferences.DF_NONE);
        } catch (IllegalArgumentException iae) {
            logln("Illegal style - dateStyle:DF_NONE / timeStyle:DF_NONE");
            illegalArg = true;
        }
        if (!illegalArg) {
            errln("FAIL: getDateFormat() must throw IllegalArgumentException for dateStyle:DF_NONE/timeStyle:DF_NONE");
        }

        // Set explicit time zone
        logln("Set timezone - America/Sao_Paulo");
        TimeZone tz = TimeZone.getTimeZone("America/Sao_Paulo");
        gp.setTimeZone(tz);
        df = gp.getDateFormat(GlobalizationPreferences.DF_LONG, GlobalizationPreferences.DF_MEDIUM);
        String tzid = df.getTimeZone().getID();
        if (!tzid.equals("America/Sao_Paulo")) {
            errln("FAIL: The DateFormat instance must use timezone America/Sao_Paulo");
        }

        // Set explicit calendar
        logln("Set calendar - japanese");
        Calendar jcal = new JapaneseCalendar();
        jcal.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        gp.setCalendar(jcal);
        df = gp.getDateFormat(GlobalizationPreferences.DF_SHORT, GlobalizationPreferences.DF_SHORT);
        Calendar dfCal = df.getCalendar();
        if (!(dfCal instanceof JapaneseCalendar)) {
            errln("FAIL: The DateFormat instance must use Japanese calendar");
        }
        // TimeZone must be still America/Sao_Paulo
        tzid = df.getTimeZone().getID();
        if (!tzid.equals("America/Sao_Paulo")) {
            errln("FAIL: The DateFormat instance must use timezone America/Sao_Paulo");
        }

        // Set explicit DateFormat
        logln("Set explicit date format - full date");
        DateFormat customFD = DateFormat.getDateInstance(new IslamicCalendar(), DateFormat.FULL, new ULocale("ar_SA"));
        customFD.setTimeZone(TimeZone.getTimeZone("Asia/Riyadh"));
        gp.setDateFormat(GlobalizationPreferences.DF_FULL, GlobalizationPreferences.DF_NONE, customFD);
        df = gp.getDateFormat(GlobalizationPreferences.DF_FULL, GlobalizationPreferences.DF_NONE);
        dfCal = df.getCalendar();
        if (!(dfCal instanceof IslamicCalendar)) {
            errln("FAIL: The DateFormat instance must use Islamic calendar");
        }
        // TimeZone in the custom DateFormat is overridden by GP's timezone setting
        tzid = df.getTimeZone().getID();
        if (!tzid.equals("America/Sao_Paulo")) {
            errln("FAIL: The DateFormat instance must use timezone America/Sao_Paulo");
        }

        // Freeze
        logln("Freeze this object");
        gp.freeze();
        DateFormat customLD = DateFormat.getDateInstance(new BuddhistCalendar(), DateFormat.LONG, new ULocale("th"));
        customLD.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
        boolean isFrozen = false;
        try {
            gp.setDateFormat(GlobalizationPreferences.DF_LONG, GlobalizationPreferences.DF_NONE, customLD);
        } catch (UnsupportedOperationException uoe) {
            logln("setDateFormat is blocked");
            isFrozen = true;
        }
        if (!isFrozen) {
            errln("FAIL: setDateFormat must be blocked after frozen");
        }

        // Modifiable clone
        logln("cloneAsThawed");
        GlobalizationPreferences gp1 = (GlobalizationPreferences)gp.cloneAsThawed();
        gp1.setDateFormat(GlobalizationPreferences.DF_LONG, GlobalizationPreferences.DF_NONE, customLD);
        
        df = gp1.getDateFormat(GlobalizationPreferences.DF_SHORT, GlobalizationPreferences.DF_SHORT);
        dfCal = df.getCalendar();
        if (!(dfCal instanceof JapaneseCalendar)) {
            errln("FAIL: The DateFormat instance must use Japanese calendar");
        }
        // TimeZone must be still America/Sao_Paulo
        tzid = df.getTimeZone().getID();
        if (!tzid.equals("America/Sao_Paulo")) {
            errln("FAIL: The DateFormat instance must use timezone America/Sao_Paulo");
        }

        df = gp1.getDateFormat(GlobalizationPreferences.DF_LONG, GlobalizationPreferences.DF_NONE);
        dfCal = df.getCalendar();
        if (!(dfCal instanceof BuddhistCalendar)) {
            errln("FAIL: The DateFormat instance must use Buddhist calendar");
        }
        // TimeZone must be still America/Sao_Paulo
        tzid = df.getTimeZone().getID();
        if (!tzid.equals("America/Sao_Paulo")) {
            errln("FAIL: The DateFormat instance must use timezone America/Sao_Paulo");
        }

    }

    @Test
    public void TestNumberFormat() {
        GlobalizationPreferences gp = new GlobalizationPreferences();

        NumberFormat nf;
        String numStr;
        double num = 123456.789;

        // Set unsupported locale with supported territory ang_KR
        logln("Set locale - ang_KR");
        gp.setLocale(new ULocale("ang_KR"));
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_CURRENCY);
        numStr = nf.format(num);
        if (!numStr.equals("\u20a9\u00a0123,457")) {
            errln("FAIL: Number string is " + numStr + " Expected: \u20a9\u00a0123,457");
        }
        
        // Set locale - de_DE
        logln("Set locale - de_DE");
        gp.setLocale(new ULocale("de_DE"));

        // NF_NUMBER
        logln("NUMBER type");
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_NUMBER);
        numStr = nf.format(num);
        if (!numStr.equals("123.456,789")) {
            errln("FAIL: Number string is " + numStr + " Expected: 123.456,789");
        }

        // NF_CURRENCY
        logln("CURRENCY type");
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_CURRENCY);
        numStr = nf.format(num);
        if (!numStr.equals("123.456,79\u00a0\u20AC")) {
            errln("FAIL: Number string is " + numStr + " Expected: 123.456,79\u00a0\u20AC");
        }

        // NF_PERCENT
        logln("PERCENT type");
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_PERCENT);
        numStr = nf.format(num);
        if (!numStr.equals("12.345.679\u00a0%")) {
            errln("FAIL: Number string is " + numStr + " Expected: 12.345.679\u00a0%");
        }

        // NF_SCIENTIFIC
        logln("SCIENTIFIC type");
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_SCIENTIFIC);
        numStr = nf.format(num);
        if (!numStr.equals("1,23456789E5")) {
            errln("FAIL: Number string is " + numStr + " Expected: 1,23456789E5");
        }

        // NF_INTEGER
        logln("INTEGER type");
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_INTEGER);
        numStr = nf.format(num);
        if (!numStr.equals("123.457")) {
            errln("FAIL: Number string is " + numStr + " Expected: 123.457");
        }

        // Invalid number type
        logln("INVALID type");
        boolean illegalArg = false;
        try {
            nf = gp.getNumberFormat(100);
        } catch (IllegalArgumentException iae) {
            logln("Illegal number format type 100");
            illegalArg = true;
        }
        if (!illegalArg) {
            errln("FAIL: getNumberFormat must throw IllegalArgumentException for type 100");
        }
        illegalArg = false;
        try {
            nf = gp.getNumberFormat(-1);
        } catch (IllegalArgumentException iae) {
            logln("Illegal number format type -1");
            illegalArg = true;
        }
        if (!illegalArg) {
            errln("FAIL: getNumberFormat must throw IllegalArgumentException for type -1");
        }
        
        // Set explicit territory
        logln("Set territory - US");
        gp.setTerritory("US");
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_CURRENCY);
        numStr = nf.format(num);
        if (!numStr.equals("123.456,79\u00a0$")) {
            errln("FAIL: Number string is " + numStr + " Expected: 123.456,79\u00a0$");
        }

        // Set explicit currency
        logln("Set currency - GBP");
        gp.setCurrency(Currency.getInstance("GBP"));
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_CURRENCY);
        numStr = nf.format(num);
        if (!numStr.equals("123.456,79\u00a0\u00A3")) {
            errln("FAIL: Number string is " + numStr + " Expected: 123.456,79\u00a0\u00A3");
        }

        // Set exliplicit NumberFormat
        logln("Set explicit NumberFormat objects");
        NumberFormat customNum = NumberFormat.getNumberInstance(new ULocale("he_IL"));
        gp.setNumberFormat(GlobalizationPreferences.NF_NUMBER, customNum);
        NumberFormat customCur = NumberFormat.getCurrencyInstance(new ULocale("zh_CN"));
        gp.setNumberFormat(GlobalizationPreferences.NF_CURRENCY, customCur);
        NumberFormat customPct = NumberFormat.getPercentInstance(new ULocale("el_GR"));
        gp.setNumberFormat(GlobalizationPreferences.NF_PERCENT, customPct);
        NumberFormat customSci = NumberFormat.getScientificInstance(new ULocale("ru_RU"));
        gp.setNumberFormat(GlobalizationPreferences.NF_SCIENTIFIC, customSci);
        NumberFormat customInt = NumberFormat.getIntegerInstance(new ULocale("pt_PT"));
        gp.setNumberFormat(GlobalizationPreferences.NF_INTEGER, customInt);

        
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_NUMBER);
        if (!nf.getLocale(ULocale.VALID_LOCALE).toString().equals("he_IL")) {
            errln("FAIL: The NumberFormat instance must use locale he_IL");
        }
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_CURRENCY);
        if (!nf.getLocale(ULocale.VALID_LOCALE).toString().equals("zh_CN")) {
            errln("FAIL: The NumberFormat instance must use locale zh_CN");
        }
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_PERCENT);
        if (!nf.getLocale(ULocale.VALID_LOCALE).toString().equals("el_GR")) {
            errln("FAIL: The NumberFormat instance must use locale el_GR");
        }
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_SCIENTIFIC);
        if (!nf.getLocale(ULocale.VALID_LOCALE).toString().equals("ru_RU")) {
            errln("FAIL: The NumberFormat instance must use locale ru_RU");
        }
        nf = gp.getNumberFormat(GlobalizationPreferences.NF_INTEGER);
        if (!nf.getLocale(ULocale.VALID_LOCALE).toString().equals("pt_PT")) {
            errln("FAIL: The NumberFormat instance must use locale pt_PT");
        }

        NumberFormat customNum1 = NumberFormat.getNumberInstance(new ULocale("hi_IN"));

        // Freeze
        logln("Freeze this object");
        boolean isFrozen = false;
        gp.freeze();
        try {
            gp.setNumberFormat(GlobalizationPreferences.NF_NUMBER, customNum1);
        } catch (UnsupportedOperationException uoe) {
            logln("setNumberFormat is blocked");
            isFrozen = true;
        }
        if (!isFrozen) {
            errln("FAIL: setNumberFormat must be blocked after frozen");
        }

        // Create a modifiable clone
        GlobalizationPreferences gp1 = (GlobalizationPreferences)gp.cloneAsThawed();

        // Number type format's locale is still he_IL
        nf = gp1.getNumberFormat(GlobalizationPreferences.NF_NUMBER);
        if (!nf.getLocale(ULocale.VALID_LOCALE).toString().equals("he_IL")) {
            errln("FAIL: The NumberFormat instance must use locale he_IL");
        }

        logln("Set custom number format using locale hi_IN");
        gp1.setNumberFormat(GlobalizationPreferences.NF_NUMBER, customNum1);
        nf = gp1.getNumberFormat(GlobalizationPreferences.NF_NUMBER);
        if (!nf.getLocale(ULocale.VALID_LOCALE).toString().equals("hi_IN")) {
            errln("FAIL: The NumberFormat instance must use locale hi_IN");
        }
    }

    /*
     * JB#5380 GlobalizationPreferences#getCalendar() should return a Calendar object
     * initialized with the current time
     */
    @Test
    public void TestJB5380() {
        GlobalizationPreferences gp = new GlobalizationPreferences();
        GregorianCalendar gcal = new GregorianCalendar();

        // set way old date
        gcal.set(Calendar.YEAR, 1950);

        // set calendar to GP
        gp.setCalendar(gcal);

        Calendar cal = gp.getCalendar();
        // Calendar instance returned from GP should be initialized
        // by the current time
        long timeDiff = System.currentTimeMillis() - cal.getTimeInMillis();
        if (Math.abs(timeDiff) > 1000) {
            // if difference is more than 1 second..
            errln("FAIL: The Calendar was not initialized by current time - difference:" + timeDiff);
        }
    }
}
