/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ******************************************************************************
 * Copyright (C) 2007-2009, International Business Machines Corporation and   *
 * others. All Rights Reserved.                                               *
 ******************************************************************************
 */

package android.icu.impl.duration;

import java.util.Collection;

/**
 * Provider of Factory instances for building PeriodBuilders, PeriodFormatters,
 * and DurationFormatters.
 * @hide Only a subset of ICU is exposed in Android
 */
public interface PeriodFormatterService {

    /**
     * Creates a new factory for creating DurationFormatters.
     * 
     * @return a new DurationFormatterFactory.
     */
    DurationFormatterFactory newDurationFormatterFactory();

    /**
     * Creates a new factory for creating PeriodFormatters.
     * 
     * @return a new PeriodFormatterFactory
     */
    PeriodFormatterFactory newPeriodFormatterFactory();

    /**
     * Creates a new factory for creating PeriodBuilders.
     * 
     * @return a new PeriodBuilderFactory
     */
    PeriodBuilderFactory newPeriodBuilderFactory();

    /**
     * Return the names of locales supported by factories produced by this
     * service.
     * 
     * @return a collection of String (locale names)
     */
    Collection<String> getAvailableLocaleNames();
}
