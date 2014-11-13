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
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.text.DateFormatSymbols;

import libcore.icu.ICU;
import libcore.icu.LocaleData;

/**
 * {@code Calendar} is an abstract base class for converting between a
 * {@code Date} object and a set of integer fields such as
 * {@code YEAR}, {@code MONTH}, {@code DAY},
 * {@code HOUR}, and so on. (A {@code Date} object represents a
 * specific instant in time with millisecond precision. See {@link Date} for
 * information about the {@code Date} class.)
 *
 * <p>
 * Subclasses of {@code Calendar} interpret a {@code Date}
 * according to the rules of a specific calendar system.
 *
 * <p>
 * Like other locale-sensitive classes, {@code Calendar} provides a class
 * method, {@code getInstance}, for getting a default instance of
 * this class for general use. {@code Calendar}'s {@code getInstance} method
 * returns a calendar whose locale is based on system settings and whose time fields
 * have been initialized with the current date and time: <blockquote>
 *
 * <pre>Calendar rightNow = Calendar.getInstance()</pre>
 *
 * </blockquote>
 *
 * <p>
 * A {@code Calendar} object can produce all the time field values needed
 * to implement the date-time formatting for a particular language and calendar
 * style (for example, Japanese-Gregorian, Japanese-Traditional).
 * {@code Calendar} defines the range of values returned by certain
 * fields, as well as their meaning. For example, the first month of the year
 * has value {@code MONTH} == {@code JANUARY} for all calendars.
 * Other values are defined by the concrete subclass, such as {@code ERA}
 * and {@code YEAR}. See individual field documentation and subclass
 * documentation for details.
 *
 * <p>
 * When a {@code Calendar} is <em>lenient</em>, it accepts a wider
 * range of field values than it produces. For example, a lenient
 * {@code GregorianCalendar} interprets {@code MONTH} ==
 * {@code JANUARY}, {@code DAY_OF_MONTH} == 32 as February 1. A
 * non-lenient {@code GregorianCalendar} throws an exception when given
 * out-of-range field settings. When calendars recompute field values for return
 * by {@code get()}, they normalize them. For example, a
 * {@code GregorianCalendar} always produces {@code DAY_OF_MONTH}
 * values between 1 and the length of the month.
 *
 * <p>
 * {@code Calendar} defines a locale-specific seven day week using two
 * parameters: the first day of the week and the minimal days in first week
 * (from 1 to 7). These numbers are taken from the locale resource data when a
 * {@code Calendar} is constructed. They may also be specified explicitly
 * through the API.
 *
 * <p>
 * When setting or getting the {@code WEEK_OF_MONTH} or
 * {@code WEEK_OF_YEAR} fields, {@code Calendar} must determine
 * the first week of the month or year as a reference point. The first week of a
 * month or year is defined as the earliest seven day period beginning on
 * {@code getFirstDayOfWeek()} and containing at least
 * {@code getMinimalDaysInFirstWeek()} days of that month or year. Weeks
 * numbered ..., -1, 0 precede the first week; weeks numbered 2, 3,... follow
 * it. Note that the normalized numbering returned by {@code get()} may
 * be different. For example, a specific {@code Calendar} subclass may
 * designate the week before week 1 of a year as week <em>n</em> of the
 * previous year.
 *
 * <p>
 * When computing a {@code Date} from time fields, two special
 * circumstances may arise: there may be insufficient information to compute the
 * {@code Date} (such as only year and month but no day in the month), or
 * there may be inconsistent information (such as "Tuesday, July 15, 1996" --
 * July 15, 1996 is actually a Monday).
 *
 * <p>
 * <strong>Insufficient information.</strong> The calendar will use default
 * information to specify the missing fields. This may vary by calendar; for the
 * Gregorian calendar, the default for a field is the same as that of the start
 * of the epoch: i.e., YEAR = 1970, MONTH = JANUARY, DATE = 1, etc.
 *
 * <p>
 * <strong>Inconsistent information.</strong> If fields conflict, the calendar
 * will give preference to fields set more recently. For example, when
 * determining the day, the calendar will look for one of the following
 * combinations of fields. The most recent combination, as determined by the
 * most recently set single field, will be used.
 *
 * <blockquote>
 *
 * <pre>
 * MONTH + DAY_OF_MONTH
 * MONTH + WEEK_OF_MONTH + DAY_OF_WEEK
 * MONTH + DAY_OF_WEEK_IN_MONTH + DAY_OF_WEEK
 * DAY_OF_YEAR
 * DAY_OF_WEEK + WEEK_OF_YEAR</pre>
 *
 * </blockquote>
 *
 * For the time of day:
 *
 * <blockquote>
 *
 * <pre>
 * HOUR_OF_DAY
 * AM_PM + HOUR</pre>
 *
 * </blockquote>
 *
 * <p>
 * <strong>Note:</strong> There are certain possible ambiguities in
 * interpretation of certain singular times, which are resolved in the following
 * ways:
 * <ol>
 * <li> 24:00:00 "belongs" to the following day. That is, 23:59 on Dec 31, 1969
 * &lt; 24:00 on Jan 1, 1970 &lt; 24:01:00 on Jan 1, 1970 form a sequence of
 * three consecutive minutes in time.
 *
 * <li> Although historically not precise, midnight also belongs to "am", and
 * noon belongs to "pm", so on the same day, we have 12:00 am (midnight) &lt; 12:01 am,
 * and 12:00 pm (noon) &lt; 12:01 pm
 * </ol>
 *
 * <p>
 * The date or time format strings are not part of the definition of a calendar,
 * as those must be modifiable or overridable by the user at runtime. Use
 * {@link java.text.DateFormat} to format dates.
 *
 * <p>
 * <strong>Field manipulation methods</strong>
 *
 * <p>
 * {@code Calendar} fields can be changed using three methods:
 * {@code set()}, {@code add()}, and {@code roll()}.
 *
 * <p>
 * <strong>{@code set(f, value)}</strong> changes field {@code f}
 * to {@code value}. In addition, it sets an internal member variable to
 * indicate that field {@code f} has been changed. Although field
 * {@code f} is changed immediately, the calendar's milliseconds is not
 * recomputed until the next call to {@code get()},
 * {@code getTime()}, or {@code getTimeInMillis()} is made. Thus,
 * multiple calls to {@code set()} do not trigger multiple, unnecessary
 * computations. As a result of changing a field using {@code set()},
 * other fields may also change, depending on the field, the field value, and
 * the calendar system. In addition, {@code get(f)} will not necessarily
 * return {@code value} after the fields have been recomputed. The
 * specifics are determined by the concrete calendar class.
 *
 * <p>
 * <em>Example</em>: Consider a {@code GregorianCalendar} originally
 * set to August 31, 1999. Calling <code>set(Calendar.MONTH,
 * Calendar.SEPTEMBER)</code>
 * sets the calendar to September 31, 1999. This is a temporary internal
 * representation that resolves to October 1, 1999 if {@code getTime()}is
 * then called. However, a call to {@code set(Calendar.DAY_OF_MONTH, 30)}
 * before the call to {@code getTime()} sets the calendar to September
 * 30, 1999, since no recomputation occurs after {@code set()} itself.
 *
 * <p>
 * <strong>{@code add(f, delta)}</strong> adds {@code delta} to
 * field {@code f}. This is equivalent to calling <code>set(f,
 * get(f) + delta)</code>
 * with two adjustments:
 *
 * <blockquote>
 * <p>
 * <strong>Add rule 1</strong>. The value of field {@code f} after the
 * call minus the value of field {@code f} before the call is
 * {@code delta}, modulo any overflow that has occurred in field
 * {@code f}. Overflow occurs when a field value exceeds its range and,
 * as a result, the next larger field is incremented or decremented and the
 * field value is adjusted back into its range.
 *
 * <p>
 * <strong>Add rule 2</strong>. If a smaller field is expected to be invariant,
 * but &nbsp; it is impossible for it to be equal to its prior value because of
 * changes in its minimum or maximum after field {@code f} is changed,
 * then its value is adjusted to be as close as possible to its expected value.
 * A smaller field represents a smaller unit of time. {@code HOUR} is a
 * smaller field than {@code DAY_OF_MONTH}. No adjustment is made to
 * smaller fields that are not expected to be invariant. The calendar system
 * determines what fields are expected to be invariant.
 * </blockquote>
 *
 * <p>
 * In addition, unlike {@code set()}, {@code add()} forces an
 * immediate recomputation of the calendar's milliseconds and all fields.
 *
 * <p>
 * <em>Example</em>: Consider a {@code GregorianCalendar} originally
 * set to August 31, 1999. Calling {@code add(Calendar.MONTH, 13)} sets
 * the calendar to September 30, 2000. <strong>Add rule 1</strong> sets the
 * {@code MONTH} field to September, since adding 13 months to August
 * gives September of the next year. Since {@code DAY_OF_MONTH} cannot be
 * 31 in September in a {@code GregorianCalendar}, <strong>add rule 2</strong>
 * sets the {@code DAY_OF_MONTH} to 30, the closest possible value.
 * Although it is a smaller field, {@code DAY_OF_WEEK} is not adjusted by
 * rule 2, since it is expected to change when the month changes in a
 * {@code GregorianCalendar}.
 *
 * <p>
 * <strong>{@code roll(f, delta)}</strong> adds {@code delta} to
 * field {@code f} without changing larger fields. This is equivalent to
 * calling {@code add(f, delta)} with the following adjustment:
 *
 * <blockquote>
 * <p>
 * <strong>Roll rule</strong>. Larger fields are unchanged after the call. A
 * larger field represents a larger unit of time. {@code DAY_OF_MONTH} is
 * a larger field than {@code HOUR}.
 * </blockquote>
 *
 * <p>
 * <em>Example</em>: Consider a {@code GregorianCalendar} originally
 * set to August 31, 1999. Calling <code>roll(Calendar.MONTH,
 * 8)</code> sets
 * the calendar to April 30, <strong>1999</strong>. Add rule 1 sets the
 * {@code MONTH} field to April. Using a {@code GregorianCalendar},
 * the {@code DAY_OF_MONTH} cannot be 31 in the month April. Add rule 2
 * sets it to the closest possible value, 30. Finally, the <strong>roll rule</strong>
 * maintains the {@code YEAR} field value of 1999.
 *
 * <p>
 * <em>Example</em>: Consider a {@code GregorianCalendar} originally
 * set to Sunday June 6, 1999. Calling
 * {@code roll(Calendar.WEEK_OF_MONTH, -1)} sets the calendar to Tuesday
 * June 1, 1999, whereas calling {@code add(Calendar.WEEK_OF_MONTH, -1)}
 * sets the calendar to Sunday May 30, 1999. This is because the roll rule
 * imposes an additional constraint: The {@code MONTH} must not change
 * when the {@code WEEK_OF_MONTH} is rolled. Taken together with add rule
 * 1, the resultant date must be between Tuesday June 1 and Saturday June 5.
 * According to add rule 2, the {@code DAY_OF_WEEK}, an invariant when
 * changing the {@code WEEK_OF_MONTH}, is set to Tuesday, the closest
 * possible value to Sunday (where Sunday is the first day of the week).
 *
 * <p>
 * <strong>Usage model</strong>. To motivate the behavior of {@code add()}
 * and {@code roll()}, consider a user interface component with
 * increment and decrement buttons for the month, day, and year, and an
 * underlying {@code GregorianCalendar}. If the interface reads January
 * 31, 1999 and the user presses the month increment button, what should it
 * read? If the underlying implementation uses {@code set()}, it might
 * read March 3, 1999. A better result would be February 28, 1999. Furthermore,
 * if the user presses the month increment button again, it should read March
 * 31, 1999, not March 28, 1999. By saving the original date and using either
 * {@code add()} or {@code roll()}, depending on whether larger
 * fields should be affected, the user interface can behave as most users will
 * intuitively expect.
 *
 * <p>
 * <b>Note:</b> You should always use {@code roll} and {@code add} rather than
 * attempting to perform arithmetic operations directly on the fields of a
 * <tt>Calendar</tt>. It is quite possible for <tt>Calendar</tt> subclasses
 * to have fields with non-linear behavior, for example missing months or days
 * during non-leap years. The subclasses' <tt>add</tt> and <tt>roll</tt>
 * methods will take this into account, while simple arithmetic manipulations
 * may give invalid results.
 *
 * @see Date
 * @see GregorianCalendar
 * @see TimeZone
 */
