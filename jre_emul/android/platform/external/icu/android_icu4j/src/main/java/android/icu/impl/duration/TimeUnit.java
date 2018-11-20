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

/**
 * 'Enum' for individual time units.  Not an actual enum so that it can be
 * used by Java 1.4.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class TimeUnit {
  /** The name of this unit, a key, not for localization. */
  final String name;

  /** The ordinal of the unit, in order from largest to smallest. */
  final byte ordinal;

  /** Private constructor */
  private TimeUnit(String name, int ordinal) {
    this.name = name;
    this.ordinal = (byte) ordinal;
  }

  @Override
  public String toString() {
    return name;
  }

  /** Represents a year. */
  public static final TimeUnit YEAR = new TimeUnit("year", 0);

  /** Represents a month. */
  public static final TimeUnit MONTH = new TimeUnit("month", 1);

  /** Represents a week. */
  public static final TimeUnit WEEK = new TimeUnit("week", 2);

  /** Represents a day. */
  public static final TimeUnit DAY = new TimeUnit("day", 3);

  /** Represents an hour. */
  public static final TimeUnit HOUR = new TimeUnit("hour", 4);

  /** Represents a minute. */
  public static final TimeUnit MINUTE = new TimeUnit("minute", 5);

  /** Represents a second. */
  public static final TimeUnit SECOND = new TimeUnit("second", 6);

  /** Represents a millisecond. */
  public static final TimeUnit MILLISECOND = new TimeUnit("millisecond", 7);

  /** Returns the next larger time unit, or null if this is the largest. */
  public TimeUnit larger() {
    return ordinal == 0 ? null : units[ordinal - 1];
  }

  /** Returns the next smaller time unit, or null if this is the smallest. */
  public TimeUnit smaller() {
    return ordinal == units.length - 1 ? null : units[ordinal + 1];
  }

  /** The list of units, in order from largest to smallest. */
  static final TimeUnit[] units = {
    YEAR, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND, MILLISECOND
  };

    /** Returns the ordinal value of this time unit, largest is 0. **/
  public int ordinal() {
    return ordinal;
  }

  /** Approximate, durations for the units independent of the time at which
      they are measured */

  // hack, initialization long array using expressions with 'L' at end doesn't
  // compute entire expression using 'long'.  differs from initializtion of
  // a single constant
  static final long[] approxDurations = {
    36525L*24*60*60*10, 3045*24*60*60*10L, 7*24*60*60*1000L, 24*60*60*1000L,
    60*60*1000L, 60*1000L, 1000L, 1L
  };
}
