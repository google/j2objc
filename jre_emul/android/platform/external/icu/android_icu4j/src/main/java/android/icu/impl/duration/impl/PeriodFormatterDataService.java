/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ******************************************************************************
 * Copyright (C) 2007-2009, International Business Machines Corporation and   *
 * others. All Rights Reserved.                                               *
 ******************************************************************************
 */

package android.icu.impl.duration.impl;

import java.util.Collection;

/**
 * Abstract service for PeriodFormatterData, which defines the localization data
 * used by period formatters.
 * @hide Only a subset of ICU is exposed in Android
 */
public abstract class PeriodFormatterDataService {
    /**
     * Returns a PeriodFormatterData for the given locale name.
     * 
     * @param localeName the name of the locale
     * @return a PeriodFormatterData object
     */
    public abstract PeriodFormatterData get(String localeName);

    /**
     * Returns a collection of all the locale names supported by this service.
     * 
     * @return a collection of locale names, as String
     */
    public abstract Collection<String> getAvailableLocales();
}
