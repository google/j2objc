/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.calendar;
import java.util.Date;

import android.icu.util.Calendar;

public class ChineseTestCase extends TestCase {

    /**
     * Initialize an object using a Julian day number and
     * the corresponding fields for the calendar being tested.
     *
     * @param era the ERA field of tested calendar on the given Julian
     * day
     * @param year the YEAR field of tested calendar on the given
     * Julian day
     * @param month the MONTH (1-based) field of tested calendar on
     * the given Julian day
     * @param isLeapMonth if true, treat month as a leap month
     * @param dayOfMonth the DAY_OF_MONTH field of tested calendar on the
     * given Julian day
     * @param dayOfWeek the DAY_OF_WEEK field of tested calendar on given
     * Julian day
     */
    public ChineseTestCase(double julian,
                           int era, int year, int month,
                           boolean isLeapMonth, int dayOfMonth, int dayOfWeek) {

        setTime(new Date(JULIAN_EPOCH + (long)(ONE_DAY * julian)));
        
        set(Calendar.ERA, era);
        set(Calendar.YEAR, year);
        set(Calendar.MONTH, month - 1);
        set(Calendar.IS_LEAP_MONTH, isLeapMonth?1:0);
        set(Calendar.DAY_OF_MONTH, dayOfMonth);
        set(Calendar.DAY_OF_WEEK, dayOfWeek);
    }

    /**
     * Return a String representation of this test case's time.
     */
    public String toString() {
        return dowToString(get(Calendar.DAY_OF_WEEK)) +
            get(Calendar.YEAR) + "of" + get(Calendar.ERA) +
            "/" + (get(Calendar.MONTH)+1) +
            (get(Calendar.IS_LEAP_MONTH)==1?"(leap)":"") + "/" +
            get(Calendar.DAY_OF_MONTH);
    }
}
