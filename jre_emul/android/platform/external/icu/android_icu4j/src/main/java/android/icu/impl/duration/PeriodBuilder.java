/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl.duration;

import java.util.TimeZone;

/**
 * Constructs a Period given a base time and a duration in milliseconds.
 * <p>
 * PeriodBuilder may be used alone or be set on a DurationFormatter
 * to customize how that formatter constructs a Period for formatting.
 * <p>
 * None of the operations on PeriodBuilder change the current builder.
 * @hide Only a subset of ICU is exposed in Android
 */
public interface PeriodBuilder {
  /**
   * Create a period of the given duration using the current system
   * time as the reference time.
   *
   * @param duration the duration in milliseconds from the current time
   * to the target time.  A negative duration indicates a time in the past
   * @return a Period that represents the duration
   */
  Period create(long duration);

  /**
   * Create a period of the given duration using the provided reference date.
   *
   * @param duration the duration in milliseconds from the referenct time
   * to the target time.  A negative duration indicates a time before the
   * reference time
   * @param referenceDate the reference date from which to compute the period
   * @return a Period that represents the duration
   */
  Period createWithReferenceDate(long duration, long referenceDate);

  /**
   * Returns a new PeriodBuilder that uses the provided locale to 
   * determine what periods are available for use.
   */
  PeriodBuilder withLocale(String localeName);

  /**
   * Returns a new PeriodBuilder that computes periods starting at
   * dates in the provided time zone.
   */
  PeriodBuilder withTimeZone(TimeZone tz);
}
