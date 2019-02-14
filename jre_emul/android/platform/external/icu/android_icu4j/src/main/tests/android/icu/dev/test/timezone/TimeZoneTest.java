/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2000-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.dev.test.timezone;

import android.icu.impl.JavaTimeZone;
import com.google.j2objc.util.NativeTimeZone;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.ICUData;
import android.icu.text.SimpleDateFormat;
import android.icu.util.BasicTimeZone;
import android.icu.util.Calendar;
import android.icu.util.DateTimeRule;
import android.icu.util.GregorianCalendar;
import android.icu.util.InitialTimeZoneRule;
import android.icu.util.RuleBasedTimeZone;
import android.icu.util.SimpleTimeZone;
import android.icu.util.TimeArrayTimeZoneRule;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.TimeZoneRule;
import android.icu.util.TimeZoneTransition;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.VTimeZone;
import android.icu.util.VersionInfo;

/**
 * @test 1.22 99/09/21
 * @bug 4028006 4044013 4096694 4107276 4107570 4112869 4130885
 * @summary test TimeZone
 * @build TimeZoneTest
 */
public class TimeZoneTest extends TestFmwk
{
    static final int millisPerHour = 3600000;

    // Some test case data is current date/tzdata version sensitive and producing errors
    // when year/rule are changed. Although we want to keep our eyes on test failures
    // caused by tzdata changes while development, keep maintaining test data in maintenance
    // stream is a little bit hassle. ICU 49 or later versions are using minor version field
    // to indicate a development build (0) or official release build (others). For development
    // builds, a test failure triggers an error, while release builds only report them in
    // verbose mode with logln.
    static final boolean isDevelopmentBuild = (VersionInfo.ICU_VERSION.getMinor() == 0);

    /**
     * NOTE: As of ICU 2.8, the mapping of 3-letter legacy aliases
     * to `real' Olson IDs is under control of the underlying JDK.
     * This test may fail on one JDK and pass on another; don't be
     * too concerned.  Alan
     *
     * Bug 4130885
     * Certain short zone IDs, used since 1.1.x, are incorrect.
     *  
     * The worst of these is:
     *
     * "CAT" (Central African Time) should be GMT+2:00, but instead returns a
     * zone at GMT-1:00. The zone at GMT-1:00 should be called EGT, CVT, EGST,
     * or AZOST, depending on which zone is meant, but in no case is it CAT.
     *
     * Other wrong zone IDs:
     *
     * ECT (European Central Time) GMT+1:00: ECT is Ecuador Time,
     * GMT-5:00. European Central time is abbreviated CEST.
     *
     * SST (Solomon Island Time) GMT+11:00. SST is actually Samoa Standard Time,
     * GMT-11:00. Solomon Island time is SBT.
     *
     * NST (New Zealand Time) GMT+12:00. NST is the abbreviation for
     * Newfoundland Standard Time, GMT-3:30. New Zealanders use NZST.
     *
     * AST (Alaska Standard Time) GMT-9:00. [This has already been noted in
     * another bug.] It should be "AKST". AST is Atlantic Standard Time,
     * GMT-4:00.
     *
     * PNT (Phoenix Time) GMT-7:00. PNT usually means Pitcairn Time,
     * GMT-8:30. There is no standard abbreviation for Phoenix time, as distinct
     * from MST with daylight savings.
     *
     * In addition to these problems, a number of zones are FAKE. That is, they
     * don't match what people use in the real world.
     *
     * FAKE zones:
     *
     * EET (should be EEST)
     * ART (should be EEST)
     * MET (should be IRST)
     * NET (should be AMST)
     * PLT (should be PKT)
     * BST (should be BDT)
     * VST (should be ICT)
     * CTT (should be CST) +
     * ACT (should be CST) +
     * AET (should be EST) +
     * MIT (should be WST) +
     * IET (should be EST) +
     * PRT (should be AST) +
     * CNT (should be NST)
     * AGT (should be ARST)
     * BET (should be EST) +
     *
     * + A zone with the correct name already exists and means something
     * else. E.g., EST usually indicates the US Eastern zone, so it cannot be
     * used for Brazil (BET).
     */
    @Test
    public void TestShortZoneIDs() throws Exception {

        // Note: If the default TimeZone type is JDK, some time zones
        // may differ from the test data below.  For example, "MST" on
        // IBM JRE is an alias of "America/Denver" for supporting Java 1.1
        // backward compatibility, while Olson tzdata (and ICU) treat it
        // as -7hour fixed offset/no DST.
        boolean isJDKTimeZone = (TimeZone.getDefaultTimeZoneType() == TimeZone.TIMEZONE_JDK);
        if (isJDKTimeZone) {
            logln("Warning: Using JDK TimeZone.  Some test cases may not return expected results.");
        }

        ZoneDescriptor[] REFERENCE_LIST = {
            new ZoneDescriptor("HST", -600, false), // Olson northamerica -10:00
            new ZoneDescriptor("AST", -540, true),  // ICU Link - America/Anchorage
            new ZoneDescriptor("PST", -480, true),  // ICU Link - America/Los_Angeles
            new ZoneDescriptor("PNT", -420, false), // ICU Link - America/Phoenix
            new ZoneDescriptor("MST", -420, false), // updated Aug 2003 aliu
            new ZoneDescriptor("CST", -360, true),  // Olson northamerica -7:00
            new ZoneDescriptor("IET", -300, true),  // ICU Link - America/Indiana/Indianapolis
            new ZoneDescriptor("EST", -300, false), // Olson northamerica -5:00
            new ZoneDescriptor("PRT", -240, false), // ICU Link - America/Puerto_Rico
            new ZoneDescriptor("CNT", -210, true),  // ICU Link - America/St_Johns
            new ZoneDescriptor("AGT", -180, false), // ICU Link - America/Argentina/Buenos_Aires
            new ZoneDescriptor("BET", -180, true),  // ICU Link - America/Sao_Paulo
            new ZoneDescriptor("GMT", 0, false),    // Olson etcetera Link - Etc/GMT
            new ZoneDescriptor("UTC", 0, false),    // Olson etcetera 0
            new ZoneDescriptor("ECT", 60, true),    // ICU Link - Europe/Paris
            new ZoneDescriptor("MET", 60, true),    // Olson europe 1:00 C-Eur
            new ZoneDescriptor("CAT", 120, false),  // ICU Link - Africa/Harare
            new ZoneDescriptor("ART", 120, false),  // ICU Link - Africa/Cairo
            new ZoneDescriptor("EET", 120, true),   // Olson europe 2:00 EU
            new ZoneDescriptor("EAT", 180, false),  // ICU Link - Africa/Addis_Ababa
            new ZoneDescriptor("NET", 240, false),  // ICU Link - Asia/Yerevan
            new ZoneDescriptor("PLT", 300, false),  // ICU Link - Asia/Karachi
            new ZoneDescriptor("IST", 330, false),  // ICU Link - Asia/Kolkata
            new ZoneDescriptor("BST", 360, false),  // ICU Link - Asia/Dhaka
            new ZoneDescriptor("VST", 420, false),  // ICU Link - Asia/Ho_Chi_Minh
            new ZoneDescriptor("CTT", 480, false),  // ICU Link - Asia/Shanghai
            new ZoneDescriptor("JST", 540, false),  // ICU Link - Asia/Tokyo
            new ZoneDescriptor("ACT", 570, false),  // ICU Link - Australia/Darwin
            new ZoneDescriptor("AET", 600, true),   // ICU Link - Australia/Sydney
            new ZoneDescriptor("SST", 660, false),  // ICU Link - Pacific/Guadalcanal
            new ZoneDescriptor("NST", 720, true),   // ICU Link - Pacific/Auckland
            new ZoneDescriptor("MIT", 780, true),   // ICU Link - Pacific/Apia

            new ZoneDescriptor("Etc/Unknown", 0, false),    // CLDR

            new ZoneDescriptor("SystemV/AST4ADT", -240, true),
            new ZoneDescriptor("SystemV/EST5EDT", -300, true),
            new ZoneDescriptor("SystemV/CST6CDT", -360, true),
            new ZoneDescriptor("SystemV/MST7MDT", -420, true),
            new ZoneDescriptor("SystemV/PST8PDT", -480, true),
            new ZoneDescriptor("SystemV/YST9YDT", -540, true),
            new ZoneDescriptor("SystemV/AST4", -240, false),
            new ZoneDescriptor("SystemV/EST5", -300, false),
            new ZoneDescriptor("SystemV/CST6", -360, false),
            new ZoneDescriptor("SystemV/MST7", -420, false),
            new ZoneDescriptor("SystemV/PST8", -480, false),
            new ZoneDescriptor("SystemV/YST9", -540, false),
            new ZoneDescriptor("SystemV/HST10", -600, false),
        };

        for (int i=0; i<REFERENCE_LIST.length; ++i) {
            ZoneDescriptor referenceZone = REFERENCE_LIST[i];
            ZoneDescriptor currentZone = new ZoneDescriptor(TimeZone.getTimeZone(referenceZone.getID()));
            if (referenceZone.equals(currentZone)) {
                logln("ok " + referenceZone);
            }
            else {
                if (!isDevelopmentBuild || isJDKTimeZone) {
                    logln("Warning: Expected " + referenceZone +
                            "; got " + currentZone);
                } else {
                    errln("Fail: Expected " + referenceZone +
                            "; got " + currentZone);
                }
            }
        }
    }

    /**
     * A descriptor for a zone; used to regress the short zone IDs.
     */
    static class ZoneDescriptor {
        String id;
        int offset; // In minutes
        boolean daylight;

        ZoneDescriptor(TimeZone zone) {
            this.id = zone.getID();
            this.offset = zone.getRawOffset() / 60000;
            this.daylight = zone.useDaylightTime();
        }

        ZoneDescriptor(String id, int offset, boolean daylight) {
            this.id = id;
            this.offset = offset;
            this.daylight = daylight;
        }

        public String getID() { return id; }

        public boolean equals(Object o) {
            ZoneDescriptor that = (ZoneDescriptor)o;
            return that != null &&
                id.equals(that.id) &&
                offset == that.offset &&
                daylight == that.daylight;
        }

        public String toString() {
            int min = offset;
            char sign = '+';
            if (min < 0) { sign = '-'; min = -min; }

            return "Zone[\"" + id + "\", GMT" + sign + (min/60) + ':' +
                (min%60<10?"0":"") + (min%60) + ", " +
                (daylight ? "Daylight" : "Standard") + "]";
        }

        public static int compare(Object o1, Object o2) {
            ZoneDescriptor i1 = (ZoneDescriptor)o1;
            ZoneDescriptor i2 = (ZoneDescriptor)o2;
            if (i1.offset > i2.offset) return 1;
            if (i1.offset < i2.offset) return -1;
            if (i1.daylight && !i2.daylight) return 1;
            if (!i1.daylight && i2.daylight) return -1;
            return i1.id.compareTo(i2.id);
        }
    }

    /**
     * As part of the VM fix (see CCC approved RFE 4028006, bug
     * 4044013), TimeZone.getTimeZone() has been modified to recognize
     * generic IDs of the form GMT[+-]hh:mm, GMT[+-]hhmm, and
     * GMT[+-]hh.  Test this behavior here.
     *
     * Bug 4044013
     */
    @Test
    public void TestCustomParse() {
        String[] DATA = {
            // ID               offset(sec)     output ID
            "GMT",              "0",            "GMT",      // system ID
            "GMT-YOUR.AD.HERE", "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT0",             "0",            "GMT0",     // system ID
            "GMT+0",            "0",            "GMT+0",    // system ID
            "GMT+1",            "3600",         "GMT+01:00",
            "GMT-0030",         "-1800",        "GMT-00:30",
            "GMT+15:99",        "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT+",             "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT-",             "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT+0:",           "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT-:",            "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT+0010",         "600",          "GMT+00:10",
            "GMT-10",           "-36000",       "GMT-10:00",
            "GMT+30",           "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT-3:30",         "-12600",       "GMT-03:30",
            "GMT-230",          "-9000",        "GMT-02:30",
            "GMT+05:13:05",     "18785",        "GMT+05:13:05",
            "GMT-71023",        "-25823",       "GMT-07:10:23",
            "GMT+01:23:45:67",  "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT+01:234",       "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT-2:31:123",     "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT+3:75",         "0",            TimeZone.UNKNOWN_ZONE_ID,
            "GMT-01010101",     "0",            TimeZone.UNKNOWN_ZONE_ID,
        };
        for (int i = 0; i < DATA.length; i += 3) {
            String id = DATA[i];
            int offset = Integer.parseInt(DATA[i+1]);
            String expId = DATA[i+2];

            TimeZone zone = TimeZone.getTimeZone(id);
            String gotID = zone.getID();
            int gotOffset = zone.getRawOffset()/1000;

            logln(id + " -> " + gotID + " " + gotOffset);

            if (offset != gotOffset) {
                errln("FAIL: Unexpected offset for " + id + " - returned:" + gotOffset + " expected:" + offset);
            }
            if (!expId.equals(gotID)) {
                if (TimeZone.getDefaultTimeZoneType() != TimeZone.TIMEZONE_ICU) {
                    logln("ID for " + id + " - returned:" + gotID + " expected:" + expId);
                } else {
                    errln("FAIL: Unexpected ID for " + id + " - returned:" + gotID + " expected:" + expId);
                }
            }
        }
    }

