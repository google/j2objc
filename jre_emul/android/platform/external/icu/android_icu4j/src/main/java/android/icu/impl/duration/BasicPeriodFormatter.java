/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007-2010, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl.duration;

import android.icu.impl.duration.BasicPeriodFormatterFactory.Customizations;
import android.icu.impl.duration.impl.DataRecord.ECountVariant;
import android.icu.impl.duration.impl.DataRecord.EMilliSupport;
import android.icu.impl.duration.impl.DataRecord.ESeparatorVariant;
import android.icu.impl.duration.impl.DataRecord.ETimeDirection;
import android.icu.impl.duration.impl.DataRecord.ETimeLimit;
import android.icu.impl.duration.impl.PeriodFormatterData;

/**
 * Core implementation class for PeriodFormatter.
 */
class BasicPeriodFormatter implements PeriodFormatter {
  private BasicPeriodFormatterFactory factory;
  private String localeName;
  private PeriodFormatterData data;
  private Customizations customs;

  BasicPeriodFormatter(BasicPeriodFormatterFactory factory,
                       String localeName,
                       PeriodFormatterData data,
                       Customizations customs) {
    this.factory = factory;
    this.localeName = localeName;
    this.data = data;
    this.customs = customs;
  }

  @Override
public String format(Period period) {
    if (!period.isSet()) {
      throw new IllegalArgumentException("period is not set");
    }
    return format(period.timeLimit, period.inFuture, period.counts);
  }

  @Override
  public PeriodFormatter withLocale(String locName) {
    if (!this.localeName.equals(locName)) {
      PeriodFormatterData newData = factory.getData(locName);
      return new BasicPeriodFormatter(factory, locName, newData,
                                      customs);
    }
    return this;
  }

  private String format(int tl, boolean inFuture, int[] counts) {
    int mask = 0;
    for (int i = 0; i < counts.length; ++i) {
      if (counts[i] > 0) {
        mask |= 1 << i;
      }
    }

    // if the data does not allow formatting of zero periods,
    // remove these from consideration.  If the result has no
    // periods set, return null to indicate we could not format
    // the duration.
    if (!data.allowZero()) {
      for (int i = 0, m = 1; i < counts.length; ++i, m <<= 1) {
        if ((mask & m) != 0 && counts[i] == 1) {
          mask &= ~m;
        }
      }
      if (mask == 0) {
        return null;
      }
    }

    // if the data does not allow milliseconds but milliseconds are
    // set, merge them with seconds and force display of seconds to
    // decimal with 3 places.
    boolean forceD3Seconds = false;
    if (data.useMilliseconds() != EMilliSupport.YES &&
        (mask & (1 << TimeUnit.MILLISECOND.ordinal)) != 0) {
      int sx = TimeUnit.SECOND.ordinal;
      int mx = TimeUnit.MILLISECOND.ordinal;
      int sf = 1 << sx;
      int mf = 1 << mx;
      switch (data.useMilliseconds()) {
        case EMilliSupport.WITH_SECONDS: {
          // if there are seconds, merge with seconds, otherwise leave alone
          if ((mask & sf) != 0) {
            counts[sx] += (counts[mx]-1)/1000;
            mask &= ~mf;
            forceD3Seconds = true;
          }
        } break;
        case EMilliSupport.NO: {
          // merge with seconds, reset seconds before use just in case
          if ((mask & sf) == 0) {
            mask |= sf;
            counts[sx] = 1;
          }
          counts[sx] += (counts[mx]-1)/1000;
          mask &= ~mf;
          forceD3Seconds = true;
        } break;
      }
    }

    // get the first and last units that are set.
    int first = 0;
    int last = counts.length - 1;
    while (first < counts.length && (mask & (1 << first)) == 0) ++first;
    while (last > first && (mask & (1 << last)) == 0) --last;

    // determine if there is any non-zero unit
    boolean isZero = true;
    for (int i = first; i <= last; ++i) {
      if (((mask & (1 << i)) != 0) &&  counts[i] > 1) {
        isZero = false;
        break;
      }
    }

    StringBuffer sb = new StringBuffer();

    // if we've been requested to not display a limit, or there are
    // no non-zero units, do not display the limit.
    if (!customs.displayLimit || isZero) {
      tl = ETimeLimit.NOLIMIT;
    }

    // if we've been requested to not display the direction, or there
    // are no non-zero units, do not display the direction.
    int td;
    if (!customs.displayDirection || isZero) {
      td = ETimeDirection.NODIRECTION;
    } else {
      td = inFuture ? ETimeDirection.FUTURE : ETimeDirection.PAST;
    }

    // format the initial portion of the string before the units.
    // record whether we need to use a digit prefix (because the
    // initial portion forces it)
    boolean useDigitPrefix = data.appendPrefix(tl, td, sb);

    // determine some formatting params and initial values
    boolean multiple = first != last;
    boolean wasSkipped = true; // no initial skip marker
    boolean skipped = false;
    boolean countSep = customs.separatorVariant != ESeparatorVariant.NONE;

    // loop for formatting the units
    for (int i = first, j = i; i <= last; i = j) {
      if (skipped) {
        // we didn't format the previous unit
        data.appendSkippedUnit(sb);
        skipped = false;
        wasSkipped = true;
      }

      while (++j < last && (mask & (1 << j)) == 0) {
        skipped = true; // skip
      }

      TimeUnit unit = TimeUnit.units[i];
      int count = counts[i] - 1;

      int cv = customs.countVariant;
      if (i == last) {
        if (forceD3Seconds) {
          cv = ECountVariant.DECIMAL3;
        }
        // else leave unchanged
      } else {
        cv = ECountVariant.INTEGER;
      }
      boolean isLast = i == last;
      boolean mustSkip = data.appendUnit(unit, count, cv, customs.unitVariant,
                                         countSep, useDigitPrefix, multiple, isLast, wasSkipped, sb);
      skipped |= mustSkip;
      wasSkipped = false;

      if (customs.separatorVariant != ESeparatorVariant.NONE && j <= last) {
        boolean afterFirst = i == first;
        boolean beforeLast = j == last;
        boolean fullSep = customs.separatorVariant == ESeparatorVariant.FULL;
        useDigitPrefix = data.appendUnitSeparator(unit, fullSep, afterFirst, beforeLast, sb);
      } else {
        useDigitPrefix = false;
      }
    }
    data.appendSuffix(tl, td, sb);

    return sb.toString();
  }
}
