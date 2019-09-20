/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2005-2011, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.util;

import java.util.Date;
import java.util.Locale;

import android.icu.util.ULocale.Category;

/**
 * Base class for EthiopicCalendar and CopticCalendar.
 */
abstract class CECalendar extends Calendar {
    // jdk1.4.2 serialver
    private static final long serialVersionUID = -999547623066414271L;

    private static final int LIMITS[][] = {
        // Minimum  Greatest    Least  Maximum
        //           Minimum  Maximum
        {        0,        0,       1,       1 }, // ERA
        {        1,        1, 5000000, 5000000 }, // YEAR
        {        0,        0,      12,      12 }, // MONTH
        {        1,        1,      52,      53 }, // WEEK_OF_YEAR
        {/*                                  */}, // WEEK_OF_MONTH
        {        1,        1,       5,      30 }, // DAY_OF_MONTH
        {        1,        1,     365,     366 }, // DAY_OF_YEAR
        {/*                                  */}, // DAY_OF_WEEK
        {       -1,       -1,       1,       5 }, // DAY_OF_WEEK_IN_MONTH
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

    //-------------------------------------------------------------------------
    // Constructors...
    //-------------------------------------------------------------------------

    /**
     * Constructs a default <code>CECalendar</code> using the current time
     * in the default time zone with the default <code>FORMAT</code> locale.
     */
    protected CECalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a <code>CECalendar</code> based on the current time
     * in the given time zone with the default <code>FORMAT</code> locale.
     *
     * @param zone The time zone for the new calendar.
     */
    protected CECalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a <code>CECalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param aLocale The locale for the new calendar.
     */
    protected CECalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    /**
     * Constructs a <code>CECalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param locale The locale for the new calendar.
     */
    protected CECalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    /**
     * Constructs a <code>CECalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone The time zone for the new calendar.
     *
     * @param aLocale The locale for the new calendar.
     */
    protected CECalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a <code>CECalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone The time zone for the new calendar.
     *
     * @param locale The locale for the new calendar.
     */
    protected CECalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a <code>CECalendar</code> with the given date set
     * in the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for Tishri.
     *
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     */
    protected CECalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.set(year, month, date);
    }

    /**
     * Constructs a <code>CECalendar</code> with the given date set
     * in the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param date      The date to which the new calendar is set.
     */
    protected CECalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.setTime(date);
    }

    /**
     * Constructs a <code>CECalendar</code> with the given date
     * and time set for the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for Tishri.
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     * @param hour      The value used to set the calendar's {@link #HOUR_OF_DAY HOUR_OF_DAY} time field.
     * @param minute    The value used to set the calendar's {@link #MINUTE MINUTE} time field.
     * @param second    The value used to set the calendar's {@link #SECOND SECOND} time field.
     */
    protected CECalendar(int year, int month, int date, int hour,
                         int minute, int second)
    {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.set(year, month, date, hour, minute, second);
    }

    //-------------------------------------------------------------------------
    // Calendar framework
    //-------------------------------------------------------------------------

    /**
     * The Coptic and Ethiopic calendars differ only in their epochs.
     * This method must be implemented by CECalendar subclasses to
     * return the date offset from Julian.
     * @hide unsupported on Android
     */
    abstract protected int getJDEpochOffset();

    /**
     * Return JD of start of given month/extended year
     */
    protected int handleComputeMonthStart(int eyear,
                                          int emonth,
                                          boolean useMonth) {
        return ceToJD(eyear, emonth, 0, getJDEpochOffset());
    }

    /**
     * Calculate the limit for a specified type of limit and field
     */
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    // (The following method is not called because all existing subclasses
    // override it.
    ///CLOVER:OFF
    /**
     * Return the number of days in the given month of the given extended
     * year of this calendar system.  Subclasses should override this
     * method if they can provide a more correct or more efficient
     * implementation than the default implementation in Calendar.
     */
    protected int handleGetMonthLength(int extendedYear, int month)
    {

        // The Ethiopian and Coptic calendars have 13 months, 12 of 30 days each and 
        // an intercalary month at the end of the year of 5 or 6 days, depending whether 
        // the year is a leap year or not. The Leap Year follows the same rules as the 
        // Julian Calendar so that the extra month always has six days in the year before 
        // a Julian Leap Year.        
        if ((month + 1) % 13 != 0)
        {
            // not intercalary month
            return 30;
        }
        else
        {
            // intercalary month 5 days + possible leap day
            return ((extendedYear % 4) / 3) + 5;
        }

    }
    ///CLOVER:ON

    //-------------------------------------------------------------------------
    // Calendar framework
    //-------------------------------------------------------------------------

    /**
     * Convert an Coptic/Ethiopic year, month and day to a Julian day
     * @param year the extended year
     * @param month the month
     * @param day the day
     * @return Julian day
     * @hide unsupported on Android
     */
    public static int ceToJD(long year, int month, int day, int jdEpochOffset) {

        // Julian<->Ethiopic algorithms from:
        // "Calendars in Ethiopia", Berhanu Beyene, Manfred Kudlek, International Conference
        // of Ethiopian Studies XV, Hamburg, 2003

        // handle month > 12, < 0 (e.g. from add/set)
        if ( month >= 0 ) {
            year += month/13;
            month %= 13;
        } else {
            ++month;
            year += month/13 - 1;
            month = month%13 + 12;
        }
        return (int) (
            jdEpochOffset           // difference from Julian epoch to 1,1,1
            + 365 * year            // number of days from years
            + floorDivide(year, 4)  // extra day of leap year
            + 30 * month            // number of days from months (months are 0-based)
            + day - 1               // number of days for present month (1 based)
            );
    }

    /**
     * Convert a Julian day to an Coptic/Ethiopic year, month and day
     * @hide unsupported on Android
     */
    public static void jdToCE(int julianDay, int jdEpochOffset, int[] fields) {
        int c4; // number of 4 year cycle (1461 days)
        int[] r4 = new int[1]; // remainder of 4 year cycle, always positive

        c4 = floorDivide(julianDay - jdEpochOffset, 1461, r4);

        // exteded year
        fields[0] = 4 * c4 + (r4[0]/365 - r4[0]/1460); // 4 * <number of 4year cycle> + <years within the last cycle>

        int doy = (r4[0] == 1460) ? 365 : (r4[0] % 365); // days in present year

        // month
        fields[1] = doy / 30; // 30 -> Coptic/Ethiopic month length up to 12th month
        // day
        fields[2] = (doy % 30) + 1; // 1-based days in a month
    }
}
