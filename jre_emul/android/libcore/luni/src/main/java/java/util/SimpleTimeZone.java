/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util;

/**
 * {@code SimpleTimeZone} is a concrete subclass of {@code TimeZone}
 * that represents a time zone for use with a Gregorian calendar. This class
 * does not handle historical changes.
 * <p>
 * Use a negative value for {@code dayOfWeekInMonth} to indicate that
 * {@code SimpleTimeZone} should count from the end of the month
 * backwards. For example, Daylight Savings Time ends at the last
 * (dayOfWeekInMonth = -1) Sunday in October, at 2 AM in standard time.
 *
 * @see Calendar
 * @see GregorianCalendar
 * @see TimeZone
 */
public class SimpleTimeZone extends TimeZone {

    private static final long serialVersionUID = -403250971215465050L;

    private int rawOffset;

    private int startYear, startMonth, startDay, startDayOfWeek, startTime;

    private int endMonth, endDay, endDayOfWeek, endTime;

    private int startMode, endMode;

    private static final int DOM_MODE = 1, DOW_IN_MONTH_MODE = 2,
            DOW_GE_DOM_MODE = 3, DOW_LE_DOM_MODE = 4;

    /**
     * The constant for representing a start or end time in GMT time mode.
     */
    public static final int UTC_TIME = 2;

    /**
     * The constant for representing a start or end time in standard local time mode,
     * based on timezone's raw offset from GMT; does not include Daylight
     * savings.
     */
    public static final int STANDARD_TIME = 1;

    /**
     * The constant for representing a start or end time in local wall clock time
     * mode, based on timezone's adjusted offset from GMT; includes
     * Daylight savings.
     */
    public static final int WALL_TIME = 0;

    private boolean useDaylight;

    private int dstSavings = 3600000;

    /**
     * Constructs a {@code SimpleTimeZone} with the given base time zone offset from GMT
     * and time zone ID. Timezone IDs can be obtained from
     * {@code TimeZone.getAvailableIDs}. Normally you should use {@code TimeZone.getDefault} to
     * construct a {@code TimeZone}.
     *
     * @param offset
     *            the given base time zone offset to GMT.
     * @param name
     *            the time zone ID which is obtained from
     *            {@code TimeZone.getAvailableIDs}.
     */
    public SimpleTimeZone(int offset, final String name) {
        setID(name);
        rawOffset = offset;
    }

