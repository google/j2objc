/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.util;
import java.util.Date;
import java.util.Locale;

import android.icu.impl.CalendarCache;
import android.icu.util.ULocale.Category;

/**
 * <code>HebrewCalendar</code> is a subclass of <code>Calendar</code>
 * that that implements the traditional Hebrew calendar.
 * This is the civil calendar in Israel and the liturgical calendar
 * of the Jewish faith worldwide.
 * <p>
 * The Hebrew calendar is lunisolar and thus has a number of interesting
 * properties that distinguish it from the Gregorian.  Months start
 * on the day of (an arithmetic approximation of) each new moon.  Since the
 * solar year (approximately 365.24 days) is not an even multiple of
 * the lunar month (approximately 29.53 days) an extra "leap month" is
 * inserted in 7 out of every 19 years.  To make matters even more
 * interesting, the start of a year can be delayed by up to three days
 * in order to prevent certain holidays from falling on the Sabbath and
 * to prevent certain illegal year lengths.  Finally, the lengths of certain
 * months can vary depending on the number of days in the year.
 * <p>
 * The leap month is known as "Adar 1" and is inserted between the
 * months of Shevat and Adar in leap years.  Since the leap month does
 * not come at the end of the year, calculations involving
 * month numbers are particularly complex.  Users of this class should
 * make sure to use the {@link #roll roll} and {@link #add add} methods
 * rather than attempting to perform date arithmetic by manipulating
 * the fields directly.
 * <p>
 * <b>Note:</b> In the traditional Hebrew calendar, days start at sunset.
 * However, in order to keep the time fields in this class
 * synchronized with those of the other calendars and with local clock time,
 * we treat days and months as beginning at midnight,
 * roughly 6 hours after the corresponding sunset.
 * <p>
 * If you are interested in more information on the rules behind the Hebrew
 * calendar, see one of the following references:
 * <ul>
 * <li>"<a href="http://www.amazon.com/exec/obidos/ASIN/0521564743">Calendrical Calculations</a>",
 *      by Nachum Dershowitz &amp; Edward Reingold, Cambridge University Press, 1997, pages 85-91.
 *
 * <li>Hebrew Calendar Science and Myths,
 *      <a href="http://web.archive.org/web/20090423084613/http://www.geocities.com/Athens/1584/">
 *      http://web.archive.org/web/20090423084613/http://www.geocities.com/Athens/1584/</a>
 *
 * <li>The Calendar FAQ,
 *      <a href="http://www.faqs.org/faqs/calendars/faq/">
 *      http://www.faqs.org/faqs/calendars/faq/</a>
 * </ul>
 *
 * <p>
 * This class should not be subclassed.</p>
 * <p>
 * HebrewCalendar usually should be instantiated using 
 * {@link android.icu.util.Calendar#getInstance(ULocale)} passing in a <code>ULocale</code>
 * with the tag <code>"@calendar=hebrew"</code>.</p>
 *
 * @see android.icu.util.GregorianCalendar
 * @see android.icu.util.Calendar
 *
 * @author Laura Werner
 * @author Alan Liu
 */
public class HebrewCalendar extends Calendar {
    // jdk1.4.2 serialver
    private static final long serialVersionUID = -1952524560588825816L;

    //-------------------------------------------------------------------------
    // Tons o' Constants...
    //-------------------------------------------------------------------------


    /** 
     * Constant for Tishri, the 1st month of the Hebrew year. 
     */
    public static final int TISHRI = 0;

    /**
     * Constant for Heshvan, the 2nd month of the Hebrew year. 
     */
    public static final int HESHVAN = 1;

    /**
     * Constant for Kislev, the 3rd month of the Hebrew year. 
     */
    public static final int KISLEV = 2;

    /**
     * Constant for Tevet, the 4th month of the Hebrew year. 
     */
    public static final int TEVET = 3;

    /**
     * Constant for Shevat, the 5th month of the Hebrew year. 
     */
    public static final int SHEVAT = 4;

