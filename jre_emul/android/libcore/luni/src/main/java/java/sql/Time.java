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

import java.util.Date;

/**
 * Java representation of an SQL {@code TIME} value. Provides utilities to
 * format and parse the time's representation as a String in JDBC escape format.
 */
public class Time extends Date {

    private static final long serialVersionUID = 8397324403548013681L;

    /**
     * Constructs a {@code Time} object using the supplied values for <i>Hour</i>,
     * <i>Minute</i> and <i>Second</i>. The <i>Year</i>, <i>Month</i> and
     * <i>Day</i> elements of the {@code Time} object are set to the date
     * of the Epoch (January 1, 1970).
     * <p>
     * Any attempt to access the <i>Year</i>, <i>Month</i> or <i>Day</i>
     * elements of a {@code Time} object will result in an {@code
     * IllegalArgumentException}.
     * <p>
     * The result is undefined if any argument is out of bounds.
     *
     * @deprecated Use the constructor {@link #Time(long)} instead.
     * @param theHour
     *            a value in the range {@code [0,23]}.
     * @param theMinute
     *            a value in the range {@code [0,59]}.
     * @param theSecond
     *            a value in the range {@code [0,59]}.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public Time(int theHour, int theMinute, int theSecond) {
        super(70, 0, 1, theHour, theMinute, theSecond);
    }

    /**
     * Constructs a {@code Time} object using a supplied time specified in
     * milliseconds.
     *
     * @param theTime
     *            a {@code Time} specified in milliseconds since the
     *            <i>Epoch</i> (January 1st 1970, 00:00:00.000).
     */
    public Time(long theTime) {
        super(theTime);
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL
     *             {@code Time} object does not have a date component.
     * @return does not return anything.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getDate() {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL
     *             {@code Time} object does not have a day component.
     * @return does not return anything.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getDay() {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL
     *             {@code Time} object does not have a month component.
     * @return does not return anything.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getMonth() {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL
     *             {@code Time} object does not have a year component.
     * @return does not return anything.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getYear() {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL
     *             {@code Time} object does not have a date component.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setDate(int i) {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL
     *             {@code Time} object does not have a month component.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setMonth(int i) {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL
     *             {@code Time} object does not have a year component.
     * @throws IllegalArgumentException
     *             if this method is called.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setYear(int i) {
        throw new IllegalArgumentException("unimplemented");
    }

    /**
     * Sets the time for this {@code Time} object to the supplied milliseconds
     * value.
     *
     * @param time
     *            A time value expressed as milliseconds since the <i>Epoch</i>.
     *            Negative values are milliseconds before the Epoch. The Epoch
     *            is January 1 1970, 00:00:00.000.
     */
    @Override
    public void setTime(long time) {
        super.setTime(time);
    }

    /**
     * Formats the {@code Time} as a String in JDBC escape format: {@code
     * hh:mm:ss}.
     *
     * @return A String representing the {@code Time} value in JDBC escape
     *         format: {@code HH:mm:ss}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(8);

        format(getHours(), 2, sb);
        sb.append(':');
        format(getMinutes(), 2, sb);
        sb.append(':');
        format(getSeconds(), 2, sb);

        return sb.toString();
    }

    private static final String PADDING = "00";

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
     * Creates a {@code Time} object from a string holding a time represented in
     * JDBC escape format: {@code hh:mm:ss}.
     * <p>
     * An exception occurs if the input string does not comply with this format.
     *
     * @param timeString
     *            A String representing the time value in JDBC escape format:
     *            {@code hh:mm:ss}.
     * @return The {@code Time} object set to a time corresponding to the given
     *         time.
     * @throws IllegalArgumentException
     *             if the supplied time string is not in JDBC escape format.
     */
    public static Time valueOf(String timeString) {
        if (timeString == null) {
            throw new IllegalArgumentException("timeString == null");
        }
        int firstIndex = timeString.indexOf(':');
        int secondIndex = timeString.indexOf(':', firstIndex + 1);
        // secondIndex == -1 means none or only one separator '-' has been
        // found.
        // The string is separated into three parts by two separator characters,
        // if the first or the third part is null string, we should throw
        // IllegalArgumentException to follow RI
        if (secondIndex == -1 || firstIndex == 0
                || secondIndex + 1 == timeString.length()) {
            throw new IllegalArgumentException();
        }
        // parse each part of the string
        int hour = Integer.parseInt(timeString.substring(0, firstIndex));
        int minute = Integer.parseInt(timeString.substring(firstIndex + 1,
                secondIndex));
        int second = Integer.parseInt(timeString.substring(secondIndex + 1,
                timeString.length()));
        return new Time(hour, minute, second);
    }
}
