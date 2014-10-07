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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * {@code GregorianCalendar} is a concrete subclass of {@link Calendar}
 * and provides the standard calendar used by most of the world.
 *
 * <p>
 * The standard (Gregorian) calendar has 2 eras, BC and AD.
 *
 * <p>
 * This implementation handles a single discontinuity, which corresponds by
 * default to the date the Gregorian calendar was instituted (October 15, 1582
 * in some countries, later in others). The cutover date may be changed by the
 * caller by calling {@code setGregorianChange()}.
 *
 * <p>
 * Historically, in those countries which adopted the Gregorian calendar first,
 * October 4, 1582 was thus followed by October 15, 1582. This calendar models
 * this correctly. Before the Gregorian cutover, {@code GregorianCalendar}
 * implements the Julian calendar. The only difference between the Gregorian and
 * the Julian calendar is the leap year rule. The Julian calendar specifies leap
 * years every four years, whereas the Gregorian calendar omits century years
 * which are not divisible by 400.
 *
 * <p>
 * {@code GregorianCalendar} implements <em>proleptic</em> Gregorian
 * and Julian calendars. That is, dates are computed by extrapolating the
 * current rules indefinitely far backward and forward in time. As a result,
 * {@code GregorianCalendar} may be used for all years to generate
 * meaningful and consistent results. However, dates obtained using
 * {@code GregorianCalendar} are historically accurate only from March 1,
 * 4 AD onward, when modern Julian calendar rules were adopted. Before this
 * date, leap year rules were applied irregularly, and before 45 BC the Julian
 * calendar did not even exist.
 *
 * <p>
 * Prior to the institution of the Gregorian calendar, New Year's Day was March
 * 25. To avoid confusion, this calendar always uses January 1. A manual
 * adjustment may be made if desired for dates that are prior to the Gregorian
 * changeover and which fall between January 1 and March 24.
 *
 * <p>
 * Values calculated for the {@code WEEK_OF_YEAR} field range from 1 to
 * 53. Week 1 for a year is the earliest seven day period starting on
 * {@code getFirstDayOfWeek()} that contains at least
 * {@code getMinimalDaysInFirstWeek()} days from that year. It thus
 * depends on the values of {@code getMinimalDaysInFirstWeek()},
 * {@code getFirstDayOfWeek()}, and the day of the week of January 1.
 * Weeks between week 1 of one year and week 1 of the following year are
 * numbered sequentially from 2 to 52 or 53 (as needed).
 *
 * <p>
 * For example, January 1, 1998 was a Thursday. If
 * {@code getFirstDayOfWeek()} is {@code MONDAY} and
 * {@code getMinimalDaysInFirstWeek()} is 4 (these are the values
 * reflecting ISO 8601 and many national standards), then week 1 of 1998 starts
 * on December 29, 1997, and ends on January 4, 1998. If, however,
 * {@code getFirstDayOfWeek()} is {@code SUNDAY}, then week 1 of
 * 1998 starts on January 4, 1998, and ends on January 10, 1998; the first three
 * days of 1998 then are part of week 53 of 1997.
 *
 * <p>
 * Values calculated for the {@code WEEK_OF_MONTH} field range from 0 or
 * 1 to 4 or 5. Week 1 of a month (the days with <code>WEEK_OF_MONTH =
 * 1</code>)
 * is the earliest set of at least {@code getMinimalDaysInFirstWeek()}
 * contiguous days in that month, ending on the day before
 * {@code getFirstDayOfWeek()}. Unlike week 1 of a year, week 1 of a
 * month may be shorter than 7 days, need not start on
 * {@code getFirstDayOfWeek()}, and will not include days of the
 * previous month. Days of a month before week 1 have a
 * {@code WEEK_OF_MONTH} of 0.
 *
 * <p>
 * For example, if {@code getFirstDayOfWeek()} is {@code SUNDAY}
 * and {@code getMinimalDaysInFirstWeek()} is 4, then the first week of
 * January 1998 is Sunday, January 4 through Saturday, January 10. These days
 * have a {@code WEEK_OF_MONTH} of 1. Thursday, January 1 through
 * Saturday, January 3 have a {@code WEEK_OF_MONTH} of 0. If
 * {@code getMinimalDaysInFirstWeek()} is changed to 3, then January 1
 * through January 3 have a {@code WEEK_OF_MONTH} of 1.
 *
 * <p>
 * <strong>Example:</strong> <blockquote>
 *
 * <pre>
 * // get the supported ids for GMT-08:00 (Pacific Standard Time)
 * String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
 * // if no ids were returned, something is wrong. get out.
 * if (ids.length == 0)
 *     System.exit(0);
 *
 *  // begin output
 * System.out.println("Current Time");
 *
 * // create a Pacific Standard Time time zone
 * SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);
 *
 * // set up rules for daylight savings time
 * pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
 * pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
 *
 * // create a GregorianCalendar with the Pacific Daylight time zone
 * // and the current date and time
 * Calendar calendar = new GregorianCalendar(pdt);
 * Date trialTime = new Date();
 * calendar.setTime(trialTime);
 *
 * // print out a bunch of interesting things
 * System.out.println("ERA: " + calendar.get(Calendar.ERA));
 * System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
 * System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
 * System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
 * System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
 * System.out.println("DATE: " + calendar.get(Calendar.DATE));
 * System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
 * System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
 * System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
 * System.out.println("DAY_OF_WEEK_IN_MONTH: "
 *                    + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
 * System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
 * System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
 * System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
 * System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
 * System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
 * System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
 * System.out.println("ZONE_OFFSET: "
 *                    + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000)));
 * System.out.println("DST_OFFSET: "
 *                    + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000)));

 * System.out.println("Current Time, with hour reset to 3");
 * calendar.clear(Calendar.HOUR_OF_DAY); // so doesn't override
 * calendar.set(Calendar.HOUR, 3);
 * System.out.println("ERA: " + calendar.get(Calendar.ERA));
 * System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
 * System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
 * System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
 * System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
 * System.out.println("DATE: " + calendar.get(Calendar.DATE));
 * System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
 * System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
 * System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
 * System.out.println("DAY_OF_WEEK_IN_MONTH: "
 *                    + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
 * System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
 * System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
 * System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
 * System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
 * System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
 * System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
 * System.out.println("ZONE_OFFSET: "
 *        + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000))); // in hours
 * System.out.println("DST_OFFSET: "
 *        + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000))); // in hours
 * </pre>
 *
 * </blockquote>
 *
 * @see Calendar
 * @see TimeZone
 */
