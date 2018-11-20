/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2005, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.impl.data;

import java.util.ListResourceBundle;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class HolidayBundle_en extends ListResourceBundle {

    // Normally, each HolidayBundle uses the holiday's US English name
    // as the string key for looking up the localized name.  This means
    // that the key itself can be used if no name is found for the requested
    // locale.
    //
    // For holidays where the key is _not_ the English name, e.g. in the
    // case of conflicts, the English name must be given here.
    //
    static private final Object[][] fContents = {
        {   "", ""  },      // Can't be empty!
    };

    @Override
    public synchronized Object[][] getContents() { return fContents; }

}
