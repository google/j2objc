/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2000-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.calendar;

import java.text.FieldPosition;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import android.icu.impl.CalendarAstronomer;
import android.icu.impl.LocaleUtility;
import android.icu.impl.ZoneMeta;
import android.icu.text.DateFormat;
import android.icu.text.DateFormatSymbols;
import android.icu.text.SimpleDateFormat;
import android.icu.util.BuddhistCalendar;
import android.icu.util.Calendar;
import android.icu.util.ChineseCalendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.JapaneseCalendar;
import android.icu.util.TaiwanCalendar;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.ULocale;

/**
 * @summary Tests of new functionality in IBMCalendar
 */
public class IBMCalendarTest extends CalendarTestFmwk {
    /**
     * Test weekend support in IBMCalendar.
     *
     * NOTE: This test will have to be updated when the isWeekend() etc.
     *       API is finalized later.
     *
     *       In particular, the test will have to be rewritten to instantiate
     *       a Calendar in the given locale (using getInstance()) and call
     *       that Calendar's isWeekend() etc. methods.
     */
    @Test
    public void TestWeekend() {
        SimpleDateFormat fmt = new SimpleDateFormat("EEE MMM dd yyyy G HH:mm:ss.SSS");

        // NOTE
        // This test tests for specific locale data.  This is probably okay
        // as far as US data is concerned, but if the Arabic/Yemen data
        // changes, this test will have to be updated.

        // Test specific days
        Object[] DATA1 = {
            Locale.US, new int[] { // Saturday:Sunday
                2000, Calendar.MARCH, 17, 23,  0, 0, // Fri 23:00
                2000, Calendar.MARCH, 18,  0, -1, 0, // Fri 23:59:59.999
                2000, Calendar.MARCH, 18,  0,  0, 1, // Sat 00:00
                2000, Calendar.MARCH, 18, 15,  0, 1, // Sat 15:00
                2000, Calendar.MARCH, 19, 23,  0, 1, // Sun 23:00
                2000, Calendar.MARCH, 20,  0, -1, 1, // Sun 23:59:59.999
                2000, Calendar.MARCH, 20,  0,  0, 0, // Mon 00:00
                2000, Calendar.MARCH, 20,  8,  0, 0, // Mon 08:00
            },
            new Locale("ar", "OM"), new int[] { // Friday:Saturday
                2000, Calendar.MARCH, 15, 23,  0, 0, // Wed 23:00
                2000, Calendar.MARCH, 16,  0, -1, 0, // Wed 23:59:59.999
                2000, Calendar.MARCH, 16,  0,  0, 0, // Thu 00:00
                2000, Calendar.MARCH, 16, 15,  0, 0, // Thu 15:00
                2000, Calendar.MARCH, 17, 23,  0, 1, // Fri 23:00
                2000, Calendar.MARCH, 18,  0, -1, 1, // Fri 23:59:59.999
                2000, Calendar.MARCH, 18,  0,  0, 1, // Sat 00:00
                2000, Calendar.MARCH, 18,  8,  0, 1, // Sat 08:00
            },
        };

        // Test days of the week
        Object[] DATA2 = {
            Locale.US, new int[] {
                Calendar.MONDAY,   Calendar.WEEKDAY,
                Calendar.FRIDAY,   Calendar.WEEKDAY,
                Calendar.SATURDAY, Calendar.WEEKEND,
                Calendar.SUNDAY,   Calendar.WEEKEND,
            },
            new Locale("ar", "OM"), new int[] { // Friday:Saturday
                Calendar.WEDNESDAY,Calendar.WEEKDAY,
                Calendar.THURSDAY, Calendar.WEEKDAY,
                Calendar.FRIDAY,   Calendar.WEEKEND,
                Calendar.SATURDAY, Calendar.WEEKEND,
            },
            new Locale("hi", "IN"), new int[] { // Sunday only
                Calendar.MONDAY,   Calendar.WEEKDAY,
                Calendar.FRIDAY,   Calendar.WEEKDAY,
                Calendar.SATURDAY, Calendar.WEEKDAY,
                Calendar.SUNDAY,   Calendar.WEEKEND,
            },
        };

        // We only test the getDayOfWeekType() and isWeekend() APIs.
        // The getWeekendTransition() API is tested indirectly via the
        // isWeekend() API, which calls it.

        for (int i1=0; i1<DATA1.length; i1+=2) {
            Locale loc = (Locale)DATA1[i1];
            int[] data = (int[]) DATA1[i1+1];
            Calendar cal = Calendar.getInstance(loc);
            logln("Locale: " + loc);
            for (int i=0; i<data.length; i+=6) {
                cal.clear();
                cal.set(data[i], data[i+1], data[i+2], data[i+3], 0, 0);
                if (data[i+4] != 0) {
                    cal.setTime(new Date(cal.getTime().getTime() + data[i+4]));
                }
                boolean isWeekend = cal.isWeekend();
                boolean ok = isWeekend == (data[i+5] != 0);
                if (ok) {
                    logln("Ok:   " + fmt.format(cal.getTime()) + " isWeekend=" + isWeekend);
                } else {
                    errln("FAIL: " + fmt.format(cal.getTime()) + " isWeekend=" + isWeekend +
                          ", expected=" + (!isWeekend));
                }
            }
        }

        for (int i2=0; i2<DATA2.length; i2+=2) {
            Locale loc = (Locale)DATA2[i2];
            int[] data = (int[]) DATA2[i2+1];
            logln("Locale: " + loc);
            Calendar cal = Calendar.getInstance(loc);
            for (int i=0; i<data.length; i+=2) {
                int type = cal.getDayOfWeekType(data[i]);
                int exp  = data[i+1];
                if (type == exp) {
                    logln("Ok:   DOW " + data[i] + " type=" + type);
                } else {
                    errln("FAIL: DOW " + data[i] + " type=" + type +
                          ", expected=" + exp);
                }
            }
        }
    }

    /**
     * Run a test of a quasi-Gregorian calendar.  This is a calendar
     * that behaves like a Gregorian but has different year/era mappings.
     * The int[] data array should have the format:
     *
     * { era, year, gregorianYear, month, dayOfMonth, ... }
     */
    void quasiGregorianTest(Calendar cal, int[] data) {
        // As of JDK 1.4.1_01, using the Sun JDK GregorianCalendar as
        // a reference throws us off by one hour.  This is most likely
        // due to the JDK 1.4 incorporation of historical time zones.
        //java.util.Calendar grego = java.util.Calendar.getInstance();
        Calendar grego = Calendar.getInstance();
        for (int i=0; i<data.length; ) {
            int era = data[i++];
            int year = data[i++];
            int gregorianYear = data[i++];
            int month = data[i++];
            int dayOfMonth = data[i++];

            grego.clear();
            grego.set(gregorianYear, month, dayOfMonth);
            Date D = grego.getTime();

            cal.clear();
            cal.set(Calendar.ERA, era);
            cal.set(year, month, dayOfMonth);
            Date d = cal.getTime();
            if (d.equals(D)) {
                logln("OK: " + era + ":" + year + "/" + (month+1) + "/" + dayOfMonth +
                      " => " + d);
            } else {
                errln("Fail: " + era + ":" + year + "/" + (month+1) + "/" + dayOfMonth +
                      " => " + d + ", expected " + D);
            }

            cal.clear();
            cal.setTime(D);
            int e = cal.get(Calendar.ERA);
            int y = cal.get(Calendar.YEAR);
            if (y == year && e == era) {
                logln("OK: " + D + " => " + cal.get(Calendar.ERA) + ":" +
                      cal.get(Calendar.YEAR) + "/" +
                      (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DATE));
            } else {
                logln("Fail: " + D + " => " + cal.get(Calendar.ERA) + ":" +
                      cal.get(Calendar.YEAR) + "/" +
                      (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DATE) +
                      ", expected " + era + ":" + year + "/" + (month+1) + "/" +
                      dayOfMonth);
            }
        }
    }

    /**
     * Verify that BuddhistCalendar shifts years to Buddhist Era but otherwise
     * behaves like GregorianCalendar.
     */
    @Test
    public void TestBuddhist() {
        quasiGregorianTest(new BuddhistCalendar(),
                           new int[] {
                               // BE 2542 == 1999 CE
                               0, 2542, 1999, Calendar.JUNE, 4
                           });
    }

    @Test
    public void TestBuddhistCoverage() {
    {
        // new BuddhistCalendar(ULocale)
        BuddhistCalendar cal = new BuddhistCalendar(ULocale.getDefault());
        if(cal == null){
            errln("could not create BuddhistCalendar with ULocale");
        }
    }

    {
        // new BuddhistCalendar(TimeZone,ULocale)
        BuddhistCalendar cal = new BuddhistCalendar(TimeZone.getDefault(),ULocale.getDefault());
        if(cal == null){
            errln("could not create BuddhistCalendar with TimeZone ULocale");
        }
    }

    {
        // new BuddhistCalendar(TimeZone)
        BuddhistCalendar cal = new BuddhistCalendar(TimeZone.getDefault());
        if(cal == null){
            errln("could not create BuddhistCalendar with TimeZone");
        }
    }

    {
        // new BuddhistCalendar(Locale)
        BuddhistCalendar cal = new BuddhistCalendar(Locale.getDefault());
        if(cal == null){
            errln("could not create BuddhistCalendar with Locale");
        }
    }

    {
        // new BuddhistCalendar(TimeZone, Locale)
        BuddhistCalendar cal = new BuddhistCalendar(TimeZone.getDefault(), Locale.getDefault());
        if(cal == null){
            errln("could not create BuddhistCalendar with TimeZone and Locale");
        }
    }

    {
        // new BuddhistCalendar(Date)
        BuddhistCalendar cal = new BuddhistCalendar(new Date());
        if(cal == null){
            errln("could not create BuddhistCalendar with Date");
        }
    }

    {
        // new BuddhistCalendar(int year, int month, int date)
        BuddhistCalendar cal = new BuddhistCalendar(2543, Calendar.MAY, 22);
        if(cal == null){
            errln("could not create BuddhistCalendar with year,month,data");
        }
    }

    {
        // new BuddhistCalendar(int year, int month, int date, int hour, int minute, int second)
        BuddhistCalendar cal = new BuddhistCalendar(2543, Calendar.MAY, 22, 1, 1, 1);
        if(cal == null){
            errln("could not create BuddhistCalendar with year,month,date,hour,minute,second");
        }
    }

    {
        // data
        BuddhistCalendar cal = new BuddhistCalendar(2543, Calendar.MAY, 22);
        Date time = cal.getTime();

        String[] calendarLocales = {
        "th_TH"
        };

        String[] formatLocales = {
        "en", "ar", "hu", "th"
        };

        for (int i = 0; i < calendarLocales.length; ++i) {
        String calLocName = calendarLocales[i];
        Locale calLocale = LocaleUtility.getLocaleFromName(calLocName);
        cal = new BuddhistCalendar(calLocale);

        for (int j = 0; j < formatLocales.length; ++j) {
            String locName = formatLocales[j];
            Locale formatLocale = LocaleUtility.getLocaleFromName(locName);
            DateFormat format = DateFormat.getDateTimeInstance(cal, DateFormat.FULL, DateFormat.FULL, formatLocale);
            logln(calLocName + "/" + locName + " --> " + format.format(time));
        }
        }
    }
    }