public class GregorianCalendar extends Calendar {

    private static final long serialVersionUID = -8125100834729963327L;

    /**
     * Value for the BC era.
     */
    public static final int BC = 0;

    /**
     * Value for the AD era.
     */
    public static final int AD = 1;

    private static final long defaultGregorianCutover = -12219292800000l;

    private long gregorianCutover = defaultGregorianCutover;

    private transient int changeYear = 1582;

    private transient int julianSkew = ((changeYear - 2000) / 400)
            + julianError() - ((changeYear - 2000) / 100);

    static byte[] DaysInMonth = new byte[] { 31, 28, 31, 30, 31, 30, 31, 31,
            30, 31, 30, 31 };

    private static int[] DaysInYear = new int[] { 0, 31, 59, 90, 120, 151, 181,
            212, 243, 273, 304, 334 };

    private static int[] maximums = new int[] { 1, 292278994, 11, 53, 6, 31,
            366, 7, 6, 1, 11, 23, 59, 59, 999, 14 * 3600 * 1000, 7200000 };

    private static int[] minimums = new int[] { 0, 1, 0, 1, 0, 1, 1, 1, 1, 0,
            0, 0, 0, 0, 0, -13 * 3600 * 1000, 0 };

    private static int[] leastMaximums = new int[] { 1, 292269054, 11, 50, 3,
            28, 355, 7, 3, 1, 11, 23, 59, 59, 999, 50400000, 1200000 };

    private int currentYearSkew = 10;

    private int lastYearSkew = 0;

