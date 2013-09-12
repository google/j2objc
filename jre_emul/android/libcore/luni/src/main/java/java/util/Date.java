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
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import libcore.icu.LocaleData;

/**
 * A specific moment in time, with millisecond precision. Values typically come
 * from {@link System#currentTimeMillis}, and are always UTC, regardless of the
 * system's time zone. This is often called "Unix time" or "epoch time".
 *
 * <p>Instances of this class are suitable for comparison, but little else.
 * Use {@link java.text.DateFormat} to format a {@code Date} for display to a human.
 * Use {@link Calendar} to break down a {@code Date} if you need to extract fields such
 * as the current month or day of week, or to construct a {@code Date} from a broken-down
 * time. That is: this class' deprecated display-related functionality is now provided
 * by {@code DateFormat}, and this class' deprecated computational functionality is
 * now provided by {@code Calendar}. Both of these other classes (and their subclasses)
 * allow you to interpret a {@code Date} in a given time zone.
 *
 * <p>Note that, surprisingly, instances of this class are mutable.
 */
public class Date implements Serializable, Cloneable, Comparable<Date> {

    private static final long serialVersionUID = 7523967970034938905L;

    // Used by parse()
    private static final int CREATION_YEAR = new Date().getYear();

    private transient long milliseconds;

    /**
     * Initializes this {@code Date} instance to the current time.
     */
    public Date() {
        this(System.currentTimeMillis());
    }

