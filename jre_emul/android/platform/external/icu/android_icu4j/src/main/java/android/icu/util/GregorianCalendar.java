/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 * Copyright (C) 1996-2016, International Business Machines
 * Corporation and others.  All Rights Reserved.
 */

package android.icu.util;

import java.util.Date;
import java.util.Locale;

import android.icu.util.ULocale.Category;

/**
 * <strong>[icu enhancement]</strong> ICU's replacement for {@link java.util.GregorianCalendar}.&nbsp;Methods, fields, and other functionality specific to ICU are labeled '<strong>[icu]</strong>'.
 *
 * <p><code>GregorianCalendar</code> is a concrete subclass of
 * {@link Calendar}
 * and provides the standard calendar used by most of the world.
 *
 * <p>The standard (Gregorian) calendar has 2 eras, BC and AD.
 *
 * <p>This implementation handles a single discontinuity, which corresponds by
 * default to the date the Gregorian calendar was instituted (October 15, 1582
 * in some countries, later in others).  The cutover date may be changed by the
 * caller by calling <code>setGregorianChange()</code>.
 *
 * <p>Historically, in those countries which adopted the Gregorian calendar first,
 * October 4, 1582 was thus followed by October 15, 1582. This calendar models
 * this correctly.  Before the Gregorian cutover, <code>GregorianCalendar</code>
 * implements the Julian calendar.  The only difference between the Gregorian
 * and the Julian calendar is the leap year rule. The Julian calendar specifies
 * leap years every four years, whereas the Gregorian calendar omits century
 * years which are not divisible by 400.
 *
 * <p><code>GregorianCalendar</code> implements <em>proleptic</em> Gregorian and
 * Julian calendars. That is, dates are computed by extrapolating the current
 * rules indefinitely far backward and forward in time. As a result,
 * <code>GregorianCalendar</code> may be used for all years to generate
 * meaningful and consistent results. However, dates obtained using
 * <code>GregorianCalendar</code> are historically accurate only from March 1, 4
 * AD onward, when modern Julian calendar rules were adopted.  Before this date,
 * leap year rules were applied irregularly, and before 45 BC the Julian
 * calendar did not even exist.
 *
 * <p>Prior to the institution of the Gregorian calendar, New Year's Day was
 * March 25. To avoid confusion, this calendar always uses January 1. A manual
 * adjustment may be made if desired for dates that are prior to the Gregorian
 * changeover and which fall between January 1 and March 24.
 *
 * <p>Values calculated for the <code>WEEK_OF_YEAR</code> field range from 1 to
 * 53.  Week 1 for a year is the earliest seven day period starting on
 * <code>getFirstDayOfWeek()</code> that contains at least
 * <code>getMinimalDaysInFirstWeek()</code> days from that year.  It thus
 * depends on the values of <code>getMinimalDaysInFirstWeek()</code>,
 * <code>getFirstDayOfWeek()</code>, and the day of the week of January 1.
 * Weeks between week 1 of one year and week 1 of the following year are
 * numbered sequentially from 2 to 52 or 53 (as needed).

 * <p>For example, January 1, 1998 was a Thursday.  If
 * <code>getFirstDayOfWeek()</code> is <code>MONDAY</code> and
 * <code>getMinimalDaysInFirstWeek()</code> is 4 (these are the values
 * reflecting ISO 8601 and many national standards), then week 1 of 1998 starts
 * on December 29, 1997, and ends on January 4, 1998.  If, however,
 * <code>getFirstDayOfWeek()</code> is <code>SUNDAY</code>, then week 1 of 1998
 * starts on January 4, 1998, and ends on January 10, 1998; the first three days
 * of 1998 then are part of week 53 of 1997.
 *
 * <p>Values calculated for the <code>WEEK_OF_MONTH</code> field range from 0 or
 * 1 to 4 or 5.  Week 1 of a month (the days with <code>WEEK_OF_MONTH =
 * 1</code>) is the earliest set of at least
 * <code>getMinimalDaysInFirstWeek()</code> contiguous days in that month,
 * ending on the day before <code>getFirstDayOfWeek()</code>.  Unlike
 * week 1 of a year, week 1 of a month may be shorter than 7 days, need
 * not start on <code>getFirstDayOfWeek()</code>, and will not include days of
 * the previous month.  Days of a month before week 1 have a
 * <code>WEEK_OF_MONTH</code> of 0.
 *
 * <p>For example, if <code>getFirstDayOfWeek()</code> is <code>SUNDAY</code>
 * and <code>getMinimalDaysInFirstWeek()</code> is 4, then the first week of
 * January 1998 is Sunday, January 4 through Saturday, January 10.  These days
 * have a <code>WEEK_OF_MONTH</code> of 1.  Thursday, January 1 through
 * Saturday, January 3 have a <code>WEEK_OF_MONTH</code> of 0.  If
 * <code>getMinimalDaysInFirstWeek()</code> is changed to 3, then January 1
 * through January 3 have a <code>WEEK_OF_MONTH</code> of 1.
 *
 * <p>
 * <strong>Example:</strong>
 * <blockquote>
 * <pre>
 * // get the supported ids for GMT-08:00 (Pacific Standard Time)
 * String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
 * // if no ids were returned, something is wrong. get out.
 * if (ids.length == 0)
 *     System.exit(0);
 *
 *  // begin output
 * System.out.println("Current Time");
 *
 * // create a Pacific Standard Time time zone
 * SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);
 *
 * // set up rules for daylight savings time
 * pdt.setStartRule(Calendar.MARCH, 2, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
 * pdt.setEndRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
 *
 * // create a GregorianCalendar with the Pacific Daylight time zone
 * // and the current date and time
 * Calendar calendar = new GregorianCalendar(pdt);
 * Date trialTime = new Date();
 * calendar.setTime(trialTime);
 *
 * // print out a bunch of interesting things
 * System.out.println("ERA: " + calendar.get(Calendar.ERA));
 * System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
 * System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
 * System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
 * System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
 * System.out.println("DATE: " + calendar.get(Calendar.DATE));
 * System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
 * System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
 * System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
 * System.out.println("DAY_OF_WEEK_IN_MONTH: "
 *                    + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
 * System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
 * System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
 * System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
 * System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
 * System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
 * System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
 * System.out.println("ZONE_OFFSET: "
 *                    + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000)));
 * System.out.println("DST_OFFSET: "
 *                    + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000)));

 * System.out.println("Current Time, with hour reset to 3");
 * calendar.clear(Calendar.HOUR_OF_DAY); // so doesn't override
 * calendar.set(Calendar.HOUR, 3);
 * System.out.println("ERA: " + calendar.get(Calendar.ERA));
 * System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
 * System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
 * System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
 * System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
 * System.out.println("DATE: " + calendar.get(Calendar.DATE));
 * System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
 * System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
 * System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
 * System.out.println("DAY_OF_WEEK_IN_MONTH: "
 *                    + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
 * System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
 * System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
 * System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
 * System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
 * System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
 * System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
 * System.out.println("ZONE_OFFSET: "
 *        + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000))); // in hours
 * System.out.println("DST_OFFSET: "
 *        + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000))); // in hours</pre>
 * </blockquote>
 * <p>
 * GregorianCalendar usually should be instantiated using 
 * {@link android.icu.util.Calendar#getInstance(ULocale)} passing in a <code>ULocale</code>
 * with the tag <code>"@calendar=gregorian"</code>.</p>

 * @see          Calendar
 * @see          TimeZone
 * @author Deborah Goldsmith, Mark Davis, Chen-Lieh Huang, Alan Liu
 */