public abstract class Calendar implements Serializable, Cloneable, Comparable<Calendar> {

    private static final long serialVersionUID = -1807547505821590642L;

    /**
     * True iff the values in {@code fields[]} correspond to {@code time}. Despite the name, this
     * is effectively "are the values in fields[] up-to-date?" --- {@code fields[]} may contain
     * non-zero values and {@code isSet[]} may contain {@code true} values even when
     * {@code areFieldsSet} is false.
     * Accessing the fields via {@code get} will ensure the fields are up-to-date.
     */
    protected boolean areFieldsSet;

    /**
     * Contains broken-down field values for the current value of {@code time} if
     * {@code areFieldsSet} is true, or stale data corresponding to some previous value otherwise.
     * Accessing the fields via {@code get} will ensure the fields are up-to-date.
     * The array length is always {@code FIELD_COUNT}.
     */
    protected int[] fields;

    /**
     * Whether the corresponding element in {@code field[]} has been set. Initially, these are all
     * false. The first time the fields are computed, these are set to true and remain set even if
     * the data becomes stale: you <i>must</i> check {@code areFieldsSet} if you want to know
     * whether the value is up-to-date.
     * Note that {@code isSet} is <i>not</i> a safe alternative to accessing this array directly,
     * and will likewise return stale data!
     * The array length is always {@code FIELD_COUNT}.
     */
    protected boolean[] isSet;