    /**
     * Constant for Adar I, the 6th month of the Hebrew year
     * (present in leap years only). In non-leap years, the calendar
     * jumps from Shevat (5th month) to Adar (7th month).
     */
    public static final int ADAR_1 = 5;

    /** 
     * Constant for the Adar, the 7th month of the Hebrew year. 
     */
    public static final int ADAR = 6;

    /**
     * Constant for Nisan, the 8th month of the Hebrew year. 
     */
    public static final int NISAN = 7;

    /**
     * Constant for Iyar, the 9th month of the Hebrew year. 
     */
    public static final int IYAR = 8;

    /**
     * Constant for Sivan, the 10th month of the Hebrew year. 
     */
    public static final int SIVAN = 9;

    /**
     * Constant for Tammuz, the 11th month of the Hebrew year. 
     */
    public static final int TAMUZ = 10;

    /**
     * Constant for Av, the 12th month of the Hebrew year. 
     */
    public static final int AV = 11;

    /**
     * Constant for Elul, the 13th month of the Hebrew year. 
     */
    public static final int ELUL = 12;

    /**
     * The absolute date, in milliseconds since 1/1/1970 AD, Gregorian,
     * of the start of the Hebrew calendar.  In order to keep this calendar's
     * time of day in sync with that of the Gregorian calendar, we use
     * midnight, rather than sunset the day before.
     */
    //private static final long EPOCH_MILLIS = -180799862400000L; // 1/1/1 HY

    private static final int LIMITS[][] = {
        // Minimum  Greatest    Least  Maximum
        //           Minimum  Maximum
        {        0,        0,       0,       0 }, // ERA
        { -5000000, -5000000, 5000000, 5000000 }, // YEAR
        {        0,        0,      12,      12 }, // MONTH
        {        1,        1,      51,      56 }, // WEEK_OF_YEAR
        {/*                                  */}, // WEEK_OF_MONTH
        {        1,        1,      29,      30 }, // DAY_OF_MONTH
        {        1,        1,     353,     385 }, // DAY_OF_YEAR
        {/*                                  */}, // DAY_OF_WEEK
        {       -1,       -1,       5,       5 }, // DAY_OF_WEEK_IN_MONTH
        {/*                                  */}, // AM_PM
        {/*                                  */}, // HOUR
        {/*                                  */}, // HOUR_OF_DAY
        {/*                                  */}, // MINUTE
        {/*                                  */}, // SECOND
        {/*                                  */}, // MILLISECOND
        {/*                                  */}, // ZONE_OFFSET
        {/*                                  */}, // DST_OFFSET
        { -5000000, -5000000, 5000000, 5000000 }, // YEAR_WOY
        {/*                                  */}, // DOW_LOCAL
        { -5000000, -5000000, 5000000, 5000000 }, // EXTENDED_YEAR
        {/*                                  */}, // JULIAN_DAY
        {/*                                  */}, // MILLISECONDS_IN_DAY
    };

    /**
     * The lengths of the Hebrew months.  This is complicated, because there
     * are three different types of years, or six if you count leap years.
     * Due to the rules for postponing the start of the year to avoid having
     * certain holidays fall on the sabbath, the year can end up being three
     * different lengths, called "deficient", "normal", and "complete".
     */
    private static final int MONTH_LENGTH[][] = {
        // Deficient  Normal     Complete
        {   30,         30,         30     },           //Tishri
        {   29,         29,         30     },           //Heshvan
        {   29,         30,         30     },           //Kislev
        {   29,         29,         29     },           //Tevet
        {   30,         30,         30     },           //Shevat
        {   30,         30,         30     },           //Adar I (leap years only)
        {   29,         29,         29     },           //Adar
        {   30,         30,         30     },           //Nisan
        {   29,         29,         29     },           //Iyar
        {   30,         30,         30     },           //Sivan
        {   29,         29,         29     },           //Tammuz
        {   30,         30,         30     },           //Av
        {   29,         29,         29     },           //Elul
    };