    /**
     * Constructs a {@code SimpleTimeZone} with the given base time zone offset from GMT,
     * time zone ID, and times to start and end the daylight savings time. Timezone IDs can
     * be obtained from {@code TimeZone.getAvailableIDs}. Normally you should use
     * {@code TimeZone.getDefault} to create a {@code TimeZone}. For a time zone that does not
     * use daylight saving time, do not use this constructor; instead you should
     * use {@code SimpleTimeZone(rawOffset, ID)}.
     * <p>
     * By default, this constructor specifies day-of-week-in-month rules. That
     * is, if the {@code startDay} is 1, and the {@code startDayOfWeek} is {@code SUNDAY}, then this
     * indicates the first Sunday in the {@code startMonth}. A {@code startDay} of -1 likewise
     * indicates the last Sunday. However, by using negative or zero values for
     * certain parameters, other types of rules can be specified.
     * <p>
     * Day of month: To specify an exact day of the month, such as March 1, set
     * {@code startDayOfWeek} to zero.
     * <p>
     * Day of week after day of month: To specify the first day of the week
     * occurring on or after an exact day of the month, make the day of the week
     * negative. For example, if {@code startDay} is 5 and {@code startDayOfWeek} is {@code -MONDAY},
     * this indicates the first Monday on or after the 5th day of the
     * {@code startMonth}.
     * <p>
     * Day of week before day of month: To specify the last day of the week
     * occurring on or before an exact day of the month, make the day of the
     * week and the day of the month negative. For example, if {@code startDay} is {@code -21}
     * and {@code startDayOfWeek} is {@code -WEDNESDAY}, this indicates the last Wednesday on or
     * before the 21st of the {@code startMonth}.
     * <p>
     * The above examples refer to the {@code startMonth}, {@code startDay}, and {@code startDayOfWeek};
     * the same applies for the {@code endMonth}, {@code endDay}, and {@code endDayOfWeek}.
     * <p>
     * The daylight savings time difference is set to the default value: one hour.
     *
     * @param offset
     *            the given base time zone offset to GMT.
     * @param name
     *            the time zone ID which is obtained from
     *            {@code TimeZone.getAvailableIDs}.
     * @param startMonth
     *            the daylight savings starting month. The month indexing is 0-based. eg, 0
     *            for January.
     * @param startDay
     *            the daylight savings starting day-of-week-in-month. Please see
     *            the member description for an example.
     * @param startDayOfWeek
     *            the daylight savings starting day-of-week. Please see the
     *            member description for an example.
     * @param startTime
     *            the daylight savings starting time in local wall time, which
     *            is standard time in this case. Please see the member
     *            description for an example.
     * @param endMonth
     *            the daylight savings ending month. The month indexing is 0-based. eg, 0 for
     *            January.
     * @param endDay
     *            the daylight savings ending day-of-week-in-month. Please see
     *            the member description for an example.
     * @param endDayOfWeek
     *            the daylight savings ending day-of-week. Please see the member
     *            description for an example.
     * @param endTime
     *            the daylight savings ending time in local wall time, which is
     *            daylight time in this case. Please see the member description
     *            for an example.
     * @throws IllegalArgumentException
     *             if the month, day, dayOfWeek, or time parameters are out of
     *             range for the start or end rule.
     */
    public SimpleTimeZone(int offset, String name, int startMonth,
            int startDay, int startDayOfWeek, int startTime, int endMonth,
            int endDay, int endDayOfWeek, int endTime) {
        this(offset, name, startMonth, startDay, startDayOfWeek, startTime,
                endMonth, endDay, endDayOfWeek, endTime, 3600000);
    }

    /**
     * Constructs a {@code SimpleTimeZone} with the given base time zone offset from GMT,
     * time zone ID, times to start and end the daylight savings time, and
     * the daylight savings time difference in milliseconds.
     *
     * @param offset
     *            the given base time zone offset to GMT.
     * @param name
     *            the time zone ID which is obtained from
     *            {@code TimeZone.getAvailableIDs}.
     * @param startMonth
     *            the daylight savings starting month. Month is 0-based. eg, 0
     *            for January.
     * @param startDay
     *            the daylight savings starting day-of-week-in-month. Please see
     *            the description of {@link #SimpleTimeZone(int, String, int, int, int, int, int, int, int, int)} for an example.
     * @param startDayOfWeek
     *            the daylight savings starting day-of-week. Please see the
     *            description of {@link #SimpleTimeZone(int, String, int, int, int, int, int, int, int, int)} for an example.
     * @param startTime
     *            The daylight savings starting time in local wall time, which
     *            is standard time in this case. Please see the description of
     *            {@link #SimpleTimeZone(int, String, int, int, int, int, int, int, int, int)} for an example.
     * @param endMonth
     *            the daylight savings ending month. Month is 0-based. eg, 0 for
     *            January.
     * @param endDay
     *            the daylight savings ending day-of-week-in-month. Please see
     *            the description of {@link #SimpleTimeZone(int, String, int, int, int, int, int, int, int, int)} for an example.
     * @param endDayOfWeek
     *            the daylight savings ending day-of-week. Please see the description of
     *            {@link #SimpleTimeZone(int, String, int, int, int, int, int, int, int, int)} for an example.
     * @param endTime
     *            the daylight savings ending time in local wall time, which is
     *            daylight time in this case. Please see the description of {@link #SimpleTimeZone(int, String, int, int, int, int, int, int, int, int)}
     *            for an example.
     * @param daylightSavings
     *            the daylight savings time difference in milliseconds.
     * @throws IllegalArgumentException
     *                if the month, day, dayOfWeek, or time parameters are out of
     *                range for the start or end rule.
     */
    public SimpleTimeZone(int offset, String name, int startMonth,
            int startDay, int startDayOfWeek, int startTime, int endMonth,
            int endDay, int endDayOfWeek, int endTime, int daylightSavings) {
        this(offset, name);
        if (daylightSavings <= 0) {
            throw new IllegalArgumentException("Invalid daylightSavings: " + daylightSavings);
        }
        dstSavings = daylightSavings;

        this.startMonth = startMonth;
        this.startDay = startDay;
        this.startDayOfWeek = startDayOfWeek;
        this.startTime = startTime;
        setStartMode();
        this.endMonth = endMonth;
        this.endDay = endDay;
        this.endDayOfWeek = endDayOfWeek;
        this.endTime = endTime;
        setEndMode();
    }

