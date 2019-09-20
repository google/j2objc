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

import java.util.Locale;
import java.util.TimeZone;

/**
 * Abstract factory object used to create DurationFormatters.
 * DurationFormatters are immutable once created.
 * <p>
 * Setters on the factory mutate the factory and return it,
 * for chaining.
 * <p>
 * Subclasses override getFormatter to return a custom
 * DurationFormatter.
 */
class BasicDurationFormatterFactory implements DurationFormatterFactory {
  private BasicPeriodFormatterService ps;
  private PeriodFormatter formatter;
  private PeriodBuilder builder;
  private DateFormatter fallback;
  private long fallbackLimit;
  private String localeName;
  private TimeZone timeZone;
  private BasicDurationFormatter f; // cache

  /**
   * Create a default formatter for the current locale and time zone.
   */
  BasicDurationFormatterFactory(BasicPeriodFormatterService ps) {
    this.ps = ps;
    this.localeName = Locale.getDefault().toString();
    this.timeZone = TimeZone.getDefault();
  }

  /**
   * Set the period formatter used by the factory.  New formatters created
   * with this factory will use the given period formatter.
   *
   * @return this BasicDurationFormatterFactory
   */
  @Override
  public DurationFormatterFactory setPeriodFormatter(
      PeriodFormatter formatter) {
    if (formatter != this.formatter) {
      this.formatter = formatter;
      reset();
    }
    return this;
  }

  /**
   * Set the builder used by the factory.  New formatters created
   * with this factory will use the given locale.
   *
   * @param builder the builder to use
   * @return this BasicDurationFormatterFactory
   */
  @Override
  public DurationFormatterFactory setPeriodBuilder(PeriodBuilder builder) {
    if (builder != this.builder) {
      this.builder = builder;
      reset();
    }
    return this;
  }

  /**
   * Set a fallback formatter for durations over a given limit.
   *
   * @param fallback the fallback formatter to use, or null
   * @return this BasicDurationFormatterFactory
   */
  @Override
  public DurationFormatterFactory setFallback(DateFormatter fallback) {
    boolean doReset = fallback == null
        ? this.fallback != null
        : !fallback.equals(this.fallback);
    if (doReset) {
      this.fallback = fallback;
      reset();
    }
    return this;
  }

  /**
   * Set a fallback limit for durations over a given limit.
   *
   * @param fallbackLimit the fallback limit to use, or 0 if none is desired.
   * @return this BasicDurationFormatterFactory
   */
  @Override
  public DurationFormatterFactory setFallbackLimit(long fallbackLimit) {
    if (fallbackLimit < 0) {
      fallbackLimit = 0;
    }
    if (fallbackLimit != this.fallbackLimit) {
      this.fallbackLimit = fallbackLimit;
      reset();
    }
    return this;
  }

  /**
   * Set the name of the locale that will be used when
   * creating new formatters.
   *
   * @param localeName the name of the Locale
   * @return this BasicDurationFormatterFactory
   */
  @Override
  public DurationFormatterFactory setLocale(String localeName) {
    if (!localeName.equals(this.localeName)) {
      this.localeName = localeName;
      if (builder != null) {
          builder = builder.withLocale(localeName);
      }
      if (formatter != null) {
          formatter = formatter.withLocale(localeName);
      }
      reset();
    }
    return this;
  }

  /**
   * Set the name of the locale that will be used when
   * creating new formatters.
   *
   * @param timeZone The time zone to use.
   * @return this BasicDurationFormatterFactory
   */
  @Override
  public DurationFormatterFactory setTimeZone(TimeZone timeZone) {
    if (!timeZone.equals(this.timeZone)) {
      this.timeZone = timeZone;
      if (builder != null) {
          builder = builder.withTimeZone(timeZone);
      }
      reset();
    }
    return this;
  }

  /**
   * Return a formatter based on this factory's current settings.
   *
   * @return a BasicDurationFormatter
   */
  @Override
  public DurationFormatter getFormatter() {
    if (f == null) {
      if (fallback != null) {
        fallback = fallback.withLocale(localeName).withTimeZone(timeZone);
      }
      formatter = getPeriodFormatter();
      builder = getPeriodBuilder();

      f = createFormatter();
    }
    return f;
  }

  /**
   * Return the current period formatter.
   *
   * @return the current period formatter
   */
  public PeriodFormatter getPeriodFormatter() {
    if (formatter == null) {
      formatter = ps.newPeriodFormatterFactory()
          .setLocale(localeName)
          .getFormatter();
    }
    return formatter;
  }

  /**
   * Return the current builder.
   *
   * @return the current builder
   */
  public PeriodBuilder getPeriodBuilder() {
    if (builder == null) {
      builder = ps.newPeriodBuilderFactory()
          .setLocale(localeName)
          .setTimeZone(timeZone)
          .getSingleUnitBuilder();
    }
    return builder;
  }

  /**
   * Return the current fallback formatter.
   *
   * @return the fallback formatter, or null if there is no fallback
   * formatter
   */
  public DateFormatter getFallback() {
    return fallback;
  }

  /**
   * Return the current fallback formatter limit
   *
   * @return the limit, or 0 if there is no fallback.
   */
  public long getFallbackLimit() {
    return fallback == null ? 0 : fallbackLimit;
  }

  /**
   * Return the current locale name.
   *
   * @return the current locale name
   */
  public String getLocaleName() {
    return localeName;
  }

  /**
   * Return the current locale name.
   *
   * @return the current locale name
   */
  public TimeZone getTimeZone() {
    return timeZone;
  }

  /**
   * Create the formatter.  All local fields are already initialized.
   */
  protected BasicDurationFormatter createFormatter() {
    return new BasicDurationFormatter(formatter, builder, fallback,
                                      fallbackLimit, localeName,
                                      timeZone);
  }

  /**
   * Clear the cached formatter.  Subclasses must call this if their
   * state has changed. This is automatically invoked by setBuilder,
   * setFormatter, setFallback, setLocaleName, and setTimeZone
   */
  protected void reset() {
    f = null;
  }
}
