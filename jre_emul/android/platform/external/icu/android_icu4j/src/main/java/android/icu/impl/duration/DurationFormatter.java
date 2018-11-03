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

import java.util.Date;
import java.util.TimeZone;

/**
 * Formatter for durations in milliseconds.
 * @hide Only a subset of ICU is exposed in Android
 */
public interface DurationFormatter {

  /**
   * Formats the duration between now and a target date.
   * <p>
   * This is a convenience method that calls
   * formatDurationFrom(long, long) using now
   * as the reference date, and the difference between now and
   * <code>targetDate.getTime()</code> as the duration.
   * 
   * @param targetDate the ending date
   * @return the formatted time
   */
  String formatDurationFromNowTo(Date targetDate);

  /**
   * Formats a duration expressed in milliseconds.
   * <p>
   * This is a convenience method that calls formatDurationFrom
   * using the current system time as the reference date.
   * 
   * @param duration the duration in milliseconds
   * @param tz the time zone
   * @return the formatted time
   */
  String formatDurationFromNow(long duration);

  /**
   * Formats a duration expressed in milliseconds from a reference date.
   * <p>
   * The reference date allows formatters to use actual durations of
   * variable-length periods (like months) if they wish.
   * <p>
   * The duration is expressed as the number of milliseconds in the
   * past (negative values) or future (positive values) with respect
   * to a reference date (expressed as milliseconds in epoch).
   * 
   * @param duration the duration in milliseconds
   * @param referenceDate the date from which to compute the duration
   * @return the formatted time
   */
  String formatDurationFrom(long duration, long referenceDate);

  /**
   * Returns a new DurationFormatter that's the same as this one 
   * but formats for a new locale.
   *
   * @param localeName the name of the new locale
   * @return a new formatter for the given locale
   */
  DurationFormatter withLocale(String localeName);

  /**
   * Returns a new DurationFormatter that's the same as this one but
   * uses a different time zone.
   *
   * @param tz the time zone in which to compute durations.
   * @return a new formatter for the given locale
   */
  DurationFormatter withTimeZone(TimeZone tz);
}