public class GregorianCalendar extends Calendar {
    // jdk1.4.2 serialver
    private static final long serialVersionUID = 9199388694351062137L;

    /*
     * Implementation Notes
     *
     * The Julian day number, as used here, is a modified number which has its
     * onset at midnight, rather than noon.
     *
     * The epoch is the number of days or milliseconds from some defined
     * starting point. The epoch for java.util.Date is used here; that is,
     * milliseconds from January 1, 1970 (Gregorian), midnight UTC.  Other
     * epochs which are used are January 1, year 1 (Gregorian), which is day 1
     * of the Gregorian calendar, and December 30, year 0 (Gregorian), which is
     * day 1 of the Julian calendar.
     *
     * We implement the proleptic Julian and Gregorian calendars.  This means we
     * implement the modern definition of the calendar even though the
     * historical usage differs.  For example, if the Gregorian change is set
     * to new Date(Long.MIN_VALUE), we have a pure Gregorian calendar which
     * labels dates preceding the invention of the Gregorian calendar in 1582 as
     * if the calendar existed then.
     *
     * Likewise, with the Julian calendar, we assume a consistent 4-year leap
     * rule, even though the historical pattern of leap years is irregular,
     * being every 3 years from 45 BC through 9 BC, then every 4 years from 8 AD
     * onwards, with no leap years in-between.  Thus date computations and
     * functions such as isLeapYear() are not intended to be historically
     * accurate.
     *
     * Given that milliseconds are a long, day numbers such as Julian day
     * numbers, Gregorian or Julian calendar days, or epoch days, are also
     * longs. Years can fit into an int.
     */

//////////////////
// Class Variables
//////////////////

