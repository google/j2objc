/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.java.text;

import tests.support.Support_Locale;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*-[
#include <sys/utsname.h>
]-*/

public class NumberFormatTest extends junit.framework.TestCase {
    // NumberFormat.format(Object, StringBuffer, FieldPosition) guarantees it calls doubleValue for
    // custom Number subclasses.
    public void test_custom_Number_gets_longValue() throws Exception {
        class MyNumber extends Number {
            public byte byteValue() { throw new UnsupportedOperationException(); }
            public double doubleValue() { return 123; }
            public float floatValue() { throw new UnsupportedOperationException(); }
            public int intValue() { throw new UnsupportedOperationException(); }
            public long longValue() { throw new UnsupportedOperationException(); }
            public short shortValue() { throw new UnsupportedOperationException(); }
            public String toString() { throw new UnsupportedOperationException(); }
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        assertEquals("123", nf.format(new MyNumber()));
    }

    // NumberFormat.format(Object, StringBuffer, FieldPosition) guarantees it calls longValue for
    // any BigInteger with a bitLength strictly less than 64.
    // TODO(tball): enable when field position iterating on iOS is supported.
//    public void test_small_BigInteger_gets_longValue() throws Exception {
//        class MyNumberFormat extends NumberFormat {
//            public StringBuffer format(double value, StringBuffer b, FieldPosition f) {
//                b.append("double");
//                return b;
//            }
//            public StringBuffer format(long value, StringBuffer b, FieldPosition f) {
//                b.append("long");
//                return b;
//            }
//            public Number parse(String string, ParsePosition p) {
//                throw new UnsupportedOperationException();
//            }
//        }
//        NumberFormat nf = new MyNumberFormat();
//        assertEquals("long", nf.format(BigInteger.valueOf(Long.MAX_VALUE)));
//        assertEquals("double", nf.format(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)));
//        assertEquals("long", nf.format(BigInteger.valueOf(Long.MIN_VALUE)));
//        assertEquals("double", nf.format(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE)));
//    }

    public void test_getIntegerInstance_ar() throws Exception {
        if (!Support_Locale.isLocaleAvailable(new Locale("ar"))) {
            return;
        }
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("ar"));
        assertEquals("#,##0.###;#,##0.###", ((DecimalFormat) numberFormat).toPattern());
        NumberFormat integerFormat = NumberFormat.getIntegerInstance(new Locale("ar"));
        assertEquals("#,##0;#,##0", ((DecimalFormat) integerFormat).toPattern());
    }

    // Formatting percentages is confusing but deliberate.
    // Ensure we don't accidentally "fix" this.
    public void test_10333() throws Exception {
        NumberFormat nf = NumberFormat.getPercentInstance(Locale.US);
        assertEquals("15%", nf.format(0.15));
        assertEquals("1,500%", nf.format(15));
        try {
            nf.format("15");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // Verify that default formatters use correct precision and rounding.
    public void testDefaultNumberFormats() {
      assertEquals(NumberFormat.getInstance().format(23.45678), "23.457");
      assertEquals(NumberFormat.getCurrencyInstance().format(23.45678), "$23.46");
      assertEquals(NumberFormat.getIntegerInstance().format(23.45678), "23");
      assertEquals(NumberFormat.getNumberInstance().format(23.45678), "23.457");
      assertEquals(NumberFormat.getPercentInstance().format(23.45678), "2,346%");
    }

    // Verify that default formatters use correct precision and rounding with
    // the French locale.
    public void testFrenchNumberFormats() {
      Locale locale = Locale.FRANCE;
      assertEquals(NumberFormat.getInstance(locale).format(23.45678), "23,457");
      assertEquals(NumberFormat.getCurrencyInstance(locale).format(23.45678), "23.46 €");
      assertEquals(NumberFormat.getIntegerInstance(locale).format(23.45678), "23");
      assertEquals(NumberFormat.getNumberInstance(locale).format(23.45678), "23,457");
      assertEquals(NumberFormat.getPercentInstance(locale).format(23.45678), "2 346 %");
    }
}
