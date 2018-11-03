/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.impl.data;

import java.util.Calendar;
import java.util.ListResourceBundle;

import android.icu.util.Holiday;
import android.icu.util.SimpleHoliday;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class HolidayBundle_en_CA extends ListResourceBundle {
    static private final Holiday[] fHolidays = {
        SimpleHoliday.NEW_YEARS_DAY,
        new SimpleHoliday(Calendar.MAY,       19, 0,                  "Victoria Day"),
        new SimpleHoliday(Calendar.JULY,       1, 0,                  "Canada Day"),
        new SimpleHoliday(Calendar.AUGUST,     1, Calendar.MONDAY,    "Civic Holiday"),
        new SimpleHoliday(Calendar.SEPTEMBER,  1, Calendar.MONDAY,    "Labor Day"),
        new SimpleHoliday(Calendar.OCTOBER,    8, Calendar.MONDAY,    "Thanksgiving"),
        new SimpleHoliday(Calendar.NOVEMBER,  11, 0,                  "Remembrance Day"),
        SimpleHoliday.CHRISTMAS,
        SimpleHoliday.BOXING_DAY,
        SimpleHoliday.NEW_YEARS_EVE,

        // Easter and related holidays
        //hey {jf} - where are these from?
//        EasterHoliday.GOOD_FRIDAY,
//        EasterHoliday.EASTER_SUNDAY,
//        EasterHoliday.EASTER_MONDAY,
    };

    static private final Object[][] fContents = {
        { "holidays",   fHolidays },

        { "Labor Day",  "Labour Day" },
    };
    @Override
    public synchronized Object[][] getContents() { return fContents; }
}
