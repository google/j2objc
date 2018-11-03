/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *   Copyright (C) 1996-2016, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 */

package android.icu.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;

import android.icu.impl.CalendarUtil;
import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.SoftCache;
import android.icu.text.DateFormat;
import android.icu.text.DateFormatSymbols;
import android.icu.text.SimpleDateFormat;
import android.icu.util.ULocale.Category;

/**
 * <strong>[icu enhancement]</strong> ICU's replacement for {@link java.util.Calendar}.&nbsp;Methods, fields, and other functionality specific to ICU are labeled '<strong>[icu]</strong>'.
 *
 * <p><code>Calendar</code> is an abstract base class for converting between
 * a <code>Date</code> object and a set of integer fields such as
 * <code>YEAR</code>, <code>MONTH</code>, <code>DAY</code>, <code>HOUR</code>,
 * and so on. (A <code>Date</code> object represents a specific instant in
 * time with millisecond precision. See
 * {@link Date}
 * for information about the <code>Date</code> class.)
 *
 * <p>Subclasses of <code>Calendar</code> interpret a <code>Date</code>
 * according to the rules of a specific calendar system.  ICU4J contains
 * several subclasses implementing different international calendar systems.
 *
 * <p>
 * Like other locale-sensitive classes, <code>Calendar</code> provides a
 * class method, <code>getInstance</code>, for getting a generally useful
 * object of this type. <code>Calendar</code>'s <code>getInstance</code> method
 * returns a calendar of a type appropriate to the locale, whose
 * time fields have been initialized with the current date and time:
 * <blockquote>
 * <pre>Calendar rightNow = Calendar.getInstance()</pre>
 * </blockquote>
 *
 * <p>When a <code>ULocale</code> is used by <code>getInstance</code>, its
 * '<code>calendar</code>' tag and value are retrieved if present.  If a recognized
 * value is supplied, a calendar is provided and configured as appropriate.
 * Currently recognized tags are "buddhist", "chinese", "coptic", "ethiopic",
 * "gregorian", "hebrew", "islamic", "islamic-civil", "japanese", and "roc".  For
 * example: <blockquote>
 * <pre>Calendar cal = Calendar.getInstance(new ULocale("en_US@calendar=japanese"));</pre>
 * </blockquote> will return an instance of JapaneseCalendar (using en_US conventions for
 * minimum days in first week, start day of week, et cetera).
 *
 * <p>A <code>Calendar</code> object can produce all the time field values
 * needed to implement the date-time formatting for a particular language and
 * calendar style (for example, Japanese-Gregorian, Japanese-Traditional).
 * <code>Calendar</code> defines the range of values returned by certain fields,
 * as well as their meaning.  For example, the first month of the year has value
 * <code>MONTH</code> == <code>JANUARY</code> for all calendars.  Other values
 * are defined by the concrete subclass, such as <code>ERA</code> and
 * <code>YEAR</code>.  See individual field documentation and subclass
 * documentation for details.
 *
 * <p>When a <code>Calendar</code> is <em>lenient</em>, it accepts a wider range
 * of field values than it produces.  For example, a lenient
 * <code>GregorianCalendar</code> interprets <code>MONTH</code> ==
 * <code>JANUARY</code>, <code>DAY_OF_MONTH</code> == 32 as February 1.  A
 * non-lenient <code>GregorianCalendar</code> throws an exception when given
 * out-of-range field settings.  When calendars recompute field values for
 * return by <code>get()</code>, they normalize them.  For example, a
 * <code>GregorianCalendar</code> always produces <code>DAY_OF_MONTH</code>
 * values between 1 and the length of the month.
 *
 * <p><code>Calendar</code> defines a locale-specific seven day week using two
 * parameters: the first day of the week and the minimal days in first week
 * (from 1 to 7).  These numbers are taken from the locale resource data when a
 * <code>Calendar</code> is constructed.  They may also be specified explicitly
 * through the API.
 *
 * <p>When setting or getting the <code>WEEK_OF_MONTH</code> or
 * <code>WEEK_OF_YEAR</code> fields, <code>Calendar</code> must determine the
 * first week of the month or year as a reference point.  The first week of a
 * month or year is defined as the earliest seven day period beginning on
 * <code>getFirstDayOfWeek()</code> and containing at least
 * <code>getMinimalDaysInFirstWeek()</code> days of that month or year.  Weeks
 * numbered ..., -1, 0 precede the first week; weeks numbered 2, 3,... follow
 * it.  Note that the normalized numbering returned by <code>get()</code> may be
 * different.  For example, a specific <code>Calendar</code> subclass may
 * designate the week before week 1 of a year as week <em>n</em> of the previous
 * year.
 *
 * <p> When computing a <code>Date</code> from time fields, some special
 * circumstances may arise: there may be insufficient information to compute the
 * <code>Date</code> (such as only year and month but no day in the month),
 * there may be inconsistent information (such as "Tuesday, July 15, 1996" --
 * July 15, 1996 is actually a Monday), or the input time might be ambiguous
 * because of time zone transition.
 *
 * <p><strong>Insufficient information.</strong> The calendar will use default
 * information to specify the missing fields. This may vary by calendar; for
 * the Gregorian calendar, the default for a field is the same as that of the
 * start of the epoch: i.e., YEAR = 1970, MONTH = JANUARY, DATE = 1, etc.
 *
 * <p><strong>Inconsistent information.</strong> If fields conflict, the calendar
 * will give preference to fields set more recently. For example, when
 * determining the day, the calendar will look for one of the following
 * combinations of fields.  The most recent combination, as determined by the
 * most recently set single field, will be used.
 *
 * <blockquote>
 * <pre>
 * MONTH + DAY_OF_MONTH
 * MONTH + WEEK_OF_MONTH + DAY_OF_WEEK
 * MONTH + DAY_OF_WEEK_IN_MONTH + DAY_OF_WEEK
 * DAY_OF_YEAR
 * DAY_OF_WEEK + WEEK_OF_YEAR</pre>
 * </blockquote>
 *
 * For the time of day:
 *
 * <blockquote>
 * <pre>
 * HOUR_OF_DAY
 * AM_PM + HOUR</pre>
 * </blockquote>
 *
 * <p><strong>Ambiguous Wall Clock Time.</strong> When time offset from UTC has
 * changed, it produces an ambiguous time slot around the transition. For example,
 * many US locations observe daylight saving time. On the date switching to daylight
 * saving time in US, wall clock time jumps from 12:59 AM (standard) to 2:00 AM
 * (daylight). Therefore, wall clock time from 1:00 AM to 1:59 AM do not exist on
 * the date. When the input wall time fall into this missing time slot, the ICU
 * Calendar resolves the time using the UTC offset before the transition by default.
 * In this example, 1:30 AM is interpreted as 1:30 AM standard time (non-exist),
 * so the final result will be 2:30 AM daylight time.
 *
 * <p>On the date switching back to standard time, wall clock time is moved back one
 * hour at 2:00 AM. So wall clock time from 1:00 AM to 1:59 AM occur twice. In this
 * case, the ICU Calendar resolves the time using the UTC offset after the transition
 * by default. For example, 1:30 AM on the date is resolved as 1:30 AM standard time.
 *
 * <p>Ambiguous wall clock time resolution behaviors can be customized by Calendar APIs
 * {@link #setRepeatedWallTimeOption(int)} and {@link #setSkippedWallTimeOption(int)}.
 * These methods are available in ICU 49 or later versions.
 *
 * <p><strong>Note:</strong> for some non-Gregorian calendars, different
 * fields may be necessary for complete disambiguation. For example, a full
 * specification of the historial Arabic astronomical calendar requires year,
 * month, day-of-month <em>and</em> day-of-week in some cases.
 *
 * <p><strong>Note:</strong> There are certain possible ambiguities in
 * interpretation of certain singular times, which are resolved in the
 * following ways:
 * <ol>
 *     <li> 24:00:00 "belongs" to the following day. That is,
 *          23:59 on Dec 31, 1969 &lt; 24:00 on Jan 1, 1970 &lt; 24:01:00 on Jan 1, 1970
 *
 *     <li> Although historically not precise, midnight also belongs to "am",
 *          and noon belongs to "pm", so on the same day,
 *          12:00 am (midnight) &lt; 12:01 am, and 12:00 pm (noon) &lt; 12:01 pm
 * </ol>
 *
 * <p>The date or time format strings are not part of the definition of a
 * calendar, as those must be modifiable or overridable by the user at
 * runtime. Use {@link DateFormat}
 * to format dates.
 *
 * <p><strong>Field manipulation methods</strong></p>
 *
 * <p><code>Calendar</code> fields can be changed using three methods:
 * <code>set()</code>, <code>add()</code>, and <code>roll()</code>.</p>
 *
 * <p><strong><code>set(f, value)</code></strong> changes field
 * <code>f</code> to <code>value</code>.  In addition, it sets an
 * internal member variable to indicate that field <code>f</code> has
 * been changed. Although field <code>f</code> is changed immediately,
 * the calendar's milliseconds is not recomputed until the next call to
 * <code>get()</code>, <code>getTime()</code>, or
 * <code>getTimeInMillis()</code> is made. Thus, multiple calls to
 * <code>set()</code> do not trigger multiple, unnecessary
 * computations. As a result of changing a field using
 * <code>set()</code>, other fields may also change, depending on the
 * field, the field value, and the calendar system. In addition,
 * <code>get(f)</code> will not necessarily return <code>value</code>
 * after the fields have been recomputed. The specifics are determined by
 * the concrete calendar class.</p>
 *
 * <p><em>Example</em>: Consider a <code>GregorianCalendar</code>
 * originally set to August 31, 1999. Calling <code>set(Calendar.MONTH,
 * Calendar.SEPTEMBER)</code> sets the calendar to September 31,
 * 1999. This is a temporary internal representation that resolves to
 * October 1, 1999 if <code>getTime()</code>is then called. However, a
 * call to <code>set(Calendar.DAY_OF_MONTH, 30)</code> before the call to
 * <code>getTime()</code> sets the calendar to September 30, 1999, since
 * no recomputation occurs after <code>set()</code> itself.</p>
 *
 * <p><strong><code>add(f, delta)</code></strong> adds <code>delta</code>
 * to field <code>f</code>.  This is equivalent to calling <code>set(f,
 * get(f) + delta)</code> with two adjustments:</p>
 *
 * <blockquote>
 *   <p><strong>Add rule 1</strong>. The value of field <code>f</code>
 *   after the call minus the value of field <code>f</code> before the
 *   call is <code>delta</code>, modulo any overflow that has occurred in
 *   field <code>f</code>. Overflow occurs when a field value exceeds its
 *   range and, as a result, the next larger field is incremented or
 *   decremented and the field value is adjusted back into its range.</p>
 *
 *   <p><strong>Add rule 2</strong>. If a smaller field is expected to be
 *   invariant, but &nbsp; it is impossible for it to be equal to its
 *   prior value because of changes in its minimum or maximum after field
 *   <code>f</code> is changed, then its value is adjusted to be as close
 *   as possible to its expected value. A smaller field represents a
 *   smaller unit of time. <code>HOUR</code> is a smaller field than
 *   <code>DAY_OF_MONTH</code>. No adjustment is made to smaller fields
 *   that are not expected to be invariant. The calendar system
 *   determines what fields are expected to be invariant.</p>
 * </blockquote>
 *
 * <p>In addition, unlike <code>set()</code>, <code>add()</code> forces
 * an immediate recomputation of the calendar's milliseconds and all
 * fields.</p>
 *
 * <p><em>Example</em>: Consider a <code>GregorianCalendar</code>
 * originally set to August 31, 1999. Calling <code>add(Calendar.MONTH,
 * 13)</code> sets the calendar to September 30, 2000. <strong>Add rule
 * 1</strong> sets the <code>MONTH</code> field to September, since
 * adding 13 months to August gives September of the next year. Since
 * <code>DAY_OF_MONTH</code> cannot be 31 in September in a
 * <code>GregorianCalendar</code>, <strong>add rule 2</strong> sets the
 * <code>DAY_OF_MONTH</code> to 30, the closest possible value. Although
 * it is a smaller field, <code>DAY_OF_WEEK</code> is not adjusted by
 * rule 2, since it is expected to change when the month changes in a
 * <code>GregorianCalendar</code>.</p>
 *
 * <p><strong><code>roll(f, delta)</code></strong> adds
 * <code>delta</code> to field <code>f</code> without changing larger
 * fields. This is equivalent to calling <code>add(f, delta)</code> with
 * the following adjustment:</p>
 *
 * <blockquote>
 *   <p><strong>Roll rule</strong>. Larger fields are unchanged after the
 *   call. A larger field represents a larger unit of
 *   time. <code>DAY_OF_MONTH</code> is a larger field than
 *   <code>HOUR</code>.</p>
 * </blockquote>
 *
 * <p><em>Example</em>: Consider a <code>GregorianCalendar</code>
 * originally set to August 31, 1999. Calling <code>roll(Calendar.MONTH,
 * 8)</code> sets the calendar to April 30, <strong>1999</strong>.  Add
 * rule 1 sets the <code>MONTH</code> field to April. Using a
 * <code>GregorianCalendar</code>, the <code>DAY_OF_MONTH</code> cannot
 * be 31 in the month April. Add rule 2 sets it to the closest possible
 * value, 30. Finally, the <strong>roll rule</strong> maintains the
 * <code>YEAR</code> field value of 1999.</p>
 *
 * <p><em>Example</em>: Consider a <code>GregorianCalendar</code>
 * originally set to Sunday June 6, 1999. Calling
 * <code>roll(Calendar.WEEK_OF_MONTH, -1)</code> sets the calendar to
 * Tuesday June 1, 1999, whereas calling
 * <code>add(Calendar.WEEK_OF_MONTH, -1)</code> sets the calendar to
 * Sunday May 30, 1999. This is because the roll rule imposes an
 * additional constraint: The <code>MONTH</code> must not change when the
 * <code>WEEK_OF_MONTH</code> is rolled. Taken together with add rule 1,
 * the resultant date must be between Tuesday June 1 and Saturday June
 * 5. According to add rule 2, the <code>DAY_OF_WEEK</code>, an invariant
 * when changing the <code>WEEK_OF_MONTH</code>, is set to Tuesday, the
 * closest possible value to Sunday (where Sunday is the first day of the
 * week).</p>
 *
 * <p><strong>Usage model</strong>. To motivate the behavior of
 * <code>add()</code> and <code>roll()</code>, consider a user interface
 * component with increment and decrement buttons for the month, day, and
 * year, and an underlying <code>GregorianCalendar</code>. If the
 * interface reads January 31, 1999 and the user presses the month
 * increment button, what should it read? If the underlying
 * implementation uses <code>set()</code>, it might read March 3, 1999. A
 * better result would be February 28, 1999. Furthermore, if the user
 * presses the month increment button again, it should read March 31,
 * 1999, not March 28, 1999. By saving the original date and using either
 * <code>add()</code> or <code>roll()</code>, depending on whether larger
 * fields should be affected, the user interface can behave as most users
 * will intuitively expect.</p>
 *
 * <p><b>Note:</b> You should always use {@link #roll roll} and {@link #add add} rather
 * than attempting to perform arithmetic operations directly on the fields
 * of a <tt>Calendar</tt>.  It is quite possible for <tt>Calendar</tt> subclasses
 * to have fields with non-linear behavior, for example missing months
 * or days during non-leap years.  The subclasses' <tt>add</tt> and <tt>roll</tt>
 * methods will take this into account, while simple arithmetic manipulations
 * may give invalid results.
 *
 * <p><big><big><b>Calendar Architecture in ICU4J</b></big></big></p>
 *
 * <p>Recently the implementation of <code>Calendar</code> has changed
 * significantly in order to better support subclassing. The original
 * <code>Calendar</code> class was designed to support subclassing, but
 * it had only one implemented subclass, <code>GregorianCalendar</code>.
 * With the implementation of several new calendar subclasses, including
 * the <code>BuddhistCalendar</code>, <code>ChineseCalendar</code>,
 * <code>HebrewCalendar</code>, <code>IslamicCalendar</code>, and
 * <code>JapaneseCalendar</code>, the subclassing API has been reworked
 * thoroughly. This section details the new subclassing API and other
 * ways in which <code>android.icu.util.Calendar</code> differs from
 * <code>java.util.Calendar</code>.
 * </p>
 *
 * <p><big><b>Changes</b></big></p>
 *
 * <p>Overview of changes between the classic <code>Calendar</code>
 * architecture and the new architecture.
 *
 * <ul>
 *
 *   <li>The <code>fields[]</code> array is <code>private</code> now
 *     instead of <code>protected</code>.  Subclasses must access it
 *     using the methods {@link #internalSet} and
 *     {@link #internalGet}.  <b>Motivation:</b> Subclasses should
 *     not directly access data members.</li>
 *
 *   <li>The <code>time</code> long word is <code>private</code> now
 *     instead of <code>protected</code>.  Subclasses may access it using
 *     the method {@link #internalGetTimeInMillis}, which does not
 *     provoke an update. <b>Motivation:</b> Subclasses should not
 *     directly access data members.</li>
 *
 *   <li>The scope of responsibility of subclasses has been drastically
 *     reduced. As much functionality as possible is implemented in the
 *     <code>Calendar</code> base class. As a result, it is much easier
 *     to subclass <code>Calendar</code>. <b>Motivation:</b> Subclasses
 *     should not have to reimplement common code. Certain behaviors are
 *     common across calendar systems: The definition and behavior of
 *     week-related fields and time fields, the arithmetic
 *     ({@link #add(int, int) add} and {@link #roll(int, int) roll}) behavior of many
 *     fields, and the field validation system.</li>
 *
 *   <li>The subclassing API has been completely redesigned.</li>
 *
 *   <li>The <code>Calendar</code> base class contains some Gregorian
 *     calendar algorithmic support that subclasses can use (specifically
 *     in {@link #handleComputeFields}).  Subclasses can use the
 *     methods <code>getGregorianXxx()</code> to obtain precomputed
 *     values. <b>Motivation:</b> This is required by all
 *     <code>Calendar</code> subclasses in order to implement consistent
 *     time zone behavior, and Gregorian-derived systems can use the
 *     already computed data.</li>
 *
 *   <li>The <code>FIELD_COUNT</code> constant has been removed. Use
 *     {@link #getFieldCount}.  In addition, framework API has been
 *     added to allow subclasses to define additional fields.
 *     <b>Motivation: </b>The number of fields is not constant across
 *     calendar systems.</li>
 *
 *   <li>The range of handled dates has been narrowed from +/-
 *     ~300,000,000 years to +/- ~5,000,000 years. In practical terms
 *     this should not affect clients. However, it does mean that client
 *     code cannot be guaranteed well-behaved results with dates such as
 *     <code>Date(Long.MIN_VALUE)</code> or
 *     <code>Date(Long.MAX_VALUE)</code>. Instead, the
 *     <code>Calendar</code> protected constants should be used.
 *     <b>Motivation:</b> With
 *     the addition of the {@link #JULIAN_DAY} field, Julian day
 *     numbers must be restricted to a 32-bit <code>int</code>.  This
 *     restricts the overall supported range. Furthermore, restricting
 *     the supported range simplifies the computations by removing
 *     special case code that was used to accomodate arithmetic overflow
 *     at millis near <code>Long.MIN_VALUE</code> and
 *     <code>Long.MAX_VALUE</code>.</li>
 *
 *   <li>New fields are implemented: {@link #JULIAN_DAY} defines
 *     single-field specification of the
 *     date. {@link #MILLISECONDS_IN_DAY} defines a single-field
 *     specification of the wall time. {@link #DOW_LOCAL} and
 *     {@link #YEAR_WOY} implement localized day-of-week and
 *     week-of-year behavior.</li>
 *
 *   <li>Subclasses can access protected millisecond constants
 *   defined in <code>Calendar</code>.</li>
 *
 *   <li>New API has been added to support calendar-specific subclasses
 *     of <code>DateFormat</code>.</li>
 *
 *   <li>Several subclasses have been implemented, representing
 *     various international calendar systems.</li>
 *
 * </ul>
 *
 * <p><big><b>Subclass API</b></big></p>
 *
 * <p>The original <code>Calendar</code> API was based on the experience
 * of implementing a only a single subclass,
 * <code>GregorianCalendar</code>. As a result, all of the subclassing
 * kinks had not been worked out. The new subclassing API has been
 * refined based on several implemented subclasses. This includes methods
 * that must be overridden and methods for subclasses to call. Subclasses
 * no longer have direct access to <code>fields</code> and
 * <code>stamp</code>. Instead, they have new API to access
 * these. Subclasses are able to allocate the <code>fields</code> array
 * through a protected framework method; this allows subclasses to
 * specify additional fields. </p>
 *
 * <p>More functionality has been moved into the base class. The base
 * class now contains much of the computational machinery to support the
 * Gregorian calendar. This is based on two things: (1) Many calendars
 * are based on the Gregorian calendar (such as the Buddhist and Japanese
 * imperial calendars). (2) <em>All</em> calendars require basic
 * Gregorian support in order to handle timezone computations. </p>
 *
 * <p>Common computations have been moved into
 * <code>Calendar</code>. Subclasses no longer compute the week related
 * fields and the time related fields. These are commonly handled for all
 * calendars by the base class. </p>
 *
 * <p><b>Subclass computation of time <tt>=&gt;</tt> fields</b>
 *
 * <p>The {@link #ERA}, {@link #YEAR},
 * {@link #EXTENDED_YEAR}, {@link #MONTH},
 * {@link #DAY_OF_MONTH}, and {@link #DAY_OF_YEAR} fields are
 * computed by the subclass, based on the Julian day. All other fields
 * are computed by <code>Calendar</code>.
 *
 * <ul>
 *
 *   <li>Subclasses should implement {@link #handleComputeFields}
 *     to compute the {@link #ERA}, {@link #YEAR},
 *     {@link #EXTENDED_YEAR}, {@link #MONTH},
 *     {@link #DAY_OF_MONTH}, and {@link #DAY_OF_YEAR} fields,
 *     based on the value of the {@link #JULIAN_DAY} field. If there
 *     are calendar-specific fields not defined by <code>Calendar</code>,
 *     they must also be computed. These are the only fields that the
 *     subclass should compute. All other fields are computed by the base
 *     class, so time and week fields behave in a consistent way across
 *     all calendars. The default version of this method in
 *     <code>Calendar</code> implements a proleptic Gregorian
 *     calendar. Within this method, subclasses may call
 *     <code>getGregorianXxx()</code> to obtain the Gregorian calendar
 *     month, day of month, and extended year for the given date.</li>
 *
 * </ul>
 *
 * <p><b>Subclass computation of fields <tt>=&gt;</tt> time</b>
 *
 * <p>The interpretation of most field values is handled entirely by
 * <code>Calendar</code>. <code>Calendar</code> determines which fields
 * are set, which are not, which are set more recently, and so on. In
 * addition, <code>Calendar</code> handles the computation of the time
 * from the time fields and handles the week-related fields. The only
 * thing the subclass must do is determine the extended year, based on
 * the year fields, and then, given an extended year and a month, it must
 * return a Julian day number.
 *
 * <ul>
 *
 *   <li>Subclasses should implement {@link #handleGetExtendedYear}
 *     to return the extended year for this calendar system, based on the
 *     {@link #YEAR}, {@link #EXTENDED_YEAR}, and any fields that
 *     the calendar system uses that are larger than a year, such as
 *     {@link #ERA}.</li>
 *
 *   <li>Subclasses should implement {@link #handleComputeMonthStart}
 *     to return the Julian day number
 *     associated with a month and extended year. This is the Julian day
 *     number of the day before the first day of the month. The month
 *     number is zero-based. This computation should not depend on any
 *     field values.</li>
 *
 * </ul>
 *
 * <p><b>Other methods</b>
 *
 * <ul>
 *
 *   <li>Subclasses should implement {@link #handleGetMonthLength}
 *     to return the number of days in a
 *     given month of a given extended year. The month number, as always,
 *     is zero-based.</li>
 *
 *   <li>Subclasses should implement {@link #handleGetYearLength}
 *     to return the number of days in the given
 *     extended year. This method is used by
 *     <tt>computeWeekFields</tt> to compute the
 *     {@link #WEEK_OF_YEAR} and {@link #YEAR_WOY} fields.</li>
 *
 *   <li>Subclasses should implement {@link #handleGetLimit}
 *     to return the protected values of a field, depending on the value of
 *     <code>limitType</code>. This method only needs to handle the
 *     fields {@link #ERA}, {@link #YEAR}, {@link #MONTH},
 *     {@link #WEEK_OF_YEAR}, {@link #WEEK_OF_MONTH},
 *     {@link #DAY_OF_MONTH}, {@link #DAY_OF_YEAR},
 *     {@link #DAY_OF_WEEK_IN_MONTH}, {@link #YEAR_WOY}, and
 *     {@link #EXTENDED_YEAR}.  Other fields are invariant (with
 *     respect to calendar system) and are handled by the base
 *     class.</li>
 *
 *   <li>Optionally, subclasses may override {@link #validateField}
 *     to check any subclass-specific fields. If the
 *     field's value is out of range, the method should throw an
 *     <code>IllegalArgumentException</code>. The method may call
 *     <code>super.validateField(field)</code> to handle fields in a
 *     generic way, that is, to compare them to the range
 *     <code>getMinimum(field)</code>..<code>getMaximum(field)</code>.</li>
 *
 *   <li>Optionally, subclasses may override
 *     {@link #handleCreateFields} to create an <code>int[]</code>
 *     array large enough to hold the calendar's fields. This is only
 *     necessary if the calendar defines additional fields beyond those
 *     defined by <code>Calendar</code>. The length of the result must be
 *     be between the base and maximum field counts.</li>
 *
 *   <li>Optionally, subclasses may override
 *     {@link #handleGetDateFormat} to create a
 *     <code>DateFormat</code> appropriate to this calendar. This is only
 *     required if a calendar subclass redefines the use of a field (for
 *     example, changes the {@link #ERA} field from a symbolic field
 *     to a numeric one) or defines an additional field.</li>
 *
 *   <li>Optionally, subclasses may override {@link #roll roll} and
 *     {@link #add add} to handle fields that are discontinuous. For
 *     example, in the Hebrew calendar the month &quot;Adar I&quot; only
 *     occurs in leap years; in other years the calendar jumps from
 *     Shevat (month #4) to Adar (month #6). The {@link
 *     HebrewCalendar#add HebrewCalendar.add} and {@link
 *     HebrewCalendar#roll HebrewCalendar.roll} methods take this into
 *     account, so that adding 1 month to Shevat gives the proper result
 *     (Adar) in a non-leap year. The protected utility method {@link
 *     #pinField pinField} is often useful when implementing these two
 *     methods. </li>
 *
 * </ul>
 *
 * <p><big><b>Normalized behavior</b></big>
 *
 * <p>The behavior of certain fields has been made consistent across all
 * calendar systems and implemented in <code>Calendar</code>.
 *
 * <ul>
 *
 *   <li>Time is normalized. Even though some calendar systems transition
 *     between days at sunset or at other times, all ICU4J calendars
 *     transition between days at <em>local zone midnight</em>.  This
 *     allows ICU4J to centralize the time computations in
 *     <code>Calendar</code> and to maintain basic correpsondences
 *     between calendar systems. Affected fields: {@link #AM_PM},
 *     {@link #HOUR}, {@link #HOUR_OF_DAY}, {@link #MINUTE},
 *     {@link #SECOND}, {@link #MILLISECOND},
 *     {@link #ZONE_OFFSET}, and {@link #DST_OFFSET}.</li>
 *
 *   <li>DST behavior is normalized. Daylight savings time behavior is
 *     computed the same for all calendar systems, and depends on the
 *     value of several <code>GregorianCalendar</code> fields: the
 *     {@link #YEAR}, {@link #MONTH}, and
 *     {@link #DAY_OF_MONTH}. As a result, <code>Calendar</code>
 *     always computes these fields, even for non-Gregorian calendar
 *     systems. These fields are available to subclasses.</li>
 *
 *   <li>Weeks are normalized. Although locales define the week
 *     differently, in terms of the day on which it starts, and the
 *     designation of week number one of a month or year, they all use a
 *     common mechanism. Furthermore, the day of the week has a simple
 *     and consistent definition throughout history. For example,
 *     although the Gregorian calendar introduced a discontinuity when
 *     first instituted, the day of week was not disrupted. For this
 *     reason, the fields {@link #DAY_OF_WEEK}, <code>WEEK_OF_YEAR,
 *     WEEK_OF_MONTH</code>, {@link #DAY_OF_WEEK_IN_MONTH},
 *     {@link #DOW_LOCAL}, {@link #YEAR_WOY} are all computed in
 *     a consistent way in the base class, based on the
 *     {@link #EXTENDED_YEAR}, {@link #DAY_OF_YEAR},
 *     {@link #MONTH}, and {@link #DAY_OF_MONTH}, which are
 *     computed by the subclass.</li>
 *
 * </ul>
 *
 * <p><big><b>Supported range</b></big>
 *
 * <p>The allowable range of <code>Calendar</code> has been
 * narrowed. <code>GregorianCalendar</code> used to attempt to support
 * the range of dates with millisecond values from
 * <code>Long.MIN_VALUE</code> to <code>Long.MAX_VALUE</code>. This
 * introduced awkward constructions (hacks) which slowed down
 * performance. It also introduced non-uniform behavior at the
 * boundaries. The new <code>Calendar</code> protocol specifies the
 * maximum range of supportable dates as those having Julian day numbers
 * of <code>-0x7F000000</code> to <code>+0x7F000000</code>. This
 * corresponds to years from ~5,800,000 BCE to ~5,800,000 CE. Programmers
 * should use the protected constants in <code>Calendar</code> to
 * specify an extremely early or extremely late date.</p>
 *
 * <p><big><b>General notes</b></big>
 *
 * <ul>
 *
 *   <li>Calendars implementations are <em>proleptic</em>. For example,
 *     even though the Gregorian calendar was not instituted until the
 *     16th century, the <code>GregorianCalendar</code> class supports
 *     dates before the historical onset of the calendar by extending the
 *     calendar system backward in time. Similarly, the
 *     <code>HebrewCalendar</code> extends backward before the start of
 *     its epoch into zero and negative years. Subclasses do not throw
 *     exceptions because a date precedes the historical start of a
 *     calendar system. Instead, they implement
 *     {@link #handleGetLimit} to return appropriate limits on
 *     {@link #YEAR}, {@link #ERA}, etc. fields. Then, if the
 *     calendar is set to not be lenient, out-of-range field values will
 *     trigger an exception.</li>
 *
 *   <li>Calendar system subclasses compute a <em>extended
 *     year</em>. This differs from the {@link #YEAR} field in that
 *     it ranges over all integer values, including zero and negative
 *     values, and it encapsulates the information of the
 *     {@link #YEAR} field and all larger fields.  Thus, for the
 *     Gregorian calendar, the {@link #EXTENDED_YEAR} is computed as
 *     <code>ERA==AD ? YEAR : 1-YEAR</code>. Another example is the Mayan
 *     long count, which has years (<code>KUN</code>) and nested cycles
 *     of years (<code>KATUN</code> and <code>BAKTUN</code>). The Mayan
 *     {@link #EXTENDED_YEAR} is computed as <code>TUN + 20 * (KATUN
 *     + 20 * BAKTUN)</code>. The <code>Calendar</code> base class uses
 *     the {@link #EXTENDED_YEAR} field to compute the week-related
 *     fields.</li>
 *
 * </ul>
 *
 * @see          Date
 * @see          GregorianCalendar
 * @see          TimeZone
 * @see          DateFormat
 * @author Mark Davis, Deborah Goldsmith, Chen-Lieh Huang, Alan Liu, Laura Werner
 */
