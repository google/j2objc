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

import android.icu.util.EasterHoliday;
import android.icu.util.Holiday;
import android.icu.util.SimpleHoliday;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class HolidayBundle_en_US extends ListResourceBundle
{
    static private final Holiday[] fHolidays = {
        SimpleHoliday.NEW_YEARS_DAY,
        new SimpleHoliday(Calendar.JANUARY,   15, Calendar.MONDAY,      "Martin Luther King Day",   1986),

        new SimpleHoliday(Calendar.FEBRUARY,  15, Calendar.MONDAY,      "Presidents' Day",          1976),
        new SimpleHoliday(Calendar.FEBRUARY,  22,                       "Washington's Birthday",    1776, 1975),

        EasterHoliday.GOOD_FRIDAY,
        EasterHoliday.EASTER_SUNDAY,

        new SimpleHoliday(Calendar.MAY,        8, Calendar.SUNDAY,      "Mother's Day",             1914),

        new SimpleHoliday(Calendar.MAY,       31, -Calendar.MONDAY,     "Memorial Day",             1971),
        new SimpleHoliday(Calendar.MAY,       30,                       "Memorial Day",             1868, 1970),

        new SimpleHoliday(Calendar.JUNE,      15, Calendar.SUNDAY,      "Father's Day",             1956),
        new SimpleHoliday(Calendar.JULY,       4,                       "Independence Day",         1776),
        new SimpleHoliday(Calendar.SEPTEMBER,  1, Calendar.MONDAY,      "Labor Day",                1894),
        new SimpleHoliday(Calendar.NOVEMBER,   2, Calendar.TUESDAY,     "Election Day"),
        new SimpleHoliday(Calendar.OCTOBER,    8, Calendar.MONDAY,      "Columbus Day",             1971),
        new SimpleHoliday(Calendar.OCTOBER ,  31,                       "Halloween"),
        new SimpleHoliday(Calendar.NOVEMBER,  11,                       "Veterans' Day",            1918),
        new SimpleHoliday(Calendar.NOVEMBER,  22, Calendar.THURSDAY,    "Thanksgiving",             1863),

        SimpleHoliday.CHRISTMAS,
    };
    static private final Object[][] fContents = {
        { "holidays",   fHolidays },
    };
    @Override
    public synchronized Object[][] getContents() { return fContents; }
}
