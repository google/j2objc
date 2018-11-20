/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007-2011, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl.duration;

import java.util.TimeZone;

import android.icu.impl.duration.impl.DataRecord;
import android.icu.impl.duration.impl.PeriodFormatterData;
import android.icu.impl.duration.impl.PeriodFormatterDataService;

/**
 * Default implementation of PeriodBuilderFactory.  This creates builders that
 * use approximate durations.
 */
class BasicPeriodBuilderFactory implements PeriodBuilderFactory {
  private PeriodFormatterDataService ds;
  private Settings settings;

  private static final short allBits = 0xff;

  BasicPeriodBuilderFactory(PeriodFormatterDataService ds) {
    this.ds = ds;
    this.settings = new Settings();
  }

  static long approximateDurationOf(TimeUnit unit) {
    return TimeUnit.approxDurations[unit.ordinal];
  }

  class Settings {
    boolean inUse;
    short uset = allBits;
    TimeUnit maxUnit = TimeUnit.YEAR;
    TimeUnit minUnit = TimeUnit.MILLISECOND;
    int maxLimit;
    int minLimit;
    boolean allowZero = true;
    boolean weeksAloneOnly;
    boolean allowMillis = true;

    Settings setUnits(int uset) {
      if (this.uset == uset) {
        return this;
      }
      Settings result = inUse ? copy() : this;

      result.uset = (short)uset;

      if ((uset & allBits) == allBits) {
        result.uset = allBits;
        result.maxUnit = TimeUnit.YEAR;
        result.minUnit = TimeUnit.MILLISECOND;
      } else {
        int lastUnit = -1;
        for (int i = 0; i < TimeUnit.units.length; ++i) {
          if (0 != (uset & (1 << i))) {
            if (lastUnit == -1) {
              result.maxUnit = TimeUnit.units[i];
            }
            lastUnit = i;
          }
        }
        if (lastUnit == -1) {
            // currently empty, but this might be transient so no fail
            result.minUnit = result.maxUnit = null;
        } else {
            result.minUnit = TimeUnit.units[lastUnit];
        }
      }

      return result;
    }

    short effectiveSet() {
      if (allowMillis) {
        return uset;
      }
      return (short)(uset & ~(1 << TimeUnit.MILLISECOND.ordinal));
    }

    TimeUnit effectiveMinUnit() {
        if (allowMillis || minUnit != TimeUnit.MILLISECOND) {
            return minUnit;
        }
        // -1 to skip millisecond
        for (int i = TimeUnit.units.length - 1; --i >= 0;) {
            if (0 != (uset & (1 << i))) {
                return TimeUnit.units[i];
            }
        }
        return TimeUnit.SECOND; // default for pathological case
    }

    Settings setMaxLimit(float maxLimit) {
      int val = maxLimit <= 0 ? 0 : (int)(maxLimit*1000);
      if (maxLimit == val) {
        return this;
      }
      Settings result = inUse ? copy() : this;
      result.maxLimit = val;
      return result;
    }

    Settings setMinLimit(float minLimit) {
      int val = minLimit <= 0 ? 0 : (int)(minLimit*1000);
      if (minLimit == val) {
        return this;
      }
      Settings result = inUse ? copy() : this;
      result.minLimit = val;
      return result;
    }

    Settings setAllowZero(boolean allow) {
      if (this.allowZero == allow) {
        return this;
      }
      Settings result = inUse ? copy() : this;
      result.allowZero = allow;
      return result;
    }

    Settings setWeeksAloneOnly(boolean weeksAlone) {
      if (this.weeksAloneOnly == weeksAlone) {
        return this;
      }
      Settings result = inUse ? copy() : this;
      result.weeksAloneOnly = weeksAlone;
      return result;
    }

    Settings setAllowMilliseconds(boolean allowMillis) {
      if (this.allowMillis == allowMillis) {
        return this;
      }
      Settings result = inUse ? copy() : this;
      result.allowMillis = allowMillis;
      return result;
    }

    Settings setLocale(String localeName) {
      PeriodFormatterData data = ds.get(localeName);
      return this
        .setAllowZero(data.allowZero())
        .setWeeksAloneOnly(data.weeksAloneOnly())
        .setAllowMilliseconds(data.useMilliseconds() != DataRecord.EMilliSupport.NO);
    }

    Settings setInUse() {
      inUse = true;
      return this;
    }

    Period createLimited(long duration, boolean inPast) {
      if (maxLimit > 0) {
          long maxUnitDuration = approximateDurationOf(maxUnit);
          if (duration * 1000 > maxLimit * maxUnitDuration) {
              return Period.moreThan(maxLimit/1000f, maxUnit).inPast(inPast);
          }
      }

      if (minLimit > 0) {
          TimeUnit emu = effectiveMinUnit();
          long emud = approximateDurationOf(emu);
          long eml = (emu == minUnit) ? minLimit :
              Math.max(1000, (approximateDurationOf(minUnit) * minLimit) / emud);
          if (duration * 1000 < eml * emud) {
              return Period.lessThan(eml/1000f, emu).inPast(inPast);
          }
      }
      return null;
    }

