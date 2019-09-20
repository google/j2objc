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

/**
 * <b>Note:</b> The Holiday framework is a technology preview.
 * Despite its age, is still draft API, and clients should treat it as such.
 * 
 * Simple implementation of DateRule.
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public class SimpleDateRule implements DateRule
{
    /**
     * Construct a rule for a fixed date within a month
     *
     * @param month         The month in which this rule occurs (0-based).
     * @param dayOfMonth    The date in that month (1-based).
     * @hide draft / provisional / internal are hidden on Android
     */
    public SimpleDateRule(int month, int dayOfMonth)
    {
        this.month      = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek  = 0;
    }

    // temporary
    /* package */SimpleDateRule(int month, int dayOfMonth, Calendar cal)
    {
        this.month      = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek  = 0;
        this.calendar   = cal;
    }

    /**
     * Construct a rule for a weekday within a month, e.g. the first Monday.
     *
     * @param month         The month in which this rule occurs (0-based).
     * @param dayOfMonth    A date within that month (1-based).
     * @param dayOfWeek     The day of the week on which this rule occurs.
     * @param after         If true, this rule selects the first dayOfWeek
     *                      on or after dayOfMonth.  If false, the rule selects
     *                      the first dayOfWeek on or before dayOfMonth.
     * @hide draft / provisional / internal are hidden on Android
     */
    public SimpleDateRule(int month, int dayOfMonth, int dayOfWeek, boolean after)
    {
        this.month      = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek  = after ? dayOfWeek : -dayOfWeek;
    }

    /**
     * Return the first occurrance of the event represented by this rule
     * that is on or after the given start date.
     *
     * @param start Only occurrances on or after this date are returned.
     *
     * @return      The date on which this event occurs, or null if it
     *              does not occur on or after the start date.
     *
     * @see #firstBetween
     * @hide draft / provisional / internal are hidden on Android
     */
    public Date firstAfter(Date start)
    {
        return doFirstBetween(start, null);
    }

    /**
     * Return the first occurrance of the event represented by this rule
     * that is on or after the given start date and before the given
     * end date.
     *
     * @param start Only occurrances on or after this date are returned.
     * @param end   Only occurrances before this date are returned.
     *
     * @return      The date on which this event occurs, or null if it
     *              does not occur between the start and end dates.
     *
     * @see #firstAfter
     * @hide draft / provisional / internal are hidden on Android
     */
    public Date firstBetween(Date start, Date end)
    {
        // Pin to the min/max dates for this rule
        return doFirstBetween(start, end);
    }

    /**
     * Checks whether this event occurs on the given date.  This does
     * <em>not</em> take time of day into account; instead it checks
     * whether this event and the given date are on the same day.
     * This is useful for applications such as determining whether a given
     * day is a holiday.
     *
     * @param date  The date to check.
     * @return      true if this event occurs on the given date.
     * @hide draft / provisional / internal are hidden on Android
     */
    public boolean isOn(Date date)
    {
        Calendar c = calendar;

        synchronized(c) {
            c.setTime(date);

            int dayOfYear = c.get(Calendar.DAY_OF_YEAR);

            c.setTime(computeInYear(c.get(Calendar.YEAR), c));

//              System.out.println("  isOn: dayOfYear = " + dayOfYear);
//              System.out.println("        holiday   = " + c.get(Calendar.DAY_OF_YEAR));

            return c.get(Calendar.DAY_OF_YEAR) == dayOfYear;
        }
    }

    /**
     * Check whether this event occurs at least once between the two
     * dates given.
     * @hide draft / provisional / internal are hidden on Android
     */
    public boolean isBetween(Date start, Date end)
    {
        return firstBetween(start, end) != null; // TODO: optimize?
    }

    private Date doFirstBetween(Date start, Date end)
    {
        Calendar c = calendar;

        synchronized(c) {
            c.setTime(start);

            int year = c.get(Calendar.YEAR);
            int mon = c.get(Calendar.MONTH);

            // If the rule is earlier in the year than the start date
            // we have to go to the next year.
            if (mon > this.month) {
                year++;
            }

            // Figure out when the rule lands in the given year
            Date result = computeInYear(year, c);

            // If the rule is in the same month as the start date, it's possible
            // to get a result that's before the start.  If so, go to next year.
            if (mon == this.month && result.before(start)) {
                result = computeInYear(year+1, c);
            }

            if (end != null && result.after(end)) {
                return null;
            }
            return result;
        }
    }

    private Date computeInYear(int year, Calendar c)
    {
        synchronized(c) {
            c.clear();
            c.set(Calendar.ERA, c.getMaximum(Calendar.ERA));
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DATE, dayOfMonth);

            //System.out.println("     computeInYear: start at " + c.getTime().toString());

            if (dayOfWeek != 0) {
                c.setTime(c.getTime());        // JDK 1.1.2 workaround
                int weekday = c.get(Calendar.DAY_OF_WEEK);

                //System.out.println("                    weekday = " + weekday);
                //System.out.println("                    dayOfYear = " + c.get(Calendar.DAY_OF_YEAR));

                int delta = 0;
                if (dayOfWeek > 0) {
                    // We want the first occurrance of the given day of the week
                    // on or after the specified date in the month.
                    delta = (dayOfWeek - weekday + 7) % 7;
                }
                else {
                    // We want the first occurrance of the (-dayOfWeek)
                    // on or before the specified date in the month.
                    delta = -((dayOfWeek + weekday + 7) % 7);
                }
                //System.out.println("                    adding " + delta + " days");
                c.add(Calendar.DATE, delta);
            }

            return c.getTime();
        }
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
//    public void setCalendar(Calendar c) {
//        calendar = c;
//    }

    private Calendar calendar = new GregorianCalendar();

    private int     month;
    private int     dayOfMonth;
    private int     dayOfWeek;
}
