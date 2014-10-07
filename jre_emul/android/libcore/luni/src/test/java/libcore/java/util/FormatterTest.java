/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import tests.support.Support_Locale;

public class FormatterTest extends junit.framework.TestCase {

  public void test_grouping() throws Exception {
        // The interesting case is -123, where you might naively output "-,123" if you're just
        // inserting a separator every three characters. The cases where there are three digits
        // before the first separator may also be interesting.
        assertEquals("-1", String.format("%,d", -1));
        assertEquals("-12", String.format("%,d", -12));
        assertEquals("-123", String.format("%,d", -123));
        assertEquals("-1,234", String.format("%,d", -1234));
        assertEquals("-12,345", String.format("%,d", -12345));
        assertEquals("-123,456", String.format("%,d", -123456));
        assertEquals("-1,234,567", String.format("%,d", -1234567));
        assertEquals("-12,345,678", String.format("%,d", -12345678));
        assertEquals("-123,456,789", String.format("%,d", -123456789));
        assertEquals("1", String.format("%,d", 1));
        assertEquals("12", String.format("%,d", 12));
        assertEquals("123", String.format("%,d", 123));
        assertEquals("1,234", String.format("%,d", 1234));
        assertEquals("12,345", String.format("%,d", 12345));
        assertEquals("123,456", String.format("%,d", 123456));
        assertEquals("1,234,567", String.format("%,d", 1234567));
        assertEquals("12,345,678", String.format("%,d", 12345678));
        assertEquals("123,456,789", String.format("%,d", 123456789));
    }

    public void test_formatNull() throws Exception {
        // We fast-path %s and %d (with no configuration) but need to make sure we handle the
        // special case of the null argument...
        assertEquals("null", String.format(Locale.US, "%s", (String) null));
        assertEquals("null", String.format(Locale.US, "%d", (Integer) null));
        // ...without screwing up conversions that don't take an argument.
        assertEquals("%", String.format(Locale.US, "%%"));
    }

    // https://code.google.com/p/android/issues/detail?id=53983
    public void test53983() throws Exception {
      checkFormat("00", "H", 00);
      checkFormat( "0", "k", 00);
      checkFormat("12", "I", 00);
      checkFormat("12", "l", 00);

      checkFormat("01", "H", 01);
      checkFormat( "1", "k", 01);
      checkFormat("01", "I", 01);
      checkFormat( "1", "l", 01);

      checkFormat("12", "H", 12);
      checkFormat("12", "k", 12);
      checkFormat("12", "I", 12);
      checkFormat("12", "l", 12);

      checkFormat("13", "H", 13);
      checkFormat("13", "k", 13);
      checkFormat("01", "I", 13);
      checkFormat( "1", "l", 13);

      checkFormat("00", "H", 24);
      checkFormat( "0", "k", 24);
      checkFormat("12", "I", 24);
      checkFormat("12", "l", 24);
    }

    private static void checkFormat(String expected, String pattern, int hour) {
      TimeZone utc = TimeZone.getTimeZone("UTC");

      Calendar c = new GregorianCalendar(utc);
      c.set(2013, Calendar.JANUARY, 1, hour, 00);

      assertEquals(expected, String.format(Locale.US, "%t" + pattern, c));
      assertEquals(expected, String.format(Locale.US, "%T" + pattern, c));
    }
}
