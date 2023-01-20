/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.icu.util;

import android.icu.impl.Grego;
import android.icu.util.AnnualTimeZoneRule;
import android.icu.util.BasicTimeZone;
import android.icu.util.DateTimeRule;
import android.icu.util.InitialTimeZoneRule;
import android.icu.util.TimeArrayTimeZoneRule;
import android.icu.util.TimeZone;
import android.icu.util.TimeZoneRule;
import android.icu.util.TimeZoneTransition;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneOffsetTransitionRule;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import libcore.api.IntraCoreApi;

/**
 * Provide extra functionalities on top of {@link TimeZone} public APIs.
 *
 * @hide
 */
@IntraCoreApi
public class ExtendedTimeZone {

  private final TimeZone timezone;

  private ExtendedTimeZone(String id) {
    timezone = TimeZone.getTimeZone(id);
  }

  // The API which calls an implementation in android.icu does not use nullability annotation
  // because the upstream can't guarantee the stability. See http://b/140196694.
  /**
   * Returns an instance from the time zone ID. Note that the returned instance could be shared.
   *
   * @see TimeZone#getTimeZone(String) for the more information.
   * @hide
   */
  @IntraCoreApi
  public static ExtendedTimeZone getInstance(String id) {
    return new ExtendedTimeZone(id);
  }

  /**
   * Returns the underlying {@link TimeZone} instance.
   *
   * @hide
   */
  @IntraCoreApi
  public TimeZone getTimeZone() {
    return timezone;
  }

