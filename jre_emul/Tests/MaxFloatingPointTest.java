/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import junit.framework.TestCase;

import java.lang.Double;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;

/**
 * Verify overflow conversions of floating point to ints and longs are
 * the same as with the JVM. This test should pass both with the JVM and
 * as a J2ObjC binary, to assure its assumptions are correct.
 */
public class MaxFloatingPointTest extends TestCase {

  private static final String MAX_LONG_AS_STRING = "9223372036854775806";

  public void testDoubleConversions() {
    if (System.getProperty("os.arch").equals("armv7")) {
      return;
    }
    Double maxDouble = Double.MAX_VALUE;
    assertEquals(Integer.MAX_VALUE, maxDouble.intValue());
    assertEquals(Long.MAX_VALUE, maxDouble.longValue());
  }

  public void testFloatConversions() {
    if (System.getProperty("os.arch").equals("armv7")) {
      return;
    }
    Float maxFloat = Float.MAX_VALUE;
    assertEquals(Integer.MAX_VALUE, maxFloat.intValue());
    assertEquals(Long.MAX_VALUE, maxFloat.longValue());
  }

  public void testDoubleStringParsing() {
    Double.parseDouble("9223372036854775804");
    Double.parseDouble("9223372036854775805");
    Double.parseDouble("9223372036854775806");
    Double.parseDouble("9223372036854775807");
    assertEquals(Long.MAX_VALUE, (long) Double.parseDouble(MAX_LONG_AS_STRING));
  }

  public void testFloatStringParsing() {
    assertEquals(Long.MAX_VALUE, (long) Float.parseFloat(MAX_LONG_AS_STRING));
  }

  // Results are from the results of the Java Language Specification's
  // Example 5.1.3-1. Narrowing Primitive Conversion.
  public void testFloatNarrowing() {
    if (System.getProperty("os.arch").equals("armv7")) {
      return;
    }
    float fmin = Float.NEGATIVE_INFINITY;
    float fmax = Float.POSITIVE_INFINITY;
    assertEquals("fmin as long failed", Long.MIN_VALUE, (long) fmin);
    assertEquals("fmax as long failed", Long.MAX_VALUE, (long) fmax);
    assertEquals("fmin as int failed", Integer.MIN_VALUE, (int) fmin);
    assertEquals("fmax as int failed", Integer.MAX_VALUE, (int) fmax);
    assertEquals("fmin as char failed", Character.MIN_VALUE, (char) fmin);
    assertEquals("fmax as char failed", Character.MAX_VALUE, (char) fmax);

    // Surprising values for shorts and bytes, but that's what's specified.
    assertEquals("fmin as short failed", 0, (short) fmin);
    assertEquals("fmax as short failed", -1, (short) fmax);
    assertEquals("fmin as byte failed", 0, (byte) fmin);
    assertEquals("fmax as byte failed", -1, (byte) fmax);
  }

  public void testDoubleNarrowing() {
    if (System.getProperty("os.arch").equals("armv7")) {
      return;
    }
    double dmin = Double.NEGATIVE_INFINITY;
    double dmax = Double.POSITIVE_INFINITY;
    assertEquals("dmin as long failed", Long.MIN_VALUE, (long) dmin);
    assertEquals("dmax as long failed", Long.MAX_VALUE, (long) dmax);
    assertEquals("dmin as int failed", Integer.MIN_VALUE, (int) dmin);
    assertEquals("dmax as int failed", Integer.MAX_VALUE, (int) dmax);
    assertEquals("dmin as char failed", Character.MIN_VALUE, (char) dmin);
    assertEquals("dmax as char failed", Character.MAX_VALUE, (char) dmax);

    assertEquals("dmin as short failed", 0, (short) dmin);
    assertEquals("dmax as short failed", -1, (short) dmax);
    assertEquals("dmin as byte failed", 0, (byte) dmin);
    assertEquals("dmax as byte failed", -1, (byte) dmax);
  }

  public void testCompoundOperators() {
    int i = Integer.MAX_VALUE;
    i += 1.0;
    assertEquals(Integer.MAX_VALUE, i);
    i *= 1.5;
    assertEquals(Integer.MAX_VALUE, i);
    i -= -1.0;
    assertEquals(Integer.MAX_VALUE, i);
    i /= 0.5;
    assertEquals(Integer.MAX_VALUE, i);
    long l = Long.MAX_VALUE;
    l += 1.0;
    assertEquals(Long.MAX_VALUE, l);
    l *= 1.5;
    assertEquals(Long.MAX_VALUE, l);
    l -= -1.0;
    assertEquals(Long.MAX_VALUE, l);
    l /= 0.5;
    assertEquals(Long.MAX_VALUE, l);
  }
}
