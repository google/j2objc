/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*********************************************************************
 * Copyright (C) 2000-2014, International Business Machines
 * Corporation and others. All Rights Reserved.
 *********************************************************************
 */

package android.icu.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Locale;

import android.icu.impl.CalendarAstronomer;
import android.icu.impl.CalendarCache;
import android.icu.text.DateFormat;
import android.icu.util.ULocale.Category;

/**
 * <code>ChineseCalendar</code> is a concrete subclass of {@link Calendar}
 * that implements a traditional Chinese calendar.  The traditional Chinese
 * calendar is a lunisolar calendar: Each month starts on a new moon, and
 * the months are numbered according to solar events, specifically, to
 * guarantee that month 11 always contains the winter solstice.  In order
 * to accomplish this, leap months are inserted in certain years.  Leap
 * months are numbered the same as the month they follow.  The decision of
 * which month is a leap month depends on the relative movements of the sun
 * and moon.
 *
 * <p>All astronomical computations are performed with respect to a time
 * zone of GMT+8:00 and a longitude of 120 degrees east.  Although some
 * calendars implement a historically more accurate convention of using
 * Beijing's local longitude (116 degrees 25 minutes east) and time zone
 * (GMT+7:45:40) for dates before 1929, we do not implement this here.
 *
 * <p>Years are counted in two different ways in the Chinese calendar.  The
 * first method is by sequential numbering from the 61st year of the reign
 * of Huang Di, 2637 BCE, which is designated year 1 on the Chinese
 * calendar.  The second method uses 60-year cycles from the same starting
 * point, which is designated year 1 of cycle 1.  In this class, the
 * <code>EXTENDED_YEAR</code> field contains the sequential year count.
 * The <code>ERA</code> field contains the cycle number, and the
 * <code>YEAR</code> field contains the year of the cycle, a value between
 * 1 and 60.
 *
 * <p>There is some variation in what is considered the starting point of
 * the calendar, with some sources starting in the first year of the reign
 * of Huang Di, rather than the 61st.  This gives continuous year numbers
 * 60 years greater and cycle numbers one greater than what this class
 * implements.
 *
 * <p>Because <code>ChineseCalendar</code> defines an additional field and
 * redefines the way the <code>ERA</code> field is used, it requires a new
 * format class, <code>ChineseDateFormat</code>.  As always, use the
 * methods <code>DateFormat.getXxxInstance(Calendar cal,...)</code> to
 * obtain a formatter for this calendar.
 *
 * <p>References:<ul>
 * 
 * <li>Dershowitz and Reingold, <i>Calendrical Calculations</i>,
 * Cambridge University Press, 1997</li>
 * 
 * <li>Helmer Aslaksen's
 * <a href="http://www.math.nus.edu.sg/aslaksen/calendar/chinese.shtml">
 * Chinese Calendar page</a></li>
 *
 * <li>The <a href="http://www.tondering.dk/claus/calendar.html">
 * Calendar FAQ</a></li>
 *
 * </ul>
 *
 * <p>
 * This class should not be subclassed.</p>
 * <p>
 * ChineseCalendar usually should be instantiated using 
 * {@link android.icu.util.Calendar#getInstance(ULocale)} passing in a <code>ULocale</code>
 * with the tag <code>"@calendar=chinese"</code>.</p>
 *
 * @see android.icu.util.Calendar
 * @author Alan Liu
 */
public class ChineseCalendar extends Calendar {
    // jdk1.4.2 serialver
    private static final long serialVersionUID = 7312110751940929420L;

    //------------------------------------------------------------------
    // Developer Notes
    // 
    // Time is represented as a scalar in two ways in this class.  One is
    // the usual UTC epoch millis, that is, milliseconds after January 1,
    // 1970 Gregorian, 0:00:00.000 UTC.  The other is in terms of 'local
    // days.'  This is the number of days after January 1, 1970 Gregorian,
    // local to Beijing, China (since all computations of the Chinese
    // calendar are done in Beijing).  That is, 0 represents January 1,
    // 1970 0:00 Asia/Shanghai.  Conversion of local days to and from
    // standard epoch milliseconds is accomplished by the daysToMillis()
    // and millisToDays() methods.
    // 
    // Several methods use caches to improve performance.  Caches are at
    // the object, not class level, under the assumption that typical
    // usage will be to have one instance of ChineseCalendar at a time.
 
    /**
     * The start year of this Chinese calendar instance. 
     */
    private int epochYear;

    /**
     * The zone used for the astronomical calculation of this Chinese
     * calendar instance.
     */
    private TimeZone zoneAstro;

