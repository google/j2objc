/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.sql;

//import java.text.ParsePosition;
//import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A Java representation of the SQL {@code TIMESTAMP} type. It provides the
 * capability of representing the SQL {@code TIMESTAMP} nanosecond value, in
 * addition to the regular date/time value which has millisecond resolution.
 * <p>
 * The {@code Timestamp} class consists of a regular date/time value, where only
 * the integral seconds value is stored, plus a nanoseconds value where the
 * fractional seconds are stored.
 * <p>
 * The addition of the nanosecond value field to the {@code Timestamp} object
 * makes it significantly different from the {@code java.util.Date} object which
 * it extends. Users should be aware that {@code Timestamp} objects are not
 * interchangable with {@code java.util.Date} objects when used outside the
 * confines of the {@code java.sql} package.
 *
 * @see Date
 * @see Time
 * @see java.util.Date
 */
public class Timestamp extends Date {

    private static final long serialVersionUID = 2745179027874758501L;

    // The nanoseconds time value of the Timestamp
    private int nanos;

    // The regex pattern of yyyy-MM-dd HH:mm:ss
    private static final String TIME_FORMAT_REGEX = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.*";

    /**
     * Returns a {@code Timestamp} corresponding to the time specified by the
     * supplied values for <i>Year</i>, <i>Month</i>, <i>Date</i>, <i>Hour</i>,
     * <i>Minutes</i>, <i>Seconds</i> and <i>Nanoseconds</i>.
     *
     * @deprecated Use the constructor {@link #Timestamp(long)} instead.
     * @param theYear
     *            specified as the year minus 1900.
     * @param theMonth
     *            specified as an integer in the range [0,11].
     * @param theDate
     *            specified as an integer in the range [1,31].
     * @param theHour
     *            specified as an integer in the range [0,23].
     * @param theMinute
     *            specified as an integer in the range [0,59].
     * @param theSecond
     *            specified as an integer in the range [0,59].
     * @param theNano
     *            which defines the nanosecond value of the timestamp specified
     *            as an integer in the range [0,999'999'999]
     * @throws IllegalArgumentException
     *             if any of the parameters is out of range.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public Timestamp(int theYear, int theMonth, int theDate, int theHour,
            int theMinute, int theSecond, int theNano)
            throws IllegalArgumentException {
        super(theYear, theMonth, theDate, theHour, theMinute, theSecond);
        if (theNano < 0 || theNano > 999999999) {
            throw new IllegalArgumentException("ns out of range: " + theNano);
        }
        nanos = theNano;
    }

    /**
     * Returns a {@code Timestamp} object corresponding to the time represented
     * by a supplied time value.
     *
     * @param theTime
     *            a time value in the format of milliseconds since the Epoch
     *            (January 1 1970 00:00:00.000 GMT).
     */
    public Timestamp(long theTime) {
        super(theTime);
        /*
         * Now set the time for this Timestamp object - which deals with the
         * nanosecond value as well as the base time
         */
        setTimeImpl(theTime);
    }

