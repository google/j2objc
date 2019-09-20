/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License

package android.icu.text;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;


public class DigitListTest extends TestFmwk {

    private static DigitList digitList = new DigitList();
    private static long testdata = 1414213562;

    @Before
    public void init() {
        digitList.set(testdata);
    }

    @Test
    public void TestToString() {
        String digitListStr = digitList.toString();
        assertEquals("DigitList incorrect", "0.1414213562x10^10", digitListStr);
    }
    @Test
    public void TestHashCode() {
        int dlHashcode = digitList.hashCode();
        assertEquals("DigitList hash code incorrect", -616183837, dlHashcode);
    }

    @Test
    public void TestEquals() {
        DigitList digitList2 = new DigitList();

	// Test for success
        digitList2.set(testdata);
        assertTrue("DigitList objects with same values found unequal", digitList.equals(digitList2));
	// Test for failure
	digitList2.set(testdata+1);
	assertFalse("DigitList objects with different values found equal", digitList.equals(digitList2));
    }
}