    /**
     * Value of the <code>ERA</code> field indicating
     * the period before the common era (before Christ), also known as BCE.
     * The sequence of years at the transition from <code>BC</code> to <code>AD</code> is
     * ..., 2 BC, 1 BC, 1 AD, 2 AD,...
     * @see Calendar#ERA
     */
    public static final int BC = 0;

    /**
     * Value of the <code>ERA</code> field indicating
     * the common era (Anno Domini), also known as CE.
     * The sequence of years at the transition from <code>BC</code> to <code>AD</code> is
     * ..., 2 BC, 1 BC, 1 AD, 2 AD,...
     * @see Calendar#ERA
     */
    public static final int AD = 1;

    private static final int EPOCH_YEAR = 1970;

    private static final int[][] MONTH_COUNT = {
        //len len2   st  st2
        {  31,  31,   0,   0 }, // Jan
        {  28,  29,  31,  31 }, // Feb
        {  31,  31,  59,  60 }, // Mar
        {  30,  30,  90,  91 }, // Apr
        {  31,  31, 120, 121 }, // May
        {  30,  30, 151, 152 }, // Jun
        {  31,  31, 181, 182 }, // Jul
        {  31,  31, 212, 213 }, // Aug
        {  30,  30, 243, 244 }, // Sep
        {  31,  31, 273, 274 }, // Oct
        {  30,  30, 304, 305 }, // Nov
        {  31,  31, 334, 335 }  // Dec
        // len  length of month
        // len2 length of month in a leap year
        // st   days in year before start of month
        // st2  days in year before month in leap year
    };
    
    /**
     * Old year limits were least max 292269054, max 292278994.
     */
    private static final int LIMITS[][] = {
        // Minimum  Greatest    Least  Maximum
        //           Minimum  Maximum
        {        0,        0,       1,       1 }, // ERA
        {        1,        1, 5828963, 5838270 }, // YEAR
        {        0,        0,      11,      11 }, // MONTH
        {        1,        1,      52,      53 }, // WEEK_OF_YEAR
        {/*                                  */}, // WEEK_OF_MONTH
        {        1,        1,      28,      31 }, // DAY_OF_MONTH
        {        1,        1,     365,     366 }, // DAY_OF_YEAR
        {/*                                  */}, // DAY_OF_WEEK
        {       -1,       -1,       4,       5 }, // DAY_OF_WEEK_IN_MONTH
        {/*                                  */}, // AM_PM
        {/*                                  */}, // HOUR
        {/*                                  */}, // HOUR_OF_DAY
        {/*                                  */}, // MINUTE
        {/*                                  */}, // SECOND
        {/*                                  */}, // MILLISECOND
        {/*                                  */}, // ZONE_OFFSET
        {/*                                  */}, // DST_OFFSET
        { -5838270, -5838270, 5828964, 5838271 }, // YEAR_WOY
        {/*                                  */}, // DOW_LOCAL
        { -5838269, -5838269, 5828963, 5838270 }, // EXTENDED_YEAR
        {/*                                  */}, // JULIAN_DAY
        {/*                                  */}, // MILLISECONDS_IN_DAY
        {/*                                  */}, // IS_LEAP_MONTH
    };