    /**
     * Returns {@code true} if this timestamp object is later than the supplied
     * timestamp, otherwise returns {@code false}.
     *
     * @param theTimestamp
     *            the timestamp to compare with this timestamp object.
     * @return {@code true} if this {@code Timestamp} object is later than the
     *         supplied timestamp, {@code false} otherwise.
     */
    public boolean after(Timestamp theTimestamp) {
        long thisTime = this.getTime();
        long compareTime = theTimestamp.getTime();

        // If the time value is later, the timestamp is later
        if (thisTime > compareTime) {
            return true;
        }
        // If the time value is earlier, the timestamp is not later
        else if (thisTime < compareTime) {
            return false;
        }
        /*
         * Otherwise the time values are equal in which case the nanoseconds
         * value determines whether this timestamp is later...
         */
        else if (this.getNanos() > theTimestamp.getNanos()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns {@code true} if this {@code Timestamp} object is earlier than the
     * supplied timestamp, otherwise returns {@code false}.
     *
     * @param theTimestamp
     *            the timestamp to compare with this {@code Timestamp} object.
     * @return {@code true} if this {@code Timestamp} object is earlier than the
     *         supplied timestamp, {@code false} otherwise.
     */
    public boolean before(Timestamp theTimestamp) {
        long thisTime = this.getTime();
        long compareTime = theTimestamp.getTime();

        // If the time value is later, the timestamp is later
        if (thisTime < compareTime) {
            return true;
        }
        // If the time value is earlier, the timestamp is not later
        else if (thisTime > compareTime) {
            return false;
        }
        /*
         * Otherwise the time values are equal in which case the nanoseconds
         * value determines whether this timestamp is later...
         */
        else if (this.getNanos() < theTimestamp.getNanos()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Compares this {@code Timestamp} object with a supplied {@code Timestamp}
     * object.
     *
     * @param theObject
     *            the timestamp to compare with this {@code Timestamp} object,
     *            passed as an {@code Object}.
     * @return <dd>
     *         <dl>
     *         {@code 0} if the two {@code Timestamp} objects are equal in time
     *         </dl>
     *         <dl>
     *         a value {@code < 0} if this {@code Timestamp} object is before
     *         the supplied {@code Timestamp} and a value
     *         </dl>
     *         <dl>
     *         {@code > 0} if this {@code Timestamp} object is after the
     *         supplied {@code Timestamp}
     *         </dl>
     *         </dd>
     * @throws ClassCastException
     *             if the supplied object is not a {@code Timestamp} object.
     */
    @Override
    public int compareTo(Date theObject) throws ClassCastException {
        return this.compareTo((Timestamp) theObject);
    }

    /**
     * Compares this {@code Timestamp} object with a supplied {@code Timestamp}
     * object.
     *
     * @param theTimestamp
     *            the timestamp to compare with this {@code Timestamp} object,
     *            passed in as a {@code Timestamp}.
     * @return one of the following:
     *         <ul>
     *         <li>{@code 0}, if the two {@code Timestamp} objects are
     *         equal in time</li>
     *         <li>{@code < 0}, if this {@code Timestamp} object is before the
     *         supplied {@code Timestamp}</li>
     *         <li> {@code > 0}, if this {@code Timestamp} object is after the
     *         supplied {@code Timestamp}</li>
     *         </ul>
     */
    public int compareTo(Timestamp theTimestamp) {
        int result = super.compareTo(theTimestamp);
        if (result == 0) {
            int thisNano = this.getNanos();
            int thatNano = theTimestamp.getNanos();
            if (thisNano > thatNano) {
                return 1;
            } else if (thisNano == thatNano) {
                return 0;
            } else {
                return -1;
            }
        }
        return result;
    }

    /**
     * Tests to see if this timestamp is equal to a supplied object.
     *
     * @param theObject
     *            the object to which this timestamp is compared.
     * @return {@code true} if this {@code Timestamp} object is equal to the
     *         supplied {@code Timestamp} object<br>{@code false} if the object
     *         is not a {@code Timestamp} object or if the object is a {@code
     *         Timestamp} but represents a different instant in time.
     */
    @Override
    public boolean equals(Object theObject) {
        if (theObject instanceof Timestamp) {
            return equals((Timestamp) theObject);
        }
        return false;
    }

    /**
     * Tests to see if this timestamp is equal to a supplied timestamp.
     *
     * @param theTimestamp
     *            the timestamp to compare with this {@code Timestamp} object,
     *            passed as an {@code Object}.
     * @return {@code true} if this {@code Timestamp} object is equal to the
     *         supplied {@code Timestamp} object, {@code false} otherwise.
     */
    public boolean equals(Timestamp theTimestamp) {
        if (theTimestamp == null) {
            return false;
        }
        return (this.getTime() == theTimestamp.getTime())
                && (this.getNanos() == theTimestamp.getNanos());
    }

    /**
     * Gets this {@code Timestamp}'s nanosecond value
     *
     * @return The timestamp's nanosecond value, an integer between 0 and
     *         999,999,999.
     */
    public int getNanos() {
        return nanos;
    }

    /**
     * Returns the time represented by this {@code Timestamp} object, as a long
     * value containing the number of milliseconds since the Epoch (January 1
     * 1970, 00:00:00.000 GMT).
     *
     * @return the number of milliseconds that have passed since January 1 1970,
     *         00:00:00.000 GMT.
     */
    @Override
    public long getTime() {
        long theTime = super.getTime();
        theTime = theTime + (nanos / 1000000);
        return theTime;
    }

    /**
     * Sets the nanosecond value for this {@code Timestamp}.
     *
     * @param n
     *            number of nanoseconds.
     * @throws IllegalArgumentException
     *             if number of nanoseconds smaller than 0 or greater than
     *             999,999,999.
     */
    public void setNanos(int n) throws IllegalArgumentException {
        if ((n < 0) || (n > 999999999)) {
            throw new IllegalArgumentException("Value out of range");
        }
        nanos = n;
    }

    /**
     * Sets the time represented by this {@code Timestamp} object to the
     * supplied time, defined as the number of milliseconds since the Epoch
     * (January 1 1970, 00:00:00.000 GMT).
     *
     * @param theTime
     *            number of milliseconds since the Epoch (January 1 1970,
     *            00:00:00.000 GMT).
     */
    @Override
    public void setTime(long theTime) {
        setTimeImpl(theTime);
    }

    private void setTimeImpl(long theTime) {
        /*
         * Deal with the nanoseconds value. The supplied time is in milliseconds -
         * so we must extract the milliseconds value and multiply by 1000000 to
         * get nanoseconds. Things are more complex if theTime value is
         * negative, since then the time value is the time before the Epoch but
         * the nanoseconds value of the Timestamp must be positive - so we must
         * take the "raw" milliseconds value and subtract it from 1000 to get to
         * the true nanoseconds value Simultaneously, recalculate the time value
         * to the exact nearest second and reset the Date time value
         */
        int milliseconds = (int) (theTime % 1000);
        theTime = theTime - milliseconds;
        if (milliseconds < 0) {
            theTime = theTime - 1000;
            milliseconds = 1000 + milliseconds;
        }
        super.setTime(theTime);
        setNanos(milliseconds * 1000000);
    }

    /**
     * Returns the timestamp formatted as a String in the JDBC Timestamp Escape
     * format, which is {@code "yyyy-MM-dd HH:mm:ss.nnnnnnnnn"}.
     *
     * @return A string representing the instant defined by the {@code
     *         Timestamp}, in JDBC Timestamp escape format.
     */
    @SuppressWarnings("deprecation")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(29);

        format((getYear() + 1900), 4, sb);
        sb.append('-');
        format((getMonth() + 1), 2, sb);
        sb.append('-');
        format(getDate(), 2, sb);
        sb.append(' ');
        format(getHours(), 2, sb);
        sb.append(':');
        format(getMinutes(), 2, sb);
        sb.append(':');
        format(getSeconds(), 2, sb);
        sb.append('.');
        if (nanos == 0) {
            sb.append('0');
        } else {
            format(nanos, 9, sb);
            while (sb.charAt(sb.length() - 1) == '0') {
                sb.setLength(sb.length() - 1);
            }
        }

        return sb.toString();
    }

    private static final String PADDING = "000000000";

    /*
    * Private method to format the time
    */
    private void format(int date, int digits, StringBuilder sb) {
        String str = String.valueOf(date);
        if (digits - str.length() > 0) {
            sb.append(PADDING.substring(0, digits - str.length()));
        }
        sb.append(str);
    }

    /**
     * Creates a {@code Timestamp} object with a time value equal to the time
     * specified by a supplied String holding the time in JDBC timestamp escape
     * format, which is {@code "yyyy-MM-dd HH:mm:ss.nnnnnnnnn}"
     *
     * @param s
     *            the {@code String} containing a time in JDBC timestamp escape
     *            format.
     * @return A {@code Timestamp} object with time value as defined by the
     *         supplied {@code String}.
     * @throws IllegalArgumentException
     *             if the provided string is {@code null}.
     */
    public static Timestamp valueOf(String s) throws IllegalArgumentException {
        if (s == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        // omit trailing whitespace
        s = s.trim();
        if (!Pattern.matches(TIME_FORMAT_REGEX, s)) {
            throw badTimestampString(s);
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        ParsePosition pp = new ParsePosition(0);

        /*
         * First parse out the yyyy-MM-dd HH:mm:ss component of the String into
         * a Date object using the SimpleDateFormat. This should stop after the
         * seconds value, according to the definition of SimpleDateFormat.parse,
         * with the ParsePosition indicating the index of the "." which should
         * precede the nanoseconds value
         */
        Date theDate;
        try {
            theDate = df.parse(s, pp);
        } catch (Exception e) {
            throw badTimestampString(s);
        }

        if (theDate == null) {
            throw badTimestampString(s);
        }

        /*
         * If we get here, the Date part of the string was OK - now for the
         * nanoseconds value. Strictly, this requires the remaining part of the
         * String to look like ".nnnnnnnnn". However, we accept anything with a
         * '.' followed by 1 to 9 digits - we also accept nothing (no fractions
         * of a second). Anything else is interpreted as incorrect format which
         * will generate an IllegalArgumentException
         */
        int position = pp.getIndex();
        int remaining = s.length() - position;
        int theNanos;

        if (remaining == 0) {
            // First, allow for the case where no fraction of a second is given:
            theNanos = 0;
        } else {
            /*
             * Case where fraction of a second is specified: Require 1 character
             * plus the "." in the remaining part of the string...
             */
            if ((s.length() - position) < ".n".length()) {
                throw badTimestampString(s);
            }

            /*
             * If we're strict, we should not allow any EXTRA characters after
             * the 9 digits
             */
            if ((s.length() - position) > ".nnnnnnnnn".length()) {
                throw badTimestampString(s);
            }

            // Require the next character to be a "."
            if (s.charAt(position) != '.') {
                throw new NumberFormatException("Bad input string format: expected '.' not '" +
                        s.charAt(position) + "' in \"" + s + "\"");
            }
            // Get the length of the number string - need to account for the '.'
            int nanoLength = s.length() - position - 1;

            // Get the 9 characters following the "." as an integer
            String theNanoString = s.substring(position + 1, position + 1
                    + nanoLength);
            /*
             * We must adjust for the cases where the nanos String was not 9
             * characters long by padding out with zeros
             */
            theNanoString = theNanoString + "000000000";
            theNanoString = theNanoString.substring(0, 9);

            try {
                theNanos = Integer.parseInt(theNanoString);
            } catch (Exception e) {
                // If we get here, the string was not a number
                throw badTimestampString(s);
            }
        }

        if (theNanos < 0 || theNanos > 999999999) {
            throw badTimestampString(s);
        }

        Timestamp theTimestamp = new Timestamp(theDate.getTime());
        theTimestamp.setNanos(theNanos);

        return theTimestamp;
    }

    private static IllegalArgumentException badTimestampString(String s) {
        throw new IllegalArgumentException("Timestamp format must be " +
                "yyyy-MM-dd HH:mm:ss.fffffffff; was '" + s + "'");
    }
}