    public Settings copy() {
        Settings result = new Settings();
        result.inUse = inUse;
        result.uset = uset;
        result.maxUnit = maxUnit;
        result.minUnit = minUnit;
        result.maxLimit = maxLimit;
        result.minLimit = minLimit;
        result.allowZero = allowZero;
        result.weeksAloneOnly = weeksAloneOnly;
        result.allowMillis = allowMillis;
        return result;
    }
  }

  @Override
  public PeriodBuilderFactory setAvailableUnitRange(TimeUnit minUnit,
                                                    TimeUnit maxUnit) {
    int uset = 0;
    for (int i = maxUnit.ordinal; i <= minUnit.ordinal; ++i) {
        uset |= 1 << i;
    }
    if (uset == 0) {
        throw new IllegalArgumentException("range " + minUnit + " to " + maxUnit + " is empty");
    }
    settings = settings.setUnits(uset);
    return this;
  }

  @Override
  public PeriodBuilderFactory setUnitIsAvailable(TimeUnit unit,
                                                      boolean available) {
    int uset = settings.uset;
    if (available) {
      uset |= 1 << unit.ordinal;
    } else {
      uset &= ~(1 << unit.ordinal);
    }
    settings = settings.setUnits(uset);
    return this;
  }

  @Override
  public PeriodBuilderFactory setMaxLimit(float maxLimit) {
    settings = settings.setMaxLimit(maxLimit);
    return this;
  }

  @Override
  public PeriodBuilderFactory setMinLimit(float minLimit) {
    settings = settings.setMinLimit(minLimit);
    return this;
  }

  @Override
  public PeriodBuilderFactory setAllowZero(boolean allow) {
    settings = settings.setAllowZero(allow);
    return this;
  }

  @Override
  public PeriodBuilderFactory setWeeksAloneOnly(boolean aloneOnly) {
    settings = settings.setWeeksAloneOnly(aloneOnly);
    return this;
  }

  @Override
  public PeriodBuilderFactory setAllowMilliseconds(boolean allow) {
    settings = settings.setAllowMilliseconds(allow);
    return this;
  }

  @Override
  public PeriodBuilderFactory setLocale(String localeName) {
    settings = settings.setLocale(localeName);
    return this;
  }

  @Override
  public PeriodBuilderFactory setTimeZone(TimeZone timeZone) {
      // ignore this
      return this;
  }

  private Settings getSettings() {
    if (settings.effectiveSet() == 0) {
      return null;
    }
    return settings.setInUse();
  }

  /**
   * Return a builder that represents relative time in terms of the single
   * given TimeUnit
   *
   * @param unit the single TimeUnit with which to represent times
   * @return a builder
   */
  @Override
  public PeriodBuilder getFixedUnitBuilder(TimeUnit unit) {
    return FixedUnitBuilder.get(unit, getSettings());
  }

  /**
   * Return a builder that represents relative time in terms of the
   * largest period less than or equal to the duration.
   *
   * @return a builder
   */
  @Override
  public PeriodBuilder getSingleUnitBuilder() {
    return SingleUnitBuilder.get(getSettings());
  }

  /**
   * Return a builder that formats the largest one or two periods,
   * Starting with the largest period less than or equal to the duration.
   * It formats two periods if the first period has a count &lt; 2
   * and the next period has a count &gt;= 1.
   *
   * @return a builder
   */
  @Override
  public PeriodBuilder getOneOrTwoUnitBuilder() {
    return OneOrTwoUnitBuilder.get(getSettings());
  }

  /**
   * Return a builder that formats the given number of periods,
   * starting with the largest period less than or equal to the
   * duration.
   *
   * @return a builder
   */
  @Override
  public PeriodBuilder getMultiUnitBuilder(int periodCount) {
    return MultiUnitBuilder.get(periodCount, getSettings());
  }
}

abstract class PeriodBuilderImpl implements PeriodBuilder {

  protected BasicPeriodBuilderFactory.Settings settings;

  @Override
  public Period create(long duration) {
    return createWithReferenceDate(duration, System.currentTimeMillis());
  }

  public long approximateDurationOf(TimeUnit unit) {
    return BasicPeriodBuilderFactory.approximateDurationOf(unit);
  }

  @Override
  public Period createWithReferenceDate(long duration, long referenceDate) {
    boolean inPast = duration < 0;
    if (inPast) {
      duration = -duration;
    }
    Period ts = settings.createLimited(duration, inPast);
    if (ts == null) {
      ts = handleCreate(duration, referenceDate, inPast);
      if (ts == null) {
        ts = Period.lessThan(1, settings.effectiveMinUnit()).inPast(inPast);
      }
    }
    return ts;
  }

  @Override
  public PeriodBuilder withTimeZone(TimeZone timeZone) {
      // ignore the time zone
      return this;
  }

  @Override
  public PeriodBuilder withLocale(String localeName) {
    BasicPeriodBuilderFactory.Settings newSettings = settings.setLocale(localeName);
    if (newSettings != settings) {
      return withSettings(newSettings);
    }
    return this;
  }