    /**
     */
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

/////////////////////
// Instance Variables
/////////////////////

    /**
     * The point at which the Gregorian calendar rules are used, measured in
     * milliseconds from the standard epoch.  Default is October 15, 1582
     * (Gregorian) 00:00:00 UTC or -12219292800000L.  For this value, October 4,
     * 1582 (Julian) is followed by October 15, 1582 (Gregorian).  This
     * corresponds to Julian day number 2299161.
     * @serial
     */
    private long gregorianCutover = -12219292800000L;

    /**
     * Julian day number of the Gregorian cutover.
     */
    private transient int cutoverJulianDay = 2299161;
    
    /**
     * The year of the gregorianCutover, with 0 representing
     * 1 BC, -1 representing 2 BC, etc.
     */
    private transient int gregorianCutoverYear = 1582;

    /**
     * Used by handleComputeJulianDay() and handleComputeMonthStart().
     */
    transient protected boolean isGregorian;

    /**
     * Used by handleComputeJulianDay() and handleComputeMonthStart().
     */
    transient protected boolean invertGregorian;

///////////////
// Constructors
///////////////

    /**
     * Constructs a default GregorianCalendar using the current time
     * in the default time zone with the default <code>FORMAT</code> locale.
     * @see Category#FORMAT
     */
    public GregorianCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a GregorianCalendar based on the current time
     * in the given time zone with the default <code>FORMAT</code> locale.
     * @param zone the given time zone.
     * @see Category#FORMAT
     */
    public GregorianCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a GregorianCalendar based on the current time
     * in the default time zone with the given locale.
     * @param aLocale the given locale.
     */
    public GregorianCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    /**
     * <strong>[icu]</strong> Constructs a GregorianCalendar based on the current time
     * in the default time zone with the given locale.
     * @param locale the given ulocale.
     */
    public GregorianCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    /**
     * <strong>[icu]</strong> Constructs a GregorianCalendar based on the current time
     * in the given time zone with the given locale.
     * @param zone the given time zone.
     * @param aLocale the given locale.
     */
    public GregorianCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a GregorianCalendar based on the current time
     * in the given time zone with the given locale.
     * @param zone the given time zone.
     * @param locale the given ulocale.
     */
    public GregorianCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a GregorianCalendar with the given date set
     * in the default time zone with the default <code>FORMAT</code> locale.
     * @param year the value used to set the YEAR time field in the calendar.
     * @param month the value used to set the MONTH time field in the calendar.
     * Month value is 0-based. e.g., 0 for January.
     * @param date the value used to set the DATE time field in the calendar.
     * @see Category#FORMAT
     */
    public GregorianCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(ERA, AD);
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
    }

    /**
     * Constructs a GregorianCalendar with the given date
     * and time set for the default time zone with the default <code>FORMAT</code> locale.
     * @param year the value used to set the YEAR time field in the calendar.
     * @param month the value used to set the MONTH time field in the calendar.
     * Month value is 0-based. e.g., 0 for January.
     * @param date the value used to set the DATE time field in the calendar.
     * @param hour the value used to set the HOUR_OF_DAY time field
     * in the calendar.
     * @param minute the value used to set the MINUTE time field
     * in the calendar.
     * @see Category#FORMAT
     */
    public GregorianCalendar(int year, int month, int date, int hour,
                             int minute) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(ERA, AD);
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
    }

    /**
     * Constructs a GregorianCalendar with the given date
     * and time set for the default time zone with the default <code>FORMAT</code> locale.
     * @param year the value used to set the YEAR time field in the calendar.
     * @param month the value used to set the MONTH time field in the calendar.
     * Month value is 0-based. e.g., 0 for January.
     * @param date the value used to set the DATE time field in the calendar.
     * @param hour the value used to set the HOUR_OF_DAY time field
     * in the calendar.
     * @param minute the value used to set the MINUTE time field
     * in the calendar.
     * @param second the value used to set the SECOND time field
     * in the calendar.
     * @see Category#FORMAT
     */
    public GregorianCalendar(int year, int month, int date, int hour,
                             int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(ERA, AD);
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
        set(SECOND, second);
    }