    /**
     * Construct a {@code SimpleTimeZone} with the given base time zone offset from GMT,
     * time zone ID, times to start and end the daylight savings time including a
     * mode specifier, the daylight savings time difference in milliseconds.
     * The mode specifies either {@link #WALL_TIME}, {@link #STANDARD_TIME}, or
     * {@link #UTC_TIME}.
     *
     * @param offset
     *            the given base time zone offset to GMT.
     * @param name
     *            the time zone ID which is obtained from
     *            {@code TimeZone.getAvailableIDs}.
     * @param startMonth
     *            the daylight savings starting month. The month indexing is 0-based. eg, 0
     *            for January.
     * @param startDay
     *            the daylight savings starting day-of-week-in-month. Please see
     *            the description of {@link #SimpleTimeZone(int, String, int, int, int, int, int, int, int, int)} for an example.
     * @param startDayOfWeek
     *            the daylight savings starting day-of-week. Please see the
     *            description of {@link #SimpleTimeZone(int, String, int, int, int, int, int, int, int, int)} for an example.
     * @param startTime
     *            the time of day in milliseconds on which daylight savings
     *            time starts, based on the {@code startTimeMode}.
     * @param startTimeMode
     *            the mode (UTC, standard, or wall time) of the start time
     *            value.
     * @param endDay
     *            the day of the week on which daylight savings time ends.
     * @param endMonth
     *            the daylight savings ending month. The month indexing is 0-based. eg, 0 for
     *            January.
     * @param endDayOfWeek
     *            the daylight savings ending day-of-week. Please see the description of
     *            {@link #SimpleTimeZone(int, String, int, int, int, int, int, int, int, int)} for an example.
     * @param endTime
     *            the time of day in milliseconds on which daylight savings
     *            time ends, based on the {@code endTimeMode}.
     * @param endTimeMode
     *            the mode (UTC, standard, or wall time) of the end time value.
     * @param daylightSavings
     *            the daylight savings time difference in milliseconds.
     * @throws IllegalArgumentException
     *             if the month, day, dayOfWeek, or time parameters are out of
     *             range for the start or end rule.
     */
    public SimpleTimeZone(int offset, String name, int startMonth,
            int startDay, int startDayOfWeek, int startTime, int startTimeMode,
            int endMonth, int endDay, int endDayOfWeek, int endTime,
            int endTimeMode, int daylightSavings) {

        this(offset, name, startMonth, startDay, startDayOfWeek, startTime,
                endMonth, endDay, endDayOfWeek, endTime, daylightSavings);
        startMode = startTimeMode;
        endMode = endTimeMode;
    }