public abstract class Calendar implements Serializable, Cloneable, Comparable<Calendar> {

    // Data flow in Calendar
    // ---------------------

    // The current time is represented in two ways by Calendar: as UTC
    // milliseconds from the epoch start (1 January 1970 0:00 UTC), and as local
    // fields such as MONTH, HOUR, AM_PM, etc.  It is possible to compute the
    // millis from the fields, and vice versa.  The data needed to do this
    // conversion is encapsulated by a TimeZone object owned by the Calendar.
    // The data provided by the TimeZone object may also be overridden if the
    // user sets the ZONE_OFFSET and/or DST_OFFSET fields directly. The class
    // keeps track of what information was most recently set by the caller, and
    // uses that to compute any other information as needed.

    // If the user sets the fields using set(), the data flow is as follows.
    // This is implemented by the Calendar subclass's computeTime() method.
    // During this process, certain fields may be ignored.  The disambiguation
    // algorithm for resolving which fields to pay attention to is described
    // above.

    //   local fields (YEAR, MONTH, DATE, HOUR, MINUTE, etc.)
    //           |
    //           | Using Calendar-specific algorithm
    //           V
    //   local standard millis
    //           |
    //           | Using TimeZone or user-set ZONE_OFFSET / DST_OFFSET
    //           V
    //   UTC millis (in time data member)

    // If the user sets the UTC millis using setTime(), the data flow is as
    // follows.  This is implemented by the Calendar subclass's computeFields()
    // method.

    //   UTC millis (in time data member)
    //           |
    //           | Using TimeZone getOffset()
    //           V
    //   local standard millis
    //           |
    //           | Using Calendar-specific algorithm
    //           V
    //   local fields (YEAR, MONTH, DATE, HOUR, MINUTE, etc.)