/////////////////
// Public methods
/////////////////

    /**
     * Sets the GregorianCalendar change date. This is the point when the switch
     * from Julian dates to Gregorian dates occurred. Default is October 15,
     * 1582. Previous to this, dates will be in the Julian calendar.
     * <p>
     * To obtain a pure Julian calendar, set the change date to
     * <code>Date(Long.MAX_VALUE)</code>.  To obtain a pure Gregorian calendar,
     * set the change date to <code>Date(Long.MIN_VALUE)</code>.
     *
     * @param date the given Gregorian cutover date.
     */
    public void setGregorianChange(Date date) {
        gregorianCutover = date.getTime();

        // If the cutover has an extreme value, then create a pure
        // Gregorian or pure Julian calendar by giving the cutover year and
        // JD extreme values.
        if (gregorianCutover <= MIN_MILLIS) {
            gregorianCutoverYear = cutoverJulianDay = Integer.MIN_VALUE;
        } else if (gregorianCutover >= MAX_MILLIS) {
            gregorianCutoverYear = cutoverJulianDay = Integer.MAX_VALUE;
        } else {
            // Precompute two internal variables which we use to do the actual
            // cutover computations.  These are the Julian day of the cutover
            // and the cutover year.
            cutoverJulianDay = (int) floorDivide(gregorianCutover, ONE_DAY);
            
            // Convert cutover millis to extended year
            GregorianCalendar cal = new GregorianCalendar(getTimeZone());
            cal.setTime(date);
            gregorianCutoverYear = cal.get(EXTENDED_YEAR);
        }
    }

    /**
     * Gets the Gregorian Calendar change date.  This is the point when the
     * switch from Julian dates to Gregorian dates occurred. Default is
     * October 15, 1582. Previous to this, dates will be in the Julian
     * calendar.
     * @return the Gregorian cutover date for this calendar.
     */
    public final Date getGregorianChange() {
        return new Date(gregorianCutover);
    }

    /**
     * Determines if the given year is a leap year. Returns true if the
     * given year is a leap year.
     * @param year the given year.
     * @return true if the given year is a leap year; false otherwise.
     */
    public boolean isLeapYear(int year) {
        return year >= gregorianCutoverYear ?
            ((year%4 == 0) && ((year%100 != 0) || (year%400 == 0))) : // Gregorian
            (year%4 == 0); // Julian
    }

    /**
     * Returns true if the given Calendar object is equivalent to this
     * one.  Calendar override.
     *
     * @param other the Calendar to be compared with this Calendar   
     */
    public boolean isEquivalentTo(Calendar other) {
        return super.isEquivalentTo(other) &&
            gregorianCutover == ((GregorianCalendar)other).gregorianCutover;
    }

    /**
     * Override hashCode.
     * Generates the hash code for the GregorianCalendar object
     */
    public int hashCode() {
        return super.hashCode() ^ (int)gregorianCutover;
    }

    /**
     * Roll a field by a signed amount.
     */
    public void roll(int field, int amount) {

        switch (field) {
        case WEEK_OF_YEAR:
            {
                // Unlike WEEK_OF_MONTH, WEEK_OF_YEAR never shifts the day of the
                // week.  Also, rolling the week of the year can have seemingly
                // strange effects simply because the year of the week of year
                // may be different from the calendar year.  For example, the
                // date Dec 28, 1997 is the first day of week 1 of 1998 (if
                // weeks start on Sunday and the minimal days in first week is
                // <= 3).
                int woy = get(WEEK_OF_YEAR);
                // Get the ISO year, which matches the week of year.  This
                // may be one year before or after the calendar year.
                int isoYear = get(YEAR_WOY);
                int isoDoy = internalGet(DAY_OF_YEAR);
                if (internalGet(MONTH) == Calendar.JANUARY) {
                    if (woy >= 52) {
                        isoDoy += handleGetYearLength(isoYear);
                    }
                } else {
                    if (woy == 1) {
                        isoDoy -= handleGetYearLength(isoYear - 1);
                    }
                }
                woy += amount;
                // Do fast checks to avoid unnecessary computation:
                if (woy < 1 || woy > 52) {
                    // Determine the last week of the ISO year.
                    // We do this using the standard formula we use
                    // everywhere in this file.  If we can see that the
                    // days at the end of the year are going to fall into
                    // week 1 of the next year, we drop the last week by
                    // subtracting 7 from the last day of the year.
                    int lastDoy = handleGetYearLength(isoYear);
                    int lastRelDow = (lastDoy - isoDoy + internalGet(DAY_OF_WEEK) -
                                      getFirstDayOfWeek()) % 7;
                    if (lastRelDow < 0) lastRelDow += 7;
                    if ((6 - lastRelDow) >= getMinimalDaysInFirstWeek()) lastDoy -= 7;
                    int lastWoy = weekNumber(lastDoy, lastRelDow + 1);
                    woy = ((woy + lastWoy - 1) % lastWoy) + 1;
                }
                set(WEEK_OF_YEAR, woy);
                set(YEAR, isoYear); // Why not YEAR_WOY? - Alan 11/6/00
                return;
            }

        default:
            super.roll(field, amount);
            return;
        }
    }

    /**
     * Return the minimum value that this field could have, given the current date.
     * For the Gregorian calendar, this is the same as getMinimum() and getGreatestMinimum().
     */
    public int getActualMinimum(int field) {
        return getMinimum(field);
    }

    /**
     * Return the maximum value that this field could have, given the current date.
     * For example, with the date "Feb 3, 1997" and the DAY_OF_MONTH field, the actual
     * maximum would be 28; for "Feb 3, 1996" it s 29.  Similarly for a Hebrew calendar,
     * for some years the actual maximum for MONTH is 12, and for others 13.
     */
    public int getActualMaximum(int field) {
        /* It is a known limitation that the code here (and in getActualMinimum)
         * won't behave properly at the extreme limits of GregorianCalendar's
         * representable range (except for the code that handles the YEAR
         * field).  That's because the ends of the representable range are at
         * odd spots in the year.  For calendars with the default Gregorian
         * cutover, these limits are Sun Dec 02 16:47:04 GMT 292269055 BC to Sun
         * Aug 17 07:12:55 GMT 292278994 AD, somewhat different for non-GMT
         * zones.  As a result, if the calendar is set to Aug 1 292278994 AD,
         * the actual maximum of DAY_OF_MONTH is 17, not 30.  If the date is Mar
         * 31 in that year, the actual maximum month might be Jul, whereas is
         * the date is Mar 15, the actual maximum might be Aug -- depending on
         * the precise semantics that are desired.  Similar considerations
         * affect all fields.  Nonetheless, this effect is sufficiently arcane
         * that we permit it, rather than complicating the code to handle such
         * intricacies. - liu 8/20/98

         * UPDATE: No longer true, since we have pulled in the limit values on
         * the year. - Liu 11/6/00 */

        switch (field) {

        case YEAR:
            /* The year computation is no different, in principle, from the
             * others, however, the range of possible maxima is large.  In
             * addition, the way we know we've exceeded the range is different.
             * For these reasons, we use the special case code below to handle
             * this field.
             *
             * The actual maxima for YEAR depend on the type of calendar:
             *
             *     Gregorian = May 17, 292275056 BC - Aug 17, 292278994 AD
             *     Julian    = Dec  2, 292269055 BC - Jan  3, 292272993 AD
             *     Hybrid    = Dec  2, 292269055 BC - Aug 17, 292278994 AD
             *
             * We know we've exceeded the maximum when either the month, date,
             * time, or era changes in response to setting the year.  We don't
             * check for month, date, and time here because the year and era are
             * sufficient to detect an invalid year setting.  NOTE: If code is
             * added to check the month and date in the future for some reason,
             * Feb 29 must be allowed to shift to Mar 1 when setting the year.
             */
            {
                Calendar cal = (Calendar) clone();
                cal.setLenient(true);
                
                int era = cal.get(ERA);
                Date d = cal.getTime();

                /* Perform a binary search, with the invariant that lowGood is a
                 * valid year, and highBad is an out of range year.
                 */
                int lowGood = LIMITS[YEAR][1];
                int highBad = LIMITS[YEAR][2]+1;
                while ((lowGood + 1) < highBad) {
                    int y = (lowGood + highBad) / 2;
                    cal.set(YEAR, y);
                    if (cal.get(YEAR) == y && cal.get(ERA) == era) {
                        lowGood = y;
                    } else {
                        highBad = y;
                        cal.setTime(d); // Restore original fields
                    }
                }
                
                return lowGood;
            }

        default:
            return super.getActualMaximum(field);
        }
    }