    /**
     * Returns a new {@code SimpleTimeZone} with the same ID, {@code rawOffset} and daylight
     * savings time rules as this SimpleTimeZone.
     *
     * @return a shallow copy of this {@code SimpleTimeZone}.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        SimpleTimeZone zone = (SimpleTimeZone) super.clone();
        return zone;
    }

    /**
     * Compares the specified object to this {@code SimpleTimeZone} and returns whether they
     * are equal. The object must be an instance of {@code SimpleTimeZone} and have the
     * same internal data.
     *
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this
     *         {@code SimpleTimeZone}, {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone tz = (SimpleTimeZone) object;
        return getID().equals(tz.getID())
                && rawOffset == tz.rawOffset
                && useDaylight == tz.useDaylight
                && (!useDaylight || (startYear == tz.startYear
                        && startMonth == tz.startMonth
                        && startDay == tz.startDay && startMode == tz.startMode
                        && startDayOfWeek == tz.startDayOfWeek
                        && startTime == tz.startTime && endMonth == tz.endMonth
                        && endDay == tz.endDay
                        && endDayOfWeek == tz.endDayOfWeek
                        && endTime == tz.endTime && endMode == tz.endMode && dstSavings == tz.dstSavings));
    }

    @Override
    public int getDSTSavings() {
        if (!useDaylight) {
            return 0;
        }
        return dstSavings;
    }

    @Override
    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int time) {
        if (era != GregorianCalendar.BC && era != GregorianCalendar.AD) {
            throw new IllegalArgumentException("Invalid era: " + era);
        }
        checkRange(month, dayOfWeek, time);
        if (month != Calendar.FEBRUARY || day != 29 || !isLeapYear(year)) {
            checkDay(month, day);
        }

        if (!useDaylightTime() || era != GregorianCalendar.AD || year < startYear) {
            return rawOffset;
        }
        if (endMonth < startMonth) {
            if (month > endMonth && month < startMonth) {
                return rawOffset;
            }
        } else {
            if (month < startMonth || month > endMonth) {
                return rawOffset;
            }
        }

        int ruleDay = 0, daysInMonth, firstDayOfMonth = mod7(dayOfWeek - day);
        if (month == startMonth) {
            switch (startMode) {
                case DOM_MODE:
                    ruleDay = startDay;
                    break;
                case DOW_IN_MONTH_MODE:
                    if (startDay >= 0) {
                        ruleDay = mod7(startDayOfWeek - firstDayOfMonth) + 1
                                + (startDay - 1) * 7;
                    } else {
                        daysInMonth = GregorianCalendar.DaysInMonth[startMonth];
                        if (startMonth == Calendar.FEBRUARY && isLeapYear(
                                year)) {
                            daysInMonth += 1;
                        }
                        ruleDay = daysInMonth
                                + 1
                                + mod7(startDayOfWeek
                                - (firstDayOfMonth + daysInMonth))
                                + startDay * 7;
                    }
                    break;
                case DOW_GE_DOM_MODE:
                    ruleDay = startDay
                            + mod7(startDayOfWeek
                            - (firstDayOfMonth + startDay - 1));
                    break;
                case DOW_LE_DOM_MODE:
                    ruleDay = startDay
                            + mod7(startDayOfWeek
                            - (firstDayOfMonth + startDay - 1));
                    if (ruleDay != startDay) {
                        ruleDay -= 7;
                    }
                    break;
            }
            if (ruleDay > day || ruleDay == day && time < startTime) {
                return rawOffset;
            }
        }

        int ruleTime = endTime - dstSavings;
        int nextMonth = (month + 1) % 12;
        if (month == endMonth || (ruleTime < 0 && nextMonth == endMonth)) {
            switch (endMode) {
                case DOM_MODE:
                    ruleDay = endDay;
                    break;
                case DOW_IN_MONTH_MODE:
                    if (endDay >= 0) {
                        ruleDay = mod7(endDayOfWeek - firstDayOfMonth) + 1
                                + (endDay - 1) * 7;
                    } else {
                        daysInMonth = GregorianCalendar.DaysInMonth[endMonth];
                        if (endMonth == Calendar.FEBRUARY && isLeapYear(year)) {
                            daysInMonth++;
                        }
                        ruleDay = daysInMonth
                                + 1
                                + mod7(endDayOfWeek
                                - (firstDayOfMonth + daysInMonth)) + endDay
                                * 7;
                    }
                    break;
                case DOW_GE_DOM_MODE:
                    ruleDay = endDay
                            + mod7(
                            endDayOfWeek - (firstDayOfMonth + endDay - 1));
                    break;
                case DOW_LE_DOM_MODE:
                    ruleDay = endDay
                            + mod7(
                            endDayOfWeek - (firstDayOfMonth + endDay - 1));
                    if (ruleDay != endDay) {
                        ruleDay -= 7;
                    }
                    break;
            }

            int ruleMonth = endMonth;
            if (ruleTime < 0) {
                int changeDays = 1 - (ruleTime / 86400000);
                ruleTime = (ruleTime % 86400000) + 86400000;
                ruleDay -= changeDays;
                if (ruleDay <= 0) {
                    if (--ruleMonth < Calendar.JANUARY) {
                        ruleMonth = Calendar.DECEMBER;
                    }
                    ruleDay += GregorianCalendar.DaysInMonth[ruleMonth];
                    if (ruleMonth == Calendar.FEBRUARY && isLeapYear(year)) {
                        ruleDay++;
                    }
                }
            }

            if (month == ruleMonth) {
                if (ruleDay < day || ruleDay == day && time >= ruleTime) {
                    return rawOffset;
                }
            } else if (nextMonth != ruleMonth) {
                return rawOffset;
            }
        }
        return rawOffset + dstSavings;
    }

    @Override
    public int getOffset(long time) {
        // Simplified variant of the ICU4J code.
        if (!useDaylightTime()) {
            return rawOffset;
        }
        int[] fields = Grego.timeToFields(time + rawOffset, null);
        return getOffset(GregorianCalendar.AD, fields[0], fields[1], fields[2],
                fields[3], fields[5]);
    }

    @Override
    public int getRawOffset() {
        return rawOffset;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * return the same value for this method.
     *
     * @return the receiver's hash.
     * @see #equals
     */
    @Override
    public synchronized int hashCode() {
        int hashCode = getID().hashCode() + rawOffset;
        if (useDaylight) {
            hashCode += startYear + startMonth + startDay + startDayOfWeek
                    + startTime + startMode + endMonth + endDay + endDayOfWeek
                    + endTime + endMode + dstSavings;
        }
        return hashCode;
    }