    /**
     * Test the basic functionality of the getDisplayName() API.
     *
     * Bug 4112869
     * Bug 4028006
     *
     * See also API change request A41.
     *
     * 4/21/98 - make smarter, so the test works if the ext resources
     * are present or not.
     */
    @Test
    public void TestDisplayName() {
        TimeZone zone = TimeZone.getTimeZone("PST");
        String name = zone.getDisplayName(Locale.ENGLISH);
        logln("PST->" + name);

        // dlf - we now (3.4.1) return generic time
        if (!name.equals("Pacific Time"))
            errln("Fail: Expected \"Pacific Time\", got " + name +
                  " for " + zone);

        //*****************************************************************
        // THE FOLLOWING LINES MUST BE UPDATED IF THE LOCALE DATA CHANGES
        // THE FOLLOWING LINES MUST BE UPDATED IF THE LOCALE DATA CHANGES
        // THE FOLLOWING LINES MUST BE UPDATED IF THE LOCALE DATA CHANGES
        //*****************************************************************

        // Test to allow the user to choose to get all the forms 
        // (z, zzzz, Z, ZZZZ, v, vvvv)
        // todo: check to see whether we can test for all of pst, pdt, pt
        Object[] DATA = {
            // z and zzzz
            Boolean.FALSE, new Integer(TimeZone.SHORT), "PST",
            Boolean.TRUE,  new Integer(TimeZone.SHORT), "PDT",
            Boolean.FALSE, new Integer(TimeZone.LONG),  "Pacific Standard Time",
            Boolean.TRUE,  new Integer(TimeZone.LONG),  "Pacific Daylight Time",
            // v and vvvv
            Boolean.FALSE, new Integer(TimeZone.SHORT_GENERIC), "PT",
            Boolean.TRUE,  new Integer(TimeZone.SHORT_GENERIC), "PT",
            Boolean.FALSE, new Integer(TimeZone.LONG_GENERIC),  "Pacific Time",
            Boolean.TRUE,  new Integer(TimeZone.LONG_GENERIC),  "Pacific Time",  
            // z and ZZZZ
            Boolean.FALSE, new Integer(TimeZone.SHORT_GMT), "-0800",
            Boolean.TRUE,  new Integer(TimeZone.SHORT_GMT), "-0700",
            Boolean.FALSE, new Integer(TimeZone.LONG_GMT),  "GMT-08:00",
            Boolean.TRUE,  new Integer(TimeZone.LONG_GMT),  "GMT-07:00",
            // V and VVVV
            Boolean.FALSE, new Integer(TimeZone.SHORT_COMMONLY_USED), "PST",
            Boolean.TRUE,  new Integer(TimeZone.SHORT_COMMONLY_USED), "PDT",
            Boolean.FALSE, new Integer(TimeZone.GENERIC_LOCATION),  "Los Angeles Time",
            Boolean.TRUE,  new Integer(TimeZone.GENERIC_LOCATION),  "Los Angeles Time",
        };

        for (int i=0; i<DATA.length; i+=3) {
            name = zone.getDisplayName(((Boolean)DATA[i]).booleanValue(),
                                       ((Integer)DATA[i+1]).intValue(),
                                       Locale.ENGLISH);
            if (!name.equals(DATA[i+2]))
                errln("Fail: Expected " + DATA[i+2] + "; got " + name);
        }

        // Make sure that we don't display the DST name by constructing a fake
        // PST zone that has DST all year long.
        // dlf - this test is no longer relevant, we display generic time now
        //    so the behavior of the timezone doesn't matter
        SimpleTimeZone zone2 = new SimpleTimeZone(0, "PST");
        zone2.setStartRule(Calendar.JANUARY, 1, 0);
        zone2.setEndRule(Calendar.DECEMBER, 31, 86399999);
        logln("Modified PST inDaylightTime->" + zone2.inDaylightTime(new Date()));
        name = zone2.getDisplayName(Locale.ENGLISH);
        logln("Modified PST->" + name);
        if (!name.equals("Pacific Time"))
            errln("Fail: Expected \"Pacific Time\"");

        // Make sure we get the default display format for Locales
        // with no display name data.
        Locale mt_MT = new Locale("mt", "MT");
        name = zone.getDisplayName(mt_MT);
        //*****************************************************************
        // THE FOLLOWING LINE MUST BE UPDATED IF THE LOCALE DATA CHANGES
        // THE FOLLOWING LINE MUST BE UPDATED IF THE LOCALE DATA CHANGES
        // THE FOLLOWING LINE MUST BE UPDATED IF THE LOCALE DATA CHANGES
        //*****************************************************************
        logln("PST(mt_MT)->" + name);

        // Now be smart -- check to see if zh resource is even present.
        // If not, we expect the en fallback behavior.

        // in icu4j 2.1 we know we have the zh_CN locale data, though it's incomplete
//    /"DateFormatZoneData", 
        UResourceBundle enRB = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME,Locale.ENGLISH);
        UResourceBundle mtRB = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, mt_MT);
        boolean noZH = enRB == mtRB;

        if (noZH) {
            logln("Warning: Not testing the mt_MT behavior because resource is absent");
            if (!name.equals("Pacific Standard Time"))
                errln("Fail: Expected Pacific Standard Time for PST in mt_MT but got ");
        }
        // dlf - we will use generic time, or if unavailable, GMT for standard time in the zone 
        //     - we now (3.4.1) have localizations for this zone, so change test string
        else if(!name.equals("\u0126in ta\u2019 Los Angeles") &&
            !name.equals("GMT-08:00") &&
            !name.equals("GMT-8:00") &&
            !name.equals("GMT-0800") &&
            !name.equals("GMT-800")) {

            errln("Fail: got '" + name + "', expected GMT-08:00 or something similar\n" +
                  "************************************************************\n" +
                  "THE ABOVE FAILURE MAY JUST MEAN THE LOCALE DATA HAS CHANGED\n" +
                  "************************************************************");
        }

        // Now try a non-existent zone
        zone2 = new SimpleTimeZone(90*60*1000, "xyzzy");
        name = zone2.getDisplayName(Locale.ENGLISH);
        logln("GMT+90min->" + name);
        if (!name.equals("GMT+01:30") &&
            !name.equals("GMT+1:30") &&
            !name.equals("GMT+0130") &&
            !name.equals("GMT+130"))
            errln("Fail: Expected GMT+01:30 or something similar");
        
        // cover getDisplayName() - null arg
        ULocale save = ULocale.getDefault();
        ULocale.setDefault(ULocale.US);
        name = zone2.getDisplayName();
        logln("GMT+90min->" + name + "for default display locale");
        if (!name.equals("GMT+01:30") &&
            !name.equals("GMT+1:30") &&
            !name.equals("GMT+0130") &&
            !name.equals("GMT+130"))
            errln("Fail: Expected GMT+01:30 or something similar");        
        ULocale.setDefault(save);
 
    }

    @Test
    public void TestDisplayName2() {
        Date now = new Date();

        String[] timezones = {"America/Chicago", "Europe/Moscow", "Europe/Rome", "Asia/Shanghai", "WET" };
        String[] locales = {"en", "fr", "de", "ja", "zh_TW", "zh_Hans" };
        for (int j = 0; j < locales.length; ++j) {
            ULocale locale = new ULocale(locales[j]);
            for (int i = 0; i < timezones.length; ++i) {
                TimeZone tz = TimeZone.getTimeZone(timezones[i]);
                String displayName0 = tz.getDisplayName(locale);
                SimpleDateFormat dt = new SimpleDateFormat("vvvv", locale);
                dt.setTimeZone(tz);
                String displayName1 = dt.format(now);  // date value _does_ matter if we fallback to GMT
                logln(locale.getDisplayName() + ", " + tz.getID() + ": " + displayName0);
                if (!displayName1.equals(displayName0)) {
                    // This could happen when the date used is in DST,
                    // because TimeZone.getDisplayName(ULocale) may use
                    // localized GMT format for the time zone's standard
                    // time.
                    if (tz.inDaylightTime(now)) {
                        // Try getDisplayName with daylight argument
                        displayName0 = tz.getDisplayName(true, TimeZone.LONG_GENERIC, locale);
                    }
                    if (!displayName1.equals(displayName0)) {
                        errln(locale.getDisplayName() + ", " + tz.getID() + 
                                ": expected " + displayName1 + " but got: " + displayName0);
                    }
                }
            }
        }
    }

    @Test
    public void TestGenericAPI() {
        String id = "NewGMT";
        int offset = 12345;

        SimpleTimeZone zone = new SimpleTimeZone(offset, id);
        if (zone.useDaylightTime()) errln("FAIL: useDaylightTime should return false");

        TimeZone zoneclone = (TimeZone)zone.clone();
        if (!zoneclone.equals(zone)) errln("FAIL: clone or operator== failed");
        zoneclone.setID("abc");
        if (zoneclone.equals(zone)) errln("FAIL: clone or operator!= failed");
        // delete zoneclone;

        zoneclone = (TimeZone)zone.clone();
        if (!zoneclone.equals(zone)) errln("FAIL: clone or operator== failed");
        zoneclone.setRawOffset(45678);
        if (zoneclone.equals(zone)) errln("FAIL: clone or operator!= failed");

        // C++ only
        /*
          SimpleTimeZone copy(*zone);
          if (!(copy == *zone)) errln("FAIL: copy constructor or operator== failed");
          copy = *(SimpleTimeZone*)zoneclone;
          if (!(copy == *zoneclone)) errln("FAIL: assignment operator or operator== failed");
          */

        TimeZone saveDefault = TimeZone.getDefault();
        TimeZone.setDefault(zone);
        TimeZone defaultzone = TimeZone.getDefault();
        if (defaultzone == zone) errln("FAIL: Default object is identical, not clone");
        // Android patch (http://b/28949992) start.
        /*
         * {icu}TimeZone.setDefault() calls {java}TimeZone.setDefault().
         * On Android {java}TimeZone.setDefault() clears the cached ICU default timezone by calling
         * {icu}TimeZone.clearCachedDefault().
         * Due to this, and some of the logic in {icu}TimeZone.getDefault(), it is not possible to
         * guarantee on Android that {icu}TimeZone.getDefault() returns something that is equal to
         * the object passed to setDefault().
         * {icu}TimeZone.setDefault() is not public / supported on Android.
         */
        // if (!defaultzone.equals(zone)) errln("FAIL: Default object is not equal");
        // Android patch (http://b/28949992) end.
        TimeZone.setDefault(saveDefault);
        // delete defaultzone;
        // delete zoneclone;

//      // ICU 2.6 Coverage
//      logln(zone.toString());
//      logln(zone.getDisplayName());
//      SimpleTimeZoneAdapter stza = new SimpleTimeZoneAdapter((SimpleTimeZone) TimeZone.getTimeZone("GMT"));
//      stza.setID("Foo");
//      if (stza.hasSameRules(java.util.TimeZone.getTimeZone("GMT"))) {
//          errln("FAIL: SimpleTimeZoneAdapter.hasSameRules");
//      }
//      stza.setRawOffset(3000);
//      offset = stza.getOffset(GregorianCalendar.BC, 2001, Calendar.DECEMBER,
//                              25, Calendar.TUESDAY, 12*60*60*1000);
//      if (offset != 3000) {
//          errln("FAIL: SimpleTimeZoneAdapter.getOffset");
//      }
//      SimpleTimeZoneAdapter dup = (SimpleTimeZoneAdapter) stza.clone();
//      if (stza.hashCode() != dup.hashCode()) {
//          errln("FAIL: SimpleTimeZoneAdapter.hashCode");
//      }
//      if (!stza.equals(dup)) {
//          errln("FAIL: SimpleTimeZoneAdapter.equals");
//      }
//      logln(stza.toString());

        String tzver = TimeZone.getTZDataVersion();
        if (tzver.length() != 5 /* 4 digits + 1 letter */) {
            errln("FAIL: getTZDataVersion returned " + tzver);
        } else {
            logln("PASS: tzdata version: " + tzver);
        }
    }

    @Test
    public void TestRuleAPI()
    {
        // ErrorCode status = ZERO_ERROR;

        int offset = (int)(60*60*1000*1.75); // Pick a weird offset
        SimpleTimeZone zone = new SimpleTimeZone(offset, "TestZone");
        if (zone.useDaylightTime()) errln("FAIL: useDaylightTime should return false");

        // Establish our expected transition times.  Do this with a non-DST
        // calendar with the (above) declared local offset.
        GregorianCalendar gc = new GregorianCalendar(zone);
        gc.clear();
        gc.set(1990, Calendar.MARCH, 1);
        long marchOneStd = gc.getTime().getTime(); // Local Std time midnight
        gc.clear();
        gc.set(1990, Calendar.JULY, 1);
        long julyOneStd = gc.getTime().getTime(); // Local Std time midnight

        // Starting and ending hours, WALL TIME
        int startHour = (int)(2.25 * 3600000);
        int endHour   = (int)(3.5  * 3600000);

        zone.setStartRule(Calendar.MARCH, 1, 0, startHour);
        zone.setEndRule  (Calendar.JULY,  1, 0, endHour);

        gc = new GregorianCalendar(zone);
        // if (failure(status, "new GregorianCalendar")) return;

        long marchOne = marchOneStd + startHour;
        long julyOne = julyOneStd + endHour - 3600000; // Adjust from wall to Std time

        long expMarchOne = 636251400000L;
        if (marchOne != expMarchOne)
        {
            errln("FAIL: Expected start computed as " + marchOne +
                  " = " + new Date(marchOne));
            logln("      Should be                  " + expMarchOne +
                  " = " + new Date(expMarchOne));
        }

        long expJulyOne = 646793100000L;
        if (julyOne != expJulyOne)
        {
            errln("FAIL: Expected start computed as " + julyOne +
                  " = " + new Date(julyOne));
            logln("      Should be                  " + expJulyOne +
                  " = " + new Date(expJulyOne));
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.set(1990, Calendar.JANUARY, 1);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(1990, Calendar.JUNE, 1);
        _testUsingBinarySearch(zone, cal1.getTimeInMillis(),
                               cal2.getTimeInMillis(), marchOne);
        cal1.set(1990, Calendar.JUNE, 1);
        cal2.set(1990, Calendar.DECEMBER, 31);
        _testUsingBinarySearch(zone, cal1.getTimeInMillis(),
                               cal2.getTimeInMillis(), julyOne);

        if (zone.inDaylightTime(new Date(marchOne - 1000)) ||
            !zone.inDaylightTime(new Date(marchOne)))
            errln("FAIL: Start rule broken");
        if (!zone.inDaylightTime(new Date(julyOne - 1000)) ||
            zone.inDaylightTime(new Date(julyOne)))
            errln("FAIL: End rule broken");

        zone.setStartYear(1991);
        if (zone.inDaylightTime(new Date(marchOne)) ||
            zone.inDaylightTime(new Date(julyOne - 1000)))
            errln("FAIL: Start year broken");

        // failure(status, "TestRuleAPI");
        // delete gc;
        // delete zone;
    }

    void _testUsingBinarySearch(SimpleTimeZone tz, long min, long max, long expectedBoundary)
    {
        // ErrorCode status = ZERO_ERROR;
        boolean startsInDST = tz.inDaylightTime(new Date(min));
        // if (failure(status, "SimpleTimeZone::inDaylightTime")) return;
        if (tz.inDaylightTime(new Date(max)) == startsInDST) {
            logln("Error: inDaylightTime(" + new Date(max) + ") != " + (!startsInDST));
            return;
        }
        // if (failure(status, "SimpleTimeZone::inDaylightTime")) return;
        while ((max - min) > INTERVAL) {
            long mid = (min + max) / 2;
            if (tz.inDaylightTime(new Date(mid)) == startsInDST) {
                min = mid;
            }
            else {
                max = mid;
            }
            // if (failure(status, "SimpleTimeZone::inDaylightTime")) return;
        }
        logln("Binary Search Before: " + min + " = " + new Date(min));
        logln("Binary Search After:  " + max + " = " + new Date(max));
        long mindelta = expectedBoundary - min;
        // not used long maxdelta = max - expectedBoundary;
        if (mindelta >= 0 &&
            mindelta <= INTERVAL &&
            mindelta >= 0 &&
            mindelta <= INTERVAL)
            logln("PASS: Expected bdry:  " + expectedBoundary + " = " + new Date(expectedBoundary));
        else
            errln("FAIL: Expected bdry:  " + expectedBoundary + " = " + new Date(expectedBoundary));
    }

    static final int INTERVAL = 100;

    // Bug 006; verify the offset for a specific zone.
    @Test
    public void TestPRTOffset()
    {
        TimeZone tz = TimeZone.getTimeZone( "PRT" );
        if( tz == null ) {
            errln( "FAIL: TimeZone(PRT) is null" );
        }
        else{
            if (tz.getRawOffset() != (-4*millisPerHour))
                warnln("FAIL: Offset for PRT should be -4, got " +
                      tz.getRawOffset() / (double)millisPerHour);
        }

    }

    // Test various calls
    @Test
    public void TestVariousAPI518()
    {
        TimeZone time_zone = TimeZone.getTimeZone("PST");
        Calendar cal = Calendar.getInstance();
        cal.set(1997, Calendar.APRIL, 30);
        Date d = cal.getTime();

        logln("The timezone is " + time_zone.getID());

        if (time_zone.inDaylightTime(d) != true)
            errln("FAIL: inDaylightTime returned false");

        if (time_zone.useDaylightTime() != true)
            errln("FAIL: useDaylightTime returned false");

        if (time_zone.getRawOffset() != -8*millisPerHour)
            errln( "FAIL: getRawOffset returned wrong value");

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        if (time_zone.getOffset(GregorianCalendar.AD, gc.get(GregorianCalendar.YEAR), gc.get(GregorianCalendar.MONTH),
                                gc.get(GregorianCalendar.DAY_OF_MONTH),
                                gc.get(GregorianCalendar.DAY_OF_WEEK), 0)
            != -7*millisPerHour)
            errln("FAIL: getOffset returned wrong value");
    }

    // Test getAvailableID API
    @Test
    public void TestGetAvailableIDs913()
    {
        StringBuffer buf = new StringBuffer("TimeZone.getAvailableIDs() = { ");
        String[] s = TimeZone.getAvailableIDs();
        for (int i=0; i<s.length; ++i)
        {
            if (i > 0) buf.append(", ");
            buf.append(s[i]);
        }
        buf.append(" };");
        logln(buf.toString());

        buf.setLength(0);
        buf.append("TimeZone.getAvailableIDs(GMT+02:00) = { ");
        s = TimeZone.getAvailableIDs(+2 * 60 * 60 * 1000);
        for (int i=0; i<s.length; ++i)
        {
            if (i > 0) buf.append(", ");
            buf.append(s[i]);
        }
        buf.append(" };");
        logln(buf.toString());

        TimeZone tz = TimeZone.getTimeZone("PST");
        if (tz != null)
            logln("getTimeZone(PST) = " + tz.getID());
        else
            errln("FAIL: getTimeZone(PST) = null");

        tz = TimeZone.getTimeZone("America/Los_Angeles");
        if (tz != null)
            logln("getTimeZone(America/Los_Angeles) = " + tz.getID());
        else
            errln("FAIL: getTimeZone(PST) = null");

        // Bug 4096694
        tz = TimeZone.getTimeZone("NON_EXISTENT");
        if (tz == null)
            errln("FAIL: getTimeZone(NON_EXISTENT) = null");
        else if (!tz.getID().equals(TimeZone.UNKNOWN_ZONE_ID))
            errln("FAIL: getTimeZone(NON_EXISTENT) = " + tz.getID());
    }

    @Test
    public void TestGetAvailableIDsNew() {
        Set<String> any = TimeZone.getAvailableIDs(SystemTimeZoneType.ANY, null, null);
        Set<String> canonical = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null);
        Set<String> canonicalLoc = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL_LOCATION, null, null);

        checkContainsAll(any, "ANY", canonical, "CANONICAL");
        checkContainsAll(canonical, "CANONICAL", canonicalLoc, "CANONICALLOC");

        Set<String> any_US = TimeZone.getAvailableIDs(SystemTimeZoneType.ANY, "US", null);
        Set<String> canonical_US = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, "US", null);
        Set<String> canonicalLoc_US = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL_LOCATION, "US", null);

        checkContainsAll(any, "ANY", any_US, "ANY_US");
        checkContainsAll(canonical, "CANONICAL", canonical_US, "CANONICAL_US");
        checkContainsAll(canonicalLoc, "CANONICALLOC", canonicalLoc_US, "CANONICALLOC_US");

        checkContainsAll(any_US, "ANY_US", canonical_US, "CANONICAL_US");
        checkContainsAll(canonical_US, "CANONICAL_US", canonicalLoc_US, "CANONICALLOC_US");

        final int HOUR = 60*60*1000;
        Set<String> any_W5 = TimeZone.getAvailableIDs(SystemTimeZoneType.ANY, null, -5 * HOUR);
        Set<String> any_CA_W5 = TimeZone.getAvailableIDs(SystemTimeZoneType.ANY, "CA", -5 * HOUR);

        checkContainsAll(any, "ANY", any_W5, "ANY_W5");
        checkContainsAll(any_W5, "ANY_W5", any_CA_W5, "ANY_CA_W5");

        boolean[] isSystemID = new boolean[1];

        // An ID in any set, but not in canonical set must not be a canonical ID
        for (String id : any) {
            if (canonical.contains(id)) {
                continue;
            }
            String cid = TimeZone.getCanonicalID(id, isSystemID);
            if (id.equals(cid)) {
                errln("FAIL: canonical ID [" + id + "] is not in CANONICAL");
            }
            if (!isSystemID[0]) {
                errln("FAIL: ANY contains non-system ID: " + id);
            }
        }

        // canonical set must contains only canonical IDs
        for (String id : canonical) {
            String cid = TimeZone.getCanonicalID(id, isSystemID);
            if (!id.equals(cid)) {
                errln("FAIL: CANONICAL contains non-canonical ID: " + id);
            }
            if (!isSystemID[0]) {
                errln("FAIL: CANONICAL contains non-system ID: " + id);
            }
        }

        // canonicalLoc set must contains only canonical location IDs
        for (String id : canonicalLoc) {
            String cid = TimeZone.getCanonicalID(id, isSystemID);
            if (!id.equals(cid)) {
                errln("FAIL: CANONICAL contains non-canonical ID: " + id);
            }
            if (!isSystemID[0]) {
                errln("FAIL: CANONICAL contains non-system ID: " + id);
            }
            String region = TimeZone.getRegion(id);
            if (region.equals("001")) {
                errln("FAIL: CANONICALLOC contains non location zone: " + id);
            }
        }

        // any_US must contain only US zones
        for (String id : any_US) {
            String region = TimeZone.getRegion(id);
            if (!region.equals("US")) {
                errln("FAIL: ANY_US contains non-US zone ID: " + id);
            }
        }

        // any_W5 must contain only GMT-05:00 zones
        for (String id : any_W5) {
            TimeZone tz = TimeZone.getTimeZone(id);
            if (tz.getRawOffset() != -5 * HOUR) {
                errln("FAIL: ANY_W5 contains a zone whose offset is not -5:00: " + id);
            }
        }

        // No US zones with GMT+14:00
        Set<String> any_US_E14 = TimeZone.getAvailableIDs(SystemTimeZoneType.ANY, "US", 14 * HOUR);
        if (!any_US_E14.isEmpty()) {
            errln("FAIL: ANY_US_E14 must be empty");
        }
    }

    private void checkContainsAll(Set<String> set1, String name1, Set<String> set2, String name2) {
        if (!set1.containsAll(set2)) {
            StringBuilder buf = new StringBuilder();
            for (String s : set2) {
                if (!set1.contains(s)) {
                    if (buf.length() != 0) {
                        buf.append(",");
                    }
                    buf.append(s);
                }
            }
            errln("FAIL: " + name1 + " does not contain all of " + name2 + " - missing: {" + buf + "}");
        }
    }

    /**
     * Bug 4107276
     */
    @Test
    public void TestDSTSavings() {
        // It might be better to find a way to integrate this test into the main TimeZone
        // tests above, but I don't have time to figure out how to do this (or if it's
        // even really a good idea).  Let's consider that a future.  --rtg 1/27/98
        SimpleTimeZone tz = new SimpleTimeZone(-5 * millisPerHour, "dstSavingsTest",
                                               Calendar.MARCH, 1, 0, 0, Calendar.SEPTEMBER, 1, 0, 0,
                                               (int)(0.5 * millisPerHour));

        if (tz.getRawOffset() != -5 * millisPerHour)
            errln("Got back a raw offset of " + (tz.getRawOffset() / millisPerHour) +
                  " hours instead of -5 hours.");
        if (!tz.useDaylightTime())
            errln("Test time zone should use DST but claims it doesn't.");
        if (tz.getDSTSavings() != 0.5 * millisPerHour)
            errln("Set DST offset to 0.5 hour, but got back " + (tz.getDSTSavings() /
                                                                 millisPerHour) + " hours instead.");

        int offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.JANUARY, 1,
                                  Calendar.THURSDAY, 10 * millisPerHour);
        if (offset != -5 * millisPerHour)
            errln("The offset for 10 AM, 1/1/98 should have been -5 hours, but we got "
                  + (offset / millisPerHour) + " hours.");

        offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.JUNE, 1, Calendar.MONDAY,
                              10 * millisPerHour);
        if (offset != -4.5 * millisPerHour)
            errln("The offset for 10 AM, 6/1/98 should have been -4.5 hours, but we got "
                  + (offset / millisPerHour) + " hours.");

        tz.setDSTSavings(millisPerHour);
        offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.JANUARY, 1,
                              Calendar.THURSDAY, 10 * millisPerHour);
        if (offset != -5 * millisPerHour)
            errln("The offset for 10 AM, 1/1/98 should have been -5 hours, but we got "
                  + (offset / millisPerHour) + " hours.");

        offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.JUNE, 1, Calendar.MONDAY,
                              10 * millisPerHour);
        if (offset != -4 * millisPerHour)
            errln("The offset for 10 AM, 6/1/98 (with a 1-hour DST offset) should have been -4 hours, but we got "
                  + (offset / millisPerHour) + " hours.");
    }

    /**
     * Bug 4107570
     */
    @Test
    public void TestAlternateRules() {
        // Like TestDSTSavings, this test should probably be integrated somehow with the main
        // test at the top of this class, but I didn't have time to figure out how to do that.
        //                      --rtg 1/28/98

        SimpleTimeZone tz = new SimpleTimeZone(-5 * millisPerHour, "alternateRuleTest");

        // test the day-of-month API
        tz.setStartRule(Calendar.MARCH, 10, 12 * millisPerHour);
        tz.setEndRule(Calendar.OCTOBER, 20, 12 * millisPerHour);

        int offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.MARCH, 5,
                                  Calendar.THURSDAY, 10 * millisPerHour);
        if (offset != -5 * millisPerHour)
            errln("The offset for 10AM, 3/5/98 should have been -5 hours, but we got "
                  + (offset / millisPerHour) + " hours.");

        offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.MARCH, 15,
                              Calendar.SUNDAY, 10 * millisPerHour);
        if (offset != -4 * millisPerHour)
            errln("The offset for 10AM, 3/15/98 should have been -4 hours, but we got "
                  + (offset / millisPerHour) + " hours.");

        offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.OCTOBER, 15,
                              Calendar.THURSDAY, 10 * millisPerHour);
        if (offset != -4 * millisPerHour)
            errln("The offset for 10AM, 10/15/98 should have been -4 hours, but we got "
                  + (offset / millisPerHour) + " hours.");

        offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.OCTOBER, 25,
                              Calendar.SUNDAY, 10 * millisPerHour);
        if (offset != -5 * millisPerHour)
            errln("The offset for 10AM, 10/25/98 should have been -5 hours, but we got "
                  + (offset / millisPerHour) + " hours.");

        // test the day-of-week-after-day-in-month API
        tz.setStartRule(Calendar.MARCH, 10, Calendar.FRIDAY, 12 * millisPerHour, true);
        tz.setEndRule(Calendar.OCTOBER, 20, Calendar.FRIDAY, 12 * millisPerHour, false);

        offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.MARCH, 11,
                              Calendar.WEDNESDAY, 10 * millisPerHour);
        if (offset != -5 * millisPerHour)
            errln("The offset for 10AM, 3/11/98 should have been -5 hours, but we got "
                  + (offset / millisPerHour) + " hours.");

        offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.MARCH, 14,
                              Calendar.SATURDAY, 10 * millisPerHour);
        if (offset != -4 * millisPerHour)
            errln("The offset for 10AM, 3/14/98 should have been -4 hours, but we got "
                  + (offset / millisPerHour) + " hours.");

        offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.OCTOBER, 15,
                              Calendar.THURSDAY, 10 * millisPerHour);
        if (offset != -4 * millisPerHour)
            errln("The offset for 10AM, 10/15/98 should have been -4 hours, but we got "
                  + (offset / millisPerHour) + " hours.");

        offset = tz.getOffset(GregorianCalendar.AD, 1998, Calendar.OCTOBER, 17,
                              Calendar.SATURDAY, 10 * millisPerHour);
        if (offset != -5 * millisPerHour)
            errln("The offset for 10AM, 10/17/98 should have been -5 hours, but we got "
                  + (offset / millisPerHour) + " hours.");
    }

    @Test
    public void TestEquivalencyGroups() {
        String id = "America/Los_Angeles";
        int n = TimeZone.countEquivalentIDs(id);
        if (n < 2) {
            errln("FAIL: countEquivalentIDs(" + id + ") returned " + n +
                  ", expected >= 2");
        }
        for (int i=0; i<n; ++i) {
            String s = TimeZone.getEquivalentID(id, i);
            if (s.length() == 0) {
                errln("FAIL: getEquivalentID(" + id + ", " + i +
                      ") returned \"" + s + "\", expected valid ID");
            } else {
                logln("" + i + ":" + s);
            }
        }

        // JB#5480 - equivalent IDs should not be empty within range
        String[] ids = TimeZone.getAvailableIDs();
        for (int i = 0; i < ids.length; i++) {
            int nEquiv = TimeZone.countEquivalentIDs(ids[i]);
            // Each equivalent ID must not be empty
            for (int j = 0; j < nEquiv; j++) {
                String equivID = TimeZone.getEquivalentID(ids[i], j);
                if (equivID.length() == 0) {
                    errln("FAIL: getEquivalentID(" + ids[i] + ", " + i +
                            ") returned \"" + equivID + "\", expected valid ID");
                }
            }
            // equivalent ID out of range must be empty
            String outOfRangeID = TimeZone.getEquivalentID(ids[i], nEquiv);
            if (outOfRangeID.length() != 0) {
                errln("FAIL: getEquivalentID(" + ids[i] + ", " + i +
                        ") returned \"" + outOfRangeID + "\", expected empty string");
            }
        }

        // Ticket#8927 invalid system ID
        final String[] invaldIDs = {"GMT-05:00", "Hello World!", ""};
        for (String invld : invaldIDs) {
            int nEquiv = TimeZone.countEquivalentIDs(invld);
            if (nEquiv != 0) {
                errln("FAIL: countEquivalentIDs(" + invld + ") returned: " + nEquiv
                        + ", expected: 0");
            }
            String sEquiv0 = TimeZone.getEquivalentID(invld, 0);
            if (sEquiv0.length() > 0) {
                errln("FAIL: getEquivalentID(" + invld + ", 0) returned \"" + sEquiv0
                        + "\", expected empty string");
            }
        }
    }

    @Test
    public void TestCountries() {
        // Make sure America/Los_Angeles is in the "US" group, and
        // Asia/Tokyo isn't.  Vice versa for the "JP" group.

        String[] s = TimeZone.getAvailableIDs("US");
        boolean la = false, tokyo = false;
        String laZone = "America/Los_Angeles", tokyoZone = "Asia/Tokyo";

        for (int i=0; i<s.length; ++i) {
            if (s[i].equals(laZone)) {
                la = true;
            }
            if (s[i].equals(tokyoZone)) {
                tokyo = true;
            }
        }
        if (!la ) {
            errln("FAIL: " + laZone + " in US = " + la);
        }
        if (tokyo) {
            errln("FAIL: " + tokyoZone + " in US = " + tokyo);
        }
        s = TimeZone.getAvailableIDs("JP");
        la = false; tokyo = false;

        for (int i=0; i<s.length; ++i) {
            if (s[i].equals(laZone)) {
                la = true;
            }
            if (s[i].equals(tokyoZone)) {
                tokyo = true;
            }
        }
        if (la) {
            errln("FAIL: " + laZone + " in JP = " + la);
        }
        if (!tokyo) {
            errln("FAIL: " + tokyoZone + " in JP = " + tokyo);
        }
    }

    /* J2ObjC removed: use of reflection. */
    @Test
    public void TestFractionalDST() {
        String tzName = "Australia/Lord_Howe"; // 30 min offset
        java.util.TimeZone tz_java = java.util.TimeZone.getTimeZone(tzName);
        int dst_java = 0;
        try {
            // hack so test compiles and runs in both JDK 1.3 and JDK 1.4
            final Object[] args = new Object[0];
            final Class[] argtypes = new Class[0];
            dst_java = tz_java.getDSTSavings();
            if (dst_java <= 0 || dst_java >= 3600000) { // didn't get the fractional time zone we wanted
            errln("didn't get fractional time zone!");
            }
        } catch (SecurityException e) {
            warnln(e.getMessage());
            return;
        }
        
        android.icu.util.TimeZone tz_icu = android.icu.util.TimeZone.getTimeZone(tzName);
        int dst_icu = tz_icu.getDSTSavings();

        if (dst_java != dst_icu) {
            warnln("java reports dst savings of " + dst_java +
              " but icu reports " + dst_icu + 
              " for tz " + tz_icu.getID());
        } else {
            logln("both java and icu report dst savings of " + dst_java + " for tz " + tz_icu.getID());
        }
    }

    @Test
    public void TestGetOffsetDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(1997, Calendar.JANUARY, 30);
        long date = cal.getTimeInMillis();

    TimeZone tz_icu = TimeZone.getTimeZone("America/Los_Angeles");
    int offset = tz_icu.getOffset(date);
    if (offset != -28800000) {
        errln("expected offset -28800000, got: " + offset);
    }

    cal.set(1997, Calendar.JULY, 30);
    date = cal.getTimeInMillis();
    offset = tz_icu.getOffset(date);
    if (offset != -25200000) {
        errln("expected offset -25200000, got: " + offset);
    }
    }

    // jb4484
    @Ignore("J2ObjC: requires reflection metadata.")
    @Test
    public void TestSimpleTimeZoneSerialization() 
    {
        SimpleTimeZone stz0 = new SimpleTimeZone(32400000, "MyTimeZone");
        SimpleTimeZone stz1 = new SimpleTimeZone(32400000, "Asia/Tokyo");
        SimpleTimeZone stz2 = new SimpleTimeZone(32400000, "Asia/Tokyo");
        stz2.setRawOffset(0);
        SimpleTimeZone stz3 = new SimpleTimeZone(32400000, "Asia/Tokyo");
        stz3.setStartYear(100);
        SimpleTimeZone stz4 = new SimpleTimeZone(32400000, "Asia/Tokyo");
        stz4.setStartYear(1000);
        stz4.setDSTSavings(1800000);
        stz4.setStartRule(3, 4, 180000);
        stz4.setEndRule(6, 3, 4, 360000);
        SimpleTimeZone stz5 = new SimpleTimeZone(32400000, "Asia/Tokyo");
        stz5.setStartRule(2, 3, 4, 360000);
        stz5.setEndRule(6, 3, 4, 360000);
        
        SimpleTimeZone[] stzs = { stz0, stz1, stz2, stz3, stz4, stz5, };

        for (int i = 0; i < stzs.length; ++i) {
            SimpleTimeZone stz = stzs[i];
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(stz);
                oos.close();
                byte[] bytes = baos.toByteArray();
                logln("id: " + stz.getID() + " length: " + bytes.length);

                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais);

                SimpleTimeZone stzDeserialized = (SimpleTimeZone)ois.readObject();
                ois.close();

                assertEquals("time zones", stz, stzDeserialized);
            }
            catch (ClassCastException cce) {
                cce.printStackTrace();
                errln("could not deserialize SimpleTimeZone");
            }
            catch (IOException ioe) {
                errln(ioe.getMessage());
            }
            catch (ClassNotFoundException cnfe) {
                errln(cnfe.getMessage());
            }
        }
    }

    // jb4175
    /* Generated by org.unicode.cldr.tool.CountItems */
    private static final String[] timeZoneTestNames = {
        "America/Argentina/Buenos_Aires", "America/Buenos_Aires",
        "America/Argentina/Catamarca", "America/Catamarca",
        "America/Argentina/Cordoba", "America/Cordoba",
        "America/Argentina/Jujuy", "America/Jujuy",
        "America/Argentina/Mendoza", "America/Mendoza",
        "America/Atka", "America/Adak",
        "America/Ensenada", "America/Tijuana",
        "America/Fort_Wayne", "America/Indianapolis",
        "America/Indiana/Indianapolis", "America/Indianapolis",
        "America/Kentucky/Louisville", "America/Louisville",
        "America/Knox_IN", "America/Indiana/Knox",
        "America/Porto_Acre", "America/Rio_Branco",
        "America/Rosario", "America/Cordoba",
        "America/Virgin", "America/St_Thomas",
        "Asia/Ashkhabad", "Asia/Ashgabat",
        "Asia/Chungking", "Asia/Chongqing",
        "Asia/Dacca", "Asia/Dhaka",
        "Asia/Istanbul", "Europe/Istanbul",
        "Asia/Macao", "Asia/Macau",
        "Asia/Tel_Aviv", "Asia/Jerusalem",
        "Asia/Thimbu", "Asia/Thimphu",
        "Asia/Ujung_Pandang", "Asia/Makassar",
        "Asia/Ulan_Bator", "Asia/Ulaanbaatar",
        "Australia/ACT", "Australia/Sydney",
        "Australia/Canberra", "Australia/Sydney",
        "Australia/LHI", "Australia/Lord_Howe",
        "Australia/NSW", "Australia/Sydney",
        "Australia/North", "Australia/Darwin",
        "Australia/Queensland", "Australia/Brisbane",
        "Australia/South", "Australia/Adelaide",
        "Australia/Tasmania", "Australia/Hobart",
        "Australia/Victoria", "Australia/Melbourne",
        "Australia/West", "Australia/Perth",
        "Australia/Yancowinna", "Australia/Broken_Hill",
        "Brazil/Acre", "America/Rio_Branco",
        "Brazil/DeNoronha", "America/Noronha",
        "Brazil/East", "America/Sao_Paulo",
        "Brazil/West", "America/Manaus",
        "CST6CDT", "America/Chicago",
        "Canada/Atlantic", "America/Halifax",
        "Canada/Central", "America/Winnipeg",
        "Canada/East-Saskatchewan", "America/Regina",
        "Canada/Eastern", "America/Toronto",
        "Canada/Mountain", "America/Edmonton",
        "Canada/Newfoundland", "America/St_Johns",
        "Canada/Pacific", "America/Vancouver",
        "Canada/Saskatchewan", "America/Regina",
        "Canada/Yukon", "America/Whitehorse",
        "Chile/Continental", "America/Santiago",
        "Chile/EasterIsland", "Pacific/Easter",
        "Cuba", "America/Havana",
        "EST", "America/Indianapolis",
        "EST5EDT", "America/New_York",
        "Egypt", "Africa/Cairo",
        "Eire", "Europe/Dublin",
        "Etc/GMT+0", "Etc/GMT",
        "Etc/GMT-0", "Etc/GMT",
        "Etc/GMT0", "Etc/GMT",
        "Etc/Greenwich", "Etc/GMT",
        "Etc/UCT", "Etc/GMT",
        "Etc/UTC", "Etc/GMT",
        "Etc/Universal", "Etc/GMT",
        "Etc/Zulu", "Etc/GMT",
        "Europe/Nicosia", "Asia/Nicosia",
        "Europe/Tiraspol", "Europe/Chisinau",
        "GB", "Europe/London",
        "GB-Eire", "Europe/London",
        "GMT", "Etc/GMT",
        "GMT+0", "Etc/GMT",
        "GMT-0", "Etc/GMT",
        "GMT0", "Etc/GMT",
        "Greenwich", "Etc/GMT",
        "HST", "Pacific/Honolulu",
        "Hongkong", "Asia/Hong_Kong",
        "Iceland", "Atlantic/Reykjavik",
        "Iran", "Asia/Tehran",
        "Israel", "Asia/Jerusalem",
        "Jamaica", "America/Jamaica",
        "Japan", "Asia/Tokyo",
        "Kwajalein", "Pacific/Kwajalein",
        "Libya", "Africa/Tripoli",
        "MST", "America/Phoenix",
        "MST7MDT", "America/Denver",
        "Mexico/BajaNorte", "America/Tijuana",
        "Mexico/BajaSur", "America/Mazatlan",
        "Mexico/General", "America/Mexico_City",
        "NZ", "Pacific/Auckland",
        "NZ-CHAT", "Pacific/Chatham",
        "Navajo", "America/Shiprock", /* fixed from Mark's original */
        "PRC", "Asia/Shanghai",
        "PST8PDT", "America/Los_Angeles",
        "Pacific/Samoa", "Pacific/Pago_Pago",
        "Poland", "Europe/Warsaw",
        "Portugal", "Europe/Lisbon",
        "ROC", "Asia/Taipei",
        "ROK", "Asia/Seoul",
        "Singapore", "Asia/Singapore",
        "SystemV/AST4", "America/Puerto_Rico",
        "SystemV/AST4ADT", "America/Halifax",
        "SystemV/CST6", "America/Regina",
        "SystemV/CST6CDT", "America/Chicago",
        "SystemV/EST5", "America/Indianapolis",
        "SystemV/EST5EDT", "America/New_York",
        "SystemV/HST10", "Pacific/Honolulu",
        "SystemV/MST7", "America/Phoenix",
        "SystemV/MST7MDT", "America/Denver",
        "SystemV/PST8", "Pacific/Pitcairn",
        "SystemV/PST8PDT", "America/Los_Angeles",
        "SystemV/YST9", "Pacific/Gambier",
        "SystemV/YST9YDT", "America/Anchorage",
        "Turkey", "Europe/Istanbul",
        "UCT", "Etc/GMT",
        "US/Alaska", "America/Anchorage",
        "US/Aleutian", "America/Adak",
        "US/Arizona", "America/Phoenix",
        "US/Central", "America/Chicago",
        "US/East-Indiana", "America/Indianapolis",
        "US/Eastern", "America/New_York",
        "US/Hawaii", "Pacific/Honolulu",
        "US/Indiana-Starke", "America/Indiana/Knox",
        "US/Michigan", "America/Detroit",
        "US/Mountain", "America/Denver",
        "US/Pacific", "America/Los_Angeles",
        "US/Pacific-New", "America/Los_Angeles",
        "US/Samoa", "Pacific/Pago_Pago",
        "UTC", "Etc/GMT",
        "Universal", "Etc/GMT",
        "W-SU", "Europe/Moscow",
        "Zulu", "Etc/GMT",
    };

    @Test
    public void TestOddTimeZoneNames() {
        for (int i = 0; i < timeZoneTestNames.length; i += 2) {
            String funkyName = timeZoneTestNames[i];
            String correctName = timeZoneTestNames[i+1];

            TimeZone ftz = TimeZone.getTimeZone(funkyName);
            TimeZone ctz = TimeZone.getTimeZone(correctName);

            String fdn = ftz.getDisplayName();
            long fro = ftz.getRawOffset();
            long fds = ftz.getDSTSavings();
            boolean fdy = ftz.useDaylightTime();

            String cdn = ctz.getDisplayName();
            long cro = ctz.getRawOffset();
            long cds = ctz.getDSTSavings();
            boolean cdy = ctz.useDaylightTime();

            if (!fdn.equals(cdn)) {
                logln("display name (" + funkyName + ", " + correctName + ") expected: " + cdn + " but got: " + fdn);
            } else if (fro != cro) {
                logln("offset (" + funkyName + ", " + correctName + ") expected: " + cro + " but got: " + fro);
            } else if (fds != cds) {
                logln("daylight (" + funkyName + ", " + correctName + ") expected: " + cds + " but got: " + fds);
            } else if (fdy != cdy) {
                logln("uses daylight (" + funkyName + ", " + correctName + ") expected: " + cdy + " but got: " + fdy);
            } else {
                // no error, assume we're referencing the same internal java object
            }
        }
    }
    
    @Test
    public void TestCoverage(){
        class StubTimeZone extends TimeZone{
            /**
             * For serialization
             */
            private static final long serialVersionUID = 8658654217433379343L;
            public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {return 0;}
            public void setRawOffset(int offsetMillis) {}
            public int getRawOffset() {return 0;}
            public boolean useDaylightTime() {return false;}
            public boolean inDaylightTime(Date date) {return false;}
        } 
        StubTimeZone stub = new StubTimeZone();
        StubTimeZone stub2 = (StubTimeZone) stub.clone();
        if (stub.getDSTSavings() != 0){
            errln("TimeZone.getDSTSavings() should return 0");
        }
        if (!stub.hasSameRules(stub2)){
            errln("TimeZone.clone() object should hasSameRules");
     
        }
    }
    @Test
    public void TestMark(){
        String tzid = "America/Argentina/ComodRivadavia";
        TimeZone tz = TimeZone.getTimeZone(tzid);
        int offset = tz.getOffset(new Date().getTime());
        logln(tzid + ":\t" + offset);
        List list = Arrays.asList(TimeZone.getAvailableIDs());
        if(!list.contains(tzid)){
            errln("Could create the time zone but it is not in getAvailableIDs");
        }
    }

    @Test
    public void TestZoneMeta() {
        java.util.TimeZone save = java.util.TimeZone.getDefault();
        java.util.TimeZone newZone = java.util.TimeZone.getTimeZone("GMT-08:00");
        android.icu.util.TimeZone.setDefault(null);
        java.util.TimeZone.setDefault(newZone);
        SimpleTimeZone zone = new SimpleTimeZone(0, "GMT");
        android.icu.util.TimeZone defaultZone = android.icu.util.TimeZone.getDefault();
        if(defaultZone==null){
            errln("TimeZone.getDefault() failed for GMT-08:00");
        }
        if(zone==null){
            errln("SimpleTimeZone(0, GMT-08:00) failed for GMT-08:00");
        }
        //reset
        java.util.TimeZone.setDefault(save);
    }

    // Copied from the protected constant in TimeZone.
    private static final int MILLIS_PER_HOUR = 60*60*1000;

    //  Test that a transition at the end of February is handled correctly.
    @Test
    public void TestFebruary() {
        // Time zone with daylight savings time from the first Sunday in November
        // to the last Sunday in February.
        // Similar to the new rule for Brazil (Sao Paulo) in tzdata2006n.
        //
        // Note: In tzdata2007h, the rule had changed, so no actual zones uses
        // lastSun in Feb anymore.
        SimpleTimeZone tz1 = new SimpleTimeZone(
                           -3 * MILLIS_PER_HOUR,                    // raw offset: 3h before (west of) GMT
                           "nov-feb",
                           Calendar.NOVEMBER, 1, Calendar.SUNDAY,   // start: November, first, Sunday
                           0,                                       //        midnight wall time
                           Calendar.FEBRUARY, -1, Calendar.SUNDAY,  // end:   February, last, Sunday
                           0);                                      //        midnight wall time

        // Now hardcode the same rules as for Brazil in tzdata 2006n, so that
        // we cover the intended code even when in the future zoneinfo hardcodes
        // these transition dates.
        SimpleTimeZone tz2= new SimpleTimeZone(
                           -3 * MILLIS_PER_HOUR,                    // raw offset: 3h before (west of) GMT
                           "nov-feb2",
                           Calendar.NOVEMBER, 1, -Calendar.SUNDAY,  // start: November, 1 or after, Sunday
                           0,                                       //        midnight wall time
                           Calendar.FEBRUARY, -29, -Calendar.SUNDAY,// end:   February, 29 or before, Sunday
                           0);                                      //        midnight wall time

        // Gregorian calendar with the UTC time zone for getting sample test date/times.
        GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("Etc/GMT"));
        // "Unable to create the UTC calendar: %s"

        int[] data = {
            // UTC time (6 fields) followed by
            // expected time zone offset in hours after GMT (negative=before GMT).
            // int year, month, day, hour, minute, second, offsetHours
            2006, Calendar.NOVEMBER,  5, 02, 59, 59, -3,
            2006, Calendar.NOVEMBER,  5, 03, 00, 00, -2,
            2007, Calendar.FEBRUARY, 25, 01, 59, 59, -2,
            2007, Calendar.FEBRUARY, 25, 02, 00, 00, -3,

            2007, Calendar.NOVEMBER,  4, 02, 59, 59, -3,
            2007, Calendar.NOVEMBER,  4, 03, 00, 00, -2,
            2008, Calendar.FEBRUARY, 24, 01, 59, 59, -2,
            2008, Calendar.FEBRUARY, 24, 02, 00, 00, -3,

            2008, Calendar.NOVEMBER,  2, 02, 59, 59, -3,
            2008, Calendar.NOVEMBER,  2, 03, 00, 00, -2,
            2009, Calendar.FEBRUARY, 22, 01, 59, 59, -2,
            2009, Calendar.FEBRUARY, 22, 02, 00, 00, -3,

            2009, Calendar.NOVEMBER,  1, 02, 59, 59, -3,
            2009, Calendar.NOVEMBER,  1, 03, 00, 00, -2,
            2010, Calendar.FEBRUARY, 28, 01, 59, 59, -2,
            2010, Calendar.FEBRUARY, 28, 02, 00, 00, -3
        };

        TimeZone timezones[] = { tz1, tz2 };

        TimeZone tz;
        Date dt;
        int t, i, raw, dst;
        int[] offsets = new int[2]; // raw = offsets[0], dst = offsets[1]
        for (t = 0; t < timezones.length; ++t) {
            tz = timezones[t];
            for (i = 0; i < data.length; i+=7) {
                gc.set(data[i], data[i+1], data[i+2],
                       data[i+3], data[i+4], data[i+5]);
                dt = gc.getTime();
                tz.getOffset(dt.getTime(), false, offsets);
                raw = offsets[0];
                dst = offsets[1];
                if ((raw + dst) != data[i+6] * MILLIS_PER_HOUR) {
                    errln("test case " + t + "." + (i/7) + ": " +
                          "tz.getOffset(" + data[i] + "-" + (data[i+1] + 1) + "-" + data[i+2] + " " +
                          data[i+3] + ":" + data[i+4] + ":" + data[i+5] +
                          ") returns " + raw + "+" + dst + " != " + data[i+6] * MILLIS_PER_HOUR);
                }
            }
        }
    }

    @Test
    public void TestCanonicalID() {
        // Some canonical IDs in CLDR are defined as "Link"
        // in Olson tzdata.
        final String[][] excluded1 = {
                {"Africa/Addis_Ababa", "Africa/Nairobi"},
                {"Africa/Asmera", "Africa/Nairobi"},
                {"Africa/Bamako", "Africa/Abidjan"},
                {"Africa/Bangui", "Africa/Lagos"},
                {"Africa/Banjul", "Africa/Abidjan"},
                {"Africa/Blantyre", "Africa/Maputo"},
                {"Africa/Brazzaville", "Africa/Lagos"},
                {"Africa/Bujumbura", "Africa/Maputo"},
                {"Africa/Conakry", "Africa/Abidjan"},
                {"Africa/Dakar", "Africa/Abidjan"},
                {"Africa/Dar_es_Salaam", "Africa/Nairobi"},
                {"Africa/Djibouti", "Africa/Nairobi"},
                {"Africa/Douala", "Africa/Lagos"},
                {"Africa/Freetown", "Africa/Abidjan"},
                {"Africa/Gaborone", "Africa/Maputo"},
                {"Africa/Harare", "Africa/Maputo"},
                {"Africa/Kampala", "Africa/Nairobi"},
                {"Africa/Khartoum", "Africa/Juba"},
                {"Africa/Kigali", "Africa/Maputo"},
                {"Africa/Kinshasa", "Africa/Lagos"},
                {"Africa/Libreville", "Africa/Lagos"},
                {"Africa/Lome", "Africa/Abidjan"},
                {"Africa/Luanda", "Africa/Lagos"},
                {"Africa/Lubumbashi", "Africa/Maputo"},
                {"Africa/Lusaka", "Africa/Maputo"},
                {"Africa/Maseru", "Africa/Johannesburg"},
                {"Africa/Malabo", "Africa/Lagos"},
                {"Africa/Mbabane", "Africa/Johannesburg"},
                {"Africa/Mogadishu", "Africa/Nairobi"},
                {"Africa/Niamey", "Africa/Lagos"},
                {"Africa/Nouakchott", "Africa/Abidjan"},
                {"Africa/Ouagadougou", "Africa/Abidjan"},
                {"Africa/Porto-Novo", "Africa/Lagos"},
                {"Africa/Sao_Tome", "Africa/Abidjan"},
                {"America/Antigua", "America/Port_of_Spain"},
                {"America/Anguilla", "America/Port_of_Spain"},
                {"America/Curacao", "America/Aruba"},
                {"America/Dominica", "America/Port_of_Spain"},
                {"America/Grenada", "America/Port_of_Spain"},
                {"America/Guadeloupe", "America/Port_of_Spain"},
                {"America/Kralendijk", "America/Aruba"},
                {"America/Lower_Princes", "America/Aruba"},
                {"America/Marigot", "America/Port_of_Spain"},
                {"America/Montserrat", "America/Port_of_Spain"},
                {"America/Panama", "America/Cayman"},
                {"America/Santa_Isabel", "America/Tijuana"},
                {"America/Shiprock", "America/Denver"},
                {"America/St_Barthelemy", "America/Port_of_Spain"},
                {"America/St_Kitts", "America/Port_of_Spain"},
                {"America/St_Lucia", "America/Port_of_Spain"},
                {"America/St_Thomas", "America/Port_of_Spain"},
                {"America/St_Vincent", "America/Port_of_Spain"},
                {"America/Toronto", "America/Montreal"},
                {"America/Tortola", "America/Port_of_Spain"},
                {"America/Virgin", "America/Port_of_Spain"},
                {"Antarctica/South_Pole", "Antarctica/McMurdo"},
                {"Arctic/Longyearbyen", "Europe/Oslo"},
                {"Asia/Kuwait", "Asia/Aden"},
                {"Asia/Muscat", "Asia/Dubai"},
                {"Asia/Phnom_Penh", "Asia/Bangkok"},
                {"Asia/Qatar", "Asia/Bahrain"},
                {"Asia/Riyadh", "Asia/Aden"},
                {"Asia/Vientiane", "Asia/Bangkok"},
                {"Atlantic/Jan_Mayen", "Europe/Oslo"},
                {"Atlantic/St_Helena", "Africa/Abidjan"},
                {"Europe/Bratislava", "Europe/Prague"},
                {"Europe/Busingen", "Europe/Zurich"},
                {"Europe/Guernsey", "Europe/London"},
                {"Europe/Isle_of_Man", "Europe/London"},
                {"Europe/Jersey", "Europe/London"},
                {"Europe/Ljubljana", "Europe/Belgrade"},
                {"Europe/Mariehamn", "Europe/Helsinki"},
                {"Europe/Podgorica", "Europe/Belgrade"},
                {"Europe/San_Marino", "Europe/Rome"},
                {"Europe/Sarajevo", "Europe/Belgrade"},
                {"Europe/Skopje", "Europe/Belgrade"},
                {"Europe/Vaduz", "Europe/Zurich"},
                {"Europe/Vatican", "Europe/Rome"},
                {"Europe/Zagreb", "Europe/Belgrade"},
                {"Indian/Antananarivo", "Africa/Nairobi"},
                {"Indian/Comoro", "Africa/Nairobi"},
                {"Indian/Mayotte", "Africa/Nairobi"},
                {"Pacific/Auckland", "Antarctica/McMurdo"},
                {"Pacific/Johnston", "Pacific/Honolulu"},
                {"Pacific/Midway", "Pacific/Pago_Pago"},
                {"Pacific/Saipan", "Pacific/Guam"},
        };

        // Following IDs are aliases of Etc/GMT in CLDR,
        // but Olson tzdata has 3 independent definitions
        // for Etc/GMT, Etc/UTC, Etc/UCT.
        // Until we merge them into one equivalent group
        // in zoneinfo.res, we exclude them in the test
        // below.
        final String[] excluded2 = {
                "Etc/UCT", "UCT",
                "Etc/UTC", "UTC",
                "Etc/Universal", "Universal",
                "Etc/Zulu", "Zulu",
        };

        // Walk through equivalency groups
        String[] ids = TimeZone.getAvailableIDs();
        for (int i = 0; i < ids.length; i++) {
            int nEquiv = TimeZone.countEquivalentIDs(ids[i]);
            if (nEquiv == 0) {
                continue;
            }
            String canonicalID = null;
            boolean bFoundCanonical = false;
            // Make sure getCanonicalID returns the exact same result
            // for all entries within a same equivalency group with some
            // exceptions listed in exluded1.
            // Also, one of them must be canonical id.
            for (int j = 0; j < nEquiv; j++) {
                String tmp = TimeZone.getEquivalentID(ids[i], j);
                String tmpCanonical = TimeZone.getCanonicalID(tmp);
                if (tmpCanonical == null) {
                    errln("FAIL: getCanonicalID(\"" + tmp + "\") returned null");
                    continue;
                }
                // Some exceptional cases
                for (int k = 0; k < excluded1.length; k++) {
                    if (tmpCanonical.equals(excluded1[k][0])) {
                        tmpCanonical = excluded1[k][1];
                    }
                }

                if (j == 0) {
                    canonicalID = tmpCanonical;
                } else if (!canonicalID.equals(tmpCanonical)) {
                    errln("FAIL: getCanonicalID(\"" + tmp + "\") returned " + tmpCanonical + " expected:" + canonicalID);
                }

                if (canonicalID.equals(tmp)) {
                    bFoundCanonical = true;
                }
            }
            // At least one ID in an equvalency group must match the
            // canonicalID
            if (!bFoundCanonical) {
                // test exclusion because of differences between Olson tzdata and CLDR
                boolean isExcluded = false;
                for (int k = 0; k < excluded1.length; k++) {
                    if (ids[i].equals(excluded2[k])) {
                        isExcluded = true;
                        break;
                    }
                }
                if (isExcluded) {
                    continue;
                }

                errln("FAIL: No timezone ids match the canonical ID " + canonicalID);
            }
        }
        // Testing some special cases
        final String[][] data = {
                {"GMT-03", "GMT-03:00", null},
                {"GMT+4", "GMT+04:00", null},
                {"GMT-055", "GMT-00:55", null},
                {"GMT+430", "GMT+04:30", null},
                {"GMT-12:15", "GMT-12:15", null},
                {"GMT-091015", "GMT-09:10:15", null},
                {"GMT+1:90", null, null},
                {"America/Argentina/Buenos_Aires", "America/Buenos_Aires", "true"},
                {"Etc/Unknown", "Etc/Unknown", null},
                {"bogus", null, null},
                {"", null, null},
                {"America/Marigot", "America/Marigot", "true"},     // Olson link, but CLDR canonical (#8953)
                {"Europe/Bratislava", "Europe/Bratislava", "true"}, // Same as above
                {null, null, null},
        };
        boolean[] isSystemID = new boolean[1];
        for (int i = 0; i < data.length; i++) {
            String canonical = TimeZone.getCanonicalID(data[i][0], isSystemID);
            if (canonical != null && !canonical.equals(data[i][1])
                    || canonical == null && data[i][1] != null) {
                errln("FAIL: getCanonicalID(\"" + data[i][0] + "\") returned " + canonical
                        + " - expected: " + data[i][1]);
            }
            if ("true".equalsIgnoreCase(data[i][2]) != isSystemID[0]) {
                errln("FAIL: getCanonicalID(\"" + data[i][0] + "\") set " + isSystemID[0]
                        + " to isSystemID");
            }
        }
    }

    @Test
    public void TestSetDefault() {
        java.util.TimeZone save = java.util.TimeZone.getDefault();

        /*
         * America/Caracs (Venezuela) changed the base offset from -4:00 to
         * -4:30 on Dec 9, 2007.
         */

        TimeZone icuCaracas = TimeZone.getTimeZone("America/Caracas", TimeZone.TIMEZONE_ICU);
        java.util.TimeZone jdkCaracas = java.util.TimeZone.getTimeZone("America/Caracas");

        // Set JDK America/Caracas as the default
        java.util.TimeZone.setDefault(jdkCaracas);

        java.util.Calendar jdkCal = java.util.Calendar.getInstance();
        jdkCal.clear();
        jdkCal.set(2007, java.util.Calendar.JANUARY, 1);

        int rawOffset = jdkCal.get(java.util.Calendar.ZONE_OFFSET);
        int dstSavings = jdkCal.get(java.util.Calendar.DST_OFFSET);

        int[] offsets = new int[2];
        icuCaracas.getOffset(jdkCal.getTime().getTime()/*jdkCal.getTimeInMillis()*/, false, offsets);

        boolean isTimeZoneSynchronized = true;

        if (rawOffset != offsets[0] || dstSavings != offsets[1]) {
            // JDK time zone rule is out of sync...
            logln("Rule for JDK America/Caracas is not same with ICU.  Skipping the rest.");
            isTimeZoneSynchronized = false;
        }

        if (isTimeZoneSynchronized) {
            // If JDK America/Caracas uses the same rule with ICU,
            // the following code should work well.
            TimeZone.setDefault(icuCaracas);

            // Create a new JDK calendar instance again.
            // This calendar should reflect the new default
            // set by ICU TimeZone#setDefault.
            jdkCal = java.util.Calendar.getInstance();
            jdkCal.clear();
            jdkCal.set(2007, java.util.Calendar.JANUARY, 1);

            rawOffset = jdkCal.get(java.util.Calendar.ZONE_OFFSET);
            dstSavings = jdkCal.get(java.util.Calendar.DST_OFFSET);

            if (rawOffset != offsets[0] || dstSavings != offsets[1]) {
                errln("ERROR: Got offset [raw:" + rawOffset + "/dst:" + dstSavings
                          + "] Expected [raw:" + offsets[0] + "/dst:" + offsets[1] + "]");
            }
        }

        // Restore the original JDK time zone
        java.util.TimeZone.setDefault(save);
    }

    /*
     * Test Display Names, choosing zones and lcoales where there are multiple
     * meta-zones defined.
     */
    @Test
    public void TestDisplayNamesMeta() {
        final Integer TZSHORT = new Integer(TimeZone.SHORT);
        final Integer TZLONG = new Integer(TimeZone.LONG);

        final Object[][] zoneDisplayTestData = {
            //  zone id             locale  summer          format      expected display name
            {"Europe/London",       "en",   Boolean.FALSE,  TZSHORT,    "GMT"},
            {"Europe/London",       "en",   Boolean.FALSE,  TZLONG,     "Greenwich Mean Time"},
            {"Europe/London",       "en",   Boolean.TRUE,   TZSHORT,    "GMT+1" /*"BST"*/},
            {"Europe/London",       "en",   Boolean.TRUE,   TZLONG,     "British Summer Time"},

            {"America/Anchorage",   "en",   Boolean.FALSE,  TZSHORT,    "AKST"},
            {"America/Anchorage",   "en",   Boolean.FALSE,  TZLONG,     "Alaska Standard Time"},
            {"America/Anchorage",   "en",   Boolean.TRUE,   TZSHORT,    "AKDT"},
            {"America/Anchorage",   "en",   Boolean.TRUE,   TZLONG,     "Alaska Daylight Time"},

            // Southern Hemisphere, all data from meta:Australia_Western
            {"Australia/Perth",     "en",   Boolean.FALSE,  TZSHORT,    "GMT+8"/*"AWST"*/},
            {"Australia/Perth",     "en",   Boolean.FALSE,  TZLONG,     "Australian Western Standard Time"},
            // Note: Perth does not observe DST currently. When display name is missing,
            // the localized GMT format with the current offset is used even daylight name was
            // requested. See #9350.
            {"Australia/Perth",     "en",   Boolean.TRUE,   TZSHORT,    "GMT+8"/*"AWDT"*/},
            {"Australia/Perth",     "en",   Boolean.TRUE,   TZLONG,     "Australian Western Daylight Time"},

            {"America/Sao_Paulo",   "en",   Boolean.FALSE,  TZSHORT,    "GMT-3"/*"BRT"*/},
            {"America/Sao_Paulo",   "en",   Boolean.FALSE,  TZLONG,     "Brasilia Standard Time"},
            {"America/Sao_Paulo",   "en",   Boolean.TRUE,   TZSHORT,    "GMT-2"/*"BRST"*/},
            {"America/Sao_Paulo",   "en",   Boolean.TRUE,   TZLONG,     "Brasilia Summer Time"},

            // No Summer Time, but had it before 1983.
            {"Pacific/Honolulu",    "en",   Boolean.FALSE,  TZSHORT,    "HST"},
            {"Pacific/Honolulu",    "en",   Boolean.FALSE,  TZLONG,     "Hawaii-Aleutian Standard Time"},
            {"Pacific/Honolulu",    "en",   Boolean.TRUE,   TZSHORT,    "HDT"},
            {"Pacific/Honolulu",    "en",   Boolean.TRUE,   TZLONG,     "Hawaii-Aleutian Daylight Time"},

            // Northern, has Summer, not commonly used.
            {"Europe/Helsinki",     "en",   Boolean.FALSE,  TZSHORT,    "GMT+2"/*"EET"*/},
            {"Europe/Helsinki",     "en",   Boolean.FALSE,  TZLONG,     "Eastern European Standard Time"},
            {"Europe/Helsinki",     "en",   Boolean.TRUE,   TZSHORT,    "GMT+3"/*"EEST"*/},
            {"Europe/Helsinki",     "en",   Boolean.TRUE,   TZLONG,     "Eastern European Summer Time"},

            // Repeating the test data for DST.  The test data below trigger the problem reported
            // by Ticket#6644
            {"Europe/London",       "en",   Boolean.TRUE,   TZSHORT,    "GMT+1" /*"BST"*/},
            {"Europe/London",       "en",   Boolean.TRUE,   TZLONG,     "British Summer Time"},
        };

        boolean isICUTimeZone = (TimeZone.getDefaultTimeZoneType() == TimeZone.TIMEZONE_ICU);

        boolean sawAnError = false;
        for (int testNum = 0; testNum < zoneDisplayTestData.length; testNum++) {
            ULocale locale = new ULocale((String)zoneDisplayTestData[testNum][1]);
            TimeZone zone = TimeZone.getTimeZone((String)zoneDisplayTestData[testNum][0]);
            String displayName = zone.getDisplayName(((Boolean)zoneDisplayTestData[testNum][2]).booleanValue(),
                    ((Integer)zoneDisplayTestData[testNum][3]).intValue());
            if (!displayName.equals(zoneDisplayTestData[testNum][4])) {
                if (isDevelopmentBuild
                        && (isICUTimeZone || !((Boolean)zoneDisplayTestData[testNum][2]).booleanValue())) {
                    sawAnError = true;
                    errln("Incorrect time zone display name.  zone = "
                            + zoneDisplayTestData[testNum][0] + ",\n"
                            + "   locale = " + locale
                            + ",   style = " + (zoneDisplayTestData[testNum][3] == TZSHORT ? "SHORT" : "LONG")
                            + ",   Summertime = " + zoneDisplayTestData[testNum][2] + "\n"
                            + "   Expected " + zoneDisplayTestData[testNum][4]
                            + ",   Got " + displayName);
                } else {
                    logln("Incorrect time zone display name.  zone = "
                            + zoneDisplayTestData[testNum][0] + ",\n"
                            + "   locale = " + locale
                            + ",   style = " + (zoneDisplayTestData[testNum][3] == TZSHORT ? "SHORT" : "LONG")
                            + ",   Summertime = " + zoneDisplayTestData[testNum][2] + "\n"
                            + "   Expected " + zoneDisplayTestData[testNum][4]
                            + ",   Got " + displayName);
                }
            }
        }
        if (sawAnError) {
            logln("Note: Errors could be the result of changes to zoneStrings locale data");
        }
    }

    /*
     * Test case for hashCode problem reported by ticket#7690 OlsonTimeZone.hashCode() throws NPE.
     */
    @Test
    public void TestHashCode() {
        String[] ids = TimeZone.getAvailableIDs();

        for (String id: ids) {
            TimeZone tz1 = TimeZone.getTimeZone(id);
            TimeZone tz2 = TimeZone.getTimeZone(id);

            // hash code are same for the same time zone
            if (tz1.hashCode() != tz2.hashCode()) {
                errln("Fail: Two time zone instances for " + id + " have different hash values.");
            }
            // string representation should be also same
            /* J2ObjC: compare classes instead of string representations. The NSObject's description
             * includes the object's address (e.g. <AndroidIcuImplOlsonTimeZone: 0x7ffbbbf061d0>) */
            if (!tz1.getClass().equals(tz2.getClass())) {
                errln("Fail: Two time zone instances for " + id + " have different classes.");
            }
        }
    }

    /*
     * Test case for getRegion
     */
    @Test
    public void TestGetRegion() {
        final String[][] TEST_DATA = {
            {"America/Los_Angeles",             "US"},
            {"America/Indianapolis",            "US"},  // CLDR canonical, Olson backward
            {"America/Indiana/Indianapolis",    "US"},  // CLDR alias
            {"Mexico/General",                  "MX"},  // Link America/Mexico_City, Olson backward
            {"Etc/UTC",                         "001"},
            {"EST5EDT",                         "001"},
            {"PST",                             "US"},  // Link America/Los_Angeles
            {"Europe/Helsinki",                 "FI"},
            {"Europe/Mariehamn",                "AX"},  // Link Europe/Helsinki, but in zone.tab
            {"Asia/Riyadh",                     "SA"},
            // tz file solar87 was removed from tzdata2013i
            // {"Asia/Riyadh87",                   "001"}, // this should be "SA" actually, but not in zone.tab
            {"Etc/Unknown",                     null},  // CLDR canonical, but not a sysmte zone ID
            {"bogus",                           null},  // bogus
            {"GMT+08:00",                       null},  // a custom ID, not a system zone ID
        };

        for (String[] test : TEST_DATA) {
            try {
                String region = TimeZone.getRegion(test[0]);
                if (!region.equals(test[1])) {
                    if (test[1] == null) {
                        errln("Fail: getRegion(\"" + test[0] + "\") returns "
                                + region + " [expected: IllegalArgumentException]");
                    } else {
                        errln("Fail: getRegion(\"" + test[0] + "\") returns "
                                + region + " [expected: " + test[1] + "]");
                    }
                }
            } catch (IllegalArgumentException e) {
                if (test[1] != null) {
                    errln("Fail: getRegion(\"" + test[0]
                                + "\") throws IllegalArgumentException [expected: " + test[1] + "]");
                }
            }
        }
    }

    @Test
    public void TestZoneFields() {
        assertEquals("UNKNOWN_ZONE wrong ID", "Etc/Unknown", TimeZone.UNKNOWN_ZONE.getID());
        assertEquals("UNKNOWN_ZONE wrong offset", 0, TimeZone.UNKNOWN_ZONE.getRawOffset());
        assertFalse("UNKNOWN_ZONE uses DST", TimeZone.UNKNOWN_ZONE.useDaylightTime());

        assertEquals("GMT_ZONE wrong ID", "Etc/GMT", TimeZone.GMT_ZONE.getID());
        assertEquals("GMT_ZONE wrong offset", 0, TimeZone.GMT_ZONE.getRawOffset());
        assertFalse("GMT_ZONE uses DST", TimeZone.GMT_ZONE.useDaylightTime());
    }

    /*
     * Test case for Freezable
     */
    @Test
    public void TestFreezable() {
        // Test zones - initially thawed
        TimeZone[] ZA1 = {
            TimeZone.getDefault(),
            TimeZone.getTimeZone("America/Los_Angeles", TimeZone.TIMEZONE_ICU),
            TimeZone.getTimeZone("America/Los_Angeles", TimeZone.TIMEZONE_JDK),
            new SimpleTimeZone(0, "stz"),
            new RuleBasedTimeZone("rbtz", new InitialTimeZoneRule("rbtz0", 0, 0)),
            VTimeZone.create("America/New_York"),
        };

        checkThawed(ZA1, "ZA1");
        // freeze
        for (int i = 0; i < ZA1.length; i++) {
            ZA1[i].freeze();
        }
        checkFrozen(ZA1, "ZA1(frozen)");

        // Test zones - initially frozen
        final TimeZone[] ZA2 = {
            TimeZone.GMT_ZONE,
            TimeZone.UNKNOWN_ZONE,
            TimeZone.getFrozenTimeZone("America/Los_Angeles"),
            new SimpleTimeZone(3600000, "frz_stz").freeze(),
            new RuleBasedTimeZone("frz_rbtz", new InitialTimeZoneRule("frz_rbtz0", 3600000, 0)).freeze(),
            VTimeZone.create("Asia/Tokyo").freeze(),
        };

        checkFrozen(ZA2, "ZA2");
        TimeZone[] ZA2_thawed = new TimeZone[ZA2.length];
        // create thawed clone
        for (int i = 0; i < ZA2_thawed.length; i++) {
            ZA2_thawed[i] = ZA2[i].cloneAsThawed();
        }
        checkThawed(ZA2_thawed, "ZA2(thawed)");

    }

    private void checkThawed(TimeZone[] thawedZones, String zaName) {
        for (int i = 0; i < thawedZones.length; i++) {
            if (thawedZones[i].isFrozen()) {
                errln("Fail: " + zaName + "[" + i + "] is frozen.");
            }

            // clone
            TimeZone copy = (TimeZone)thawedZones[i].clone();
            if (thawedZones[i] == copy || !thawedZones[i].equals(copy)) {
                errln("Fail: " + zaName + "[" + i + "] - clone does not work.");
            }

            // cloneAsThawed
            TimeZone thawed = (TimeZone)thawedZones[i].cloneAsThawed();
            if (thawed.isFrozen() || !thawedZones[i].equals(thawed)) {
                errln("Fail: " + zaName + "[" + i + "] - cloneAsThawed does not work.");
            }

            // setID
            try {
                String newID = "foo";
                thawedZones[i].setID(newID);
                if (!thawedZones[i].getID().equals(newID)) {
                    errln("Fail: " + zaName + "[" + i + "] - setID(\"" + newID + "\") does not work.");
                }
            } catch (UnsupportedOperationException e) {
                errln("Fail: " + zaName + "[" + i + "] - setID throws UnsupportedOperationException.");
            }

            // setRawOffset
            // J2ObjC: RuleBasedTimeZone and NativeTimeZone do not support setRawOffset.
            boolean isNativeTimeZone =
                Optional.of(thawedZones[i])
                    .filter(JavaTimeZone.class::isInstance)
                    .map(JavaTimeZone.class::cast)
                    .map(JavaTimeZone::unwrap)
                    .filter(NativeTimeZone.class::isInstance)
                    .isPresent();
            if (!(thawedZones[i] instanceof RuleBasedTimeZone) && !isNativeTimeZone) {
                try {
                    int newOffset = -3600000;
                    thawedZones[i].setRawOffset(newOffset);
                    if (thawedZones[i].getRawOffset() != newOffset) {
                        errln("Fail: " + zaName + "[" + i + "] - setRawOffset(" + newOffset + ") does not work.");
                    }
                } catch (UnsupportedOperationException e) {
                    errln("Fail: " + zaName + "[" + i + "] - setRawOffset throws UnsupportedOperationException.");
                }
            }

            if (thawedZones[i] instanceof SimpleTimeZone) {
                SimpleTimeZone stz = (SimpleTimeZone)thawedZones[i];
                // setDSTSavings
                try {
                    int newDSTSavings = 1800000;
                    stz.setDSTSavings(newDSTSavings);
                    if (stz.getDSTSavings() != newDSTSavings) {
                        errln("Fail: (SimpleTimeZone)" + zaName + "[" + i + "] - setDSTSavings(" + newDSTSavings + ") does not work.");
                    }
                } catch (UnsupportedOperationException e) {
                    errln("Fail: (SimpleTimeZone)" + zaName + "[" + i + "] - setDSTSavings throws UnsupportedOperationException.");
                }
                // setStartRule
                try {
                    stz.setStartRule(Calendar.JANUARY, -1, Calendar.SUNDAY, 0);
                } catch (UnsupportedOperationException e) {
                    errln("Fail: (SimpleTimeZone)" + zaName + "[" + i + "] - setStartRule throws UnsupportedOperationException.");
                }
                // setEndRule
                try {
                    stz.setEndRule(Calendar.DECEMBER, 1, Calendar.SUNDAY, 0);
                } catch (UnsupportedOperationException e) {
                    errln("Fail: (SimpleTimeZone)" + zaName + "[" + i + "] - setEndRule throws UnsupportedOperationException.");
                }
                // setStartYear
                try {
                    stz.setStartYear(2000);
                } catch (UnsupportedOperationException e) {
                    errln("Fail: (SimpleTimeZone)" + zaName + "[" + i + "] - setStartYear throws UnsupportedOperationException.");
                }
            } else if (thawedZones[i] instanceof RuleBasedTimeZone) {
                RuleBasedTimeZone rbtz = (RuleBasedTimeZone)thawedZones[i];
                // addTransitionRule
                try {
                    TimeArrayTimeZoneRule tr1 = new TimeArrayTimeZoneRule("tr1", 7200000, 0, new long[] {0}, DateTimeRule.UTC_TIME);
                    rbtz.addTransitionRule(tr1);
                } catch (UnsupportedOperationException e) {
                    errln("Fail: (RuleBasedTimeZone)" + zaName + "[" + i + "] - addTransitionRule throws UnsupportedOperationException.");
                }
            } else if (thawedZones[i] instanceof VTimeZone) {
                VTimeZone vtz = (VTimeZone)thawedZones[i];
                // setTZURL
                try {
                    String tzUrl = "http://icu-project.org/timezone";
                    vtz.setTZURL(tzUrl);
                    if (!vtz.getTZURL().equals(tzUrl)) {
                        errln("Fail: (VTimeZone)" + zaName + "[" + i + "] - setTZURL does not work.");
                    }
                } catch (UnsupportedOperationException e) {
                    errln("Fail: (VTimeZone)" + zaName + "[" + i + "] - setTZURL throws UnsupportedOperationException.");
                }
                // setLastModified
                try {
                    Date d = new Date();
                    vtz.setLastModified(d);
                    if (!vtz.getLastModified().equals(d)) {
                        errln("Fail: (VTimeZone)" + zaName + "[" + i + "] - setLastModified does not work.");
                    }
                } catch (UnsupportedOperationException e) {
                    errln("Fail: (VTimeZone)" + zaName + "[" + i + "] - setLastModified throws UnsupportedOperationException.");
                }
            }
        }
    }

    private void checkFrozen(TimeZone[] frozenZones, String zaName) {
        for (int i = 0; i < frozenZones.length; i++) {
            if (!frozenZones[i].isFrozen()) {
                errln("Fail: " + zaName + "[" + i + "] is not frozen.");
            }

            // clone
            TimeZone copy = (TimeZone)frozenZones[i].clone();
            if (frozenZones[i] != copy) {
                errln("Fail: " + zaName + "[" + i + "] - clone does not return the object itself.");
            }

            // cloneAsThawed
            TimeZone thawed = (TimeZone)frozenZones[i].cloneAsThawed();
            if (thawed.isFrozen() || !frozenZones[i].equals(thawed)) {
                errln("Fail: " + zaName + "[" + i + "] - cloneAsThawed does not work.");
            }

            // setID
            try {
                String newID = "foo";
                frozenZones[i].setID(newID);
                errln("Fail: " + zaName + "[" + i + "] - setID must throw UnsupportedOperationException.");
            } catch (UnsupportedOperationException e) {
                // OK
            }

            // setRawOffset
            if (!(frozenZones[i] instanceof RuleBasedTimeZone)) {    // RuleBasedTimeZone does not supprot setRawOffset
                try {
                    int newOffset = -3600000;
                    frozenZones[i].setRawOffset(newOffset);
                    errln("Fail: " + zaName + "[" + i + "] - setRawOffset must throw UnsupportedOperationException.");
                } catch (UnsupportedOperationException e) {
                    // OK
                }
            }

            if (frozenZones[i] instanceof SimpleTimeZone) {
                SimpleTimeZone stz = (SimpleTimeZone)frozenZones[i];
                // setDSTSavings
                try {
                    int newDSTSavings = 1800000;
                    stz.setDSTSavings(newDSTSavings);
                    errln("Fail: (SimpleTimeZone)" + zaName + "[" + i + "] - setDSTSavings must throw UnsupportedOperationException.");
                } catch (UnsupportedOperationException e) {
                    // OK
                }
                // setStartRule
                try {
                    stz.setStartRule(Calendar.JANUARY, -1, Calendar.SUNDAY, 0);
                    errln("Fail: (SimpleTimeZone)" + zaName + "[" + i + "] - setStartRule must throw UnsupportedOperationException.");
                } catch (UnsupportedOperationException e) {
                    // OK
                }
                // setEndRule
                try {
                    stz.setEndRule(Calendar.DECEMBER, 1, Calendar.SUNDAY, 0);
                    errln("Fail: (SimpleTimeZone)" + zaName + "[" + i + "] - setEndRule must throw UnsupportedOperationException.");
                } catch (UnsupportedOperationException e) {
                    // OK
                }
                // setStartYear
                try {
                    stz.setStartYear(2000);
                    errln("Fail: (SimpleTimeZone)" + zaName + "[" + i + "] - setStartYear must throw UnsupportedOperationException.");
                } catch (UnsupportedOperationException e) {
                    // OK
                }
            } else if (frozenZones[i] instanceof RuleBasedTimeZone) {
                RuleBasedTimeZone rbtz = (RuleBasedTimeZone)frozenZones[i];
                // addTransitionRule
                try {
                    TimeArrayTimeZoneRule tr1 = new TimeArrayTimeZoneRule("tr1", 7200000, 0, new long[] {0}, DateTimeRule.UTC_TIME);
                    rbtz.addTransitionRule(tr1);
                    errln("Fail: (RuleBasedTimeZone)" + zaName + "[" + i + "] - addTransitionRule must throw UnsupportedOperationException.");
                } catch (UnsupportedOperationException e) {
                    // OK
                }
            } else if (frozenZones[i] instanceof VTimeZone) {
                VTimeZone vtz = (VTimeZone)frozenZones[i];
                // setTZURL
                try {
                    String tzUrl = "http://icu-project.org/timezone";
                    vtz.setTZURL(tzUrl);
                    errln("Fail: (VTimeZone)" + zaName + "[" + i + "] - setTZURL must throw UnsupportedOperationException.");
                } catch (UnsupportedOperationException e) {
                    // OK
                }
                // setLastModified
                try {
                    Date d = new Date();
                    vtz.setLastModified(d);
                    errln("Fail: (VTimeZone)" + zaName + "[" + i + "] - setLastModified must throw UnsupportedOperationException.");
                } catch (UnsupportedOperationException e) {
                    // OK
                }
            }
        }
    }

    @Test
    public void TestObservesDaylightTime() {
        boolean observesDaylight;
        long current = System.currentTimeMillis();

        // J2ObjC change: time zones going through adjustments are problematic when comparing
        // the ICU tz data vs. the operating system tz data.
        Set<String> unstableTzids = new HashSet<>();
        // https://github.com/eggert/tz/blob/379f7ba9b81eee97f0209a322540b4a522122b5d/africa#L847
        unstableTzids.add("Africa/Casablanca");
        unstableTzids.add("Africa/El_Aaiun");

        String[] tzids = TimeZone.getAvailableIDs();
        for (String tzid : tzids) {
            // OlsonTimeZone
            TimeZone tz = TimeZone.getTimeZone(tzid, TimeZone.TIMEZONE_ICU);
            observesDaylight = tz.observesDaylightTime();
            if (observesDaylight != isDaylightTimeAvailable(tz, current)) {
                errln("Fail: [OlsonTimeZone] observesDaylightTime() returned " + observesDaylight + " for " + tzid);
            }

            // RuleBasedTimeZone
            RuleBasedTimeZone rbtz = createRBTZ((BasicTimeZone)tz, current);
            boolean observesDaylightRBTZ = rbtz.observesDaylightTime();
            if (observesDaylightRBTZ != isDaylightTimeAvailable(rbtz, current)) {
                errln("Fail: [RuleBasedTimeZone] observesDaylightTime() returned " + observesDaylightRBTZ + " for " + rbtz.getID());
            } else if (observesDaylight != observesDaylightRBTZ) {
                errln("Fail: RuleBasedTimeZone " + rbtz.getID() + " returns " + observesDaylightRBTZ + ", but different from match OlsonTimeZone");
            }

            // JavaTimeZone
            tz = TimeZone.getTimeZone(tzid, TimeZone.TIMEZONE_JDK);
            observesDaylight = tz.observesDaylightTime();
            if (!unstableTzids.contains(tzid)
                && observesDaylight != isDaylightTimeAvailable(tz, current)) {
                errln("Fail: [JavaTimeZone] observesDaylightTime() returned " + observesDaylight + " for " + tzid);
            }

            // VTimeZone
            tz = VTimeZone.getTimeZone(tzid);
            observesDaylight = tz.observesDaylightTime();
            if (observesDaylight != isDaylightTimeAvailable(tz, current)) {
                errln("Fail: [VTimeZone] observesDaylightTime() returned " + observesDaylight + " for " + tzid);
            }
        }

        // SimpleTimeZone
        SimpleTimeZone[] stzs = {
            new SimpleTimeZone(0, "STZ0"),
            new SimpleTimeZone(-5*60*60*1000, "STZ-5D", Calendar.MARCH, 2, Calendar.SUNDAY, 2*60*60*1000,
                    Calendar.NOVEMBER, 1, Calendar.SUNDAY, 2*60*60*1000),
        };
        for (SimpleTimeZone stz : stzs) {
            observesDaylight = stz.observesDaylightTime();
            if (observesDaylight != isDaylightTimeAvailable(stz, current)) {
                errln("Fail: [SimpleTimeZone] observesDaylightTime() returned " + observesDaylight + " for " + stz.getID());
            }
        }
    }
    
    @Test
    public void Test11619_UnrecognizedTimeZoneID() {
        VTimeZone vzone = VTimeZone.create("ABadTimeZoneId");
        TestFmwk.assertNull("", vzone);
    }

    private static boolean isDaylightTimeAvailable(TimeZone tz, long start) {
        if (tz.inDaylightTime(new Date(start))) {
            return true;
        }

        long date;
        if (tz instanceof BasicTimeZone) {
            BasicTimeZone btz = (BasicTimeZone)tz;
            // check future transitions, up to 100
            date = start;
            for (int i = 0; i < 100; i++) {
                TimeZoneTransition tzt = btz.getNextTransition(date, false);
                if (tzt == null) {
                    // no more transitions
                    break;
                }
                if (tzt.getTo().getDSTSavings() != 0) {
                    return true;
                }
                date = tzt.getTime();
            }
        } else {
            // check future times by incrementing 30 days, up to 200 times (about 16 years)
            final long inc = 30L * 24 * 60 * 60 * 1000;
            int[] offsets = new int[2];
            date = start + inc;
            for (int i = 0; i < 200; i++, date += inc) {
                tz.getOffset(date, false, offsets);
                if (offsets[1] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static RuleBasedTimeZone createRBTZ(BasicTimeZone btz, long start) {
        TimeZoneRule[] rules = btz.getTimeZoneRules(start);
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone("RBTZ:btz.getID()", (InitialTimeZoneRule)rules[0]);
        for (int i = 1; i < rules.length; i++) {
            rbtz.addTransitionRule(rules[i]);
        }
        return rbtz;
     }

    @Test
    public void TestGetWindowsID() {
        String[][] TESTDATA = {
            {"America/New_York",        "Eastern Standard Time"},
            {"America/Montreal",        "Eastern Standard Time"},
            {"America/Los_Angeles",     "Pacific Standard Time"},
            {"America/Vancouver",       "Pacific Standard Time"},
            {"Asia/Shanghai",           "China Standard Time"},
            {"Asia/Chongqing",          "China Standard Time"},
            {"America/Indianapolis",    "US Eastern Standard Time"},            // CLDR canonical name
            {"America/Indiana/Indianapolis",    "US Eastern Standard Time"},    // tzdb canonical name
            {"Asia/Khandyga",           "Yakutsk Standard Time"},
            {"Australia/Eucla",         "Aus Central W. Standard Time"}, // Now Windows does have a mapping
            {"Bogus",                   null},
        };

        for (String[] data : TESTDATA) {
            String winID = TimeZone.getWindowsID(data[0]);
            assertEquals("Fail: ID=" + data[0], data[1], winID);
        }
    }

    @Test
    public void TestGetIDForWindowsID() {
        final String[][] TESTDATA = {
            {"Eastern Standard Time",   null,   "America/New_York"},
            {"Eastern Standard Time",   "US",   "America/New_York"},
            {"Eastern Standard Time",   "CA",   "America/Toronto"},
            {"Eastern Standard Time",   "CN",   "America/New_York"},
            {"China Standard Time",     null,   "Asia/Shanghai"},
            {"China Standard Time",     "CN",   "Asia/Shanghai"},
            {"China Standard Time",     "HK",   "Asia/Hong_Kong"},
            {"Mid-Atlantic Standard Time",  null,   null}, // No tz database mapping
            {"Bogus",                   null,   null},
        };

        for (String[] data : TESTDATA) {
            String id = TimeZone.getIDForWindowsID(data[0], data[1]);
            assertEquals("Fail: Windows ID=" + data[0] + ", Region=" + data[1],
                    data[2], id);
        }
    }
}

//eof