//////////////////////
// Proposed public API
//////////////////////

    /**
     * Return true if the current time for this Calendar is in Daylignt
     * Savings Time.
     */
    boolean inDaylightTime() {
        if (!getTimeZone().useDaylightTime()) return false;
        complete(); // Force update of DST_OFFSET field
        return internalGet(DST_OFFSET) != 0;
    }


/////////////////////
// Calendar framework
/////////////////////

    /**
     */
    protected int handleGetMonthLength(int extendedYear, int month) {
        // If the month is out of range, adjust it into range, and
        // modify the extended year value accordingly.
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            extendedYear += floorDivide(month, 12, rem);
            month = rem[0];
        }

        return MONTH_COUNT[month][isLeapYear(extendedYear)?1:0];
    }

    /**
     */
    protected int handleGetYearLength(int eyear) {
        return isLeapYear(eyear) ? 366 : 365;
    }

/////////////////////////////
// Time => Fields computation
/////////////////////////////

    /**
     * Override Calendar to compute several fields specific to the hybrid
     * Gregorian-Julian calendar system.  These are:
     *
     * <ul><li>ERA
     * <li>YEAR
     * <li>MONTH
     * <li>DAY_OF_MONTH
     * <li>DAY_OF_YEAR
     * <li>EXTENDED_YEAR</ul>
     */
    protected void handleComputeFields(int julianDay) {
        int eyear, month, dayOfMonth, dayOfYear;

        if (julianDay >= cutoverJulianDay) {
            month = getGregorianMonth();
            dayOfMonth = getGregorianDayOfMonth();
            dayOfYear = getGregorianDayOfYear();
            eyear = getGregorianYear();
        } else {
            // The Julian epoch day (not the same as Julian Day)
            // is zero on Saturday December 30, 0 (Gregorian).
            long julianEpochDay = julianDay - (JAN_1_1_JULIAN_DAY - 2);
            eyear = (int) floorDivide(4*julianEpochDay + 1464, 1461);
            
            // Compute the Julian calendar day number for January 1, eyear
            long january1 = 365L*(eyear-1L) + floorDivide(eyear-1L, 4L);
            dayOfYear = (int)(julianEpochDay - january1); // 0-based
            
            // Julian leap years occurred historically every 4 years starting
            // with 8 AD.  Before 8 AD the spacing is irregular; every 3 years
            // from 45 BC to 9 BC, and then none until 8 AD.  However, we don't
            // implement this historical detail; instead, we implement the
            // computatinally cleaner proleptic calendar, which assumes
            // consistent 4-year cycles throughout time.
            boolean isLeap = ((eyear&0x3) == 0); // equiv. to (eyear%4 == 0)
            
            // Common Julian/Gregorian calculation
            int correction = 0;
            int march1 = isLeap ? 60 : 59; // zero-based DOY for March 1
            if (dayOfYear >= march1) {
                correction = isLeap ? 1 : 2;
            }
            month = (12 * (dayOfYear + correction) + 6) / 367; // zero-based month
            dayOfMonth = dayOfYear - MONTH_COUNT[month][isLeap?3:2] + 1; // one-based DOM
            ++dayOfYear;
        }
        internalSet(MONTH, month);
        internalSet(DAY_OF_MONTH, dayOfMonth);
        internalSet(DAY_OF_YEAR, dayOfYear);
        internalSet(EXTENDED_YEAR, eyear);
        int era = AD;
        if (eyear < 1) {
            era = BC;
            eyear = 1 - eyear;
        }
        internalSet(ERA, era);
        internalSet(YEAR, eyear);
    }