  /**
   * Returns a {@link ZoneRules} instance for this time zone.
   *
   * @throws ZoneRulesException if the internal rules can't be parsed correctly, or it's not
   *     implemented for the subtype of {@link TimeZone}.
   * @implNote This implementations relies on {@link BasicTimeZone#getTimeZoneRules()} in the
   *     following way: Returned array starts with {@code InitialTimeZoneRule}, followed by {@code
   *     TimeArrayTimeZoneRule}, and, if available, ends with {@code AnnualTimeZoneRule}.
   * @hide
   */
  @IntraCoreApi
  public ZoneRules createZoneRules() {
    if (!(timezone instanceof BasicTimeZone)) {
      throw zoneRulesException(
          "timezone is "
              + timezone.getClass().getCanonicalName()
              + " which is not instance of BasicTimeZone");
    }

    BasicTimeZone basicTimeZone = (BasicTimeZone) timezone;

    TimeZoneRule[] timeZoneRules = basicTimeZone.getTimeZoneRules();

    if (timeZoneRules.length == 0) {
      throw zoneRulesException("Got 0 time zone rules");
    }

    ZoneOffset baseStandardOffset = null;
    ZoneOffset baseWallOffset = null;

    NavigableMap<Long, TimeArrayTimeZoneRule> rulesByStartTime = new TreeMap<>();
    boolean hasRecurringRules = false;

    for (TimeZoneRule timeZoneRule : timeZoneRules) {
      if (timeZoneRule instanceof InitialTimeZoneRule) {
        InitialTimeZoneRule initialTimeZoneRule = (InitialTimeZoneRule) timeZoneRule;
        baseStandardOffset = standardOffset(initialTimeZoneRule);
        baseWallOffset = fullOffset(initialTimeZoneRule);
      } else if (timeZoneRule instanceof TimeArrayTimeZoneRule) {
        TimeArrayTimeZoneRule timeArrayTimeZoneRule = (TimeArrayTimeZoneRule) timeZoneRule;

        for (long startTime : timeArrayTimeZoneRule.getStartTimes()) {
          rulesByStartTime.put(
              utcStartTime(startTime, timeArrayTimeZoneRule), timeArrayTimeZoneRule);
        }
      } else if (timeZoneRule instanceof AnnualTimeZoneRule) {
        // Order of AnnualTimeZoneRule-s in BasicTimeZone#getTimeZoneRules is not
        // specified, they will be fetched using different API.
        hasRecurringRules = true;
      } else {
        throw zoneRulesException("Unrecognized time zone rule " + timeZoneRule.getClass() + ".");
      }
    }

    // Keep in mind that transitionList is not superset of standardOffsetTransitionList.
    // transitionList keeps track of wall clock changes, but it might remain the same after
    // standard offset change if DST was changed too.
    List<ZoneOffsetTransition> standardOffsetTransitionList = new ArrayList<>();
    List<ZoneOffsetTransition> transitionList = new ArrayList<>();

    ZoneOffset lastStandardOffset = baseStandardOffset;
    ZoneOffset lastWallOffset = baseWallOffset;

    for (Map.Entry<Long, TimeArrayTimeZoneRule> entry : rulesByStartTime.entrySet()) {
      long startTime = entry.getKey();
      TimeArrayTimeZoneRule timeZoneRule = entry.getValue();

      ZoneOffset ruleStandardOffset = standardOffset(timeZoneRule);

      if (!ruleStandardOffset.equals(lastStandardOffset)) {
        // ZoneRules needs changes in standard offsets only as an argument.
        // ZoneOffsetTransition requires before and after offsets to be different, so wall
        // clock offset can't be used as beforeOffset(it can be equal to afterOffset). Using
        // previous standard offset seems to be the only reasonable choice left.
        // As of 2021 transition and beforeOffset arguments are used to calculate UTC offset
        // of the switch date and previous standard offset will do the trick.
        ZoneOffsetTransition zoneOffsetTransition =
            ZoneOffsetTransition.of(
                localDateTime(startTime, lastStandardOffset),
                lastStandardOffset,
                ruleStandardOffset);

        standardOffsetTransitionList.add(zoneOffsetTransition);
        lastStandardOffset = ruleStandardOffset;
      }

      ZoneOffset ruleWallOffset = fullOffset(timeZoneRule);

      // ZoneOffsetTransition tracks only changes in full offset - if raw and DST offsets
      // sum is not changed after a transition, such transition is not tracked by ZoneRules.
      // ICU does not squash such transitions.
      if (!lastWallOffset.equals(ruleWallOffset)) {
        ZoneOffsetTransition zoneOffsetTransition =
            ZoneOffsetTransition.of(
                localDateTime(startTime, lastWallOffset), lastWallOffset, ruleWallOffset);

        transitionList.add(zoneOffsetTransition);
        lastWallOffset = ruleWallOffset;
      }
    }

    List<ZoneOffsetTransitionRule> lastRules = new ArrayList<>();

    if (hasRecurringRules) {
      List<AnnualTimeZoneRule> annualTimeZoneRules = new ArrayList<>();

      // ZoneOffsetTransitionRule requires beforeOffset. As total offset in
      // TimeArrayTimeZoneRule may differ from offset of the last recurring rule
      // we apply all available recurring rule once. It is possible to build lastRules
      // in loop below, but doing it in separate loop simplifies code significantly.
      TimeZoneTransition firstTransitionToAnnualRule =
          basicTimeZone.getNextTransition(rulesByStartTime.lastKey(), false /* inclusive */);
      AnnualTimeZoneRule firstAnnualRule = (AnnualTimeZoneRule) firstTransitionToAnnualRule.getTo();
      AnnualTimeZoneRule currentTimeZoneRule = firstAnnualRule;
      long currentUnixEpochTime = firstTransitionToAnnualRule.getTime();

      do {
        annualTimeZoneRules.add(currentTimeZoneRule);

        if (annualTimeZoneRules.size() > 16) {
          throw zoneRulesException("More than 16 annual transitions found.");
        }

        ZoneOffset ruleStandardOffset = standardOffset(currentTimeZoneRule);

        if (!lastStandardOffset.equals(ruleStandardOffset)) {
          standardOffsetTransitionList.add(
              ZoneOffsetTransition.of(
                  localDateTime(currentUnixEpochTime, lastStandardOffset),
                  lastStandardOffset,
                  ruleStandardOffset));
          lastStandardOffset = ruleStandardOffset;
        }

        int currentYear =
            Instant.ofEpochMilli(currentUnixEpochTime).atOffset(lastWallOffset).getYear();
        ZoneOffsetTransition recurringRuleTransition =
            createZoneOffsetTransitionRule(currentTimeZoneRule, lastStandardOffset, lastWallOffset)
                .createTransition(currentYear);

        // After introduction of first annual rule wall offset may not change.
        if (!lastWallOffset.equals(recurringRuleTransition.getOffsetAfter())) {
          transitionList.add(
              ZoneOffsetTransition.of(
                  localDateTime(currentUnixEpochTime, lastWallOffset),
                  lastWallOffset,
                  recurringRuleTransition.getOffsetAfter()));
          lastWallOffset = recurringRuleTransition.getOffsetAfter();
        }

        TimeZoneTransition nextTransition =
            basicTimeZone.getNextTransition(currentUnixEpochTime, false /* inclusive */);
        currentUnixEpochTime = nextTransition.getTime();
        currentTimeZoneRule = (AnnualTimeZoneRule) nextTransition.getTo();

        if (currentTimeZoneRule == null) {
          throw zoneRulesException(
              "No transitions after "
                  + currentUnixEpochTime
                  + " for a timezone with recurring rules");
        }
      } while (!currentTimeZoneRule.isEquivalentTo(firstAnnualRule));

      // All annual rules use the same standard offset and wall offset is always updated on
      // transition.
      // The initial value of lastWallOffset is the wall offset of the last recurring
      // AnnualTimeZoneRule.
      for (AnnualTimeZoneRule annualTimeZoneRule : annualTimeZoneRules) {
        ZoneOffsetTransitionRule zoneOffsetTransitionRule =
            createZoneOffsetTransitionRule(annualTimeZoneRule, lastStandardOffset, lastWallOffset);

        lastWallOffset = zoneOffsetTransitionRule.getOffsetAfter();

        lastRules.add(zoneOffsetTransitionRule);
      }

      // ZoneRules does not specify it, but internally it expects lastRules to be sorted
      // (see ZoneRules#getOffset) in the order they will happen within a year. For example,
      // if rule A starts in October 2021, and rule B starts in March 2022, expected order
      // is [B, A].
      // We assume that for any year that order is fixed, even though it is possible
      // to build set of rules where order depends on a given year.
      // annualTimeZoneRules stores rules in the order they happened, so we just need to find
      // a break in startYear.
      int firstRuleIndex = 0;
      while (firstRuleIndex < annualTimeZoneRules.size()
          && firstAnnualRule.getStartYear()
              == annualTimeZoneRules.get(firstRuleIndex).getStartYear()) {
        ++firstRuleIndex;
      }

      Collections.rotate(lastRules, -firstRuleIndex);
    }

    return ZoneRules.of(
        baseStandardOffset,
        baseWallOffset,
        standardOffsetTransitionList,
        transitionList,
        lastRules);
  }