    /**
     * Whether {@code time} corresponds to the values in {@code fields[]}. If false, {@code time}
     * is out-of-date with respect to changes {@code fields[]}.
     * Accessing the time via {@code getTimeInMillis} will always return the correct value.
     */
    protected boolean isTimeSet;

    /**
     * A time in milliseconds since January 1, 1970. See {@code isTimeSet}.
     * Accessing the time via {@code getTimeInMillis} will always return the correct value.
     */
    protected long time;

    transient int lastTimeFieldSet;

    transient int lastDateFieldSet;

    private boolean lenient;

    private int firstDayOfWeek;

    private int minimalDaysInFirstWeek;

    private TimeZone zone;

    /**
     * Value of the {@code MONTH} field indicating the first month of the
     * year.
     */
    public static final int JANUARY = 0;

    /**
     * Value of the {@code MONTH} field indicating the second month of
     * the year.
     */
    public static final int FEBRUARY = 1;

    /**
     * Value of the {@code MONTH} field indicating the third month of the
     * year.
     */
    public static final int MARCH = 2;

    /**
     * Value of the {@code MONTH} field indicating the fourth month of
     * the year.
     */
    public static final int APRIL = 3;

    /**
     * Value of the {@code MONTH} field indicating the fifth month of the
     * year.
     */
    public static final int MAY = 4;

    /**
     * Value of the {@code MONTH} field indicating the sixth month of the
     * year.
     */
    public static final int JUNE = 5;

    /**
     * Value of the {@code MONTH} field indicating the seventh month of
     * the year.
     */
    public static final int JULY = 6;

    /**
     * Value of the {@code MONTH} field indicating the eighth month of
     * the year.
     */
    public static final int AUGUST = 7;

    /**
     * Value of the {@code MONTH} field indicating the ninth month of the
     * year.
     */
    public static final int SEPTEMBER = 8;

    /**
     * Value of the {@code MONTH} field indicating the tenth month of the
     * year.
     */
    public static final int OCTOBER = 9;

    /**
     * Value of the {@code MONTH} field indicating the eleventh month of
     * the year.
     */
    public static final int NOVEMBER = 10;

    /**
     * Value of the {@code MONTH} field indicating the twelfth month of
     * the year.
     */
    public static final int DECEMBER = 11;

    /**
     * Value of the {@code MONTH} field indicating the thirteenth month
     * of the year. Although {@code GregorianCalendar} does not use this
     * value, lunar calendars do.
     */
    public static final int UNDECIMBER = 12;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Sunday.
     */
    public static final int SUNDAY = 1;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Monday.
     */
    public static final int MONDAY = 2;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Tuesday.
     */
    public static final int TUESDAY = 3;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Wednesday.
     */
    public static final int WEDNESDAY = 4;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Thursday.
     */
    public static final int THURSDAY = 5;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Friday.
     */
    public static final int FRIDAY = 6;