    /**
     * The cumulative # of days to the end of each month in a non-leap year
     * Although this can be calculated from the MONTH_LENGTH table,
     * keeping it around separately makes some calculations a lot faster
     */
    private static final int MONTH_START[][] = {
        // Deficient  Normal     Complete
        {    0,          0,          0  },          // (placeholder)
        {   30,         30,         30  },          // Tishri
        {   59,         59,         60  },          // Heshvan
        {   88,         89,         90  },          // Kislev
        {  117,        118,        119  },          // Tevet
        {  147,        148,        149  },          // Shevat
        {  147,        148,        149  },          // (Adar I placeholder)
        {  176,        177,        178  },          // Adar
        {  206,        207,        208  },          // Nisan
        {  235,        236,        237  },          // Iyar
        {  265,        266,        267  },          // Sivan
        {  294,        295,        296  },          // Tammuz
        {  324,        325,        326  },          // Av
        {  353,        354,        355  },          // Elul
    };

    /**
     * The cumulative # of days to the end of each month in a leap year
     */
    private static final int LEAP_MONTH_START[][] = {
        // Deficient  Normal     Complete
        {    0,          0,          0  },          // (placeholder)
        {   30,         30,         30  },          // Tishri
        {   59,         59,         60  },          // Heshvan
        {   88,         89,         90  },          // Kislev
        {  117,        118,        119  },          // Tevet
        {  147,        148,        149  },          // Shevat
        {  177,        178,        179  },          // Adar I
        {  206,        207,        208  },          // Adar II
        {  236,        237,        238  },          // Nisan
        {  265,        266,        267  },          // Iyar
        {  295,        296,        297  },          // Sivan
        {  324,        325,        326  },          // Tammuz
        {  354,        355,        356  },          // Av
        {  383,        384,        385  },          // Elul
    };

    //-------------------------------------------------------------------------
    // Data Members...
    //-------------------------------------------------------------------------

    private static CalendarCache cache = new CalendarCache();
    
    //-------------------------------------------------------------------------
    // Constructors...
    //-------------------------------------------------------------------------

