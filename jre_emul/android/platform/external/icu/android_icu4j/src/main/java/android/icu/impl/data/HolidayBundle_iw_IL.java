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

import java.util.ListResourceBundle;

import android.icu.util.HebrewHoliday;
import android.icu.util.Holiday;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class HolidayBundle_iw_IL extends ListResourceBundle {
    static private final Holiday[] fHolidays = {
        HebrewHoliday.ROSH_HASHANAH,
        HebrewHoliday.YOM_KIPPUR,
        HebrewHoliday.HANUKKAH,
        HebrewHoliday.PURIM,
        HebrewHoliday.PASSOVER,
        HebrewHoliday.SHAVUOT,
        HebrewHoliday.SELIHOT,
    };

    static private final Object[][] fContents = {
        { "holidays",   fHolidays },
    };
    @Override
    public synchronized Object[][] getContents() { return fContents; }
}
