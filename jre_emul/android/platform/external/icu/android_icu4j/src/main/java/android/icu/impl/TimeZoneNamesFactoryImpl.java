/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.impl;

import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.Factory;
import android.icu.util.ULocale;

/**
 * The implementation class of <code>TimeZoneNames.Factory</code>
 * @hide Only a subset of ICU is exposed in Android
 */
public class TimeZoneNamesFactoryImpl extends Factory {

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames.Factory#getTimeZoneNames(android.icu.util.ULocale)
     */
    @Override
    public TimeZoneNames getTimeZoneNames(ULocale locale) {
        return new TimeZoneNamesImpl(locale);
    }

}
