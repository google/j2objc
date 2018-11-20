/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2009, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl.duration;

import java.util.TimeZone;

/**
 * Factory used to construct DurationFormatters.
 * Formatters are immutable once created.
 * <p>
 * Setters on the factory mutate the factory and return it,
 * for chaining.
 * @hide Only a subset of ICU is exposed in Android
 */
public interface DurationFormatterFactory {

  /**
   * Set the period formatter used by the factory.  New formatters created
   * with this factory will use the given period formatter.
   *
   * @param formatter the formatter to use
   * @return this DurationFormatterFactory
   */
  public DurationFormatterFactory setPeriodFormatter(PeriodFormatter formatter);

  /**
   * Set the builder used by the factory.  New formatters created
   * with this factory will use the given locale.
   *
   * @param builder the builder to use
   * @return this DurationFormatterFactory
   */
  public DurationFormatterFactory setPeriodBuilder(PeriodBuilder builder);

  /**
   * Set a fallback formatter for durations over a given limit.
   *
   * @param fallback the fallback formatter to use, or null
   * @return this DurationFormatterFactory
   */
  public DurationFormatterFactory setFallback(DateFormatter fallback);

  /**
   * Set a fallback limit for durations over a given limit.
   *
   * @param fallbackLimit the fallback limit to use, or 0 if none is desired.
   * @return this DurationFormatterFactory
   */
  public DurationFormatterFactory setFallbackLimit(long fallbackLimit);

  /**
   * Set the name of the locale that will be used when 
   * creating new formatters.
   *
   * @param localeName the name of the Locale
   * @return this DurationFormatterFactory
   */
  public DurationFormatterFactory setLocale(String localeName);

  /**
   * Set the name of the locale that will be used when 
   * creating new formatters.
   *
   * @param timeZone The time zone to set.
   * @return this DurationFormatterFactory
   */
  public DurationFormatterFactory setTimeZone(TimeZone timeZone);

  /**
   * Return a formatter based on this factory's current settings.
   *
   * @return a DurationFormatter
   */
  public DurationFormatter getFormatter();
}
