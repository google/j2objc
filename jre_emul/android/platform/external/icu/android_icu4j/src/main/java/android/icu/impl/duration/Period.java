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

import android.icu.impl.duration.impl.DataRecord.ETimeLimit;

/**
 * Represents an approximate duration in multiple TimeUnits.  Each unit,
 * if set, has a count (which can be fractional and must be non-negative).
 * In addition Period can either represent the duration as being into the past
 * or future, and as being more or less than the defined value.
 * <p>
 * Use a PeriodFormatter to convert a Period to a String.
 * <p>
 * Periods are immutable.  Mutating operations return the new
 * result leaving the original unchanged.
 * <p>
 * Example:<pre>
 * Period p1 = Period.at(3, WEEK).and(2, DAY).inFuture();
 * Period p2 = p1.and(12, HOUR);</pre>
 * @hide Only a subset of ICU is exposed in Android
 */
public final class Period {
  final byte timeLimit;
  final boolean inFuture;
  final int[] counts;

  /**
   * Constructs a Period representing a duration of
   * count units extending into the past.
   * @param count the number of units, must be non-negative
   * @param unit the unit
   * @return the new Period
   */
  public static Period at(float count, TimeUnit unit) {
    checkCount(count);
    return new Period(ETimeLimit.NOLIMIT, false, count, unit);
  }

  /**
   * Constructs a Period representing a duration more than
   * count units extending into the past.
   * @param count the number of units. must be non-negative
   * @param unit the unit
   * @return the new Period
   */
  public static Period moreThan(float count, TimeUnit unit) {
    checkCount(count);
    return new Period(ETimeLimit.MT, false, count, unit);
  }

  /**
   * Constructs a Period representing a duration
   * less than count units extending into the past.
   * @param count the number of units. must be non-negative
   * @param unit the unit
   * @return the new Period
   */
  public static Period lessThan(float count, TimeUnit unit) {
    checkCount(count);
    return new Period(ETimeLimit.LT, false, count, unit);
  }

  /**
   * Set the given unit to have the given count.  Marks the
   * unit as having been set.  This can be used to set
   * multiple units, or to reset a unit to have a new count.
   * This does <b>not</b> add the count to an existing count
   * for this unit.
   *
   * @param count the number of units.  must be non-negative
   * @param unit the unit
   * @return the new Period
   */
  public Period and(float count, TimeUnit unit) {
    checkCount(count);
    return setTimeUnitValue(unit, count);
  }

  /**
   * Mark the given unit as not being set.
   *
   * @param unit the unit to unset
   * @return the new Period
   */
  public Period omit(TimeUnit unit) {
    return setTimeUnitInternalValue(unit, 0);
  }

  /**
   * Mark the duration as being at the defined duration.
   *
   * @return the new Period
   */
  public Period at() {
    return setTimeLimit(ETimeLimit.NOLIMIT);
  }

  /**
   * Mark the duration as being more than the defined duration.
   *
   * @return the new Period
   */
  public Period moreThan() {
    return setTimeLimit(ETimeLimit.MT);
  }

  /**
   * Mark the duration as being less than the defined duration.
   *
   * @return the new Period
   */
  public Period lessThan() {
    return setTimeLimit(ETimeLimit.LT);
  }

  /**
   * Mark the time as being in the future.
   *
   * @return the new Period
   */
  public Period inFuture() {
    return setFuture(true);
  }

  /**
   * Mark the duration as extending into the past.
   *
   * @return the new Period
   */
  public Period inPast() {
    return setFuture(false);
  }

  /**
   * Mark the duration as extending into the future if
   * future is true, and into the past otherwise.
   *
   * @param future true if the time is in the future
   * @return the new Period
   */
  public Period inFuture(boolean future) {
    return setFuture(future);
  }

  /**
   * Mark the duration as extending into the past if
   * past is true, and into the future otherwise.
   *
   * @param past true if the time is in the past
   * @return the new Period
   */
  public Period inPast(boolean past) {
    return setFuture(!past);
  }

