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

package libcore.java.lang;

public class FloatTest extends junit.framework.TestCase {
    // Needed to prevent testVerifierTyping from statically resolving the if statement.
    static boolean testVerifierTypingBool = false;

    public void test_valueOf_String1() throws Exception {
        // This threw OutOfMemoryException.
        // http://code.google.com/p/android/issues/detail?id=4185
        assertEquals(2358.166016f, Float.valueOf("2358.166016"));
    }

    public void test_valueOf_String2() throws Exception {
        // This threw OutOfMemoryException.
        // http://code.google.com/p/android/issues/detail?id=3156
        assertEquals(-2.14748365E9f, Float.valueOf(String.valueOf(Integer.MIN_VALUE)));
    }

    public void testNamedFloats() throws Exception {
        assertEquals(Float.NaN, Float.parseFloat("NaN"));
        assertEquals(Float.NaN, Float.parseFloat("-NaN"));
        assertEquals(Float.NaN, Float.parseFloat("+NaN"));
        try {
            Float.parseFloat("NNaN");
            fail();
        } catch (NumberFormatException expected) {
        }
        try {
            Float.parseFloat("NaNN");
            fail();
        } catch (NumberFormatException expected) {
        }

        assertEquals(Float.POSITIVE_INFINITY, Float.parseFloat("+Infinity"));
        assertEquals(Float.POSITIVE_INFINITY, Float.parseFloat("Infinity"));
        assertEquals(Float.NEGATIVE_INFINITY, Float.parseFloat("-Infinity"));
        try {
            Float.parseFloat("IInfinity");
            fail();
        } catch (NumberFormatException expected) {
        }
        try {
            Float.parseFloat("Infinityy");
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    public void testSuffixParsing() throws Exception {
        String[] badStrings = { "1ff", "1fd", "1df", "1dd" };
        for (String string : badStrings) {
            try {
                Float.parseFloat(string);
                fail(string);
            } catch (NumberFormatException expected) {
            }
        }
        assertEquals(1.0f, Float.parseFloat("1f"));
        assertEquals(1.0f, Float.parseFloat("1d"));
        assertEquals(1.0f, Float.parseFloat("1F"));
        assertEquals(1.0f, Float.parseFloat("1D"));
        assertEquals(1.0f, Float.parseFloat("1.D"));
        assertEquals(1.0f, Float.parseFloat("1.E0D"));
        assertEquals(1.0f, Float.parseFloat(".1E1D"));
    }

    public void testExponentParsing() throws Exception {
        String[] strings = {
            // Exponents missing integer values.
            "1.0e", "1.0e+", "1.0e-",
            // Exponents with too many explicit signs.
            "1.0e++1", "1.0e+-1", "1.0e-+1", "1.0e--1"
        };
        for (String string : strings) {
            try {
                Float.parseFloat(string);
                fail(string);
            } catch (NumberFormatException expected) {
            }
        }

        assertEquals(1.0e-45f, Float.parseFloat("1.0e-45"));
        assertEquals(0.0f, Float.parseFloat("1.0e-46"));
        assertEquals(-1.0e-45f, Float.parseFloat("-1.0e-45"));
        assertEquals(-0.0f, Float.parseFloat("-1.0e-46"));

        assertEquals(1.0e+38f, Float.parseFloat("1.0e+38"));
        assertEquals(Float.POSITIVE_INFINITY, Float.parseFloat("1.0e+39"));
        assertEquals(-1.0e+38f, Float.parseFloat("-1.0e+38"));
        assertEquals(Float.NEGATIVE_INFINITY, Float.parseFloat("-1.0e+39"));

        assertEquals(Float.POSITIVE_INFINITY, Float.parseFloat("1.0e+9999999999"));
        assertEquals(Float.NEGATIVE_INFINITY, Float.parseFloat("-1.0e+9999999999"));
        assertEquals(0.0f, Float.parseFloat("1.0e-9999999999"));
        assertEquals(-0.0f, Float.parseFloat("-1.0e-9999999999"));

        assertEquals(Float.POSITIVE_INFINITY, Float.parseFloat("320.0E+2147483647"));
        assertEquals(-0.0f, Float.parseFloat("-1.4E-2147483314"));
    }

    public void testVerifierTyping() throws Exception {
      float f1 = 0;
      if (testVerifierTypingBool) {
        f1 = Float.MIN_VALUE;
      }
      assertEquals(f1, 0f);
    }
}