  /**
   * Converts {@link AnnualTimeZoneRule} to {@link ZoneOffsetTransitionRule}. Switch date may be
   * represented relative to UTC, wall clock, or standard offset. For the latter 2 cases {@code
   * lastWallOffset} and {@code lastStandardOffset} are used.
   *
   * @param annualTimeZoneRule rule to be converted
   * @param lastStandardOffset standard offset of a rule which preceded {@code annualTimeZoneRule}
   * @param lastWallOffset wall offset of a rule which preceded {@code annualTimeZoneRule}
   */
  private ZoneOffsetTransitionRule createZoneOffsetTransitionRule(
      AnnualTimeZoneRule annualTimeZoneRule,
      ZoneOffset lastStandardOffset,
      ZoneOffset lastWallOffset) {
    DateTimeRule dateTimeRule = annualTimeZoneRule.getRule();
    Month month = Month.of(dateTimeRule.getRuleMonth() + 1);
    final DayOfWeek dayOfWeek;
    final int dayOfMonthIndicator;
    switch (dateTimeRule.getDateRuleType()) {
      case DateTimeRule.DOM:
        dayOfMonthIndicator = dateTimeRule.getRuleDayOfMonth();
        dayOfWeek = null;
        break;
      case DateTimeRule.DOW:
        int weekInMonth = dateTimeRule.getRuleWeekInMonth();
        if (weekInMonth > 0) {
          dayOfMonthIndicator = (weekInMonth - 1) * 7 + 1;
        } else if (weekInMonth < 0) {
          dayOfMonthIndicator = (weekInMonth + 1) * 7 - 1;
        } else {
          throw zoneRulesException(
              "Invalid DateTimeRule in "
                  + annualTimeZoneRule
                  + ". Non-zero weekInMonth expected in "
                  + dateTimeRule);
        }
        dayOfWeek = dayOfWeek(dateTimeRule);
        break;
      case DateTimeRule.DOW_GEQ_DOM:
        dayOfMonthIndicator = dateTimeRule.getRuleDayOfMonth();
        dayOfWeek = dayOfWeek(dateTimeRule);
        break;
      case DateTimeRule.DOW_LEQ_DOM:
        // java.time.ZoneRules uses negative numbers to indicate that switch date should
        // come before certain date. Using leap year so that lastSun like rule will
        // always work correctly.
        dayOfMonthIndicator = dateTimeRule.getRuleDayOfMonth() - month.maxLength() - 1;
        dayOfWeek = dayOfWeek(dateTimeRule);
        break;
      default:
        throw zoneRulesException(
            "Unexpected dateTimeRule.dateRuleType="
                + dateTimeRule.getTimeRuleType()
                + " in "
                + annualTimeZoneRule);
    }

    final boolean timeEndOfDay;
    final LocalTime switchDateTime;
    if (dateTimeRule.getRuleMillisInDay() == Grego.MILLIS_PER_DAY) {
      timeEndOfDay = true;
      switchDateTime = LocalTime.MIDNIGHT;
    } else {
      timeEndOfDay = false;
      switchDateTime = LocalTime.ofNanoOfDay(dateTimeRule.getRuleMillisInDay() * 1_000_000L);
    }

    ZoneOffsetTransitionRule.TimeDefinition timeDefinition = timeDefinition(annualTimeZoneRule);

    // JavaDoc for standardOffset tells that it should be "offset in force at the cutover".
    // It's not clear what offset is in effect at the cutover moment, but zic format assumes
    // that only DST is changed in annual rules and standard offset is handled differently.
    return ZoneOffsetTransitionRule.of(
        month,
        dayOfMonthIndicator,
        dayOfWeek,
        switchDateTime,
        timeEndOfDay,
        timeDefinition,
        lastStandardOffset,
        lastWallOffset,
        fullOffset(annualTimeZoneRule));
  }