    /**
     * Constructs a new {@code Date} initialized to midnight in the default {@code TimeZone} on
     * the specified date.
     *
     * @param year
     *            the year, 0 is 1900.
     * @param month
     *            the month, 0 - 11.
     * @param day
     *            the day of the month, 1 - 31.
     *
     * @deprecated use
     *             {@link GregorianCalendar#GregorianCalendar(int, int, int)}
     */
    @Deprecated
    public Date(int year, int month, int day) {
        GregorianCalendar cal = new GregorianCalendar(false);
        cal.set(1900 + year, month, day);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Constructs a new {@code Date} initialized to the specified date and time in the
     * default {@code TimeZone}.
     *
     * @param year
     *            the year, 0 is 1900.
     * @param month
     *            the month, 0 - 11.
     * @param day
     *            the day of the month, 1 - 31.
     * @param hour
     *            the hour of day, 0 - 23.
     * @param minute
     *            the minute of the hour, 0 - 59.
     *
     * @deprecated use
     *             {@link GregorianCalendar#GregorianCalendar(int, int, int, int, int)}
     */
    @Deprecated
    public Date(int year, int month, int day, int hour, int minute) {
        GregorianCalendar cal = new GregorianCalendar(false);
        cal.set(1900 + year, month, day, hour, minute);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Constructs a new {@code Date} initialized to the specified date and time in the
     * default {@code TimeZone}.
     *
     * @param year
     *            the year, 0 is 1900.
     * @param month
     *            the month, 0 - 11.
     * @param day
     *            the day of the month, 1 - 31.
     * @param hour
     *            the hour of day, 0 - 23.
     * @param minute
     *            the minute of the hour, 0 - 59.
     * @param second
     *            the second of the minute, 0 - 59.
     *
     * @deprecated use
     *             {@link GregorianCalendar#GregorianCalendar(int, int, int, int, int, int)}
     */
    @Deprecated
    public Date(int year, int month, int day, int hour, int minute, int second) {
        GregorianCalendar cal = new GregorianCalendar(false);
        cal.set(1900 + year, month, day, hour, minute, second);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Initializes this {@code Date} instance using the specified millisecond value. The
     * value is the number of milliseconds since Jan. 1, 1970 GMT.
     *
     * @param milliseconds
     *            the number of milliseconds since Jan. 1, 1970 GMT.
     */
    public Date(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    /**
     * Constructs a new {@code Date} initialized to the date and time parsed from the
     * specified String.
     *
     * @param string
     *            the String to parse.
     *
     * @deprecated use {@link DateFormat}
     */
    @Deprecated
    public Date(String string) {
        milliseconds = parse(string);
    }

    /**
     * Returns if this {@code Date} is after the specified Date.
     *
     * @param date
     *            a Date instance to compare.
     * @return {@code true} if this {@code Date} is after the specified {@code Date},
     *         {@code false} otherwise.
     */
    public boolean after(Date date) {
        return milliseconds > date.milliseconds;
    }

    /**
     * Returns if this {@code Date} is before the specified Date.
     *
     * @param date
     *            a {@code Date} instance to compare.
     * @return {@code true} if this {@code Date} is before the specified {@code Date},
     *         {@code false} otherwise.
     */
    public boolean before(Date date) {
        return milliseconds < date.milliseconds;
    }

    /**
     * Returns a new {@code Date} with the same millisecond value as this {@code Date}.
     *
     * @return a shallow copy of this {@code Date}.
     *
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Compare the receiver to the specified {@code Date} to determine the relative
     * ordering.
     *
     * @param date
     *            a {@code Date} to compare against.
     * @return an {@code int < 0} if this {@code Date} is less than the specified {@code Date}, {@code 0} if
     *         they are equal, and an {@code int > 0} if this {@code Date} is greater.
     */
    public int compareTo(Date date) {
        if (milliseconds < date.milliseconds) {
            return -1;
        }
        if (milliseconds == date.milliseconds) {
            return 0;
        }
        return 1;
    }

    /**
     * Compares the specified object to this {@code Date} and returns if they are equal.
     * To be equal, the object must be an instance of {@code Date} and have the same millisecond
     * value.
     *
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this {@code Date}, {@code false}
     *         otherwise.
     *
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        return (object == this) || (object instanceof Date)
                && (milliseconds == ((Date) object).milliseconds);
    }

    /**
     * Returns the gregorian calendar day of the month for this {@code Date} object.
     *
     * @return the day of the month.
     *
     * @deprecated use {@code Calendar.get(Calendar.DATE)}
     */
    @Deprecated
    public int getDate() {
        return new GregorianCalendar(milliseconds).get(Calendar.DATE);
    }

    /**
     * Returns the gregorian calendar day of the week for this {@code Date} object.
     *
     * @return the day of the week.
     *
     * @deprecated use {@code Calendar.get(Calendar.DAY_OF_WEEK)}
     */
    @Deprecated
    public int getDay() {
        return new GregorianCalendar(milliseconds).get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * Returns the gregorian calendar hour of the day for this {@code Date} object.
     *
     * @return the hour of the day.
     *
     * @deprecated use {@code Calendar.get(Calendar.HOUR_OF_DAY)}
     */
    @Deprecated
    public int getHours() {
        return new GregorianCalendar(milliseconds).get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Returns the gregorian calendar minute of the hour for this {@code Date} object.
     *
     * @return the minutes.
     *
     * @deprecated use {@code Calendar.get(Calendar.MINUTE)}
     */
    @Deprecated
    public int getMinutes() {
        return new GregorianCalendar(milliseconds).get(Calendar.MINUTE);
    }

    /**
     * Returns the gregorian calendar month for this {@code Date} object.
     *
     * @return the month.
     *
     * @deprecated use {@code Calendar.get(Calendar.MONTH)}
     */
    @Deprecated
    public int getMonth() {
        return new GregorianCalendar(milliseconds).get(Calendar.MONTH);
    }

    /**
     * Returns the gregorian calendar second of the minute for this {@code Date} object.
     *
     * @return the seconds.
     *
     * @deprecated use {@code Calendar.get(Calendar.SECOND)}
     */
    @Deprecated
    public int getSeconds() {
        return new GregorianCalendar(milliseconds).get(Calendar.SECOND);
    }

    /**
     * Returns this {@code Date} as a millisecond value. The value is the number of
     * milliseconds since Jan. 1, 1970, midnight GMT.
     *
     * @return the number of milliseconds since Jan. 1, 1970, midnight GMT.
     */
    public long getTime() {
        return milliseconds;
    }

    /**
     * Returns the timezone offset in minutes of the default {@code TimeZone}.
     *
     * @return the timezone offset in minutes of the default {@code TimeZone}.
     *
     * @deprecated use
     *             {@code (Calendar.get(Calendar.ZONE_OFFSET) + Calendar.get(Calendar.DST_OFFSET)) / 60000}
     */
    @Deprecated
    public int getTimezoneOffset() {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        return -(cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / 60000;
    }

    /**
     * Returns the gregorian calendar year since 1900 for this {@code Date} object.
     *
     * @return the year - 1900.
     *
     * @deprecated use {@code Calendar.get(Calendar.YEAR) - 1900}
     */
    @Deprecated
    public int getYear() {
        return new GregorianCalendar(milliseconds).get(Calendar.YEAR) - 1900;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * return the same value for this method.
     *
     * @return this {@code Date}'s hash.
     *
     * @see #equals
     */
    @Override
    public int hashCode() {
        return (int) (milliseconds >>> 32) ^ (int) milliseconds;
    }

    private static int parse(String string, String[] array) {
        for (int i = 0, alength = array.length, slength = string.length(); i < alength; i++) {
            if (string.regionMatches(true, 0, array[i], 0, slength)) {
                return i;
            }
        }
        return -1;
    }

    private static IllegalArgumentException parseError(String string) {
        throw new IllegalArgumentException("Parse error: " + string);
    }

    /**
     * Returns the millisecond value of the date and time parsed from the
     * specified {@code String}. Many date/time formats are recognized, including IETF
     * standard syntax, i.e. Tue, 22 Jun 1999 12:16:00 GMT-0500
     *
     * @param string
     *            the String to parse.
     * @return the millisecond value parsed from the String.
     *
     * @deprecated use {@link DateFormat}
     */
    @Deprecated
    public static long parse(String string) {
        if (string == null) {
            throw new IllegalArgumentException("The string argument is null");
        }

        char sign = 0;
        int commentLevel = 0;
        int offset = 0, length = string.length(), state = 0;
        int year = -1, month = -1, date = -1;
        int hour = -1, minute = -1, second = -1, zoneOffset = 0, minutesOffset = 0;
        boolean zone = false;
        final int PAD = 0, LETTERS = 1, NUMBERS = 2;
        StringBuilder buffer = new StringBuilder();

        while (offset <= length) {
            char next = offset < length ? string.charAt(offset) : '\r';
            offset++;

            if (next == '(') {
                commentLevel++;
            }
            if (commentLevel > 0) {
                if (next == ')') {
                    commentLevel--;
                }
                if (commentLevel == 0) {
                    next = ' ';
                } else {
                    continue;
                }
            }

            int nextState = PAD;
            if ('a' <= next && next <= 'z' || 'A' <= next && next <= 'Z') {
                nextState = LETTERS;
            } else if ('0' <= next && next <= '9') {
                nextState = NUMBERS;
            } else if (!Character.isSpace(next) && ",+-:/".indexOf(next) == -1) {
                throw parseError(string);
            }

            if (state == NUMBERS && nextState != NUMBERS) {
                int digit = Integer.parseInt(buffer.toString());
                buffer.setLength(0);
                if (sign == '+' || sign == '-') {
                    if (zoneOffset == 0) {
                        zone = true;
                        if (next == ':') {
                            minutesOffset = sign == '-' ? -Integer
                                    .parseInt(string.substring(offset,
                                            offset + 2)) : Integer
                                    .parseInt(string.substring(offset,
                                            offset + 2));
                            offset += 2;
                        }
                        zoneOffset = sign == '-' ? -digit : digit;
                        sign = 0;
                    } else {
                        throw parseError(string);
                    }
                } else if (digit >= 70) {
                    if (year == -1
                            && (Character.isSpace(next) || next == ','
                                    || next == '/' || next == '\r')) {
                        year = digit;
                    } else {
                        throw parseError(string);
                    }
                } else if (next == ':') {
                    if (hour == -1) {
                        hour = digit;
                    } else if (minute == -1) {
                        minute = digit;
                    } else {
                        throw parseError(string);
                    }
                } else if (next == '/') {
                    if (month == -1) {
                        month = digit - 1;
                    } else if (date == -1) {
                        date = digit;
                    } else {
                        throw parseError(string);
                    }
                } else if (Character.isSpace(next) || next == ','
                        || next == '-' || next == '\r') {
                    if (hour != -1 && minute == -1) {
                        minute = digit;
                    } else if (minute != -1 && second == -1) {
                        second = digit;
                    } else if (date == -1) {
                        date = digit;
                    } else if (year == -1) {
                        year = digit;
                    } else {
                        throw parseError(string);
                    }
                } else if (year == -1 && month != -1 && date != -1) {
                    year = digit;
                } else {
                    throw parseError(string);
                }
            } else if (state == LETTERS && nextState != LETTERS) {
                String text = buffer.toString().toUpperCase(Locale.US);
                buffer.setLength(0);
                if (text.length() == 1) {
                    throw parseError(string);
                }
                if (text.equals("AM")) {
                    if (hour == 12) {
                        hour = 0;
                    } else if (hour < 1 || hour > 12) {
                        throw parseError(string);
                    }
                } else if (text.equals("PM")) {
                    if (hour == 12) {
                        hour = 0;
                    } else if (hour < 1 || hour > 12) {
                        throw parseError(string);
                    }
                    hour += 12;
                } else {
                    DateFormatSymbols symbols = new DateFormatSymbols(Locale.US);
                    String[] weekdays = symbols.getWeekdays(), months = symbols
                            .getMonths();
                    int value;
                    if (parse(text, weekdays) != -1) {/* empty */
                    } else if (month == -1 && (month = parse(text, months)) != -1) {/* empty */
                    } else if (text.equals("GMT") || text.equals("UT") || text.equals("UTC")) {
                        zone = true;
                        zoneOffset = 0;
                    } else if ((value = zone(text)) != 0) {
                        zone = true;
                        zoneOffset = value;
                    } else {
                        throw parseError(string);
                    }
                }
            }

            if (next == '+' || (year != -1 && next == '-')) {
                sign = next;
            } else if (!Character.isSpace(next) && next != ','
                    && nextState != NUMBERS) {
                sign = 0;
            }

            if (nextState == LETTERS || nextState == NUMBERS) {
                buffer.append(next);
            }
            state = nextState;
        }

        if (year != -1 && month != -1 && date != -1) {
            if (hour == -1) {
                hour = 0;
            }
            if (minute == -1) {
                minute = 0;
            }
            if (second == -1) {
                second = 0;
            }
            if (year < (CREATION_YEAR - 80)) {
                year += 2000;
            } else if (year < 100) {
                year += 1900;
            }
            minute -= minutesOffset;
            if (zone) {
                if (zoneOffset >= 24 || zoneOffset <= -24) {
                    hour -= zoneOffset / 100;
                    minute -= zoneOffset % 100;
                } else {
                    hour -= zoneOffset;
                }
                return UTC(year - 1900, month, date, hour, minute, second);
            }
            return new Date(year - 1900, month, date, hour, minute, second)
                    .getTime();
        }
        throw parseError(string);
    }

    /**
     * Sets the gregorian calendar day of the month for this {@code Date} object.
     *
     * @param day
     *            the day of the month.
     *
     * @deprecated use {@code Calendar.set(Calendar.DATE, day)}
     */
    @Deprecated
    public void setDate(int day) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.DATE, day);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Sets the gregorian calendar hour of the day for this {@code Date} object.
     *
     * @param hour
     *            the hour of the day.
     *
     * @deprecated use {@code Calendar.set(Calendar.HOUR_OF_DAY, hour)}
     */
    @Deprecated
    public void setHours(int hour) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Sets the gregorian calendar minute of the hour for this {@code Date} object.
     *
     * @param minute
     *            the minutes.
     *
     * @deprecated use {@code Calendar.set(Calendar.MINUTE, minute)}
     */
    @Deprecated
    public void setMinutes(int minute) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.MINUTE, minute);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Sets the gregorian calendar month for this {@code Date} object.
     *
     * @param month
     *            the month.
     *
     * @deprecated use {@code Calendar.set(Calendar.MONTH, month)}
     */
    @Deprecated
    public void setMonth(int month) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.MONTH, month);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Sets the gregorian calendar second of the minute for this {@code Date} object.
     *
     * @param second
     *            the seconds.
     *
     * @deprecated use {@code Calendar.set(Calendar.SECOND, second)}
     */
    @Deprecated
    public void setSeconds(int second) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.SECOND, second);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Sets this {@code Date} to the specified millisecond value. The value is the
     * number of milliseconds since Jan. 1, 1970 GMT.
     *
     * @param milliseconds
     *            the number of milliseconds since Jan. 1, 1970 GMT.
     */
    public void setTime(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    /**
     * Sets the gregorian calendar year since 1900 for this {@code Date} object.
     *
     * @param year
     *            the year since 1900.
     *
     * @deprecated use {@code Calendar.set(Calendar.YEAR, year + 1900)}
     */
    @Deprecated
    public void setYear(int year) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.YEAR, year + 1900);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Returns the string representation of this {@code Date} in GMT in the format
     * {@code "22 Jun 1999 13:02:00 GMT"}.
     *
     * @deprecated use {@link DateFormat}
     */
    @Deprecated
    public String toGMTString() {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM y HH:mm:ss 'GMT'", Locale.US);
        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(gmtZone);
        GregorianCalendar gc = new GregorianCalendar(gmtZone);
        gc.setTimeInMillis(milliseconds);
        return sdf.format(this);
    }

    /**
     * Returns the string representation of this {@code Date} for the default {@code Locale}.
     *
     * @deprecated use {@link DateFormat}
     */
    @Deprecated
    public String toLocaleString() {
        return DateFormat.getDateTimeInstance().format(this);
    }

    /**
     * Returns a string representation of this {@code Date}.
     * The formatting is equivalent to using a {@code SimpleDateFormat} with
     * the format string "EEE MMM dd HH:mm:ss zzz yyyy", which looks something
     * like "Tue Jun 22 13:07:00 PDT 1999". The current default time zone and
     * locale are used. If you need control over the time zone or locale,
     * use {@code SimpleDateFormat} instead.
     */
    @Override
    public String toString() {
        // TODO: equivalent to the following one-liner, though that's slower on stingray
        // at 476us versus 69us...
        //   return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").format(d);
        LocaleData localeData = LocaleData.get(Locale.US);
        Calendar cal = new GregorianCalendar(milliseconds);
        TimeZone tz = cal.getTimeZone();
        StringBuilder result = new StringBuilder();
        result.append(localeData.shortWeekdayNames[cal.get(Calendar.DAY_OF_WEEK)]);
        result.append(' ');
        result.append(localeData.shortMonthNames[cal.get(Calendar.MONTH)]);
        result.append(' ');
        appendTwoDigits(result, cal.get(Calendar.DAY_OF_MONTH));
        result.append(' ');
        appendTwoDigits(result, cal.get(Calendar.HOUR_OF_DAY));
        result.append(':');
        appendTwoDigits(result, cal.get(Calendar.MINUTE));
        result.append(':');
        appendTwoDigits(result, cal.get(Calendar.SECOND));
        result.append(' ');
        result.append(tz.getDisplayName(tz.inDaylightTime(this), TimeZone.SHORT));
        result.append(' ');
        result.append(cal.get(Calendar.YEAR));
        return result.toString();
    }

    private static void appendTwoDigits(StringBuilder sb, int n) {
        if (n < 10) {
            sb.append('0');
        }
        sb.append(n);
    }

    /**
     * Returns the millisecond value of the specified date and time in GMT.
     *
     * @param year
     *            the year, 0 is 1900.
     * @param month
     *            the month, 0 - 11.
     * @param day
     *            the day of the month, 1 - 31.
     * @param hour
     *            the hour of day, 0 - 23.
     * @param minute
     *            the minute of the hour, 0 - 59.
     * @param second
     *            the second of the minute, 0 - 59.
     * @return the date and time in GMT in milliseconds.
     *
     * @deprecated use: <code>
     *  Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
     *  cal.set(year + 1900, month, day, hour, minute, second);
     *  cal.getTime().getTime();</code>
     */
    @Deprecated
    public static long UTC(int year, int month, int day, int hour, int minute,
            int second) {
        GregorianCalendar cal = new GregorianCalendar(false);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(1900 + year, month, day, hour, minute, second);
        return cal.getTimeInMillis();
    }

    private static int zone(String text) {
        if (text.equals("EST")) {
            return -5;
        }
        if (text.equals("EDT")) {
            return -4;
        }
        if (text.equals("CST")) {
            return -6;
        }
        if (text.equals("CDT")) {
            return -5;
        }
        if (text.equals("MST")) {
            return -7;
        }
        if (text.equals("MDT")) {
            return -6;
        }
        if (text.equals("PST")) {
            return -8;
        }
        if (text.equals("PDT")) {
            return -7;
        }
        return 0;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeLong(getTime());
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        setTime(stream.readLong());
    }
}