    /**
     * Value of the {@code DAY_OF_WEEK} field indicating Saturday.
     */
    public static final int SATURDAY = 7;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * era, e.g., AD or BC in the Julian calendar. This is a calendar-specific
     * value; see subclass documentation.
     *
     * @see GregorianCalendar#AD
     * @see GregorianCalendar#BC
     */
    public static final int ERA = 0;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * year. This is a calendar-specific value; see subclass documentation.
     */
    public static final int YEAR = 1;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * month. This is a calendar-specific value. The first month of the year is
     * {@code JANUARY}; the last depends on the number of months in a
     * year.
     *
     * @see #JANUARY
     * @see #FEBRUARY
     * @see #MARCH
     * @see #APRIL
     * @see #MAY
     * @see #JUNE
     * @see #JULY
     * @see #AUGUST
     * @see #SEPTEMBER
     * @see #OCTOBER
     * @see #NOVEMBER
     * @see #DECEMBER
     * @see #UNDECIMBER
     */
    public static final int MONTH = 2;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * week number within the current year. The first week of the year, as
     * defined by {@code getFirstDayOfWeek()} and
     * {@code getMinimalDaysInFirstWeek()}, has value 1. Subclasses
     * define the value of {@code WEEK_OF_YEAR} for days before the first
     * week of the year.
     *
     * @see #getFirstDayOfWeek
     * @see #getMinimalDaysInFirstWeek
     */
    public static final int WEEK_OF_YEAR = 3;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * week number within the current month. The first week of the month, as
     * defined by {@code getFirstDayOfWeek()} and
     * {@code getMinimalDaysInFirstWeek()}, has value 1. Subclasses
     * define the value of {@code WEEK_OF_MONTH} for days before the
     * first week of the month.
     *
     * @see #getFirstDayOfWeek
     * @see #getMinimalDaysInFirstWeek
     */
    public static final int WEEK_OF_MONTH = 4;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * day of the month. This is a synonym for {@code DAY_OF_MONTH}. The
     * first day of the month has value 1.
     *
     * @see #DAY_OF_MONTH
     */
    public static final int DATE = 5;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * day of the month. This is a synonym for {@code DATE}. The first
     * day of the month has value 1.
     *
     * @see #DATE
     */
    public static final int DAY_OF_MONTH = 5;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * day number within the current year. The first day of the year has value
     * 1.
     */
    public static final int DAY_OF_YEAR = 6;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * day of the week. This field takes values {@code SUNDAY},
     * {@code MONDAY}, {@code TUESDAY}, {@code WEDNESDAY},
     * {@code THURSDAY}, {@code FRIDAY}, and
     * {@code SATURDAY}.
     *
     * @see #SUNDAY
     * @see #MONDAY
     * @see #TUESDAY
     * @see #WEDNESDAY
     * @see #THURSDAY
     * @see #FRIDAY
     * @see #SATURDAY
     */
    public static final int DAY_OF_WEEK = 7;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * ordinal number of the day of the week within the current month. Together
     * with the {@code DAY_OF_WEEK} field, this uniquely specifies a day
     * within a month. Unlike {@code WEEK_OF_MONTH} and
     * {@code WEEK_OF_YEAR}, this field's value does <em>not</em>
     * depend on {@code getFirstDayOfWeek()} or
     * {@code getMinimalDaysInFirstWeek()}. {@code DAY_OF_MONTH 1}
     * through {@code 7} always correspond to <code>DAY_OF_WEEK_IN_MONTH
     * 1</code>;
     * {@code 8} through {@code 15} correspond to
     * {@code DAY_OF_WEEK_IN_MONTH 2}, and so on.
     * {@code DAY_OF_WEEK_IN_MONTH 0} indicates the week before
     * {@code DAY_OF_WEEK_IN_MONTH 1}. Negative values count back from
     * the end of the month, so the last Sunday of a month is specified as
     * {@code DAY_OF_WEEK = SUNDAY, DAY_OF_WEEK_IN_MONTH = -1}. Because
     * negative values count backward they will usually be aligned differently
     * within the month than positive values. For example, if a month has 31
     * days, {@code DAY_OF_WEEK_IN_MONTH -1} will overlap
     * {@code DAY_OF_WEEK_IN_MONTH 5} and the end of {@code 4}.
     *
     * @see #DAY_OF_WEEK
     * @see #WEEK_OF_MONTH
     */
    public static final int DAY_OF_WEEK_IN_MONTH = 8;

    /**
     * Field number for {@code get} and {@code set} indicating
     * whether the {@code HOUR} is before or after noon. E.g., at
     * 10:04:15.250 PM the {@code AM_PM} is {@code PM}.
     *
     * @see #AM
     * @see #PM
     * @see #HOUR
     */
    public static final int AM_PM = 9;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * hour of the morning or afternoon. {@code HOUR} is used for the
     * 12-hour clock. E.g., at 10:04:15.250 PM the {@code HOUR} is 10.
     *
     * @see #AM_PM
     * @see #HOUR_OF_DAY
     */
    public static final int HOUR = 10;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * hour of the day. {@code HOUR_OF_DAY} is used for the 24-hour
     * clock. E.g., at 10:04:15.250 PM the {@code HOUR_OF_DAY} is 22.
     *
     * @see #HOUR
     */
    public static final int HOUR_OF_DAY = 11;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * minute within the hour. E.g., at 10:04:15.250 PM the {@code MINUTE}
     * is 4.
     */
    public static final int MINUTE = 12;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * second within the minute. E.g., at 10:04:15.250 PM the
     * {@code SECOND} is 15.
     */
    public static final int SECOND = 13;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * millisecond within the second. E.g., at 10:04:15.250 PM the
     * {@code MILLISECOND} is 250.
     */
    public static final int MILLISECOND = 14;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * raw offset from GMT in milliseconds.
     */
    public static final int ZONE_OFFSET = 15;

