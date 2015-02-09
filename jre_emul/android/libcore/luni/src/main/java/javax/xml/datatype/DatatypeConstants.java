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

// $Id: DatatypeConstants.java 446598 2006-09-15 12:55:40Z jeremias $

package javax.xml.datatype;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * <p>Utility class to contain basic Datatype values as constants.</p>
 *
 * @author <a href="mailto:Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 446598 $, $Date: 2006-09-15 05:55:40 -0700 (Fri, 15 Sep 2006) $
 * @since 1.5
 */

public final class DatatypeConstants {

    /**
     * <p>Private constructor to prevent instantiation.</p>
     */
    private DatatypeConstants() {
    }

    /**
     * Value for first month of year.
     */
    public static final int JANUARY  = 1;

    /**
     * Value for second month of year.
     */
    public static final int FEBRUARY = 2;

    /**
     * Value for third month of year.
     */
    public static final int MARCH    = 3;

    /**
     * Value for fourth month of year.
     */
    public static final int APRIL    = 4;

    /**
     * Value for fifth month of year.
     */
    public static final int MAY      = 5;

    /**
     * Value for sixth month of year.
     */
    public static final int JUNE     = 6;

    /**
     * Value for seventh month of year.
     */
    public static final int JULY     = 7;

    /**
     * Value for eighth month of year.
     */
    public static final int AUGUST   = 8;

    /**
     * Value for ninth month of year.
     */
    public static final int SEPTEMBER = 9;

    /**
     * Value for tenth month of year.
     */
    public static final int OCTOBER = 10;

    /**
     * Value for eleven month of year.
     */
    public static final int NOVEMBER = 11;

    /**
     * Value for twelve month of year.
     */
    public static final int DECEMBER = 12;

    /**
     * <p>Comparison result.</p>
     */
    public static final int LESSER = -1;

    /**
     * <p>Comparison result.</p>
     */
    public static final int EQUAL =  0;

    /**
     * <p>Comparison result.</p>
     */
    public static final int GREATER =  1;

    /**
     * <p>Comparison result.</p>
     */
    public static final int INDETERMINATE =  2;

    /**
     * Designation that an "int" field is not set.
     */
    public static final int FIELD_UNDEFINED = Integer.MIN_VALUE;

    /**
     * <p>A constant that represents the years field.</p>
     */
    public static final Field YEARS = new Field("YEARS", 0);

    /**
     * <p>A constant that represents the months field.</p>
     */
    public static final Field MONTHS = new Field("MONTHS", 1);

    /**
     * <p>A constant that represents the days field.</p>
     */
    public static final Field DAYS = new Field("DAYS", 2);

    /**
     * <p>A constant that represents the hours field.</p>
     */
    public static final Field HOURS = new Field("HOURS", 3);

    /**
     * <p>A constant that represents the minutes field.</p>
     */
    public static final Field MINUTES = new Field("MINUTES", 4);

    /**
     * <p>A constant that represents the seconds field.</p>
     */
    public static final Field SECONDS = new Field("SECONDS", 5);

    /**
     * Type-safe enum class that represents six fields
     * of the {@link Duration} class.
     */
    public static final class Field {

        /**
         * <p><code>String</code> representation of <ode>Field</code>.</p>
         */
        private final String str;
        /**
         * <p>Unique id of the field.</p>
         *
         * <p>This value allows the {@link Duration} class to use switch
         * statements to process fields.</p>
         */
        private final int id;

        /**
         * <p>Construct a <code>Field</code> with specified values.</p>
         * @param str <code>String</code> representation of <code>Field</code>
         * @param id  <code>int</code> representation of <code>Field</code>
         */
        private Field(final String str, final int id) {
            this.str = str;
            this.id = id;
        }
        /**
         * Returns a field name in English. This method
         * is intended to be used for debugging/diagnosis
         * and not for display to end-users.
         *
         * @return
         *      a non-null valid String constant.
         */
        public String toString() { return str; }

        /**
         * <p>Get id of this Field.</p>
         *
         * @return Id of field.
         */
        public int getId() {
            return id;
        }
    }

    /**
     * <p>Fully qualified name for W3C XML Schema 1.0 datatype <code>dateTime</code>.</p>
     */
    public static final QName DATETIME = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "dateTime");

    /**
     * <p>Fully qualified name for W3C XML Schema 1.0 datatype <code>time</code>.</p>
     */
    public static final QName TIME = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "time");

    /**
     * <p>Fully qualified name for W3C XML Schema 1.0 datatype <code>date</code>.</p>
     */
    public static final QName DATE = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "date");

    /**
     * <p>Fully qualified name for W3C XML Schema 1.0 datatype <code>gYearMonth</code>.</p>
     */
    public static final QName GYEARMONTH = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "gYearMonth");

    /**
     * <p>Fully qualified name for W3C XML Schema 1.0 datatype <code>gMonthDay</code>.</p>
     */
    public static final QName GMONTHDAY = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "gMonthDay");

    /**
     * <p>Fully qualified name for W3C XML Schema 1.0 datatype <code>gYear</code>.</p>
     */
    public static final QName GYEAR = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "gYear");

    /**
     * <p>Fully qualified name for W3C XML Schema 1.0 datatype <code>gMonth</code>.</p>
     */
    public static final QName GMONTH = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "gMonth");

    /**
     * <p>Fully qualified name for W3C XML Schema 1.0 datatype <code>gDay</code>.</p>
     */
    public static final QName GDAY = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "gDay");

    /**
     * <p>Fully qualified name for W3C XML Schema datatype <code>duration</code>.</p>
     */
    public static final QName DURATION = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "duration");

    /**
     * <p>Fully qualified name for XQuery 1.0 and XPath 2.0 datatype <code>dayTimeDuration</code>.</p>
     */
    public static final QName DURATION_DAYTIME = new QName(XMLConstants.W3C_XPATH_DATATYPE_NS_URI, "dayTimeDuration");

    /**
     * <p>Fully qualified name for XQuery 1.0 and XPath 2.0 datatype <code>yearMonthDuration</code>.</p>
     */
    public static final QName DURATION_YEARMONTH = new QName(XMLConstants.W3C_XPATH_DATATYPE_NS_URI, "yearMonthDuration");

    /**
     * W3C XML Schema max timezone offset is -14:00. Zone offset is in minutes.
     */
    public static final int MAX_TIMEZONE_OFFSET = -14 * 60;

    /**
     * W3C XML Schema min timezone offset is +14:00. Zone offset is in minutes.
     */
    public static final int MIN_TIMEZONE_OFFSET = 14 * 60;

}