  protected abstract PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settingsToUse);

  protected abstract Period handleCreate(long duration, long referenceDate,
                                         boolean inPast);

  protected PeriodBuilderImpl(BasicPeriodBuilderFactory.Settings settings) {
    this.settings = settings;
  }
}

class FixedUnitBuilder extends PeriodBuilderImpl {
  private TimeUnit unit;

  public static FixedUnitBuilder get(TimeUnit unit, BasicPeriodBuilderFactory.Settings settingsToUse) {
    if (settingsToUse != null && (settingsToUse.effectiveSet() & (1 << unit.ordinal)) != 0) {
      return new FixedUnitBuilder(unit, settingsToUse);
    }
    return null;
  }

  FixedUnitBuilder(TimeUnit unit, BasicPeriodBuilderFactory.Settings settings) {
    super(settings);
    this.unit = unit;
  }

  @Override
  protected PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settingsToUse) {
    return get(unit, settingsToUse);
  }

  @Override
  protected Period handleCreate(long duration, long referenceDate,
                                boolean inPast) {
    if (unit == null) {
      return null;
    }
    long unitDuration = approximateDurationOf(unit);
    return Period.at((float)((double)duration/unitDuration), unit)
        .inPast(inPast);
  }
}

class SingleUnitBuilder extends PeriodBuilderImpl {
  SingleUnitBuilder(BasicPeriodBuilderFactory.Settings settings) {
    super(settings);
  }

  public static SingleUnitBuilder get(BasicPeriodBuilderFactory.Settings settings) {
    if (settings == null) {
      return null;
    }
    return new SingleUnitBuilder(settings);
  }

  @Override
  protected PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settingsToUse) {
    return SingleUnitBuilder.get(settingsToUse);
  }

  @Override
  protected Period handleCreate(long duration, long referenceDate,
                                boolean inPast) {
    short uset = settings.effectiveSet();
    for (int i = 0; i < TimeUnit.units.length; ++i) {
      if (0 != (uset & (1 << i))) {
        TimeUnit unit = TimeUnit.units[i];
        long unitDuration = approximateDurationOf(unit);
        if (duration >= unitDuration) {
          return Period.at((float)((double)duration/unitDuration), unit)
              .inPast(inPast);
        }
      }
    }
    return null;
  }
}

class OneOrTwoUnitBuilder extends PeriodBuilderImpl {
  OneOrTwoUnitBuilder(BasicPeriodBuilderFactory.Settings settings) {
    super(settings);
  }

  public static OneOrTwoUnitBuilder get(BasicPeriodBuilderFactory.Settings settings) {
    if (settings == null) {
      return null;
    }
    return new OneOrTwoUnitBuilder(settings);
  }

  @Override
  protected PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settingsToUse) {
    return OneOrTwoUnitBuilder.get(settingsToUse);
  }

  @Override
  protected Period handleCreate(long duration, long referenceDate,
                                boolean inPast) {
    Period period = null;
    short uset = settings.effectiveSet();
    for (int i = 0; i < TimeUnit.units.length; ++i) {
      if (0 != (uset & (1 << i))) {
        TimeUnit unit = TimeUnit.units[i];
        long unitDuration = approximateDurationOf(unit);
        if (duration >= unitDuration || period != null) {
          double count = (double)duration/unitDuration;
          if (period == null) {
            if (count >= 2) {
              period = Period.at((float)count, unit);
              break;
            }
            period = Period.at(1, unit).inPast(inPast);
            duration -= unitDuration;
          } else {
            if (count >= 1) {
              period = period.and((float)count, unit);
            }
            break;
          }
        }
      }
    }
    return period;
  }
}

class MultiUnitBuilder extends PeriodBuilderImpl {
  private int nPeriods;

  MultiUnitBuilder(int nPeriods, BasicPeriodBuilderFactory.Settings settings) {
    super(settings);
    this.nPeriods = nPeriods;
  }

  public static MultiUnitBuilder get(int nPeriods, BasicPeriodBuilderFactory.Settings settings) {
    if (nPeriods > 0 && settings != null) {
      return new MultiUnitBuilder(nPeriods, settings);
    }
    return null;
  }

  @Override
  protected PeriodBuilder withSettings(BasicPeriodBuilderFactory.Settings settingsToUse) {
    return MultiUnitBuilder.get(nPeriods, settingsToUse);
  }

  @Override
  protected Period handleCreate(long duration, long referenceDate,
                                boolean inPast) {
    Period period = null;
    int n = 0;
    short uset = settings.effectiveSet();
    for (int i = 0; i < TimeUnit.units.length; ++i) {
      if (0 != (uset & (1 << i))) {
        TimeUnit unit = TimeUnit.units[i];
        if (n == nPeriods) {
          break;
        }
        long unitDuration = approximateDurationOf(unit);
        if (duration >= unitDuration || n > 0) {
          ++n;
          double count = (double)duration / unitDuration;
          if (n < nPeriods) {
            count = Math.floor(count);
            duration -= (long)(count * unitDuration);
          }
          if (period == null) {
            period = Period.at((float)count, unit).inPast(inPast);
          } else {
            period = period.and((float)count, unit);
          }
        }
      }
    }
    return period;
  }
}