    /**
     * Field number for {@code get} and {@code set} indicating the
     * daylight savings offset in milliseconds.
     */
    public static final int DST_OFFSET = 16;

    /**
     * This is the total number of fields in this calendar.
     */
    public static final int FIELD_COUNT = 17;

    /**
     * Value of the {@code AM_PM} field indicating the period of the day
     * from midnight to just before noon.
     */
    public static final int AM = 0;

    /**
     * Value of the {@code AM_PM} field indicating the period of the day
     * from noon to just before midnight.
     */
    public static final int PM = 1;

    /**
     * Requests both {@code SHORT} and {@code LONG} styles in the map returned by
     * {@link #getDisplayNames}.
     * @since 1.6
     */
    public static final int ALL_STYLES = 0;

    /**
     * Requests short names (such as "Jan") from
     * {@link #getDisplayName} or {@link #getDisplayNames}.
     * @since 1.6
     */
    public static final int SHORT = 1;

    /**
     * Requests long names (such as "January") from
     * {@link #getDisplayName} or {@link #getDisplayNames}.
     * @since 1.6
     */
    public static final int LONG = 2;

    private static final String[] FIELD_NAMES = { "ERA", "YEAR", "MONTH",
            "WEEK_OF_YEAR", "WEEK_OF_MONTH", "DAY_OF_MONTH", "DAY_OF_YEAR",
            "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR",
            "HOUR_OF_DAY", "MINUTE", "SECOND", "MILLISECOND",
            "ZONE_OFFSET", "DST_OFFSET" };

    /**
     * Constructs a {@code Calendar} instance using the default {@code TimeZone} and {@code Locale}.
     */
    protected Calendar() {
        this(TimeZone.getDefault(), Locale.getDefault());
    }

    Calendar(TimeZone timezone) {
        fields = new int[FIELD_COUNT];
        isSet = new boolean[FIELD_COUNT];
        areFieldsSet = isTimeSet = false;
        setLenient(true);
        setTimeZone(timezone);
    }

    /**
     * Constructs a {@code Calendar} instance using the given {@code TimeZone} and {@code Locale}.
     */
    protected Calendar(TimeZone timezone, Locale locale) {
        this(timezone);
        LocaleData localeData = LocaleData.get(locale);
        setFirstDayOfWeek(localeData.firstDayOfWeek.intValue());
        setMinimalDaysInFirstWeek(localeData.minimalDaysInFirstWeek.intValue());
    }


    /**
     * Adds the given amount to a {@code Calendar} field.
     *
     * @param field
     *            the {@code Calendar} field to modify.
     * @param value
     *            the amount to add to the field.
     * @throws IllegalArgumentException
     *                if {@code field} is {@code DST_OFFSET} or {@code
     *                ZONE_OFFSET}.
     */
    public abstract void add(int field, int value);

    /**
     * Returns whether the {@code Date} represented by this {@code Calendar} instance is after the {@code Date}
     * represented by the parameter. The comparison is not dependent on the time
     * zones of the {@code Calendar}.
     *
     * @param calendar
     *            the {@code Calendar} instance to compare.
     * @return {@code true} when this Calendar is after calendar, {@code false} otherwise.
     * @throws IllegalArgumentException
     *                if the time is not set and the time cannot be computed
     *                from the current field values.
     */
    public boolean after(Object calendar) {
        if (!(calendar instanceof Calendar)) {
            return false;
        }
        return getTimeInMillis() > ((Calendar) calendar).getTimeInMillis();
    }

    /**
     * Returns whether the {@code Date} represented by this {@code Calendar} instance is before the
     * {@code Date} represented by the parameter. The comparison is not dependent on the
     * time zones of the {@code Calendar}.
     *
     * @param calendar
     *            the {@code Calendar} instance to compare.
     * @return {@code true} when this Calendar is before calendar, {@code false} otherwise.
     * @throws IllegalArgumentException
     *                if the time is not set and the time cannot be computed
     *                from the current field values.
     */
    public boolean before(Object calendar) {
        if (!(calendar instanceof Calendar)) {
            return false;
        }
        return getTimeInMillis() < ((Calendar) calendar).getTimeInMillis();
    }

    /**
     * Clears the values of all the time fields, marking them all unset and assigning
     * them all a value of zero. The actual field values will be determined the next
     * time the fields are accessed.
     */
    public final void clear() {
        for (int i = 0; i < FIELD_COUNT; i++) {
            fields[i] = 0;
            isSet[i] = false;
        }
        areFieldsSet = isTimeSet = false;
    }

    /**
     * Clears the value in the given time field, marking it unset and assigning
     * it a value of zero. The actual field value will be determined the next
     * time the field is accessed.
     */
    public final void clear(int field) {
        fields[field] = 0;
        isSet[field] = false;
        areFieldsSet = isTimeSet = false;
    }