    /**
     * Test limits of the Buddhist calendar.
     */
    @Test
    public void TestBuddhistLimits() {
        // Final parameter is either number of days, if > 0, or test
        // duration in seconds, if < 0.
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 1);
        BuddhistCalendar buddhist = new BuddhistCalendar();
        doLimitsTest(buddhist, null, cal.getTime());
        doTheoreticalLimitsTest(buddhist, false);
    }

    /**
     * Default calendar for Thai (Ticket#6302)
     */
    @Test
    public void TestThaiDefault() {
        // Buddhist calendar is used as the default calendar for
        // Thai locale
        Calendar cal = Calendar.getInstance(new ULocale("th_TH"));
        String type = cal.getType();
        // Android patch: Force default Gregorian calendar.
        if (!type.equals("gregorian")) {
            errln("FAIL: Gregorian calendar is not returned for locale " + cal.toString());
        }
        // Android patch end.
    }

    /**
     * Verify that TaiwanCalendar shifts years to Minguo Era but otherwise
     * behaves like GregorianCalendar.
     */
    @Test
    public void TestTaiwan() {
        quasiGregorianTest(new TaiwanCalendar(),
                           new int[] {
                               TaiwanCalendar.BEFORE_MINGUO, 8, 1904, Calendar.FEBRUARY, 29,
                               TaiwanCalendar.MINGUO, 1, 1912, Calendar.JUNE, 4,
                               TaiwanCalendar.MINGUO, 3, 1914, Calendar.FEBRUARY, 12,
                               TaiwanCalendar.MINGUO, 96,2007, Calendar.FEBRUARY, 12,
                           });
    }

    /**
     * Test limits of the Taiwan calendar.
     */
    @Test
    public void TestTaiwanLimits() {
        // Final parameter is either number of days, if > 0, or test
        // duration in seconds, if < 0.
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 1);
        TaiwanCalendar taiwan = new TaiwanCalendar();
        doLimitsTest(taiwan, null, cal.getTime());
        doTheoreticalLimitsTest(taiwan, false);
    }

    @Test
    public void TestTaiwanCoverage() {
    {
        // new TaiwanCalendar(ULocale)
        TaiwanCalendar cal = new TaiwanCalendar(ULocale.getDefault());
        if(cal == null){
            errln("could not create TaiwanCalendar with ULocale");
        }
    }

    {
        // new TaiwanCalendar(TimeZone,ULocale)
        TaiwanCalendar cal = new TaiwanCalendar(TimeZone.getDefault(),ULocale.getDefault());
        if(cal == null){
            errln("could not create TaiwanCalendar with TimeZone ULocale");
        }
    }

    {
        // new TaiwanCalendar(TimeZone)
        TaiwanCalendar cal = new TaiwanCalendar(TimeZone.getDefault());
        if(cal == null){
            errln("could not create TaiwanCalendar with TimeZone");
        }
    }

    {
        // new TaiwanCalendar(Locale)
        TaiwanCalendar cal = new TaiwanCalendar(Locale.getDefault());
        if(cal == null){
            errln("could not create TaiwanCalendar with Locale");
        }
    }

    {
        // new TaiwanCalendar(TimeZone, Locale)
        TaiwanCalendar cal = new TaiwanCalendar(TimeZone.getDefault(), Locale.getDefault());
        if(cal == null){
            errln("could not create TaiwanCalendar with TimeZone and Locale");
        }
    }

    {
        // new TaiwanCalendar(Date)
        TaiwanCalendar cal = new TaiwanCalendar(new Date());
        if(cal == null){
            errln("could not create TaiwanCalendar with Date");
        }
    }

    {
        // new TaiwanCalendar(int year, int month, int date)
        TaiwanCalendar cal = new TaiwanCalendar(34, Calendar.MAY, 22);
        if(cal == null){
            errln("could not create TaiwanCalendar with year,month,data");
        }
    }

    {
        // new TaiwanCalendar(int year, int month, int date, int hour, int minute, int second)
        TaiwanCalendar cal = new TaiwanCalendar(34, Calendar.MAY, 22, 1, 1, 1);
        if(cal == null){
            errln("could not create TaiwanCalendar with year,month,date,hour,minute,second");
        }
    }

    {
        // data
        TaiwanCalendar cal = new TaiwanCalendar(34, Calendar.MAY, 22);
        Date time = cal.getTime();

        String[] calendarLocales = {
        "en","zh"
        };

        String[] formatLocales = {
        "en", "ar", "hu", "th"
        };

        for (int i = 0; i < calendarLocales.length; ++i) {
        String calLocName = calendarLocales[i];
        Locale calLocale = LocaleUtility.getLocaleFromName(calLocName);
        cal = new TaiwanCalendar(calLocale);

        for (int j = 0; j < formatLocales.length; ++j) {
            String locName = formatLocales[j];
            Locale formatLocale = LocaleUtility.getLocaleFromName(locName);
            DateFormat format = DateFormat.getDateTimeInstance(cal, DateFormat.FULL, DateFormat.FULL, formatLocale);
            logln(calLocName + "/" + locName + " --> " + format.format(time));
        }
        }
    }
    }

    /**
     * Verify that JapaneseCalendar shifts years to Japanese Eras but otherwise
     * behaves like GregorianCalendar.
     */
    @Test
    public void TestJapanese() {
        // First make sure this test works for GregorianCalendar
        int[] control = {
            GregorianCalendar.AD, 1868, 1868, Calendar.SEPTEMBER, 8,
            GregorianCalendar.AD, 1868, 1868, Calendar.SEPTEMBER, 9,
            GregorianCalendar.AD, 1869, 1869, Calendar.JUNE, 4,
            GregorianCalendar.AD, 1912, 1912, Calendar.JULY, 29,
            GregorianCalendar.AD, 1912, 1912, Calendar.JULY, 30,
            GregorianCalendar.AD, 1912, 1912, Calendar.AUGUST, 1,
        };
        quasiGregorianTest(new GregorianCalendar(), control);

        int[] data = {
            JapaneseCalendar.MEIJI, 1, 1868, Calendar.SEPTEMBER, 8,
            JapaneseCalendar.MEIJI, 1, 1868, Calendar.SEPTEMBER, 9,
            JapaneseCalendar.MEIJI, 2, 1869, Calendar.JUNE, 4,
            JapaneseCalendar.MEIJI, 45, 1912, Calendar.JULY, 29,
            JapaneseCalendar.TAISHO, 1, 1912, Calendar.JULY, 30,
            JapaneseCalendar.TAISHO, 1, 1912, Calendar.AUGUST, 1,
        };
        quasiGregorianTest(new JapaneseCalendar(), data);
    }

    /**
     * Test limits of the Gregorian calendar.
     */
    @Test
    public void TestGregorianLimits() {
        // Final parameter is either number of days, if > 0, or test
        // duration in seconds, if < 0.
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JANUARY, 1);
        GregorianCalendar gregorian = new GregorianCalendar();
        doLimitsTest(gregorian, null, cal.getTime());
        doTheoreticalLimitsTest(gregorian, false);
    }

    /**
     * Test behavior of fieldDifference around leap years.  Also test a large
     * field difference to check binary search.
     */
    @Test
    public void TestLeapFieldDifference() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.FEBRUARY, 29);
        Date date2004 = cal.getTime();
        cal.set(2000, Calendar.FEBRUARY, 29);
        Date date2000 = cal.getTime();
        int y = cal.fieldDifference(date2004, Calendar.YEAR);
        int d = cal.fieldDifference(date2004, Calendar.DAY_OF_YEAR);
        if (d == 0) {
            logln("Ok: 2004/Feb/29 - 2000/Feb/29 = " + y + " years, " + d + " days");
        } else {
            errln("FAIL: 2004/Feb/29 - 2000/Feb/29 = " + y + " years, " + d + " days");
        }
        cal.setTime(date2004);
        y = cal.fieldDifference(date2000, Calendar.YEAR);
        d = cal.fieldDifference(date2000, Calendar.DAY_OF_YEAR);
        if (d == 0) {
            logln("Ok: 2000/Feb/29 - 2004/Feb/29 = " + y + " years, " + d + " days");
        } else {
            errln("FAIL: 2000/Feb/29 - 2004/Feb/29 = " + y + " years, " + d + " days");
        }
        // Test large difference
        cal.set(2001, Calendar.APRIL, 5); // 2452005
        Date ayl = cal.getTime();
        cal.set(1964, Calendar.SEPTEMBER, 7); // 2438646
        Date asl = cal.getTime();
        d = cal.fieldDifference(ayl, Calendar.DAY_OF_MONTH);
        cal.setTime(ayl);
        int d2 = cal.fieldDifference(asl, Calendar.DAY_OF_MONTH);
        if (d == -d2 && d == 13359) {
            logln("Ok: large field difference symmetrical " + d);
        } else {
            logln("FAIL: large field difference incorrect " + d + ", " + d2 +
                  ", expect +/- 13359");
        }
    }

    /**
     * Test ms_MY "Malay (Malaysia)" locale.  Bug 1543.
     */
    @Test
    public void TestMalaysianInstance() {
        Locale loc = new Locale("ms", "MY");  // Malay (Malaysia)
        Calendar cal = Calendar.getInstance(loc);
        if(cal == null){
            errln("could not create Malaysian instance");
        }
    }

    /**
     * setFirstDayOfWeek and setMinimalDaysInFirstWeek may change the
     * field <=> time mapping, since they affect the interpretation of
     * the WEEK_OF_MONTH or WEEK_OF_YEAR fields.
     */
    @Test
    public void TestWeekShift() {
        Calendar cal = new GregorianCalendar(
                             TimeZone.getTimeZone("America/Los_Angeles"),
                             new Locale("en", "US"));
        cal.setTime(new Date(997257600000L)); // Wed Aug 08 01:00:00 PDT 2001
        // In pass one, change the first day of week so that the weeks
        // shift in August 2001.  In pass two, change the minimal days
        // in the first week so that the weeks shift in August 2001.
        //     August 2001
        // Su Mo Tu We Th Fr Sa
        //           1  2  3  4
        //  5  6  7  8  9 10 11
        // 12 13 14 15 16 17 18
        // 19 20 21 22 23 24 25
        // 26 27 28 29 30 31
        for (int pass=0; pass<2; ++pass) {
            if (pass==0) {
                cal.setFirstDayOfWeek(Calendar.WEDNESDAY);
                cal.setMinimalDaysInFirstWeek(4);
            } else {
                cal.setFirstDayOfWeek(Calendar.SUNDAY);
                cal.setMinimalDaysInFirstWeek(4);
            }
            cal.add(Calendar.DATE, 1); // Force recalc
            cal.add(Calendar.DATE, -1);

            Date time1 = cal.getTime(); // Get time -- should not change

            // Now change a week parameter and then force a recalc.
            // The bug is that the recalc should not be necessary --
            // calendar should do so automatically.
            if (pass==0) {
                cal.setFirstDayOfWeek(Calendar.THURSDAY);
            } else {
                cal.setMinimalDaysInFirstWeek(5);
            }

            int woy1 = cal.get(Calendar.WEEK_OF_YEAR);
            int wom1 = cal.get(Calendar.WEEK_OF_MONTH);

            cal.add(Calendar.DATE, 1); // Force recalc
            cal.add(Calendar.DATE, -1);

            int woy2 = cal.get(Calendar.WEEK_OF_YEAR);
            int wom2 = cal.get(Calendar.WEEK_OF_MONTH);

            Date time2 = cal.getTime();

            if (!time1.equals(time2)) {
                errln("FAIL: shifting week should not alter time");
            } else {
                logln(time1.toString());
            }
            if (woy1 == woy2 && wom1 == wom2) {
                logln("Ok: WEEK_OF_YEAR: " + woy1 +
                      ", WEEK_OF_MONTH: " + wom1);
            } else {
                errln("FAIL: WEEK_OF_YEAR: " + woy1 + " => " + woy2 +
                      ", WEEK_OF_MONTH: " + wom1 + " => " + wom2 +
                      " after week shift");
            }
        }
    }

    /**
     * Make sure that when adding a day, we actually wind up in a
     * different day.  The DST adjustments we use to keep the hour
     * constant across DST changes can backfire and change the day.
     */
    @Test
    public void TestTimeZoneTransitionAdd() {
        Locale locale = Locale.US; // could also be CHINA
        SimpleDateFormat dateFormat =
            new SimpleDateFormat("MM/dd/yyyy HH:mm z", locale);

        String tz[] = TimeZone.getAvailableIDs();

        for (int z=0; z<tz.length; ++z) {
            TimeZone t = TimeZone.getTimeZone(tz[z]);
            dateFormat.setTimeZone(t);

            Calendar cal = Calendar.getInstance(t, locale);
            cal.clear();
            // Scan the year 2003, overlapping the edges of the year
            cal.set(Calendar.YEAR, 2002);
            cal.set(Calendar.MONTH, Calendar.DECEMBER);
            cal.set(Calendar.DAY_OF_MONTH, 25);

            for (int i=0; i<365+10; ++i) {
                Date yesterday = cal.getTime();
                int yesterday_day = cal.get(Calendar.DAY_OF_MONTH);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                if (yesterday_day == cal.get(Calendar.DAY_OF_MONTH)) {
                    errln(tz[z] + " " +
                          dateFormat.format(yesterday) + " +1d= " +
                          dateFormat.format(cal.getTime()));
                }
            }
        }
    }

    @Test
    public void TestJB1684() {
        class TestData {
            int year;
            int month;
            int date;
            int womyear;
            int wommon;
            int wom;
            int dow;
            String data;
            String normalized;

            public TestData(int year, int month, int date,
                            int womyear, int wommon, int wom, int dow,
                            String data, String normalized) {
                this.year = year;
                this.month = month-1;
                this.date = date;
                this.womyear = womyear;
                this.wommon = wommon-1;
                this.wom = wom;
                this.dow = dow;
                this.data = data; // year, month, week of month, day
                this.normalized = data;
                if (normalized != null) this.normalized = normalized;
            }
        }

        //      July 2001            August 2001           January 2002
        // Su Mo Tu We Th Fr Sa  Su Mo Tu We Th Fr Sa  Su Mo Tu We Th Fr Sa
        //  1  2  3  4  5  6  7            1  2  3  4         1  2  3  4  5
        //  8  9 10 11 12 13 14   5  6  7  8  9 10 11   6  7  8  9 10 11 12
        // 15 16 17 18 19 20 21  12 13 14 15 16 17 18  13 14 15 16 17 18 19
        // 22 23 24 25 26 27 28  19 20 21 22 23 24 25  20 21 22 23 24 25 26
        // 29 30 31              26 27 28 29 30 31     27 28 29 30 31
        TestData[] tests = {
            new TestData(2001, 8,  6,  2001,8,2,Calendar.MONDAY,    "2001 08 02 Mon", null),
            new TestData(2001, 8,  7,  2001,8,2,Calendar.TUESDAY,   "2001 08 02 Tue", null),
            new TestData(2001, 8,  5,/*12,*/ 2001,8,2,Calendar.SUNDAY,    "2001 08 02 Sun", null),
            new TestData(2001, 8,6, /*7,  30,*/ 2001,7,6,Calendar.MONDAY,    "2001 07 06 Mon", "2001 08 02 Mon"),
            new TestData(2001, 8,7, /*7,  31,*/ 2001,7,6,Calendar.TUESDAY,   "2001 07 06 Tue", "2001 08 02 Tue"),
            new TestData(2001, 8,  5,  2001,7,6,Calendar.SUNDAY,    "2001 07 06 Sun", "2001 08 02 Sun"),
            new TestData(2001, 7,  30, 2001,8,1,Calendar.MONDAY,    "2001 08 01 Mon", "2001 07 05 Mon"),
            new TestData(2001, 7,  31, 2001,8,1,Calendar.TUESDAY,   "2001 08 01 Tue", "2001 07 05 Tue"),
            new TestData(2001, 7,29, /*8,  5,*/  2001,8,1,Calendar.SUNDAY,    "2001 08 01 Sun", "2001 07 05 Sun"),
            new TestData(2001, 12, 31, 2001,12,6,Calendar.MONDAY,   "2001 12 06 Mon", null),
            new TestData(2002, 1,  1,  2002,1,1,Calendar.TUESDAY,   "2002 01 01 Tue", null),
            new TestData(2002, 1,  2,  2002,1,1,Calendar.WEDNESDAY, "2002 01 01 Wed", null),
            new TestData(2002, 1,  3,  2002,1,1,Calendar.THURSDAY,  "2002 01 01 Thu", null),
            new TestData(2002, 1,  4,  2002,1,1,Calendar.FRIDAY,    "2002 01 01 Fri", null),
            new TestData(2002, 1,  5,  2002,1,1,Calendar.SATURDAY,  "2002 01 01 Sat", null),
            new TestData(2001,12,30, /*2002, 1,  6,*/  2002,1,1,Calendar.SUNDAY,    "2002 01 01 Sun", "2001 12 06 Sun"),
        };

        int pass = 0, error = 0, warning = 0;

        final String pattern = "yyyy MM WW EEE";
        GregorianCalendar cal = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setCalendar(cal);

        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.setMinimalDaysInFirstWeek(1);

        for (int i = 0; i < tests.length; ++i) {
            TestData test = tests[i];
            log("\n-----\nTesting round trip of " + test.year +
                  " " + (test.month + 1) +
                  " " + test.date +
                  " (written as) " + test.data);

            cal.clear();
            cal.set(test.year, test.month, test.date);
            Date ms = cal.getTime();

            cal.clear();
            cal.set(Calendar.YEAR, test.womyear);
            cal.set(Calendar.MONTH, test.wommon);
            cal.set(Calendar.WEEK_OF_MONTH, test.wom);
            cal.set(Calendar.DAY_OF_WEEK, test.dow);
            Date ms2 = cal.getTime();

            if (!ms2.equals(ms)) {
                log("\nError: GregorianCalendar.DOM gave " + ms +
                    "\n       GregorianCalendar.WOM gave " + ms2);
                error++;
            } else {
                pass++;
            }

            ms2 = null;
            try {
                ms2 = sdf.parse(test.data);
            }
            catch (ParseException e) {
                errln("parse exception: " + e);
            }

            if (!ms2.equals(ms)) {
                log("\nError: GregorianCalendar gave      " + ms +
                    "\n       SimpleDateFormat.parse gave " + ms2);
                error++;
            } else {
                pass++;
            }

            String result = sdf.format(ms);
            if (!result.equals(test.normalized)) {
                log("\nWarning: format of '" + test.data + "' gave" +
                    "\n                   '" + result + "'" +
                    "\n          expected '" + test.normalized + "'");
                warning++;
            } else {
                pass++;
            }

            Date ms3 = null;
            try {
                ms3 = sdf.parse(result);
            }
            catch (ParseException e) {
                errln("parse exception 2: " + e);
            }

            if (!ms3.equals(ms)) {
                error++;
                log("\nError: Re-parse of '" + result + "' gave time of " +
                    "\n        " + ms3 +
                    "\n    not " + ms);
            } else {
                pass++;
            }
        }
        String info = "\nPassed: " + pass + ", Warnings: " + warning + ", Errors: " + error;
        if (error > 0) {
            errln(info);
        } else {
            logln(info);
        }
    }

    /**
     * Test the ZoneMeta API.
     */
    @Test
    public void TestZoneMeta() {
        // Test index by country API

        // Format: {country, zone1, zone2, ..., zoneN}
        String COUNTRY[][] = { {""},
                               {"US", "America/Los_Angeles", "PST"} };
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<COUNTRY.length; ++i) {
            Set<String> a = ZoneMeta.getAvailableIDs(SystemTimeZoneType.ANY, COUNTRY[i][0], null);
            buf.setLength(0);
            buf.append("Country \"" + COUNTRY[i][0] + "\": [");
            // Use bitmask to track which of the expected zones we see
            int mask = 0;
            boolean first = true;
            for (String z : a) {
                if (first) {
                    first = false;
                } else {
                    buf.append(", ");
                }
                buf.append(z);
                for (int k = 1; k < COUNTRY[i].length; ++k) {
                    if ((mask & (1 << k)) == 0 && z.equals(COUNTRY[i][k])) {
                        mask |= (1 << k);
                    }
                }
            }
            buf.append("]");
            mask >>= 1;
            // Check bitmask to see if we saw all expected zones
            if (mask == (1 << (COUNTRY[i].length-1))-1) {
                logln(buf.toString());
            } else {
                errln(buf.toString());
            }
        }

        // Test equivalent IDs API

        int n = ZoneMeta.countEquivalentIDs("PST");
        boolean ok = false;
        buf.setLength(0);
        buf.append("Equivalent to PST: ");
        for (int i=0; i<n; ++i) {
            String id = ZoneMeta.getEquivalentID("PST", i);
            if (id.equals("America/Los_Angeles")) {
                ok = true;
            }
            if (i!=0) buf.append(", ");
            buf.append(id);
        }
        if (ok) {
            logln(buf.toString());
        } else {
            errln(buf.toString());
        }
    }

    @Test
    public void TestComparable() {
    GregorianCalendar c0 = new GregorianCalendar();
    GregorianCalendar c1 = new GregorianCalendar();
    c1.add(Calendar.DAY_OF_MONTH, 1);
    if (c0.compareTo(c1) >= 0) {
        errln("calendar " + c0 + " not < " + c1);
    }
    c0.add(Calendar.MONTH, 1);
    if (c0.compareTo(c1) <= 0) {
        errln("calendar " + c0 + " not > " + c1);
    }

    c0.setTimeInMillis(c1.getTimeInMillis());
    if (c0.compareTo(c1) != 0) {
        errln("calendar " + c0 + " not == " + c1);
    }

    }

    /**
     * Miscellaneous tests to increase coverage.
     */
    @Test
    public void TestCoverage() {
        // BuddhistCalendar
        BuddhistCalendar bcal = new BuddhistCalendar();
        /*int i =*/ bcal.getMinimum(Calendar.ERA);
        bcal.add(Calendar.YEAR, 1);
        bcal.add(Calendar.MONTH, 1);
        /*Date d = */bcal.getTime();

        // CalendarAstronomer
        // (This class should probably be made package-private.)
        CalendarAstronomer astro = new CalendarAstronomer();
        /*String s = */astro.local(0);

        // ChineseCalendar
        ChineseCalendar ccal = new ChineseCalendar(TimeZone.getDefault(),
                                                   Locale.getDefault());
        ccal.add(Calendar.MONTH, 1);
        ccal.add(Calendar.YEAR, 1);
        ccal.roll(Calendar.MONTH, 1);
        ccal.roll(Calendar.YEAR, 1);
        ccal.getTime();

        // ICU 2.6
        Calendar cal = Calendar.getInstance(Locale.US);
        logln(cal.toString());
        logln(cal.getDisplayName(Locale.US));
        int weekendOnset=-1;
        int weekendCease=-1;
        for (int i=Calendar.SUNDAY; i<=Calendar.SATURDAY; ++i) {
            if (cal.getDayOfWeekType(i) == Calendar.WEEKEND_ONSET) {
                weekendOnset = i;
            }
            if (cal.getDayOfWeekType(i) == Calendar.WEEKEND_CEASE) {
                weekendCease = i;
            }
        }
        // can't call this unless we get a transition day (unusual),
        // but make the call anyway for coverage reasons
        try {
            /*int x=*/ cal.getWeekendTransition(weekendOnset);
            /*int x=*/ cal.getWeekendTransition(weekendCease);
        } catch (IllegalArgumentException e) {}
        /*int x=*/ cal.isWeekend(new Date());

        // new GregorianCalendar(ULocale)
        GregorianCalendar gcal = new GregorianCalendar(ULocale.getDefault());
        if(gcal==null){
            errln("could not create GregorianCalendar with ULocale");
        } else {
            logln("Calendar display name: " + gcal.getDisplayName(ULocale.getDefault()));
        }

        //cover getAvailableULocales
        final ULocale[] locales = Calendar.getAvailableULocales();
        long count = locales.length;
        if (count == 0)
            errln("getAvailableULocales return empty list");
        logln("" + count + " available ulocales in Calendar.");

        // Jitterbug 4451, for coverage
        class StubCalendar extends Calendar{
            /**
             * For serialization
             */
            private static final long serialVersionUID = -4558903444622684759L;

            @Override
            protected int handleGetLimit(int field, int limitType) {
                if (limitType == Calendar.LEAST_MAXIMUM) {
                    return 1;
                } else if (limitType == Calendar.GREATEST_MINIMUM) {
                    return 7;
                }
               return -1;
            }
            @Override
            protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
                if (useMonth) {
                    return eyear * 365 + month * 31;
                } else {
                    return eyear * 365;
                }
            }
            @Override
            protected int handleGetExtendedYear() {return 2017;}

            public void run(){
                if (Calendar.gregorianPreviousMonthLength(2000,2) != 29){
                    errln("Year 2000 Feb should have 29 days.");
                }
                long millis = Calendar.julianDayToMillis(Calendar.MAX_JULIAN);
                if(millis != Calendar.MAX_MILLIS){
                    errln("Did not get the expected value from julianDayToMillis. Got:" + millis);
                }
                DateFormat df = handleGetDateFormat("",Locale.getDefault());
                if (!df.equals(handleGetDateFormat("",ULocale.getDefault()))){
                    errln ("Calendar.handleGetDateFormat(String, Locale) should delegate to ( ,ULocale)");
                }
                if (!getType().equals("unknown")){
                    errln ("Calendar.getType() should be 'unknown'");
                }

                // Tests for complete coverage of Calendar functions.
                int julianDay = Calendar.millisToJulianDay(millis - 1);
                assertEquals("Julian max day -1", julianDay, Calendar.MAX_JULIAN - 1);

                DateFormat df1 = handleGetDateFormat("GG yyyy-d:MM", "option=xyz", Locale.getDefault());
                if (!df1.equals(handleGetDateFormat("GG yyyy-d:MM", "option=xyz", ULocale.getDefault()))){
                    errln ("Calendar.handleGetDateFormat(String, Locale) should delegate to ( ,ULocale)");
                }

                // Prove that the local overrides are used.
                int leastMsInDay = handleGetLimit(Calendar.MILLISECONDS_IN_DAY, Calendar.LEAST_MAXIMUM);
                assertEquals("getLimit test 1", leastMsInDay, 1);
                int maxMsInDay = handleGetLimit(Calendar.WEEK_OF_MONTH, Calendar.GREATEST_MINIMUM);
                assertEquals("getLimit test 2", 7, maxMsInDay);

                int febLeapLength = handleGetMonthLength(2020, Calendar.FEBRUARY);
                assertEquals("handleMonthLength", 31, febLeapLength);
                int exYear = handleGetExtendedYear();
                assertEquals("handleGetExtendeYear", exYear, 2017);
                int monthStart = handleComputeMonthStart(2016, 4, false);
                assertEquals("handleComputeMonthStart false", 735840, monthStart);
                monthStart = handleComputeMonthStart(2016, 4, true);
                assertEquals("handleComputeMonthStart true", 735964, monthStart);

                Calendar cal = Calendar.getInstance();
                cal.set(1980, 5, 2);
                this.setTime(cal.getTime());
                assertEquals("handleComputeFields: year set", 1980, get(YEAR));
                assertEquals("handleComputeFields: month set", 5, get(MONTH));
                assertEquals("handleComputeFields: day set", 2, get(DAY_OF_MONTH));
            }
        }
        StubCalendar stub = new StubCalendar();
        stub.run();
    }

    // Tests for jb 4541
    @Test
    public void TestJB4541() {
        ULocale loc = new ULocale("en_US");

        // !!! Shouldn't we have an api like this?
        // !!! Question: should this reflect those actually available in this copy of ICU, or
        // the list of types we assume is available?
        // String[] calTypes = Calendar.getAvailableTypes();
        final String[] calTypes = {
            "buddhist", "chinese", "coptic", "ethiopic", "gregorian", "hebrew",
            "islamic", "islamic-civil", "japanese", "roc"
        };

        // constructing a DateFormat with a locale indicating a calendar type should construct a
        // date format appropriate to that calendar
        final Date time = new Date();
        for (int i = 0; i < calTypes.length; ++i) {
            ULocale aLoc = loc.setKeywordValue("calendar", calTypes[i]);
            logln("locale: " + aLoc);

            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                           DateFormat.FULL,
                                                           aLoc);

            logln("df type: " + df.getClass().getName() + " loc: " + df.getLocale(ULocale.VALID_LOCALE));

            Calendar cal = df.getCalendar();
            assertEquals("calendar types", cal.getType(), calTypes[i]);
            DateFormat df2 = cal.getDateTimeFormat(DateFormat.FULL, DateFormat.FULL, ULocale.US);
            logln("df2 type: " + df2.getClass().getName() + " loc: " + df2.getLocale(ULocale.VALID_LOCALE));
            assertEquals("format results", df.format(time), df2.format(time));
        }

        // dateFormat.setCalendar should throw exception if wrong format for calendar
        if (false) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                           DateFormat.FULL,
                                                           new ULocale("en_US@calendar=chinese"));

            logln("dateformat type: " + df.getClass().getName());

            Calendar cal = Calendar.getInstance(new ULocale("en_US@calendar=chinese"));

            logln("calendar type: " + cal.getClass().getName());
        }
    }

    @Test
    public void TestTypes() {
        String[] locs = {
                "en_US_VALLEYGIRL",
                "en_US_VALLEYGIRL@collation=phonebook;calendar=japanese",
                "en_US_VALLEYGIRL@collation=phonebook;calendar=gregorian",
                "ja_JP@calendar=japanese",
                "th_TH@calendar=buddhist",
                "th-TH-u-ca-gregory",
                "ja_JP_TRADITIONAL",
                "th_TH_TRADITIONAL",
                "th_TH_TRADITIONAL@calendar=gregorian",
                "en_US",
                "th_TH",    // Default calendar for th_TH is buddhist
                "th",       // th's default region is TH and buddhist is used as default for TH
                "en_TH",    // Default calendar for any locales with region TH is buddhist
                "th_TH@calendar=iso8601",   // iso8601 calendar type
                "fr_CH",
                "fr_SA",
                "fr_CH@rg=sazzzz",
                "fr_CH@calendar=japanese;rg=sazzzz",
                "fr_TH@rg=SA",  // ignore malformed rg tag, use buddhist
                "th@rg=SA",		// ignore malformed rg tag, use buddhist
        };

        // Android patch: Force default Gregorian calendar.
        String[] types = {
                "gregorian",
                "japanese",
                "gregorian",
                "japanese",
                "buddhist",
                "gregorian",
                "japanese",
                "buddhist",
                "gregorian",
                "gregorian",
                "gregorian",
                "gregorian",
                "gregorian",
                "gregorian",    // iso8601 is a gregorian sub type
                "gregorian",
                "gregorian",
                "gregorian",
                "japanese",
                "gregorian",
                "gregorian",
        };
        // Android patch end.

        for (int i = 0; i < locs.length; i++) {
            Calendar cal = Calendar.getInstance(new ULocale(locs[i]));
            if (!cal.getType().equals(types[i])) {
                errln(locs[i] + " Calendar type " + cal.getType() + " instead of " + types[i]);
            }
        }
    }

    @Test
    public void TestISO8601() {
        final ULocale[] TEST_LOCALES = {
            new ULocale("en_US@calendar=iso8601"),
            new ULocale("en_US@calendar=Iso8601"),
            new ULocale("th_TH@calendar=iso8601"),
            new ULocale("ar_EG@calendar=iso8601")
        };

        final int[][] TEST_DATA = {
            // {<year>, <week# of Jan 1>, <week# year of Jan 1>}
            {2008, 1, 2008},
            {2009, 1, 2009},
            {2010, 53, 2009},
            {2011, 52, 2010},
            {2012, 52, 2011},
            {2013, 1, 2013},
            {2014, 1, 2014},
        };

        for (ULocale locale : TEST_LOCALES) {
            Calendar cal = Calendar.getInstance(locale);
            // No matter what locale is used, if calendar type is "iso8601",
            // calendar type must be Gregorian
            if (!cal.getType().equals("gregorian")) {
                errln("Error: Gregorian calendar is not used for locale: " + locale);
            }

            for (int[] data : TEST_DATA) {
                cal.set(data[0], Calendar.JANUARY, 1);
                int weekNum = cal.get(Calendar.WEEK_OF_YEAR);
                int weekYear = cal.get(Calendar.YEAR_WOY);

                if (weekNum != data[1] || weekYear != data[2]) {
                    errln("Error: Incorrect week of year on January 1st, " + data[0]
                            + " for locale " + locale
                            + ": Returned [weekNum=" + weekNum + ", weekYear=" + weekYear
                            + "], Expected [weekNum=" + data[1] + ", weekYear=" + data[2] + "]");
                }
            }
        }
    }

    private static class CalFields {
        private int year;
        private int month;
        private int day;
        private int hour;
        private int min;
        private int sec;
        private int ms;

        CalFields(int year, int month, int day, int hour, int min, int sec) {
            this(year, month, day, hour, min, sec, 0);
        }

        CalFields(int year, int month, int day, int hour, int min, int sec, int ms) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.hour = hour;
            this.min = min;
            this.sec = sec;
            this.ms = ms;
        }

        void setTo(Calendar cal) {
            cal.clear();
            cal.set(year,  month - 1, day, hour, min, sec);
            cal.set(Calendar.MILLISECOND, ms);
        }

        @Override
        public String toString() {
            return String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d", year, month, day, hour, min, sec, ms);
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof CalFields) {
                CalFields otr = (CalFields)other;
                return (year == otr.year
                        && month == otr.month
                        && day == otr.day
                        && hour == otr.hour
                        && min == otr.min
                        && sec == otr.sec
                        && ms == otr.ms);
            }
            return false;
        }

        boolean isEquivalentTo(Calendar cal) {
            return year == cal.get(Calendar.YEAR)
                    && month == cal.get(Calendar.MONTH) + 1
                    && day == cal.get(Calendar.DAY_OF_MONTH)
                    && hour == cal.get(Calendar.HOUR_OF_DAY)
                    && min == cal.get(Calendar.MINUTE)
                    && sec == cal.get(Calendar.SECOND)
                    && ms == cal.get(Calendar.MILLISECOND);
        }

        static CalFields createFrom(Calendar cal) {
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int sec = cal.get(Calendar.SECOND);

            return new CalFields(year, month, day, hour, min, sec);
        }
    }

    @Test
    public void TestAmbiguousWallTimeAPIs() {
        Calendar cal = Calendar.getInstance();

        assertEquals("Default repeated wall time option", cal.getRepeatedWallTimeOption(), Calendar.WALLTIME_LAST);
        assertEquals("Default skipped wall time option", cal.getSkippedWallTimeOption(), Calendar.WALLTIME_LAST);

        Calendar cal2 = (Calendar)cal.clone();

        assertTrue("Equality", cal2.equals(cal));
        assertTrue("Hash code", cal.hashCode() == cal2.hashCode());

        cal2.setRepeatedWallTimeOption(Calendar.WALLTIME_FIRST);
        cal2.setSkippedWallTimeOption(Calendar.WALLTIME_FIRST);

        assertFalse("Equality after mod", cal2.equals(cal));
        assertFalse("Hash code after mod", cal.hashCode() == cal2.hashCode());

        assertEquals("getRepeatedWallTimeOption after mod", cal2.getRepeatedWallTimeOption(), Calendar.WALLTIME_FIRST);
        assertEquals("getSkippedWallTimeOption after mod", cal2.getSkippedWallTimeOption(), Calendar.WALLTIME_FIRST);

        try {
            cal.setRepeatedWallTimeOption(Calendar.WALLTIME_NEXT_VALID);
            errln("IAE expected on setRepeatedWallTimeOption(WALLTIME_NEXT_VALID");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void TestRepeatedWallTime() {
        final Object[][] TESTDATA = {
            // Time zone            Input wall time                     WALLTIME_LAST in GMT                WALLTIME_FIRST in GMT
            {"America/New_York",    new CalFields(2011,11,6,0,59,59),   new CalFields(2011,11,6,4,59,59),   new CalFields(2011,11,6,4,59,59)},
            {"America/New_York",    new CalFields(2011,11,6,1,0,0),     new CalFields(2011,11,6,6,0,0),     new CalFields(2011,11,6,5,0,0)},
            {"America/New_York",    new CalFields(2011,11,6,1,0,1),     new CalFields(2011,11,6,6,0,1),     new CalFields(2011,11,6,5,0,1)},
            {"America/New_York",    new CalFields(2011,11,6,1,30,0),    new CalFields(2011,11,6,6,30,0),    new CalFields(2011,11,6,5,30,0)},
            {"America/New_York",    new CalFields(2011,11,6,1,59,59),   new CalFields(2011,11,6,6,59,59),   new CalFields(2011,11,6,5,59,59)},
            {"America/New_York",    new CalFields(2011,11,6,2,0,0),     new CalFields(2011,11,6,7,0,0),     new CalFields(2011,11,6,7,0,0)},
            {"America/New_York",    new CalFields(2011,11,6,2,0,1),     new CalFields(2011,11,6,7,0,1),     new CalFields(2011,11,6,7,0,1)},

            {"Australia/Lord_Howe", new CalFields(2011,4,3,1,29,59),    new CalFields(2011,4,2,14,29,59),   new CalFields(2011,4,2,14,29,59)},
            {"Australia/Lord_Howe", new CalFields(2011,4,3,1,30,0),     new CalFields(2011,4,2,15,0,0),     new CalFields(2011,4,2,14,30,0)},
            {"Australia/Lord_Howe", new CalFields(2011,4,3,1,45,0),     new CalFields(2011,4,2,15,15,0),    new CalFields(2011,4,2,14,45,0)},
            {"Australia/Lord_Howe", new CalFields(2011,4,3,1,59,59),    new CalFields(2011,4,2,15,29,59),   new CalFields(2011,4,2,14,59,59)},
            {"Australia/Lord_Howe", new CalFields(2011,4,3,2,0,0),      new CalFields(2011,4,2,15,30,0),    new CalFields(2011,4,2,15,30,0)},
            {"Australia/Lord_Howe", new CalFields(2011,4,3,2,0,1),      new CalFields(2011,4,2,15,30,1),    new CalFields(2011,4,2,15,30,1)},
        };

        Calendar calGMT = Calendar.getInstance(TimeZone.GMT_ZONE);

        Calendar calDefault = Calendar.getInstance();
        Calendar calLast = Calendar.getInstance();
        Calendar calFirst = Calendar.getInstance();

        calFirst.setRepeatedWallTimeOption(Calendar.WALLTIME_FIRST);
        calLast.setRepeatedWallTimeOption(Calendar.WALLTIME_LAST);

        for (Object[] test : TESTDATA) {
            String tzid = (String)test[0];
            TimeZone tz = TimeZone.getTimeZone(tzid);
            CalFields in = (CalFields)test[1];
            CalFields expLastGMT = (CalFields)test[2];
            CalFields expFirstGMT = (CalFields)test[3];

            // WALLTIME_LAST
            calLast.setTimeZone(tz);
            in.setTo(calLast);
            calGMT.setTimeInMillis(calLast.getTimeInMillis());
            CalFields outLastGMT = CalFields.createFrom(calGMT);
            if (!outLastGMT.equals(expLastGMT)) {
                errln("Fail: WALLTIME_LAST " + in + "[" + tzid + "] is parsed as " + outLastGMT + "[GMT]. Expected: " + expLastGMT + "[GMT]");
            }

            // default
            calDefault.setTimeZone(tz);
            in.setTo(calDefault);
            calGMT.setTimeInMillis(calDefault.getTimeInMillis());
            CalFields outDefGMT = CalFields.createFrom(calGMT);
            if (!outDefGMT.equals(expLastGMT)) {
                errln("Fail: (default) " + in + "[" + tzid + "] is parsed as " + outDefGMT + "[GMT]. Expected: " + expLastGMT + "[GMT]");
            }

            // WALLTIME_FIRST
            calFirst.setTimeZone(tz);
            in.setTo(calFirst);
            calGMT.setTimeInMillis(calFirst.getTimeInMillis());
            CalFields outFirstGMT = CalFields.createFrom(calGMT);
            if (!outFirstGMT.equals(expFirstGMT)) {
                errln("Fail: WALLTIME_FIRST " + in + "[" + tzid + "] is parsed as " + outFirstGMT + "[GMT]. Expected: " + expFirstGMT + "[GMT]");
            }
        }
    }

    @Test
    public void TestSkippedWallTime() {
        final Object[][] TESTDATA = {
            // Time zone            Input wall time                     Valid wall time?
            {"America/New_York",    new CalFields(2011,3,13,1,59,59),   true,
                //  WALLTIME_LAST in GMT                WALLTIME_FIRST in GMT           WALLTIME_NEXT_VALID in GMT
                new CalFields(2011,3,13,6,59,59),   new CalFields(2011,3,13,6,59,59),   new CalFields(2011,3,13,6,59,59)},

            {"America/New_York",    new CalFields(2011,3,13,2,0,0),     false,
                new CalFields(2011,3,13,7,0,0),     new CalFields(2011,3,13,6,0,0),     new CalFields(2011,3,13,7,0,0)},

            {"America/New_York",    new CalFields(2011,3,13,2,1,0),     false,
                new CalFields(2011,3,13,7,1,0),     new CalFields(2011,3,13,6,1,0),     new CalFields(2011,3,13,7,0,0)},

            {"America/New_York",    new CalFields(2011,3,13,2,30,0),    false,
                new CalFields(2011,3,13,7,30,0),    new CalFields(2011,3,13,6,30,0),    new CalFields(2011,3,13,7,0,0)},

            {"America/New_York",    new CalFields(2011,3,13,2,59,59),   false,
                new CalFields(2011,3,13,7,59,59),   new CalFields(2011,3,13,6,59,59),   new CalFields(2011,3,13,7,0,0)},

            {"America/New_York",    new CalFields(2011,3,13,3,0,0),     true,
                new CalFields(2011,3,13,7,0,0),     new CalFields(2011,3,13,7,0,0),     new CalFields(2011,3,13,7,0,0)},

            {"Pacific/Apia",        new CalFields(2011,12,29,23,59,59), true,
                new CalFields(2011,12,30,9,59,59),  new CalFields(2011,12,30,9,59,59),  new CalFields(2011,12,30,9,59,59)},

            {"Pacific/Apia",        new CalFields(2011,12,30,0,0,0),    false,
                new CalFields(2011,12,30,10,0,0),  new CalFields(2011,12,29,10,0,0),  new CalFields(2011,12,30,10,0,0)},

            {"Pacific/Apia",        new CalFields(2011,12,30,12,0,0),   false,
                new CalFields(2011,12,30,22,0,0),  new CalFields(2011,12,29,22,0,0),  new CalFields(2011,12,30,10,0,0)},

            {"Pacific/Apia",        new CalFields(2011,12,30,23,59,59), false,
                new CalFields(2011,12,31,9,59,59), new CalFields(2011,12,30,9,59,59), new CalFields(2011,12,30,10,0,0)},

            {"Pacific/Apia",        new CalFields(2011,12,31,0,0,0),    true,
                new CalFields(2011,12,30,10,0,0),  new CalFields(2011,12,30,10,0,0),  new CalFields(2011,12,30,10,0,0)},
        };

        Calendar calGMT = Calendar.getInstance(TimeZone.GMT_ZONE);

        Calendar calDefault = Calendar.getInstance();
        Calendar calLast = Calendar.getInstance();
        Calendar calFirst = Calendar.getInstance();
        Calendar calNextAvail = Calendar.getInstance();

        calLast.setSkippedWallTimeOption(Calendar.WALLTIME_LAST);
        calFirst.setSkippedWallTimeOption(Calendar.WALLTIME_FIRST);
        calNextAvail.setSkippedWallTimeOption(Calendar.WALLTIME_NEXT_VALID);

        for (Object[] test : TESTDATA) {
            String tzid = (String)test[0];
            TimeZone tz = TimeZone.getTimeZone(tzid);
            CalFields in = (CalFields)test[1];
            boolean isValid = (Boolean)test[2];
            CalFields expLastGMT = (CalFields)test[3];
            CalFields expFirstGMT = (CalFields)test[4];
            CalFields expNextAvailGMT = (CalFields)test[5];

            for (int i = 0; i < 2; i++) {
                boolean bLenient = (i == 0);

                // WALLTIME_LAST
                calLast.setLenient(bLenient);
                calLast.setTimeZone(tz);
                try {
                    in.setTo(calLast);
                    calGMT.setTimeInMillis(calLast.getTimeInMillis());
                    CalFields outLastGMT = CalFields.createFrom(calGMT);
                    if (!bLenient && !isValid) {
                        errln("Fail: IllegalArgumentException expected - " + in + "[" + tzid + "] (WALLTIME_LAST)");
                    } else if (!outLastGMT.equals(expLastGMT)) {
                        errln("Fail: WALLTIME_LAST " + in + "[" + tzid + "] is parsed as " + outLastGMT + "[GMT]. Expected: " + expLastGMT + "[GMT]");
                    }
                } catch (IllegalArgumentException e) {
                    if (bLenient || isValid) {
                        errln("Fail: Unexpected IllegalArgumentException - " + in + "[" + tzid + "] (WALLTIME_LAST)");
                    }
                }

                // default
                calDefault.setLenient(bLenient);
                calDefault.setTimeZone(tz);
                try {
                    in.setTo(calDefault);
                    calGMT.setTimeInMillis(calDefault.getTimeInMillis());
                    CalFields outDefGMT = CalFields.createFrom(calGMT);
                    if (!bLenient && !isValid) {
                        errln("Fail: IllegalArgumentException expected - " + in + "[" + tzid + "] (default)");
                    } else if (!outDefGMT.equals(expLastGMT)) {
                        errln("Fail: (default) " + in + "[" + tzid + "] is parsed as " + outDefGMT + "[GMT]. Expected: " + expLastGMT + "[GMT]");
                    }
                } catch (IllegalArgumentException e) {
                    if (bLenient || isValid) {
                        errln("Fail: Unexpected IllegalArgumentException - " + in + "[" + tzid + "] (default)");
                    }
                }

                // WALLTIME_FIRST
                calFirst.setLenient(bLenient);
                calFirst.setTimeZone(tz);
                try {
                    in.setTo(calFirst);
                    calGMT.setTimeInMillis(calFirst.getTimeInMillis());
                    CalFields outFirstGMT = CalFields.createFrom(calGMT);
                    if (!bLenient && !isValid) {
                        errln("Fail: IllegalArgumentException expected - " + in + "[" + tzid + "] (WALLTIME_FIRST)");
                    } else if (!outFirstGMT.equals(expFirstGMT)) {
                        errln("Fail: WALLTIME_FIRST " + in + "[" + tzid + "] is parsed as " + outFirstGMT + "[GMT]. Expected: " + expFirstGMT + "[GMT]");
                    }
                } catch (IllegalArgumentException e) {
                    if (bLenient || isValid) {
                        errln("Fail: Unexpected IllegalArgumentException - " + in + "[" + tzid + "] (WALLTIME_FIRST)");
                    }
                }

                // WALLTIME_NEXT_VALID
                calNextAvail.setLenient(bLenient);
                calNextAvail.setTimeZone(tz);
                try {
                    in.setTo(calNextAvail);
                    calGMT.setTimeInMillis(calNextAvail.getTimeInMillis());
                    CalFields outNextAvailGMT = CalFields.createFrom(calGMT);
                    if (!bLenient && !isValid) {
                        errln("Fail: IllegalArgumentException expected - " + in + "[" + tzid + "] (WALLTIME_NEXT_VALID)");
                    } else if (!outNextAvailGMT.equals(expNextAvailGMT)) {
                        errln("Fail: WALLTIME_NEXT_VALID " + in + "[" + tzid + "] is parsed as " + outNextAvailGMT + "[GMT]. Expected: " + expNextAvailGMT + "[GMT]");
                    }
                } catch (IllegalArgumentException e) {
                    if (bLenient || isValid) {
                        errln("Fail: Unexpected IllegalArgumentException - " + in + "[" + tzid + "] (WALLTIME_NEXT_VALID)");
                    }
                }
            }
        }
    }

    @Test
    public void TestFieldDifference() {
        class TFDItem {
            public String tzname;
            public String locale;
            public long start;
            public long target;
            public boolean progressive; // true to compute progressive difference for each field, false to reset calendar after each call
            int yDiff;
            int MDiff;
            int dDiff;
            int HDiff;
            int mDiff;
            int sDiff; // 0x7FFFFFFF indicates overflow error expected
             // Simple constructor
            public TFDItem(String tz, String loc, long st, long tg, boolean prg, int yD, int MD, int dD, int HD, int mD, int sD ) {
                tzname = tz;
                locale = loc;
                start = st;
                target = tg;
                progressive = prg;
                yDiff = yD;
                MDiff = MD;
                dDiff = dD;
                HDiff = HD;
                mDiff = mD;
                sDiff = sD;
            }
        };
        final TFDItem[] tfdItems = {
            //           timezobe      locale        start            target            prog   yDf  MDf    dDf     HDf       mDf         sDf
            // For these we compute the progressive difference for each field - not resetting the calendar after each call
            new TFDItem( "US/Pacific", "en_US",        1267459800000L,  1277772600000L, true,    0,   3,    27,      9,       40,          0 ), // 2010-Mar-01 08:10 -> 2010-Jun-28 17:50
            new TFDItem( "US/Pacific", "en_US",        1267459800000L,  1299089280000L, true,    1,   0,     1,      1,       58,          0 ), // 2010-Mar-01 08:10 -> 2011-Mar-02 10:08
            // For these we compute the total difference for each field - resetting the calendar after each call
            new TFDItem( "GMT",        "en_US",        0,               1073692800000L, false,  34, 408, 12427, 298248, 17894880, 1073692800 ), // 1970-Jan-01 00:00 -> 2004-Jan-10 00:00
            new TFDItem( "GMT",        "en_US",        0,               1073779200000L, false,  34, 408, 12428, 298272, 17896320, 1073779200 ), // 1970-Jan-01 00:00 -> 2004-Jan-11 00:00
            new TFDItem( "GMT",        "en_US",        0,               2147472000000L, false,  68, 816, 24855, 596520, 35791200, 2147472000 ), // 1970-Jan-01 00:00 -> 2038-Jan-19 00:00
//          new TFDItem( "GMT",        "en_US",        0,               2147558400000L, false,  68, 816, 24856, 596544, 35792640, 0x7FFFFFFF ), // 1970-Jan-01 00:00 -> 2038-Jan-20 00:00, seconds overflow => exception in ICU4J
            new TFDItem( "GMT",        "en_US",        0,              -1073692800000L, false, -34,-408,-12427,-298248,-17894880,-1073692800 ), // 1970-Jan-01 00:00 -> 1935-Dec-24 00:00
            new TFDItem( "GMT",        "en_US",        0,              -1073779200000L, false, -34,-408,-12428,-298272,-17896320,-1073779200 ), // 1970-Jan-01 00:00 -> 1935-Dec-23 00:00
            // check fwd/backward on either side of era boundary and across era boundary
            new TFDItem( "GMT",        "en_US",      -61978089600000L,-61820409600000L, false,   4,  59,  1825,  43800,  2628000,  157680000 ), // CE   5-Dec-31 00:00 -> CE  10-Dec-30 00:00
            new TFDItem( "GMT",        "en_US",      -61820409600000L,-61978089600000L, false,  -4, -59, -1825, -43800, -2628000, -157680000 ), // CE  10-Dec-30 00:00 -> CE   5-Dec-31 00:00
            new TFDItem( "GMT",        "en_US",      -62451129600000L,-62293449600000L, false,   4,  59,  1825,  43800,  2628000,  157680000 ), // BCE 10-Jan-04 00:00 -> BCE  5-Jan-03 00:00
            new TFDItem( "GMT",        "en_US",      -62293449600000L,-62451129600000L, false,  -4, -59, -1825, -43800, -2628000, -157680000 ), // BCE  5-Jan-03 00:00 -> BCE 10-Jan-04 00:00
            new TFDItem( "GMT",        "en_US",      -62293449600000L,-61978089600000L, false,   9, 119,  3650,  87600,  5256000,  315360000 ), // BCE  5-Jan-03 00:00 -> CE   5-Dec-31 00:00
            new TFDItem( "GMT",        "en_US",      -61978089600000L,-62293449600000L, false,  -9,-119, -3650, -87600, -5256000, -315360000 ), // CE   5-Dec-31 00:00 -> BCE  5-Jan-03 00:00
            new TFDItem( "GMT", "en@calendar=roc",    -1672704000000L, -1515024000000L, false,   4,  59,  1825,  43800,  2628000,  157680000 ), // MG   5-Dec-30 00:00 -> MG  10-Dec-29 00:00
            new TFDItem( "GMT", "en@calendar=roc",    -1515024000000L, -1672704000000L, false,  -4, -59, -1825, -43800, -2628000, -157680000 ), // MG  10-Dec-29 00:00 -> MG   5-Dec-30 00:00
            new TFDItem( "GMT", "en@calendar=roc",    -2145744000000L, -1988064000000L, false,   4,  59,  1825,  43800,  2628000,  157680000 ), // BMG 10-Jan-03 00:00 -> BMG  5-Jan-02 00:00
            new TFDItem( "GMT", "en@calendar=roc",    -1988064000000L, -2145744000000L, false,  -4, -59, -1825, -43800, -2628000, -157680000 ), // BMG  5-Jan-02 00:00 -> BMG 10-Jan-03 00:00
            new TFDItem( "GMT", "en@calendar=roc",    -1988064000000L, -1672704000000L, false,   9, 119,  3650,  87600,  5256000,  315360000 ), // BMG  5-Jan-02 00:00 -> MG   5-Dec-30 00:00
            new TFDItem( "GMT", "en@calendar=roc",    -1672704000000L, -1988064000000L, false,  -9,-119, -3650, -87600, -5256000, -315360000 ), // MG   5-Dec-30 00:00 -> BMG  5-Jan-02 00:00
            new TFDItem( "GMT", "en@calendar=coptic",-53026531200000L,-52868851200000L, false,   4,  64,  1825,  43800,  2628000,  157680000 ), // Er1  5-Nas-05 00:00 -> Er1 10-Nas-04 00:00
            new TFDItem( "GMT", "en@calendar=coptic",-52868851200000L,-53026531200000L, false,  -4, -64, -1825, -43800, -2628000, -157680000 ), // Er1 10-Nas-04 00:00 -> Er1  5-Nas-05 00:00
            new TFDItem( "GMT", "en@calendar=coptic",-53499571200000L,-53341891200000L, false,   4,  64,  1825,  43800,  2628000,  157680000 ), // Er0 10-Tou-04 00:00 -> Er0  5-Tou-02 00:00
            new TFDItem( "GMT", "en@calendar=coptic",-53341891200000L,-53499571200000L, false,  -4, -64, -1825, -43800, -2628000, -157680000 ), // Er0  5-Tou-02 00:00 -> Er0 10-Tou-04 00:00
            new TFDItem( "GMT", "en@calendar=coptic",-53341891200000L,-53026531200000L, false,   9, 129,  3650,  87600,  5256000,  315360000 ), // Er0  5-Tou-02 00:00 -> Er1  5-Nas-05 00:00
            new TFDItem( "GMT", "en@calendar=coptic",-53026531200000L,-53341891200000L, false,  -9,-129, -3650, -87600, -5256000, -315360000 ), // Er1  5-Nas-05 00:00 -> Er0  5-Tou-02 00:00
        };
        for (TFDItem tfdItem: tfdItems) {
            TimeZone timezone = TimeZone.getFrozenTimeZone(tfdItem.tzname);
            Calendar ucal = Calendar.getInstance(timezone, new ULocale(tfdItem.locale));
            ucal.setTimeInMillis(tfdItem.target);
            Date targetDate = ucal.getTime();
            int yDf, MDf, dDf, HDf, mDf, sDf;
            if (tfdItem.progressive) {
                ucal.setTimeInMillis(tfdItem.start);
                yDf = ucal.fieldDifference(targetDate, YEAR);
                MDf = ucal.fieldDifference(targetDate, MONTH);
                dDf = ucal.fieldDifference(targetDate, DATE);
                HDf = ucal.fieldDifference(targetDate, HOUR);
                mDf = ucal.fieldDifference(targetDate, MINUTE);
                sDf = ucal.fieldDifference(targetDate, SECOND);
                if ( yDf != tfdItem.yDiff || MDf != tfdItem.MDiff || dDf != tfdItem.dDiff || HDf != tfdItem.HDiff || mDf != tfdItem.mDiff || sDf != tfdItem.sDiff ) {
                    errln("Fail: for locale \"" + tfdItem.locale + "\", start " + tfdItem.start + ", target " +  tfdItem.target + ", expected y-M-d-H-m-s progressive diffs " +
                            tfdItem.yDiff +","+ tfdItem.MDiff +","+ tfdItem.dDiff +","+ tfdItem.HDiff +","+ tfdItem.mDiff +","+ tfdItem.sDiff + ", got " +
                            yDf +","+ MDf +","+ dDf +","+ HDf +","+ mDf +","+ sDf);
                }
            } else {
                ucal.setTimeInMillis(tfdItem.start);
                yDf = ucal.fieldDifference(targetDate, YEAR);
                ucal.setTimeInMillis(tfdItem.start);
                MDf = ucal.fieldDifference(targetDate, MONTH);
                ucal.setTimeInMillis(tfdItem.start);
                dDf = ucal.fieldDifference(targetDate, DATE);
                ucal.setTimeInMillis(tfdItem.start);
                HDf = ucal.fieldDifference(targetDate, HOUR);
                ucal.setTimeInMillis(tfdItem.start);
                mDf = ucal.fieldDifference(targetDate, MINUTE);
                if ( yDf != tfdItem.yDiff || MDf != tfdItem.MDiff || dDf != tfdItem.dDiff || HDf != tfdItem.HDiff || mDf != tfdItem.mDiff ) {
                    errln("Fail: for locale \"" + tfdItem.locale + "\", start " + tfdItem.start + ", target " +  tfdItem.target + ", expected y-M-d-H-m total diffs " +
                            tfdItem.yDiff +","+ tfdItem.MDiff +","+ tfdItem.dDiff +","+ tfdItem.HDiff +","+ tfdItem.mDiff + ", got " +
                            yDf +","+ MDf +","+ dDf +","+ HDf +","+ mDf);
                }
                ucal.setTimeInMillis(tfdItem.start);
                sDf = ucal.fieldDifference(targetDate, SECOND);
                if ( sDf != 0x7FFFFFFF && sDf != tfdItem.sDiff ) {
                    errln("Fail: for locale \"" + tfdItem.locale + "\", start " + tfdItem.start + ", target " +  tfdItem.target + ", expected seconds total diffs " +
                            tfdItem.sDiff + ", got " + sDf);
                }
            }
        }
    }

    @Test
    public void TestAddRollEra0AndEraBounds() {
        final String[] localeIDs = {
            // calendars with non-modern era 0 that goes backwards, max era == 1
            "en@calendar=gregorian",
            "en@calendar=roc",
            "en@calendar=coptic",
            // calendars with non-modern era 0 that goes forwards, max era > 1
            "en@calendar=japanese",
            "en@calendar=chinese",
            // calendars with non-modern era 0 that goes forwards, max era == 1
            "en@calendar=ethiopic",
            // calendars with only one era  = 0, forwards
            "en@calendar=buddhist",
            "en@calendar=hebrew",
            "en@calendar=islamic",
            "en@calendar=indian",
            //"en@calendar=persian", // no persian calendar in ICU4J yet
            "en@calendar=ethiopic-amete-alem",
        };
        TimeZone zoneGMT = TimeZone.getFrozenTimeZone("GMT");
        for (String localeID : localeIDs) {
            Calendar ucalTest = Calendar.getInstance(zoneGMT, new ULocale(localeID));
            String calType = ucalTest.getType();
            boolean era0YearsGoBackwards = (calType.equals("gregorian") || calType.equals("roc") || calType.equals("coptic"));
            int yrBefore, yrAfter, yrMax, eraAfter, eraMax, eraNow;

            ucalTest.clear();
            ucalTest.set(Calendar.YEAR, 2);
            ucalTest.set(Calendar.ERA, 0);
            yrBefore = ucalTest.get(Calendar.YEAR);
            ucalTest.add(Calendar.YEAR, 1);
            yrAfter = ucalTest.get(Calendar.YEAR);
            if ( (era0YearsGoBackwards && yrAfter>yrBefore) || (!era0YearsGoBackwards && yrAfter<yrBefore) ) {
                errln("Fail: era 0 add 1 year does not move forward in time for " + localeID);
            }

            ucalTest.clear();
            ucalTest.set(Calendar.YEAR, 2);
            ucalTest.set(Calendar.ERA, 0);
            yrBefore = ucalTest.get(Calendar.YEAR);
            ucalTest.roll(Calendar.YEAR, 1);
            yrAfter = ucalTest.get(Calendar.YEAR);
            if ( (era0YearsGoBackwards && yrAfter>yrBefore) || (!era0YearsGoBackwards && yrAfter<yrBefore) ) {
                errln("Fail: era 0 roll 1 year does not move forward in time for " + localeID);
            }

            ucalTest.clear();
            ucalTest.set(Calendar.YEAR, 1);
            ucalTest.set(Calendar.ERA, 0);
            if (era0YearsGoBackwards) {
                ucalTest.roll(Calendar.YEAR, 1);
                yrAfter = ucalTest.get(Calendar.YEAR);
                eraAfter = ucalTest.get(Calendar.ERA);
                if (eraAfter != 0 || yrAfter != 1) {
                    errln("Fail: era 0 roll 1 year from year 1 does not stay within era or pin to year 1 for "
                            + localeID + " (get era " + eraAfter + " year " + yrAfter + ")");
                }
            } else {
                // roll backward in time to where era 0 years go negative, except for the Chinese
                // calendar, which uses negative eras instead of having years outside the range 1-60
                ucalTest.roll(Calendar.YEAR, -2);
                yrAfter = ucalTest.get(Calendar.YEAR);
                eraAfter = ucalTest.get(Calendar.ERA);
                if ( !calType.equals("chinese") && (eraAfter != 0 || yrAfter != -1) ) {
                    errln("Fail: era 0 roll -2 years from year 1 does not stay within era or produce year -1 for "
                            + localeID + " (get era " + eraAfter + " year " + yrAfter + ")");
                }
            }

            ucalTest.clear();
            {
                int eraMin = ucalTest.getMinimum(Calendar.ERA);
                if (eraMin != 0 && calType.compareTo("chinese") != 0) {
                    errln("Fail: getMinimum returns minimum era " + eraMin + " (should be 0) for calType " + calType);
                }
            }

            ucalTest.clear();
            ucalTest.set(Calendar.YEAR, 1);
            ucalTest.set(Calendar.ERA, 0);
            eraMax = ucalTest.getMaximum(Calendar.ERA);
            if (eraMax > 0) {
                // try similar tests for era 1 (if calendar has it), in which years always go forward

                ucalTest.clear();
                ucalTest.set(Calendar.YEAR, 2);
                ucalTest.set(Calendar.ERA, 1);
                yrBefore = ucalTest.get(Calendar.YEAR);
                ucalTest.add(Calendar.YEAR, 1);
                yrAfter = ucalTest.get(Calendar.YEAR);
                if ( yrAfter<yrBefore ) {
                    errln("Fail: era 1 add 1 year does not move forward in time for " + localeID);
                }

                ucalTest.clear();
                ucalTest.set(Calendar.YEAR, 2);
                ucalTest.set(Calendar.ERA, 1);
                yrBefore = ucalTest.get(Calendar.YEAR);
                ucalTest.roll(Calendar.YEAR, 1);
                yrAfter = ucalTest.get(Calendar.YEAR);
                if ( yrAfter<yrBefore ) {
                    errln("Fail: era 1 roll 1 year does not move forward in time for " + localeID);
                }

                ucalTest.clear();
                ucalTest.set(Calendar.YEAR, 1);
                ucalTest.set(Calendar.ERA, 1);
                yrMax = ucalTest.getActualMaximum(Calendar.YEAR);
                ucalTest.roll(Calendar.YEAR, -1); // roll down which should pin or wrap to end
                yrAfter = ucalTest.get(Calendar.YEAR);
                eraAfter = ucalTest.get(Calendar.ERA);
                // if yrMax is reasonable we should wrap to that, else we should pin at yr 1
                if (yrMax >= 32768) {
                    if (eraAfter != 1 || yrAfter != 1) {
                        errln("Fail: era 1 roll -1 year from year 1 does not stay within era or pin to year 1 for "
                                + localeID + " (get era " + eraAfter + " year " + yrAfter + ")");
                    }
                } else if (eraAfter != 1 || yrAfter != yrMax) {
                    errln("Fail: era 1 roll -1 year from year 1 does not stay within era or wrap to year "
                            + yrMax + " for " + localeID + " (get era " + eraAfter + " year " + yrAfter + ")");
                } else {
                    ucalTest.roll(Calendar.YEAR, 1); // now roll up which should wrap to beginning
                    yrAfter = ucalTest.get(Calendar.YEAR);
                    eraAfter = ucalTest.get(Calendar.ERA);
                    if (eraAfter != 1 || yrAfter != 1) {
                        errln("Fail: era 1 roll 1 year from year " + yrMax +
                                " does not stay within era or wrap to year 1 for "
                                + localeID + " (get era " + eraAfter + " year " + yrAfter + ")");
                    }
                }

                // if current era  > 1, try the same roll tests for current era
                ucalTest.setTime(new Date());
                eraNow = ucalTest.get(Calendar.ERA);
                if (eraNow > 1) {
                    ucalTest.clear();
                    ucalTest.set(Calendar.YEAR, 1);
                    ucalTest.set(Calendar.ERA, eraNow);
                    yrMax = ucalTest.getActualMaximum(Calendar.YEAR); // max year value for this era
                    ucalTest.roll(Calendar.YEAR, -1);
                    yrAfter = ucalTest.get(Calendar.YEAR);
                    eraAfter = ucalTest.get(Calendar.ERA);
                    // if yrMax is reasonable we should wrap to that, else we should pin at yr 1
                    if (yrMax >= 32768) {
                        if (eraAfter != eraNow || yrAfter != 1) {
                            errln("Fail: era " + eraNow +
                                    " roll -1 year from year 1 does not stay within era or pin to year 1 for "
                                    + localeID + " (get era " + eraAfter + " year " + yrAfter + ")");
                        }
                    } else if (eraAfter != eraNow || yrAfter != yrMax) {
                        errln("Fail: era " + eraNow +
                                " roll -1 year from year 1 does not stay within era or wrap to year " + yrMax
                                + " for " + localeID + " (get era " + eraAfter + " year " + yrAfter + ")");
                    } else {
                        ucalTest.roll(Calendar.YEAR, 1); // now roll up which should wrap to beginning
                        yrAfter = ucalTest.get(Calendar.YEAR);
                        eraAfter = ucalTest.get(Calendar.ERA);
                        if (eraAfter != eraNow || yrAfter != 1) {
                            errln("Fail: era " + eraNow + " roll 1 year from year " + yrMax +
                                    " does not stay within era or wrap to year 1 for "
                                    + localeID + " (get era " + eraAfter + " year " + yrAfter + ")");
                        }
                    }
                }
            }
        }
    }

    @Test
    public void TestWeekData() {
        // Each line contains two locales using the same set of week rule data.
        final String LOCALE_PAIRS[] = {
            "en",       "en_US",
            "de",       "de_DE",
            "de_DE",    "en_DE",
            "en_GB",    "und_GB",
            "ar_EG",    "en_EG",
            "ar_SA",    "fr_SA",
        };

        for (int i = 0; i < LOCALE_PAIRS.length; i += 2) {
            Calendar cal1 = Calendar.getInstance(new ULocale(LOCALE_PAIRS[i]));
            Calendar cal2 = Calendar.getInstance(new ULocale(LOCALE_PAIRS[i + 1]));

            // First day of week
            int dow1 = cal1.getFirstDayOfWeek();
            int dow2 = cal2.getFirstDayOfWeek();
            if (dow1 != dow2) {
                errln("getFirstDayOfWeek: " + LOCALE_PAIRS[i] + "->" + dow1 + ", " + LOCALE_PAIRS[i + 1] + "->" + dow2);
            }

            // Minimum days in first week
            int minDays1 = cal1.getMinimalDaysInFirstWeek();
            int minDays2 = cal2.getMinimalDaysInFirstWeek();
            if (minDays1 != minDays2) {
                errln("getMinimalDaysInFirstWeek: " + LOCALE_PAIRS[i] + "->" + minDays1 + ", " + LOCALE_PAIRS[i + 1] + "->" + minDays2);
            }

            // Weekdays and Weekends
            for (int d = Calendar.SUNDAY; d <= Calendar.SATURDAY; d++) {
                int wdt1 = cal1.getDayOfWeekType(d);
                int wdt2 = cal2.getDayOfWeekType(d);
                if (wdt1 != wdt2) {
                    errln("getDayOfWeekType(" + d + "): " + LOCALE_PAIRS[i] + "->" + wdt1 + ", " + LOCALE_PAIRS[i + 1] + "->" + wdt2);
                }
            }
        }
    }

    @Test
    public void TestAddAcrossZoneTransition() {
        class TestData {
            String zone;
            CalFields base;
            int deltaDays;
            int skippedWTOpt;
            CalFields expected;

            TestData(String zone, CalFields base, int deltaDays, int skippedWTOpt, CalFields expected) {
                this.zone = zone;
                this.base = base;
                this.deltaDays = deltaDays;
                this.skippedWTOpt = skippedWTOpt;
                this.expected = expected;
            }
        }

        TestData[] data = new TestData[] {
            // Add 1 day, from the date before DST transition
            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 1, 59, 59, 999), 1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2014, 3, 9, 1, 59, 59, 999)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 1, 59, 59, 999), 1, Calendar.WALLTIME_LAST,
                                                new CalFields(2014, 3, 9, 1, 59, 59, 999)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 1, 59, 59, 999), 1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2014, 3, 9, 1, 59, 59, 999)),


            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 2, 0, 0, 0), 1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2014, 3, 9, 1, 0, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 2, 0, 0, 0), 1, Calendar.WALLTIME_LAST,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 2, 0, 0, 0), 1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),


            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 2, 30, 0, 0), 1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2014, 3, 9, 1, 30, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 2, 30, 0, 0), 1, Calendar.WALLTIME_LAST,
                                                new CalFields(2014, 3, 9, 3, 30, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 2, 30, 0, 0), 1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),


            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 3, 0, 0, 0), 1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 3, 0, 0, 0), 1, Calendar.WALLTIME_LAST,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 8, 3, 0, 0, 0), 1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),


            // Subtract 1 day, from one day after DST transition
            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 1, 59, 59, 999), -1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2014, 3, 9, 1, 59, 59, 999)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 1, 59, 59, 999), -1, Calendar.WALLTIME_LAST,
                                                new CalFields(2014, 3, 9, 1, 59, 59, 999)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 1, 59, 59, 999), -1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2014, 3, 9, 1, 59, 59, 999)),


            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 2, 0, 0, 0), -1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2014, 3, 9, 1, 0, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 2, 0, 0, 0), -1, Calendar.WALLTIME_LAST,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 2, 0, 0, 0), -1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),


            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 2, 30, 0, 0), -1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2014, 3, 9, 1, 30, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 2, 30, 0, 0), -1, Calendar.WALLTIME_LAST,
                                                new CalFields(2014, 3, 9, 3, 30, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 2, 30, 0, 0), -1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),


            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 3, 0, 0, 0), -1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 3, 0, 0, 0), -1, Calendar.WALLTIME_LAST,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),

            new TestData("America/Los_Angeles", new CalFields(2014, 3, 10, 3, 0, 0, 0), -1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2014, 3, 9, 3, 0, 0, 0)),


            // Test case for ticket#10544
            new TestData("America/Santiago",    new CalFields(2013, 4, 27, 0, 0, 0, 0), 134, Calendar.WALLTIME_FIRST,
                                                new CalFields(2013, 9, 7, 23, 0, 0, 0)),

            new TestData("America/Santiago",    new CalFields(2013, 4, 27, 0, 0, 0, 0), 134, Calendar.WALLTIME_LAST,
                                                new CalFields(2013, 9, 8, 1, 0, 0, 0)),

            new TestData("America/Santiago",    new CalFields(2013, 4, 27, 0, 0, 0, 0), 134, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2013, 9, 8, 1, 0, 0, 0)),


            new TestData("America/Santiago",    new CalFields(2013, 4, 27, 0, 30, 0, 0), 134, Calendar.WALLTIME_FIRST,
                                                new CalFields(2013, 9, 7, 23, 30, 0, 0)),

            new TestData("America/Santiago",    new CalFields(2013, 4, 27, 0, 30, 0, 0), 134, Calendar.WALLTIME_LAST,
                                                new CalFields(2013, 9, 8, 1, 30, 0, 0)),

            new TestData("America/Santiago",    new CalFields(2013, 4, 27, 0, 30, 0, 0), 134, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2013, 9, 8, 1, 0, 0, 0)),


            // Extreme transition - Pacific/Apia completely skips 2011-12-30
            new TestData("Pacific/Apia",        new CalFields(2011, 12, 29, 0, 0, 0, 0), 1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2011, 12, 31, 0, 0, 0, 0)),

            new TestData("Pacific/Apia",        new CalFields(2011, 12, 29, 0, 0, 0, 0), 1, Calendar.WALLTIME_LAST,
                                                new CalFields(2011, 12, 31, 0, 0, 0, 0)),

            new TestData("Pacific/Apia",        new CalFields(2011, 12, 29, 0, 0, 0, 0), 1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2011, 12, 31, 0, 0, 0, 0)),


            new TestData("Pacific/Apia",        new CalFields(2011, 12, 31, 12, 0, 0, 0), -1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2011, 12, 29, 12, 0, 0, 0)),

            new TestData("Pacific/Apia",        new CalFields(2011, 12, 31, 12, 0, 0, 0), -1, Calendar.WALLTIME_LAST,
                                                new CalFields(2011, 12, 29, 12, 0, 0, 0)),

            new TestData("Pacific/Apia",        new CalFields(2011, 12, 31, 12, 0, 0, 0), -1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2011, 12, 29, 12, 0, 0, 0)),


            // 30 minutes DST - Australia/Lord_Howe
            new TestData("Australia/Lord_Howe", new CalFields(2013, 10, 5, 2, 15, 0, 0), 1, Calendar.WALLTIME_FIRST,
                                                new CalFields(2013, 10, 6, 1, 45, 0, 0)),

            new TestData("Australia/Lord_Howe", new CalFields(2013, 10, 5, 2, 15, 0, 0), 1, Calendar.WALLTIME_LAST,
                                                new CalFields(2013, 10, 6, 2, 45, 0, 0)),

            new TestData("Australia/Lord_Howe", new CalFields(2013, 10, 5, 2, 15, 0, 0), 1, Calendar.WALLTIME_NEXT_VALID,
                                                new CalFields(2013, 10, 6, 2, 30, 0, 0)),
        };

        Calendar cal = Calendar.getInstance();
        for (TestData d : data) {
            cal.setTimeZone(TimeZone.getTimeZone(d.zone));
            cal.setSkippedWallTimeOption(d.skippedWTOpt);
            d.base.setTo(cal);
            cal.add(Calendar.DATE, d.deltaDays);

            if (!d.expected.isEquivalentTo(cal)) {
                CalFields res = CalFields.createFrom(cal);
                String optDisp = d.skippedWTOpt == Calendar.WALLTIME_FIRST ? "FIRST" :
                    d.skippedWTOpt == Calendar.WALLTIME_LAST ? "LAST" : "NEXT_VALID";
                errln("Error: base:" + d.base.toString() + ", tz:" + d.zone
                        + ", delta:" + d.deltaDays + " day(s), opt:" + optDisp
                        + ", result:" + res.toString() + " - expected:" + d.expected.toString());
            }
        }
    }

    public void TestSimpleDateFormatCoverage() {

        class StubSimpleDateFormat extends SimpleDateFormat {
            private static final long serialVersionUID = 1L;

            public StubSimpleDateFormat(String pattern, Locale loc) {
                new SimpleDateFormat(pattern, loc);
            }

            public void run(){
                Calendar cal = Calendar.getInstance(Locale.US);
                cal.clear();
                cal.set(2000, Calendar.MARCH, 18, 15,  0, 1); // Sat 15:00

                DateFormatSymbols theseSymbols = this.getSymbols();
                String shouldBeMonday = theseSymbols.getWeekdays()[Calendar.MONDAY];
                assertEquals("Should be Monday", "Monday", shouldBeMonday);

                String [] matchData = {"16", "2016", "2016AD", "Monday", "lunes"};
                int matchIndex =  matchString("Monday March 28, 2016", 0, Calendar.DAY_OF_WEEK, matchData, cal);
                assertEquals("matchData for Monday", 6, matchIndex); // Position of the pointer after the matched string.
                matchIndex =  matchString("Monday March 28, 2016 AD", 17, Calendar.YEAR, matchData, cal);
                assertEquals("matchData for 2016", 21, matchIndex); // Position of the pointer after the matched string.

                char ch = 'y';
                int count = 4;
                int beginOffset = 0;
                cal.set(Calendar.YEAR, 2000);  // Reset this
                assertEquals("calendar year reset", 2000, cal.get(Calendar.YEAR));
                FieldPosition pos = new FieldPosition(java.text.DateFormat.YEAR_FIELD);
                String subFormatResult = subFormat(ch, count, beginOffset,
                        pos, theseSymbols, cal);
                assertEquals("subFormat result", "2000", subFormatResult);

                String testParseString = "some text with a date 2017-03-15";
                int start = 22;
                boolean obeyCount = true;
                boolean allowNegative = false;
                boolean ambiguousYear[] = {true, false, true};
                int subParseResult = subParse(testParseString, start, ch, count,
                        obeyCount, allowNegative, ambiguousYear, cal);
                assertEquals("subParseResult result", 26, subParseResult);
                assertEquals("parsed year", 2017, cal.get(Calendar.YEAR));
            }
        }
        StubSimpleDateFormat stub = new StubSimpleDateFormat("EEE MMM dd yyyy G HH:mm:ss.SSS", Locale.US);
        stub.run();
    }
}