/////////////////////////////
// Fields => Time computation
/////////////////////////////

    /**
     */
    protected int handleGetExtendedYear() {
        int year;
        if (newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR) {
            year = internalGet(EXTENDED_YEAR, EPOCH_YEAR);
        } else {
            // The year defaults to the epoch start, the era to AD
            int era = internalGet(ERA, AD);
            if (era == BC) {
                year = 1 - internalGet(YEAR, 1); // Convert to extended year
            } else {
                year = internalGet(YEAR, EPOCH_YEAR);
            }
        }
        return year;
    }

    /**
     */
    protected int handleComputeJulianDay(int bestField) {

        invertGregorian = false;

        int jd = super.handleComputeJulianDay(bestField);

        // The following check handles portions of the cutover year BEFORE the
        // cutover itself happens.
        if (isGregorian != (jd >= cutoverJulianDay)) {
            invertGregorian = true;
            jd = super.handleComputeJulianDay(bestField);
        }
        
        return jd;
    }

    /**
     * Return JD of start of given month/year
     */
    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {

        // If the month is out of range, adjust it into range, and
        // modify the extended year value accordingly.
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            eyear += floorDivide(month, 12, rem);
            month = rem[0];
        }

        boolean isLeap = eyear%4 == 0;
        int y = eyear - 1;
        int julianDay = 365*y + floorDivide(y, 4) + (JAN_1_1_JULIAN_DAY - 3);

        isGregorian = (eyear >= gregorianCutoverYear);
        if (invertGregorian) {
            isGregorian = !isGregorian;
        }
        if (isGregorian) {
            isLeap = isLeap && ((eyear%100 != 0) || (eyear%400 == 0));
            // Add 2 because Gregorian calendar starts 2 days after
            // Julian calendar
            julianDay += floorDivide(y, 400) - floorDivide(y, 100) + 2;
        }

        // At this point julianDay indicates the day BEFORE the first
        // day of January 1, <eyear> of either the Julian or Gregorian
        // calendar.

        if (month != 0) {
            julianDay += MONTH_COUNT[month][isLeap?3:2];
        }

        return julianDay;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return "gregorian";
    }

    /*
    private static CalendarFactory factory;
    public static CalendarFactory factory() {
        if (factory == null) {
            factory = new CalendarFactory() {
                public Calendar create(TimeZone tz, ULocale loc) {
                    return new GregorianCalendar(tz, loc);
                }

                public String factoryName() {
                    return "Gregorian";
                }
            };
        }
        return factory;
    }
    */
}