    @Override
    public boolean hasSameRules(TimeZone zone) {
        if (!(zone instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone tz = (SimpleTimeZone) zone;
        if (useDaylight != tz.useDaylight) {
            return false;
        }
        if (!useDaylight) {
            return rawOffset == tz.rawOffset;
        }
        return rawOffset == tz.rawOffset && dstSavings == tz.dstSavings
                && startYear == tz.startYear && startMonth == tz.startMonth
                && startDay == tz.startDay && startMode == tz.startMode
                && startDayOfWeek == tz.startDayOfWeek
                && startTime == tz.startTime && endMonth == tz.endMonth
                && endDay == tz.endDay && endDayOfWeek == tz.endDayOfWeek
                && endTime == tz.endTime && endMode == tz.endMode;
    }

    @Override public boolean inDaylightTime(Date time) {
        return useDaylightTime() && getOffset(time.getTime()) != getRawOffset();
    }

    private boolean isLeapYear(int year) {
        if (year > 1582) {
            return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
        }
        return year % 4 == 0;
    }

    private int mod7(int num1) {
        int rem = num1 % 7;
        return (num1 < 0 && rem < 0) ? 7 + rem : rem;
    }

    /**
     * Sets the daylight savings offset in milliseconds for this {@code SimpleTimeZone}.
     *
     * @param milliseconds
     *            the daylight savings offset in milliseconds.
     */
    public void setDSTSavings(int milliseconds) {
        if (milliseconds > 0) {
            dstSavings = milliseconds;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void checkRange(int month, int dayOfWeek, int time) {
        if (month < Calendar.JANUARY || month > Calendar.DECEMBER) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        if (dayOfWeek < Calendar.SUNDAY || dayOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Invalid day of week: " + dayOfWeek);
        }
        if (time < 0 || time >= 24 * 3600000) {
            throw new IllegalArgumentException("Invalid time: " + time);
        }
    }

    private void checkDay(int month, int day) {
        if (day <= 0 || day > GregorianCalendar.DaysInMonth[month]) {
            throw new IllegalArgumentException("Invalid day of month: " + day);
        }
    }

    private void setEndMode() {
        if (endDayOfWeek == 0) {
            endMode = DOM_MODE;
        } else if (endDayOfWeek < 0) {
            endDayOfWeek = -endDayOfWeek;
            if (endDay < 0) {
                endDay = -endDay;
                endMode = DOW_LE_DOM_MODE;
            } else {
                endMode = DOW_GE_DOM_MODE;
            }
        } else {
            endMode = DOW_IN_MONTH_MODE;
        }
        useDaylight = startDay != 0 && endDay != 0;
        if (endDay != 0) {
            checkRange(endMonth, endMode == DOM_MODE ? 1 : endDayOfWeek,
                    endTime);
            if (endMode != DOW_IN_MONTH_MODE) {
                checkDay(endMonth, endDay);
            } else {
                if (endDay < -5 || endDay > 5) {
                    throw new IllegalArgumentException("Day of week in month: " + endDay);
                }
            }
        }
        if (endMode != DOM_MODE) {
            endDayOfWeek--;
        }
    }

    /**
     * Sets the rule which specifies the end of daylight savings time.
     *
     * @param month
     *            the {@code Calendar} month in which daylight savings time ends.
     * @param dayOfMonth
     *            the {@code Calendar} day of the month on which daylight savings time
     *            ends.
     * @param time
     *            the time of day in milliseconds standard time on which
     *            daylight savings time ends.
     */
    public void setEndRule(int month, int dayOfMonth, int time) {
        endMonth = month;
        endDay = dayOfMonth;
        endDayOfWeek = 0; // Initialize this value for hasSameRules()
        endTime = time;
        setEndMode();
    }

    /**
     * Sets the rule which specifies the end of daylight savings time.
     *
     * @param month
     *            the {@code Calendar} month in which daylight savings time ends.
     * @param day
     *            the occurrence of the day of the week on which daylight
     *            savings time ends.
     * @param dayOfWeek
     *            the {@code Calendar} day of the week on which daylight savings time
     *            ends.
     * @param time
     *            the time of day in milliseconds standard time on which
     *            daylight savings time ends.
     */
    public void setEndRule(int month, int day, int dayOfWeek, int time) {
        endMonth = month;
        endDay = day;
        endDayOfWeek = dayOfWeek;
        endTime = time;
        setEndMode();
    }

    /**
     * Sets the rule which specifies the end of daylight savings time.
     *
     * @param month
     *            the {@code Calendar} month in which daylight savings time ends.
     * @param day
     *            the {@code Calendar} day of the month.
     * @param dayOfWeek
     *            the {@code Calendar} day of the week on which daylight savings time
     *            ends.
     * @param time
     *            the time of day in milliseconds on which daylight savings time
     *            ends.
     * @param after
     *            selects the day after or before the day of month.
     */
    public void setEndRule(int month, int day, int dayOfWeek, int time, boolean after) {
        endMonth = month;
        endDay = after ? day : -day;
        endDayOfWeek = -dayOfWeek;
        endTime = time;
        setEndMode();
    }

    /**
     * Sets the offset for standard time from GMT for this {@code SimpleTimeZone}.
     *
     * @param offset
     *            the offset from GMT of standard time in milliseconds.
     */
    @Override
    public void setRawOffset(int offset) {
        rawOffset = offset;
    }

    private void setStartMode() {
        if (startDayOfWeek == 0) {
            startMode = DOM_MODE;
        } else if (startDayOfWeek < 0) {
            startDayOfWeek = -startDayOfWeek;
            if (startDay < 0) {
                startDay = -startDay;
                startMode = DOW_LE_DOM_MODE;
            } else {
                startMode = DOW_GE_DOM_MODE;
            }
        } else {
            startMode = DOW_IN_MONTH_MODE;
        }
        useDaylight = startDay != 0 && endDay != 0;
        if (startDay != 0) {
            checkRange(startMonth, startMode == DOM_MODE ? 1 : startDayOfWeek,
                    startTime);
            if (startMode != DOW_IN_MONTH_MODE) {
                checkDay(startMonth, startDay);
            } else {
                if (startDay < -5 || startDay > 5) {
                    throw new IllegalArgumentException("Day of week in month: " + startDay);
                }
            }
        }
        if (startMode != DOM_MODE) {
            startDayOfWeek--;
        }
    }

    /**
     * Sets the rule which specifies the start of daylight savings time.
     *
     * @param month
     *            the {@code Calendar} month in which daylight savings time starts.
     * @param dayOfMonth
     *            the {@code Calendar} day of the month on which daylight savings time
     *            starts.
     * @param time
     *            the time of day in milliseconds on which daylight savings time
     *            starts.
     */
    public void setStartRule(int month, int dayOfMonth, int time) {
        startMonth = month;
        startDay = dayOfMonth;
        startDayOfWeek = 0; // Initialize this value for hasSameRules()
        startTime = time;
        setStartMode();
    }

    /**
     * Sets the rule which specifies the start of daylight savings time.
     *
     * @param month
     *            the {@code Calendar} month in which daylight savings time starts.
     * @param day
     *            the occurrence of the day of the week on which daylight
     *            savings time starts.
     * @param dayOfWeek
     *            the {@code Calendar} day of the week on which daylight savings time
     *            starts.
     * @param time
     *            the time of day in milliseconds on which daylight savings time
     *            starts.
     */
    public void setStartRule(int month, int day, int dayOfWeek, int time) {
        startMonth = month;
        startDay = day;
        startDayOfWeek = dayOfWeek;
        startTime = time;
        setStartMode();
    }

    /**
     * Sets the rule which specifies the start of daylight savings time.
     *
     * @param month
     *            the {@code Calendar} month in which daylight savings time starts.
     * @param day
     *            the {@code Calendar} day of the month.
     * @param dayOfWeek
     *            the {@code Calendar} day of the week on which daylight savings time
     *            starts.
     * @param time
     *            the time of day in milliseconds on which daylight savings time
     *            starts.
     * @param after
     *            selects the day after or before the day of month.
     */
    public void setStartRule(int month, int day, int dayOfWeek, int time, boolean after) {
        startMonth = month;
        startDay = after ? day : -day;
        startDayOfWeek = -dayOfWeek;
        startTime = time;
        setStartMode();
    }

    /**
     * Sets the starting year for daylight savings time in this {@code SimpleTimeZone}.
     * Years before this start year will always be in standard time.
     *
     * @param year
     *            the starting year.
     */
    public void setStartYear(int year) {
        startYear = year;
        useDaylight = true;
    }

    /**
     * Returns the string representation of this {@code SimpleTimeZone}.
     *
     * @return the string representation of this {@code SimpleTimeZone}.
     */
    @Override
    public String toString() {
        return getClass().getName()
                + "[id="
                + getID()
                + ",offset="
                + rawOffset
                + ",dstSavings="
                + dstSavings
                + ",useDaylight="
                + useDaylight
                + ",startYear="
                + startYear
                + ",startMode="
                + startMode
                + ",startMonth="
                + startMonth
                + ",startDay="
                + startDay
                + ",startDayOfWeek="
                + (useDaylight && (startMode != DOM_MODE) ? startDayOfWeek + 1
                        : 0) + ",startTime=" + startTime + ",endMode="
                + endMode + ",endMonth=" + endMonth + ",endDay=" + endDay
                + ",endDayOfWeek="
                + (useDaylight && (endMode != DOM_MODE) ? endDayOfWeek + 1 : 0)
                + ",endTime=" + endTime + "]";
    }

    @Override
    public boolean useDaylightTime() {
        return useDaylight;
    }

/*
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("dstSavings", int.class),
        new ObjectStreamField("endDay", int.class),
        new ObjectStreamField("endDayOfWeek", int.class),
        new ObjectStreamField("endMode", int.class),
        new ObjectStreamField("endMonth", int.class),
        new ObjectStreamField("endTime", int.class),
        new ObjectStreamField("monthLength", byte[].class),
        new ObjectStreamField("rawOffset", int.class),
        new ObjectStreamField("serialVersionOnStream", int.class),
        new ObjectStreamField("startDay", int.class),
        new ObjectStreamField("startDayOfWeek", int.class),
        new ObjectStreamField("startMode", int.class),
        new ObjectStreamField("startMonth", int.class),
        new ObjectStreamField("startTime", int.class),
        new ObjectStreamField("startYear", int.class),
        new ObjectStreamField("useDaylight", boolean.class),
    };

    private void writeObject(ObjectOutputStream stream) throws IOException {
        int sEndDay = endDay, sEndDayOfWeek = endDayOfWeek + 1, sStartDay = startDay, sStartDayOfWeek = startDayOfWeek + 1;
        if (useDaylight
                && (startMode != DOW_IN_MONTH_MODE || endMode != DOW_IN_MONTH_MODE)) {
            Calendar cal = new GregorianCalendar(this);
            if (endMode != DOW_IN_MONTH_MODE) {
                cal.set(Calendar.MONTH, endMonth);
                cal.set(Calendar.DATE, endDay);
                sEndDay = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                if (endMode == DOM_MODE) {
                    sEndDayOfWeek = cal.getFirstDayOfWeek();
                }
            }
            if (startMode != DOW_IN_MONTH_MODE) {
                cal.set(Calendar.MONTH, startMonth);
                cal.set(Calendar.DATE, startDay);
                sStartDay = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                if (startMode == DOM_MODE) {
                    sStartDayOfWeek = cal.getFirstDayOfWeek();
                }
            }
        }
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("dstSavings", dstSavings);
        fields.put("endDay", sEndDay);
        fields.put("endDayOfWeek", sEndDayOfWeek);
        fields.put("endMode", endMode);
        fields.put("endMonth", endMonth);
        fields.put("endTime", endTime);
        fields.put("monthLength", GregorianCalendar.DaysInMonth);
        fields.put("rawOffset", rawOffset);
        fields.put("serialVersionOnStream", 1);
        fields.put("startDay", sStartDay);
        fields.put("startDayOfWeek", sStartDayOfWeek);
        fields.put("startMode", startMode);
        fields.put("startMonth", startMonth);
        fields.put("startTime", startTime);
        fields.put("startYear", startYear);
        fields.put("useDaylight", useDaylight);
        stream.writeFields();
        stream.writeInt(4);
        byte[] values = new byte[4];
        values[0] = (byte) startDay;
        values[1] = (byte) (startMode == DOM_MODE ? 0 : startDayOfWeek + 1);
        values[2] = (byte) endDay;
        values[3] = (byte) (endMode == DOM_MODE ? 0 : endDayOfWeek + 1);
        stream.write(values);
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        rawOffset = fields.get("rawOffset", 0);
        useDaylight = fields.get("useDaylight", false);
        if (useDaylight) {
            endMonth = fields.get("endMonth", 0);
            endTime = fields.get("endTime", 0);
            startMonth = fields.get("startMonth", 0);
            startTime = fields.get("startTime", 0);
            startYear = fields.get("startYear", 0);
        }
        if (fields.get("serialVersionOnStream", 0) == 0) {
            if (useDaylight) {
                startMode = endMode = DOW_IN_MONTH_MODE;
                endDay = fields.get("endDay", 0);
                endDayOfWeek = fields.get("endDayOfWeek", 0) - 1;
                startDay = fields.get("startDay", 0);
                startDayOfWeek = fields.get("startDayOfWeek", 0) - 1;
            }
        } else {
            dstSavings = fields.get("dstSavings", 0);
            if (useDaylight) {
                endMode = fields.get("endMode", 0);
                startMode = fields.get("startMode", 0);
                int length = stream.readInt();
                byte[] values = new byte[length];
                stream.readFully(values);
                if (length >= 4) {
                    startDay = values[0];
                    startDayOfWeek = values[1];
                    if (startMode != DOM_MODE) {
                        startDayOfWeek--;
                    }
                    endDay = values[2];
                    endDayOfWeek = values[3];
                    if (endMode != DOM_MODE) {
                        endDayOfWeek--;
                    }
                }
            }
        }
    }
*/
}
