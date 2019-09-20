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
 * DateRule is an interface for calculating the date of an event.
 * It supports both recurring events and those which occur only once.
 * DateRule is useful for storing information about holidays,
 * Daylight Savings Time rules, and other events such as meetings.
 *
 * @see SimpleDateRule
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public interface DateRule
{
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
    abstract public Date    firstAfter(Date start);

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
    abstract public Date    firstBetween(Date start, Date end);

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
    abstract public boolean isOn(Date date);

    /**
     * Check whether this event occurs at least once between the two
     * dates given.
     * @hide draft / provisional / internal are hidden on Android
     */
    abstract public boolean isBetween(Date start, Date end);
}