    // In general, a round trip from fields, through local and UTC millis, and
    // back out to fields is made when necessary.  This is implemented by the
    // complete() method.  Resolving a partial set of fields into a UTC millis
    // value allows all remaining fields to be generated from that value.  If
    // the Calendar is lenient, the fields are also renormalized to standard
    // ranges when they are regenerated.

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * era, e.g., AD or BC in the Julian calendar. This is a calendar-specific
     * value; see subclass documentation.
     * @see GregorianCalendar#AD
     * @see GregorianCalendar#BC
     */
    public final static int ERA = 0;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * year. This is a calendar-specific value; see subclass documentation.
     */
    public final static int YEAR = 1;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * month. This is a calendar-specific value. The first month of the year is
     * <code>JANUARY</code>; the last depends on the number of months in a year.
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
    public final static int MONTH = 2;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * week number within the current year.  The first week of the year, as
     * defined by {@link #getFirstDayOfWeek()} and
     * {@link #getMinimalDaysInFirstWeek()}, has value 1.  Subclasses define
     * the value of {@link #WEEK_OF_YEAR} for days before the first week of
     * the year.
     * @see #getFirstDayOfWeek
     * @see #getMinimalDaysInFirstWeek
     */
    public final static int WEEK_OF_YEAR = 3;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * week number within the current month.  The first week of the month, as
     * defined by {@link #getFirstDayOfWeek()} and
     * {@link #getMinimalDaysInFirstWeek()}, has value 1.  Subclasses define
     * the value of {@link #WEEK_OF_MONTH} for days before the first week of
     * the month.
     * @see #getFirstDayOfWeek
     * @see #getMinimalDaysInFirstWeek
     */
    public final static int WEEK_OF_MONTH = 4;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * day of the month. This is a synonym for {@link #DAY_OF_MONTH}.
     * The first day of the month has value 1.
     * @see #DAY_OF_MONTH
     */
    public final static int DATE = 5;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * day of the month. This is a synonym for {@link #DATE}.
     * The first day of the month has value 1.
     * @see #DATE
     */
    public final static int DAY_OF_MONTH = 5;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the day
     * number within the current year.  The first day of the year has value 1.
     */
    public final static int DAY_OF_YEAR = 6;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the day
     * of the week.  This field takes values {@link #SUNDAY},
     * {@link #MONDAY}, {@link #TUESDAY}, {@link #WEDNESDAY},
     * {@link #THURSDAY}, {@link #FRIDAY}, and {@link #SATURDAY}.
     * @see #SUNDAY
     * @see #MONDAY
     * @see #TUESDAY
     * @see #WEDNESDAY
     * @see #THURSDAY
     * @see #FRIDAY
     * @see #SATURDAY
     */
    public final static int DAY_OF_WEEK = 7;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * ordinal number of the day of the week within the current month. Together
     * with the {@link #DAY_OF_WEEK} field, this uniquely specifies a day
     * within a month.  Unlike {@link #WEEK_OF_MONTH} and
     * {@link #WEEK_OF_YEAR}, this field's value does <em>not</em> depend on
     * {@link #getFirstDayOfWeek()} or
     * {@link #getMinimalDaysInFirstWeek()}.  <code>DAY_OF_MONTH 1</code>
     * through <code>7</code> always correspond to <code>DAY_OF_WEEK_IN_MONTH
     * 1</code>; <code>8</code> through <code>15</code> correspond to
     * <code>DAY_OF_WEEK_IN_MONTH 2</code>, and so on.
     * <code>DAY_OF_WEEK_IN_MONTH 0</code> indicates the week before
     * <code>DAY_OF_WEEK_IN_MONTH 1</code>.  Negative values count back from the
     * end of the month, so the last Sunday of a month is specified as
     * <code>DAY_OF_WEEK = SUNDAY, DAY_OF_WEEK_IN_MONTH = -1</code>.  Because
     * negative values count backward they will usually be aligned differently
     * within the month than positive values.  For example, if a month has 31
     * days, <code>DAY_OF_WEEK_IN_MONTH -1</code> will overlap
     * <code>DAY_OF_WEEK_IN_MONTH 5</code> and the end of <code>4</code>.
     * @see #DAY_OF_WEEK
     * @see #WEEK_OF_MONTH
     */
    public final static int DAY_OF_WEEK_IN_MONTH = 8;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating
     * whether the <code>HOUR</code> is before or after noon.
     * E.g., at 10:04:15.250 PM the <code>AM_PM</code> is <code>PM</code>.
     * @see #AM
     * @see #PM
     * @see #HOUR
     */
    public final static int AM_PM = 9;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * hour of the morning or afternoon. <code>HOUR</code> is used for the 12-hour
     * clock.
     * E.g., at 10:04:15.250 PM the <code>HOUR</code> is 10.
     * @see #AM_PM
     * @see #HOUR_OF_DAY
     */
    public final static int HOUR = 10;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * hour of the day. <code>HOUR_OF_DAY</code> is used for the 24-hour clock.
     * E.g., at 10:04:15.250 PM the <code>HOUR_OF_DAY</code> is 22.
     * @see #HOUR
     */
    public final static int HOUR_OF_DAY = 11;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * minute within the hour.
     * E.g., at 10:04:15.250 PM the <code>MINUTE</code> is 4.
     */
    public final static int MINUTE = 12;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * second within the minute.
     * E.g., at 10:04:15.250 PM the <code>SECOND</code> is 15.
     */
    public final static int SECOND = 13;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * millisecond within the second.
     * E.g., at 10:04:15.250 PM the <code>MILLISECOND</code> is 250.
     */
    public final static int MILLISECOND = 14;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * raw offset from GMT in milliseconds.
     */
    public final static int ZONE_OFFSET = 15;

    /**
     * Field number for <code>get</code> and <code>set</code> indicating the
     * daylight savings offset in milliseconds.
     */
    public final static int DST_OFFSET = 16;

    /**
     * <strong>[icu]</strong> Field number for <code>get()</code> and <code>set()</code>
     * indicating the extended year corresponding to the
     * {@link #WEEK_OF_YEAR} field.  This may be one greater or less
     * than the value of {@link #EXTENDED_YEAR}.
     */
    public static final int YEAR_WOY = 17;

    /**
     * <strong>[icu]</strong> Field number for <code>get()</code> and <code>set()</code>
     * indicating the localized day of week.  This will be a value from 1
     * to 7 inclusive, with 1 being the localized first day of the week.
     */
    public static final int DOW_LOCAL = 18;

    /**
     * <strong>[icu]</strong> Field number for <code>get()</code> and <code>set()</code>
     * indicating the extended year.  This is a single number designating
     * the year of this calendar system, encompassing all supra-year
     * fields.  For example, for the Julian calendar system, year numbers
     * are positive, with an era of BCE or CE.  An extended year value for
     * the Julian calendar system assigns positive values to CE years and
     * negative values to BCE years, with 1 BCE being year 0.
     */
    public static final int EXTENDED_YEAR = 19;

    /**
     * <strong>[icu]</strong> Field number for <code>get()</code> and <code>set()</code>
     * indicating the modified Julian day number.  This is different from
     * the conventional Julian day number in two regards.  First, it
     * demarcates days at local zone midnight, rather than noon GMT.
     * Second, it is a local number; that is, it depends on the local time
     * zone.  It can be thought of as a single number that encompasses all
     * the date-related fields.
     */
    public static final int JULIAN_DAY = 20;

    /**
     * <strong>[icu]</strong> Field number for <code>get()</code> and <code>set()</code>
     * indicating the milliseconds in the day.  This ranges from 0 to
     * 23:59:59.999 (regardless of DST).  This field behaves
     * <em>exactly</em> like a composite of all time-related fields, not
     * including the zone fields.  As such, it also reflects
     * discontinuities of those fields on DST transition days.  On a day of
     * DST onset, it will jump forward.  On a day of DST cessation, it will
     * jump backward.  This reflects the fact that is must be combined with
     * the DST_OFFSET field to obtain a unique local time value.
     */
    public static final int MILLISECONDS_IN_DAY = 21;

    /**
     * <strong>[icu]</strong> Field indicating whether or not the current month is a leap month.
     * Should have a value of 0 for non-leap months, and 1 for leap months.
     */
    public static final int IS_LEAP_MONTH = 22;

    /**
     * The number of fields defined by this class.  Subclasses may define
     * addition fields starting with this number.
     * @deprecated ICU 58 The numeric value may change over time, see ICU ticket #12420.
     */
    @Deprecated
    protected static final int BASE_FIELD_COUNT = 23;

    /**
     * The maximum number of fields possible.  Subclasses must not define
     * more total fields than this number.
     * @deprecated ICU 58 The numeric value may change over time, see ICU ticket #12420.
     */
    @Deprecated
    protected static final int MAX_FIELD_COUNT = 32;

    /**
     * Value of the <code>DAY_OF_WEEK</code> field indicating
     * Sunday.
     */
    public final static int SUNDAY = 1;

    /**
     * Value of the <code>DAY_OF_WEEK</code> field indicating
     * Monday.
     */
    public final static int MONDAY = 2;

    /**
     * Value of the <code>DAY_OF_WEEK</code> field indicating
     * Tuesday.
     */
    public final static int TUESDAY = 3;

    /**
     * Value of the <code>DAY_OF_WEEK</code> field indicating
     * Wednesday.
     */
    public final static int WEDNESDAY = 4;

    /**
     * Value of the <code>DAY_OF_WEEK</code> field indicating
     * Thursday.
     */
    public final static int THURSDAY = 5;

    /**
     * Value of the <code>DAY_OF_WEEK</code> field indicating
     * Friday.
     */
    public final static int FRIDAY = 6;

    /**
     * Value of the <code>DAY_OF_WEEK</code> field indicating
     * Saturday.
     */
    public final static int SATURDAY = 7;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * first month of the year.
     */
    public final static int JANUARY = 0;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * second month of the year.
     */
    public final static int FEBRUARY = 1;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * third month of the year.
     */
    public final static int MARCH = 2;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * fourth month of the year.
     */
    public final static int APRIL = 3;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * fifth month of the year.
     */
    public final static int MAY = 4;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * sixth month of the year.
     */
    public final static int JUNE = 5;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * seventh month of the year.
     */
    public final static int JULY = 6;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * eighth month of the year.
     */
    public final static int AUGUST = 7;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * ninth month of the year.
     */
    public final static int SEPTEMBER = 8;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * tenth month of the year.
     */
    public final static int OCTOBER = 9;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * eleventh month of the year.
     */
    public final static int NOVEMBER = 10;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * twelfth month of the year.
     */
    public final static int DECEMBER = 11;

    /**
     * Value of the <code>MONTH</code> field indicating the
     * thirteenth month of the year. Although {@link GregorianCalendar}
     * does not use this value, lunar calendars do.
     */
    public final static int UNDECIMBER = 12;

    /**
     * Value of the <code>AM_PM</code> field indicating the
     * period of the day from midnight to just before noon.
     */
    public final static int AM = 0;

    /**
     * Value of the <code>AM_PM</code> field indicating the
     * period of the day from noon to just before midnight.
     */
    public final static int PM = 1;

    /**
     * <strong>[icu]</strong> Value returned by getDayOfWeekType(int dayOfWeek) to indicate a
     * weekday.
     * @see #WEEKEND
     * @see #WEEKEND_ONSET
     * @see #WEEKEND_CEASE
     * @see #getDayOfWeekType
     * @deprecated ICU 54 use {@link #getWeekDataForRegion(String)}, {@link #getWeekData()}, {@link #setWeekData(WeekData)}
     * @hide original deprecated declaration
     */
    @Deprecated
    public static final int WEEKDAY = 0;

    /**
     * <strong>[icu]</strong> Value returned by getDayOfWeekType(int dayOfWeek) to indicate a
     * weekend day.
     * @see #WEEKDAY
     * @see #WEEKEND_ONSET
     * @see #WEEKEND_CEASE
     * @see #getDayOfWeekType
     * @deprecated  ICU 54 use {@link #getWeekDataForRegion(String)}, {@link #getWeekData()}, {@link #setWeekData(WeekData)}
     * @hide original deprecated declaration
     */
    @Deprecated
    public static final int WEEKEND = 1;

    /**
     * <strong>[icu]</strong> Value returned by getDayOfWeekType(int dayOfWeek) to indicate a
     * day that starts as a weekday and transitions to the weekend.
     * Call getWeekendTransition() to get the point of transition.
     * @see #WEEKDAY
     * @see #WEEKEND
     * @see #WEEKEND_CEASE
     * @see #getDayOfWeekType
     * @deprecated ICU 54 use {@link #getWeekDataForRegion(String)}, {@link #getWeekData()}, {@link #setWeekData(WeekData)}
     * @hide original deprecated declaration
     */
    @Deprecated
    public static final int WEEKEND_ONSET = 2;

    /**
     * <strong>[icu]</strong> Value returned by getDayOfWeekType(int dayOfWeek) to indicate a
     * day that starts as the weekend and transitions to a weekday.
     * Call getWeekendTransition() to get the point of transition.
     * @see #WEEKDAY
     * @see #WEEKEND
     * @see #WEEKEND_ONSET
     * @see #getDayOfWeekType
     * @deprecated ICU 54 use {@link #getWeekDataForRegion(String)}, {@link #getWeekData()}, {@link #setWeekData(WeekData)}
     * @hide original deprecated declaration
     */
    @Deprecated
    public static final int WEEKEND_CEASE = 3;

    /**
     * <strong>[icu]</strong>Option used by {@link #setRepeatedWallTimeOption(int)} and
     * {@link #setSkippedWallTimeOption(int)} specifying an ambiguous wall time
     * to be interpreted as the latest.
     * @see #setRepeatedWallTimeOption(int)
     * @see #getRepeatedWallTimeOption()
     * @see #setSkippedWallTimeOption(int)
     * @see #getSkippedWallTimeOption()
     */
    public static final int WALLTIME_LAST = 0;

    /**
     * <strong>[icu]</strong>Option used by {@link #setRepeatedWallTimeOption(int)} and
     * {@link #setSkippedWallTimeOption(int)} specifying an ambiguous wall time
     * to be interpreted as the earliest.
     * @see #setRepeatedWallTimeOption(int)
     * @see #getRepeatedWallTimeOption()
     * @see #setSkippedWallTimeOption(int)
     * @see #getSkippedWallTimeOption()
     */
    public static final int WALLTIME_FIRST = 1;

    /**
     * <strong>[icu]</strong>Option used by {@link #setSkippedWallTimeOption(int)} specifying an
     * ambiguous wall time to be interpreted as the next valid wall time.
     * @see #setSkippedWallTimeOption(int)
     * @see #getSkippedWallTimeOption()
     */
    public static final int WALLTIME_NEXT_VALID = 2;

    /**
     * The number of milliseconds in one second.
     */
    protected static final int  ONE_SECOND = 1000;

    /**
     * The number of milliseconds in one minute.
     */
    protected static final int  ONE_MINUTE = 60*ONE_SECOND;

    /**
     * The number of milliseconds in one hour.
     */
    protected static final int  ONE_HOUR   = 60*ONE_MINUTE;

    /**
     * The number of milliseconds in one day.  Although ONE_DAY and
     * ONE_WEEK can fit into ints, they must be longs in order to prevent
     * arithmetic overflow when performing (bug 4173516).
     */
    protected static final long ONE_DAY    = 24*ONE_HOUR;

    /**
     * The number of milliseconds in one week.  Although ONE_DAY and
     * ONE_WEEK can fit into ints, they must be longs in order to prevent
     * arithmetic overflow when performing (bug 4173516).
     */
    protected static final long ONE_WEEK   = 7*ONE_DAY;

    /**
     * The Julian day of the Gregorian epoch, that is, January 1, 1 on the
     * Gregorian calendar.
     */
    protected static final int JAN_1_1_JULIAN_DAY = 1721426;

    /**
     * The Julian day of the epoch, that is, January 1, 1970 on the
     * Gregorian calendar.
     */
    protected static final int EPOCH_JULIAN_DAY   = 2440588;

    /**
     * The minimum supported Julian day.  This value is equivalent to
     * {@link #MIN_MILLIS} and {@link #MIN_DATE}.
     * @see #JULIAN_DAY
     */
    protected static final int MIN_JULIAN = -0x7F000000;

    /**
     * The minimum supported epoch milliseconds.  This value is equivalent
     * to {@link #MIN_JULIAN} and {@link #MIN_DATE}.
     */
    protected static final long MIN_MILLIS = -184303902528000000L;

    // Get around bug in jikes 1.12 for now.  Later, use:
    //protected static final long MIN_MILLIS = (MIN_JULIAN - EPOCH_JULIAN_DAY) * ONE_DAY;

    /**
     * The minimum supported <code>Date</code>.  This value is equivalent
     * to {@link #MIN_JULIAN} and {@link #MIN_MILLIS}.
     */
    protected static final Date MIN_DATE = new Date(MIN_MILLIS);

    /**
     * The maximum supported Julian day.  This value is equivalent to
     * {@link #MAX_MILLIS} and {@link #MAX_DATE}.
     * @see #JULIAN_DAY
     */
    protected static final int MAX_JULIAN = +0x7F000000;

    /**
     * The maximum supported epoch milliseconds.  This value is equivalent
     * to {@link #MAX_JULIAN} and {@link #MAX_DATE}.
     */
    protected static final long MAX_MILLIS = (MAX_JULIAN - EPOCH_JULIAN_DAY) * ONE_DAY;

    /**
     * The maximum supported <code>Date</code>.  This value is equivalent
     * to {@link #MAX_JULIAN} and {@link #MAX_MILLIS}.
     */
    protected static final Date MAX_DATE = new Date(MAX_MILLIS);

    // Internal notes:
    // Calendar contains two kinds of time representations: current "time" in
    // milliseconds, and a set of time "fields" representing the current time.
    // The two representations are usually in sync, but can get out of sync
    // as follows.
    // 1. Initially, no fields are set, and the time is invalid.
    // 2. If the time is set, all fields are computed and in sync.
    // 3. If a single field is set, the time is invalid.
    // Recomputation of the time and fields happens when the object needs
    // to return a result to the user, or use a result for a computation.

    /**
     * The field values for the currently set time for this calendar.
     * This is an array of at least {@link #BASE_FIELD_COUNT} integers.
     * @see #handleCreateFields
     * @serial
     */
    private transient int           fields[];

    /**
     * Pseudo-time-stamps which specify when each field was set. There
     * are two special values, UNSET and INTERNALLY_SET. Values from
     * MINIMUM_USER_SET to Integer.MAX_VALUE are legal user set values.
     */
    private transient int           stamp[];

    /**
     * The currently set time for this calendar, expressed in milliseconds after
     * January 1, 1970, 0:00:00 GMT.
     * @serial
     */
    private long          time;

    /**
     * True if then the value of <code>time</code> is valid.
     * The time is made invalid by a change to an item of <code>field[]</code>.
     * @see #time
     * @serial
     */
    private transient boolean       isTimeSet;

    /**
     * True if <code>fields[]</code> are in sync with the currently set time.
     * If false, then the next attempt to get the value of a field will
     * force a recomputation of all fields from the current value of
     * <code>time</code>.
     * @serial
     */
    private transient boolean       areFieldsSet;

    /**
     * True if all fields have been set.  This is only false in a few
     * situations: In a newly created, partially constructed object.  After
     * a call to clear().  In an object just read from a stream using
     * readObject().  Once computeFields() has been called this is set to
     * true and stays true until one of the above situations recurs.
     * @serial
     */
    private transient boolean       areAllFieldsSet;

    /**
     * True if all fields have been virtually set, but have not yet been
     * computed.  This occurs only in setTimeInMillis(), or after readObject().
     * A calendar set to this state will compute all fields from the time if it
     * becomes necessary, but otherwise will delay such computation.
     */
    private transient boolean areFieldsVirtuallySet;

    /**
     * True if this calendar allows out-of-range field values during computation
     * of <code>time</code> from <code>fields[]</code>.
     * @see #setLenient
     * @serial
     */
    private boolean         lenient = true;

    /**
     * The {@link TimeZone} used by this calendar. {@link Calendar}
     * uses the time zone data to translate between local and GMT time.
     * @serial
     */
    private TimeZone        zone;

    /**
     * The first day of the week, with possible values {@link #SUNDAY},
     * {@link #MONDAY}, etc.  This is a locale-dependent value.
     * @serial
     */
    private int             firstDayOfWeek;

    /**
     * The number of days required for the first week in a month or year,
     * with possible values from 1 to 7.  This is a locale-dependent value.
     * @serial
     */
    private int             minimalDaysInFirstWeek;

    /**
     * First day of the weekend in this calendar's locale.  Must be in
     * the range SUNDAY...SATURDAY (1..7).  The weekend starts at
     * weekendOnsetMillis milliseconds after midnight on that day of
     * the week.  This value is taken from locale resource data.
     */
    private int weekendOnset;

    /**
     * Milliseconds after midnight at which the weekend starts on the
     * day of the week weekendOnset.  Times that are greater than or
     * equal to weekendOnsetMillis are considered part of the weekend.
     * Must be in the range 0..24*60*60*1000-1.  This value is taken
     * from locale resource data.
     */
    private int weekendOnsetMillis;

    /**
     * Day of the week when the weekend stops in this calendar's
     * locale.  Must be in the range SUNDAY...SATURDAY (1..7).  The
     * weekend stops at weekendCeaseMillis milliseconds after midnight
     * on that day of the week.  This value is taken from locale
     * resource data.
     */
    private int weekendCease;

    /**
     * Milliseconds after midnight at which the weekend stops on the
     * day of the week weekendCease.  Times that are greater than or
     * equal to weekendCeaseMillis are considered not to be the
     * weekend.  Must be in the range 0..24*60*60*1000-1.  This value
     * is taken from locale resource data.
     */
    private int weekendCeaseMillis;

    /**
     * Option used when the specified wall time occurs multiple times.
     */
    private int repeatedWallTime = WALLTIME_LAST;

    /**
     * Option used when the specified wall time does not exist.
     */
    private int skippedWallTime = WALLTIME_LAST;

    /**
     * Value of the time stamp <code>stamp[]</code> indicating that
     * a field has not been set since the last call to <code>clear()</code>.
     * @see #INTERNALLY_SET
     * @see #MINIMUM_USER_STAMP
     */
    protected static final int UNSET = 0;

    /**
     * Value of the time stamp <code>stamp[]</code> indicating that a field
     * has been set via computations from the time or from other fields.
     * @see #UNSET
     * @see #MINIMUM_USER_STAMP
     */
    protected static final int INTERNALLY_SET = 1;

    /**
     * If the time stamp <code>stamp[]</code> has a value greater than or
     * equal to <code>MINIMUM_USER_SET</code> then it has been set by the
     * user via a call to <code>set()</code>.
     * @see #UNSET
     * @see #INTERNALLY_SET
     */
    protected static final int MINIMUM_USER_STAMP = 2;

    /**
     * The next available value for <code>stamp[]</code>, an internal array.
     * @serial
     */
    private transient int             nextStamp = MINIMUM_USER_STAMP;

    /* Max value for stamp allowable before recalcution */
    private static int STAMP_MAX = 10000;

    // the internal serial version which says which version was written
    // - 0 (default) for version up to JDK 1.1.5
    // - 1 for version from JDK 1.1.6, which writes a correct 'time' value
    //     as well as compatible values for other fields.  This is a
    //     transitional format.
    // - 2 (not implemented yet) a future version, in which fields[],
    //     areFieldsSet, and isTimeSet become transient, and isSet[] is
    //     removed. In JDK 1.1.6 we write a format compatible with version 2.
    // static final int        currentSerialVersion = 1;

    /**
     * The version of the serialized data on the stream.  Possible values:
     * <dl>
     * <dt><b>0</b> or not present on stream</dt>
     * <dd>
     * JDK 1.1.5 or earlier.
     * </dd>
     * <dt><b>1</b></dt>
     * <dd>
     * JDK 1.1.6 or later.  Writes a correct 'time' value
     * as well as compatible values for other fields.  This is a
     * transitional format.
     * </dd>
     * </dl>
     * When streaming out this class, the most recent format
     * and the highest allowable <code>serialVersionOnStream</code>
     * is written.
     * @serial
     */
    // private int             serialVersionOnStream = currentSerialVersion;

    // Proclaim serialization compatibility with JDK 1.1
    // static final long       serialVersionUID = -1807547505821590642L;

    // haven't been compatible for awhile, no longer try
    // jdk1.4.2 serialver
    private static final long serialVersionUID = 6222646104888790989L;

    /**
     * Bitmask for internalSet() defining which fields may legally be set
     * by subclasses.  Any attempt to set a field not in this bitmask
     * results in an exception, because such fields must be set by the base
     * class.
     */
    private transient int internalSetMask;

    /**
     * The Gregorian year, as computed by computeGregorianFields() and
     * returned by getGregorianYear().
     */
    private transient int gregorianYear;

    /**
     * The Gregorian month, as computed by computeGregorianFields() and
     * returned by getGregorianMonth().
     */
    private transient int gregorianMonth;

    /**
     * The Gregorian day of the year, as computed by
     * computeGregorianFields() and returned by getGregorianDayOfYear().
     */
    private transient int gregorianDayOfYear;

    /**
     * The Gregorian day of the month, as computed by
     * computeGregorianFields() and returned by getGregorianDayOfMonth().
     */
    private transient int gregorianDayOfMonth;

    /**
     * Constructs a Calendar with the default time zone
     * and the default <code>FORMAT</code> locale.
     * @see     TimeZone#getDefault
     * @see Category#FORMAT
     */
    protected Calendar()
    {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a calendar with the specified time zone and locale.
     * @param zone the time zone to use
     * @param aLocale the locale for the week data
     */
    protected Calendar(TimeZone zone, Locale aLocale)
    {
        this(zone, ULocale.forLocale(aLocale));
    }

    /**
     * Constructs a calendar with the specified time zone and locale.
     * @param zone the time zone to use
     * @param locale the ulocale for the week data
     */
    protected Calendar(TimeZone zone, ULocale locale)
    {
        this.zone = zone;

        // week data
        setWeekData(getRegionForCalendar(locale));

        // set valid/actual locale
        setCalendarLocale(locale);

        initInternal();
    }

    /*
     * Set valid/actual locale to this calendar during initialization.
     *
     * Valid or actual locale does not make much sense for Calendar
     * object. An instance of Calendar is initialized by week data
     * determine by region and calendar type (either region or keyword).
     * Language is not really used for calendar creation.
     */
    private void setCalendarLocale(ULocale locale) {
        ULocale calLocale = locale;

        if (locale.getVariant().length() != 0 || locale.getKeywords() != null) {
            // Construct a ULocale, without variant and keywords (except calendar).
            StringBuilder buf = new StringBuilder();

            buf.append(locale.getLanguage());

            String script = locale.getScript();
            if (script.length() > 0) {
                buf.append("_").append(script);
            }

            String region = locale.getCountry();
            if (region.length() > 0) {
                buf.append("_").append(region);
            }

            String calType = locale.getKeywordValue("calendar");
            if (calType != null) {
                buf.append("@calendar=").append(calType);
            }

            calLocale = new ULocale(buf.toString());
        }

        setLocale(calLocale, calLocale);
    }

    private void recalculateStamp() {
        int index;
        int currentValue;
        int j, i;

        nextStamp = 1;

        for (j = 0; j < stamp.length; j++) {
            currentValue = STAMP_MAX;
            index = -1;

            for (i = 0; i < stamp.length; i++) {
                if (stamp[i] > nextStamp && stamp[i] < currentValue) {
                    currentValue = stamp[i];
                    index = i;
                }
            }

            if (index >= 0) {
                stamp[index] = ++nextStamp;
            } else {
                break;
            }
        }
        nextStamp++;
    }

    private void initInternal()
    {
        // Allocate fields through the framework method.  Subclasses
        // may override this to define additional fields.
        fields = handleCreateFields();
        ///CLOVER:OFF
        // todo: fix, difficult to test without subclassing
        if (fields == null || fields.length < BASE_FIELD_COUNT ||
                fields.length > MAX_FIELD_COUNT) {
            throw new IllegalStateException("Invalid fields[]");
        }
        ///CLOVER:ON
        stamp = new int[fields.length];
        int mask = (1 << ERA) |
                (1 << YEAR) |
                (1 << MONTH) |
                (1 << DAY_OF_MONTH) |
                (1 << DAY_OF_YEAR) |
                (1 << EXTENDED_YEAR) |
                (1 << IS_LEAP_MONTH);
        for (int i=BASE_FIELD_COUNT; i<fields.length; ++i) {
            mask |= (1 << i);
        }
        internalSetMask = mask;
    }

    /**
     * Returns a calendar using the default time zone and locale.
     * @return a Calendar.
     */
    public static Calendar getInstance()
    {
        return getInstanceInternal(null, null);
    }

    /**
     * Returns a calendar using the specified time zone and default locale.
     * @param zone the time zone to use
     * @return a Calendar.
     */
    public static Calendar getInstance(TimeZone zone)
    {
        return getInstanceInternal(zone, null);
    }

    /**
     * Returns a calendar using the default time zone and specified locale.
     * @param aLocale the locale for the week data
     * @return a Calendar.
     */
    public static Calendar getInstance(Locale aLocale)
    {
        return getInstanceInternal(null, ULocale.forLocale(aLocale));
    }

    /**
     * Returns a calendar using the default time zone and specified locale.
     * @param locale the ulocale for the week data
     * @return a Calendar.
     */
    public static Calendar getInstance(ULocale locale)
    {
        return getInstanceInternal(null, locale);
    }

    /**
     * Returns a calendar with the specified time zone and locale.
     * @param zone the time zone to use
     * @param aLocale the locale for the week data
     * @return a Calendar.
     */
    public static Calendar getInstance(TimeZone zone, Locale aLocale) {
        return getInstanceInternal(zone, ULocale.forLocale(aLocale));
    }

    /**
     * Returns a calendar with the specified time zone and locale.
     * @param zone the time zone to use
     * @param locale the ulocale for the week data
     * @return a Calendar.
     */
    public static Calendar getInstance(TimeZone zone, ULocale locale) {
        return getInstanceInternal(zone, locale);
    }

    /*
     * All getInstance implementations call this private method to create a new
     * Calendar instance.
     */
    private static Calendar getInstanceInternal(TimeZone tz, ULocale locale) {
        if (locale == null) {
            locale = ULocale.getDefault(Category.FORMAT);
        }
        if (tz == null) {
            tz = TimeZone.getDefault();
        }

        Calendar cal = createInstance(locale);
        cal.setTimeZone(tz);
        cal.setTimeInMillis(System.currentTimeMillis());
        return cal;
    }

    private static String getRegionForCalendar(ULocale loc) {
        String region = ULocale.getRegionForSupplementalData(loc, true);
        if (region.length() == 0) {
            region = "001";
        }
        return region;
    }

    private enum CalType {
        GREGORIAN("gregorian"),
        ISO8601("iso8601"),

        BUDDHIST("buddhist"),
        CHINESE("chinese"),
        COPTIC("coptic"),
        DANGI("dangi"),
        ETHIOPIC("ethiopic"),
        ETHIOPIC_AMETE_ALEM("ethiopic-amete-alem"),
        HEBREW("hebrew"),
        INDIAN("indian"),
        ISLAMIC("islamic"),
        ISLAMIC_CIVIL("islamic-civil"),
        ISLAMIC_RGSA("islamic-rgsa"),
        ISLAMIC_TBLA("islamic-tbla"),
        ISLAMIC_UMALQURA("islamic-umalqura"),
        JAPANESE("japanese"),
        PERSIAN("persian"),
        ROC("roc"),

        UNKNOWN("unknown");

        String id;

        CalType(String id) {
            this.id = id;
        }
    }

    private static CalType getCalendarTypeForLocale(ULocale l) {
        String s = CalendarUtil.getCalendarType(l);
        if (s != null) {
            s = s.toLowerCase(Locale.ENGLISH);
            for (CalType type : CalType.values()) {
                if (s.equals(type.id)) {
                    return type;
                }
            }
        }
        return CalType.UNKNOWN;
    }

    private static Calendar createInstance(ULocale locale) {
        Calendar cal = null;
        TimeZone zone = TimeZone.getDefault();
        CalType calType = getCalendarTypeForLocale(locale);
        if (calType == CalType.UNKNOWN) {
            // fallback to Gregorian
            calType = CalType.GREGORIAN;
        }

        switch (calType) {
        case GREGORIAN:
            cal = new GregorianCalendar(zone, locale);
            break;
        case ISO8601:
            // Only differs week numbering rule from Gregorian
            cal = new GregorianCalendar(zone, locale);
            cal.setFirstDayOfWeek(MONDAY);
            cal.setMinimalDaysInFirstWeek(4);
            break;

        /* J2ObjC removed: Only "gregorian" and "julian" calendars are supported.
        case BUDDHIST:
            cal = new BuddhistCalendar(zone, locale);
            break;
        case CHINESE:
            cal = new ChineseCalendar(zone, locale);
            break;
        case COPTIC:
            cal = new CopticCalendar(zone, locale);
            break;
        case DANGI:
            cal = new DangiCalendar(zone, locale);
            break;
        case ETHIOPIC:
            cal = new EthiopicCalendar(zone, locale);
            break;
        case ETHIOPIC_AMETE_ALEM:
            cal = new EthiopicCalendar(zone, locale);
            ((EthiopicCalendar)cal).setAmeteAlemEra(true);
            break;
        case HEBREW:
            cal = new HebrewCalendar(zone, locale);
            break;
        case INDIAN:
            cal = new IndianCalendar(zone, locale);
            break;
        case ISLAMIC_CIVIL:
        case ISLAMIC_UMALQURA :
        case ISLAMIC_TBLA:
        case ISLAMIC_RGSA:
        case ISLAMIC:
            cal = new IslamicCalendar(zone, locale);
            break;
        case JAPANESE:
            cal = new JapaneseCalendar(zone, locale);
            break;
        case PERSIAN:
            cal = new PersianCalendar(zone, locale);
            break;
        case ROC:
            cal = new TaiwanCalendar(zone, locale);
            break; */

        default:
            // we must not get here, because unknown type is mapped to
            // Gregorian at the beginning of this method.
            throw new IllegalArgumentException("Unknown calendar type");
        }

        return cal;
    }

    /**
     * Returns the list of locales for which Calendars are installed.
     * @return the list of locales for which Calendars are installed.
     */
    public static Locale[] getAvailableLocales()
    {
        // TODO
        return ICUResourceBundle.getAvailableLocales();
    }

    /**
     * <strong>[icu]</strong> Returns the list of locales for which Calendars are installed.
     * @return the list of locales for which Calendars are installed.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static ULocale[] getAvailableULocales()
    {
        // TODO
        return ICUResourceBundle.getAvailableULocales();
    }

    /**
     * <strong>[icu]</strong> Given a key and a locale, returns an array of string values in a preferred
     * order that would make a difference. These are all and only those values where
     * the open (creation) of the service with the locale formed from the input locale
     * plus input keyword and that value has different behavior than creation with the
     * input locale alone.
     * @param key           one of the keys supported by this service.  For now, only
     *                      "calendar" is supported.
     * @param locale        the locale
     * @param commonlyUsed  if set to true it will return only commonly used values
     *                      with the given locale in preferred order.  Otherwise,
     *                      it will return all the available values for the locale.
     * @return an array of string values for the given key and the locale.
     */
    public static final String[] getKeywordValuesForLocale(String key, ULocale locale,
            boolean commonlyUsed) {
        // Resolve region
        String prefRegion = ULocale.getRegionForSupplementalData(locale, true);

        // Read preferred calendar values from supplementalData calendarPreferences
        ArrayList<String> values = new ArrayList<String>();

        UResourceBundle rb = UResourceBundle.getBundleInstance(
                ICUData.ICU_BASE_NAME,
                "supplementalData",
                ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        UResourceBundle calPref = rb.get("calendarPreferenceData");
        UResourceBundle order = null;
        try {
            order = calPref.get(prefRegion);
        } catch (MissingResourceException mre) {
            // use "001" as fallback
            order = calPref.get("001");
        }

        String[] caltypes = order.getStringArray();
        if (commonlyUsed) {
            // we have all commonly used calendar for the target region
            return caltypes;
        }

        // if not commonlyUsed, add all preferred calendars in the order
        for (int i = 0; i < caltypes.length; i++) {
            values.add(caltypes[i]);
        }
        // then, add other available clanedars
        for (CalType t : CalType.values()) {
            if (!values.contains(t.id)) {
                values.add(t.id);
            }
        }
        return values.toArray(new String[values.size()]);
    }

    /**
     * Returns this Calendar's current time.
     * @return the current time.
     */
    public final Date getTime() {
        return new Date( getTimeInMillis() );
    }

    /**
     * Sets this Calendar's current time with the given Date.
     *
     * <p>Note: Calling <code>setTime</code> with
     * <code>Date(Long.MAX_VALUE)</code> or <code>Date(Long.MIN_VALUE)</code>
     * may yield incorrect field values from {@link #get(int)}.
     * @param date the given Date.
     */
    public final void setTime(Date date) {
        setTimeInMillis( date.getTime() );
    }

    /**
     * Returns this Calendar's current time as a long.
     * @return the current time as UTC milliseconds from the epoch.
     */
    public long getTimeInMillis() {
        if (!isTimeSet) updateTime();
        return time;
    }

    /**
     * Sets this Calendar's current time from the given long value.
     * An IllegalIcuArgumentException is thrown when millis is outside the range permitted
     * by a Calendar object when in strict mode.
     * When in lenient mode the out of range values are pinned to their respective min/max.
     * @param millis the new time in UTC milliseconds from the epoch.
     */
    public void setTimeInMillis( long millis ) {
        if (millis > MAX_MILLIS) {
            if(isLenient()) {
                millis = MAX_MILLIS;
            } else {
                throw new IllegalArgumentException("millis value greater than upper bounds for a Calendar : " + millis);
            }
        } else if (millis < MIN_MILLIS) {
            if(isLenient()) {
                millis = MIN_MILLIS;
            } else {
                throw new IllegalArgumentException("millis value less than lower bounds for a Calendar : " + millis);
            }
        }
        time = millis;
        areFieldsSet = areAllFieldsSet = false;
        isTimeSet = areFieldsVirtuallySet = true;

        for (int i=0; i<fields.length; ++i) {
            fields[i] = stamp[i] = 0; // UNSET == 0
        }

    }

    /**
     * Returns the value for a given time field.
     * @param field the given time field.
     * @return the value for the given time field.
     */
    public final int get(int field)
    {
        complete();
        return fields[field];
    }

    /**
     * Returns the value for a given time field.  This is an internal method
     * for subclasses that does <em>not</em> trigger any calculations.
     * @param field the given time field.
     * @return the value for the given time field.
     */
    protected final int internalGet(int field)
    {
        return fields[field];
    }

    /**
     * Returns the value for a given time field, or return the given default
     * value if the field is not set.  This is an internal method for
     * subclasses that does <em>not</em> trigger any calculations.
     * @param field the given time field.
     * @param defaultValue value to return if field is not set
     * @return the value for the given time field of defaultValue if the
     * field is unset
     */
    protected final int internalGet(int field, int defaultValue) {
        return (stamp[field] > UNSET) ? fields[field] : defaultValue;
    }

    /**
     * Sets the time field with the given value.
     * @param field the given time field.
     * @param value the value to be set for the given time field.
     */
    public final void set(int field, int value)
    {
        if (areFieldsVirtuallySet) {
            computeFields();
        }
        fields[field] = value;
        /* Ensure that the fNextStamp value doesn't go pass max value for 32 bit integer */
        if (nextStamp == STAMP_MAX) {
            recalculateStamp();
        }
        stamp[field] = nextStamp++;
        isTimeSet = areFieldsSet = areFieldsVirtuallySet = false;
    }

    /**
     * Sets the values for the fields year, month, and date.
     * Previous values of other fields are retained.  If this is not desired,
     * call {@link #clear()} first.
     * @param year the value used to set the YEAR time field.
     * @param month the value used to set the MONTH time field.
     * Month value is 0-based. e.g., 0 for January.
     * @param date the value used to set the DATE time field.
     */
    public final void set(int year, int month, int date)
    {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
    }

    /**
     * Sets the values for the fields year, month, date, hour, and minute.
     * Previous values of other fields are retained.  If this is not desired,
     * call {@link #clear()} first.
     * @param year the value used to set the YEAR time field.
     * @param month the value used to set the MONTH time field.
     * Month value is 0-based. e.g., 0 for January.
     * @param date the value used to set the DATE time field.
     * @param hour the value used to set the HOUR_OF_DAY time field.
     * @param minute the value used to set the MINUTE time field.
     */
    public final void set(int year, int month, int date, int hour, int minute)
    {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
    }

    /**
     * Sets the values for the fields year, month, date, hour, minute, and second.
     * Previous values of other fields are retained.  If this is not desired,
     * call {@link #clear} first.
     * @param year the value used to set the YEAR time field.
     * @param month the value used to set the MONTH time field.
     * Month value is 0-based. e.g., 0 for January.
     * @param date the value used to set the DATE time field.
     * @param hour the value used to set the HOUR_OF_DAY time field.
     * @param minute the value used to set the MINUTE time field.
     * @param second the value used to set the SECOND time field.
     */
    public final void set(int year, int month, int date, int hour, int minute,
            int second)
    {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
        set(SECOND, second);
    }

    // -------------------------------------
    // For now the full getRelatedYear implementation is here;
    // per #10752 move the non-default implementation to subclasses
    // (default implementation will do no year adjustment)

    /**
     * utility function for getRelatedYear
     */
    private static int gregoYearFromIslamicStart(int year) {
        // ad hoc conversion, improve under #10752
        // rough est for now, ok for grego 1846-2138,
        // otherwise occasionally wrong (for 3% of years)
        int cycle, offset, shift = 0;
        if (year >= 1397) {
            cycle = (year - 1397) / 67;
            offset = (year - 1397) % 67;
            shift = 2*cycle + ((offset >= 33)? 1: 0);
        } else {
            cycle = (year - 1396) / 67 - 1;
            offset = -(year - 1396) % 67;
            shift = 2*cycle + ((offset <= 33)? 1: 0);
        }
        return year + 579 - shift;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public final int getRelatedYear() {
        int year = get(EXTENDED_YEAR);
        CalType type = CalType.GREGORIAN;
        String typeString = getType();
        for (CalType testType : CalType.values()) {
            if (typeString.equals(testType.id)) {
                type = testType;
                break;
            }
        }
        switch (type) {
            case PERSIAN:
                year += 622; break;
            case HEBREW:
                year -= 3760; break;
            case CHINESE:
                year -= 2637; break;
            case INDIAN:
                year += 79; break;
            case COPTIC:
                year += 284; break;
            case ETHIOPIC:
                year += 8; break;
            case ETHIOPIC_AMETE_ALEM:
                year -=5492; break;
            case DANGI:
                year -= 2333; break;
            case ISLAMIC_CIVIL:
            case ISLAMIC:
            case ISLAMIC_UMALQURA:
            case ISLAMIC_TBLA:
            case ISLAMIC_RGSA:
                year = gregoYearFromIslamicStart(year); break;
            // case GREGORIAN:
            // case JAPANESE:
            // case BUDDHIST:
            // case ROC:
            // case ISO8601:
            default:
                // do nothing, EXTENDED_YEAR same as Gregorian
                break;
        }
        return year;
    }

    // -------------------------------------
    // For now the full setRelatedYear implementation is here;
    // per #10752 move the non-default implementation to subclasses
    // (default implementation will do no year adjustment)

    /**
     * utility function for setRelatedYear
     */
    private static int firstIslamicStartYearFromGrego(int year) {
        // ad hoc conversion, improve under #10752
        // rough est for now, ok for grego 1846-2138,
        // otherwise occasionally wrong (for 3% of years)
        int cycle, offset, shift = 0;
        if (year >= 1977) {
            cycle = (year - 1977) / 65;
            offset = (year - 1977) % 65;
            shift = 2*cycle + ((offset >= 32)? 1: 0);
        } else {
            cycle = (year - 1976) / 65 - 1;
            offset = -(year - 1976) % 65;
            shift = 2*cycle + ((offset <= 32)? 1: 0);
        }
        return year - 579 + shift;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public final void setRelatedYear(int year) {
        CalType type = CalType.GREGORIAN;
        String typeString = getType();
        for (CalType testType : CalType.values()) {
            if (typeString.equals(testType.id)) {
                type = testType;
                break;
            }
        }
        switch (type) {
            case PERSIAN:
                year -= 622; break;
            case HEBREW:
                year += 3760; break;
            case CHINESE:
                year += 2637; break;
            case INDIAN:
                year -= 79; break;
            case COPTIC:
                year -= 284; break;
            case ETHIOPIC:
                year -= 8; break;
            case ETHIOPIC_AMETE_ALEM:
                year +=5492; break;
            case DANGI:
                year += 2333; break;
            case ISLAMIC_CIVIL:
            case ISLAMIC:
            case ISLAMIC_UMALQURA:
            case ISLAMIC_TBLA:
            case ISLAMIC_RGSA:
                year = firstIslamicStartYearFromGrego(year); break;
            // case GREGORIAN:
            // case JAPANESE:
            // case BUDDHIST:
            // case ROC:
            // case ISO8601:
            default:
                // do nothing, EXTENDED_YEAR same as Gregorian
                break;
        }
        set(EXTENDED_YEAR, year);
    }

    /**
     * Clears the values of all the time fields.
     */
    public final void clear()
    {
        for (int i=0; i<fields.length; ++i) {
            fields[i] = stamp[i] = 0; // UNSET == 0
        }
        isTimeSet = areFieldsSet = areAllFieldsSet = areFieldsVirtuallySet = false;
    }

    /**
     * Clears the value in the given time field.
     * @param field the time field to be cleared.
     */
    public final void clear(int field)
    {
        if (areFieldsVirtuallySet) {
            computeFields();
        }
        fields[field] = 0;
        stamp[field] = UNSET;
        isTimeSet = areFieldsSet = areAllFieldsSet = areFieldsVirtuallySet = false;
    }

    /**
     * Determines if the given time field has a value set.
     * @return true if the given time field has a value set; false otherwise.
     */
    public final boolean isSet(int field)
    {
        return areFieldsVirtuallySet || (stamp[field] != UNSET);
    }

    /**
     * Fills in any unset fields in the time field list.
     */
    protected void complete()
    {
        if (!isTimeSet) updateTime();
        if (!areFieldsSet) {
            computeFields(); // fills in unset fields
            areFieldsSet = true;
            areAllFieldsSet = true;
        }
    }

    /**
     * Compares this calendar to the specified object.
     * The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and is a <code>Calendar</code> object that
     * represents the same calendar as this object.
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }

        Calendar that = (Calendar) obj;

        return isEquivalentTo(that) &&
                getTimeInMillis() == that.getTime().getTime();
    }

    /**
     * <strong>[icu]</strong> Returns true if the given Calendar object is equivalent to this
     * one.  An equivalent Calendar will behave exactly as this one
     * does, but it may be set to a different time.  By contrast, for
     * the equals() method to return true, the other Calendar must
     * be set to the same time.
     *
     * @param other the Calendar to be compared with this Calendar
     */
    public boolean isEquivalentTo(Calendar other) {
        return this.getClass() == other.getClass() &&
                isLenient() == other.isLenient() &&
                getFirstDayOfWeek() == other.getFirstDayOfWeek() &&
                getMinimalDaysInFirstWeek() == other.getMinimalDaysInFirstWeek() &&
                getTimeZone().equals(other.getTimeZone()) &&
                getRepeatedWallTimeOption() == other.getRepeatedWallTimeOption() &&
                getSkippedWallTimeOption() == other.getSkippedWallTimeOption();
    }

    /**
     * Returns a hash code for this calendar.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        /* Don't include the time because (a) we don't want the hash value to
         * move around just because a calendar is set to different times, and
         * (b) we don't want to trigger a time computation just to get a hash.
         * Note that it is not necessary for unequal objects to always have
         * unequal hashes, but equal objects must have equal hashes.  */
        return (lenient ? 1 : 0)
                | (firstDayOfWeek << 1)
                | (minimalDaysInFirstWeek << 4)
                | (repeatedWallTime << 7)
                | (skippedWallTime << 9)
                | (zone.hashCode() << 11);
    }

    /**
     * Returns the difference in milliseconds between the moment this
     * calendar is set to and the moment the given calendar or Date object
     * is set to.
     */
    private long compare(Object that) {
        long thatMs;
        if (that instanceof Calendar) {
            thatMs = ((Calendar)that).getTimeInMillis();
        } else if (that instanceof Date) {
            thatMs = ((Date)that).getTime();
        } else {
            throw new IllegalArgumentException(that + "is not a Calendar or Date");
        }
        return getTimeInMillis() - thatMs;
    }

    /**
     * Compares the time field records.
     * Equivalent to comparing result of conversion to UTC.
     * @param when the Calendar to be compared with this Calendar.
     * @return true if the current time of this Calendar is before
     * the time of Calendar when; false otherwise.
     */
    public boolean before(Object when) {
        return compare(when) < 0;
    }

    /**
     * Compares the time field records.
     * Equivalent to comparing result of conversion to UTC.
     * @param when the Calendar to be compared with this Calendar.
     * @return true if the current time of this Calendar is after
     * the time of Calendar when; false otherwise.
     */
    public boolean after(Object when) {
        return compare(when) > 0;
    }

    /**
     * Returns the maximum value that this field could have, given the
     * current date.  For example, with the Gregorian date February 3, 1997
     * and the {@link #DAY_OF_MONTH DAY_OF_MONTH} field, the actual maximum
     * is 28; for February 3, 1996 it is 29.
     *
     * <p>The actual maximum computation ignores smaller fields and the
     * current value of like-sized fields.  For example, the actual maximum
     * of the DAY_OF_YEAR or MONTH depends only on the year and supra-year
     * fields.  The actual maximum of the DAY_OF_MONTH depends, in
     * addition, on the MONTH field and any other fields at that
     * granularity (such as IS_LEAP_MONTH).  The
     * DAY_OF_WEEK_IN_MONTH field does not depend on the current
     * DAY_OF_WEEK; it returns the maximum for any day of week in the
     * current month.  Likewise for the WEEK_OF_MONTH and WEEK_OF_YEAR
     * fields.
     *
     * @param field the field whose maximum is desired
     * @return the maximum of the given field for the current date of this calendar
     * @see #getMaximum
     * @see #getLeastMaximum
     */
    public int getActualMaximum(int field) {
        int result;

        switch (field) {
        case DAY_OF_MONTH:
        {
            Calendar cal = (Calendar) clone();
            cal.setLenient(true);
            cal.prepareGetActual(field, false);
            result = handleGetMonthLength(cal.get(EXTENDED_YEAR), cal.get(MONTH));
        }
        break;

        case DAY_OF_YEAR:
        {
            Calendar cal = (Calendar) clone();
            cal.setLenient(true);
            cal.prepareGetActual(field, false);
            result = handleGetYearLength(cal.get(EXTENDED_YEAR));
        }
        break;

        case ERA:
        case DAY_OF_WEEK:
        case AM_PM:
        case HOUR:
        case HOUR_OF_DAY:
        case MINUTE:
        case SECOND:
        case MILLISECOND:
        case ZONE_OFFSET:
        case DST_OFFSET:
        case DOW_LOCAL:
        case JULIAN_DAY:
        case MILLISECONDS_IN_DAY:
            // These fields all have fixed minima/maxima
            result = getMaximum(field);
            break;

        default:
            // For all other fields, do it the hard way....
            result = getActualHelper(field, getLeastMaximum(field), getMaximum(field));
            break;
        }
        return result;
    }

    /**
     * Returns the minimum value that this field could have, given the current date.
     * For most fields, this is the same as {@link #getMinimum getMinimum}
     * and {@link #getGreatestMinimum getGreatestMinimum}.  However, some fields,
     * especially those related to week number, are more complicated.
     * <p>
     * For example, assume {@link #getMinimalDaysInFirstWeek getMinimalDaysInFirstWeek}
     * returns 4 and {@link #getFirstDayOfWeek getFirstDayOfWeek} returns SUNDAY.
     * If the first day of the month is Sunday, Monday, Tuesday, or Wednesday
     * there will be four or more days in the first week, so it will be week number 1,
     * and <code>getActualMinimum(WEEK_OF_MONTH)</code> will return 1.  However,
     * if the first of the month is a Thursday, Friday, or Saturday, there are
     * <em>not</em> four days in that week, so it is week number 0, and
     * <code>getActualMinimum(WEEK_OF_MONTH)</code> will return 0.
     * <p>
     * @param field the field whose actual minimum value is desired.
     * @return the minimum of the given field for the current date of this calendar
     *
     * @see #getMinimum
     * @see #getGreatestMinimum
     */
    public int getActualMinimum(int field) {
        int result;

        switch (field) {
        case DAY_OF_WEEK:
        case AM_PM:
        case HOUR:
        case HOUR_OF_DAY:
        case MINUTE:
        case SECOND:
        case MILLISECOND:
        case ZONE_OFFSET:
        case DST_OFFSET:
        case DOW_LOCAL:
        case JULIAN_DAY:
        case MILLISECONDS_IN_DAY:
            // These fields all have fixed minima/maxima
            result = getMinimum(field);
            break;

        default:
            // For all other fields, do it the hard way....
            result = getActualHelper(field, getGreatestMinimum(field), getMinimum(field));
            break;
        }
        return result;
    }

    /**
     * Prepare this calendar for computing the actual minimum or maximum.
     * This method modifies this calendar's fields; it is called on a
     * temporary calendar.
     *
     * <p>Rationale: The semantics of getActualXxx() is to return the
     * maximum or minimum value that the given field can take, taking into
     * account other relevant fields.  In general these other fields are
     * larger fields.  For example, when computing the actual maximum
     * DAY_OF_MONTH, the current value of DAY_OF_MONTH itself is ignored,
     * as is the value of any field smaller.
     *
     * <p>The time fields all have fixed minima and maxima, so we don't
     * need to worry about them.  This also lets us set the
     * MILLISECONDS_IN_DAY to zero to erase any effects the time fields
     * might have when computing date fields.
     *
     * <p>DAY_OF_WEEK is adjusted specially for the WEEK_OF_MONTH and
     * WEEK_OF_YEAR fields to ensure that they are computed correctly.
     */
    protected void prepareGetActual(int field, boolean isMinimum) {
        set(MILLISECONDS_IN_DAY, 0);

        switch (field) {
        case YEAR:
        case EXTENDED_YEAR:
            set(DAY_OF_YEAR, getGreatestMinimum(DAY_OF_YEAR));
            break;

        case YEAR_WOY:
            set(WEEK_OF_YEAR, getGreatestMinimum(WEEK_OF_YEAR));
            break;

        case MONTH:
            set(DAY_OF_MONTH, getGreatestMinimum(DAY_OF_MONTH));
            break;

        case DAY_OF_WEEK_IN_MONTH:
            // For dowim, the maximum occurs for the DOW of the first of the
            // month.
            set(DAY_OF_MONTH, 1);
            set(DAY_OF_WEEK, get(DAY_OF_WEEK)); // Make this user set
            break;

        case WEEK_OF_MONTH:
        case WEEK_OF_YEAR:
            // If we're counting weeks, set the day of the week to either the
            // first or last localized DOW.  We know the last week of a month
            // or year will contain the first day of the week, and that the
            // first week will contain the last DOW.
        {
            int dow = firstDayOfWeek;
            if (isMinimum) {
                dow = (dow + 6) % 7; // set to last DOW
                if (dow < SUNDAY) {
                    dow += 7;
                }
            }
            set(DAY_OF_WEEK, dow);
        }
        break;
        }

        // Do this last to give it the newest time stamp
        set(field, getGreatestMinimum(field));
    }

    private int getActualHelper(int field, int startValue, int endValue) {

        if (startValue == endValue) {
            // if we know that the maximum value is always the same, just return it
            return startValue;
        }

        final int delta = (endValue > startValue) ? 1 : -1;

        // clone the calendar so we don't mess with the real one, and set it to
        // accept anything for the field values
        Calendar work = (Calendar) clone();

        // need to resolve time here, otherwise, fields set for actual limit
        // may cause conflict with fields previously set (but not yet resolved).
        work.complete();

        work.setLenient(true);
        work.prepareGetActual(field, delta < 0);

        // now try each value from the start to the end one by one until
        // we get a value that normalizes to another value.  The last value that
        // normalizes to itself is the actual maximum for the current date

        work.set(field, startValue);
        // prepareGetActual sets the first day of week in the same week with
        // the first day of a month.  Unlike WEEK_OF_YEAR, week number for the
        // which week contains days from both previous and current month is
        // not unique.  For example, last several days in the previous month
        // is week 5, and the rest of week is week 1.
        if (work.get(field) != startValue
                && field != WEEK_OF_MONTH && delta > 0) {
            return startValue;
        }
        int result = startValue;
        do {
            startValue += delta;
            work.add(field, delta);
            if (work.get(field) != startValue) {
                break;
            }
            result = startValue;
        } while (startValue != endValue);

        return result;
    }

    /**
     * Rolls (up/down) a single unit of time on the given field.  If the
     * field is rolled past its maximum allowable value, it will "wrap" back
     * to its minimum and continue rolling. For
     * example, to roll the current date up by one day, you can call:
     * <p>
     * <code>roll({@link #DATE}, true)</code>
     * <p>
     * When rolling on the {@link #YEAR} field, it will roll the year
     * value in the range between 1 and the value returned by calling
     * {@link #getMaximum getMaximum}({@link #YEAR}).
     * <p>
     * When rolling on certain fields, the values of other fields may conflict and
     * need to be changed.  For example, when rolling the <code>MONTH</code> field
     * for the Gregorian date 1/31/96 upward, the <code>DAY_OF_MONTH</code> field
     * must be adjusted so that the result is 2/29/96 rather than the invalid
     * 2/31/96.
     * <p>
     * Rolling up always means rolling forward in time (unless
     * the limit of the field is reached, in which case it may pin or wrap), so for the
     * Gregorian calendar, starting with 100 BC and rolling the year up results in 99 BC.
     * When eras have a definite beginning and end (as in the Chinese calendar, or as in
     * most eras in the Japanese calendar) then rolling the year past either limit of the
     * era will cause the year to wrap around. When eras only have a limit at one end,
     * then attempting to roll the year past that limit will result in pinning the year
     * at that limit. Note that for most calendars in which era 0 years move forward in
     * time (such as Buddhist, Hebrew, or Islamic), it is possible for add or roll to
     * result in negative years for era 0 (that is the only way to represent years before
     * the calendar epoch in such calendars).
     * <p>
     * <b>Note:</b> Calling <tt>roll(field, true)</tt> N times is <em>not</em>
     * necessarily equivalent to calling <tt>roll(field, N)</tt>.  For example,
     * imagine that you start with the date Gregorian date January 31, 1995.  If you call
     * <tt>roll(Calendar.MONTH, 2)</tt>, the result will be March 31, 1995.
     * But if you call <tt>roll(Calendar.MONTH, true)</tt>, the result will be
     * February 28, 1995.  Calling it one more time will give March 28, 1995, which
     * is usually not the desired result.
     * <p>
     * <b>Note:</b> You should always use <tt>roll</tt> and <tt>add</tt> rather
     * than attempting to perform arithmetic operations directly on the fields
     * of a <tt>Calendar</tt>.  It is quite possible for <tt>Calendar</tt> subclasses
     * to have fields with non-linear behavior, for example missing months
     * or days during non-leap years.  The subclasses' <tt>add</tt> and <tt>roll</tt>
     * methods will take this into account, while simple arithmetic manipulations
     * may give invalid results.
     * <p>
     * @param field the calendar field to roll.
     *
     * @param up    indicates if the value of the specified time field is to be
     *              rolled up or rolled down. Use <code>true</code> if rolling up,
     *              <code>false</code> otherwise.
     *
     * @exception   IllegalArgumentException if the field is invalid or refers
     *              to a field that cannot be handled by this method.
     * @see #roll(int, int)
     * @see #add
     */
    public final void roll(int field, boolean up)
    {
        roll(field, up ? +1 : -1);
    }

    /**
     * Rolls (up/down) a specified amount time on the given field.  For
     * example, to roll the current date up by three days, you can call
     * <code>roll(Calendar.DATE, 3)</code>.  If the
     * field is rolled past its maximum allowable value, it will "wrap" back
     * to its minimum and continue rolling.
     * For example, calling <code>roll(Calendar.DATE, 10)</code>
     * on a Gregorian calendar set to 4/25/96 will result in the date 4/5/96.
     * <p>
     * When rolling on certain fields, the values of other fields may conflict and
     * need to be changed.  For example, when rolling the {@link #MONTH MONTH} field
     * for the Gregorian date 1/31/96 by +1, the {@link #DAY_OF_MONTH DAY_OF_MONTH} field
     * must be adjusted so that the result is 2/29/96 rather than the invalid
     * 2/31/96.
     * <p>
     * Rolling by a positive value always means rolling forward in time (unless
     * the limit of the field is reached, in which case it may pin or wrap), so for the
     * Gregorian calendar, starting with 100 BC and rolling the year by + 1 results in 99 BC.
     * When eras have a definite beginning and end (as in the Chinese calendar, or as in
     * most eras in the Japanese calendar) then rolling the year past either limit of the
     * era will cause the year to wrap around. When eras only have a limit at one end,
     * then attempting to roll the year past that limit will result in pinning the year
     * at that limit. Note that for most calendars in which era 0 years move forward in
     * time (such as Buddhist, Hebrew, or Islamic), it is possible for add or roll to
     * result in negative years for era 0 (that is the only way to represent years before
     * the calendar epoch in such calendars).
     * <p>
     * <strong>[icu] Note:</strong> the ICU implementation of this method is able to roll
     * all fields except for {@link #ERA ERA}, {@link #DST_OFFSET DST_OFFSET},
     * and {@link #ZONE_OFFSET ZONE_OFFSET}.  Subclasses may, of course, add support for
     * additional fields in their overrides of <code>roll</code>.
     * <p>
     * <b>Note:</b> You should always use <tt>roll</tt> and <tt>add</tt> rather
     * than attempting to perform arithmetic operations directly on the fields
     * of a <tt>Calendar</tt>.  It is quite possible for <tt>Calendar</tt> subclasses
     * to have fields with non-linear behavior, for example missing months
     * or days during non-leap years.  The subclasses' <tt>add</tt> and <tt>roll</tt>
     * methods will take this into account, while simple arithmetic manipulations
     * may give invalid results.
     * <p>
     * <b>Subclassing:</b><br>
     * This implementation of <code>roll</code> assumes that the behavior of the
     * field is continuous between its minimum and maximum, which are found by
     * calling {@link #getActualMinimum getActualMinimum} and {@link #getActualMaximum getActualMaximum}.
     * For most such fields, simple addition, subtraction, and modulus operations
     * are sufficient to perform the roll.  For week-related fields,
     * the results of {@link #getFirstDayOfWeek getFirstDayOfWeek} and
     * {@link #getMinimalDaysInFirstWeek getMinimalDaysInFirstWeek} are also necessary.
     * Subclasses can override these two methods if their values differ from the defaults.
     * <p>
     * Subclasses that have fields for which the assumption of continuity breaks
     * down must overide <code>roll</code> to handle those fields specially.
     * For example, in the Hebrew calendar the month "Adar I"
     * only occurs in leap years; in other years the calendar jumps from
     * Shevat (month #4) to Adar (month #6).  The
     * {@link HebrewCalendar#roll HebrewCalendar.roll} method takes this into account,
     * so that rolling the month of Shevat by one gives the proper result (Adar) in a
     * non-leap year.
     * <p>
     * @param field     the calendar field to roll.
     * @param amount    the amount by which the field should be rolled.
     *
     * @exception   IllegalArgumentException if the field is invalid or refers
     *              to a field that cannot be handled by this method.
     * @see #roll(int, boolean)
     * @see #add
     */
    public void roll(int field, int amount) {

        if (amount == 0) {
            return; // Nothing to do
        }

        complete();

        switch (field) {
        case DAY_OF_MONTH:
        case AM_PM:
        case MINUTE:
        case SECOND:
        case MILLISECOND:
        case MILLISECONDS_IN_DAY:
        case ERA:
            // These are the standard roll instructions.  These work for all
            // simple cases, that is, cases in which the limits are fixed, such
            // as the hour, the day of the month, and the era.
        {
            int min = getActualMinimum(field);
            int max = getActualMaximum(field);
            int gap = max - min + 1;

            int value = internalGet(field) + amount;
            value = (value - min) % gap;
            if (value < 0) {
                value += gap;
            }
            value += min;

            set(field, value);
            return;
        }

        case HOUR:
        case HOUR_OF_DAY:
            // Rolling the hour is difficult on the ONSET and CEASE days of
            // daylight savings.  For example, if the change occurs at
            // 2 AM, we have the following progression:
            // ONSET: 12 Std -> 1 Std -> 3 Dst -> 4 Dst
            // CEASE: 12 Dst -> 1 Dst -> 1 Std -> 2 Std
            // To get around this problem we don't use fields; we manipulate
            // the time in millis directly.
        {
            // Assume min == 0 in calculations below
            long start = getTimeInMillis();
            int oldHour = internalGet(field);
            int max = getMaximum(field);
            int newHour = (oldHour + amount) % (max + 1);
            if (newHour < 0) {
                newHour += max + 1;
            }
            setTimeInMillis(start + ONE_HOUR * ((long)newHour - oldHour));
            return;
        }

        case MONTH:
            // Rolling the month involves both pinning the final value
            // and adjusting the DAY_OF_MONTH if necessary.  We only adjust the
            // DAY_OF_MONTH if, after updating the MONTH field, it is illegal.
            // E.g., <jan31>.roll(MONTH, 1) -> <feb28> or <feb29>.
        {
            int max = getActualMaximum(MONTH);
            int mon = (internalGet(MONTH) + amount) % (max+1);

            if (mon < 0) {
                mon += (max + 1);
            }
            set(MONTH, mon);

            // Keep the day of month in range.  We don't want to spill over
            // into the next month; e.g., we don't want jan31 + 1 mo -> feb31 ->
            // mar3.
            pinField(DAY_OF_MONTH);
            return;
        }

        case YEAR:
        case YEAR_WOY:
            // * If era==0 and years go backwards in time, change sign of amount.
            // * Until we have new API per #9393, we temporarily hardcode knowledge of
            //   which calendars have era 0 years that go backwards.
        {
            boolean era0WithYearsThatGoBackwards = false;
            int era = get(ERA);
            if (era == 0) {
                String calType = getType();
                if (calType.equals("gregorian") || calType.equals("roc") || calType.equals("coptic")) {
                    amount = -amount;
                    era0WithYearsThatGoBackwards = true;
                }
            }
            int newYear = internalGet(field) + amount;
            if (era > 0 || newYear >= 1) {
                int maxYear = getActualMaximum(field);
                if (maxYear < 32768) {
                    // this era has real bounds, roll should wrap years
                    if (newYear < 1) {
                        newYear = maxYear - ((-newYear) % maxYear);
                    } else if (newYear > maxYear) {
                        newYear = ((newYear - 1) % maxYear) + 1;
                    }
                    // else era is unbounded, just pin low year instead of wrapping
                } else if (newYear < 1) {
                    newYear = 1;
                }
                // else we are in era 0 with newYear < 1;
                // calendars with years that go backwards must pin the year value at 0,
                // other calendars can have years < 0 in era 0
            } else if (era0WithYearsThatGoBackwards) {
                newYear = 1;
            }
            set(field, newYear);
            pinField(MONTH);
            pinField(DAY_OF_MONTH);
            return;
        }
        case EXTENDED_YEAR:
            // Rolling the year can involve pinning the DAY_OF_MONTH.
            set(field, internalGet(field) + amount);
            pinField(MONTH);
            pinField(DAY_OF_MONTH);
            return;

        case WEEK_OF_MONTH:
        {
            // This is tricky, because during the roll we may have to shift
            // to a different day of the week.  For example:

            //    s  m  t  w  r  f  s
            //          1  2  3  4  5
            //    6  7  8  9 10 11 12

            // When rolling from the 6th or 7th back one week, we go to the
            // 1st (assuming that the first partial week counts).  The same
            // thing happens at the end of the month.

            // The other tricky thing is that we have to figure out whether
            // the first partial week actually counts or not, based on the
            // minimal first days in the week.  And we have to use the
            // correct first day of the week to delineate the week
            // boundaries.

            // Here's our algorithm.  First, we find the real boundaries of
            // the month.  Then we discard the first partial week if it
            // doesn't count in this locale.  Then we fill in the ends with
            // phantom days, so that the first partial week and the last
            // partial week are full weeks.  We then have a nice square
            // block of weeks.  We do the usual rolling within this block,
            // as is done elsewhere in this method.  If we wind up on one of
            // the phantom days that we added, we recognize this and pin to
            // the first or the last day of the month.  Easy, eh?

            // Normalize the DAY_OF_WEEK so that 0 is the first day of the week
            // in this locale.  We have dow in 0..6.
            int dow = internalGet(DAY_OF_WEEK) - getFirstDayOfWeek();
            if (dow < 0) dow += 7;

            // Find the day of the week (normalized for locale) for the first
            // of the month.
            int fdm = (dow - internalGet(DAY_OF_MONTH) + 1) % 7;
            if (fdm < 0) fdm += 7;

            // Get the first day of the first full week of the month,
            // including phantom days, if any.  Figure out if the first week
            // counts or not; if it counts, then fill in phantom days.  If
            // not, advance to the first real full week (skip the partial week).
            int start;
            if ((7 - fdm) < getMinimalDaysInFirstWeek())
                start = 8 - fdm; // Skip the first partial week
            else
                start = 1 - fdm; // This may be zero or negative

            // Get the day of the week (normalized for locale) for the last
            // day of the month.
            int monthLen = getActualMaximum(DAY_OF_MONTH);
            int ldm = (monthLen - internalGet(DAY_OF_MONTH) + dow) % 7;
            // We know monthLen >= DAY_OF_MONTH so we skip the += 7 step here.

            // Get the limit day for the blocked-off rectangular month; that
            // is, the day which is one past the last day of the month,
            // after the month has already been filled in with phantom days
            // to fill out the last week.  This day has a normalized DOW of 0.
            int limit = monthLen + 7 - ldm;

            // Now roll between start and (limit - 1).
            int gap = limit - start;
            int day_of_month = (internalGet(DAY_OF_MONTH) + amount*7 -
                    start) % gap;
            if (day_of_month < 0) day_of_month += gap;
            day_of_month += start;

            // Finally, pin to the real start and end of the month.
            if (day_of_month < 1) day_of_month = 1;
            if (day_of_month > monthLen) day_of_month = monthLen;

            // Set the DAY_OF_MONTH.  We rely on the fact that this field
            // takes precedence over everything else (since all other fields
            // are also set at this point).  If this fact changes (if the
            // disambiguation algorithm changes) then we will have to unset
            // the appropriate fields here so that DAY_OF_MONTH is attended
            // to.
            set(DAY_OF_MONTH, day_of_month);
            return;
        }
        case WEEK_OF_YEAR:
        {
            // This follows the outline of WEEK_OF_MONTH, except it applies
            // to the whole year.  Please see the comment for WEEK_OF_MONTH
            // for general notes.

            // Normalize the DAY_OF_WEEK so that 0 is the first day of the week
            // in this locale.  We have dow in 0..6.
            int dow = internalGet(DAY_OF_WEEK) - getFirstDayOfWeek();
            if (dow < 0) dow += 7;

            // Find the day of the week (normalized for locale) for the first
            // of the year.
            int fdy = (dow - internalGet(DAY_OF_YEAR) + 1) % 7;
            if (fdy < 0) fdy += 7;

            // Get the first day of the first full week of the year,
            // including phantom days, if any.  Figure out if the first week
            // counts or not; if it counts, then fill in phantom days.  If
            // not, advance to the first real full week (skip the partial week).
            int start;
            if ((7 - fdy) < getMinimalDaysInFirstWeek())
                start = 8 - fdy; // Skip the first partial week
            else
                start = 1 - fdy; // This may be zero or negative

            // Get the day of the week (normalized for locale) for the last
            // day of the year.
            int yearLen = getActualMaximum(DAY_OF_YEAR);
            int ldy = (yearLen - internalGet(DAY_OF_YEAR) + dow) % 7;
            // We know yearLen >= DAY_OF_YEAR so we skip the += 7 step here.

            // Get the limit day for the blocked-off rectangular year; that
            // is, the day which is one past the last day of the year,
            // after the year has already been filled in with phantom days
            // to fill out the last week.  This day has a normalized DOW of 0.
            int limit = yearLen + 7 - ldy;

            // Now roll between start and (limit - 1).
            int gap = limit - start;
            int day_of_year = (internalGet(DAY_OF_YEAR) + amount*7 -
                    start) % gap;
            if (day_of_year < 0) day_of_year += gap;
            day_of_year += start;

            // Finally, pin to the real start and end of the month.
            if (day_of_year < 1) day_of_year = 1;
            if (day_of_year > yearLen) day_of_year = yearLen;

            // Make sure that the year and day of year are attended to by
            // clearing other fields which would normally take precedence.
            // If the disambiguation algorithm is changed, this section will
            // have to be updated as well.
            set(DAY_OF_YEAR, day_of_year);
            clear(MONTH);
            return;
        }
        case DAY_OF_YEAR:
        {
            // Roll the day of year using millis.  Compute the millis for
            // the start of the year, and get the length of the year.
            long delta = amount * ONE_DAY; // Scale up from days to millis
            long min2 = time - (internalGet(DAY_OF_YEAR) - 1) * ONE_DAY;
            int yearLength = getActualMaximum(DAY_OF_YEAR);
            time = (time + delta - min2) % (yearLength*ONE_DAY);
            if (time < 0) time += yearLength*ONE_DAY;
            setTimeInMillis(time + min2);
            return;
        }
        case DAY_OF_WEEK:
        case DOW_LOCAL:
        {
            // Roll the day of week using millis.  Compute the millis for
            // the start of the week, using the first day of week setting.
            // Restrict the millis to [start, start+7days).
            long delta = amount * ONE_DAY; // Scale up from days to millis
            // Compute the number of days before the current day in this
            // week.  This will be a value 0..6.
            int leadDays = internalGet(field);
            leadDays -= (field == DAY_OF_WEEK) ? getFirstDayOfWeek() : 1;
            if (leadDays < 0) leadDays += 7;
            long min2 = time - leadDays * ONE_DAY;
            time = (time + delta - min2) % ONE_WEEK;
            if (time < 0) time += ONE_WEEK;
            setTimeInMillis(time + min2);
            return;
        }
        case DAY_OF_WEEK_IN_MONTH:
        {
            // Roll the day of week in the month using millis.  Determine
            // the first day of the week in the month, and then the last,
            // and then roll within that range.
            long delta = amount * ONE_WEEK; // Scale up from weeks to millis
            // Find the number of same days of the week before this one
            // in this month.
            int preWeeks = (internalGet(DAY_OF_MONTH) - 1) / 7;
            // Find the number of same days of the week after this one
            // in this month.
            int postWeeks = (getActualMaximum(DAY_OF_MONTH) -
                    internalGet(DAY_OF_MONTH)) / 7;
            // From these compute the min and gap millis for rolling.
            long min2 = time - preWeeks * ONE_WEEK;
            long gap2 = ONE_WEEK * (preWeeks + postWeeks + 1); // Must add 1!
            // Roll within this range
            time = (time + delta - min2) % gap2;
            if (time < 0) time += gap2;
            setTimeInMillis(time + min2);
            return;
        }
        case JULIAN_DAY:
            set(field, internalGet(field) + amount);
            return;
        default:
            // Other fields cannot be rolled by this method
            throw new IllegalArgumentException("Calendar.roll(" + fieldName(field) +
                    ") not supported");
        }
    }

    /**
     * Add a signed amount to a specified field, using this calendar's rules.
     * For example, to add three days to the current date, you can call
     * <code>add(Calendar.DATE, 3)</code>.
     * <p>
     * When adding to certain fields, the values of other fields may conflict and
     * need to be changed.  For example, when adding one to the {@link #MONTH MONTH} field
     * for the Gregorian date 1/31/96, the {@link #DAY_OF_MONTH DAY_OF_MONTH} field
     * must be adjusted so that the result is 2/29/96 rather than the invalid
     * 2/31/96.
     * <p>
     * Adding a positive value always means moving forward in time, so for the Gregorian
     * calendar, starting with 100 BC and adding +1 to year results in 99 BC (even though
     * this actually reduces the numeric value of the field itself).
     * <p>
     * <strong>[icu] Note:</strong> The ICU implementation of this method is able to add to
     * all fields except for {@link #ERA ERA}, {@link #DST_OFFSET DST_OFFSET},
     * and {@link #ZONE_OFFSET ZONE_OFFSET}.  Subclasses may, of course, add support for
     * additional fields in their overrides of <code>add</code>.
     * <p>
     * <b>Note:</b> You should always use <tt>roll</tt> and <tt>add</tt> rather
     * than attempting to perform arithmetic operations directly on the fields
     * of a <tt>Calendar</tt>.  It is quite possible for <tt>Calendar</tt> subclasses
     * to have fields with non-linear behavior, for example missing months
     * or days during non-leap years.  The subclasses' <tt>add</tt> and <tt>roll</tt>
     * methods will take this into account, while simple arithmetic manipulations
     * may give invalid results.
     * <p>
     * <b>Subclassing:</b><br>
     * This implementation of <code>add</code> assumes that the behavior of the
     * field is continuous between its minimum and maximum, which are found by
     * calling {@link #getActualMinimum getActualMinimum} and
     * {@link #getActualMaximum getActualMaximum}.
     * For such fields, simple arithmetic operations are sufficient to
     * perform the add.
     * <p>
     * Subclasses that have fields for which this assumption of continuity breaks
     * down must overide <code>add</code> to handle those fields specially.
     * For example, in the Hebrew calendar the month "Adar I"
     * only occurs in leap years; in other years the calendar jumps from
     * Shevat (month #4) to Adar (month #6).  The
     * {@link HebrewCalendar#add HebrewCalendar.add} method takes this into account,
     * so that adding one month
     * to a date in Shevat gives the proper result (Adar) in a non-leap year.
     * <p>
     * @param field     the time field.
     * @param amount    the amount to add to the field.
     *
     * @exception   IllegalArgumentException if the field is invalid or refers
     *              to a field that cannot be handled by this method.
     * @see #roll(int, int)
     */
    @SuppressWarnings("fallthrough")
    public void add(int field, int amount) {

        if (amount == 0) {
            return;   // Do nothing!
        }

        // We handle most fields in the same way.  The algorithm is to add
        // a computed amount of millis to the current millis.  The only
        // wrinkle is with DST (and/or a change to the zone's UTC offset, which
        // we'll include with DST) -- for some fields, like the DAY_OF_MONTH,
        // we don't want the wall time to shift due to changes in DST.  If the
        // result of the add operation is to move from DST to Standard, or
        // vice versa, we need to adjust by an hour forward or back,
        // respectively.  For such fields we set keepWallTimeInvariant to true.

        // We only adjust the DST for fields larger than an hour.  For
        // fields smaller than an hour, we cannot adjust for DST without
        // causing problems.  for instance, if you add one hour to April 5,
        // 1998, 1:00 AM, in PST, the time becomes "2:00 AM PDT" (an
        // illegal value), but then the adjustment sees the change and
        // compensates by subtracting an hour.  As a result the time
        // doesn't advance at all.

        // For some fields larger than a day, such as a MONTH, we pin the
        // DAY_OF_MONTH.  This allows <March 31>.add(MONTH, 1) to be
        // <April 30>, rather than <April 31> => <May 1>.

        long delta = amount; // delta in ms
        boolean keepWallTimeInvariant = true;

        switch (field) {
        case ERA:
            set(field, get(field) + amount);
            pinField(ERA);
            return;

        case YEAR:
        case YEAR_WOY:
            // * If era=0 and years go backwards in time, change sign of amount.
            // * Until we have new API per #9393, we temporarily hardcode knowledge of
            //   which calendars have era 0 years that go backwards.
            // * Note that for YEAR (but not YEAR_WOY) we could instead handle
            //   this by applying the amount to the EXTENDED_YEAR field; but since
            //   we would still need to handle YEAR_WOY as below, might as well
            //   also handle YEAR the same way.
        {
            int era = get(ERA);
            if (era == 0) {
                String calType = getType();
                if (calType.equals("gregorian") || calType.equals("roc") || calType.equals("coptic")) {
                    amount = -amount;
                }
            }
        }
        // Fall through into standard handling
        case EXTENDED_YEAR:
        case MONTH:
        {
            boolean oldLenient = isLenient();
            setLenient(true);
            set(field, get(field) + amount);
            pinField(DAY_OF_MONTH);
            if(oldLenient==false) {
                complete();
                setLenient(oldLenient);
            }
        }
        return;

        case WEEK_OF_YEAR:
        case WEEK_OF_MONTH:
        case DAY_OF_WEEK_IN_MONTH:
            delta *= ONE_WEEK;
            break;

        case AM_PM:
            delta *= 12 * ONE_HOUR;
            break;

        case DAY_OF_MONTH:
        case DAY_OF_YEAR:
        case DAY_OF_WEEK:
        case DOW_LOCAL:
        case JULIAN_DAY:
            delta *= ONE_DAY;
            break;

        case HOUR_OF_DAY:
        case HOUR:
            delta *= ONE_HOUR;
            keepWallTimeInvariant = false;
            break;

        case MINUTE:
            delta *= ONE_MINUTE;
            keepWallTimeInvariant = false;
            break;

        case SECOND:
            delta *= ONE_SECOND;
            keepWallTimeInvariant = false;
            break;

        case MILLISECOND:
        case MILLISECONDS_IN_DAY:
            keepWallTimeInvariant = false;
            break;

        default:
            throw new IllegalArgumentException("Calendar.add(" + fieldName(field) +
                    ") not supported");
        }

        // In order to keep the wall time invariant (for fields where this is
        // appropriate), check the combined DST & ZONE offset before and
        // after the add() operation. If it changes, then adjust the millis
        // to compensate.
        int prevOffset = 0;
        int prevWallTime = 0;
        if (keepWallTimeInvariant) {
            prevOffset = get(DST_OFFSET) + get(ZONE_OFFSET);
            prevWallTime = get(MILLISECONDS_IN_DAY);
        }

        setTimeInMillis(getTimeInMillis() + delta);

        if (keepWallTimeInvariant) {
            int newWallTime = get(MILLISECONDS_IN_DAY);
            if (newWallTime != prevWallTime) {
                // There is at least one zone transition between the base
                // time and the result time. As the result, wall time has
                // changed.
                long t = internalGetTimeInMillis();
                int newOffset = get(DST_OFFSET) + get(ZONE_OFFSET);
                if (newOffset != prevOffset) {
                    // When the difference of the previous UTC offset and
                    // the new UTC offset exceeds 1 full day, we do not want
                    // to roll over/back the date. For now, this only happens
                    // in Samoa (Pacific/Apia) on Dec 30, 2011. See ticket:9452.
                    long adjAmount = (prevOffset - newOffset) % ONE_DAY;
                    if (adjAmount != 0) {
                        setTimeInMillis(t + adjAmount);
                        newWallTime = get(MILLISECONDS_IN_DAY);
                    }
                    if (newWallTime != prevWallTime) {
                        // The result wall time or adjusted wall time was shifted because
                        // the target wall time does not exist on the result date.
                        switch (skippedWallTime) {
                        case WALLTIME_FIRST:
                            if (adjAmount > 0) {
                                setTimeInMillis(t);
                            }
                            break;
                        case WALLTIME_LAST:
                            if (adjAmount < 0) {
                                setTimeInMillis(t);
                            }
                            break;
                        case WALLTIME_NEXT_VALID:
                            long tmpT = adjAmount > 0 ? internalGetTimeInMillis() : t;
                            Long immediatePrevTrans = getImmediatePreviousZoneTransition(tmpT);
                            if (immediatePrevTrans != null) {
                                setTimeInMillis(immediatePrevTrans);
                            } else {
                                throw new RuntimeException("Could not locate a time zone transition before " + tmpT);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the name of this calendar in the language of the given locale.
     */
    public String getDisplayName(Locale loc) {
        return this.getClass().getName();
    }

    /**
     * Returns the name of this calendar in the language of the given locale.
     */
    public String getDisplayName(ULocale loc) {
        return this.getClass().getName();
    }

    /**
     * Compares the times (in millis) represented by two
     * <code>Calendar</code> objects.
     *
     * @param that the <code>Calendar</code> to compare to this.
     * @return <code>0</code> if the time represented by
     * this <code>Calendar</code> is equal to the time represented
     * by that <code>Calendar</code>, a value less than
     * <code>0</code> if the time represented by this is before
     * the time represented by that, and a value greater than
     * <code>0</code> if the time represented by this
     * is after the time represented by that.
     * @throws NullPointerException if that
     * <code>Calendar</code> is null.
     * @throws IllegalArgumentException if the time of that
     * <code>Calendar</code> can't be obtained because of invalid
     * calendar values.
     */
    @Override
    public int compareTo(Calendar that) {
        long v = getTimeInMillis() - that.getTimeInMillis();
        return v < 0 ? -1 : (v > 0 ? 1 : 0);
    }

    //-------------------------------------------------------------------------
    // Interface for creating custon DateFormats for different types of Calendars
    //-------------------------------------------------------------------------

    /**
     * <strong>[icu]</strong> Returns a <code>DateFormat</code> appropriate to this calendar.
     * Subclasses wishing to specialize this behavior should override
     * {@link #handleGetDateFormat}.
     */
    public DateFormat getDateTimeFormat(int dateStyle, int timeStyle, Locale loc) {
        return formatHelper(this, ULocale.forLocale(loc), dateStyle, timeStyle);
    }

    /**
     * <strong>[icu]</strong> Returns a <code>DateFormat</code> appropriate to this calendar.
     * Subclasses wishing to specialize this behavior should override
     * {@link #handleGetDateFormat}.
     */
    public DateFormat getDateTimeFormat(int dateStyle, int timeStyle, ULocale loc) {
        return formatHelper(this, loc, dateStyle, timeStyle);
    }

    /**
     * Creates a <code>DateFormat</code> appropriate to this calendar.
     * This is a framework method for subclasses to override.  This method
     * is responsible for creating the calendar-specific DateFormat and
     * DateFormatSymbols objects as needed.
     * @param pattern the pattern, specific to the <code>DateFormat</code>
     * subclass
     * @param locale the locale for which the symbols should be drawn
     * @return a <code>DateFormat</code> appropriate to this calendar
     */
    protected DateFormat handleGetDateFormat(String pattern, Locale locale) {
        return handleGetDateFormat(pattern, null, ULocale.forLocale(locale));
    }

    /**
     * Creates a <code>DateFormat</code> appropriate to this calendar.
     * This is a framework method for subclasses to override.  This method
     * is responsible for creating the calendar-specific DateFormat and
     * DateFormatSymbols objects as needed.
     * @param pattern the pattern, specific to the <code>DateFormat</code>
     * subclass
     * @param override The override string.  A numbering system override string can take one of the following forms:
     *     1). If just a numbering system name is specified, it applies to all numeric fields in the date format pattern.
     *     2). To specify an alternate numbering system on a field by field basis, use the field letters from the pattern
     *         followed by an = sign, followed by the numbering system name.  For example, to specify that just the year
     *         be formatted using Hebrew digits, use the override "y=hebr".  Multiple overrides can be specified in a single
     *         string by separating them with a semi-colon. For example, the override string "m=thai;y=deva" would format using
     *         Thai digits for the month and Devanagari digits for the year.
     * @param locale the locale for which the symbols should be drawn
     * @return a <code>DateFormat</code> appropriate to this calendar
     */
    protected DateFormat handleGetDateFormat(String pattern, String override, Locale locale) {
        return handleGetDateFormat(pattern, override, ULocale.forLocale(locale));
    }

    /**
     * Creates a <code>DateFormat</code> appropriate to this calendar.
     * This is a framework method for subclasses to override.  This method
     * is responsible for creating the calendar-specific DateFormat and
     * DateFormatSymbols objects as needed.
     * @param pattern the pattern, specific to the <code>DateFormat</code>
     * subclass
     * @param locale the locale for which the symbols should be drawn
     * @return a <code>DateFormat</code> appropriate to this calendar
     */
    protected DateFormat handleGetDateFormat(String pattern, ULocale locale) {
        return handleGetDateFormat(pattern, null, locale);
    }

    /**
     * Creates a <code>DateFormat</code> appropriate to this calendar.
     * This is a framework method for subclasses to override.  This method
     * is responsible for creating the calendar-specific DateFormat and
     * DateFormatSymbols objects as needed.
     * @param pattern the pattern, specific to the <code>DateFormat</code>
     * subclass
     * @param locale the locale for which the symbols should be drawn
     * @return a <code>DateFormat</code> appropriate to this calendar
     * @hide draft / provisional / internal are hidden on Android
     */
    protected DateFormat handleGetDateFormat(String pattern, String override, ULocale locale) {
        FormatConfiguration fmtConfig = new FormatConfiguration();
        fmtConfig.pattern = pattern;
        fmtConfig.override = override;
        fmtConfig.formatData = new DateFormatSymbols(this, locale);
        fmtConfig.loc = locale;
        fmtConfig.cal = this;

        return SimpleDateFormat.getInstance(fmtConfig);
    }

    // date format pattern cache
    private static final ICUCache<String, PatternData> PATTERN_CACHE =
            new SimpleCache<String, PatternData>();
    // final fallback patterns
    private static final String[] DEFAULT_PATTERNS = {
        "HH:mm:ss z",
        "HH:mm:ss z",
        "HH:mm:ss",
        "HH:mm",
        "EEEE, yyyy MMMM dd",
        "yyyy MMMM d",
        "yyyy MMM d",
        "yy/MM/dd",
        "{1} {0}",
        "{1} {0}",
        "{1} {0}",
        "{1} {0}",
        "{1} {0}"
    };

    static private DateFormat formatHelper(Calendar cal, ULocale loc, int dateStyle,
            int timeStyle) {
        if (timeStyle < DateFormat.NONE || timeStyle > DateFormat.SHORT) {
            throw new IllegalArgumentException("Illegal time style " + timeStyle);
        }
        if (dateStyle < DateFormat.NONE || dateStyle > DateFormat.SHORT) {
            throw new IllegalArgumentException("Illegal date style " + dateStyle);
        }

        PatternData patternData = PatternData.make(cal, loc);
        String override = null;

        // Resolve a pattern for the date/time style
        String pattern = null;
        if ((timeStyle >= 0) && (dateStyle >= 0)) {
            pattern = SimpleFormatterImpl.formatRawPattern(
                    patternData.getDateTimePattern(dateStyle), 2, 2,
                    patternData.patterns[timeStyle],
                    patternData.patterns[dateStyle + 4]);
            // Might need to merge the overrides from the date and time into a single
            // override string TODO: Right now we are forcing the date's override into the
            // time style.
            if ( patternData.overrides != null ) {
                String dateOverride = patternData.overrides[dateStyle + 4];
                String timeOverride = patternData.overrides[timeStyle];
                override = mergeOverrideStrings(
                        patternData.patterns[dateStyle+4],
                        patternData.patterns[timeStyle],
                        dateOverride, timeOverride);
            }
        } else if (timeStyle >= 0) {
            pattern = patternData.patterns[timeStyle];
            if ( patternData.overrides != null ) {
                override = patternData.overrides[timeStyle];
            }
        } else if (dateStyle >= 0) {
            pattern = patternData.patterns[dateStyle + 4];
            if ( patternData.overrides != null ) {
                override = patternData.overrides[dateStyle + 4];
            }
        } else {
            throw new IllegalArgumentException("No date or time style specified");
        }
        DateFormat result = cal.handleGetDateFormat(pattern, override, loc);
        result.setCalendar(cal);
        return result;
    }

    // Android patch (http://b/28832222) start.
    // Expose method to get format string for java.time.
    /**
     * Get the date time format string for the specified values.
     * This is a copy of {@link #formatHelper(Calendar, ULocale, int, int)} with the following
     * changes:
     * <ul>
     *     <li>Made public, but hidden</li>
     *     <li>take calendar type string instead of Calendar</li>
     *     <li>Ignore overrides</li>
     *     <li>Return format string instead of DateFormat.</li>
     * </ul>
     * This is not meant as public API.
     * @hide draft / provisional / internal are hidden on Android
     */
    // TODO: Check if calType can be passed via keyword on loc parameter instead.
    public static String getDateTimeFormatString(ULocale loc, String calType, int dateStyle,
            int timeStyle) {
        if (timeStyle < DateFormat.NONE || timeStyle > DateFormat.SHORT) {
            throw new IllegalArgumentException("Illegal time style " + timeStyle);
        }
        if (dateStyle < DateFormat.NONE || dateStyle > DateFormat.SHORT) {
            throw new IllegalArgumentException("Illegal date style " + dateStyle);
        }

        PatternData patternData = PatternData.make(loc, calType);

        // Resolve a pattern for the date/time style
        String pattern = null;
        if ((timeStyle >= 0) && (dateStyle >= 0)) {
            pattern = SimpleFormatterImpl.formatRawPattern(
                    patternData.getDateTimePattern(dateStyle), 2, 2,
                    patternData.patterns[timeStyle],
                    patternData.patterns[dateStyle + 4]);
        } else if (timeStyle >= 0) {
            pattern = patternData.patterns[timeStyle];
        } else if (dateStyle >= 0) {
            pattern = patternData.patterns[dateStyle + 4];
        } else {
            throw new IllegalArgumentException("No date or time style specified");
        }
        return pattern;
    }
    // Android patch (http://b/28832222) end.

    static class PatternData {
        // TODO make this even more object oriented
        private String[] patterns;
        private String[] overrides;
        public PatternData(String[] patterns, String[] overrides) {
            this.patterns = patterns;
            this.overrides = overrides;
        }
        private String getDateTimePattern(int dateStyle) {
            int glueIndex = 8;
            if (patterns.length >= 13) {
                glueIndex += (dateStyle + 1);
            }
            final String dateTimePattern = patterns[glueIndex];
            return dateTimePattern;
        }
        private static PatternData make(Calendar cal, ULocale loc) {
            // Android patch (http://b/28832222) start.
            return make(loc, cal.getType());
        }
        private static PatternData make(ULocale loc, String calType) {
            // Android patch (http://b/28832222) end.
            // First, try to get a pattern from PATTERN_CACHE
            String key = loc.getBaseName() + "+" + calType;
            PatternData patternData = PATTERN_CACHE.get(key);
            if (patternData == null) {
                // Cache missed.  Get one from bundle
                try {
                    patternData = getPatternData(loc, calType);
                } catch (MissingResourceException e) {
                    patternData = new PatternData(DEFAULT_PATTERNS, null);
                }
                PATTERN_CACHE.put(key, patternData);
            }
            return patternData;
        }
    }

    /**
     * Retrieves the DateTime patterns and overrides from the resource bundle and generates a
     * new PatternData object.
     * @param locale Locale to retrieve.
     * @param calType Calendar type to retrieve. If not found will fallback to gregorian.
     * @return PatternData object for this locale and calendarType.
     */
    private static PatternData getPatternData(ULocale locale, String calType) {
        ICUResourceBundle rb =
                (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, locale);
        ICUResourceBundle dtPatternsRb = rb.findWithFallback("calendar/" + calType + "/DateTimePatterns");
        if (dtPatternsRb == null) {
            dtPatternsRb = rb.getWithFallback("calendar/gregorian/DateTimePatterns");
        }

        int patternsSize = dtPatternsRb.getSize();
        String[] dateTimePatterns = new String[patternsSize];
        String[] dateTimePatternsOverrides = new String[patternsSize];
        for (int i = 0; i < patternsSize; i++) {
            ICUResourceBundle concatenationPatternRb = (ICUResourceBundle) dtPatternsRb.get(i);
            switch (concatenationPatternRb.getType()) {
                case UResourceBundle.STRING:
                    dateTimePatterns[i] = concatenationPatternRb.getString();
                    break;
                case UResourceBundle.ARRAY:
                    dateTimePatterns[i] = concatenationPatternRb.getString(0);
                    dateTimePatternsOverrides[i] = concatenationPatternRb.getString(1);
                    break;
            }
        }
        return new PatternData(dateTimePatterns, dateTimePatternsOverrides);
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static String getDateTimePattern(Calendar cal, ULocale uLocale, int dateStyle) {
        PatternData patternData = PatternData.make(cal, uLocale);
        return patternData.getDateTimePattern(dateStyle);
    }

    private static String mergeOverrideStrings( String datePattern, String timePattern,
            String dateOverride, String timeOverride ) {

        if ( dateOverride == null && timeOverride == null ) {
            return null;
        }

        if ( dateOverride == null ) {
            return expandOverride(timePattern,timeOverride);
        }

        if ( timeOverride == null ) {
            return expandOverride(datePattern,dateOverride);
        }

        if ( dateOverride.equals(timeOverride) ) {
            return dateOverride;
        }

        return (expandOverride(datePattern,dateOverride)+";"+
                expandOverride(timePattern,timeOverride));

    }

    private static final char QUOTE = '\'';
    private static String expandOverride(String pattern, String override) {

        if (override.indexOf('=') >= 0) {
            return override;
        }
        boolean inQuotes = false;
        char prevChar = ' ';
        StringBuilder result = new StringBuilder();

        StringCharacterIterator it = new StringCharacterIterator(pattern);

        for (char c = it.first(); c!= StringCharacterIterator.DONE; c = it.next()) {
            if ( c == QUOTE ) {
                inQuotes = !inQuotes;
                prevChar = c;
                continue;
            }
            if ( !inQuotes && c != prevChar ) {
                if (result.length() > 0) {
                    result.append(";");
                }
                result.append(c);
                result.append("=");
                result.append(override);
            }
            prevChar = c;
        }
        return result.toString();
    }
    /**
     * An instance of FormatConfiguration represents calendar specific
     * date format configuration and used for calling the ICU private
     * SimpleDateFormat factory method.
     *
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static class FormatConfiguration {
        private String pattern;
        private String override;
        private DateFormatSymbols formatData;
        private Calendar cal;
        private ULocale loc;

        // Only Calendar can instantiate
        private FormatConfiguration() {
        }

        /**
         * Returns the pattern string
         * @return the format pattern string
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public String getPatternString() {
            return pattern;
        }

        /**
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public String getOverrideString() {
            return override;
        }

        /**
         * Returns the calendar
         * @return the calendar
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public Calendar getCalendar() {
            return cal;
        }

        /**
         * Returns the locale
         * @return the locale
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public ULocale getLocale() {
            return loc;
        }

        /**
         * Returns the format symbols
         * @return the format symbols
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public DateFormatSymbols getDateFormatSymbols() {
            return formatData;
        }
    }

    //-------------------------------------------------------------------------
    // Protected utility methods for use by subclasses.  These are very handy
    // for implementing add, roll, and computeFields.
    //-------------------------------------------------------------------------

    /**
     * Adjust the specified field so that it is within
     * the allowable range for the date to which this calendar is set.
     * For example, in a Gregorian calendar pinning the {@link #DAY_OF_MONTH DAY_OF_MONTH}
     * field for a calendar set to April 31 would cause it to be set
     * to April 30.
     * <p>
     * <b>Subclassing:</b>
     * <br>
     * This utility method is intended for use by subclasses that need to implement
     * their own overrides of {@link #roll roll} and {@link #add add}.
     * <p>
     * <b>Note:</b>
     * <code>pinField</code> is implemented in terms of
     * {@link #getActualMinimum getActualMinimum}
     * and {@link #getActualMaximum getActualMaximum}.  If either of those methods uses
     * a slow, iterative algorithm for a particular field, it would be
     * unwise to attempt to call <code>pinField</code> for that field.  If you
     * really do need to do so, you should override this method to do
     * something more efficient for that field.
     * <p>
     * @param field The calendar field whose value should be pinned.
     *
     * @see #getActualMinimum
     * @see #getActualMaximum
     */
    protected void pinField(int field) {
        int max = getActualMaximum(field);
        int min = getActualMinimum(field);

        if (fields[field] > max) {
            set(field, max);
        } else if (fields[field] < min) {
            set(field, min);
        }
    }

    /**
     * Returns the week number of a day, within a period. This may be the week number in
     * a year or the week number in a month. Usually this will be a value &gt;= 1, but if
     * some initial days of the period are excluded from week 1, because
     * {@link #getMinimalDaysInFirstWeek getMinimalDaysInFirstWeek} is &gt; 1, then
     * the week number will be zero for those
     * initial days. This method requires the day number and day of week for some
     * known date in the period in order to determine the day of week
     * on the desired day.
     * <p>
     * <b>Subclassing:</b>
     * <br>
     * This method is intended for use by subclasses in implementing their
     * {@link #computeTime computeTime} and/or {@link #computeFields computeFields} methods.
     * It is often useful in {@link #getActualMinimum getActualMinimum} and
     * {@link #getActualMaximum getActualMaximum} as well.
     * <p>
     * This variant is handy for computing the week number of some other
     * day of a period (often the first or last day of the period) when its day
     * of the week is not known but the day number and day of week for some other
     * day in the period (e.g. the current date) <em>is</em> known.
     * <p>
     * @param desiredDay    The {@link #DAY_OF_YEAR DAY_OF_YEAR} or
     *              {@link #DAY_OF_MONTH DAY_OF_MONTH} whose week number is desired.
     *              Should be 1 for the first day of the period.
     *
     * @param dayOfPeriod   The {@link #DAY_OF_YEAR DAY_OF_YEAR}
     *              or {@link #DAY_OF_MONTH DAY_OF_MONTH} for a day in the period whose
     *              {@link #DAY_OF_WEEK DAY_OF_WEEK} is specified by the
     *              <code>dayOfWeek</code> parameter.
     *              Should be 1 for first day of period.
     *
     * @param dayOfWeek  The {@link #DAY_OF_WEEK DAY_OF_WEEK} for the day
     *              corresponding to the <code>dayOfPeriod</code> parameter.
     *              1-based with 1=Sunday.
     *
     * @return      The week number (one-based), or zero if the day falls before
     *              the first week because
     *              {@link #getMinimalDaysInFirstWeek getMinimalDaysInFirstWeek}
     *              is more than one.
     */
    protected int weekNumber(int desiredDay, int dayOfPeriod, int dayOfWeek)
    {
        // Determine the day of the week of the first day of the period
        // in question (either a year or a month).  Zero represents the
        // first day of the week on this calendar.
        int periodStartDayOfWeek = (dayOfWeek - getFirstDayOfWeek() - dayOfPeriod + 1) % 7;
        if (periodStartDayOfWeek < 0) periodStartDayOfWeek += 7;

        // Compute the week number.  Initially, ignore the first week, which
        // may be fractional (or may not be).  We add periodStartDayOfWeek in
        // order to fill out the first week, if it is fractional.
        int weekNo = (desiredDay + periodStartDayOfWeek - 1)/7;

        // If the first week is long enough, then count it.  If
        // the minimal days in the first week is one, or if the period start
        // is zero, we always increment weekNo.
        if ((7 - periodStartDayOfWeek) >= getMinimalDaysInFirstWeek()) ++weekNo;

        return weekNo;
    }

    /**
     * Returns the week number of a day, within a period. This may be the week number in
     * a year, or the week number in a month. Usually this will be a value &gt;= 1, but if
     * some initial days of the period are excluded from week 1, because
     * {@link #getMinimalDaysInFirstWeek getMinimalDaysInFirstWeek} is &gt; 1,
     * then the week number will be zero for those
     * initial days. This method requires the day of week for the given date in order to
     * determine the result.
     * <p>
     * <b>Subclassing:</b>
     * <br>
     * This method is intended for use by subclasses in implementing their
     * {@link #computeTime computeTime} and/or {@link #computeFields computeFields} methods.
     * It is often useful in {@link #getActualMinimum getActualMinimum} and
     * {@link #getActualMaximum getActualMaximum} as well.
     * <p>
     * @param dayOfPeriod   The {@link #DAY_OF_YEAR DAY_OF_YEAR} or
     *                      {@link #DAY_OF_MONTH DAY_OF_MONTH} whose week number is desired.
     *                      Should be 1 for the first day of the period.
     *
     * @param dayOfWeek     The {@link #DAY_OF_WEEK DAY_OF_WEEK} for the day
     *                      corresponding to the <code>dayOfPeriod</code> parameter.
     *                      1-based with 1=Sunday.
     *
     * @return      The week number (one-based), or zero if the day falls before
     *              the first week because
     *              {@link #getMinimalDaysInFirstWeek getMinimalDaysInFirstWeek}
     *              is more than one.
     */
    protected final int weekNumber(int dayOfPeriod, int dayOfWeek)
    {
        return weekNumber(dayOfPeriod, dayOfPeriod, dayOfWeek);
    }

    //-------------------------------------------------------------------------
    // Constants
    //-------------------------------------------------------------------------

    private static final int FIELD_DIFF_MAX_INT = Integer.MAX_VALUE;    // 2147483647

    /**
     * <strong>[icu]</strong> Returns the difference between the given time and the time this
     * calendar object is set to.  If this calendar is set
     * <em>before</em> the given time, the returned value will be
     * positive.  If this calendar is set <em>after</em> the given
     * time, the returned value will be negative.  The
     * <code>field</code> parameter specifies the units of the return
     * value.  For example, if <code>fieldDifference(when,
     * Calendar.MONTH)</code> returns 3, then this calendar is set to
     * 3 months before <code>when</code>, and possibly some additional
     * time less than one month.
     *
     * <p>As a side effect of this call, this calendar is advanced
     * toward <code>when</code> by the given amount.  That is, calling
     * this method has the side effect of calling <code>add(field,
     * n)</code>, where <code>n</code> is the return value.
     *
     * <p>Usage: To use this method, call it first with the largest
     * field of interest, then with progressively smaller fields.  For
     * example:
     *
     * <pre>
     * int y = cal.fieldDifference(when, Calendar.YEAR);
     * int m = cal.fieldDifference(when, Calendar.MONTH);
     * int d = cal.fieldDifference(when, Calendar.DATE);</pre>
     *
     * computes the difference between <code>cal</code> and
     * <code>when</code> in years, months, and days.
     *
     * <p>Note: <code>fieldDifference()</code> is
     * <em>asymmetrical</em>.  That is, in the following code:
     *
     * <pre>
     * cal.setTime(date1);
     * int m1 = cal.fieldDifference(date2, Calendar.MONTH);
     * int d1 = cal.fieldDifference(date2, Calendar.DATE);
     * cal.setTime(date2);
     * int m2 = cal.fieldDifference(date1, Calendar.MONTH);
     * int d2 = cal.fieldDifference(date1, Calendar.DATE);</pre>
     *
     * one might expect that <code>m1 == -m2 &amp;&amp; d1 == -d2</code>.
     * However, this is not generally the case, because of
     * irregularities in the underlying calendar system (e.g., the
     * Gregorian calendar has a varying number of days per month).
     *
     * @param when the date to compare this calendar's time to
     * @param field the field in which to compute the result
     * @return the difference, either positive or negative, between
     * this calendar's time and <code>when</code>, in terms of
     * <code>field</code>.
     */
    public int fieldDifference(Date when, int field) {
        int min = 0;
        long startMs = getTimeInMillis();
        long targetMs = when.getTime();
        // Always add from the start millis.  This accomodates
        // operations like adding years from February 29, 2000 up to
        // February 29, 2004.  If 1, 1, 1, 1 is added to the year
        // field, the DOM gets pinned to 28 and stays there, giving an
        // incorrect DOM difference of 1.  We have to add 1, reset, 2,
        // reset, 3, reset, 4.
        if (startMs < targetMs) {
            int max = 1;
            // Find a value that is too large
            for (;;) {
                setTimeInMillis(startMs);
                add(field, max);
                long ms = getTimeInMillis();
                if (ms == targetMs) {
                    return max;
                } else if (ms > targetMs) {
                    break;
                } else if (max < FIELD_DIFF_MAX_INT) {
                    min = max;
                    max <<= 1;
                    if (max < 0) {
                        max = FIELD_DIFF_MAX_INT;
                    }
                } else {
                    // Field difference too large to fit into int
                    throw new RuntimeException();
                }
            }
            // Do a binary search
            while ((max - min) > 1) {
                int t = min + (max - min)/2; // make sure intermediate values don't exceed FIELD_DIFF_MAX_INT
                setTimeInMillis(startMs);
                add(field, t);
                long ms = getTimeInMillis();
                if (ms == targetMs) {
                    return t;
                } else if (ms > targetMs) {
                    max = t;
                } else {
                    min = t;
                }
            }
        } else if (startMs > targetMs) {
            //Eclipse stated the following is "dead code"
            /*if (false) {
                // This works, and makes the code smaller, but costs
                // an extra object creation and an extra couple cycles
                // of calendar computation.
                setTimeInMillis(targetMs);
                min = -fieldDifference(new Date(startMs), field);
            }*/
            int max = -1;
            // Find a value that is too small
            for (;;) {
                setTimeInMillis(startMs);
                add(field, max);
                long ms = getTimeInMillis();
                if (ms == targetMs) {
                    return max;
                } else if (ms < targetMs) {
                    break;
                } else {
                    min = max;
                    max <<= 1;
                    if (max == 0) {
                        // Field difference too large to fit into int
                        throw new RuntimeException();
                    }
                }
            }
            // Do a binary search
            while ((min - max) > 1) {
                int t = min + (max - min)/2; // make sure intermediate values don't exceed FIELD_DIFF_MAX_INT
                setTimeInMillis(startMs);
                add(field, t);
                long ms = getTimeInMillis();
                if (ms == targetMs) {
                    return t;
                } else if (ms < targetMs) {
                    max = t;
                } else {
                    min = t;
                }
            }
        }
        // Set calendar to end point
        setTimeInMillis(startMs);
        add(field, min);
        return min;
    }

    /**
     * Sets the time zone with the given time zone value.
     * @param value the given time zone.
     */
    public void setTimeZone(TimeZone value)
    {
        zone = value;
        /* Recompute the fields from the time using the new zone.  This also
         * works if isTimeSet is false (after a call to set()).  In that case
         * the time will be computed from the fields using the new zone, then
         * the fields will get recomputed from that.  Consider the sequence of
         * calls: cal.setTimeZone(EST); cal.set(HOUR, 1); cal.setTimeZone(PST).
         * Is cal set to 1 o'clock EST or 1 o'clock PST?  Answer: PST.  More
         * generally, a call to setTimeZone() affects calls to set() BEFORE AND
         * AFTER it up to the next call to complete().
         */
        areFieldsSet = false;
    }

    /**
     * Returns the time zone.
     * @return the time zone object associated with this calendar.
     */
    public TimeZone getTimeZone()
    {
        return zone;
    }

    /**
     * Specify whether or not date/time interpretation is to be lenient.  With
     * lenient interpretation, a date such as "February 942, 1996" will be
     * treated as being equivalent to the 941st day after February 1, 1996.
     * With strict interpretation, such dates will cause an exception to be
     * thrown.
     *
     * @see DateFormat#setLenient
     */
    public void setLenient(boolean lenient)
    {
        this.lenient = lenient;
    }

    /**
     * Tell whether date/time interpretation is to be lenient.
     */
    public boolean isLenient()
    {
        return lenient;
    }

    /**
     * <strong>[icu]</strong>Sets the behavior for handling wall time repeating multiple times
     * at negative time zone offset transitions. For example, 1:30 AM on
     * November 6, 2011 in US Eastern time (Ameirca/New_York) occurs twice;
     * 1:30 AM EDT, then 1:30 AM EST one hour later. When <code>WALLTIME_FIRST</code>
     * is used, the wall time 1:30AM in this example will be interpreted as 1:30 AM EDT
     * (first occurrence). When <code>WALLTIME_LAST</code> is used, it will be
     * interpreted as 1:30 AM EST (last occurrence). The default value is
     * <code>WALLTIME_LAST</code>.
     *
     * @param option the behavior for handling repeating wall time, either
     * <code>WALLTIME_FIRST</code> or <code>WALLTIME_LAST</code>.
     * @throws IllegalArgumentException when <code>option</code> is neither
     * <code>WALLTIME_FIRST</code> nor <code>WALLTIME_LAST</code>.
     *
     * @see #getRepeatedWallTimeOption()
     * @see #WALLTIME_FIRST
     * @see #WALLTIME_LAST
     */
    public void setRepeatedWallTimeOption(int option) {
        if (option != WALLTIME_LAST && option != WALLTIME_FIRST) {
            throw new IllegalArgumentException("Illegal repeated wall time option - " + option);
        }
        repeatedWallTime = option;
    }

    /**
     * <strong>[icu]</strong>Gets the behavior for handling wall time repeating multiple times
     * at negative time zone offset transitions.
     *
     * @return the behavior for handling repeating wall time, either
     * <code>WALLTIME_FIRST</code> or <code>WALLTIME_LAST</code>.
     *
     * @see #setRepeatedWallTimeOption(int)
     * @see #WALLTIME_FIRST
     * @see #WALLTIME_LAST
     */
    public int getRepeatedWallTimeOption() {
        return repeatedWallTime;
    }

    /**
     * <strong>[icu]</strong>Sets the behavior for handling skipped wall time at positive time zone offset
     * transitions. For example, 2:30 AM on March 13, 2011 in US Eastern time (America/New_York)
     * does not exist because the wall time jump from 1:59 AM EST to 3:00 AM EDT. When
     * <code>WALLTIME_FIRST</code> is used, 2:30 AM is interpreted as 30 minutes before 3:00 AM
     * EDT, therefore, it will be resolved as 1:30 AM EST. When <code>WALLTIME_LAST</code>
     * is used, 2:30 AM is interpreted as 31 minutes after 1:59 AM EST, therefore, it will be
     * resolved as 3:30 AM EDT. When <code>WALLTIME_NEXT_VALID</code> is used, 2:30 AM will
     * be resolved as next valid wall time, that is 3:00 AM EDT. The default value is
     * <code>WALLTIME_LAST</code>.
     * <p>
     * <b>Note:</b>This option is effective only when this calendar is {@link #isLenient() lenient}.
     * When the calendar is strict, such non-existing wall time will cause an exception.
     *
     * @param option the behavior for handling skipped wall time at positive time zone
     * offset transitions, one of <code>WALLTIME_FIRST</code>, <code>WALLTIME_LAST</code> and
     * <code>WALLTIME_NEXT_VALID</code>.
     * @throws IllegalArgumentException when <code>option</code> is not any of
     * <code>WALLTIME_FIRST</code>, <code>WALLTIME_LAST</code> and <code>WALLTIME_NEXT_VALID</code>.
     *
     * @see #getSkippedWallTimeOption()
     * @see #WALLTIME_FIRST
     * @see #WALLTIME_LAST
     * @see #WALLTIME_NEXT_VALID
     */
    public void setSkippedWallTimeOption(int option) {
        if (option != WALLTIME_LAST && option != WALLTIME_FIRST && option != WALLTIME_NEXT_VALID) {
            throw new IllegalArgumentException("Illegal skipped wall time option - " + option);
        }
        skippedWallTime = option;
    }

    /**
     * <strong>[icu]</strong>Gets the behavior for handling skipped wall time at positive time zone offset
     * transitions.
     *
     * @return the behavior for handling skipped wall time, one of
     * <code>WALLTIME_FIRST</code>, <code>WALLTIME_LAST</code> and <code>WALLTIME_NEXT_VALID</code>.
     *
     * @see #setSkippedWallTimeOption(int)
     * @see #WALLTIME_FIRST
     * @see #WALLTIME_LAST
     * @see #WALLTIME_NEXT_VALID
     */
    public int getSkippedWallTimeOption() {
        return skippedWallTime;
    }

    /**
     * Sets what the first day of the week is,
     * where 1 = {@link #SUNDAY} and 7 = {@link #SATURDAY}.
     * @param value the given first day of the week, where 1 = {@link #SUNDAY} and 7 = {@link #SATURDAY}.
     */
    public void setFirstDayOfWeek(int value)
    {
        if (firstDayOfWeek != value) {
            if (value < SUNDAY || value > SATURDAY) {
                throw new IllegalArgumentException("Invalid day of week");
            }
            firstDayOfWeek = value;
            areFieldsSet = false;
        }
    }

    /**
     * Returns what the first day of the week is,
     * where 1 = {@link #SUNDAY} and 7 = {@link #SATURDAY}.
     * e.g., Sunday in US, Monday in France
     * @return the first day of the week, where 1 = {@link #SUNDAY} and 7 = {@link #SATURDAY}.
     */
    public int getFirstDayOfWeek()
    {
        return firstDayOfWeek;
    }

    /**
     * Sets what the minimal days required in the first week of the year are.
     * For example, if the first week is defined as one that contains the first
     * day of the first month of a year, call the method with value 1. If it
     * must be a full week, use value 7.
     * @param value the given minimal days required in the first week
     * of the year.
     */
    public void setMinimalDaysInFirstWeek(int value)
    {
        // Values less than 1 have the same effect as 1; values greater
        // than 7 have the same effect as 7. However, we normalize values
        // so operator== and so forth work.
        if (value < 1) {
            value = 1;
        } else if (value > 7) {
            value = 7;
        }
        if (minimalDaysInFirstWeek != value) {
            minimalDaysInFirstWeek = value;
            areFieldsSet = false;
        }
    }

    /**
     * Returns what the minimal days required in the first week of the year are.
     * That is, if the first week is defined as one that contains the first day
     * of the first month of a year, getMinimalDaysInFirstWeek returns 1. If
     * the minimal days required must be a full week, getMinimalDaysInFirstWeek
     * returns 7.
     * @return the minimal days required in the first week of the year.
     */
    public int getMinimalDaysInFirstWeek()
    {
        return minimalDaysInFirstWeek;
    }

    private static final int LIMITS[][] = {
        //    Minimum  Greatest min      Least max   Greatest max
        {/*                                                      */}, // ERA
        {/*                                                      */}, // YEAR
        {/*                                                      */}, // MONTH
        {/*                                                      */}, // WEEK_OF_YEAR
        {/*                                                      */}, // WEEK_OF_MONTH
        {/*                                                      */}, // DAY_OF_MONTH
        {/*                                                      */}, // DAY_OF_YEAR
        {           1,            1,             7,             7  }, // DAY_OF_WEEK
        {/*                                                      */}, // DAY_OF_WEEK_IN_MONTH
        {           0,            0,             1,             1  }, // AM_PM
        {           0,            0,            11,            11  }, // HOUR
        {           0,            0,            23,            23  }, // HOUR_OF_DAY
        {           0,            0,            59,            59  }, // MINUTE
        {           0,            0,            59,            59  }, // SECOND
        {           0,            0,           999,           999  }, // MILLISECOND
        {-12*ONE_HOUR, -12*ONE_HOUR,   12*ONE_HOUR,   12*ONE_HOUR  }, // ZONE_OFFSET
        {           0,            0,    1*ONE_HOUR,    1*ONE_HOUR  }, // DST_OFFSET
        {/*                                                      */}, // YEAR_WOY
        {           1,            1,             7,             7  }, // DOW_LOCAL
        {/*                                                      */}, // EXTENDED_YEAR
        { -0x7F000000,  -0x7F000000,    0x7F000000,    0x7F000000  }, // JULIAN_DAY
        {           0,            0, 24*ONE_HOUR-1, 24*ONE_HOUR-1  }, // MILLISECONDS_IN_DAY
        {           0,            0,             1,             1  }, // IS_LEAP_MONTH
    };

    /**
     * Subclass API for defining limits of different types.
     * Subclasses must implement this method to return limits for the
     * following fields:
     *
     * <pre>ERA
     * YEAR
     * MONTH
     * WEEK_OF_YEAR
     * WEEK_OF_MONTH
     * DAY_OF_MONTH
     * DAY_OF_YEAR
     * DAY_OF_WEEK_IN_MONTH
     * YEAR_WOY
     * EXTENDED_YEAR</pre>
     *
     * @param field one of the above field numbers
     * @param limitType one of <code>MINIMUM</code>, <code>GREATEST_MINIMUM</code>,
     * <code>LEAST_MAXIMUM</code>, or <code>MAXIMUM</code>
     */
    abstract protected int handleGetLimit(int field, int limitType);

    /**
     * Returns a limit for a field.
     * @param field the field, from 0..<code>getFieldCount()-1</code>
     * @param limitType the type specifier for the limit
     * @see #MINIMUM
     * @see #GREATEST_MINIMUM
     * @see #LEAST_MAXIMUM
     * @see #MAXIMUM
     */
    protected int getLimit(int field, int limitType) {
        switch (field) {
        case DAY_OF_WEEK:
        case AM_PM:
        case HOUR:
        case HOUR_OF_DAY:
        case MINUTE:
        case SECOND:
        case MILLISECOND:
        case ZONE_OFFSET:
        case DST_OFFSET:
        case DOW_LOCAL:
        case JULIAN_DAY:
        case MILLISECONDS_IN_DAY:
        case IS_LEAP_MONTH:
            return LIMITS[field][limitType];

        case WEEK_OF_MONTH:
        {
            int limit;
            if (limitType == MINIMUM) {
                limit = getMinimalDaysInFirstWeek() == 1 ? 1 : 0;
            } else if (limitType == GREATEST_MINIMUM){
                limit = 1;
            } else {
                int minDaysInFirst = getMinimalDaysInFirstWeek();
                int daysInMonth = handleGetLimit(DAY_OF_MONTH, limitType);
                if (limitType == LEAST_MAXIMUM) {
                    limit = (daysInMonth + (7 - minDaysInFirst)) / 7;
                } else { // limitType == MAXIMUM
                    limit = (daysInMonth + 6 + (7 - minDaysInFirst)) / 7;
                }
            }
            return limit;
        }

        }
        return handleGetLimit(field, limitType);
    }

    /**
     * Limit type for <code>getLimit()</code> and <code>handleGetLimit()</code>
     * indicating the minimum value that a field can take (least minimum).
     * @see #getLimit
     * @see #handleGetLimit
     */
    protected static final int MINIMUM = 0;

    /**
     * Limit type for <code>getLimit()</code> and <code>handleGetLimit()</code>
     * indicating the greatest minimum value that a field can take.
     * @see #getLimit
     * @see #handleGetLimit
     */
    protected static final int GREATEST_MINIMUM = 1;

    /**
     * Limit type for <code>getLimit()</code> and <code>handleGetLimit()</code>
     * indicating the least maximum value that a field can take.
     * @see #getLimit
     * @see #handleGetLimit
     */
    protected static final int LEAST_MAXIMUM = 2;

    /**
     * Limit type for <code>getLimit()</code> and <code>handleGetLimit()</code>
     * indicating the maximum value that a field can take (greatest maximum).
     * @see #getLimit
     * @see #handleGetLimit
     */
    protected static final int MAXIMUM = 3;

    /**
     * Returns the minimum value for the given time field.
     * e.g., for Gregorian DAY_OF_MONTH, 1.
     * @param field the given time field.
     * @return the minimum value for the given time field.
     */
    public final int getMinimum(int field) {
        return getLimit(field, MINIMUM);
    }

    /**
     * Returns the maximum value for the given time field.
     * e.g. for Gregorian DAY_OF_MONTH, 31.
     * @param field the given time field.
     * @return the maximum value for the given time field.
     */
    public final int getMaximum(int field) {
        return getLimit(field, MAXIMUM);
    }

    /**
     * Returns the highest minimum value for the given field if varies.
     * Otherwise same as getMinimum(). For Gregorian, no difference.
     * @param field the given time field.
     * @return the highest minimum value for the given time field.
     */
    public final int getGreatestMinimum(int field) {
        return getLimit(field, GREATEST_MINIMUM);
    }

    /**
     * Returns the lowest maximum value for the given field if varies.
     * Otherwise same as getMaximum(). e.g., for Gregorian DAY_OF_MONTH, 28.
     * @param field the given time field.
     * @return the lowest maximum value for the given time field.
     */
    public final int getLeastMaximum(int field) {
        return getLimit(field, LEAST_MAXIMUM);
    }

    //-------------------------------------------------------------------------
    // Weekend support -- determining which days of the week are the weekend
    // in a given locale
    //-------------------------------------------------------------------------

    /**
     * <strong>[icu]</strong> Returns whether the given day of the week is a weekday, a
     * weekend day, or a day that transitions from one to the other, for the
     * locale and calendar system associated with this Calendar (the locale's
     * region is often the most determinant factor). If a transition occurs at
     * midnight, then the days before and after the transition will have the
     * type WEEKDAY or WEEKEND.  If a transition occurs at a time
     * other than midnight, then the day of the transition will have
     * the type WEEKEND_ONSET or WEEKEND_CEASE.  In this case, the
     * method getWeekendTransition() will return the point of
     * transition.
     * @param dayOfWeek either SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
     * THURSDAY, FRIDAY, or SATURDAY
     * @return either WEEKDAY, WEEKEND, WEEKEND_ONSET, or
     * WEEKEND_CEASE
     * @exception IllegalArgumentException if dayOfWeek is not
     * between SUNDAY and SATURDAY, inclusive
     * @see #WEEKDAY
     * @see #WEEKEND
     * @see #WEEKEND_ONSET
     * @see #WEEKEND_CEASE
     * @see #getWeekendTransition
     * @see #isWeekend(Date)
     * @see #isWeekend()
     * @deprecated ICU 54 use {@link #getWeekDataForRegion(String)}, {@link #getWeekData()}, {@link #setWeekData(WeekData)}
     * @hide original deprecated declaration
     */
    @Deprecated
    public int getDayOfWeekType(int dayOfWeek) {
        if (dayOfWeek < SUNDAY || dayOfWeek > SATURDAY) {
            throw new IllegalArgumentException("Invalid day of week");
        }
        if (weekendOnset == weekendCease) {
            if (dayOfWeek != weekendOnset)
                return WEEKDAY;
            return (weekendOnsetMillis == 0) ? WEEKEND : WEEKEND_ONSET;
        }
        if (weekendOnset < weekendCease) {
            if (dayOfWeek < weekendOnset || dayOfWeek > weekendCease) {
                return WEEKDAY;
            }
        } else {
            if (dayOfWeek > weekendCease && dayOfWeek < weekendOnset) {
                return WEEKDAY;
            }
        }
        if (dayOfWeek == weekendOnset) {
            return (weekendOnsetMillis == 0) ? WEEKEND : WEEKEND_ONSET;
        }
        if (dayOfWeek == weekendCease) {
            return (weekendCeaseMillis >= 86400000) ? WEEKEND : WEEKEND_CEASE;
        }
        return WEEKEND;
    }

    /**
     * <strong>[icu]</strong> Returns the time during the day at which the weekend begins or end in this
     * calendar system.  If getDayOfWeekType(dayOfWeek) == WEEKEND_ONSET return the time
     * at which the weekend begins.  If getDayOfWeekType(dayOfWeek) == WEEKEND_CEASE
     * return the time at which the weekend ends.  If getDayOfWeekType(dayOfWeek) has some
     * other value, then throw an exception.
     * @param dayOfWeek either SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
     * THURSDAY, FRIDAY, or SATURDAY
     * @return the milliseconds after midnight at which the
     * weekend begins or ends
     * @exception IllegalArgumentException if dayOfWeek is not
     * WEEKEND_ONSET or WEEKEND_CEASE
     * @see #getDayOfWeekType
     * @see #isWeekend(Date)
     * @see #isWeekend()
     * @deprecated ICU 54 use {@link #getWeekDataForRegion(String)}, {@link #getWeekData()}, {@link #setWeekData(WeekData)}
     * @hide original deprecated declaration
     */
    @Deprecated
    public int getWeekendTransition(int dayOfWeek) {
        if (dayOfWeek == weekendOnset) {
            return weekendOnsetMillis;
        } else if (dayOfWeek == weekendCease) {
            return weekendCeaseMillis;
        }
        throw new IllegalArgumentException("Not weekend transition day");
    }

    /**
     * <strong>[icu]</strong> Returns true if the given date and time is in the weekend in this calendar
     * system.  Equivalent to calling setTime() followed by isWeekend().  Note: This
     * method changes the time this calendar is set to.
     * @param date the date and time
     * @return true if the given date and time is part of the
     * weekend
     * @see #getDayOfWeekType
     * @see #getWeekendTransition
     * @see #isWeekend()
     */
    public boolean isWeekend(Date date) {
        setTime(date);
        return isWeekend();
    }

    /**
     * <strong>[icu]</strong> Returns true if this Calendar's current date and time is in the weekend in
     * this calendar system.
     * @return true if the given date and time is part of the
     * weekend
     * @see #getDayOfWeekType
     * @see #getWeekendTransition
     * @see #isWeekend(Date)
     */
    public boolean isWeekend() {
        int dow =  get(DAY_OF_WEEK);
        int dowt = getDayOfWeekType(dow);
        switch (dowt) {
        case WEEKDAY:
            return false;
        case WEEKEND:
            return true;
        default: // That is, WEEKEND_ONSET or WEEKEND_CEASE
            // Use internalGet() because the above call to get() populated
            // all fields.
            // [Note: There should be a better way to get millis in day.
            //  For ICU4J, submit request for a MILLIS_IN_DAY field
            //  and a DAY_NUMBER field (could be Julian day #). - aliu]
            int millisInDay = internalGet(MILLISECOND) + 1000 * (internalGet(SECOND) +
                    60 * (internalGet(MINUTE) + 60 * internalGet(HOUR_OF_DAY)));
            int transition = getWeekendTransition(dow);
            return (dowt == WEEKEND_ONSET)
                    ? (millisInDay >= transition)
                            : (millisInDay <  transition);
        }
        // (We can never reach this point.)
    }

    //-------------------------------------------------------------------------
    // End of weekend support
    //-------------------------------------------------------------------------

    /**
     * Overrides Cloneable
     */
    @Override
    public Object clone()
    {
        try {
            Calendar other = (Calendar) super.clone();

            other.fields = new int[fields.length];
            other.stamp = new int[fields.length];
            System.arraycopy(this.fields, 0, other.fields, 0, fields.length);
            System.arraycopy(this.stamp, 0, other.stamp, 0, fields.length);

            other.zone = (TimeZone) zone.clone();
            return other;
        }
        catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new ICUCloneNotSupportedException(e);
        }
    }

    /**
     * Returns a string representation of this calendar. This method
     * is intended to be used only for debugging purposes, and the
     * format of the returned string may vary between implementations.
     * The returned string may be empty but may not be <code>null</code>.
     *
     * @return  a string representation of this calendar.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getName());
        buffer.append("[time=");
        buffer.append(isTimeSet ? String.valueOf(time) : "?");
        buffer.append(",areFieldsSet=");
        buffer.append(areFieldsSet);
        buffer.append(",areAllFieldsSet=");
        buffer.append(areAllFieldsSet);
        buffer.append(",lenient=");
        buffer.append(lenient);
        buffer.append(",zone=");
        buffer.append(zone);
        buffer.append(",firstDayOfWeek=");
        buffer.append(firstDayOfWeek);
        buffer.append(",minimalDaysInFirstWeek=");
        buffer.append(minimalDaysInFirstWeek);
        buffer.append(",repeatedWallTime=");
        buffer.append(repeatedWallTime);
        buffer.append(",skippedWallTime=");
        buffer.append(skippedWallTime);
        for (int i=0; i<fields.length; ++i) {
            buffer.append(',').append(fieldName(i)).append('=');
            buffer.append(isSet(i) ? String.valueOf(fields[i]) : "?");
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Simple, immutable struct-like class for access to the CLDR weekend data.
     */
    public static final class WeekData {
        /**
         * the first day of the week, where 1 = {@link #SUNDAY} and 7 = {@link #SATURDAY}
         */
        public final int firstDayOfWeek;
        /**
         * the minimal number of days in the first week
         */
        public final int minimalDaysInFirstWeek;
        /**
         * the onset day, where 1 = {@link #SUNDAY} and 7 = {@link #SATURDAY}
         */
        public final int weekendOnset;
        /**
         * the onset time in millis during the onset day
         */
        public final int weekendOnsetMillis;
        /**
         * the cease day, where 1 = {@link #SUNDAY} and 7 = {@link #SATURDAY}
         */
        public final int weekendCease;
        /**
         * the cease time in millis during the cease day. Exclusive, so the max is 24:00:00.000.
         * Note that this will format as 00:00 the next day.
         */
        public final int weekendCeaseMillis;

        /**
         * Constructor
         *
         * @param fdow the first day of the week, where 1 = {@link #SUNDAY} and 7 = {@link #SATURDAY}
         * @param mdifw the minimal number of days in the first week
         * @param weekendOnset the onset day, where 1 = Sunday and 7 = Saturday
         * @param weekendOnsetMillis the onset time in millis during the onset day
         * @param weekendCease the cease day, where 1 = Sunday and 7 = Saturday
         * @param weekendCeaseMillis the cease time in millis during the cease day.
         */
        public WeekData(int fdow, int mdifw,
                int weekendOnset, int weekendOnsetMillis,
                int weekendCease, int weekendCeaseMillis) {
            this.firstDayOfWeek = fdow;
            this.minimalDaysInFirstWeek = mdifw;
            this.weekendOnset = weekendOnset;
            this.weekendOnsetMillis = weekendOnsetMillis;
            this.weekendCease = weekendCease;
            this.weekendCeaseMillis = weekendCeaseMillis;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return ((((firstDayOfWeek * 37 + minimalDaysInFirstWeek) * 37 + weekendOnset) * 37
                    + weekendOnsetMillis) * 37 + weekendCease) * 37 + weekendCeaseMillis;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof WeekData)) {
                return false;
            }
            WeekData that = (WeekData) other;
            return firstDayOfWeek == that.firstDayOfWeek
                    && minimalDaysInFirstWeek == that.minimalDaysInFirstWeek
                    && weekendOnset == that.weekendOnset
                    && weekendOnsetMillis == that.weekendOnsetMillis
                    && weekendCease == that.weekendCease
                    && weekendCeaseMillis == that.weekendCeaseMillis;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "{" + firstDayOfWeek
                    + ", " + minimalDaysInFirstWeek
                    + ", " + weekendOnset
                    + ", " + weekendOnsetMillis
                    + ", " + weekendCease
                    + ", " + weekendCeaseMillis
                    + "}";
        }
    }

    /**
     * <strong>[icu]</strong> Return simple, immutable struct-like class for access to the CLDR weekend data.
     * @param region The input region. The results are undefined if the region code is not valid.
     * @return the WeekData for the input region. It is never null.
     */
    public static WeekData getWeekDataForRegion(String region) {
        return WEEK_DATA_CACHE.createInstance(region, region);
    }

    /**
     * <strong>[icu]</strong> Return simple, immutable struct-like class for access to the weekend data in this calendar.
     * @return the WeekData for this calendar.
     */
    public WeekData getWeekData() {
        return new WeekData(firstDayOfWeek, minimalDaysInFirstWeek, weekendOnset, weekendOnsetMillis, weekendCease, weekendCeaseMillis);
    }

    /**
     * <strong>[icu]</strong> Set data in this calendar based on the WeekData input.
     * @param wdata The week data to use
     * @return this, for chaining
     */
    public Calendar setWeekData(WeekData wdata) {
        setFirstDayOfWeek(wdata.firstDayOfWeek);
        setMinimalDaysInFirstWeek(wdata.minimalDaysInFirstWeek);

        weekendOnset       = wdata.weekendOnset;
        weekendOnsetMillis = wdata.weekendOnsetMillis;
        weekendCease       = wdata.weekendCease;
        weekendCeaseMillis = wdata.weekendCeaseMillis;
        return this;
    }

    private static WeekData getWeekDataForRegionInternal(String region) {
        if (region == null) {
            region = "001";
        }

        UResourceBundle rb = UResourceBundle.getBundleInstance(
                ICUData.ICU_BASE_NAME,
                "supplementalData",
                ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        UResourceBundle weekDataInfo = rb.get("weekData");
        UResourceBundle weekDataBundle = null;

        try {
            weekDataBundle = weekDataInfo.get(region);
        } catch (MissingResourceException mre) {
            if (!region.equals("001")) {
                // use "001" as fallback
                weekDataBundle = weekDataInfo.get("001");
            } else {
                throw mre;
            }
        }

        int[] wdi = weekDataBundle.getIntVector();
        return new WeekData(wdi[0],wdi[1],wdi[2],wdi[3],wdi[4],wdi[5]);
    }

    /*
     * Cache to hold week data by region
     */
    private static class WeekDataCache extends SoftCache<String, WeekData, String> {

        /* (non-Javadoc)
         * @see android.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected WeekData createInstance(String key, String data) {
            return getWeekDataForRegionInternal(key);
        }
    }

    private static final WeekDataCache WEEK_DATA_CACHE = new WeekDataCache();

    /*
     * Set this calendar to contain week and weekend data for the given region.
     */
    private void setWeekData(String region) {
        if (region == null) {
            region = "001";
        }
        WeekData wdata = WEEK_DATA_CACHE.getInstance(region, region);
        setWeekData(wdata);
    }

    /**
     * Recompute the time and update the status fields isTimeSet
     * and areFieldsSet.  Callers should check isTimeSet and only
     * call this method if isTimeSet is false.
     */
    private void updateTime() {
        computeTime();
        // If we are lenient, we need to recompute the fields to normalize
        // the values.  Also, if we haven't set all the fields yet (i.e.,
        // in a newly-created object), we need to fill in the fields. [LIU]
        if (isLenient() || !areAllFieldsSet) areFieldsSet = false;
        isTimeSet = true;
        areFieldsVirtuallySet = false;
    }

    /**
     * Save the state of this object to a stream (i.e., serialize it).
     */
    private void writeObject(ObjectOutputStream stream)
            throws IOException
            {
        // Try to compute the time correctly, for the future (stream
        // version 2) in which we don't write out fields[] or isSet[].
        if (!isTimeSet) {
            try {
                updateTime();
            }
            catch (IllegalArgumentException e) {}
        }

        // Write out the 1.1 FCS object.
        stream.defaultWriteObject();
            }

    /**
     * Reconstitute this object from a stream (i.e., deserialize it).
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {

        stream.defaultReadObject();

        initInternal();

        isTimeSet = true;
        areFieldsSet = areAllFieldsSet = false;
        areFieldsVirtuallySet = true; // cause fields to be recalculated if requested.
        nextStamp = MINIMUM_USER_STAMP;
    }


    //----------------------------------------------------------------------
    // Time -> Fields
    //----------------------------------------------------------------------

    /**
     * Converts the current millisecond time value <code>time</code> to
     * field values in <code>fields[]</code>.  This synchronizes the time
     * field values with a new time that is set for the calendar.  The time
     * is <em>not</em> recomputed first; to recompute the time, then the
     * fields, call the <code>complete</code> method.
     * @see #complete
     */
    protected void computeFields() {
        int offsets[] = new int[2];
        getTimeZone().getOffset(time, false, offsets);
        long localMillis = time + offsets[0] + offsets[1];

        // Mark fields as set.  Do this before calling handleComputeFields().
        int mask = internalSetMask;
        for (int i=0; i<fields.length; ++i) {
            if ((mask & 1) == 0) {
                stamp[i] = INTERNALLY_SET;
            } else {
                stamp[i] = UNSET;
            }
            mask >>= 1;
        }

        // We used to check for and correct extreme millis values (near
        // Long.MIN_VALUE or Long.MAX_VALUE) here.  Such values would cause
        // overflows from positive to negative (or vice versa) and had to
        // be manually tweaked.  We no longer need to do this because we
        // have limited the range of supported dates to those that have a
        // Julian day that fits into an int.  This allows us to implement a
        // JULIAN_DAY field and also removes some inelegant code. - Liu
        // 11/6/00

        long days = floorDivide(localMillis, ONE_DAY);

        fields[JULIAN_DAY] = (int) days + EPOCH_JULIAN_DAY;

        computeGregorianAndDOWFields(fields[JULIAN_DAY]);

        // Call framework method to have subclass compute its fields.
        // These must include, at a minimum, MONTH, DAY_OF_MONTH,
        // EXTENDED_YEAR, YEAR, DAY_OF_YEAR.  This method will call internalSet(),
        // which will update stamp[].
        handleComputeFields(fields[JULIAN_DAY]);

        // Compute week-related fields, based on the subclass-computed
        // fields computed by handleComputeFields().
        computeWeekFields();

        // Compute time-related fields.  These are indepent of the date and
        // of the subclass algorithm.  They depend only on the local zone
        // wall milliseconds in day.
        int millisInDay = (int) (localMillis - (days * ONE_DAY));
        fields[MILLISECONDS_IN_DAY] = millisInDay;
        fields[MILLISECOND] = millisInDay % 1000;
        millisInDay /= 1000;
        fields[SECOND] = millisInDay % 60;
        millisInDay /= 60;
        fields[MINUTE] = millisInDay % 60;
        millisInDay /= 60;
        fields[HOUR_OF_DAY] = millisInDay;
        fields[AM_PM] = millisInDay / 12; // Assume AM == 0
        fields[HOUR] = millisInDay % 12;
        fields[ZONE_OFFSET] = offsets[0];
        fields[DST_OFFSET] = offsets[1];
    }

    /**
     * Compute the Gregorian calendar year, month, and day of month from
     * the given Julian day.  These values are not stored in fields, but in
     * member variables gregorianXxx.  Also compute the DAY_OF_WEEK and
     * DOW_LOCAL fields.
     */
    private final void computeGregorianAndDOWFields(int julianDay) {
        computeGregorianFields(julianDay);

        // Compute day of week: JD 0 = Monday
        int dow = fields[DAY_OF_WEEK] = julianDayToDayOfWeek(julianDay);

        // Calculate 1-based localized day of week
        int dowLocal = dow - getFirstDayOfWeek() + 1;
        if (dowLocal < 1) {
            dowLocal += 7;
        }
        fields[DOW_LOCAL] = dowLocal;
    }

    /**
     * Compute the Gregorian calendar year, month, and day of month from the
     * Julian day.  These values are not stored in fields, but in member
     * variables gregorianXxx.  They are used for time zone computations and by
     * subclasses that are Gregorian derivatives.  Subclasses may call this
     * method to perform a Gregorian calendar millis-&gt;fields computation.
     * To perform a Gregorian calendar fields-&gt;millis computation, call
     * computeGregorianMonthStart().
     * @see #computeGregorianMonthStart
     */
    protected final void computeGregorianFields(int julianDay) {
        int year, month, dayOfMonth, dayOfYear;

        // The Gregorian epoch day is zero for Monday January 1, year 1.
        long gregorianEpochDay = julianDay - JAN_1_1_JULIAN_DAY;

        // Here we convert from the day number to the multiple radix
        // representation.  We use 400-year, 100-year, and 4-year cycles.
        // For example, the 4-year cycle has 4 years + 1 leap day; giving
        // 1461 == 365*4 + 1 days.
        int[] rem = new int[1];
        int n400 = floorDivide(gregorianEpochDay, 146097, rem); // 400-year cycle length
        int n100 = floorDivide(rem[0], 36524, rem); // 100-year cycle length
        int n4 = floorDivide(rem[0], 1461, rem); // 4-year cycle length
        int n1 = floorDivide(rem[0], 365, rem);
        year = 400*n400 + 100*n100 + 4*n4 + n1;
        dayOfYear = rem[0]; // zero-based day of year
        if (n100 == 4 || n1 == 4) {
            dayOfYear = 365; // Dec 31 at end of 4- or 400-yr cycle
        } else {
            ++year;
        }

        boolean isLeap = ((year&0x3) == 0) && // equiv. to (year%4 == 0)
                (year%100 != 0 || year%400 == 0);

        int correction = 0;
        int march1 = isLeap ? 60 : 59; // zero-based DOY for March 1
        if (dayOfYear >= march1) correction = isLeap ? 1 : 2;
        month = (12 * (dayOfYear + correction) + 6) / 367; // zero-based month
        dayOfMonth = dayOfYear -
                GREGORIAN_MONTH_COUNT[month][isLeap?3:2] + 1; // one-based DOM

        gregorianYear = year;
        gregorianMonth = month; // 0-based already
        gregorianDayOfMonth = dayOfMonth; // 1-based already
        gregorianDayOfYear = dayOfYear + 1; // Convert from 0-based to 1-based
    }

    /**
     * Compute the fields WEEK_OF_YEAR, YEAR_WOY, WEEK_OF_MONTH,
     * DAY_OF_WEEK_IN_MONTH, and DOW_LOCAL from EXTENDED_YEAR, YEAR,
     * DAY_OF_WEEK, and DAY_OF_YEAR.  The latter fields are computed by the
     * subclass based on the calendar system.
     *
     * <p>The YEAR_WOY field is computed simplistically.  It is equal to YEAR
     * most of the time, but at the year boundary it may be adjusted to YEAR-1
     * or YEAR+1 to reflect the overlap of a week into an adjacent year.  In
     * this case, a simple increment or decrement is performed on YEAR, even
     * though this may yield an invalid YEAR value.  For instance, if the YEAR
     * is part of a calendar system with an N-year cycle field CYCLE, then
     * incrementing the YEAR may involve incrementing CYCLE and setting YEAR
     * back to 0 or 1.  This is not handled by this code, and in fact cannot be
     * simply handled without having subclasses define an entire parallel set of
     * fields for fields larger than or equal to a year.  This additional
     * complexity is not warranted, since the intention of the YEAR_WOY field is
     * to support ISO 8601 notation, so it will typically be used with a
     * proleptic Gregorian calendar, which has no field larger than a year.
     */
    private final void computeWeekFields() {
        int eyear = fields[EXTENDED_YEAR];
        int dayOfWeek = fields[DAY_OF_WEEK];
        int dayOfYear = fields[DAY_OF_YEAR];

        // WEEK_OF_YEAR start
        // Compute the week of the year.  For the Gregorian calendar, valid week
        // numbers run from 1 to 52 or 53, depending on the year, the first day
        // of the week, and the minimal days in the first week.  For other
        // calendars, the valid range may be different -- it depends on the year
        // length.  Days at the start of the year may fall into the last week of
        // the previous year; days at the end of the year may fall into the
        // first week of the next year.  ASSUME that the year length is less than
        // 7000 days.
        int yearOfWeekOfYear = eyear;
        int relDow = (dayOfWeek + 7 - getFirstDayOfWeek()) % 7; // 0..6
        int relDowJan1 = (dayOfWeek - dayOfYear + 7001 - getFirstDayOfWeek()) % 7; // 0..6
        int woy = (dayOfYear - 1 + relDowJan1) / 7; // 0..53
        if ((7 - relDowJan1) >= getMinimalDaysInFirstWeek()) {
            ++woy;
        }

        // Adjust for weeks at the year end that overlap into the previous or
        // next calendar year.
        if (woy == 0) {
            // We are the last week of the previous year.
            // Check to see if we are in the last week; if so, we need
            // to handle the case in which we are the first week of the
            // next year.

            int prevDoy = dayOfYear + handleGetYearLength(eyear - 1);
            woy = weekNumber(prevDoy, dayOfWeek);
            yearOfWeekOfYear--;
        } else {
            int lastDoy = handleGetYearLength(eyear);
            // Fast check: For it to be week 1 of the next year, the DOY
            // must be on or after L-5, where L is yearLength(), then it
            // cannot possibly be week 1 of the next year:
            //          L-5                  L
            // doy: 359 360 361 362 363 364 365 001
            // dow:      1   2   3   4   5   6   7
            if (dayOfYear >= (lastDoy - 5)) {
                int lastRelDow = (relDow + lastDoy - dayOfYear) % 7;
                if (lastRelDow < 0) {
                    lastRelDow += 7;
                }
                if (((6 - lastRelDow) >= getMinimalDaysInFirstWeek()) &&
                        ((dayOfYear + 7 - relDow) > lastDoy)) {
                    woy = 1;
                    yearOfWeekOfYear++;
                }
            }
        }
        fields[WEEK_OF_YEAR] = woy;
        fields[YEAR_WOY] = yearOfWeekOfYear;
        // WEEK_OF_YEAR end

        int dayOfMonth = fields[DAY_OF_MONTH];
        fields[WEEK_OF_MONTH] = weekNumber(dayOfMonth, dayOfWeek);
        fields[DAY_OF_WEEK_IN_MONTH] = (dayOfMonth-1) / 7 + 1;
    }

    //----------------------------------------------------------------------
    // Fields -> Time
    //----------------------------------------------------------------------

    /**
     * Value to OR against resolve table field values for remapping.
     * @see #resolveFields
     */
    protected static final int RESOLVE_REMAP = 32;
    // A power of 2 greater than or equal to MAX_FIELD_COUNT

    // Default table for day in year
    static final int[][][] DATE_PRECEDENCE = {
        {
            { DAY_OF_MONTH },
            { WEEK_OF_YEAR, DAY_OF_WEEK },
            { WEEK_OF_MONTH, DAY_OF_WEEK },
            { DAY_OF_WEEK_IN_MONTH, DAY_OF_WEEK },
            { WEEK_OF_YEAR, DOW_LOCAL },
            { WEEK_OF_MONTH, DOW_LOCAL },
            { DAY_OF_WEEK_IN_MONTH, DOW_LOCAL },
            { DAY_OF_YEAR },
            { RESOLVE_REMAP | DAY_OF_MONTH, YEAR },  // if YEAR is set over YEAR_WOY use DAY_OF_MONTH
            { RESOLVE_REMAP | WEEK_OF_YEAR, YEAR_WOY },  // if YEAR_WOY is set,  calc based on WEEK_OF_YEAR
        },
        {
            { WEEK_OF_YEAR },
            { WEEK_OF_MONTH },
            { DAY_OF_WEEK_IN_MONTH },
            { RESOLVE_REMAP | DAY_OF_WEEK_IN_MONTH, DAY_OF_WEEK },
            { RESOLVE_REMAP | DAY_OF_WEEK_IN_MONTH, DOW_LOCAL },
        },
    };

    static final int[][][] DOW_PRECEDENCE = {
        {
            { DAY_OF_WEEK },
            { DOW_LOCAL },
        },
    };

    /**
     * Given a precedence table, return the newest field combination in
     * the table, or -1 if none is found.
     *
     * <p>The precedence table is a 3-dimensional array of integers.  It
     * may be thought of as an array of groups.  Each group is an array of
     * lines.  Each line is an array of field numbers.  Within a line, if
     * all fields are set, then the time stamp of the line is taken to be
     * the stamp of the most recently set field.  If any field of a line is
     * unset, then the line fails to match.  Within a group, the line with
     * the newest time stamp is selected.  The first field of the line is
     * returned to indicate which line matched.
     *
     * <p>In some cases, it may be desirable to map a line to field that
     * whose stamp is NOT examined.  For example, if the best field is
     * DAY_OF_WEEK then the DAY_OF_WEEK_IN_MONTH algorithm may be used.  In
     * order to do this, insert the value <code>REMAP_RESOLVE | F</code> at
     * the start of the line, where <code>F</code> is the desired return
     * field value.  This field will NOT be examined; it only determines
     * the return value if the other fields in the line are the newest.
     *
     * <p>If all lines of a group contain at least one unset field, then no
     * line will match, and the group as a whole will fail to match.  In
     * that case, the next group will be processed.  If all groups fail to
     * match, then -1 is returned.
     */
    protected int resolveFields(int[][][] precedenceTable) {
        int bestField = -1;
        int tempBestField;
        for (int g=0; g<precedenceTable.length && bestField < 0; ++g) {
            int[][] group = precedenceTable[g];
            int bestStamp = UNSET;
            linesInGroup:
                for (int l=0; l<group.length; ++l) {
                    int[] line= group[l];
                    int lineStamp = UNSET;
                    // Skip over first entry if it is negative
                    for (int i=(line[0]>=RESOLVE_REMAP)?1:0; i<line.length; ++i) {
                        int s = stamp[line[i]];
                        // If any field is unset then don't use this line
                        if (s == UNSET) {
                            continue linesInGroup;
                        } else {
                            lineStamp = Math.max(lineStamp, s);
                        }
                    }
                    // Record new maximum stamp & field no.
                    if (lineStamp > bestStamp) {
                        tempBestField = line[0]; // First field refers to entire line
                        if (tempBestField >= RESOLVE_REMAP) {
                            tempBestField &= (RESOLVE_REMAP-1);
                            // This check is needed to resolve some issues with UCAL_YEAR precedence mapping
                            if (tempBestField != DATE || (stamp[WEEK_OF_MONTH] < stamp[tempBestField])) {
                                bestField = tempBestField;
                            }
                        } else {
                            bestField = tempBestField;
                        }

                        if (bestField == tempBestField) {
                            bestStamp = lineStamp;
                        }
                    }
                }
        }
        return (bestField>=RESOLVE_REMAP)?(bestField&(RESOLVE_REMAP-1)):bestField;
    }

    /**
     * Returns the newest stamp of a given range of fields.
     */
    protected int newestStamp(int first, int last, int bestStampSoFar) {
        int bestStamp = bestStampSoFar;
        for (int i=first; i<=last; ++i) {
            if (stamp[i] > bestStamp) {
                bestStamp = stamp[i];
            }
        }
        return bestStamp;
    }

    /**
     * Returns the timestamp of a field.
     */
    protected final int getStamp(int field) {
        return stamp[field];
    }

    /**
     * Returns the field that is newer, either defaultField, or
     * alternateField.  If neither is newer or neither is set, return defaultField.
     */
    protected int newerField(int defaultField, int alternateField) {
        if (stamp[alternateField] > stamp[defaultField]) {
            return alternateField;
        }
        return defaultField;
    }

    /**
     * Ensure that each field is within its valid range by calling {@link
     * #validateField(int)} on each field that has been set.  This method
     * should only be called if this calendar is not lenient.
     * @see #isLenient
     * @see #validateField(int)
     */
    protected void validateFields() {
        for (int field = 0; field < fields.length; field++) {
            if (stamp[field] >= MINIMUM_USER_STAMP) {
                validateField(field);
            }
        }
    }

    /**
     * Validate a single field of this calendar.  Subclasses should
     * override this method to validate any calendar-specific fields.
     * Generic fields can be handled by
     * <code>Calendar.validateField()</code>.
     * @see #validateField(int, int, int)
     */
    protected void validateField(int field) {
        int y;
        switch (field) {
        case DAY_OF_MONTH:
            y = handleGetExtendedYear();
            validateField(field, 1, handleGetMonthLength(y, internalGet(MONTH)));
            break;
        case DAY_OF_YEAR:
            y = handleGetExtendedYear();
            validateField(field, 1, handleGetYearLength(y));
            break;
        case DAY_OF_WEEK_IN_MONTH:
            if (internalGet(field) == 0) {
                throw new IllegalArgumentException("DAY_OF_WEEK_IN_MONTH cannot be zero");
            }
            validateField(field, getMinimum(field), getMaximum(field));
            break;
        default:
            validateField(field, getMinimum(field), getMaximum(field));
            break;
        }
    }

    /**
     * Validate a single field of this calendar given its minimum and
     * maximum allowed value.  If the field is out of range, throw a
     * descriptive <code>IllegalArgumentException</code>.  Subclasses may
     * use this method in their implementation of {@link
     * #validateField(int)}.
     */
    protected final void validateField(int field, int min, int max) {
        int value = fields[field];
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName(field) +
                    '=' + value + ", valid range=" +
                    min + ".." + max);
        }
    }

    /**
     * Converts the current field values in <code>fields[]</code> to the
     * millisecond time value <code>time</code>.
     */
    protected void computeTime() {
        if (!isLenient()) {
            validateFields();
        }

        // Compute the Julian day
        int julianDay = computeJulianDay();

        long millis = julianDayToMillis(julianDay);

        int millisInDay;

        // We only use MILLISECONDS_IN_DAY if it has been set by the user.
        // This makes it possible for the caller to set the calendar to a
        // time and call clear(MONTH) to reset the MONTH to January.  This
        // is legacy behavior.  Without this, clear(MONTH) has no effect,
        // since the internally set JULIAN_DAY is used.
        if (stamp[MILLISECONDS_IN_DAY] >= MINIMUM_USER_STAMP &&
                newestStamp(AM_PM, MILLISECOND, UNSET) <= stamp[MILLISECONDS_IN_DAY]) {
            millisInDay = internalGet(MILLISECONDS_IN_DAY);
        } else {
            millisInDay = computeMillisInDay();
        }

        if (stamp[ZONE_OFFSET] >= MINIMUM_USER_STAMP ||
                stamp[DST_OFFSET] >= MINIMUM_USER_STAMP) {
            time = millis + millisInDay - (internalGet(ZONE_OFFSET) + internalGet(DST_OFFSET));
        } else {
            // Compute the time zone offset and DST offset.  There are two potential
            // ambiguities here.  We'll assume a 2:00 am (wall time) switchover time
            // for discussion purposes here.
            //
            // 1. The positive offset change such as transition into DST.
            //    Here, a designated time of 2:00 am - 2:59 am does not actually exist.
            //    For this case, skippedWallTime option specifies the behavior.
            //    For example, 2:30 am is interpreted as;
            //      - WALLTIME_LAST(default): 3:30 am (DST) (interpreting 2:30 am as 31 minutes after 1:59 am (STD))
            //      - WALLTIME_FIRST: 1:30 am (STD) (interpreting 2:30 am as 30 minutes before 3:00 am (DST))
            //      - WALLTIME_NEXT_VALID: 3:00 am (DST) (next valid time after 2:30 am on a wall clock)
            // 2. The negative offset change such as transition out of DST.
            //    Here, a designated time of 1:00 am - 1:59 am can be in standard or DST.  Both are valid
            //    representations (the rep jumps from 1:59:59 DST to 1:00:00 Std).
            //    For this case, repeatedWallTime option specifies the behavior.
            //    For example, 1:30 am is interpreted as;
            //      - WALLTIME_LAST(default): 1:30 am (STD) - latter occurrence
            //      - WALLTIME_FIRST: 1:30 am (DST) - former occurrence
            //
            // In addition to above, when calendar is strict (not default), wall time falls into
            // the skipped time range will be processed as an error case.
            //
            // These special cases are mostly handled in #computeZoneOffset(long), except WALLTIME_NEXT_VALID
            // at positive offset change. The protected method computeZoneOffset(long) is exposed to Calendar
            // subclass implementations and marked as @stable. Strictly speaking, WALLTIME_NEXT_VALID
            // should be also handled in the same place, but we cannot change the code flow without deprecating
            // the protected method.
            //
            // We use the TimeZone object, unless the user has explicitly set the ZONE_OFFSET
            // or DST_OFFSET fields; then we use those fields.

            if (!lenient || skippedWallTime == WALLTIME_NEXT_VALID) {
                // When strict, invalidate a wall time falls into a skipped wall time range.
                // When lenient and skipped wall time option is WALLTIME_NEXT_VALID,
                // the result time will be adjusted to the next valid time (on wall clock).
                int zoneOffset = computeZoneOffset(millis, millisInDay);
                long tmpTime = millis + millisInDay - zoneOffset;

                int zoneOffset1 = zone.getOffset(tmpTime);

                // zoneOffset != zoneOffset1 only when the given wall time fall into
                // a skipped wall time range caused by positive zone offset transition.
                if (zoneOffset != zoneOffset1) {
                    if (!lenient) {
                        throw new IllegalArgumentException("The specified wall time does not exist due to time zone offset transition.");
                    }

                    assert skippedWallTime == WALLTIME_NEXT_VALID : skippedWallTime;
                    // Adjust time to the next valid wall clock time.
                    // At this point, tmpTime is on or after the zone offset transition causing
                    // the skipped time range.
                    Long immediatePrevTransition = getImmediatePreviousZoneTransition(tmpTime);
                    if (immediatePrevTransition == null) {
                        throw new RuntimeException("Could not locate a time zone transition before " + tmpTime);
                    }
                    time = immediatePrevTransition;
                } else {
                    time = tmpTime;
                }
            } else {
                time = millis + millisInDay - computeZoneOffset(millis, millisInDay);
            }
        }
    }

    /**
     * Find the previous zone transtion near the given time.
     *
     * @param base The base time, inclusive.
     * @return The time of the previous transition, or null if not found.
     */
    private Long getImmediatePreviousZoneTransition(long base) {
        Long transitionTime = null;

        if (zone instanceof BasicTimeZone) {
            TimeZoneTransition transition = ((BasicTimeZone) zone).getPreviousTransition(base, true);
            if (transition != null) {
                transitionTime = transition.getTime();
            }
        } else {
            // Usually, it is enough to check past one hour because such transition is most
            // likely +1 hour shift. However, there is an example jumped +24 hour in the tz database.
            transitionTime = getPreviousZoneTransitionTime(zone, base, 2 * 60 * 60 * 1000); // check last 2 hours
            if (transitionTime == null) {
                transitionTime = getPreviousZoneTransitionTime(zone, base, 30 * 60 * 60 * 1000); // try last 30 hours
            }
        }
        return transitionTime;
    }

    /**
     * Find the previous zone transition within the specified duration.
     * Note: This method is only used when TimeZone is NOT a BasicTimeZone.
     * @param tz The time zone.
     * @param base The base time, inclusive.
     * @param duration The range of time evaluated.
     * @return The time of the previous zone transition, or null if not available.
     */
    private static Long getPreviousZoneTransitionTime(TimeZone tz, long base, long duration) {
        assert duration > 0;

        long upper = base;
        long lower = base - duration - 1;
        int offsetU = tz.getOffset(upper);
        int offsetL = tz.getOffset(lower);
        if (offsetU == offsetL) {
            return null;
        }
        return findPreviousZoneTransitionTime(tz, offsetU, upper, lower);
    }

    /**
     * The time units used by {@link #findPreviousZoneTransitionTime(TimeZone, int, long, long)}
     * for optimizing transition time binary search.
     */
    private static final int[] FIND_ZONE_TRANSITION_TIME_UNITS = {
        60*60*1000, // 1 hour
        30*60*1000, // 30 minutes
        60*1000,    // 1 minute
        1000,       // 1 second
    };

    /**
     * Implementing binary search for zone transtion detection, used by {@link #getPreviousZoneTransitionTime(TimeZone, long, long)}
     * @param tz The time zone.
     * @param upperOffset The zone offset at <code>upper</code>
     * @param upper The upper bound, inclusive.
     * @param lower The lower bound, exclusive.
     * @return The time of the previous zone transition, or null if not available.
     */
    private static Long findPreviousZoneTransitionTime(TimeZone tz, int upperOffset, long upper, long lower) {
        boolean onUnitTime = false;
        long mid = 0;

        for (int unit : FIND_ZONE_TRANSITION_TIME_UNITS) {
            long lunits = lower/unit;
            long uunits = upper/unit;
            if (uunits > lunits) {
                mid = ((lunits + uunits + 1) >>> 1) * unit;
                onUnitTime = true;
                break;
            }
        }

        int midOffset;
        if (!onUnitTime) {
            mid = (upper + lower) >>> 1;
        }

        if (onUnitTime) {
            if (mid != upper) {
                midOffset  = tz.getOffset(mid);
                if (midOffset != upperOffset) {
                    return findPreviousZoneTransitionTime(tz, upperOffset, upper, mid);
                }
                upper = mid;
            }
            // check mid-1
            mid--;
        } else {
            mid = (upper + lower) >>> 1;
        }

        if (mid == lower) {
            return Long.valueOf(upper);
        }
        midOffset = tz.getOffset(mid);
        if (midOffset != upperOffset) {
            if (onUnitTime) {
                return Long.valueOf(upper);
            }
            return findPreviousZoneTransitionTime(tz, upperOffset, upper, mid);
        }
        return findPreviousZoneTransitionTime(tz, upperOffset, mid, lower);
    }

    /**
     * Compute the milliseconds in the day from the fields.  This is a
     * value from 0 to 23:59:59.999 inclusive, unless fields are out of
     * range, in which case it can be an arbitrary value.  This value
     * reflects local zone wall time.
     * @deprecated This method suffers from a potential integer overflow and may be removed or
     *     changed in a future release. See <a href="http://bugs.icu-project.org/trac/ticket/11632">
     *     ICU ticket #11632</a> for details.
     */
    protected int computeMillisInDay() {
        // Do the time portion of the conversion.

        int millisInDay = 0;

        // Find the best set of fields specifying the time of day.  There
        // are only two possibilities here; the HOUR_OF_DAY or the
        // AM_PM and the HOUR.
        int hourOfDayStamp = stamp[HOUR_OF_DAY];
        int hourStamp = Math.max(stamp[HOUR], stamp[AM_PM]);
        int bestStamp = (hourStamp > hourOfDayStamp) ? hourStamp : hourOfDayStamp;

        // Hours
        if (bestStamp != UNSET) {
            if (bestStamp == hourOfDayStamp) {
                // Don't normalize here; let overflow bump into the next period.
                // This is consistent with how we handle other fields.
                millisInDay += internalGet(HOUR_OF_DAY);
            } else {
                // Don't normalize here; let overflow bump into the next period.
                // This is consistent with how we handle other fields.
                millisInDay += internalGet(HOUR);
                millisInDay += 12 * internalGet(AM_PM); // Default works for unset AM_PM
            }
        }

        // We use the fact that unset == 0; we start with millisInDay
        // == HOUR_OF_DAY.
        millisInDay *= 60;
        millisInDay += internalGet(MINUTE); // now have minutes
        millisInDay *= 60;
        millisInDay += internalGet(SECOND); // now have seconds
        millisInDay *= 1000;
        millisInDay += internalGet(MILLISECOND); // now have millis

        return millisInDay;
    }

    /**
     * This method can assume EXTENDED_YEAR has been set.
     * @param millis milliseconds of the date fields (local midnight millis)
     * @param millisInDay milliseconds of the time fields; may be out
     * or range.
     * @return total zone offset (raw + DST) for the given moment
     * @deprecated This method suffers from a potential integer overflow and may be removed or
     *     changed in a future release. See <a href="http://bugs.icu-project.org/trac/ticket/11632">
     *     ICU ticket #11632</a> for details.
     */
    protected int computeZoneOffset(long millis, int millisInDay) {
        int[] offsets = new int[2];
        long wall = millis + millisInDay;
        if (zone instanceof BasicTimeZone) {
            int duplicatedTimeOpt = (repeatedWallTime == WALLTIME_FIRST) ? BasicTimeZone.LOCAL_FORMER : BasicTimeZone.LOCAL_LATTER;
            int nonExistingTimeOpt = (skippedWallTime == WALLTIME_FIRST) ? BasicTimeZone.LOCAL_LATTER : BasicTimeZone.LOCAL_FORMER;
            ((BasicTimeZone)zone).getOffsetFromLocal(wall, nonExistingTimeOpt, duplicatedTimeOpt, offsets);
        } else {
            // By default, TimeZone#getOffset behaves WALLTIME_LAST for both.
            zone.getOffset(wall, true, offsets);

            boolean sawRecentNegativeShift = false;
            if (repeatedWallTime == WALLTIME_FIRST) {
                // Check if the given wall time falls into repeated time range
                long tgmt = wall - (offsets[0] + offsets[1]);

                // Any negative zone transition within last 6 hours?
                // Note: The maximum historic negative zone transition is -3 hours in the tz database.
                // 6 hour window would be sufficient for this purpose.
                int offsetBefore6 = zone.getOffset(tgmt - 6*60*60*1000);
                int offsetDelta = (offsets[0] + offsets[1]) - offsetBefore6;

                assert offsetDelta > -6*60*60*1000 : offsetDelta;
                if (offsetDelta < 0) {
                    sawRecentNegativeShift = true;
                    // Negative shift within last 6 hours. When WALLTIME_FIRST is used and the given wall time falls
                    // into the repeated time range, use offsets before the transition.
                    // Note: If it does not fall into the repeated time range, offsets remain unchanged below.
                    zone.getOffset(wall + offsetDelta, true, offsets);
                }
            }
            if (!sawRecentNegativeShift && skippedWallTime == WALLTIME_FIRST) {
                // When skipped wall time option is WALLTIME_FIRST,
                // recalculate offsets from the resolved time (non-wall).
                // When the given wall time falls into skipped wall time,
                // the offsets will be based on the zone offsets AFTER
                // the transition (which means, earliest possibe interpretation).
                long tgmt = wall - (offsets[0] + offsets[1]);
                zone.getOffset(tgmt, false, offsets);
            }
        }
        return offsets[0] + offsets[1];
    }

    /**
     * Compute the Julian day number as specified by this calendar's fields.
     */
    protected int computeJulianDay() {

        // We want to see if any of the date fields is newer than the
        // JULIAN_DAY.  If not, then we use JULIAN_DAY.  If so, then we do
        // the normal resolution.  We only use JULIAN_DAY if it has been
        // set by the user.  This makes it possible for the caller to set
        // the calendar to a time and call clear(MONTH) to reset the MONTH
        // to January.  This is legacy behavior.  Without this,
        // clear(MONTH) has no effect, since the internally set JULIAN_DAY
        // is used.
        if (stamp[JULIAN_DAY] >= MINIMUM_USER_STAMP) {
            int bestStamp = newestStamp(ERA, DAY_OF_WEEK_IN_MONTH, UNSET);
            bestStamp = newestStamp(YEAR_WOY, EXTENDED_YEAR, bestStamp);
            if (bestStamp <= stamp[JULIAN_DAY]) {
                return internalGet(JULIAN_DAY);
            }
        }

        int bestField = resolveFields(getFieldResolutionTable());
        if (bestField < 0) {
            bestField = DAY_OF_MONTH;
        }

        return handleComputeJulianDay(bestField);
    }

    /**
     * Returns the field resolution array for this calendar.  Calendars that
     * define additional fields or change the semantics of existing fields
     * should override this method to adjust the field resolution semantics
     * accordingly.  Other subclasses should not override this method.
     * @see #resolveFields
     */
    protected int[][][] getFieldResolutionTable() {
        return DATE_PRECEDENCE;
    }

    /**
     * Returns the Julian day number of day before the first day of the
     * given month in the given extended year.  Subclasses should override
     * this method to implement their calendar system.
     * @param eyear the extended year
     * @param month the zero-based month, or 0 if useMonth is false
     * @param useMonth if false, compute the day before the first day of
     * the given year, otherwise, compute the day before the first day of
     * the given month
     * @return the Julian day number of the day before the first
     * day of the given month and year
     */
    abstract protected int handleComputeMonthStart(int eyear, int month,
            boolean useMonth);

    /**
     * Returns the extended year defined by the current fields.  This will
     * use the EXTENDED_YEAR field or the YEAR and supra-year fields (such
     * as ERA) specific to the calendar system, depending on which set of
     * fields is newer.
     * @return the extended year
     */
    abstract protected int handleGetExtendedYear();

    // (The following method is not called because all existing subclasses
    // override it.  2003-06-11 ICU 2.6 Alan)
    ///CLOVER:OFF
    /**
     * Returns the number of days in the given month of the given extended
     * year of this calendar system.  Subclasses should override this
     * method if they can provide a more correct or more efficient
     * implementation than the default implementation in Calendar.
     */
    protected int handleGetMonthLength(int extendedYear, int month) {
        return handleComputeMonthStart(extendedYear, month+1, true) -
                handleComputeMonthStart(extendedYear, month, true);
    }
    ///CLOVER:ON

    /**
     * Returns the number of days in the given extended year of this
     * calendar system.  Subclasses should override this method if they can
     * provide a more correct or more efficient implementation than the
     * default implementation in Calendar.
     */
    protected int handleGetYearLength(int eyear) {
        return handleComputeMonthStart(eyear+1, 0, false) -
                handleComputeMonthStart(eyear, 0, false);
    }

    /**
     * Subclasses that use additional fields beyond those defined in
     * <code>Calendar</code> should override this method to return an
     * <code>int[]</code> array of the appropriate length.  The length
     * must be at least <code>BASE_FIELD_COUNT</code> and no more than
     * <code>MAX_FIELD_COUNT</code>.
     */
    protected int[] handleCreateFields() {
        return new int[BASE_FIELD_COUNT];
    }

    /**
     * Subclasses may override this.
     * Called by handleComputeJulianDay.  Returns the default month (0-based) for the year,
     * taking year and era into account.  Defaults to 0 (JANUARY) for Gregorian.
     * @param extendedYear the extendedYear, as returned by handleGetExtendedYear
     * @return the default month
     * @see #MONTH
     * @hide draft / provisional / internal are hidden on Android
     */
    protected int getDefaultMonthInYear(int extendedYear) {
        return Calendar.JANUARY;
    }

    /**
     * Subclasses may override this.
     * Called by handleComputeJulianDay.  Returns the default day (1-based) for the month,
     * taking currently-set year and era into account.  Defaults to 1 for Gregorian.
     * @param extendedYear the extendedYear, as returned by handleGetExtendedYear
     * @param month the month, as returned by getDefaultMonthInYear
     * @return the default day of the month
     * @see #DAY_OF_MONTH
     * @hide draft / provisional / internal are hidden on Android
     */
    protected int getDefaultDayInMonth(int extendedYear, int month) {
        return 1;
    }


    /**
     * Subclasses may override this.  This method calls
     * handleGetMonthLength() to obtain the calendar-specific month
     * length.
     */
    protected int handleComputeJulianDay(int bestField) {

        boolean useMonth = (bestField == DAY_OF_MONTH ||
                bestField == WEEK_OF_MONTH ||
                bestField == DAY_OF_WEEK_IN_MONTH);

        int year;

        if (bestField == WEEK_OF_YEAR) {
            // Nota Bene!  It is critical that YEAR_WOY be used as the year here, if it is
            // set.  Otherwise, when WOY is the best field, the year may be wrong at the
            // extreme limits of the year.  If YEAR_WOY is not set then it will fall back.
            // TODO: Should resolveField(YEAR_PRECEDENCE) be brought to bear?
            year = internalGet(YEAR_WOY, handleGetExtendedYear());
        } else {
            year = handleGetExtendedYear();
        }

        internalSet(EXTENDED_YEAR, year);

        int month = useMonth ? internalGet(MONTH, getDefaultMonthInYear(year)) : 0;

        // Get the Julian day of the day BEFORE the start of this year.
        // If useMonth is true, get the day before the start of the month.
        int julianDay = handleComputeMonthStart(year, month, useMonth);

        if (bestField == DAY_OF_MONTH) {
            if(isSet(DAY_OF_MONTH)) {
                return julianDay + internalGet(DAY_OF_MONTH, getDefaultDayInMonth(year, month));
            } else {
                return julianDay + getDefaultDayInMonth(year, month);
            }
        }

        if (bestField == DAY_OF_YEAR) {
            return julianDay + internalGet(DAY_OF_YEAR);
        }

        int firstDOW = getFirstDayOfWeek(); // Localized fdw

        // At this point julianDay is the 0-based day BEFORE the first day of
        // January 1, year 1 of the given calendar.  If julianDay == 0, it
        // specifies (Jan. 1, 1) - 1, in whatever calendar we are using (Julian
        // or Gregorian).

        // At this point we need to process the WEEK_OF_MONTH or
        // WEEK_OF_YEAR, which are similar, or the DAY_OF_WEEK_IN_MONTH.
        // First, perform initial shared computations.  These locate the
        // first week of the period.

        // Get the 0-based localized DOW of day one of the month or year.
        // Valid range 0..6.
        int first = julianDayToDayOfWeek(julianDay + 1) - firstDOW;
        if (first < 0) {
            first += 7;
        }

        // Get zero-based localized DOW, valid range 0..6.  This is the DOW
        // we are looking for.
        int dowLocal = 0;
        switch (resolveFields(DOW_PRECEDENCE)) {
        case DAY_OF_WEEK:
            dowLocal = internalGet(DAY_OF_WEEK) - firstDOW;
            break;
        case DOW_LOCAL:
            dowLocal = internalGet(DOW_LOCAL) - 1;
            break;
        }
        dowLocal = dowLocal % 7;
        if (dowLocal < 0) {
            dowLocal += 7;
        }

        // Find the first target DOW (dowLocal) in the month or year.
        // Actually, it may be just before the first of the month or year.
        // It will be an integer from -5..7.
        int date = 1 - first + dowLocal;

        if (bestField == DAY_OF_WEEK_IN_MONTH) {

            // Adjust the target DOW to be in the month or year.
            if (date < 1) {
                date += 7;
            }

            // The only trickiness occurs if the day-of-week-in-month is
            // negative.
            int dim = internalGet(DAY_OF_WEEK_IN_MONTH, 1);
            if (dim >= 0) {
                date += 7*(dim - 1);

            } else {
                // Move date to the last of this day-of-week in this month,
                // then back up as needed.  If dim==-1, we don't back up at
                // all.  If dim==-2, we back up once, etc.  Don't back up
                // past the first of the given day-of-week in this month.
                // Note that we handle -2, -3, etc. correctly, even though
                // values < -1 are technically disallowed.
                int m = internalGet(MONTH, JANUARY);
                int monthLength = handleGetMonthLength(year, m);
                date += ((monthLength - date) / 7 + dim + 1) * 7;
            }
        } else {
            // assert(bestField == WEEK_OF_MONTH || bestField == WEEK_OF_YEAR)

            // Adjust for minimal days in first week
            if ((7 - first) < getMinimalDaysInFirstWeek()) {
                date += 7;
            }

            // Now adjust for the week number.
            date += 7 * (internalGet(bestField) - 1);
        }

        return julianDay + date;
    }

    /**
     * Compute the Julian day of a month of the Gregorian calendar.
     * Subclasses may call this method to perform a Gregorian calendar
     * fields-&gt;millis computation.  To perform a Gregorian calendar
     * millis-&gt;fields computation, call computeGregorianFields().
     * @param year extended Gregorian year
     * @param month zero-based Gregorian month
     * @return the Julian day number of the day before the first
     * day of the given month in the given extended year
     * @see #computeGregorianFields
     */
    protected int computeGregorianMonthStart(int year, int month) {

        // If the month is out of range, adjust it into range, and
        // modify the extended year value accordingly.
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            year += floorDivide(month, 12, rem);
            month = rem[0];
        }

        boolean isLeap = (year%4 == 0) && ((year%100 != 0) || (year%400 == 0));
        int y = year - 1;
        // This computation is actually ... + (JAN_1_1_JULIAN_DAY - 3) + 2.
        // Add 2 because Gregorian calendar starts 2 days after Julian
        // calendar.
        int julianDay = 365*y + floorDivide(y, 4) - floorDivide(y, 100) +
                floorDivide(y, 400) + JAN_1_1_JULIAN_DAY - 1;

        // At this point julianDay indicates the day BEFORE the first day
        // of January 1, <eyear> of the Gregorian calendar.
        if (month != 0) {
            julianDay += GREGORIAN_MONTH_COUNT[month][isLeap?3:2];
        }

        return julianDay;
    }

    //----------------------------------------------------------------------
    // Subclass API
    // For subclasses to override
    //----------------------------------------------------------------------

    // (The following method is not called because all existing subclasses
    // override it.  2003-06-11 ICU 2.6 Alan)
    ///CLOVER:OFF
    /**
     * Subclasses may override this method to compute several fields
     * specific to each calendar system.  These are:
     *
     * <ul><li>ERA
     * <li>YEAR
     * <li>MONTH
     * <li>DAY_OF_MONTH
     * <li>DAY_OF_YEAR
     * <li>EXTENDED_YEAR</ul>
     *
     * Subclasses can refer to the DAY_OF_WEEK and DOW_LOCAL fields, which
     * will be set when this method is called.  Subclasses can also call
     * the getGregorianXxx() methods to obtain Gregorian calendar
     * equivalents for the given Julian day.
     *
     * <p>In addition, subclasses should compute any subclass-specific
     * fields, that is, fields from BASE_FIELD_COUNT to
     * getFieldCount() - 1.
     *
     * <p>The default implementation in <code>Calendar</code> implements
     * a pure proleptic Gregorian calendar.
     */
    protected void handleComputeFields(int julianDay) {
        internalSet(MONTH, getGregorianMonth());
        internalSet(DAY_OF_MONTH, getGregorianDayOfMonth());
        internalSet(DAY_OF_YEAR, getGregorianDayOfYear());
        int eyear = getGregorianYear();
        internalSet(EXTENDED_YEAR, eyear);
        int era = GregorianCalendar.AD;
        if (eyear < 1) {
            era = GregorianCalendar.BC;
            eyear = 1 - eyear;
        }
        internalSet(ERA, era);
        internalSet(YEAR, eyear);
    }
    ///CLOVER:ON

    //----------------------------------------------------------------------
    // Subclass API
    // For subclasses to call
    //----------------------------------------------------------------------

    /**
     * Returns the extended year on the Gregorian calendar as computed by
     * <code>computeGregorianFields()</code>.
     * @see #computeGregorianFields
     */
    protected final int getGregorianYear() {
        return gregorianYear;
    }

    /**
     * Returns the month (0-based) on the Gregorian calendar as computed by
     * <code>computeGregorianFields()</code>.
     * @see #computeGregorianFields
     */
    protected final int getGregorianMonth() {
        return gregorianMonth;
    }

    /**
     * Returns the day of year (1-based) on the Gregorian calendar as
     * computed by <code>computeGregorianFields()</code>.
     * @see #computeGregorianFields
     */
    protected final int getGregorianDayOfYear() {
        return gregorianDayOfYear;
    }

    /**
     * Returns the day of month (1-based) on the Gregorian calendar as
     * computed by <code>computeGregorianFields()</code>.
     * @see #computeGregorianFields
     */
    protected final int getGregorianDayOfMonth() {
        return gregorianDayOfMonth;
    }

    /**
     * <strong>[icu]</strong> Returns the number of fields defined by this calendar.  Valid field
     * arguments to <code>set()</code> and <code>get()</code> are
     * <code>0..getFieldCount()-1</code>.
     */
    public final int getFieldCount() {
        return fields.length;
    }

    /**
     * Set a field to a value.  Subclasses should use this method when
     * computing fields.  It sets the time stamp in the
     * <code>stamp[]</code> array to <code>INTERNALLY_SET</code>.  If a
     * field that may not be set by subclasses is passed in, an
     * <code>IllegalArgumentException</code> is thrown.  This prevents
     * subclasses from modifying fields that are intended to be
     * calendar-system invariant.
     */
    protected final void internalSet(int field, int value) {
        if (((1 << field) & internalSetMask) == 0) {
            throw new IllegalStateException("Subclass cannot set " +
                    fieldName(field));
        }
        fields[field] = value;
        stamp[field] = INTERNALLY_SET;
    }

    private static final int[][] GREGORIAN_MONTH_COUNT = {
        //len len2   st  st2
        {  31,  31,   0,   0 }, // Jan
        {  28,  29,  31,  31 }, // Feb
        {  31,  31,  59,  60 }, // Mar
        {  30,  30,  90,  91 }, // Apr
        {  31,  31, 120, 121 }, // May
        {  30,  30, 151, 152 }, // Jun
        {  31,  31, 181, 182 }, // Jul
        {  31,  31, 212, 213 }, // Aug
        {  30,  30, 243, 244 }, // Sep
        {  31,  31, 273, 274 }, // Oct
        {  30,  30, 304, 305 }, // Nov
        {  31,  31, 334, 335 }  // Dec
        // len  length of month
        // len2 length of month in a leap year
        // st   days in year before start of month
        // st2  days in year before month in leap year
    };

    /**
     * Determines if the given year is a leap year. Returns true if the
     * given year is a leap year.
     * @param year the given year.
     * @return true if the given year is a leap year; false otherwise.
     */
    protected static final boolean isGregorianLeapYear(int year) {
        return (year%4 == 0) && ((year%100 != 0) || (year%400 == 0));
    }

    /**
     * Returns the length of a month of the Gregorian calendar.
     * @param y the extended year
     * @param m the 0-based month number
     * @return the number of days in the given month
     */
    protected static final int gregorianMonthLength(int y, int m) {
        return GREGORIAN_MONTH_COUNT[m][isGregorianLeapYear(y)?1:0];
    }

    /**
     * Returns the length of a previous month of the Gregorian calendar.
     * @param y the extended year
     * @param m the 0-based month number
     * @return the number of days in the month previous to the given month
     */
    protected static final int gregorianPreviousMonthLength(int y, int m) {
        return (m > 0) ? gregorianMonthLength(y, m-1) : 31;
    }

    /**
     * Divide two long integers, returning the floor of the quotient.
     * <p>
     * Unlike the built-in division, this is mathematically well-behaved.
     * E.g., <code>-1/4</code> =&gt; 0
     * but <code>floorDivide(-1,4)</code> =&gt; -1.
     * @param numerator the numerator
     * @param denominator a divisor which must be &gt; 0
     * @return the floor of the quotient.
     */
    protected static final long floorDivide(long numerator, long denominator) {
        // We do this computation in order to handle
        // a numerator of Long.MIN_VALUE correctly
        return (numerator >= 0) ?
                numerator / denominator :
                    ((numerator + 1) / denominator) - 1;
    }

    /**
     * Divide two integers, returning the floor of the quotient.
     * <p>
     * Unlike the built-in division, this is mathematically well-behaved.
     * E.g., <code>-1/4</code> =&gt; 0
     * but <code>floorDivide(-1,4)</code> =&gt; -1.
     * @param numerator the numerator
     * @param denominator a divisor which must be &gt; 0
     * @return the floor of the quotient.
     */
    protected static final int floorDivide(int numerator, int denominator) {
        // We do this computation in order to handle
        // a numerator of Integer.MIN_VALUE correctly
        return (numerator >= 0) ?
                numerator / denominator :
                    ((numerator + 1) / denominator) - 1;
    }

    /**
     * Divide two integers, returning the floor of the quotient, and
     * the modulus remainder.
     * <p>
     * Unlike the built-in division, this is mathematically well-behaved.
     * E.g., <code>-1/4</code> =&gt; 0 and <code>-1%4</code> =&gt; -1,
     * but <code>floorDivide(-1,4)</code> =&gt; -1 with <code>remainder[0]</code> =&gt; 3.
     * @param numerator the numerator
     * @param denominator a divisor which must be &gt; 0
     * @param remainder an array of at least one element in which the value
     * <code>numerator mod denominator</code> is returned. Unlike <code>numerator
     * % denominator</code>, this will always be non-negative.
     * @return the floor of the quotient.
     */
    protected static final int floorDivide(int numerator, int denominator, int[] remainder) {
        if (numerator >= 0) {
            remainder[0] = numerator % denominator;
            return numerator / denominator;
        }
        int quotient = ((numerator + 1) / denominator) - 1;
        remainder[0] = numerator - (quotient * denominator);
        return quotient;
    }

    /**
     * Divide two integers, returning the floor of the quotient, and
     * the modulus remainder.
     * <p>
     * Unlike the built-in division, this is mathematically well-behaved.
     * E.g., <code>-1/4</code> =&gt; 0 and <code>-1%4</code> =&gt; -1,
     * but <code>floorDivide(-1,4)</code> =&gt; -1 with <code>remainder[0]</code> =&gt; 3.
     * @param numerator the numerator
     * @param denominator a divisor which must be &gt; 0
     * @param remainder an array of at least one element in which the value
     * <code>numerator mod denominator</code> is returned. Unlike <code>numerator
     * % denominator</code>, this will always be non-negative.
     * @return the floor of the quotient.
     */
    protected static final int floorDivide(long numerator, int denominator, int[] remainder) {
        if (numerator >= 0) {
            remainder[0] = (int)(numerator % denominator);
            return (int)(numerator / denominator);
        }
        int quotient = (int)(((numerator + 1) / denominator) - 1);
        remainder[0] = (int)(numerator - ((long)quotient * denominator));
        return quotient;
    }

    private static final String[] FIELD_NAME = {
        "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH",
        "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK",
        "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY",
        "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET",
        "DST_OFFSET", "YEAR_WOY", "DOW_LOCAL", "EXTENDED_YEAR",
        "JULIAN_DAY", "MILLISECONDS_IN_DAY",
    };

    /**
     * Returns a string name for a field, for debugging and exceptions.
     */
    protected String fieldName(int field) {
        try {
            return FIELD_NAME[field];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Field " + field;
        }
    }

    /**
     * Converts time as milliseconds to Julian day.
     * @param millis the given milliseconds.
     * @return the Julian day number.
     */
    protected static final int millisToJulianDay(long millis) {
        return (int) (EPOCH_JULIAN_DAY + floorDivide(millis, ONE_DAY));
    }

    /**
     * Converts Julian day to time as milliseconds.
     * @param julian the given Julian day number.
     * @return time as milliseconds.
     */
    protected static final long julianDayToMillis(int julian) {
        return (julian - EPOCH_JULIAN_DAY) * ONE_DAY;
    }

    /**
     * Returns the day of week, from SUNDAY to SATURDAY, given a Julian day.
     */
    protected static final int julianDayToDayOfWeek(int julian) {
        // If julian is negative, then julian%7 will be negative, so we adjust
        // accordingly.  Julian day 0 is Monday.
        int dayOfWeek = (julian + MONDAY) % 7;
        if (dayOfWeek < SUNDAY) {
            dayOfWeek += 7;
        }
        return dayOfWeek;
    }

    /**
     * Returns the current milliseconds without recomputing.
     */
    protected final long internalGetTimeInMillis() {
        return time;
    }

    /**
     * <strong>[icu]</strong> Returns the calendar type name string for this Calendar object.
     * The returned string is the legacy ICU calendar attribute value,
     * for example, "gregorian" or "japanese".
     *
     * <p>See type="old type name" for the calendar attribute of locale IDs
     * at http://www.unicode.org/reports/tr35/#Key_Type_Definitions
     *
     * @return legacy calendar type name string
     */
    public String getType() {
        return "unknown";
    }

    /**
     * Returns if two digit representation of year in this calendar type
     * customarily implies a default century (i.e. 03 -&gt; 2003).
     * The default implementation returns <code>true</code>. A subclass may
     * return <code>false</code> if such practice is not applicable (for example,
     * Chinese calendar and Japanese calendar).
     *
     * @return <code>true</code> if this calendar has a default century.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public boolean haveDefaultCentury() {
        return true;
    }

    // -------- BEGIN ULocale boilerplate --------

    /**
     * <strong>[icu]</strong> Returns the locale that was used to create this object, or null.
     * This may may differ from the locale requested at the time of
     * this object's creation.  For example, if an object is created
     * for locale <tt>en_US_CALIFORNIA</tt>, the actual data may be
     * drawn from <tt>en</tt> (the <i>actual</i> locale), and
     * <tt>en_US</tt> may be the most specific locale that exists (the
     * <i>valid</i> locale).
     *
     * <p>Note: This method will be implemented in ICU 3.0; ICU 2.8
     * contains a partial preview implementation.  The * <i>actual</i>
     * locale is returned correctly, but the <i>valid</i> locale is
     * not, in most cases.
     * @param type type of information requested, either {@link
     * android.icu.util.ULocale#VALID_LOCALE} or {@link
     * android.icu.util.ULocale#ACTUAL_LOCALE}.
     * @return the information specified by <i>type</i>, or null if
     * this object was not constructed from locale data.
     * @see android.icu.util.ULocale
     * @see android.icu.util.ULocale#VALID_LOCALE
     * @see android.icu.util.ULocale#ACTUAL_LOCALE
     * @hide draft / provisional / internal are hidden on Android
     */
    public final ULocale getLocale(ULocale.Type type) {
        return type == ULocale.ACTUAL_LOCALE ?
                this.actualLocale : this.validLocale;
    }

    /**
     * Set information about the locales that were used to create this
     * object.  If the object was not constructed from locale data,
     * both arguments should be set to null.  Otherwise, neither
     * should be null.  The actual locale must be at the same level or
     * less specific than the valid locale.  This method is intended
     * for use by factories or other entities that create objects of
     * this class.
     * @param valid the most specific locale containing any resource
     * data, or null
     * @param actual the locale containing data used to construct this
     * object, or null
     * @see android.icu.util.ULocale
     * @see android.icu.util.ULocale#VALID_LOCALE
     * @see android.icu.util.ULocale#ACTUAL_LOCALE
     */
    final void setLocale(ULocale valid, ULocale actual) {
        // Change the following to an assertion later
        if ((valid == null) != (actual == null)) {
            ///CLOVER:OFF
            throw new IllegalArgumentException();
            ///CLOVER:ON
        }
        // Another check we could do is that the actual locale is at
        // the same level or less specific than the valid locale.
        this.validLocale = valid;
        this.actualLocale = actual;
    }

    /**
     * The most specific locale containing any resource data, or null.
     * @see android.icu.util.ULocale
     */
    private ULocale validLocale;

    /**
     * The locale containing data used to construct this object, or
     * null.
     * @see android.icu.util.ULocale
     */
    private ULocale actualLocale;

    // -------- END ULocale boilerplate --------
}