    /**
     * Constructs a new {@code GregorianCalendar} initialized to the current date and
     * time with the default {@code Locale} and {@code TimeZone}.
     */
    public GregorianCalendar() {
        this(TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Constructs a new {@code GregorianCalendar} initialized to midnight in the default
     * {@code TimeZone} and {@code Locale} on the specified date.
     *
     * @param year
     *            the year.
     * @param month
     *            the month.
     * @param day
     *            the day of the month.
     */
    public GregorianCalendar(int year, int month, int day) {
        super(TimeZone.getDefault(), Locale.getDefault());
        set(year, month, day);
    }

    /**
     * Constructs a new {@code GregorianCalendar} initialized to the specified date and
     * time in the default {@code TimeZone} and {@code Locale}.
     *
     * @param year
     *            the year.
     * @param month
     *            the month.
     * @param day
     *            the day of the month.
     * @param hour
     *            the hour.
     * @param minute
     *            the minute.
     */
    public GregorianCalendar(int year, int month, int day, int hour, int minute) {
        super(TimeZone.getDefault(), Locale.getDefault());
        set(year, month, day, hour, minute);
    }

    /**
     * Constructs a new {@code GregorianCalendar} initialized to the specified date and
     * time in the default {@code TimeZone} and {@code Locale}.
     *
     * @param year
     *            the year.
     * @param month
     *            the month.
     * @param day
     *            the day of the month.
     * @param hour
     *            the hour.
     * @param minute
     *            the minute.
     * @param second
     *            the second.
     */
    public GregorianCalendar(int year, int month, int day, int hour,
            int minute, int second) {
        super(TimeZone.getDefault(), Locale.getDefault());
        set(year, month, day, hour, minute, second);
    }

    GregorianCalendar(long milliseconds) {
        this(false);
        setTimeInMillis(milliseconds);
    }

    /**
     * Constructs a new {@code GregorianCalendar} initialized to the current date and
     * time and using the specified {@code Locale} and the default {@code TimeZone}.
     *
     * @param locale
     *            the {@code Locale}.
     */
    public GregorianCalendar(Locale locale) {
        this(TimeZone.getDefault(), locale);
    }

    /**
     * Constructs a new {@code GregorianCalendar} initialized to the current date and
     * time and using the specified {@code TimeZone} and the default {@code Locale}.
     *
     * @param timezone
     *            the {@code TimeZone}.
     */
    public GregorianCalendar(TimeZone timezone) {
        this(timezone, Locale.getDefault());
    }

    /**
     * Constructs a new {@code GregorianCalendar} initialized to the current date and
     * time and using the specified {@code TimeZone} and {@code Locale}.
     *
     * @param timezone
     *            the {@code TimeZone}.
     * @param locale
     *            the {@code Locale}.
     */
    public GregorianCalendar(TimeZone timezone, Locale locale) {
        super(timezone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    GregorianCalendar(boolean ignored) {
        super(TimeZone.getDefault());
        setFirstDayOfWeek(SUNDAY);
        setMinimalDaysInFirstWeek(1);
    }

    /**
     * Adds the specified amount to a {@code Calendar} field.
     *
     * @param field
     *            the {@code Calendar} field to modify.
     * @param value
     *            the amount to add to the field.
     *
     * @throws IllegalArgumentException
     *                if the specified field is DST_OFFSET or ZONE_OFFSET.
     */
    @Override
    public void add(int field, int value) {
        if (value == 0) {
            return;
        }
        if (field < 0 || field >= ZONE_OFFSET) {
            throw new IllegalArgumentException();
        }

        if (field == ERA) {
            complete();
            if (fields[ERA] == AD) {
                if (value >= 0) {
                    return;
                }
                set(ERA, BC);
            } else {
                if (value <= 0) {
                    return;
                }
                set(ERA, AD);
            }
            complete();
            return;
        }

        if (field == YEAR || field == MONTH) {
            complete();
            if (field == MONTH) {
                int month = fields[MONTH] + value;
                if (month < 0) {
                    value = (month - 11) / 12;
                    month = 12 + (month % 12);
                } else {
                    value = month / 12;
                }
                set(MONTH, month % 12);
            }
            set(YEAR, fields[YEAR] + value);
            int days = daysInMonth(isLeapYear(fields[YEAR]), fields[MONTH]);
            if (fields[DATE] > days) {
                set(DATE, days);
            }
            complete();
            return;
        }

        long multiplier = 0;
        getTimeInMillis(); // Update the time
        switch (field) {
            case MILLISECOND:
                time += value;
                break;
            case SECOND:
                time += value * 1000L;
                break;
            case MINUTE:
                time += value * 60000L;
                break;
            case HOUR:
            case HOUR_OF_DAY:
                time += value * 3600000L;
                break;
            case AM_PM:
                multiplier = 43200000L;
                break;
            case DATE:
            case DAY_OF_YEAR:
            case DAY_OF_WEEK:
                multiplier = 86400000L;
                break;
            case WEEK_OF_YEAR:
            case WEEK_OF_MONTH:
            case DAY_OF_WEEK_IN_MONTH:
                multiplier = 604800000L;
                break;
        }

        if (multiplier == 0) {
            areFieldsSet = false;
            complete();
            return;
        }

        long delta = value * multiplier;

        /*
         * Attempt to keep the hour and minute constant when we've crossed a DST
         * boundary and the user's units are AM_PM or larger. The typical
         * consequence is that calls to add(DATE, 1) will add 23, 24 or 25 hours
         * depending on whether the DST goes forward, constant, or backward.
         *
         * We know we've crossed a DST boundary if the new time will have a
         * different timezone offset. Adjust by adding the difference of the two
         * offsets. We don't adjust when doing so prevents the change from
         * crossing the boundary.
         */
        int zoneOffset = getTimeZone().getRawOffset();
        int offsetBefore = getOffset(time + zoneOffset);
        int offsetAfter = getOffset(time + zoneOffset + delta);
        int dstDelta = offsetBefore - offsetAfter;
        if (getOffset(time + zoneOffset + delta + dstDelta) == offsetAfter) {
            delta += dstDelta;
        }

        time += delta;
        areFieldsSet = false;
        complete();
    }

    private void fullFieldsCalc(long timeVal, int zoneOffset) {
        int millis = (int) (time % 86400000);
        long days = timeVal / 86400000;

        if (millis < 0) {
            millis += 86400000;
            days--;
        }
        // Cannot add ZONE_OFFSET to time as it might overflow
        millis += zoneOffset;
        while (millis < 0) {
            millis += 86400000;
            days--;
        }
        while (millis >= 86400000) {
            millis -= 86400000;
            days++;
        }

        int dayOfYear = computeYearAndDay(days, timeVal + zoneOffset);
        fields[DAY_OF_YEAR] = dayOfYear;
        if(fields[YEAR] == changeYear && gregorianCutover <= timeVal + zoneOffset){
            dayOfYear += currentYearSkew;
        }
        int month = dayOfYear / 32;
        boolean leapYear = isLeapYear(fields[YEAR]);
        int date = dayOfYear - daysInYear(leapYear, month);
        if (date > daysInMonth(leapYear, month)) {
            date -= daysInMonth(leapYear, month);
            month++;
        }
        fields[DAY_OF_WEEK] = mod7(days - 3) + 1;
        int dstOffset = fields[YEAR] <= 0 ? 0 : getTimeZone().getOffset(AD,
                fields[YEAR], month, date, fields[DAY_OF_WEEK], millis);
        if (fields[YEAR] > 0) {
            dstOffset -= zoneOffset;
        }
        fields[DST_OFFSET] = dstOffset;
        if (dstOffset != 0) {
            long oldDays = days;
            millis += dstOffset;
            if (millis < 0) {
                millis += 86400000;
                days--;
            } else if (millis >= 86400000) {
                millis -= 86400000;
                days++;
            }
            if (oldDays != days) {
                dayOfYear = computeYearAndDay(days, timeVal - zoneOffset
                        + dstOffset);
                fields[DAY_OF_YEAR] = dayOfYear;
                if(fields[YEAR] == changeYear && gregorianCutover <= timeVal - zoneOffset + dstOffset){
                    dayOfYear += currentYearSkew;
                }
                month = dayOfYear / 32;
                leapYear = isLeapYear(fields[YEAR]);
                date = dayOfYear - daysInYear(leapYear, month);
                if (date > daysInMonth(leapYear, month)) {
                    date -= daysInMonth(leapYear, month);
                    month++;
                }
                fields[DAY_OF_WEEK] = mod7(days - 3) + 1;
            }
        }

        fields[MILLISECOND] = (millis % 1000);
        millis /= 1000;
        fields[SECOND] = (millis % 60);
        millis /= 60;
        fields[MINUTE] = (millis % 60);
        millis /= 60;
        fields[HOUR_OF_DAY] = (millis % 24);
        fields[AM_PM] = fields[HOUR_OF_DAY] > 11 ? 1 : 0;
        fields[HOUR] = fields[HOUR_OF_DAY] % 12;

        if (fields[YEAR] <= 0) {
            fields[ERA] = BC;
            fields[YEAR] = -fields[YEAR] + 1;
        } else {
            fields[ERA] = AD;
        }
        fields[MONTH] = month;
        fields[DATE] = date;
        fields[DAY_OF_WEEK_IN_MONTH] = (date - 1) / 7 + 1;
        fields[WEEK_OF_MONTH] = (date - 1 + mod7(days - date - 2
                - (getFirstDayOfWeek() - 1))) / 7 + 1;
        int daysFromStart = mod7(days - 3 - (fields[DAY_OF_YEAR] - 1)
                - (getFirstDayOfWeek() - 1));
        int week = (fields[DAY_OF_YEAR] - 1 + daysFromStart) / 7
                + (7 - daysFromStart >= getMinimalDaysInFirstWeek() ? 1 : 0);
        if (week == 0) {
            fields[WEEK_OF_YEAR] = 7 - mod7(daysFromStart
                    - (isLeapYear(fields[YEAR] - 1) ? 2 : 1)) >= getMinimalDaysInFirstWeek() ? 53
                    : 52;
        } else if (fields[DAY_OF_YEAR] >= (leapYear ? 367 : 366)
                - mod7(daysFromStart + (leapYear ? 2 : 1))) {
            fields[WEEK_OF_YEAR] = 7 - mod7(daysFromStart + (leapYear ? 2 : 1)) >= getMinimalDaysInFirstWeek() ? 1
                    : week;
        } else {
            fields[WEEK_OF_YEAR] = week;
        }
    }

    @Override
    protected void computeFields() {
        TimeZone timeZone = getTimeZone();
        int dstOffset = timeZone.inDaylightTime(new Date(time)) ? timeZone.getDSTSavings() : 0;
        int zoneOffset = timeZone.getRawOffset();
        fields[DST_OFFSET] = dstOffset;
        fields[ZONE_OFFSET] = zoneOffset;

        fullFieldsCalc(time, zoneOffset);

        for (int i = 0; i < FIELD_COUNT; i++) {
            isSet[i] = true;
        }
    }

    @Override
    protected void computeTime() {
        if (!isLenient()) {
            if (isSet[HOUR_OF_DAY]) {
                if (fields[HOUR_OF_DAY] < 0 || fields[HOUR_OF_DAY] > 23) {
                    throw new IllegalArgumentException();
                }
            } else if (isSet[HOUR] && (fields[HOUR] < 0 || fields[HOUR] > 11)) {
                throw new IllegalArgumentException();
            }
            if (isSet[MINUTE] && (fields[MINUTE] < 0 || fields[MINUTE] > 59)) {
                throw new IllegalArgumentException();
            }
            if (isSet[SECOND] && (fields[SECOND] < 0 || fields[SECOND] > 59)) {
                throw new IllegalArgumentException();
            }
            if (isSet[MILLISECOND]
                    && (fields[MILLISECOND] < 0 || fields[MILLISECOND] > 999)) {
                throw new IllegalArgumentException();
            }
            if (isSet[WEEK_OF_YEAR]
                    && (fields[WEEK_OF_YEAR] < 1 || fields[WEEK_OF_YEAR] > 53)) {
                throw new IllegalArgumentException();
            }
            if (isSet[DAY_OF_WEEK]
                    && (fields[DAY_OF_WEEK] < 1 || fields[DAY_OF_WEEK] > 7)) {
                throw new IllegalArgumentException();
            }
            if (isSet[DAY_OF_WEEK_IN_MONTH]
                    && (fields[DAY_OF_WEEK_IN_MONTH] < 1 || fields[DAY_OF_WEEK_IN_MONTH] > 6)) {
                throw new IllegalArgumentException();
            }
            if (isSet[WEEK_OF_MONTH]
                    && (fields[WEEK_OF_MONTH] < 1 || fields[WEEK_OF_MONTH] > 6)) {
                throw new IllegalArgumentException();
            }
            if (isSet[AM_PM] && fields[AM_PM] != AM && fields[AM_PM] != PM) {
                throw new IllegalArgumentException();
            }
            if (isSet[HOUR] && (fields[HOUR] < 0 || fields[HOUR] > 11)) {
                throw new IllegalArgumentException();
            }
            if (isSet[YEAR]) {
                if (isSet[ERA] && fields[ERA] == BC
                        && (fields[YEAR] < 1 || fields[YEAR] > 292269054)) {
                    throw new IllegalArgumentException();
                } else if (fields[YEAR] < 1 || fields[YEAR] > 292278994) {
                    throw new IllegalArgumentException();
                }
            }
            if (isSet[MONTH] && (fields[MONTH] < 0 || fields[MONTH] > 11)) {
                throw new IllegalArgumentException();
            }
        }

        long timeVal;
        long hour = 0;
        if (isSet[HOUR_OF_DAY] && lastTimeFieldSet != HOUR) {
            hour = fields[HOUR_OF_DAY];
        } else if (isSet[HOUR]) {
            hour = (fields[AM_PM] * 12) + fields[HOUR];
        }
        timeVal = hour * 3600000;

        if (isSet[MINUTE]) {
            timeVal += ((long) fields[MINUTE]) * 60000;
        }
        if (isSet[SECOND]) {
            timeVal += ((long) fields[SECOND]) * 1000;
        }
        if (isSet[MILLISECOND]) {
            timeVal += fields[MILLISECOND];
        }

        long days;
        int year = isSet[YEAR] ? fields[YEAR] : 1970;
        if (isSet[ERA]) {
            // Always test for valid ERA, even if the Calendar is lenient
            if (fields[ERA] != BC && fields[ERA] != AD) {
                throw new IllegalArgumentException();
            }
            if (fields[ERA] == BC) {
                year = 1 - year;
            }
        }

        boolean weekMonthSet = isSet[WEEK_OF_MONTH]
                || isSet[DAY_OF_WEEK_IN_MONTH];
        boolean useMonth = (isSet[DATE] || isSet[MONTH] || weekMonthSet)
                && lastDateFieldSet != DAY_OF_YEAR;
        if (useMonth
                && (lastDateFieldSet == DAY_OF_WEEK || lastDateFieldSet == WEEK_OF_YEAR)) {
            if (isSet[WEEK_OF_YEAR] && isSet[DAY_OF_WEEK]) {
                useMonth = lastDateFieldSet != WEEK_OF_YEAR && weekMonthSet
                        && isSet[DAY_OF_WEEK];
            } else if (isSet[DAY_OF_YEAR]) {
                useMonth = isSet[DATE] && isSet[MONTH];
            }
        }

        if (useMonth) {
            int month = fields[MONTH];
            year += month / 12;
            month %= 12;
            if (month < 0) {
                year--;
                month += 12;
            }
            boolean leapYear = isLeapYear(year);
            days = daysFromBaseYear(year) + daysInYear(leapYear, month);
            boolean useDate = isSet[DATE];
            if (useDate
                    && (lastDateFieldSet == DAY_OF_WEEK
                            || lastDateFieldSet == WEEK_OF_MONTH || lastDateFieldSet == DAY_OF_WEEK_IN_MONTH)) {
                useDate = !(isSet[DAY_OF_WEEK] && weekMonthSet);
            }
            if (useDate) {
                if (!isLenient()
                        && (fields[DATE] < 1 || fields[DATE] > daysInMonth(
                                leapYear, month))) {
                    throw new IllegalArgumentException();
                }
                days += fields[DATE] - 1;
            } else {
                int dayOfWeek;
                if (isSet[DAY_OF_WEEK]) {
                    dayOfWeek = fields[DAY_OF_WEEK] - 1;
                } else {
                    dayOfWeek = getFirstDayOfWeek() - 1;
                }
                if (isSet[WEEK_OF_MONTH]
                        && lastDateFieldSet != DAY_OF_WEEK_IN_MONTH) {
                    int skew = mod7(days - 3 - (getFirstDayOfWeek() - 1));
                    days += (fields[WEEK_OF_MONTH] - 1) * 7
                            + mod7(skew + dayOfWeek - (days - 3)) - skew;
                } else if (isSet[DAY_OF_WEEK_IN_MONTH]) {
                    if (fields[DAY_OF_WEEK_IN_MONTH] >= 0) {
                        days += mod7(dayOfWeek - (days - 3))
                                + (fields[DAY_OF_WEEK_IN_MONTH] - 1) * 7;
                    } else {
                        days += daysInMonth(leapYear, month)
                                + mod7(dayOfWeek
                                        - (days + daysInMonth(leapYear, month) - 3))
                                + fields[DAY_OF_WEEK_IN_MONTH] * 7;
                    }
                } else if (isSet[DAY_OF_WEEK]) {
                    int skew = mod7(days - 3 - (getFirstDayOfWeek() - 1));
                    days += mod7(mod7(skew + dayOfWeek - (days - 3)) - skew);
                }
            }
        } else {
            boolean useWeekYear = isSet[WEEK_OF_YEAR]
                    && lastDateFieldSet != DAY_OF_YEAR;
            if (useWeekYear && isSet[DAY_OF_YEAR]) {
                useWeekYear = isSet[DAY_OF_WEEK];
            }
            days = daysFromBaseYear(year);
            if (useWeekYear) {
                int dayOfWeek;
                if (isSet[DAY_OF_WEEK]) {
                    dayOfWeek = fields[DAY_OF_WEEK] - 1;
                } else {
                    dayOfWeek = getFirstDayOfWeek() - 1;
                }
                int skew = mod7(days - 3 - (getFirstDayOfWeek() - 1));
                days += (fields[WEEK_OF_YEAR] - 1) * 7
                        + mod7(skew + dayOfWeek - (days - 3)) - skew;
                if (7 - skew < getMinimalDaysInFirstWeek()) {
                    days += 7;
                }
            } else if (isSet[DAY_OF_YEAR]) {
                if (!isLenient()
                        && (fields[DAY_OF_YEAR] < 1 || fields[DAY_OF_YEAR] > (365 + (isLeapYear(year) ? 1
                                : 0)))) {
                    throw new IllegalArgumentException();
                }
                days += fields[DAY_OF_YEAR] - 1;
            } else if (isSet[DAY_OF_WEEK]) {
                days += mod7(fields[DAY_OF_WEEK] - 1 - (days - 3));
            }
        }
        lastDateFieldSet = 0;

        timeVal += days * 86400000;
        // Use local time to compare with the gregorian change
        if (year == changeYear
                && timeVal >= gregorianCutover + julianError() * 86400000L) {
            timeVal -= julianError() * 86400000L;
        }

        // It is not possible to simply subtract getOffset(timeVal) from timeVal
        // to get UTC.
        // The trick is needed for the moment when DST transition occurs,
        // say 1:00 is a transition time when DST offset becomes +1 hour,
        // then wall time in the interval 1:00 - 2:00 is invalid and is
        // treated as UTC time.
        long timeValWithoutDST = timeVal - getOffset(timeVal)
                + getTimeZone().getRawOffset();
        timeVal -= getOffset(timeValWithoutDST);
        // Need to update wall time in fields, since it was invalid due to DST
        // transition
        this.time = timeVal;
        if (timeValWithoutDST != timeVal) {
            computeFields();
            areFieldsSet = true;
        }
    }

    private int computeYearAndDay(long dayCount, long localTime) {
        int year = 1970;
        long days = dayCount;
        if (localTime < gregorianCutover) {
            days -= julianSkew;
        }
        int approxYears;

        while ((approxYears = (int) (days / 365)) != 0) {
            year = year + approxYears;
            days = dayCount - daysFromBaseYear(year);
        }
        if (days < 0) {
            year = year - 1;
            days = days + daysInYear(year);
        }
        fields[YEAR] = year;
        return (int) days + 1;
    }

    private long daysFromBaseYear(long year) {
        if (year >= 1970) {
            long days = (year - 1970) * 365 + ((year - 1969) / 4);
            if (year > changeYear) {
                days -= ((year - 1901) / 100) - ((year - 1601) / 400);
            } else {
                if (year == changeYear) {
                    days += currentYearSkew;
                } else if (year == changeYear - 1) {
                    days += lastYearSkew;
                } else {
                    days += julianSkew;
                }
            }
            return days;
        } else if (year <= changeYear) {
            return (year - 1970) * 365 + ((year - 1972) / 4) + julianSkew;
        }
        return (year - 1970) * 365 + ((year - 1972) / 4)
                - ((year - 2000) / 100) + ((year - 2000) / 400);
    }

    private int daysInMonth() {
        return daysInMonth(isLeapYear(fields[YEAR]), fields[MONTH]);
    }

    private int daysInMonth(boolean leapYear, int month) {
        if (leapYear && month == FEBRUARY) {
            return DaysInMonth[month] + 1;
        }

        return DaysInMonth[month];
    }

    private int daysInYear(int year) {
        int daysInYear = isLeapYear(year) ? 366 : 365;
        if (year == changeYear) {
            daysInYear -= currentYearSkew;
        }
        if (year == changeYear - 1) {
            daysInYear -= lastYearSkew;
        }
        return daysInYear;
    }

    private int daysInYear(boolean leapYear, int month) {
        if (leapYear && month > FEBRUARY) {
            return DaysInYear[month] + 1;
        }

        return DaysInYear[month];
    }

    /**
     * Returns true if {@code object} is a GregorianCalendar with the same
     * properties.
     */
    @Override public boolean equals(Object object) {
        if (!(object instanceof GregorianCalendar)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        return super.equals(object)
                && gregorianCutover == ((GregorianCalendar) object).gregorianCutover;
    }

    @Override public int getActualMaximum(int field) {
        int value;
        if ((value = maximums[field]) == leastMaximums[field]) {
            return value;
        }

        complete();
        long orgTime = time;
        int result = 0;
        switch (field) {
            case WEEK_OF_YEAR:
                set(DATE, 31);
                set(MONTH, DECEMBER);
                result = get(WEEK_OF_YEAR);
                if (result == 1) {
                    set(DATE, 31 - 7);
                    result = get(WEEK_OF_YEAR);
                }
                areFieldsSet = false;
                break;
            case WEEK_OF_MONTH:
                set(DATE, daysInMonth());
                result = get(WEEK_OF_MONTH);
                areFieldsSet = false;
                break;
            case DATE:
                return daysInMonth();
            case DAY_OF_YEAR:
                return daysInYear(fields[YEAR]);
            case DAY_OF_WEEK_IN_MONTH:
                result = get(DAY_OF_WEEK_IN_MONTH)
                        + ((daysInMonth() - get(DATE)) / 7);
                break;
            case YEAR:
                GregorianCalendar clone = (GregorianCalendar) clone();
                if (get(ERA) == AD) {
                    clone.setTimeInMillis(Long.MAX_VALUE);
                } else {
                    clone.setTimeInMillis(Long.MIN_VALUE);
                }
                result = clone.get(YEAR);
                clone.set(YEAR, get(YEAR));
                if (clone.before(this)) {
                    result--;
                }
                break;
            case DST_OFFSET:
                result = getMaximum(DST_OFFSET);
                break;
        }
        time = orgTime;
        return result;
    }

    /**
     * Gets the minimum value of the specified field for the current date. For
     * the gregorian calendar, this value is the same as
     * {@code getMinimum()}.
     *
     * @param field
     *            the field.
     * @return the minimum value of the specified field.
     */
    @Override
    public int getActualMinimum(int field) {
        return getMinimum(field);
    }

    /**
     * Gets the greatest minimum value of the specified field. For the gregorian
     * calendar, this value is the same as {@code getMinimum()}.
     *
     * @param field
     *            the field.
     * @return the greatest minimum value of the specified field.
     */
    @Override
    public int getGreatestMinimum(int field) {
        return minimums[field];
    }

    /**
     * Returns the gregorian change date of this calendar. This is the date on
     * which the gregorian calendar came into effect.
     *
     * @return a {@code Date} which represents the gregorian change date.
     */
    public final Date getGregorianChange() {
        return new Date(gregorianCutover);
    }

    /**
     * Gets the smallest maximum value of the specified field. For example, 28
     * for the day of month field.
     *
     * @param field
     *            the field.
     * @return the smallest maximum value of the specified field.
     */
    @Override
    public int getLeastMaximum(int field) {
        // return value for WEEK_OF_YEAR should make corresponding changes when
        // the gregorian change date have been reset.
        if (gregorianCutover != defaultGregorianCutover
                && field == WEEK_OF_YEAR) {
            long currentTimeInMillis = time;
            setTimeInMillis(gregorianCutover);
            int actual = getActualMaximum(field);
            setTimeInMillis(currentTimeInMillis);
            return actual;
        }
        return leastMaximums[field];
    }

    /**
     * Gets the greatest maximum value of the specified field. For example, 31
     * for the day of month field.
     *
     * @param field
     *            the field.
     * @return the greatest maximum value of the specified field.
     */
    @Override
    public int getMaximum(int field) {
        return maximums[field];
    }

    /**
     * Gets the smallest minimum value of the specified field.
     *
     * @param field
     *            the field.
     * @return the smallest minimum value of the specified field.
     */
    @Override
    public int getMinimum(int field) {
        return minimums[field];
    }

    private int getOffset(long localTime) {
        TimeZone timeZone = getTimeZone();

        long dayCount = localTime / 86400000;
        int millis = (int) (localTime % 86400000);
        if (millis < 0) {
            millis += 86400000;
            dayCount--;
        }

        int year = 1970;
        long days = dayCount;
        if (localTime < gregorianCutover) {
            days -= julianSkew;
        }
        int approxYears;

        while ((approxYears = (int) (days / 365)) != 0) {
            year = year + approxYears;
            days = dayCount - daysFromBaseYear(year);
        }
        if (days < 0) {
            year = year - 1;
            days = days + 365 + (isLeapYear(year) ? 1 : 0);
            if (year == changeYear && localTime < gregorianCutover) {
                days -= julianError();
            }
        }
        if (year <= 0) {
            return timeZone.getRawOffset();
        }
        int dayOfYear = (int) days + 1;

        int month = dayOfYear / 32;
        boolean leapYear = isLeapYear(year);
        int date = dayOfYear - daysInYear(leapYear, month);
        if (date > daysInMonth(leapYear, month)) {
            date -= daysInMonth(leapYear, month);
            month++;
        }
        int dayOfWeek = mod7(dayCount - 3) + 1;
        return timeZone.getOffset(AD, year, month, date, dayOfWeek, millis);
    }

    @Override public int hashCode() {
        return super.hashCode()
                + ((int) (gregorianCutover >>> 32) ^ (int) gregorianCutover);
    }

    /**
     * Returns true if {@code year} is a leap year.
     */
    public boolean isLeapYear(int year) {
        if (year > changeYear) {
            return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
        }

        return year % 4 == 0;
    }

    private int julianError() {
        return changeYear / 100 - changeYear / 400 - 2;
    }

    private int mod(int value, int mod) {
        int rem = value % mod;
        if (value < 0 && rem < 0) {
            return rem + mod;
        }
        return rem;
    }

    private int mod7(long num1) {
        int rem = (int) (num1 % 7);
        if (num1 < 0 && rem < 0) {
            return rem + 7;
        }
        return rem;
    }

    /**
     * Adds the specified amount the specified field and wraps the value of the
     * field when it goes beyond the maximum or minimum value for the current
     * date. Other fields will be adjusted as required to maintain a consistent
     * date.
     *
     * @param field
     *            the field to roll.
     * @param value
     *            the amount to add.
     *
     * @throws IllegalArgumentException
     *                if an invalid field is specified.
     */
    @Override
    public void roll(int field, int value) {
        if (value == 0) {
            return;
        }
        if (field < 0 || field >= ZONE_OFFSET) {
            throw new IllegalArgumentException();
        }

        complete();
        int days, day, mod, maxWeeks, newWeek;
        int max = -1;
        switch (field) {
        case YEAR:
            max = maximums[field];
            break;
        case WEEK_OF_YEAR:
            days = daysInYear(fields[YEAR]);
            day = DAY_OF_YEAR;
            mod = mod7(fields[DAY_OF_WEEK] - fields[day]
                    - (getFirstDayOfWeek() - 1));
            maxWeeks = (days - 1 + mod) / 7 + 1;
            newWeek = mod(fields[field] - 1 + value, maxWeeks) + 1;
            if (newWeek == maxWeeks) {
                int addDays = (newWeek - fields[field]) * 7;
                if (fields[day] > addDays && fields[day] + addDays > days) {
                    set(field, 1);
                } else {
                    set(field, newWeek - 1);
                }
            } else if (newWeek == 1) {
                int week = (fields[day] - ((fields[day] - 1) / 7 * 7) - 1 + mod) / 7 + 1;
                if (week > 1) {
                    set(field, 1);
                } else {
                    set(field, newWeek);
                }
            } else {
                set(field, newWeek);
            }
            break;
        case WEEK_OF_MONTH:
            days = daysInMonth();
            day = DATE;
            mod = mod7(fields[DAY_OF_WEEK] - fields[day]
                    - (getFirstDayOfWeek() - 1));
            maxWeeks = (days - 1 + mod) / 7 + 1;
            newWeek = mod(fields[field] - 1 + value, maxWeeks) + 1;
            if (newWeek == maxWeeks) {
                if (fields[day] + (newWeek - fields[field]) * 7 > days) {
                    set(day, days);
                } else {
                    set(field, newWeek);
                }
            } else if (newWeek == 1) {
                int week = (fields[day] - ((fields[day] - 1) / 7 * 7) - 1 + mod) / 7 + 1;
                if (week > 1) {
                    set(day, 1);
                } else {
                    set(field, newWeek);
                }
            } else {
                set(field, newWeek);
            }
            break;
        case DATE:
            max = daysInMonth();
            break;
        case DAY_OF_YEAR:
            max = daysInYear(fields[YEAR]);
            break;
        case DAY_OF_WEEK:
            max = maximums[field];
            lastDateFieldSet = WEEK_OF_MONTH;
            break;
        case DAY_OF_WEEK_IN_MONTH:
            max = (fields[DATE] + ((daysInMonth() - fields[DATE]) / 7 * 7) - 1) / 7 + 1;
            break;

        case ERA:
        case MONTH:
        case AM_PM:
        case HOUR:
        case HOUR_OF_DAY:
        case MINUTE:
        case SECOND:
        case MILLISECOND:
            set(field, mod(fields[field] + value, maximums[field] + 1));
            if (field == MONTH && fields[DATE] > daysInMonth()) {
                set(DATE, daysInMonth());
            } else if (field == AM_PM) {
                lastTimeFieldSet = HOUR;
            }
            break;
        }
        if (max != -1) {
            set(field, mod(fields[field] - 1 + value, max) + 1);
        }
        complete();
    }

    /**
     * Increments or decrements the specified field and wraps the value of the
     * field when it goes beyond the maximum or minimum value for the current
     * date. Other fields will be adjusted as required to maintain a consistent
     * date. For example, March 31 will roll to April 30 when rolling the month
     * field.
     *
     * @param field
     *            the field to roll.
     * @param increment
     *            {@code true} to increment the field, {@code false} to
     *            decrement.
     * @throws IllegalArgumentException
     *                if an invalid field is specified.
     */
    @Override
    public void roll(int field, boolean increment) {
        roll(field, increment ? 1 : -1);
    }

    /**
     * Sets the gregorian change date of this calendar.
     */
    public void setGregorianChange(Date date) {
        gregorianCutover = date.getTime();
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        changeYear = cal.get(YEAR);
        if (cal.get(ERA) == BC) {
            changeYear = 1 - changeYear;
        }
        julianSkew = ((changeYear - 2000) / 400) + julianError()
                - ((changeYear - 2000) / 100);
        int dayOfYear = cal.get(DAY_OF_YEAR);
        if (dayOfYear < julianSkew) {
            currentYearSkew = dayOfYear-1;
            lastYearSkew = julianSkew - dayOfYear + 1;
        } else {
            lastYearSkew = 0;
            currentYearSkew = julianSkew;
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        setGregorianChange(new Date(gregorianCutover));
    }
}