    /**
     * Returns a shallow copy of this {@code Calendar} with the same properties.
     */
    @Override
    public Object clone() {
        try {
            Calendar clone = (Calendar) super.clone();
            clone.fields = fields.clone();
            clone.isSet = isSet.clone();
            clone.zone = (TimeZone) zone.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Computes the time from the fields if the time has not already been set.
     * Computes the fields from the time if the fields are not already set.
     *
     * @throws IllegalArgumentException
     *                if the time is not set and the time cannot be computed
     *                from the current field values.
     */
    protected void complete() {
        if (!isTimeSet) {
            computeTime();
            isTimeSet = true;
        }
        if (!areFieldsSet) {
            computeFields();
            areFieldsSet = true;
        }
    }

    /**
     * Computes the {@code Calendar} fields from {@code time}.
     */
    protected abstract void computeFields();

    /**
     * Computes {@code time} from the Calendar fields.
     *
     * @throws IllegalArgumentException
     *                if the time cannot be computed from the current field
     *                values.
     */
    protected abstract void computeTime();

    /**
     * Compares the given object to this {@code Calendar} and returns whether they are
     * equal. The object must be an instance of {@code Calendar} and have the same
     * properties.
     *
     * @return {@code true} if the given object is equal to this {@code Calendar}, {@code false}
     *         otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Calendar)) {
            return false;
        }
        Calendar cal = (Calendar) object;
        return getTimeInMillis() == cal.getTimeInMillis()
                && isLenient() == cal.isLenient()
                && getFirstDayOfWeek() == cal.getFirstDayOfWeek()
                && getMinimalDaysInFirstWeek() == cal.getMinimalDaysInFirstWeek()
                && getTimeZone().equals(cal.getTimeZone());
    }

    /**
     * Returns the value of the given field after computing the field values by
     * calling {@code complete()} first.
     *
     * @throws IllegalArgumentException
     *                if the fields are not set, the time is not set, and the
     *                time cannot be computed from the current field values.
     * @throws ArrayIndexOutOfBoundsException
     *                if the field is not inside the range of possible fields.
     *                The range is starting at 0 up to {@code FIELD_COUNT}.
     */
    public int get(int field) {
        complete();
        return fields[field];
    }

    /**
     * Returns the maximum value of the given field for the current date.
     * For example, the maximum number of days in the current month.
     */
    public int getActualMaximum(int field) {
        int value, next;
        if (getMaximum(field) == (next = getLeastMaximum(field))) {
            return next;
        }
        complete();
        long orgTime = time;
        set(field, next);
        do {
            value = next;
            roll(field, true);
            next = get(field);
        } while (next > value);
        time = orgTime;
        areFieldsSet = false;
        return value;
    }

    /**
     * Returns the minimum value of the given field for the current date.
     */
    public int getActualMinimum(int field) {
        int value, next;
        if (getMinimum(field) == (next = getGreatestMinimum(field))) {
            return next;
        }
        complete();
        long orgTime = time;
        set(field, next);
        do {
            value = next;
            roll(field, false);
            next = get(field);
        } while (next < value);
        time = orgTime;
        areFieldsSet = false;
        return value;
    }

    /**
     * Returns an array of locales for which custom {@code Calendar} instances
     * are available.
     * <p>Note that Android does not support user-supplied locale service providers.
     */
    public static synchronized Locale[] getAvailableLocales() {
        return ICU.getAvailableCalendarLocales();
    }

    /**
     * Returns the first day of the week for this {@code Calendar}.
     */
    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * Returns the greatest minimum value of the given field. This is the
     * biggest value that {@code getActualMinimum} can return for any possible
     * time.
     */
    public abstract int getGreatestMinimum(int field);

    /**
     * Constructs a new instance of the {@code Calendar} subclass appropriate for the
     * default {@code Locale} and default {@code TimeZone}, set to the current date and time.
     */
    public static synchronized Calendar getInstance() {
        return new GregorianCalendar();
    }

    /**
     * Constructs a new instance of the {@code Calendar} subclass appropriate for the
     * given {@code Locale} and default {@code TimeZone}, set to the current date and time.
     */
    public static synchronized Calendar getInstance(Locale locale) {
        return new GregorianCalendar(locale);
    }

    /**
     * Constructs a new instance of the {@code Calendar} subclass appropriate for the
     * default {@code Locale} and given {@code TimeZone}, set to the current date and time.
     */
    public static synchronized Calendar getInstance(TimeZone timezone) {
        return new GregorianCalendar(timezone);
    }

    /**
     * Constructs a new instance of the {@code Calendar} subclass appropriate for the
     * given {@code Locale} and given {@code TimeZone}, set to the current date and time.
     */
    public static synchronized Calendar getInstance(TimeZone timezone, Locale locale) {
        return new GregorianCalendar(timezone, locale);
    }

    /**
     * Returns the smallest maximum value of the given field. This is the
     * smallest value that {@code getActualMaximum()} can return for any
     * possible time.
     */
    public abstract int getLeastMaximum(int field);

    /**
     * Returns the greatest maximum value of the given field. This returns the
     * biggest value that {@code get} can return for the given field.
     */
    public abstract int getMaximum(int field);

    /**
     * Returns the minimal days in the first week of the year.
     */
    public int getMinimalDaysInFirstWeek() {
        return minimalDaysInFirstWeek;
    }

    /**
     * Returns the smallest minimum value of the given field. this returns the
     * smallest value that {@code get} can return for the given field.
     */
    public abstract int getMinimum(int field);

    /**
     * Returns the time of this {@code Calendar} as a {@code Date} object.
     *
     * @throws IllegalArgumentException
     *                if the time is not set and the time cannot be computed
     *                from the current field values.
     */
    public final Date getTime() {
        return new Date(getTimeInMillis());
    }

    /**
     * Returns the time represented by this {@code Calendar}, recomputing the time from its
     * fields if necessary.
     *
     * @throws IllegalArgumentException
     *                if the time is not set and the time cannot be computed
     *                from the current field values.
     */
    public long getTimeInMillis() {
        if (!isTimeSet) {
            computeTime();
            isTimeSet = true;
        }
        return time;
    }

    /**
     * Returns the time zone used by this {@code Calendar}.
     */
    public TimeZone getTimeZone() {
        return zone;
    }

    @Override
    public int hashCode() {
        return (isLenient() ? 1237 : 1231) + getFirstDayOfWeek()
                + getMinimalDaysInFirstWeek() + getTimeZone().hashCode();
    }

    /**
     * Returns the value of the given field without recomputing.
     */
    protected final int internalGet(int field) {
        return fields[field];
    }

    /**
     * Tests whether this {@code Calendar} accepts field values which are outside the valid
     * range for the field.
     */
    public boolean isLenient() {
        return lenient;
    }

    /**
     * Tests whether the given field is set. Note that the interpretation of "is set" is
     * somewhat technical. In particular, it does <i>not</i> mean that the field's value is up
     * to date. If you want to know whether a field contains an up-to-date value, you must also
     * check {@code areFieldsSet}, making this method somewhat useless unless you're a subclass,
     * in which case you can access the {@code isSet} array directly.
     * <p>
     * A field remains "set" from the first time its value is computed until it's cleared by one
     * of the {@code clear} methods. Thus "set" does not mean "valid". You probably want to call
     * {@code get} -- which will update fields as necessary -- rather than try to make use of
     * this method.
     */
    public final boolean isSet(int field) {
        return isSet[field];
    }

    /**
     * Adds the given amount to the given field and wraps the value of
     * the field when it goes beyond the maximum or minimum value for the
     * current date. Other fields will be adjusted as required to maintain a
     * consistent date.
     */
    public void roll(int field, int value) {
        boolean increment = value >= 0;
        int count = increment ? value : -value;
        for (int i = 0; i < count; i++) {
            roll(field, increment);
        }
    }

    /**
     * Increment or decrement the given field and wrap the value of the
     * field when it goes beyond the maximum or minimum value for the current
     * date. Other fields will be adjusted as required to maintain a consistent
     * date.
     */
    public abstract void roll(int field, boolean increment);

    /**
     * Sets the given field to the given value.
     */
    public void set(int field, int value) {
        fields[field] = value;
        isSet[field] = true;
        areFieldsSet = isTimeSet = false;
        if (field > MONTH && field < AM_PM) {
            lastDateFieldSet = field;
        }
        if (field == HOUR || field == HOUR_OF_DAY) {
            lastTimeFieldSet = field;
        }
        if (field == AM_PM) {
            lastTimeFieldSet = HOUR;
        }
    }

    /**
     * Sets the year, month, and day of the month fields.
     * Other fields are not changed; call {@link #clear} first if this is not desired.
     * The month value is 0-based, so it may be clearer to use a constant like {@code JANUARY}.
     */
    public final void set(int year, int month, int day) {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, day);
    }

    /**
     * Sets the year, month, day of the month, hour of day, and minute fields.
     * Other fields are not changed; call {@link #clear} first if this is not desired.
     * The month value is 0-based, so it may be clearer to use a constant like {@code JANUARY}.
     */
    public final void set(int year, int month, int day, int hourOfDay, int minute) {
        set(year, month, day);
        set(HOUR_OF_DAY, hourOfDay);
        set(MINUTE, minute);
    }

    /**
     * Sets the year, month, day of the month, hour of day, minute, and second fields.
     * Other fields are not changed; call {@link #clear} first if this is not desired.
     * The month value is 0-based, so it may be clearer to use a constant like {@code JANUARY}.
     */
    public final void set(int year, int month, int day, int hourOfDay, int minute, int second) {
        set(year, month, day, hourOfDay, minute);
        set(SECOND, second);
    }

    /**
     * Sets the first day of the week for this {@code Calendar}.
     * The value should be a day of the week such as {@code MONDAY}.
     */
    public void setFirstDayOfWeek(int value) {
        firstDayOfWeek = value;
    }

    /**
     * Sets whether this {@code Calendar} accepts field values which are outside the valid
     * range for the field.
     */
    public void setLenient(boolean value) {
        lenient = value;
    }

    /**
     * Sets the minimal days in the first week of the year.
     */
    public void setMinimalDaysInFirstWeek(int value) {
        minimalDaysInFirstWeek = value;
    }

    /**
     * Sets the time of this {@code Calendar}.
     */
    public final void setTime(Date date) {
        setTimeInMillis(date.getTime());
    }

    /**
     * Sets the time of this {@code Calendar} to the given Unix time. See {@link Date} for more
     * about what this means.
     */
    public void setTimeInMillis(long milliseconds) {
        if (!isTimeSet || !areFieldsSet || time != milliseconds) {
            time = milliseconds;
            isTimeSet = true;
            areFieldsSet = false;
            complete();
        }
    }

    /**
     * Sets the {@code TimeZone} used by this Calendar.
     */
    public void setTimeZone(TimeZone timezone) {
        zone = timezone;
        areFieldsSet = false;
    }

    /**
     * Returns a string representation of this {@code Calendar}, showing which fields are set.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getName() +
                "[time=" + (isTimeSet ? String.valueOf(time) : "?") +
                ",areFieldsSet=" + areFieldsSet +
                ",lenient=" + lenient +
                ",zone=" + zone.getID() +
                ",firstDayOfWeek=" + firstDayOfWeek +
                ",minimalDaysInFirstWeek=" + minimalDaysInFirstWeek);
        for (int i = 0; i < FIELD_COUNT; i++) {
            result.append(',');
            result.append(FIELD_NAMES[i]);
            result.append('=');
            if (isSet[i]) {
                result.append(fields[i]);
            } else {
                result.append('?');
            }
        }
        result.append(']');
        return result.toString();
    }

    /**
     * Compares the time represented by this {@code Calendar} to that represented by the given
     * {@code Calendar}.
     *
     * @return 0 if the times of the two {@code Calendar}s are equal, -1 if the time of
     *         this {@code Calendar} is before the other one, 1 if the time of this
     *         {@code Calendar} is after the other one.
     * @throws NullPointerException
     *             if the argument is null.
     * @throws IllegalArgumentException
     *             if the argument does not include a valid time
     *             value.
     */
    public int compareTo(Calendar anotherCalendar) {
        if (anotherCalendar == null) {
            throw new NullPointerException("anotherCalendar == null");
        }
        long timeInMillis = getTimeInMillis();
        long anotherTimeInMillis = anotherCalendar.getTimeInMillis();
        if (timeInMillis > anotherTimeInMillis) {
            return 1;
        }
        if (timeInMillis == anotherTimeInMillis) {
            return 0;
        }
        return -1;
    }

    /**
     * Returns a human-readable string for the value of {@code field}
     * using the given style and locale. If no string is available, returns null.
     * The value is retrieved by invoking {@code get(field)}.
     *
     * <p>For example, {@code getDisplayName(MONTH, SHORT, Locale.US)} will return "Jan"
     * while {@code getDisplayName(MONTH, LONG, Locale.US)} will return "January".
     *
     * @param field the field
     * @param style {@code SHORT} or {@code LONG}
     * @param locale the locale
     * @return the display name, or null
     * @throws NullPointerException if {@code locale == null}
     * @throws IllegalArgumentException if {@code field} or {@code style} is invalid
     * @since 1.6
     */
    public String getDisplayName(int field, int style, Locale locale) {
        // TODO: the RI's documentation says ALL_STYLES is invalid, but actually treats it as SHORT.
        if (style == ALL_STYLES) {
            style = SHORT;
        }
        String[] array = getDisplayNameArray(field, style, locale);
        int value = get(field);
        return (array != null) ? array[value] : null;
    }

    private String[] getDisplayNameArray(int field, int style, Locale locale) {
        if (field < 0 || field >= FIELD_COUNT) {
            throw new IllegalArgumentException("bad field " + field);
        }
        checkStyle(style);
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(locale);
        switch (field) {
        case AM_PM:
            return dfs.getAmPmStrings();
        case DAY_OF_WEEK:
            return (style == LONG) ? dfs.getWeekdays() : dfs.getShortWeekdays();
        case ERA:
            return dfs.getEras();
        case MONTH:
            return (style == LONG) ? dfs.getMonths() : dfs.getShortMonths();
        }
        return null;
    }

    private static void checkStyle(int style) {
        if (style != ALL_STYLES && style != SHORT && style != LONG) {
            throw new IllegalArgumentException("bad style " + style);
        }
    }

    /**
     * Returns a map of human-readable strings to corresponding values,
     * for the given field, style, and locale.
     * Returns null if no strings are available.
     *
     * <p>For example, {@code getDisplayNames(MONTH, ALL_STYLES, Locale.US)} would
     * contain mappings from "Jan" and "January" to {@link #JANUARY}, and so on.
     *
     * @param field the field
     * @param style {@code SHORT}, {@code LONG}, or {@code ALL_STYLES}
     * @param locale the locale
     * @return the display name, or null
     * @throws NullPointerException if {@code locale == null}
     * @throws IllegalArgumentException if {@code field} or {@code style} is invalid
     * @since 1.6
     */
    public Map<String, Integer> getDisplayNames(int field, int style, Locale locale) {
        checkStyle(style);
        complete();
        Map<String, Integer> result = new HashMap<String, Integer>();
        if (style == SHORT || style == ALL_STYLES) {
            insertValuesInMap(result, getDisplayNameArray(field, SHORT, locale));
        }
        if (style == LONG || style == ALL_STYLES) {
            insertValuesInMap(result, getDisplayNameArray(field, LONG, locale));
        }
        return result.isEmpty() ? null : result;
    }

    private static void insertValuesInMap(Map<String, Integer> map, String[] values) {
        if (values == null) {
            return;
        }
        for (int i = 0; i < values.length; ++i) {
            if (values[i] != null && !values[i].isEmpty()) {
                map.put(values[i], i);
            }
        }
    }

    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("areFieldsSet", boolean.class),
        new ObjectStreamField("fields", int[].class),
        new ObjectStreamField("firstDayOfWeek", int.class),
        new ObjectStreamField("isSet", boolean[].class),
        new ObjectStreamField("isTimeSet", boolean.class),
        new ObjectStreamField("lenient", boolean.class),
        new ObjectStreamField("minimalDaysInFirstWeek", int.class),
        new ObjectStreamField("nextStamp", int.class),
        new ObjectStreamField("serialVersionOnStream", int.class),
        new ObjectStreamField("time", long.class),
        new ObjectStreamField("zone", TimeZone.class),
    };

