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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class TimeZoneTest extends junit.framework.TestCase {


    /**
     * java.util.TimeZone#getTimeZone(java.lang.String)
     */
    public void test_getTimeZoneLjava_lang_String() {
        assertEquals("Must return GMT when given an invalid TimeZone id SMT-8.",
                "GMT", TimeZone.getTimeZone("SMT-8").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+28:70.",
                "GMT", TimeZone.getTimeZone("GMT+28:70").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+28:30.",
                "GMT", TimeZone.getTimeZone("GMT+28:30").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+8:70.",
                "GMT", TimeZone.getTimeZone("GMT+8:70").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+3:.",
                "GMT", TimeZone.getTimeZone("GMT+3:").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+3:0.",
                "GMT", TimeZone.getTimeZone("GMT+3:0").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+2360.",
                "GMT", TimeZone.getTimeZone("GMT+2360").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+892.",
                "GMT", TimeZone.getTimeZone("GMT+892").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+082.",
                "GMT", TimeZone.getTimeZone("GMT+082").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+28.",
                "GMT", TimeZone.getTimeZone("GMT+28").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+30.",
                "GMT", TimeZone.getTimeZone("GMT+30").getID());
        assertEquals("Must return GMT when given TimeZone GMT.",
                "GMT", TimeZone.getTimeZone("GMT").getID());
        assertEquals("Must return GMT when given TimeZone GMT+.",
                "GMT", TimeZone.getTimeZone("GMT+").getID());
        assertEquals("Must return GMT when given TimeZone GMT-.",
                "GMT", TimeZone.getTimeZone("GMT-").getID());
        /* j2objc: NSTimeZone can actually parse this format.
        assertEquals("Must return GMT when given an invalid TimeZone time GMT-8.45.",
                "GMT", TimeZone.getTimeZone("GMT-8.45").getID());*/
        assertEquals("Must return GMT when given an invalid TimeZone time GMT-123:23.",
                "GMT", TimeZone.getTimeZone("GMT-123:23").getID());
        /* j2objc: NSTimeZone can actually parse this format.
        assertEquals("Must return proper GMT formatted string for GMT+8:30 (eg. GMT+08:20).",
                "GMT+08:30", TimeZone.getTimeZone("GMT+8:30").getID());*/
        assertEquals("Must return proper GMT formatted string for GMT+3 (eg. GMT+08:20).",
                "GMT+03:00", TimeZone.getTimeZone("GMT+3").getID());
        /* j2objc: NSTimeZone can actually parse this format.
        assertEquals("Must return proper GMT formatted string for GMT+3:02 (eg. GMT+08:20).",
                "GMT+03:02", TimeZone.getTimeZone("GMT+3:02").getID());*/
        assertEquals("Must return proper GMT formatted string for GMT+2359 (eg. GMT+08:20).",
                "GMT+23:59", TimeZone.getTimeZone("GMT+2359").getID());
        assertEquals("Must return proper GMT formatted string for GMT+520 (eg. GMT+08:20).",
                "GMT+05:20", TimeZone.getTimeZone("GMT+520").getID());
        assertEquals("Must return proper GMT formatted string for GMT+052 (eg. GMT+08:20).",
                "GMT+00:52", TimeZone.getTimeZone("GMT+052").getID());
        /* j2objc: NSTimeZone treats GMT-00 as GMT.
        // GMT-0 is an available ID in ICU, so replace it with GMT-00
        assertEquals("Must return proper GMT formatted string for GMT-00 (eg. GMT+08:20).",
                "GMT-00:00", TimeZone.getTimeZone("GMT-00").getID());*/
    }

}