  /**
   * Returns true if any unit is set.
   * @return true if any unit is set
   */
  public boolean isSet() {
    for (int i = 0; i < counts.length; ++i) {
      if (counts[i] != 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if the given unit is set.
   * @param unit the unit to test
   * @return true if the given unit is set.
   */
  public boolean isSet(TimeUnit unit) {
    return counts[unit.ordinal] > 0;
  }

  /**
   * Returns the count for the specified unit.  If the
   * unit is not set, returns 0.
   * @param unit the unit to test
   * @return the count
   */
  public float getCount(TimeUnit unit) {
    int ord = unit.ordinal;
    if (counts[ord] == 0) {
      return 0;
    }
    return (counts[ord] - 1)/1000f;
  }

  /**
   * Returns true if this represents a
   * duration into the future.
   * @return true if this represents a
   * duration into the future.
   */
  public boolean isInFuture() {
    return inFuture;
  }

  /**
   * Returns true if this represents a
   * duration into the past
   * @return true if this represents a
   * duration into the past
   */
  public boolean isInPast  () {
    return !inFuture;
  }

  /**
   * Returns true if this represents a duration in
   * excess of the defined duration.
   * @return true if this represents a duration in
   * excess of the defined duration.
   */
  public boolean isMoreThan() {
    return timeLimit == ETimeLimit.MT;
  }

  /**
   * Returns true if this represents a duration
   * less than the defined duration.
   * @return true if this represents a duration
   * less than the defined duration.
   */
  public boolean isLessThan() {
    return timeLimit == ETimeLimit.LT;
  }

  /**
   * Returns true if rhs extends Period and
   * the two Periods are equal.
   * @param rhs the object to compare to
   * @return true if rhs is a Period and is equal to this
   */
  @Override
  public boolean equals(Object rhs) {
    try {
      return equals((Period)rhs);
    }
    catch (ClassCastException e) {
      return false;
    }
  }

  /**
   * Returns true if the same units are defined with
   * the same counts, both extend into the future or both into the
   * past, and if the limits (at, more than, less than) are the same.
   * Note that this means that a period of 1000ms and a period of 1sec
   * will not compare equal.
   *
   * @param rhs the period to compare to
   * @return true if the two periods are equal
   */
  public boolean equals(Period rhs) {
    if (rhs != null &&
        this.timeLimit == rhs.timeLimit &&
        this.inFuture == rhs.inFuture) {
      for (int i = 0; i < counts.length; ++i) {
        if (counts[i] != rhs.counts[i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Returns the hashCode.
   * @return the hashCode
   */
  @Override
public int hashCode() {
    int hc = (timeLimit << 1) | (inFuture ? 1 : 0);
    for (int i = 0; i < counts.length; ++i) {
      hc = (hc << 2) ^ counts[i];
    }
    return hc;
  }

  /**
   * Private constructor used by static factory methods.
   */
  private Period(int limit, boolean future, float count, TimeUnit unit) {
    this.timeLimit = (byte) limit;
    this.inFuture = future;
    this.counts = new int[TimeUnit.units.length];
    this.counts[unit.ordinal] = (int)(count * 1000) + 1;
  }

  /**
   * Package private constructor used by setters and factory.
   */
  Period(int timeLimit, boolean inFuture, int[] counts) {
    this.timeLimit = (byte) timeLimit;
    this.inFuture = inFuture;
    this.counts = counts;
  }

  /**
   * Set the unit's internal value, converting from float to int.
   */
  private Period setTimeUnitValue(TimeUnit unit, float value) {
    if (value < 0) {
      throw new IllegalArgumentException("value: " + value);
    }
    return setTimeUnitInternalValue(unit, (int)(value * 1000) + 1);
  }

  /**
   * Sets the period to have the provided value, 1/1000 of the
   * unit plus 1.  Thus unset values are '0', 1' is the set value '0',
   * 2 is the set value '1/1000', 3 is the set value '2/1000' etc.
   * @param p the period to change
   * @param value the int value as described above.
   * @eturn the new Period object.
   */
  private Period setTimeUnitInternalValue(TimeUnit unit, int value) {
    int ord = unit.ordinal;
    if (counts[ord] != value) {
      int[] newCounts = new int[counts.length];
      for (int i = 0; i < counts.length; ++i) {
        newCounts[i] = counts[i];
      }
      newCounts[ord] = value;
      return new Period(timeLimit, inFuture, newCounts);
    }
    return this;
  }

  /**
   * Sets whether this defines a future time.
   * @param future true if the time is in the future
   * @return  the new Period
   */
  private Period setFuture(boolean future) {
    if (this.inFuture != future) {
      return new Period(timeLimit, future, counts);
    }
    return this;
  }

  /**
   * Sets whether this is more than, less than, or
   * 'about' the specified time.
   * @param limit the kind of limit
   * @return the new Period
   */
  private Period setTimeLimit(byte limit) {
    if (this.timeLimit != limit) {
      return new Period(limit, inFuture, counts);

    }
    return this;
  }

  /**
   * Validate count.
   */
  private static void checkCount(float count) {
    if (count < 0) {
      throw new IllegalArgumentException("count (" + count +
                                         ") cannot be negative");
    }
  }
}
