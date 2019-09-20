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
public class HolidayBundle_es_MX extends ListResourceBundle {
    static private final Holiday[] fHolidays = {
        SimpleHoliday.NEW_YEARS_DAY,
        new SimpleHoliday(Calendar.FEBRUARY,   5,  0,    "Constitution Day"),
        new SimpleHoliday(Calendar.MARCH,     21,  0,    "Benito Ju\u00E1rez Day"),
        SimpleHoliday.MAY_DAY,
        new SimpleHoliday(Calendar.MAY,        5,  0,    "Cinco de Mayo"),
        new SimpleHoliday(Calendar.JUNE,       1,  0,    "Navy Day"),
        new SimpleHoliday(Calendar.SEPTEMBER, 16,  0,    "Independence Day"),
        new SimpleHoliday(Calendar.OCTOBER,   12,  0,    "D\u00EDa de la Raza"),
        SimpleHoliday.ALL_SAINTS_DAY,
        new SimpleHoliday(Calendar.NOVEMBER,   2,  0,    "Day of the Dead"),
        new SimpleHoliday(Calendar.NOVEMBER,  20,  0,    "Revolution Day"),
        new SimpleHoliday(Calendar.DECEMBER,  12,  0,    "Flag Day"),
        SimpleHoliday.CHRISTMAS,
    };
    static private final Object[][] fContents = {
        { "holidays",   fHolidays },
    };
    @Override
    public synchronized Object[][] getContents() { return fContents; }
}