    /**
     * We have one instance per object, and we don't synchronize it because
     * Calendar doesn't support multithreaded execution in the first place.
     */
    private transient CalendarAstronomer astro = new CalendarAstronomer();

    /**
     * Cache that maps Gregorian year to local days of winter solstice.
     * @see #winterSolstice
     */
    private transient CalendarCache winterSolsticeCache = new CalendarCache();

    /**
     * Cache that maps Gregorian year to local days of Chinese new year.
     * @see #newYear
     */
    private transient CalendarCache newYearCache = new CalendarCache();

    /**
     * True if the current year is a leap year.  Updated with each time to
     * fields resolution.
     * @see #computeChineseFields
     */
    private transient boolean isLeapYear;

    //------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------

    /**
     * Construct a <code>ChineseCalendar</code> with the default time zone and locale.
     */
    public ChineseCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT), CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    /**
     * Construct a <code>ChineseCalendar</code> with the give date set in the default time zone
     * with the default locale.
     * @param date The date to which the new calendar is set.
     */
    public ChineseCalendar(Date date) {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT), CHINESE_EPOCH_YEAR, CHINA_ZONE);
        setTime(date);
    }

    /**
     * Constructs a <code>ChineseCalendar</code> with the given date set
     * in the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for January.
     * @param isLeapMonth The value used to set the Chinese calendar's {@link #IS_LEAP_MONTH}
     *                  time field.
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     * @see Category#FORMAT
     */
    public ChineseCalendar(int year, int month, int isLeapMonth, int date) {
        this(year, month, isLeapMonth, date, 0, 0, 0);
    }

    /**
     * Constructs a <code>ChineseCalendar</code> with the given date
     * and time set for the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param year  the value used to set the {@link #YEAR YEAR} time field in the calendar.
     * @param month the value used to set the {@link #MONTH MONTH} time field in the calendar.
     *              Note that the month value is 0-based. e.g., 0 for January.
     * @param isLeapMonth the value used to set the {@link #IS_LEAP_MONTH} time field
     *              in the calendar.
     * @param date  the value used to set the {@link #DATE DATE} time field in the calendar.
     * @param hour  the value used to set the {@link #HOUR_OF_DAY HOUR_OF_DAY} time field
     *              in the calendar.
     * @param minute the value used to set the {@link #MINUTE MINUTE} time field
     *              in the calendar.
     * @param second the value used to set the {@link #SECOND SECOND} time field
     *              in the calendar.
     * @see Category#FORMAT
     */
    public ChineseCalendar(int year, int month, int isLeapMonth, int date, int hour,
                             int minute, int second)
    {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT), CHINESE_EPOCH_YEAR, CHINA_ZONE);

        // The current time is set at this point, so ERA field is already
        // set to the current era.

        // Then we need to clean up time fields
        this.set(MILLISECOND, 0);

        // Then, set the given field values.
        this.set(YEAR, year);
        this.set(MONTH, month);
        this.set(IS_LEAP_MONTH, isLeapMonth);
        this.set(DATE, date);
        this.set(HOUR_OF_DAY, hour);
        this.set(MINUTE, minute);
        this.set(SECOND, second);
    }

    /** 
     * Constructs a <code>ChineseCalendar</code> with the given date set 
     * in the default time zone with the default <code>FORMAT</code> locale. 
     * 
     * @param era       The value used to set the calendar's {@link #ERA ERA} time field. 
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field. 
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field. 
     *                  The value is 0-based. e.g., 0 for January. 
     * @param isLeapMonth The value used to set the Chinese calendar's {@link #IS_LEAP_MONTH}
     *                  time field. 
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     * @see Category#FORMAT
     */ 
    public ChineseCalendar(int era, int year, int month, int isLeapMonth, int date) 
    { 
        this(era, year, month, isLeapMonth, date, 0, 0, 0);
    } 
  
    /** 
     * Constructs a <code>ChineseCalendar</code> with the given date 
     * and time set for the default time zone with the default <code>FORMAT</code> locale. 
     * 
     * @param era   the value used to set the calendar's {@link #ERA ERA} time field. 
     * @param year  the value used to set the {@link #YEAR YEAR} time field in the calendar. 
     * @param month the value used to set the {@link #MONTH MONTH} time field in the calendar. 
     *              Note that the month value is 0-based. e.g., 0 for January. 
     * @param isLeapMonth the value used to set the {@link #IS_LEAP_MONTH} time field 
     *              in the calendar. 
     * @param date  the value used to set the {@link #DATE DATE} time field in the calendar. 
     * @param hour  the value used to set the {@link #HOUR_OF_DAY HOUR_OF_DAY} time field 
     *              in the calendar. 
     * @param minute the value used to set the {@link #MINUTE MINUTE} time field 
     *              in the calendar. 
     * @param second the value used to set the {@link #SECOND SECOND} time field 
     *              in the calendar.
     * @see Category#FORMAT
     */
    public ChineseCalendar(int era, int year, int month, int isLeapMonth, int date, int hour, 
                           int minute, int second) 
    { 
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT), CHINESE_EPOCH_YEAR, CHINA_ZONE);

        // Set 0 to millisecond field 
        this.set(MILLISECOND, 0); 

        // Then, set the given field values. 
        this.set(ERA, era); 
        this.set(YEAR, year); 
        this.set(MONTH, month); 
        this.set(IS_LEAP_MONTH, isLeapMonth); 
        this.set(DATE, date); 
        this.set(HOUR_OF_DAY, hour); 
        this.set(MINUTE, minute); 
        this.set(SECOND, second); 
    }     
    
    /**
     * Constructs a <code>ChineseCalendar</code> based on the current time
     * in the default time zone with the given locale.
     * @param aLocale The given locale
     */
    public ChineseCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), ULocale.forLocale(aLocale), CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    /**
     * Construct a <code>ChineseCalendar</code> based on the current time
     * in the given time zone with the default <code>FORMAT</code> locale.
     * @param zone the given time zone
     * @see Category#FORMAT
     */
    public ChineseCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT), CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    /**
     * Construct a <code>ChineseCalendar</code> based on the current time
     * in the given time zone with the given locale.
     * @param zone the given time zone
     * @param aLocale the given locale
     */
    public ChineseCalendar(TimeZone zone, Locale aLocale) {
        this(zone, ULocale.forLocale(aLocale), CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    /**
     * Constructs a <code>ChineseCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param locale the given ulocale
     */
    public ChineseCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale, CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    /**
     * Construct a <code>ChineseCalendar</code>  based on the current time
     * with the given time zone with the given locale.
     * @param zone the given time zone
     * @param locale the given ulocale
     */
    public ChineseCalendar(TimeZone zone, ULocale locale) {
        this(zone, locale, CHINESE_EPOCH_YEAR, CHINA_ZONE);
    }

    /**
     * Construct a <code>ChineseCalenar</code> based on the current time
     * with the given time zone, the locale, the epoch year and the time zone
     * used for astronomical calculation.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected ChineseCalendar(TimeZone zone, ULocale locale, int epochYear, TimeZone zoneAstroCalc) {
        super(zone, locale);
        this.epochYear = epochYear;
        this.zoneAstro = zoneAstroCalc;
        setTimeInMillis(System.currentTimeMillis());
    }

    //------------------------------------------------------------------
    // Public constants
    //------------------------------------------------------------------

    /**
     * Field indicating whether or not the current month is a leap month.
     * Should have a value of 0 for non-leap months, and 1 for leap months.
     * @stable ICU 2.8
     */
    // public static int IS_LEAP_MONTH = BASE_FIELD_COUNT;


    //------------------------------------------------------------------
    // Calendar framework
    //------------------------------------------------------------------

    /**
     * Array defining the limits of field values for this class.  Field
     * limits which are invariant with respect to calendar system and
     * defined by Calendar are left blank.
     *
     * Notes:
     *
     * ERA 5000000 / 60 = 83333.
     *
     * MONTH There are 12 or 13 lunar months in a year.  However, we always
     * number them 0..11, with an intercalated, identically numbered leap
     * month, when necessary.
     *
     * DAY_OF_YEAR In a non-leap year there are 353, 354, or 355 days.  In
     * a leap year there are 383, 384, or 385 days.
     *
     * WEEK_OF_YEAR The least maximum occurs if there are 353 days in the
     * year, and the first 6 are the last week of the previous year.  Then
     * we have 49 full weeks and 4 days in the last week: 6 + 49*7 + 4 =
     * 353.  So the least maximum is 50.  The maximum occurs if there are
     * 385 days in the year, and WOY 1 extends 6 days into the prior year.
     * Then there are 54 full weeks, and 6 days in the last week: 1 + 54*7
     * + 6 = 385.  The 6 days of the last week will fall into WOY 1 of the
     * next year.  Maximum is 55.
     *
     * WEEK_OF_MONTH In a 29 day month, if the first 7 days make up week 1
     * that leaves 3 full weeks and 1 day at the end.  The least maximum is
     * thus 5.  In a 30 days month, if the previous 6 days belong WOM 1 of
     * this month, we have 4 full weeks and 1 days at the end (which
     * technically will be WOM 1 of the next month, but will be reported by
     * time->fields and hence by getActualMaximum as WOM 6 of this month).
     * Maximum is 6.
     *
     * DAY_OF_WEEK_IN_MONTH In a 29 or 30 day month, there are 4 full weeks
     * plus 1 or 2 days at the end, so the maximum is always 5.
     */
    private static final int LIMITS[][] = {
        // Minimum  Greatest    Least  Maximum
        //           Minimum  Maximum
        {        1,        1,   83333,   83333 }, // ERA
        {        1,        1,      60,      60 }, // YEAR
        {        0,        0,      11,      11 }, // MONTH
        {        1,        1,      50,      55 }, // WEEK_OF_YEAR
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
        {        0,        0,       1,       1 }, // IS_LEAP_MONTH
    };

    /**
     * Override Calendar to return the limit value for the given field.
     */
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    /**
     * Implement abstract Calendar method to return the extended year
     * defined by the current fields.  This will use either the ERA and
     * YEAR field as the cycle and year-of-cycle, or the EXTENDED_YEAR
     * field as the continuous year count, depending on which is newer.
     */
    protected int handleGetExtendedYear() {
        int year;
        if (newestStamp(ERA, YEAR, UNSET) <= getStamp(EXTENDED_YEAR)) {
            year = internalGet(EXTENDED_YEAR, 1); // Default to year 1
        } else {
            int cycle = internalGet(ERA, 1) - 1; // 0-based cycle
            // adjust to the instance specific epoch
            year = cycle * 60 + internalGet(YEAR, 1) - (epochYear - CHINESE_EPOCH_YEAR);
        }
        return year;
    }

    /**
     * Override Calendar method to return the number of days in the given
     * extended year and month.
     *
     * <p>Note: This method also reads the IS_LEAP_MONTH field to determine
     * whether or not the given month is a leap month.
     */
    protected int handleGetMonthLength(int extendedYear, int month) {
        int thisStart = handleComputeMonthStart(extendedYear, month, true) -
            EPOCH_JULIAN_DAY + 1; // Julian day -> local days
        int nextStart = newMoonNear(thisStart + SYNODIC_GAP, true);
        return nextStart - thisStart;
    }

    /**
     * {@inheritDoc}
     */
    protected DateFormat handleGetDateFormat(String pattern, String override, ULocale locale) {
        // Note: ICU 50 or later versions no longer use ChineseDateFormat.
        // The super class's handleGetDateFormat will create an instance of
        // SimpleDateFormat which supports Chinese calendar date formatting
        // since ICU 49.

        //return new ChineseDateFormat(pattern, override, locale);
        return super.handleGetDateFormat(pattern, override, locale);
    }

    /**
     * Field resolution table that incorporates IS_LEAP_MONTH.
     */
    static final int[][][] CHINESE_DATE_PRECEDENCE = {
        {
            { DAY_OF_MONTH },
            { WEEK_OF_YEAR, DAY_OF_WEEK },
            { WEEK_OF_MONTH, DAY_OF_WEEK },
            { DAY_OF_WEEK_IN_MONTH, DAY_OF_WEEK },
            { WEEK_OF_YEAR, DOW_LOCAL },
            { WEEK_OF_MONTH, DOW_LOCAL },
            { DAY_OF_WEEK_IN_MONTH, DOW_LOCAL },
            { DAY_OF_YEAR },
            { RESOLVE_REMAP | DAY_OF_MONTH, IS_LEAP_MONTH },
        },
        {
            { WEEK_OF_YEAR },
            { WEEK_OF_MONTH },
            { DAY_OF_WEEK_IN_MONTH },
            { RESOLVE_REMAP | DAY_OF_WEEK_IN_MONTH, DAY_OF_WEEK },
            { RESOLVE_REMAP | DAY_OF_WEEK_IN_MONTH, DOW_LOCAL },
        },
    };

    /**
     * Override Calendar to add IS_LEAP_MONTH to the field resolution
     * table.
     */
    protected int[][][] getFieldResolutionTable() {
        return CHINESE_DATE_PRECEDENCE;
    }

    /**
     * Adjust this calendar to be delta months before or after a given
     * start position, pinning the day of month if necessary.  The start
     * position is given as a local days number for the start of the month
     * and a day-of-month.  Used by add() and roll().
     * @param newMoon the local days of the first day of the month of the
     * start position (days after January 1, 1970 0:00 Asia/Shanghai)
     * @param dom the 1-based day-of-month of the start position
     * @param delta the number of months to move forward or backward from
     * the start position
     */
    private void offsetMonth(int newMoon, int dom, int delta) {
        // Move to the middle of the month before our target month.
        newMoon += (int) (CalendarAstronomer.SYNODIC_MONTH * (delta - 0.5));

        // Search forward to the target month's new moon
        newMoon = newMoonNear(newMoon, true);

        // Find the target dom
        int jd = newMoon + EPOCH_JULIAN_DAY - 1 + dom;

        // Pin the dom.  In this calendar all months are 29 or 30 days
        // so pinning just means handling dom 30.
        if (dom > 29) {
            set(JULIAN_DAY, jd-1);
            // TODO Fix this.  We really shouldn't ever have to
            // explicitly call complete().  This is either a bug in
            // this method, in ChineseCalendar, or in
            // Calendar.getActualMaximum().  I suspect the last.
            complete();
            if (getActualMaximum(DAY_OF_MONTH) >= dom) {
                set(JULIAN_DAY, jd);
            }
        } else {
            set(JULIAN_DAY, jd);
        }
    }

    /**
     * Override Calendar to handle leap months properly.
     */
    public void add(int field, int amount) {
        switch (field) {
        case MONTH:
            if (amount != 0) {
                int dom = get(DAY_OF_MONTH);
                int day = get(JULIAN_DAY) - EPOCH_JULIAN_DAY; // Get local day
                int moon = day - dom + 1; // New moon 
                offsetMonth(moon, dom, amount);
            }
            break;
        default:
            super.add(field, amount);
            break;
        }
    }

    /**
     * Override Calendar to handle leap months properly.
     */
    public void roll(int field, int amount) {
        switch (field) {
        case MONTH:
            if (amount != 0) {
                int dom = get(DAY_OF_MONTH);
                int day = get(JULIAN_DAY) - EPOCH_JULIAN_DAY; // Get local day
                int moon = day - dom + 1; // New moon (start of this month)

                // Note throughout the following:  Months 12 and 1 are never
                // followed by a leap month (D&R p. 185).

                // Compute the adjusted month number m.  This is zero-based
                // value from 0..11 in a non-leap year, and from 0..12 in a
                // leap year.
                int m = get(MONTH); // 0-based month
                if (isLeapYear) { // (member variable)
                    if (get(IS_LEAP_MONTH) == 1) {
                        ++m;
                    } else {
                        // Check for a prior leap month.  (In the
                        // following, month 0 is the first month of the
                        // year.)  Month 0 is never followed by a leap
                        // month, and we know month m is not a leap month.
                        // moon1 will be the start of month 0 if there is
                        // no leap month between month 0 and month m;
                        // otherwise it will be the start of month 1.
                        int moon1 = moon -
                            (int) (CalendarAstronomer.SYNODIC_MONTH * (m - 0.5));
                        moon1 = newMoonNear(moon1, true);
                        if (isLeapMonthBetween(moon1, moon)) {
                            ++m;
                        }
                    }
                }

                // Now do the standard roll computation on m, with the
                // allowed range of 0..n-1, where n is 12 or 13.
                int n = isLeapYear ? 13 : 12; // Months in this year
                int newM = (m + amount) % n;
                if (newM < 0) {
                    newM += n;
                }

                if (newM != m) {
                    offsetMonth(moon, dom, newM - m);
                }
            }
            break;
        default:
            super.roll(field, amount);
            break;
        }
    }

    //------------------------------------------------------------------
    // Support methods and constants
    //------------------------------------------------------------------
   
    /**
     * The start year of the Chinese calendar, the 61st year of the reign
     * of Huang Di.  Some sources use the first year of his reign,
     * resulting in EXTENDED_YEAR values 60 years greater and ERA (cycle)
     * values one greater.
     */
    private static final int CHINESE_EPOCH_YEAR = -2636; // Gregorian year

    /**
     * The time zone used for performing astronomical computations.
     * Some sources use a different historically accurate
     * offset of GMT+7:45:40 for years before 1929; we do not do this.
     */
    private static final TimeZone CHINA_ZONE = new SimpleTimeZone(8 * ONE_HOUR, "CHINA_ZONE").freeze();

    /**
     * Value to be added or subtracted from the local days of a new moon to
     * get close to the next or prior new moon, but not cross it.  Must be
     * >= 1 and < CalendarAstronomer.SYNODIC_MONTH.
     */
    private static final int SYNODIC_GAP = 25;

    /**
     * Convert local days to UTC epoch milliseconds.
     * This is not an accurate conversion in terms that getTimezoneOffset 
     * takes the milliseconds in GMT (not local time).  In theory, more 
     * accurate algorithm can be implemented but practically we do not need 
     * to go through that complication as long as the historically timezone 
     * changes did not happen around the 'tricky' new moon (new moon around 
     * the midnight). 
     *  
     * @param days days after January 1, 1970 0:00 in the astronomical base zone
     * @return milliseconds after January 1, 1970 0:00 GMT
     */
    private final long daysToMillis(int days) {
        long millis = days * ONE_DAY;
        return millis - zoneAstro.getOffset(millis);
    }

    /**
     * Convert UTC epoch milliseconds to local days.
     * @param millis milliseconds after January 1, 1970 0:00 GMT
     * @return days days after January 1, 1970 0:00 in the astronomical base zone
     */
    private final int millisToDays(long millis) {
        return (int) floorDivide(millis + zoneAstro.getOffset(millis), ONE_DAY);
    }

    //------------------------------------------------------------------
    // Astronomical computations
    //------------------------------------------------------------------
    
    /**
     * Return the major solar term on or after December 15 of the given
     * Gregorian year, that is, the winter solstice of the given year.
     * Computations are relative to Asia/Shanghai time zone.
     * @param gyear a Gregorian year
     * @return days after January 1, 1970 0:00 Asia/Shanghai of the
     * winter solstice of the given year
     */
    private int winterSolstice(int gyear) {

        long cacheValue = winterSolsticeCache.get(gyear);

        if (cacheValue == CalendarCache.EMPTY) {
            // In books December 15 is used, but it fails for some years
            // using our algorithms, e.g.: 1298 1391 1492 1553 1560.  That
            // is, winterSolstice(1298) starts search at Dec 14 08:00:00
            // PST 1298 with a final result of Dec 14 10:31:59 PST 1299.
            long ms = daysToMillis(computeGregorianMonthStart(gyear, DECEMBER) +
                                   1 - EPOCH_JULIAN_DAY);
            astro.setTime(ms);
            
            // Winter solstice is 270 degrees solar longitude aka Dongzhi
            long solarLong = astro.getSunTime(CalendarAstronomer.WINTER_SOLSTICE,
                                              true);
            cacheValue = millisToDays(solarLong);
            winterSolsticeCache.put(gyear, cacheValue);
        }
        return (int) cacheValue;
    }

    /**
     * Return the closest new moon to the given date, searching either
     * forward or backward in time.
     * @param days days after January 1, 1970 0:00 Asia/Shanghai
     * @param after if true, search for a new moon on or after the given
     * date; otherwise, search for a new moon before it
     * @return days after January 1, 1970 0:00 Asia/Shanghai of the nearest
     * new moon after or before <code>days</code>
     */
    private int newMoonNear(int days, boolean after) {
        
        astro.setTime(daysToMillis(days));
        long newMoon = astro.getMoonTime(CalendarAstronomer.NEW_MOON, after);
        
        return millisToDays(newMoon);
    }

    /**
     * Return the nearest integer number of synodic months between
     * two dates.
     * @param day1 days after January 1, 1970 0:00 Asia/Shanghai
     * @param day2 days after January 1, 1970 0:00 Asia/Shanghai
     * @return the nearest integer number of months between day1 and day2
     */
    private int synodicMonthsBetween(int day1, int day2) {
        return (int) Math.round((day2 - day1) / CalendarAstronomer.SYNODIC_MONTH);
    }

    /**
     * Return the major solar term on or before a given date.  This
     * will be an integer from 1..12, with 1 corresponding to 330 degrees,
     * 2 to 0 degrees, 3 to 30 degrees,..., and 12 to 300 degrees.
     * @param days days after January 1, 1970 0:00 Asia/Shanghai
     */
    private int majorSolarTerm(int days) {
        
        astro.setTime(daysToMillis(days));

        // Compute (floor(solarLongitude / (pi/6)) + 2) % 12
        int term = ((int) Math.floor(6 * astro.getSunLongitude() / Math.PI) + 2) % 12;
        if (term < 1) {
            term += 12;
        }
        return term;
    }

    /**
     * Return true if the given month lacks a major solar term.
     * @param newMoon days after January 1, 1970 0:00 Asia/Shanghai of a new
     * moon
     */
    private boolean hasNoMajorSolarTerm(int newMoon) {
        
        int mst = majorSolarTerm(newMoon);
        int nmn = newMoonNear(newMoon + SYNODIC_GAP, true);
        int mstt = majorSolarTerm(nmn);
        return mst == mstt;
        /*
        return majorSolarTerm(newMoon) ==
            majorSolarTerm(newMoonNear(newMoon + SYNODIC_GAP, true));
        */
    }

    //------------------------------------------------------------------
    // Time to fields
    //------------------------------------------------------------------
    
    /**
     * Return true if there is a leap month on or after month newMoon1 and
     * at or before month newMoon2.
     * @param newMoon1 days after January 1, 1970 0:00 astronomical base zone of a
     * new moon
     * @param newMoon2 days after January 1, 1970 0:00 astronomical base zone of a
     * new moon
     */
    private boolean isLeapMonthBetween(int newMoon1, int newMoon2) {

        // This is only needed to debug the timeOfAngle divergence bug.
        // Remove this later. Liu 11/9/00
        // DEBUG
        if (synodicMonthsBetween(newMoon1, newMoon2) >= 50) {
            throw new IllegalArgumentException("isLeapMonthBetween(" + newMoon1 +
                                               ", " + newMoon2 +
                                               "): Invalid parameters");
        }

        return (newMoon2 >= newMoon1) &&
            (isLeapMonthBetween(newMoon1, newMoonNear(newMoon2 - SYNODIC_GAP, false)) ||
             hasNoMajorSolarTerm(newMoon2));
    }

    /**
     * Override Calendar to compute several fields specific to the Chinese
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
     * method is called.  The getGregorianXxx() methods return Gregorian
     * calendar equivalents for the given Julian day.
     *
     * <p>Compute the ChineseCalendar-specific field IS_LEAP_MONTH.
     */
    protected void handleComputeFields(int julianDay) {

        computeChineseFields(julianDay - EPOCH_JULIAN_DAY, // local days
                             getGregorianYear(), getGregorianMonth(),
                             true); // set all fields
    }

    /**
     * Compute fields for the Chinese calendar system.  This method can
     * either set all relevant fields, as required by
     * <code>handleComputeFields()</code>, or it can just set the MONTH and
     * IS_LEAP_MONTH fields, as required by
     * <code>handleComputeMonthStart()</code>.
     *
     * <p>As a side effect, this method sets {@link #isLeapYear}.
     * @param days days after January 1, 1970 0:00 astronomical base zone of the
     * date to compute fields for
     * @param gyear the Gregorian year of the given date
     * @param gmonth the Gregorian month of the given date
     * @param setAllFields if true, set the EXTENDED_YEAR, ERA, YEAR,
     * DAY_OF_MONTH, and DAY_OF_YEAR fields.  In either case set the MONTH
     * and IS_LEAP_MONTH fields.
     */
    private void computeChineseFields(int days, int gyear, int gmonth,
                                      boolean setAllFields) {

        // Find the winter solstices before and after the target date.
        // These define the boundaries of this Chinese year, specifically,
        // the position of month 11, which always contains the solstice.
        // We want solsticeBefore <= date < solsticeAfter.
        int solsticeBefore;
        int solsticeAfter = winterSolstice(gyear);
        if (days < solsticeAfter) {
            solsticeBefore = winterSolstice(gyear - 1);
        } else {
            solsticeBefore = solsticeAfter;
            solsticeAfter = winterSolstice(gyear + 1);
        }

        // Find the start of the month after month 11.  This will be either
        // the prior month 12 or leap month 11 (very rare).  Also find the
        // start of the following month 11.
        int firstMoon = newMoonNear(solsticeBefore + 1, true);
        int lastMoon = newMoonNear(solsticeAfter + 1, false);
        int thisMoon = newMoonNear(days + 1, false); // Start of this month
        // Note: isLeapYear is a member variable
        isLeapYear = synodicMonthsBetween(firstMoon, lastMoon) == 12;

        int month = synodicMonthsBetween(firstMoon, thisMoon);
        if (isLeapYear && isLeapMonthBetween(firstMoon, thisMoon)) {
            month--;
        }
        if (month < 1) {
            month += 12;
        }

        boolean isLeapMonth = isLeapYear &&
            hasNoMajorSolarTerm(thisMoon) &&
            !isLeapMonthBetween(firstMoon, newMoonNear(thisMoon - SYNODIC_GAP, false));

        internalSet(MONTH, month-1); // Convert from 1-based to 0-based
        internalSet(IS_LEAP_MONTH, isLeapMonth?1:0);

        if (setAllFields) {

            // Extended year and cycle year is based on the epoch year
            int extended_year = gyear - epochYear;
            int cycle_year = gyear - CHINESE_EPOCH_YEAR;
            if (month < 11 ||
                gmonth >= JULY) {
                extended_year++;
                cycle_year++;
            }
            int dayOfMonth = days - thisMoon + 1;

            internalSet(EXTENDED_YEAR, extended_year);

            // 0->0,60  1->1,1  60->1,60  61->2,1  etc.
            int[] yearOfCycle = new int[1];
            int cycle = floorDivide(cycle_year-1, 60, yearOfCycle);
            internalSet(ERA, cycle+1);
            internalSet(YEAR, yearOfCycle[0]+1);

            internalSet(DAY_OF_MONTH, dayOfMonth);

            // Days will be before the first new year we compute if this
            // date is in month 11, leap 11, 12.  There is never a leap 12.
            // New year computations are cached so this should be cheap in
            // the long run.
            int newYear = newYear(gyear);
            if (days < newYear) {
                newYear = newYear(gyear-1);
            }
            internalSet(DAY_OF_YEAR, days - newYear + 1);
        }
    }

    //------------------------------------------------------------------
    // Fields to time
    //------------------------------------------------------------------
    
    /**
     * Return the Chinese new year of the given Gregorian year.
     * @param gyear a Gregorian year
     * @return days after January 1, 1970 0:00 astronomical base zone of the
     * Chinese new year of the given year (this will be a new moon)
     */
    private int newYear(int gyear) {

        long cacheValue = newYearCache.get(gyear);

        if (cacheValue == CalendarCache.EMPTY) {

            int solsticeBefore= winterSolstice(gyear - 1);
            int solsticeAfter = winterSolstice(gyear);
            int newMoon1 = newMoonNear(solsticeBefore + 1, true);
            int newMoon2 = newMoonNear(newMoon1 + SYNODIC_GAP, true);
            int newMoon11 = newMoonNear(solsticeAfter + 1, false);
            
            if (synodicMonthsBetween(newMoon1, newMoon11) == 12 &&
                (hasNoMajorSolarTerm(newMoon1) || hasNoMajorSolarTerm(newMoon2))) {
                cacheValue = newMoonNear(newMoon2 + SYNODIC_GAP, true);
            } else {
                cacheValue = newMoon2;
            }

            newYearCache.put(gyear, cacheValue);
        }
        return (int) cacheValue;
    }

    /**
     * Return the Julian day number of day before the first day of the
     * given month in the given extended year.
     * 
     * <p>Note: This method reads the IS_LEAP_MONTH field to determine
     * whether the given month is a leap month.
     * @param eyear the extended year
     * @param month the zero-based month.  The month is also determined
     * by reading the IS_LEAP_MONTH field.
     * @return the Julian day number of the day before the first
     * day of the given month and year
     */
    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {

        // If the month is out of range, adjust it into range, and
        // modify the extended year value accordingly.
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            eyear += floorDivide(month, 12, rem);
            month = rem[0];
        }

        int gyear = eyear + epochYear - 1; // Gregorian year
        int newYear = newYear(gyear);
        int newMoon = newMoonNear(newYear + month * 29, true);
        
        int julianDay = newMoon + EPOCH_JULIAN_DAY;

        // Save fields for later restoration
        int saveMonth = internalGet(MONTH);
        int saveIsLeapMonth = internalGet(IS_LEAP_MONTH);

        // Ignore IS_LEAP_MONTH field if useMonth is false
        int isLeapMonth = useMonth ? saveIsLeapMonth : 0;

        computeGregorianFields(julianDay);
        
        // This will modify the MONTH and IS_LEAP_MONTH fields (only)
        computeChineseFields(newMoon, getGregorianYear(),
                             getGregorianMonth(), false);        

        if (month != internalGet(MONTH) ||
            isLeapMonth != internalGet(IS_LEAP_MONTH)) {
            newMoon = newMoonNear(newMoon + SYNODIC_GAP, true);
            julianDay = newMoon + EPOCH_JULIAN_DAY;
        }

        internalSet(MONTH, saveMonth);
        internalSet(IS_LEAP_MONTH, saveIsLeapMonth);

        return julianDay - 1;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return "chinese";
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public boolean haveDefaultCentury() {
        return false;
    }

    /**
     * Override readObject.
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException
    {
        epochYear = CHINESE_EPOCH_YEAR;
        zoneAstro = CHINA_ZONE;

        stream.defaultReadObject();

        /* set up the transient caches... */
        astro = new CalendarAstronomer();
        winterSolsticeCache = new CalendarCache();
        newYearCache = new CalendarCache();
    }
    
    /*
    private static CalendarFactory factory;
    public static CalendarFactory factory() {
        if (factory == null) {
            factory = new CalendarFactory() {
                public Calendar create(TimeZone tz, ULocale loc) {
                    return new ChineseCalendar(tz, loc);
                }

                public String factoryName() {
                    return "Chinese";
                }
            };
        }
        return factory;
    }
    */
}