    /**
     * Constructs a default <code>HebrewCalendar</code> using the current time
     * in the default time zone with the default <code>FORMAT</code> locale.
     * @see Category#FORMAT
     */
    public HebrewCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a <code>HebrewCalendar</code> based on the current time
     * in the given time zone with the default <code>FORMAT</code> locale.
     *
     * @param zone The time zone for the new calendar.
     * @see Category#FORMAT
     */
    public HebrewCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a <code>HebrewCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param aLocale The locale for the new calendar.
     */
    public HebrewCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    /**
     * Constructs a <code>HebrewCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param locale The locale for the new calendar.
     */
    public HebrewCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    /**
     * Constructs a <code>HebrewCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone The time zone for the new calendar.
     *
     * @param aLocale The locale for the new calendar.
     */
    public HebrewCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a <code>HebrewCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone The time zone for the new calendar.
     *
     * @param locale The locale for the new calendar.
     */
    public HebrewCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a <code>HebrewCalendar</code> with the given date set
     * in the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for Tishri.
     *
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     * @see Category#FORMAT
     */
    public HebrewCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.set(YEAR, year);
        this.set(MONTH, month);
        this.set(DATE, date);
    }

    /**
     * Constructs a <code>HebrewCalendar</code> with the given date set
     * in the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param date      The date to which the new calendar is set.
     * @see Category#FORMAT
     */
    public HebrewCalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.setTime(date);
    }

    /**
     * Constructs a <code>HebrewCalendar</code> with the given date
     * and time set for the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for Tishri.
     *
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     *
     * @param hour      The value used to set the calendar's {@link #HOUR_OF_DAY HOUR_OF_DAY} time field.
     *
     * @param minute    The value used to set the calendar's {@link #MINUTE MINUTE} time field.
     *
     * @param second    The value used to set the calendar's {@link #SECOND SECOND} time field.
     * @see Category#FORMAT
     */
    public HebrewCalendar(int year, int month, int date, int hour,
                             int minute, int second)
    {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.set(YEAR, year);
        this.set(MONTH, month);
        this.set(DATE, date);
        this.set(HOUR_OF_DAY, hour);
        this.set(MINUTE, minute);
        this.set(SECOND, second);
    }

    //-------------------------------------------------------------------------
    // Rolling and adding functions overridden from Calendar
    //
    // These methods call through to the default implementation in IBMCalendar
    // for most of the fields and only handle the unusual ones themselves.
    //-------------------------------------------------------------------------

    /**
     * Add a signed amount to a specified field, using this calendar's rules.
     * For example, to add three days to the current date, you can call
     * <code>add(Calendar.DATE, 3)</code>. 
     * <p>
     * When adding to certain fields, the values of other fields may conflict and
     * need to be changed.  For example, when adding one to the {@link #MONTH MONTH} field
     * for the date "30 Av 5758", the {@link #DAY_OF_MONTH DAY_OF_MONTH} field
     * must be adjusted so that the result is "29 Elul 5758" rather than the invalid
     * "30 Elul 5758".
     * <p>
     * This method is able to add to
     * all fields except for {@link #ERA ERA}, {@link #DST_OFFSET DST_OFFSET},
     * and {@link #ZONE_OFFSET ZONE_OFFSET}.
     * <p>
     * <b>Note:</b> You should always use {@link #roll roll} and add rather
     * than attempting to perform arithmetic operations directly on the fields
     * of a <tt>HebrewCalendar</tt>.  Since the {@link #MONTH MONTH} field behaves
     * discontinuously in non-leap years, simple arithmetic can give invalid results.
     * <p>
     * @param field     the time field.
     * @param amount    the amount to add to the field.
     *
     * @exception   IllegalArgumentException if the field is invalid or refers
     *              to a field that cannot be handled by this method.
     */
    public void add(int field, int amount)
    {
        switch (field) {
        case MONTH: 
            {
                // We can't just do a set(MONTH, get(MONTH) + amount).  The
                // reason is ADAR_1.  Suppose amount is +2 and we land in
                // ADAR_1 -- then we have to bump to ADAR_2 aka ADAR.  But
                // if amount is -2 and we land in ADAR_1, then we have to
                // bump the other way -- down to SHEVAT.  - Alan 11/00
                int month = get(MONTH);
                int year = get(YEAR);
                boolean acrossAdar1;
                if (amount > 0) {
                    acrossAdar1 = (month < ADAR_1); // started before ADAR_1?
                    month += amount;
                    for (;;) {
                        if (acrossAdar1 && month>=ADAR_1 && !isLeapYear(year)) {
                            ++month;
                        }
                        if (month <= ELUL) {
                            break;
                        }
                        month -= ELUL+1;
                        ++year;
                        acrossAdar1 = true;
                    }
                } else {
                    acrossAdar1 = (month > ADAR_1); // started after ADAR_1?
                    month += amount;
                    for (;;) {
                        if (acrossAdar1 && month<=ADAR_1 && !isLeapYear(year)) {
                            --month;
                        }
                        if (month >= 0) {
                            break;
                        }
                        month += ELUL+1;
                        --year;
                        acrossAdar1 = true;
                    }
                }
                set(MONTH, month);
                set(YEAR, year);
                pinField(DAY_OF_MONTH);
                break;
            }
            
        default:
            super.add(field, amount);
            break;
        }
    }

    /**
     * Rolls (up/down) a specified amount time on the given field.  For
     * example, to roll the current date up by three days, you can call
     * <code>roll(Calendar.DATE, 3)</code>.  If the
     * field is rolled past its maximum allowable value, it will "wrap" back
     * to its minimum and continue rolling.  
     * For example, calling <code>roll(Calendar.DATE, 10)</code>
     * on a Hebrew calendar set to "25 Av 5758" will result in the date "5 Av 5758".
     * <p>
     * When rolling certain fields, the values of other fields may conflict and
     * need to be changed.  For example, when rolling the {@link #MONTH MONTH} field
     * upward by one for the date "30 Av 5758", the {@link #DAY_OF_MONTH DAY_OF_MONTH} field
     * must be adjusted so that the result is "29 Elul 5758" rather than the invalid
     * "30 Elul".
     * <p>
     * This method is able to roll
     * all fields except for {@link #ERA ERA}, {@link #DST_OFFSET DST_OFFSET},
     * and {@link #ZONE_OFFSET ZONE_OFFSET}.  Subclasses may, of course, add support for
     * additional fields in their overrides of <code>roll</code>.
     * <p>
     * <b>Note:</b> You should always use roll and {@link #add add} rather
     * than attempting to perform arithmetic operations directly on the fields
     * of a <tt>HebrewCalendar</tt>.  Since the {@link #MONTH MONTH} field behaves
     * discontinuously in non-leap years, simple arithmetic can give invalid results.
     * <p>
     * @param field     the time field.
     * @param amount    the amount by which the field should be rolled.
     *
     * @exception   IllegalArgumentException if the field is invalid or refers
     *              to a field that cannot be handled by this method.
     */
    public void roll(int field, int amount)
    {
        switch (field) {
        case MONTH:
            {
                int month = get(MONTH);
                int year = get(YEAR);
                
                boolean leapYear = isLeapYear(year);
                int yearLength = monthsInYear(year);
                int newMonth = month + (amount % yearLength);
                //
                // If it's not a leap year and we're rolling past the missing month
                // of ADAR_1, we need to roll an extra month to make up for it.
                //
                if (!leapYear) {
                    if (amount > 0 && month < ADAR_1 && newMonth >= ADAR_1) {
                        newMonth++;
                    } else if (amount < 0 && month > ADAR_1 && newMonth <= ADAR_1) {
                        newMonth--;
                    }
                }
                set(MONTH, (newMonth + 13) % 13);
                pinField(DAY_OF_MONTH);
                return;
            }
        default:
            super.roll(field, amount);
        }
    }

    //-------------------------------------------------------------------------
    // Support methods
    //-------------------------------------------------------------------------

    // Hebrew date calculations are performed in terms of days, hours, and
    // "parts" (or halakim), which are 1/1080 of an hour, or 3 1/3 seconds.
    private static final long HOUR_PARTS = 1080;
    private static final long DAY_PARTS  = 24*HOUR_PARTS;
    
    // An approximate value for the length of a lunar month.
    // It is used to calculate the approximate year and month of a given
    // absolute date.
    static private final int  MONTH_DAYS = 29;
    static private final long MONTH_FRACT = 12*HOUR_PARTS + 793;
    static private final long MONTH_PARTS = MONTH_DAYS*DAY_PARTS + MONTH_FRACT;
    
    // The time of the new moon (in parts) on 1 Tishri, year 1 (the epoch)
    // counting from noon on the day before.  BAHARAD is an abbreviation of
    // Bet (Monday), Hey (5 hours from sunset), Resh-Daled (204).
    static private final long BAHARAD = 11*HOUR_PARTS + 204;

    /**
     * Finds the day # of the first day in the given Hebrew year.
     * To do this, we want to calculate the time of the Tishri 1 new moon
     * in that year.
     * <p>
     * The algorithm here is similar to ones described in a number of
     * references, including:
     * <ul>
     * <li>"Calendrical Calculations", by Nachum Dershowitz & Edward Reingold,
     *     Cambridge University Press, 1997, pages 85-91.
     *
     * <li>Hebrew Calendar Science and Myths,
     *     <a href="http://www.geocities.com/Athens/1584/">
     *     http://www.geocities.com/Athens/1584/</a>
     *
     * <li>The Calendar FAQ,
     *      <a href="http://www.faqs.org/faqs/calendars/faq/">
     *      http://www.faqs.org/faqs/calendars/faq/</a>
     * </ul>
     */
    private static long startOfYear(int year)
    {
        long day = cache.get(year);
        
        if (day == CalendarCache.EMPTY) {
            int months = (235 * year - 234) / 19;           // # of months before year

            long frac = months * MONTH_FRACT + BAHARAD;     // Fractional part of day #
            day  = months * 29 + (frac / DAY_PARTS);        // Whole # part of calculation
            frac = frac % DAY_PARTS;                        // Time of day

            int wd = (int)(day % 7);                        // Day of week (0 == Monday)

            if (wd == 2 || wd == 4 || wd == 6) {
                // If the 1st is on Sun, Wed, or Fri, postpone to the next day
                day += 1;
                wd = (int)(day % 7);
            }
            if (wd == 1 && frac > 15*HOUR_PARTS+204 && !isLeapYear(year) ) {
                // If the new moon falls after 3:11:20am (15h204p from the previous noon)
                // on a Tuesday and it is not a leap year, postpone by 2 days.
                // This prevents 356-day years.
                day += 2;
            }
            else if (wd == 0 && frac > 21*HOUR_PARTS+589 && isLeapYear(year-1) ) {
                // If the new moon falls after 9:32:43 1/3am (21h589p from yesterday noon)
                // on a Monday and *last* year was a leap year, postpone by 1 day.
                // Prevents 382-day years.
                day += 1;
            }
            cache.put(year, day);
        }
        return day;
    }

    /*
     * Find the day of the week for a given day
     *
     * @param day   The # of days since the start of the Hebrew calendar,
     *              1-based (i.e. 1/1/1 AM is day 1).
     */
    /*private static int absoluteDayToDayOfWeek(long day)
    {
        // We know that 1/1/1 AM is a Monday, which makes the math easy...
        return (int)(day % 7) + 1;
    }*/

    /**
     * Returns the the type of a given year.
     *  0   "Deficient" year with 353 or 383 days
     *  1   "Normal"    year with 354 or 384 days
     *  2   "Complete"  year with 355 or 385 days
     */
    private final int yearType(int year)
    {
        int yearLength = handleGetYearLength(year);

        if (yearLength > 380) {
           yearLength -= 30;        // Subtract length of leap month.
        }

        int type = 0;

        switch (yearLength) {
            case 353:
                type = 0; break;
            case 354:
                type = 1; break;
            case 355:
                type = 2; break;
            default:
                throw new IllegalArgumentException("Illegal year length " + yearLength + " in year " + year);

        }
        return type;
    }

    /**
     * Determine whether a given Hebrew year is a leap year
     *
     * The rule here is that if (year % 19) == 0, 3, 6, 8, 11, 14, or 17.
     * The formula below performs the same test, believe it or not.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static boolean isLeapYear(int year) {
        //return (year * 12 + 17) % 19 >= 12;
        int x = (year*12 + 17) % 19;
        return x >= ((x < 0) ? -7 : 12);
    }

    private static int monthsInYear(int year) {
        return isLeapYear(year) ? 13 : 12;
    }

    //-------------------------------------------------------------------------
    // Calendar framework
    //-------------------------------------------------------------------------

    /**
     */
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    /**
     * Returns the length of the given month in the given year
     */
    protected int handleGetMonthLength(int extendedYear, int month) {
        // Resolve out-of-range months.  This is necessary in order to
        // obtain the correct year.  We correct to
        // a 12- or 13-month year (add/subtract 12 or 13, depending
        // on the year) but since we _always_ number from 0..12, and
        // the leap year determines whether or not month 5 (Adar 1)
        // is present, we allow 0..12 in any given year.
        while (month < 0) {
            month += monthsInYear(--extendedYear);
        }
        // Careful: allow 0..12 in all years
        while (month > 12) {
            month -= monthsInYear(extendedYear++);
        }

        switch (month) {
            case HESHVAN:
            case KISLEV:
                // These two month lengths can vary
                return MONTH_LENGTH[month][yearType(extendedYear)];
                
            default:
                // The rest are a fixed length
                return MONTH_LENGTH[month][0];
        }
    }

    /**
     * Returns the number of days in the given Hebrew year
     */
    protected int handleGetYearLength(int eyear) {
        return (int)(startOfYear(eyear+1) - startOfYear(eyear));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overrides {@link Calendar#validateField(int)} to provide
     * special handling for month validation for Hebrew calendar.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected void validateField(int field) {
        if (field == MONTH && !isLeapYear(handleGetExtendedYear()) && internalGet(MONTH) == ADAR_1) {
            throw new IllegalArgumentException("MONTH cannot be ADAR_1(5) except leap years");
        }

        super.validateField(field);
    }

    //-------------------------------------------------------------------------
    // Functions for converting from milliseconds to field values
    //-------------------------------------------------------------------------

    /**
     * Subclasses may override this method to compute several fields
     * specific to each calendar system.  These are:
     *
     * <ul><li>ERA
     * <li>YEAR
     * <li>MONTH
     * <li>DAY_OF_MONTH
     * <li>DAY_OF_YEAR
     * <li>EXTENDED_YEAR</ul>
     * 
     * Subclasses can refer to the DAY_OF_WEEK and DOW_LOCAL fields,
     * which will be set when this method is called.  Subclasses can
     * also call the getGregorianXxx() methods to obtain Gregorian
     * calendar equivalents for the given Julian day.
     *
     * <p>In addition, subclasses should compute any subclass-specific
     * fields, that is, fields from BASE_FIELD_COUNT to
     * getFieldCount() - 1.
     */
    protected void handleComputeFields(int julianDay) {
        long d = julianDay - 347997;
        long m = (d * DAY_PARTS) / MONTH_PARTS;         // Months (approx)
        int year = (int)((19 * m + 234) / 235) + 1;     // Years (approx)
        long ys  = startOfYear(year);                   // 1st day of year
        int dayOfYear = (int)(d - ys);

        // Because of the postponement rules, it's possible to guess wrong.  Fix it.
        while (dayOfYear < 1) {
            year--;
            ys  = startOfYear(year);
            dayOfYear = (int)(d - ys);
        }

        // Now figure out which month we're in, and the date within that month
        int yearType = yearType(year);
        int monthStart[][] = isLeapYear(year) ? LEAP_MONTH_START : MONTH_START;

        int month = 0;
        while (dayOfYear > monthStart[month][yearType]) {
            month++;
        }
        month--;
        int dayOfMonth = dayOfYear - monthStart[month][yearType];

        internalSet(ERA, 0);
        internalSet(YEAR, year);
        internalSet(EXTENDED_YEAR, year);
        internalSet(MONTH, month);
        internalSet(DAY_OF_MONTH, dayOfMonth);
        internalSet(DAY_OF_YEAR, dayOfYear);       
    }

    //-------------------------------------------------------------------------
    // Functions for converting from field values to milliseconds
    //-------------------------------------------------------------------------

    /**
     */
    protected int handleGetExtendedYear() {
        int year;
        if (newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR) {
            year = internalGet(EXTENDED_YEAR, 1); // Default to year 1
        } else {
            year = internalGet(YEAR, 1); // Default to year 1
        }
        return year;
    }

    /**
     * Return JD of start of given month/year.
     */
    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {

        // Resolve out-of-range months.  This is necessary in order to
        // obtain the correct year.  We correct to
        // a 12- or 13-month year (add/subtract 12 or 13, depending
        // on the year) but since we _always_ number from 0..12, and
        // the leap year determines whether or not month 5 (Adar 1)
        // is present, we allow 0..12 in any given year.
        while (month < 0) {
            month += monthsInYear(--eyear);
        }
        // Careful: allow 0..12 in all years
        while (month > 12) {
            month -= monthsInYear(eyear++);
        }

        long day = startOfYear(eyear);

        if (month != 0) {
            if (isLeapYear(eyear)) {
                day += LEAP_MONTH_START[month][yearType(eyear)];
            } else {
                day += MONTH_START[month][yearType(eyear)];
            }
        }

        return (int) (day + 347997);
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return "hebrew";
    }

    /*
    private static CalendarFactory factory;
    public static CalendarFactory factory() {
        if (factory == null) {
            factory = new CalendarFactory() {
                public Calendar create(TimeZone tz, ULocale loc) {
                    return new HebrewCalendar(tz, loc);
                }

                public String factoryName() {
                    return "Hebrew";
                }
            };
        }
        return factory;
    }
    */
}
