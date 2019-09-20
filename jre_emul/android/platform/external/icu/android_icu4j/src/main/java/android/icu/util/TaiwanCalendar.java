/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.util;

import java.util.Date;
import java.util.Locale;

/** 
 * <code>TaiwanCalendar</code> is a subclass of <code>GregorianCalendar</code>
 * that numbers years since 1912. 
 * <p>
 * The Taiwan calendar is identical to the Gregorian calendar in all respects
 * except for the year and era.  Years are numbered since 1912 AD (Gregorian).
 * <p>
 * The Taiwan Calendar has one era: <code>MINGUO</code>.
 * <p>
 * This class should not be subclassed.</p>
 * <p>
 * TaiwanCalendar usually should be instantiated using 
 * {@link android.icu.util.Calendar#getInstance(ULocale)} passing in a <code>ULocale</code>
 * with the tag <code>"@calendar=roc"</code>.</p>
 * 
 * @see android.icu.util.Calendar
 * @see android.icu.util.GregorianCalendar
 *
 * @author Laura Werner
 * @author Alan Liu
 * @author Steven R. Loomis
 */
public class TaiwanCalendar extends GregorianCalendar {
    // jdk1.4.2 serialver
    private static final long serialVersionUID = 2583005278132380631L;

    //-------------------------------------------------------------------------
    // Constructors...
    //-------------------------------------------------------------------------

    /**
     * Constant for the Taiwan Era for years before Minguo 1.
     * Brefore Minuo 1 is Gregorian 1911, Before Minguo 2 is Gregorian 1910
     * and so on.
     *
     * @see android.icu.util.Calendar#ERA
     */
    public static final int BEFORE_MINGUO = 0;

    /**
     * Constant for the Taiwan Era for Minguo.  Minguo 1 is 1912 in
     * Gregorian calendar.
     *
     * @see android.icu.util.Calendar#ERA
     */
    public static final int MINGUO = 1;

    /**
     * Constructs a <code>TaiwanCalendar</code> using the current time
     * in the default time zone with the default locale.
     */
    public TaiwanCalendar() {
        super();
    }

    /**
     * Constructs a <code>TaiwanCalendar</code> based on the current time
     * in the given time zone with the default locale.
     *
     * @param zone the given time zone.
     */
    public TaiwanCalendar(TimeZone zone) {
        super(zone);
    }

    /**
     * Constructs a <code>TaiwanCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param aLocale the given locale.
     */
    public TaiwanCalendar(Locale aLocale) {
        super(aLocale);
    }

    /**
     * Constructs a <code>TaiwanCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param locale the given ulocale.
     */
    public TaiwanCalendar(ULocale locale) {
        super(locale);
    }

    /**
     * Constructs a <code>TaiwanCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone the given time zone.
     */
    public TaiwanCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }

    /**
     * Constructs a <code>TaiwanCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone the given time zone.
     * @param locale the given ulocale.
     */
    public TaiwanCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
    }

    /**
     * Constructs a <code>TaiwanCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param date      The date to which the new calendar is set.
     */
    public TaiwanCalendar(Date date) {
        this();
        setTime(date);
    }

    /**
     * Constructs a <code>TaiwanCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for January.
     *
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     */
    public TaiwanCalendar(int year, int month, int date) {
        super(year, month, date);
    }

    /**
     * Constructs a TaiwanCalendar with the given date
     * and time set for the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for January.
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     * @param hour      The value used to set the calendar's {@link #HOUR_OF_DAY HOUR_OF_DAY} time field.
     * @param minute    The value used to set the calendar's {@link #MINUTE MINUTE} time field.
     * @param second    The value used to set the calendar's {@link #SECOND SECOND} time field.
     */
    public TaiwanCalendar(int year, int month, int date, int hour,
                             int minute, int second)
    {
        super(year, month, date, hour, minute, second);
    }


    //-------------------------------------------------------------------------
    // The only practical difference from a Gregorian calendar is that years
    // are numbered since 1912, inclusive.  A couple of overrides will
    // take care of that....
    //-------------------------------------------------------------------------
    
    private static final int Taiwan_ERA_START = 1911; // 0=1911, 1=1912

    // Use 1970 as the default value of EXTENDED_YEAR
    private static final int GREGORIAN_EPOCH = 1970;


    /**
     * {@inheritDoc}
     */    
    protected int handleGetExtendedYear() {
        // EXTENDED_YEAR in TaiwanCalendar is a Gregorian year
        // The default value of EXTENDED_YEAR is 1970 (Minguo 59)
        int year = GREGORIAN_EPOCH;
        if (newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR
                && newerField(EXTENDED_YEAR, ERA) == EXTENDED_YEAR) {
            year = internalGet(EXTENDED_YEAR, GREGORIAN_EPOCH);
        } else {
            int era = internalGet(ERA, MINGUO);
            if (era == MINGUO) {
                year = internalGet(YEAR, 1) + Taiwan_ERA_START;
            } else {
                year = 1 - internalGet(YEAR, 1) + Taiwan_ERA_START;
            }
        }
        return year;
    }

    /**
     * {@inheritDoc}
     */
    protected void handleComputeFields(int julianDay) {
        super.handleComputeFields(julianDay);
        int y = internalGet(EXTENDED_YEAR) - Taiwan_ERA_START;
        if (y > 0) {
            internalSet(ERA, MINGUO);
            internalSet(YEAR, y);
        } else {
            internalSet(ERA, BEFORE_MINGUO);
            internalSet(YEAR, 1- y);
        }
    }

    /**
     * Override GregorianCalendar.  There is only one Taiwan ERA.  We
     * should really handle YEAR, YEAR_WOY, and EXTENDED_YEAR here too to
     * implement the 1..5000000 range, but it's not critical.
     */
    protected int handleGetLimit(int field, int limitType) {
        if (field == ERA) {
            if (limitType == MINIMUM || limitType == GREATEST_MINIMUM) {
                return BEFORE_MINGUO;
            } else {
                return MINGUO;
            }
        }
        return super.handleGetLimit(field, limitType);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getType() {
        return "roc";
    }
}