  private ZoneOffsetTransitionRule.TimeDefinition timeDefinition(
      AnnualTimeZoneRule annualTimeZoneRule) {
    DateTimeRule dateTimeRule = annualTimeZoneRule.getRule();
    switch (dateTimeRule.getTimeRuleType()) {
      case DateTimeRule.STANDARD_TIME:
        return ZoneOffsetTransitionRule.TimeDefinition.STANDARD;
      case DateTimeRule.UTC_TIME:
        return ZoneOffsetTransitionRule.TimeDefinition.UTC;
      case DateTimeRule.WALL_TIME:
        return ZoneOffsetTransitionRule.TimeDefinition.WALL;
      default:
        throw zoneRulesException(
            "Unexpected dateTimeRule.timeRuleType="
                + dateTimeRule.getTimeRuleType()
                + " in AnnualTimeZoneRule: "
                + annualTimeZoneRule);
    }
  }

  private long utcStartTime(long startTime, TimeArrayTimeZoneRule timeZoneRule) {
    switch (timeZoneRule.getTimeType()) {
      case DateTimeRule.UTC_TIME:
        return startTime;
      case DateTimeRule.STANDARD_TIME:
        return startTime - timeZoneRule.getRawOffset();
      case DateTimeRule.WALL_TIME:
        return startTime - timeZoneRule.getRawOffset() - timeZoneRule.getDSTSavings();
      default:
        throw zoneRulesException("Unexpected timeType in " + timeZoneRule);
    }
  }

  private ZoneRulesException zoneRulesException(String message) {
    return new ZoneRulesException(
        "Failed to build ZoneRules for " + timezone.getID() + ". " + message);
  }

  private static LocalDateTime localDateTime(long epochMillis, ZoneOffset zoneOffset) {
    return Instant.ofEpochMilli(epochMillis).atOffset(zoneOffset).toLocalDateTime();
  }

  private static DayOfWeek dayOfWeek(DateTimeRule dateTimeRule) {
    return DayOfWeek.SUNDAY.plus(dateTimeRule.getRuleDayOfWeek() - 1);
  }

  private static ZoneOffset standardOffset(TimeZoneRule timeZoneRule) {
    return toOffset(timeZoneRule.getRawOffset());
  }

  private static ZoneOffset fullOffset(TimeZoneRule timeZoneRule) {
    return toOffset(timeZoneRule.getRawOffset() + timeZoneRule.getDSTSavings());
  }

  private static ZoneOffset toOffset(int rawOffset) {
    return ZoneOffset.ofTotalSeconds(rawOffset / 1_000);
  }
}
