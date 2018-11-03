/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.util;

import java.util.Date;
import java.util.Locale;

import android.icu.util.ULocale.Category;

/**
 * <code>PersianCalendar</code> is a subclass of <code>Calendar</code> that
 * that implements the Persian calendar.  It is used as the main civil
 * calendar in Iran and Afghanistan, and by Iranians and Afghans worldwide.
 * <p>
 * The Persian calendar is solar, and is similar to the Gregorian calendar
 * in various ways, except its leap year rule, which is determined
 * astronomically.  The Persian year starts around the March equinox.
 * <p>
 * The modern Persian calendar (used in Iran since 1925 CE and in
 * Afghanistan since 1957 CE), has the lengths of the months fixed.  The
 * first six months are 31 days each, the next five months are 30 days each,
 * and the final month is 29 days in non-leap years and 30 days in leap
 * ones.  Historically, the lengths of the month differed in different
 * years, but they were finally fixed at the times mentioned above.  Partial
 * information is available about the historical lengths.
 * <p>
 * The official rule for determination of the beginning of the Persian year
 * is locale dependent, but at the same time, it has not specified a locale. 
 * Iranians around the world traditionally follow the calendar authorities
 * of Iran, which haven't officially specified the locale.  Some
 * calendarists use some point in Tehran as the locale, while others have
 * tried the more neutral 52.5 degrees east meridian.  It is not clear which
 * locale should be used for the Persian calendar of Afghanistan, but it is
 * expected that for about one year in every twenty-four years, the Afghan
 * calendar may become different from the Iranian one.
 * <p> 
 * The exact locale to be used for the Iranian calendar starts to make a
 * difference at around 2090 CE.  The specific arithmetic method implemented
 * here, commonly known as the 33-year cycle rule, matches the astronomical
 * calendar at least for the whole period that the calendar has been both
 * well-defined and official, from 1925 to around 2090 CE.  The other
 * commonly known algorithm, the 2820-year cycle, has been incorrectly
 * designed to follow the tropical year instead of the spring equinoctial
 * year, and fails to match the astronomical one as early as 2025 CE.
 * <p>
 * This class should not be subclassed.</p>
 * <p>
 * PersianCalendar usually should be instantiated using
 * {@link android.icu.util.Calendar#getInstance(ULocale)} passing in a
 * <code>ULocale</code> with the tag <code>"@calendar=persian"</code>.</p>
 *
 * @see android.icu.util.GregorianCalendar
 * @see android.icu.util.Calendar
 *
 * @author Roozbeh Pournader
 *
 * @deprecated This API is ICU internal only.
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
@Deprecated
public class PersianCalendar extends Calendar {
    private static final long serialVersionUID = -6727306982975111643L;

    //-------------------------------------------------------------------------
    // Constants...
    //-------------------------------------------------------------------------
    
    private static final int[][] MONTH_COUNT = {
        //len len2   st
        {  31,  31,   0 }, // Farvardin
        {  31,  31,  31 }, // Ordibehesht
        {  31,  31,  62 }, // Khordad
        {  31,  31,  93 }, // Tir
        {  31,  31, 124 }, // Mordad
        {  31,  31, 155 }, // Shahrivar
        {  30,  30, 186 }, // Mehr
        {  30,  30, 216 }, // Aban
        {  30,  30, 246 }, // Azar
        {  30,  30, 276 }, // Dey
        {  30,  30, 306 }, // Bahman
        {  29,  30, 336 }  // Esfand
        // len  length of month
        // len2 length of month in a leap year
        // st   days in year before start of month
    };
    
    private static final int PERSIAN_EPOCH = 1948320;

    //-------------------------------------------------------------------------
    // Constructors...
    //-------------------------------------------------------------------------

    /**
     * Constructs a default <code>PersianCalendar</code> using the current time
     * in the default time zone with the default <code>FORMAT</code> locale.
     * @see Category#FORMAT
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public PersianCalendar()
    {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a <code>PersianCalendar</code> based on the current time
     * in the given time zone with the default <code>FORMAT</code> locale.
     * @param zone the given time zone.
     * @see Category#FORMAT
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public PersianCalendar(TimeZone zone)
    {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a <code>PersianCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param aLocale the given locale.
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public PersianCalendar(Locale aLocale)
    {
        this(TimeZone.getDefault(), aLocale);
    }

    /**
     * Constructs a <code>PersianCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param locale the given ulocale.
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public PersianCalendar(ULocale locale)
    {
        this(TimeZone.getDefault(), locale);
    }

    /**
     * Constructs a <code>PersianCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone the given time zone.
     * @param aLocale the given locale.
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public PersianCalendar(TimeZone zone, Locale aLocale)
    {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a <code>PersianCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone the given time zone.
     * @param locale the given ulocale.
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public PersianCalendar(TimeZone zone, ULocale locale)
    {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a <code>PersianCalendar</code> with the given date set
     * in the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param date      The date to which the new calendar is set.
     * @see Category#FORMAT
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public PersianCalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.setTime(date);
    }

    /**
     * Constructs a <code>PersianCalendar</code> with the given date set
     * in the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param year the value used to set the {@link #YEAR YEAR} time field in the calendar.
     * @param month the value used to set the {@link #MONTH MONTH} time field in the calendar.
     *              Note that the month value is 0-based. e.g., 0 for Farvardin.
     * @param date the value used to set the {@link #DATE DATE} time field in the calendar.
     * @see Category#FORMAT
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public PersianCalendar(int year, int month, int date)
    {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.set(Calendar.YEAR, year);
        this.set(Calendar.MONTH, month);
        this.set(Calendar.DATE, date);
    }

    /**
     * Constructs a <code>PersianCalendar</code> with the given date
     * and time set for the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param year  the value used to set the {@link #YEAR YEAR} time field in the calendar.
     * @param month the value used to set the {@link #MONTH MONTH} time field in the calendar.
     *              Note that the month value is 0-based. e.g., 0 for Farvardin.
     * @param date  the value used to set the {@link #DATE DATE} time field in the calendar.
     * @param hour  the value used to set the {@link #HOUR_OF_DAY HOUR_OF_DAY} time field
     *              in the calendar.
     * @param minute the value used to set the {@link #MINUTE MINUTE} time field
     *              in the calendar.
     * @param second the value used to set the {@link #SECOND SECOND} time field
     *              in the calendar.
     * @see Category#FORMAT
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public PersianCalendar(int year, int month, int date, int hour,
                           int minute, int second)
    {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.set(Calendar.YEAR, year);
        this.set(Calendar.MONTH, month);
        this.set(Calendar.DATE, date);
        this.set(Calendar.HOUR_OF_DAY, hour);
        this.set(Calendar.MINUTE, minute);
        this.set(Calendar.SECOND, second);
    }

    //-------------------------------------------------------------------------
    // Minimum / Maximum access functions
    //-------------------------------------------------------------------------

    private static final int LIMITS[][] = {
        // Minimum  Greatest     Least   Maximum
        //           Minimum   Maximum
        {        0,        0,        0,        0}, // ERA
        { -5000000, -5000000,  5000000,  5000000}, // YEAR
        {        0,        0,       11,       11}, // MONTH
        {        1,        1,       52,       53}, // WEEK_OF_YEAR
        {/*                                   */}, // WEEK_OF_MONTH
        {        1,        1,       29,       31}, // DAY_OF_MONTH
        {        1,        1,      365,      366}, // DAY_OF_YEAR
        {/*                                   */}, // DAY_OF_WEEK
        {       -1,       -1,        5,        5}, // DAY_OF_WEEK_IN_MONTH
        {/*                                   */}, // AM_PM
        {/*                                   */}, // HOUR
        {/*                                   */}, // HOUR_OF_DAY
        {/*                                   */}, // MINUTE
        {/*                                   */}, // SECOND
        {/*                                   */}, // MILLISECOND
        {/*                                   */}, // ZONE_OFFSET
        {/*                                   */}, // DST_OFFSET
        { -5000000, -5000000,  5000000,  5000000}, // YEAR_WOY
        {/*                                   */}, // DOW_LOCAL
        { -5000000, -5000000,  5000000,  5000000}, // EXTENDED_YEAR
        {/*                                   */}, // JULIAN_DAY
        {/*                                   */}, // MILLISECONDS_IN_DAY
    };

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    //-------------------------------------------------------------------------
    // Assorted calculation utilities
    //

    /**
     * Determine whether a year is a leap year in the Persian calendar
     */
    private final static boolean isLeapYear(int year)
    {
        int[] remainder = new int[1];
        floorDivide(25 * year + 11, 33, remainder);
        return remainder[0] < 8;
        
    }

    //----------------------------------------------------------------------
    // Calendar framework
    //----------------------------------------------------------------------

    /**
     * Return the length (in days) of the given month.
     *
     * @param extendedYear  The Persian year
     * @param month The Persian month, 0-based
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
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
     * Return the number of days in the given Persian year
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected int handleGetYearLength(int extendedYear) {
        return isLeapYear(extendedYear) ? 366 : 365;
    }
    
    //-------------------------------------------------------------------------
    // Functions for converting from field values to milliseconds....
    //-------------------------------------------------------------------------

    /**
     * Return JD of start of given month/year
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        // If the month is out of range, adjust it into range, and
        // modify the extended year value accordingly.
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            eyear += floorDivide(month, 12, rem);
            month = rem[0];
        }

        int julianDay = PERSIAN_EPOCH - 1 + 365 * (eyear - 1) + floorDivide(8 * eyear + 21, 33);
        if (month != 0) {
            julianDay += MONTH_COUNT[month][2];
        }
        return julianDay;
    }    

    //-------------------------------------------------------------------------
    // Functions for converting from milliseconds to field values
    //-------------------------------------------------------------------------

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
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
     * Override Calendar to compute several fields specific to the Persian
     * calendar system.  These are:
     *
     * <ul><li>ERA
     * <li>YEAR
     * <li>MONTH
     * <li>DAY_OF_MONTH
     * <li>DAY_OF_YEAR
     * <li>EXTENDED_YEAR</ul>
     * 
     * The DAY_OF_WEEK and DOW_LOCAL fields are already set when this
     * method is called.
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected void handleComputeFields(int julianDay) {
        int year, month, dayOfMonth, dayOfYear;

        long daysSinceEpoch = julianDay - PERSIAN_EPOCH;
        year = 1 + (int) floorDivide(33 * daysSinceEpoch + 3, 12053);

        long farvardin1 = 365L * (year - 1L) + floorDivide(8L * year + 21, 33L);
        dayOfYear = (int)(daysSinceEpoch - farvardin1); // 0-based
        if (dayOfYear < 216) { // Compute 0-based month
            month = dayOfYear / 31;
        } else {
            month = (dayOfYear - 6) / 30;
        }
        dayOfMonth = dayOfYear - MONTH_COUNT[month][2] + 1;
        ++dayOfYear; // Make it 1-based now
        
        internalSet(ERA, 0);
        internalSet(YEAR, year);
        internalSet(EXTENDED_YEAR, year);
        internalSet(MONTH, month);
        internalSet(DAY_OF_MONTH, dayOfMonth);
        internalSet(DAY_OF_YEAR, dayOfYear);       
    }    

    /**
     * {@inheritDoc}
     *
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public String getType() {
        return "persian";
    }
}
