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

package test.j2objc;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateTest extends junit.framework.TestCase {

    /**
     * java.util.Date#toString()
     */
    public void test_toString() {
        // Test for method java.lang.String java.util.Date.toString()
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date d = cal.getTime();
        String result = d.toString();
        assertTrue("Incorrect result: " + d, result
                .startsWith("Thu Jan 01 00:00:00")
                && result.endsWith("1970"));

        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));
        try {
            Date d1 = new Date(0);
            assertTrue("Returned incorrect string: " + d1, d1.toString()
                    .equals("Wed Dec 31 19:00:00 GMT-05:00 1969"));
        } finally {
            TimeZone.setDefault(tz);
        }

        // Test for HARMONY-5468
        TimeZone.setDefault(TimeZone.getTimeZone("MST"));
        Date d2 = new Date(108, 7, 27);
        assertTrue("Returned incorrect string: " + d2, d2.toString()
                .startsWith("Wed Aug 27 00:00:00")
                && d2.toString().endsWith("2008"));
    }


    static TimeZone defaultTimeZone = TimeZone.getDefault();

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        TimeZone.setDefault(defaultTimeZone);
    }
}
