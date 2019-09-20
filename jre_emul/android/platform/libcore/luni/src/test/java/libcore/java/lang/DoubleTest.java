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

package libcore.java.lang;

import junit.framework.TestCase;

public class DoubleTest extends TestCase {
    public void testDoubleToStringUnsignedDivide() throws Exception {
        // http://b/3238333
        assertEquals("0.008", Double.toString(0.008));
        assertEquals("0.008366", Double.toString(0.008366));
        // http://code.google.com/p/android/issues/detail?id=14033
        assertEquals("0.009", Double.toString(0.009));
        // http://code.google.com/p/android/issues/detail?id=14302
        assertEquals("0.008567856012638986", Double.toString(0.008567856012638986));
        assertEquals("0.010206713752229896", Double.toString(0.010206713752229896));
    }

    public void testNamedDoubles() throws Exception {
        assertEquals(Double.NaN, Double.parseDouble("NaN"));
        assertEquals(Double.NaN, Double.parseDouble("-NaN"));
        assertEquals(Double.NaN, Double.parseDouble("+NaN"));
        try {
            Double.parseDouble("NNaN");
            fail();
        } catch (NumberFormatException expected) {
        }
        try {
            Double.parseDouble("NaNN");
            fail();
        } catch (NumberFormatException expected) {
        }

        assertEquals(Double.POSITIVE_INFINITY, Double.parseDouble("+Infinity"));
        assertEquals(Double.POSITIVE_INFINITY, Double.parseDouble("Infinity"));
        assertEquals(Double.NEGATIVE_INFINITY, Double.parseDouble("-Infinity"));
        try {
            Double.parseDouble("IInfinity");
            fail();
        } catch (NumberFormatException expected) {
        }
        try {
            Double.parseDouble("Infinityy");
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    public void testSuffixParsing() throws Exception {
        String[] badStrings = { "1ff", "1fd", "1df", "1dd" };
        for (String string : badStrings) {
            try {
                Double.parseDouble(string);
                fail(string);
            } catch (NumberFormatException expected) {
            }
        }
        assertEquals(1.0, Double.parseDouble("1f"));
        assertEquals(1.0, Double.parseDouble("1d"));
        assertEquals(1.0, Double.parseDouble("1F"));
        assertEquals(1.0, Double.parseDouble("1D"));
        assertEquals(1.0, Double.parseDouble("1.D"));
        assertEquals(1.0, Double.parseDouble("1.E0D"));
        assertEquals(1.0, Double.parseDouble(".1E1D"));
    }

    public void testExponentParsing() throws Exception {
        String[] strings = {
            // Exponents missing integer values.
            "1.0e", "1.0e+", "1.0e-",
            // Exponents with too many explicit signs.
            "1.0e++1", "1.0e+-1", "1.0e-+1", "1.0e--1",
            // http://code.google.com/p/android/issues/detail?id=20728
            "save+", "save-",
        };
        for (String string : strings) {
            try {
                Double.parseDouble(string);
                fail(string);
            } catch (NumberFormatException expected) {
            }
        }

        assertEquals(1.0e-323, Double.parseDouble("1.0e-323"));
        assertEquals(0.0, Double.parseDouble("1.0e-324"));
        assertEquals(-1.0e-323, Double.parseDouble("-1.0e-323"));
        assertEquals(-0.0, Double.parseDouble("-1.0e-324"));

        assertEquals(1.0e+308, Double.parseDouble("1.0e+308"));
        assertEquals(Double.POSITIVE_INFINITY, Double.parseDouble("1.0e+309"));
        assertEquals(-1.0e+308, Double.parseDouble("-1.0e+308"));
        assertEquals(Double.NEGATIVE_INFINITY, Double.parseDouble("-1.0e+309"));

        assertEquals(Double.POSITIVE_INFINITY, Double.parseDouble("1.0e+9999999999"));
        assertEquals(Double.NEGATIVE_INFINITY, Double.parseDouble("-1.0e+9999999999"));
        assertEquals(0.0, Double.parseDouble("1.0e-9999999999"));
        assertEquals(-0.0, Double.parseDouble("-1.0e-9999999999"));

        assertEquals(Double.POSITIVE_INFINITY, Double.parseDouble("320.0e+2147483647"));
        assertEquals(-0.0, Double.parseDouble("-1.4e-2147483314"));
    }

    /**
     * This value has been known to cause javac and java to infinite loop.
     * http://www.exploringbinary.com/java-hangs-when-converting-2-2250738585072012e-308/
     */
    public void testParseLargestSubnormalDoublePrecision() {
        assertEquals(2.2250738585072014E-308, Double.parseDouble("2.2250738585072012e-308"));
        assertEquals(2.2250738585072014E-308, Double.parseDouble("0.00022250738585072012e-304"));
        assertEquals(2.2250738585072014E-308, Double.parseDouble("00000002.2250738585072012e-308"));
        assertEquals(2.2250738585072014E-308, Double.parseDouble("2.225073858507201200000e-308"));
        assertEquals(2.2250738585072014E-308, Double.parseDouble("2.2250738585072012e-00308"));
        assertEquals(2.2250738585072014E-308, Double.parseDouble("2.22507385850720129978001e-308"));
        assertEquals(-2.2250738585072014E-308, Double.parseDouble("-2.2250738585072012e-308"));
    }

    // https://code.google.com/p/android/issues/detail?id=71216
    public void testParse_bug71216() {
        try {
            Double.parseDouble("73706943-9580-4406-a02f-0304e4324844");
            fail();
        } catch (NumberFormatException expected) {
        }

        try {
            Double.parseDouble("bade999999999999999999999999999999");
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    public void testStaticHashCode() {
        assertEquals(Double.valueOf(567.0).hashCode(), Double.hashCode(567.0));
    }

    public void testMax() {
        double a = 567.0;
        double b = 578.0;
        assertEquals(Math.max(a, b), Double.max(a, b));
    }

    public void testMin() {
        double a = 567.0;
        double b = 578.0;
        assertEquals(Math.min(a, b), Double.min(a, b));
    }

    public void testSum() {
        double a = 567.0;
        double b = 578.0;
        assertEquals(a + b, Double.sum(a, b));
    }

    public void testBYTES() {
        assertEquals(8, Double.BYTES);
    }
}
