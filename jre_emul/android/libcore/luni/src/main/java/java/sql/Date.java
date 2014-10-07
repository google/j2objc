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

/**
 * A class which can consume and produce dates in SQL {@code Date} format.
 * <p>
 * Dates are represented in SQL as {@code yyyy-MM-dd}. Note that this date
 * format only deals with year, month and day values. There are no values for
 * hours, minutes, seconds.
 * <p>
 * This is unlike the familiar {@code java.util.Date} object, which also includes
 * values for hours, minutes, seconds, and milliseconds.
 * <p>
 * Time points are handled as millisecond values - milliseconds since the Epoch,
 * January 1st 1970, 00:00:00.000 GMT. Time values passed to the {@code
 * java.sql.Date} class are "normalized" to the time 00:00:00.000 GMT on the
 * date implied by the time value.
 */
//@FindBugsSuppressWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class Date extends java.util.Date {

    private static final long serialVersionUID = 1511598038487230103L;

    /**
     * Constructs a {@code Date} object corresponding to the supplied year,
     * month and day.
     *
     * @deprecated Use the constructor {@link #Date(long)} instead.
     * @param theYear
     *            the year, specified as the year minus 1900. Must be in the
     *            range {@code [0,8099]}.
     * @param theMonth
     *            the month, specified as a number with 0 = January. Must be in
     *            the range {@code [0,11]}.
     * @param theDay
     *            the day in the month. Must be in the range {@code [1,31]}.
     */
    @Deprecated
    public Date(int theYear, int theMonth, int theDay) {
        super(theYear, theMonth, theDay);
    }

    /**
     * Creates a date which corresponds to the day determined by the supplied
     * milliseconds time value {@code theDate}.
     *
     * @param theDate
     *            a time value in milliseconds since the epoch - January 1 1970
     *            00:00:00 GMT. The time value (hours, minutes, seconds,
     *            milliseconds) stored in the {@code Date} object is adjusted to
     *            correspond to 00:00:00 GMT on the day determined by the supplied
     *            time value.
     */
    public Date(long theDate) {
        super(normalizeTime(theDate));
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL {@code
     *             Date} values do not have an hours component.
     * @return does not return anything.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @Deprecated
    @Override
    public int getHours() {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL {@code
     *             Date} values do not have a minutes component.
     * @return does not return anything.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @Deprecated
    @Override
    public int getMinutes() {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL {@code
     *             Date} values do not have a seconds component.
     * @return does not return anything.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @Deprecated
    @Override
    public int getSeconds() {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL {@code
     *             Date} values do not have an hours component.
     * @param theHours
     *            the number of hours to set.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @Deprecated
    @Override
    public void setHours(int theHours) {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL {@code
     *             Date} values do not have a minutes component.
     * @param theMinutes
     *            the number of minutes to set.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @Deprecated
    @Override
    public void setMinutes(int theMinutes) {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL {@code
     *             Date} values do not have a seconds component.
     * @param theSeconds
     *            the number of seconds to set.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @Deprecated
    @Override
    public void setSeconds(int theSeconds) {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * Sets this date to a date supplied as a milliseconds value. The date is
     * set based on the supplied time value and rounded to zero GMT for that day.
     *
     * @param theTime
     *            the time in milliseconds since the Epoch.
     */
    @Override
    public void setTime(long theTime) {
        /*
         * Store the Date based on the supplied time after removing any time
         * elements finer than the day based on zero GMT
         */
        super.setTime(normalizeTime(theTime));
    }

    /**
     * Produces a string representation of the date in SQL format
     *
     * @return a string representation of the date in SQL format - {@code "yyyy-MM-dd"}.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(10);

        format((getYear() + 1900), 4, sb);
        sb.append('-');
        format((getMonth() + 1), 2, sb);
        sb.append('-');
        format(getDate(), 2, sb);

        return sb.toString();
    }

    private static final String PADDING = "0000";

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
     * Creates a {@code Date} from a string representation of a date in SQL
     * format.
     *
     * @param dateString
     *            the string representation of a date in SQL format - " {@code yyyy-MM-dd}".
     * @return the {@code Date} object.
     * @throws IllegalArgumentException
     *             if the format of the supplied string does not match the SQL
     *             format.
     */
    public static Date valueOf(String dateString) {
        if (dateString == null) {
            throw new IllegalArgumentException("dateString == null");
        }
        int firstIndex = dateString.indexOf('-');
        int secondIndex = dateString.indexOf('-', firstIndex + 1);
        // secondIndex == -1 means none or only one separator '-' has been
        // found.
        // The string is separated into three parts by two separator characters,
        // if the first or the third part is null string, we should throw
        // IllegalArgumentException to follow RI
        if (secondIndex == -1 || firstIndex == 0
                || secondIndex + 1 == dateString.length()) {
            throw new IllegalArgumentException();
        }
        // parse each part of the string
        int year = Integer.parseInt(dateString.substring(0, firstIndex));
        int month = Integer.parseInt(dateString.substring(firstIndex + 1,
                secondIndex));
        int day = Integer.parseInt(dateString.substring(secondIndex + 1,
                dateString.length()));
        return new Date(year - 1900, month - 1, day);
    }

    /*
     * Private method which normalizes a Time value, removing all low
     * significance digits corresponding to milliseconds, seconds, minutes and
     * hours, so that the returned Time value corresponds to 00:00:00 GMT on a
     * particular day.
     */
    private static long normalizeTime(long theTime) {
        return theTime;
    }
}