    private void writeObject(ObjectOutputStream stream) throws IOException {
        complete();
        ObjectOutputStream.PutField putFields = stream.putFields();
        putFields.put("areFieldsSet", areFieldsSet);
        putFields.put("fields", this.fields);
        putFields.put("firstDayOfWeek", firstDayOfWeek);
        putFields.put("isSet", isSet);
        putFields.put("isTimeSet", isTimeSet);
        putFields.put("lenient", lenient);
        putFields.put("minimalDaysInFirstWeek", minimalDaysInFirstWeek);
        putFields.put("nextStamp", 2 /* MINIMUM_USER_STAMP */);
        putFields.put("serialVersionOnStream", 1);
        putFields.put("time", time);
        putFields.put("zone", zone);
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField readFields = stream.readFields();
        areFieldsSet = readFields.get("areFieldsSet", false);
        this.fields = (int[]) readFields.get("fields", null);
        firstDayOfWeek = readFields.get("firstDayOfWeek", Calendar.SUNDAY);
        isSet = (boolean[]) readFields.get("isSet", null);
        isTimeSet = readFields.get("isTimeSet", false);
        lenient = readFields.get("lenient", true);
        minimalDaysInFirstWeek = readFields.get("minimalDaysInFirstWeek", 1);
        time = readFields.get("time", 0L);
        zone = (TimeZone) readFields.get("zone", null);
    }
}
