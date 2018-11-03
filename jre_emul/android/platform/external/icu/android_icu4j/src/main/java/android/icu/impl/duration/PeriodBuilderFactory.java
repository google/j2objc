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

import java.util.TimeZone;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public interface PeriodBuilderFactory {

  /**
   * Sets the time units available for use.  Default is all units.
   * @param minUnit the smallest time unit available for use
   * @param maxUnit the largest time unit available for use
   * @return this factory
   */
  PeriodBuilderFactory setAvailableUnitRange(TimeUnit minUnit,
                         TimeUnit maxUnit);

  /**
   * Sets whether the time unit is available for use.
   * @param unit the time unit
   * @param available true if the unit is available for use
   * @return this factory
   */
  PeriodBuilderFactory setUnitIsAvailable(TimeUnit unit, boolean available);

  /**
   * Sets the maximum value for the largest available time unit (as
   * set in setUnits).  Periods that represent a longer duration than
   * this will be pinned to this value of that time unit and return
   * true for 'isMoreThan'.  Default is no limit.  Setting a value of
   * zero restores the default.
   */
  PeriodBuilderFactory setMaxLimit(float maxLimit);

  /**
   * Sets the minimum value for the smallest available time unit (as
   * set in setUnits).  Periods that represent a shorter duration than
   * this will be pinned to this value of that time unit and return
   * true for 'isLessThan'.  Default is no limit.  Setting a value of
   * zero restores the default.
   */
  PeriodBuilderFactory setMinLimit(float minLimit);

  /**
   * Sets whether units with a value of zero are represented in a
   * period when 'gaps' appear between time units, e.g. 
   * '2 hours, 0 minutes, and 33 seconds'.  Default is to
   * not represent these explicitly ('2 hours and 33 seconds').
   */
  PeriodBuilderFactory setAllowZero(boolean allow);

  /**
   * Sets whether weeks are used with other units, or only when
   * weeks are the only unit.  For example '3 weeks and 2 days'
   * versus '23 days'.  Default is to use them alone only.
   */
  PeriodBuilderFactory setWeeksAloneOnly(boolean aloneOnly);

  /**
   * Sets whether milliseconds are allowed.  This is only examined
   * when milliseconds are an available field. The default is to allow 
   * milliseconds to display normally.
   * <p>
   * This is intended to be used to set locale-specific behavior.  Typically clients will
   * not call this API and instead call {@link #setLocale}.
   *
   * @param allow whether milliseconds should be allowed.
   * @return a builder
   */
   PeriodBuilderFactory setAllowMilliseconds(boolean allow);
   
  /**
   * Sets the locale for the factory.  Setting the locale can adjust
   * the values for some or all of the other properties to reflect
   * language or cultural conventions.  Default is to use
   * the default locale.
   */
  PeriodBuilderFactory setLocale(String localeName);

  /**
   * Sets the time zone for the factory.  This can affect the timezone
   * used for date computations.
   * @param timeZone the timeZone
   * @return a builder
   */
  PeriodBuilderFactory setTimeZone(TimeZone timeZone);
 /**
   * Returns a builder that represents durations in terms of the single
   * given TimeUnit.  If the factory settings don't make the given unit
   * available, this will return null.
   *
   * @param unit the single TimeUnit with which to represent times
   * @return a builder
   */
  PeriodBuilder getFixedUnitBuilder(TimeUnit unit);

  /**
   * Returns a builder that represents durations in terms of the
   * single largest period less than or equal to the duration.
   *
   * @return a builder
   */
  PeriodBuilder getSingleUnitBuilder();

  /**
   * Returns a builder that formats the largest one or two time units,
   * starting with the largest period less than or equal to the duration.
   * It formats two periods if the first period has a count &lt; 2
   * and the next period has a count &gt;= 1.
   *
   * @return a builder
   */
  PeriodBuilder getOneOrTwoUnitBuilder();

  /**
   * Returns a builder that formats up to the given number of time units,
   * starting with the largest unit less than or equal to the
   * duration.
   *
   * @return a builder
   */
  PeriodBuilder getMultiUnitBuilder(int unitCount);
}

