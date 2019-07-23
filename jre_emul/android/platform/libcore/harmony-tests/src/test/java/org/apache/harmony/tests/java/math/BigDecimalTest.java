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

package tests.api.java.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;


public class BigDecimalTest extends junit.framework.TestCase {
	BigInteger value = new BigInteger("12345908");

	BigInteger value2 = new BigInteger("12334560000");

	/**
	 * @tests java.math.BigDecimal#BigDecimal(java.math.BigInteger)
	 */
	public void test_ConstructorLjava_math_BigInteger() {
		BigDecimal big = new BigDecimal(value);
		assertTrue("the BigDecimal value is not initialized properly", big
				.unscaledValue().equals(value)
				&& big.scale() == 0);
	}

	/**
	 * @tests java.math.BigDecimal#BigDecimal(java.math.BigInteger, int)
	 */
	public void test_ConstructorLjava_math_BigIntegerI() {
		BigDecimal big = new BigDecimal(value2, 5);
		assertTrue("the BigDecimal value is not initialized properly", big
				.unscaledValue().equals(value2)
				&& big.scale() == 5);
		assertTrue("the BigDecimal value is not represented properly", big
				.toString().equals("123345.60000"));
	}

	/**
	 * @tests java.math.BigDecimal#BigDecimal(double)
	 */
	public void test_ConstructorD() {
		BigDecimal big = new BigDecimal(123E04);
		assertTrue(
				"the BigDecimal value taking a double argument is not initialized properly",
				big.toString().equals("1230000"));
		big = new BigDecimal(1.2345E-12);
		assertTrue("the double representation is not correct", big
				.doubleValue() == 1.2345E-12);
		big = new BigDecimal(-12345E-3);
		assertTrue("the double representation is not correct", big
				.doubleValue() == -12.345);
		big = new BigDecimal(5.1234567897654321e138);
		assertTrue("the double representation is not correct", big
				.doubleValue() == 5.1234567897654321E138
				&& big.scale() == 0);
		big = new BigDecimal(0.1);
		assertTrue(
				"the double representation of 0.1 bigDecimal is not correct",
				big.doubleValue() == 0.1);
		big = new BigDecimal(0.00345);
		assertTrue(
				"the double representation of 0.00345 bigDecimal is not correct",
				big.doubleValue() == 0.00345);
        // regression test for HARMONY-2429
        big = new BigDecimal(-0.0);
        assertTrue(
        		"the double representation of -0.0 bigDecimal is not correct",
        		big.scale() == 0);
	}

	/**
	 * @tests java.math.BigDecimal#BigDecimal(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() throws NumberFormatException {
		BigDecimal big = new BigDecimal("345.23499600293850");
		assertTrue("the BigDecimal value is not initialized properly", big
				.toString().equals("345.23499600293850")
				&& big.scale() == 14);
		big = new BigDecimal("-12345");
		assertTrue("the BigDecimal value is not initialized properly", big
				.toString().equals("-12345")
				&& big.scale() == 0);
		big = new BigDecimal("123.");
		assertTrue("the BigDecimal value is not initialized properly", big
				.toString().equals("123")
				&& big.scale() == 0);

		new BigDecimal("1.234E02");
	}

	/**
	 * @tests java.math.BigDecimal#BigDecimal(java.lang.String)
	 */
	public void test_constructor_String_plus_exp() {
		/*
		 * BigDecimal does not support a + sign in the exponent when converting
		 * from a String
		 */
		new BigDecimal(+23e-0);
		new BigDecimal(-23e+0);
	}

	/**
	 * @tests java.math.BigDecimal#BigDecimal(java.lang.String)
	 */
	public void test_constructor_String_empty() {
		try {
			new BigDecimal("");			
            fail("NumberFormatException expected");
		} catch (NumberFormatException e) {
		}
	}
	
	/**
	 * @tests java.math.BigDecimal#BigDecimal(java.lang.String)
	 */
	public void test_constructor_String_plus_minus_exp() {
		try {
			new BigDecimal("+35e+-2");			
            fail("NumberFormatException expected");
		} catch (NumberFormatException e) {
		}
		
		try {
			new BigDecimal("-35e-+2");			
            fail("NumberFormatException expected");
		} catch (NumberFormatException e) {
		}
	}
    
    /**
     * @tests java.math.BigDecimal#BigDecimal(char[])
     */
    public void test_constructor_CC_plus_minus_exp() {
        try {
            new BigDecimal("+35e+-2".toCharArray());          
            fail("NumberFormatException expected");
        } catch (NumberFormatException e) {
        }
        
        try {
            new BigDecimal("-35e-+2".toCharArray());          
            fail("NumberFormatException expected");
        } catch (NumberFormatException e) {
        }
    }

	/**
	 * @tests java.math.BigDecimal#abs()
	 */
	public void test_abs() {
		BigDecimal big = new BigDecimal("-1234");
		BigDecimal bigabs = big.abs();
		assertTrue("the absolute value of -1234 is not 1234", bigabs.toString()
				.equals("1234"));
		big = new BigDecimal(new BigInteger("2345"), 2);
		bigabs = big.abs();
		assertTrue("the absolute value of 23.45 is not 23.45", bigabs
				.toString().equals("23.45"));
	}

	/**
	 * @tests java.math.BigDecimal#add(java.math.BigDecimal)
	 */
	public void test_addLjava_math_BigDecimal() {
		BigDecimal add1 = new BigDecimal("23.456");
		BigDecimal add2 = new BigDecimal("3849.235");
		BigDecimal sum = add1.add(add2);
		assertTrue("the sum of 23.456 + 3849.235 is wrong", sum.unscaledValue()
				.toString().equals("3872691")
				&& sum.scale() == 3);
		assertTrue("the sum of 23.456 + 3849.235 is not printed correctly", sum
				.toString().equals("3872.691"));
		BigDecimal add3 = new BigDecimal(12.34E02D);
		assertTrue("the sum of 23.456 + 12.34E02 is not printed correctly",
				(add1.add(add3)).toString().equals("1257.456"));
	}

	/**
	 * @tests java.math.BigDecimal#compareTo(java.math.BigDecimal)
	 */
	public void test_compareToLjava_math_BigDecimal() {
		BigDecimal comp1 = new BigDecimal("1.00");
		BigDecimal comp2 = new BigDecimal(1.000000D);
		assertTrue("1.00 and 1.000000 should be equal",
				comp1.compareTo(comp2) == 0);
		BigDecimal comp3 = new BigDecimal("1.02");
		assertTrue("1.02 should be bigger than 1.00",
				comp3.compareTo(comp1) == 1);
		BigDecimal comp4 = new BigDecimal(0.98D);
		assertTrue("0.98 should be less than 1.00",
				comp4.compareTo(comp1) == -1);
	}

	/**
	 * @tests java.math.BigDecimal#divide(java.math.BigDecimal, int)
	 */
	public void test_divideLjava_math_BigDecimalI() {
		BigDecimal divd1 = new BigDecimal(value, 2);
		BigDecimal divd2 = new BigDecimal("2.335");
		BigDecimal divd3 = divd1.divide(divd2, BigDecimal.ROUND_UP);
		assertTrue("123459.08/2.335 is not correct", divd3.toString().equals(
				"52873.27")
				&& divd3.scale() == divd1.scale());
		assertTrue(
				"the unscaledValue representation of 123459.08/2.335 is not correct",
				divd3.unscaledValue().toString().equals("5287327"));
		divd2 = new BigDecimal(123.4D);
		divd3 = divd1.divide(divd2, BigDecimal.ROUND_DOWN);
		assertTrue("123459.08/123.4  is not correct", divd3.toString().equals(
				"1000.47")
				&& divd3.scale() == 2);
		divd2 = new BigDecimal(000D);

		try {
			divd1.divide(divd2, BigDecimal.ROUND_DOWN);
            fail("divide by zero is not caught");
		} catch (ArithmeticException e) {
		}
	}

	/**
	 * @tests java.math.BigDecimal#divide(java.math.BigDecimal, int, int)
	 */
	public void test_divideLjava_math_BigDecimalII() {
		BigDecimal divd1 = new BigDecimal(value2, 4);
		BigDecimal divd2 = new BigDecimal("0.0023");
		BigDecimal divd3 = divd1.divide(divd2, 3, BigDecimal.ROUND_HALF_UP);
		assertTrue("1233456/0.0023 is not correct", divd3.toString().equals(
				"536285217.391")
				&& divd3.scale() == 3);
		divd2 = new BigDecimal(1345.5E-02D);
		divd3 = divd1.divide(divd2, 0, BigDecimal.ROUND_DOWN);
		assertTrue(
				"1233456/13.455 is not correct or does not have the correct scale",
				divd3.toString().equals("91672") && divd3.scale() == 0);
		divd2 = new BigDecimal(0000D);

		try {
			divd1.divide(divd2, 4, BigDecimal.ROUND_DOWN);
            fail("divide by zero is not caught");
		} catch (ArithmeticException e) {
		}
	}

	/**
	 * @tests java.math.BigDecimal#doubleValue()
	 */
	public void test_doubleValue() {
		BigDecimal bigDB = new BigDecimal(-1.234E-112);
//		Commenting out this part because it causes an endless loop (see HARMONY-319 and HARMONY-329)
//		assertTrue(
//				"the double representation of this BigDecimal is not correct",
//				bigDB.doubleValue() == -1.234E-112);
		bigDB = new BigDecimal(5.00E-324);
		assertTrue("the double representation of bigDecimal is not correct",
				bigDB.doubleValue() == 5.00E-324);
		bigDB = new BigDecimal(1.79E308);
		assertTrue("the double representation of bigDecimal is not correct",
				bigDB.doubleValue() == 1.79E308 && bigDB.scale() == 0);
		bigDB = new BigDecimal(-2.33E102);
		assertTrue(
				"the double representation of bigDecimal -2.33E102 is not correct",
				bigDB.doubleValue() == -2.33E102 && bigDB.scale() == 0);
		bigDB = new BigDecimal(Double.MAX_VALUE);
		bigDB = bigDB.add(bigDB);
		assertTrue(
				"a  + number out of the double range should return infinity",
				bigDB.doubleValue() == Double.POSITIVE_INFINITY);
		bigDB = new BigDecimal(-Double.MAX_VALUE);
		bigDB = bigDB.add(bigDB);
		assertTrue(
				"a  - number out of the double range should return neg infinity",
				bigDB.doubleValue() == Double.NEGATIVE_INFINITY);
	}

	/**
	 * @tests java.math.BigDecimal#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		BigDecimal equal1 = new BigDecimal(1.00D);
		BigDecimal equal2 = new BigDecimal("1.0");
		assertFalse("1.00 and 1.0 should not be equal",
				equal1.equals(equal2));
		equal2 = new BigDecimal(1.01D);
		assertFalse("1.00 and 1.01 should not be equal",
				equal1.equals(equal2));
		equal2 = new BigDecimal("1.00");
		assertFalse("1.00D and 1.00 should not be equal",
				equal1.equals(equal2));
		BigInteger val = new BigInteger("100");
		equal1 = new BigDecimal("1.00");
		equal2 = new BigDecimal(val, 2);
		assertTrue("1.00(string) and 1.00(bigInteger) should be equal", equal1
				.equals(equal2));
		equal1 = new BigDecimal(100D);
		equal2 = new BigDecimal("2.34576");
		assertFalse("100D and 2.34576 should not be equal", equal1
				.equals(equal2));
		assertFalse("bigDecimal 100D does not equal string 23415", equal1
				.equals("23415"));
	}

	/**
	 * @tests java.math.BigDecimal#floatValue()
	 */
	public void test_floatValue() {
		BigDecimal fl1 = new BigDecimal("234563782344567");
		assertTrue("the float representation of bigDecimal 234563782344567",
				fl1.floatValue() == 234563782344567f);
		BigDecimal fl2 = new BigDecimal(2.345E37);
		assertTrue("the float representation of bigDecimal 2.345E37", fl2
				.floatValue() == 2.345E37F);
		fl2 = new BigDecimal(-1.00E-44);
		assertTrue("the float representation of bigDecimal -1.00E-44", fl2
				.floatValue() == -1.00E-44F);
		fl2 = new BigDecimal(-3E12);
		assertTrue("the float representation of bigDecimal -3E12", fl2
				.floatValue() == -3E12F);
		fl2 = new BigDecimal(Double.MAX_VALUE);
		assertTrue(
				"A number can't be represented by float should return infinity",
				fl2.floatValue() == Float.POSITIVE_INFINITY);
		fl2 = new BigDecimal(-Double.MAX_VALUE);
		assertTrue(
				"A number can't be represented by float should return infinity",
				fl2.floatValue() == Float.NEGATIVE_INFINITY);

	}

	/**
	 * @tests java.math.BigDecimal#hashCode()
	 */
	public void test_hashCode() {
		// anything that is equal must have the same hashCode
		BigDecimal hash = new BigDecimal("1.00");
		BigDecimal hash2 = new BigDecimal(1.00D);
		assertTrue("the hashCode of 1.00 and 1.00D is equal",
				hash.hashCode() != hash2.hashCode() && !hash.equals(hash2));
		hash2 = new BigDecimal("1.0");
		assertTrue("the hashCode of 1.0 and 1.00 is equal",
				hash.hashCode() != hash2.hashCode() && !hash.equals(hash2));
		BigInteger val = new BigInteger("100");
		hash2 = new BigDecimal(val, 2);
		assertTrue("hashCode of 1.00 and 1.00(bigInteger) is not equal", hash
				.hashCode() == hash2.hashCode()
				&& hash.equals(hash2));
		hash = new BigDecimal(value, 2);
		hash2 = new BigDecimal("-1233456.0000");
		assertTrue("hashCode of 123459.08 and -1233456.0000 is not equal", hash
				.hashCode() != hash2.hashCode()
				&& !hash.equals(hash2));
		hash2 = new BigDecimal(value.negate(), 2);
		assertTrue("hashCode of 123459.08 and -123459.08 is not equal", hash
				.hashCode() != hash2.hashCode()
				&& !hash.equals(hash2));
	}

	/**
	 * @tests java.math.BigDecimal#intValue()
	 */
	public void test_intValue() {
		BigDecimal int1 = new BigDecimal(value, 3);
		assertTrue("the int value of 12345.908 is not 12345",
				int1.intValue() == 12345);
		int1 = new BigDecimal("1.99");
		assertTrue("the int value of 1.99 is not 1", int1.intValue() == 1);
		int1 = new BigDecimal("23423419083091823091283933");
		// ran JDK and found representation for the above was -249268259
		assertTrue("the int value of 23423419083091823091283933 is wrong", int1
				.intValue() == -249268259);
		int1 = new BigDecimal(-1235D);
		assertTrue("the int value of -1235 is not -1235",
				int1.intValue() == -1235);
	}

	/**
	 * @tests java.math.BigDecimal#longValue()
	 */
	public void test_longValue() {
		BigDecimal long1 = new BigDecimal(value2.negate(), 0);
		assertTrue("the long value of 12334560000 is not 12334560000", long1
				.longValue() == -12334560000L);
		long1 = new BigDecimal(-1345.348E-123D);
		assertTrue("the long value of -1345.348E-123D is not zero", long1
				.longValue() == 0);
		long1 = new BigDecimal("31323423423419083091823091283933");
		// ran JDK and found representation for the above was
		// -5251313250005125155
		assertTrue(
				"the long value of 31323423423419083091823091283933 is wrong",
				long1.longValue() == -5251313250005125155L);
	}

	/**
	 * @tests java.math.BigDecimal#max(java.math.BigDecimal)
	 */
	public void test_maxLjava_math_BigDecimal() {
		BigDecimal max1 = new BigDecimal(value2, 1);
		BigDecimal max2 = new BigDecimal(value2, 4);
		assertTrue("1233456000.0 is not greater than 1233456", max1.max(max2)
				.equals(max1));
		max1 = new BigDecimal(-1.224D);
		max2 = new BigDecimal(-1.2245D);
		assertTrue("-1.224 is not greater than -1.2245", max1.max(max2).equals(
				max1));
		max1 = new BigDecimal(123E18);
		max2 = new BigDecimal(123E19);
		assertTrue("123E19 is the not the max", max1.max(max2).equals(max2));
	}

	/**
	 * @tests java.math.BigDecimal#min(java.math.BigDecimal)
	 */
	public void test_minLjava_math_BigDecimal() {
		BigDecimal min1 = new BigDecimal(-12345.4D);
		BigDecimal min2 = new BigDecimal(-12345.39D);
		assertTrue("-12345.39 should have been returned", min1.min(min2)
				.equals(min1));
		min1 = new BigDecimal(value2, 5);
		min2 = new BigDecimal(value2, 0);
		assertTrue("123345.6 should have been returned", min1.min(min2).equals(
				min1));
	}

	/**
	 * @tests java.math.BigDecimal#movePointLeft(int)
	 */
	public void test_movePointLeftI() {
		BigDecimal movePtLeft = new BigDecimal("123456265.34");
		BigDecimal alreadyMoved = movePtLeft.movePointLeft(5);
		assertTrue("move point left 5 failed", alreadyMoved.scale() == 7
				&& alreadyMoved.toString().equals("1234.5626534"));
		movePtLeft = new BigDecimal(value2.negate(), 0);
		alreadyMoved = movePtLeft.movePointLeft(12);
		assertTrue("move point left 12 failed", alreadyMoved.scale() == 12
				&& alreadyMoved.toString().equals("-0.012334560000"));
		movePtLeft = new BigDecimal(123E18);
		alreadyMoved = movePtLeft.movePointLeft(2);
		assertTrue("move point left 2 failed",
				alreadyMoved.scale() == movePtLeft.scale() + 2
						&& alreadyMoved.doubleValue() == 1.23E18);
		movePtLeft = new BigDecimal(1.123E-12);
		alreadyMoved = movePtLeft.movePointLeft(3);
		assertTrue("move point left 3 failed",
				alreadyMoved.scale() == movePtLeft.scale() + 3
						&& alreadyMoved.doubleValue() == 1.123E-15);
		movePtLeft = new BigDecimal(value, 2);
		alreadyMoved = movePtLeft.movePointLeft(-2);
		assertTrue("move point left -2 failed",
				alreadyMoved.scale() == movePtLeft.scale() - 2
						&& alreadyMoved.toString().equals("12345908"));
	}

	/**
	 * @tests java.math.BigDecimal#movePointRight(int)
	 */
	public void test_movePointRightI() {
		BigDecimal movePtRight = new BigDecimal("-1.58796521458");
		BigDecimal alreadyMoved = movePtRight.movePointRight(8);
		assertTrue("move point right 8 failed", alreadyMoved.scale() == 3
				&& alreadyMoved.toString().equals("-158796521.458"));
		movePtRight = new BigDecimal(value, 2);
		alreadyMoved = movePtRight.movePointRight(4);
		assertTrue("move point right 4 failed", alreadyMoved.scale() == 0
				&& alreadyMoved.toString().equals("1234590800"));
		movePtRight = new BigDecimal(134E12);
		alreadyMoved = movePtRight.movePointRight(2);
		assertTrue("move point right 2 failed", alreadyMoved.scale() == 0
				&& alreadyMoved.toString().equals("13400000000000000"));
		movePtRight = new BigDecimal(-3.4E-10);
		alreadyMoved = movePtRight.movePointRight(5);
		assertTrue("move point right 5 failed",
				alreadyMoved.scale() == movePtRight.scale() - 5
						&& alreadyMoved.doubleValue() == -0.000034);
		alreadyMoved = alreadyMoved.movePointRight(-5);
		assertTrue("move point right -5 failed", alreadyMoved
				.equals(movePtRight));
	}

	/**
	 * @tests java.math.BigDecimal#multiply(java.math.BigDecimal)
	 */
	public void test_multiplyLjava_math_BigDecimal() {
		BigDecimal multi1 = new BigDecimal(value, 5);
		BigDecimal multi2 = new BigDecimal(2.345D);
		BigDecimal result = multi1.multiply(multi2);
		assertTrue("123.45908 * 2.345 is not correct: " + result, result
				.toString().startsWith("289.51154260")
				&& result.scale() == multi1.scale() + multi2.scale());
		multi1 = new BigDecimal("34656");
		multi2 = new BigDecimal("-2");
		result = multi1.multiply(multi2);
		assertTrue("34656 * 2 is not correct", result.toString().equals(
				"-69312")
				&& result.scale() == 0);
		multi1 = new BigDecimal(-2.345E-02);
		multi2 = new BigDecimal(-134E130);
		result = multi1.multiply(multi2);
		assertTrue("-2.345E-02 * -134E130 is not correct " + result.doubleValue(),
				result.doubleValue() == 3.1422999999999997E130
						&& result.scale() == multi1.scale() + multi2.scale());
		multi1 = new BigDecimal("11235");
		multi2 = new BigDecimal("0");
		result = multi1.multiply(multi2);
		assertTrue("11235 * 0 is not correct", result.doubleValue() == 0
				&& result.scale() == 0);
		multi1 = new BigDecimal("-0.00234");
		multi2 = new BigDecimal(13.4E10);
		result = multi1.multiply(multi2);
		assertTrue("-0.00234 * 13.4E10 is not correct",
				result.doubleValue() == -313560000
						&& result.scale() == multi1.scale() + multi2.scale());
	}

	/**
	 * @tests java.math.BigDecimal#negate()
	 */
	public void test_negate() {
		BigDecimal negate1 = new BigDecimal(value2, 7);
		assertTrue("the negate of 1233.4560000 is not -1233.4560000", negate1
				.negate().toString().equals("-1233.4560000"));
		negate1 = new BigDecimal("-23465839");
		assertTrue("the negate of -23465839 is not 23465839", negate1.negate()
				.toString().equals("23465839"));
		negate1 = new BigDecimal(-3.456E6);
		assertTrue("the negate of -3.456E6 is not 3.456E6", negate1.negate()
				.negate().equals(negate1));
	}

	/**
	 * @tests java.math.BigDecimal#scale()
	 */
	public void test_scale() {
		BigDecimal scale1 = new BigDecimal(value2, 8);
		assertTrue("the scale of the number 123.34560000 is wrong", scale1
				.scale() == 8);
		BigDecimal scale2 = new BigDecimal("29389.");
		assertTrue("the scale of the number 29389. is wrong",
				scale2.scale() == 0);
		BigDecimal scale3 = new BigDecimal(3.374E13);
		assertTrue("the scale of the number 3.374E13 is wrong",
				scale3.scale() == 0);
		BigDecimal scale4 = new BigDecimal("-3.45E-203");
		// note the scale is calculated as 15 digits of 345000.... + exponent -
		// 1. -1 for the 3
		assertTrue("the scale of the number -3.45E-203 is wrong: "
				+ scale4.scale(), scale4.scale() == 205);
		scale4 = new BigDecimal("-345.4E-200");
		assertTrue("the scale of the number -345.4E-200 is wrong", scale4
				.scale() == 201);
	}

	/**
	 * @tests java.math.BigDecimal#setScale(int)
	 */
	public void test_setScaleI() {
		// rounding mode defaults to zero
		BigDecimal setScale1 = new BigDecimal(value, 3);
		BigDecimal setScale2 = setScale1.setScale(5);
		BigInteger setresult = new BigInteger("1234590800");
		assertTrue("the number 12345.908 after setting scale is wrong",
				setScale2.unscaledValue().equals(setresult)
						&& setScale2.scale() == 5);

		try {
			setScale2 = setScale1.setScale(2, BigDecimal.ROUND_UNNECESSARY);
            fail("arithmetic Exception not caught as a result of loosing precision");
		} catch (ArithmeticException e) {
		}
	}

	/**
	 * @tests java.math.BigDecimal#setScale(int, int)
	 */
	public void test_setScaleII() {
		BigDecimal setScale1 = new BigDecimal(2.323E102);
		BigDecimal setScale2 = setScale1.setScale(4);
		assertTrue("the number 2.323E102 after setting scale is wrong",
				setScale2.scale() == 4);
		assertTrue("the representation of the number 2.323E102 is wrong",
				setScale2.doubleValue() == 2.323E102);
		setScale1 = new BigDecimal("-1.253E-12");
		setScale2 = setScale1.setScale(17, BigDecimal.ROUND_CEILING);
		assertTrue("the number -1.253E-12 after setting scale is wrong",
				setScale2.scale() == 17);
		assertTrue(
				"the representation of the number -1.253E-12 after setting scale is wrong, " + setScale2.toString(),
				setScale2.toString().equals("-1.25300E-12"));

		// testing rounding Mode ROUND_CEILING
		setScale1 = new BigDecimal(value, 4);
		setScale2 = setScale1.setScale(1, BigDecimal.ROUND_CEILING);
		assertTrue(
				"the number 1234.5908 after setting scale to 1/ROUND_CEILING is wrong",
				setScale2.toString().equals("1234.6") && setScale2.scale() == 1);
		BigDecimal setNeg = new BigDecimal(value.negate(), 4);
		setScale2 = setNeg.setScale(1, BigDecimal.ROUND_CEILING);
		assertTrue(
				"the number -1234.5908 after setting scale to 1/ROUND_CEILING is wrong",
				setScale2.toString().equals("-1234.5")
						&& setScale2.scale() == 1);

		// testing rounding Mode ROUND_DOWN
		setScale2 = setNeg.setScale(1, BigDecimal.ROUND_DOWN);
		assertTrue(
				"the number -1234.5908 after setting scale to 1/ROUND_DOWN is wrong",
				setScale2.toString().equals("-1234.5")
						&& setScale2.scale() == 1);
		setScale1 = new BigDecimal(value, 4);
		setScale2 = setScale1.setScale(1, BigDecimal.ROUND_DOWN);
		assertTrue(
				"the number 1234.5908 after setting scale to 1/ROUND_DOWN is wrong",
				setScale2.toString().equals("1234.5") && setScale2.scale() == 1);

		// testing rounding Mode ROUND_FLOOR
		setScale2 = setScale1.setScale(1, BigDecimal.ROUND_FLOOR);
		assertTrue(
				"the number 1234.5908 after setting scale to 1/ROUND_FLOOR is wrong",
				setScale2.toString().equals("1234.5") && setScale2.scale() == 1);
		setScale2 = setNeg.setScale(1, BigDecimal.ROUND_FLOOR);
		assertTrue(
				"the number -1234.5908 after setting scale to 1/ROUND_FLOOR is wrong",
				setScale2.toString().equals("-1234.6")
						&& setScale2.scale() == 1);

		// testing rounding Mode ROUND_HALF_DOWN
		setScale2 = setScale1.setScale(3, BigDecimal.ROUND_HALF_DOWN);
		assertTrue(
				"the number 1234.5908 after setting scale to 3/ROUND_HALF_DOWN is wrong",
				setScale2.toString().equals("1234.591")
						&& setScale2.scale() == 3);
		setScale1 = new BigDecimal(new BigInteger("12345000"), 5);
		setScale2 = setScale1.setScale(1, BigDecimal.ROUND_HALF_DOWN);
		assertTrue(
				"the number 123.45908 after setting scale to 1/ROUND_HALF_DOWN is wrong",
				setScale2.toString().equals("123.4") && setScale2.scale() == 1);
		setScale2 = new BigDecimal("-1234.5000").setScale(0,
				BigDecimal.ROUND_HALF_DOWN);
		assertTrue(
				"the number -1234.5908 after setting scale to 0/ROUND_HALF_DOWN is wrong",
				setScale2.toString().equals("-1234") && setScale2.scale() == 0);

		// testing rounding Mode ROUND_HALF_EVEN
		setScale1 = new BigDecimal(1.2345789D);
		setScale2 = setScale1.setScale(4, BigDecimal.ROUND_HALF_EVEN);
		assertTrue(
				"the number 1.2345789 after setting scale to 4/ROUND_HALF_EVEN is wrong",
				setScale2.doubleValue() == 1.2346D && setScale2.scale() == 4);
		setNeg = new BigDecimal(-1.2335789D);
		setScale2 = setNeg.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		assertTrue(
				"the number -1.2335789 after setting scale to 2/ROUND_HALF_EVEN is wrong",
				setScale2.doubleValue() == -1.23D && setScale2.scale() == 2);
		setScale2 = new BigDecimal("1.2345000").setScale(3,
				BigDecimal.ROUND_HALF_EVEN);
		assertTrue(
				"the number 1.2345789 after setting scale to 3/ROUND_HALF_EVEN is wrong",
				setScale2.doubleValue() == 1.234D && setScale2.scale() == 3);
		setScale2 = new BigDecimal("-1.2345000").setScale(3,
				BigDecimal.ROUND_HALF_EVEN);
		assertTrue(
				"the number -1.2335789 after setting scale to 3/ROUND_HALF_EVEN is wrong",
				setScale2.doubleValue() == -1.234D && setScale2.scale() == 3);

		// testing rounding Mode ROUND_HALF_UP
		setScale1 = new BigDecimal("134567.34650");
		setScale2 = setScale1.setScale(3, BigDecimal.ROUND_HALF_UP);
		assertTrue(
				"the number 134567.34658 after setting scale to 3/ROUND_HALF_UP is wrong",
				setScale2.toString().equals("134567.347")
						&& setScale2.scale() == 3);
		setNeg = new BigDecimal("-1234.4567");
		setScale2 = setNeg.setScale(0, BigDecimal.ROUND_HALF_UP);
		assertTrue(
				"the number -1234.4567 after setting scale to 0/ROUND_HALF_UP is wrong",
				setScale2.toString().equals("-1234") && setScale2.scale() == 0);

		// testing rounding Mode ROUND_UNNECESSARY
		try {
			setScale1.setScale(3, BigDecimal.ROUND_UNNECESSARY);
            fail("arithmetic Exception not caught for round unnecessary");
		} catch (ArithmeticException e) {
		}

		// testing rounding Mode ROUND_UP
		setScale1 = new BigDecimal("100000.374");
		setScale2 = setScale1.setScale(2, BigDecimal.ROUND_UP);
		assertTrue(
				"the number 100000.374 after setting scale to 2/ROUND_UP is wrong",
				setScale2.toString().equals("100000.38")
						&& setScale2.scale() == 2);
		setNeg = new BigDecimal(-134.34589D);
		setScale2 = setNeg.setScale(2, BigDecimal.ROUND_UP);
		assertTrue(
				"the number -134.34589 after setting scale to 2/ROUND_UP is wrong",
				setScale2.doubleValue() == -134.35D && setScale2.scale() == 2);

		// testing invalid rounding modes
		try {
			setScale2 = setScale1.setScale(0, -123);
            fail("IllegalArgumentException is not caught for wrong rounding mode");
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * @tests java.math.BigDecimal#signum()
	 */
	public void test_signum() {
		BigDecimal sign = new BigDecimal(123E-104);
		assertTrue("123E-104 is not positive in signum()", sign.signum() == 1);
		sign = new BigDecimal("-1234.3959");
		assertTrue("-1234.3959 is not negative in signum()",
				sign.signum() == -1);
		sign = new BigDecimal(000D);
		assertTrue("000D is not zero in signum()", sign.signum() == 0);
	}

	/**
	 * @tests java.math.BigDecimal#subtract(java.math.BigDecimal)
	 */
	public void test_subtractLjava_math_BigDecimal() {
		BigDecimal sub1 = new BigDecimal("13948");
		BigDecimal sub2 = new BigDecimal("2839.489");
		BigDecimal result = sub1.subtract(sub2);
		assertTrue("13948 - 2839.489 is wrong: " + result, result.toString()
				.equals("11108.511")
				&& result.scale() == 3);
		BigDecimal result2 = sub2.subtract(sub1);
		assertTrue("2839.489 - 13948 is wrong", result2.toString().equals(
				"-11108.511")
				&& result2.scale() == 3);
		assertTrue("13948 - 2839.489 is not the negative of 2839.489 - 13948",
				result.equals(result2.negate()));
		sub1 = new BigDecimal(value, 1);
		sub2 = new BigDecimal("0");
		result = sub1.subtract(sub2);
		assertTrue("1234590.8 - 0 is wrong", result.equals(sub1));
		sub1 = new BigDecimal(1.234E-03);
		sub2 = new BigDecimal(3.423E-10);
		result = sub1.subtract(sub2);
		assertTrue("1.234E-03 - 3.423E-10 is wrong, " + result.doubleValue(),
				result.doubleValue() == 0.0012339996577);
		sub1 = new BigDecimal(1234.0123);
		sub2 = new BigDecimal(1234.0123000);
		result = sub1.subtract(sub2);
		assertTrue("1234.0123 - 1234.0123000 is wrong, " + result.doubleValue(),
				result.doubleValue() == 0.0);
	}

	/**
	 * @tests java.math.BigDecimal#toBigInteger()
	 */
	public void test_toBigInteger() {
		BigDecimal sub1 = new BigDecimal("-29830.989");
		BigInteger result = sub1.toBigInteger();

		assertTrue("the bigInteger equivalent of -29830.989 is wrong", result
				.toString().equals("-29830"));
		sub1 = new BigDecimal(-2837E10);
		result = sub1.toBigInteger();
		assertTrue("the bigInteger equivalent of -2837E10 is wrong", result
				.doubleValue() == -2837E10);
		sub1 = new BigDecimal(2.349E-10);
		result = sub1.toBigInteger();
		assertTrue("the bigInteger equivalent of 2.349E-10 is wrong", result
				.equals(BigInteger.ZERO));
		sub1 = new BigDecimal(value2, 6);
		result = sub1.toBigInteger();
		assertTrue("the bigInteger equivalent of 12334.560000 is wrong", result
				.toString().equals("12334"));
	}

	/**
	 * @tests java.math.BigDecimal#toString()
	 */
	public void test_toString() {
		BigDecimal toString1 = new BigDecimal("1234.000");
		assertTrue("the toString representation of 1234.000 is wrong",
				toString1.toString().equals("1234.000"));
		toString1 = new BigDecimal("-123.4E-5");
		assertTrue("the toString representation of -123.4E-5 is wrong: "
				+ toString1, toString1.toString().equals("-0.001234"));
		toString1 = new BigDecimal("-1.455E-20");
		assertTrue("the toString representation of -1.455E-20 is wrong",
				toString1.toString().equals("-1.455E-20"));
		toString1 = new BigDecimal(value2, 4);
		assertTrue("the toString representation of 1233456.0000 is wrong",
				toString1.toString().equals("1233456.0000"));
	}

	/**
	 * @tests java.math.BigDecimal#unscaledValue()
	 */
	public void test_unscaledValue() {
		BigDecimal unsVal = new BigDecimal("-2839485.000");
		assertTrue("the unscaledValue of -2839485.000 is wrong", unsVal
				.unscaledValue().toString().equals("-2839485000"));
		unsVal = new BigDecimal(123E10);
		assertTrue("the unscaledValue of 123E10 is wrong", unsVal
				.unscaledValue().toString().equals("1230000000000"));
		unsVal = new BigDecimal("-4.56E-13");
		assertTrue("the unscaledValue of -4.56E-13 is wrong: "
				+ unsVal.unscaledValue(), unsVal.unscaledValue().toString()
				.equals("-456"));
		unsVal = new BigDecimal(value, 3);
		assertTrue("the unscaledValue of 12345.908 is wrong", unsVal
				.unscaledValue().toString().equals("12345908"));

	}

	/**
	 * @tests java.math.BigDecimal#valueOf(long)
	 */
	public void test_valueOfJ() {
		BigDecimal valueOfL = BigDecimal.valueOf(9223372036854775806L);
		assertTrue("the bigDecimal equivalent of 9223372036854775806 is wrong",
				valueOfL.unscaledValue().toString().equals(
						"9223372036854775806")
						&& valueOfL.scale() == 0);
		assertTrue(
				"the toString representation of 9223372036854775806 is wrong",
				valueOfL.toString().equals("9223372036854775806"));
		valueOfL = BigDecimal.valueOf(0L);
		assertTrue("the bigDecimal equivalent of 0 is wrong", valueOfL
				.unscaledValue().toString().equals("0")
				&& valueOfL.scale() == 0);
	}

	/**
	 * @tests java.math.BigDecimal#valueOf(long, int)
	 */
	public void test_valueOfJI() {
		BigDecimal valueOfJI = BigDecimal.valueOf(9223372036854775806L, 5);
		assertTrue(
				"the bigDecimal equivalent of 92233720368547.75806 is wrong",
				valueOfJI.unscaledValue().toString().equals(
						"9223372036854775806")
						&& valueOfJI.scale() == 5);
		assertTrue(
				"the toString representation of 9223372036854775806 is wrong",
				valueOfJI.toString().equals("92233720368547.75806"));
		valueOfJI = BigDecimal.valueOf(1234L, 8);
		assertTrue(
				"the bigDecimal equivalent of 92233720368547.75806 is wrong",
				valueOfJI.unscaledValue().toString().equals("1234")
						&& valueOfJI.scale() == 8);
		assertTrue(
				"the toString representation of 9223372036854775806 is wrong",
				valueOfJI.toString().equals("0.00001234"));
		valueOfJI = BigDecimal.valueOf(0, 3);
		assertTrue(
				"the bigDecimal equivalent of 92233720368547.75806 is wrong",
				valueOfJI.unscaledValue().toString().equals("0")
						&& valueOfJI.scale() == 3);
		assertTrue(
				"the toString representation of 9223372036854775806 is wrong",
				valueOfJI.toString().equals("0.000"));

	}
	
	/**
	 * @tests java.math.BigDecimal#stripTrailingZero(long)
	 */
	public void test_stripTrailingZero() {
		BigDecimal sixhundredtest = new BigDecimal("600.0");
		assertTrue("stripTrailingZero failed for 600.0",
				((sixhundredtest.stripTrailingZeros()).scale() == -2)
				);
		
		/* Single digit, no trailing zero, odd number */
		BigDecimal notrailingzerotest = new BigDecimal("1");
		assertTrue("stripTrailingZero failed for 1",
				((notrailingzerotest.stripTrailingZeros()).scale() == 0)
				);
		
		/* Zero */
        //regression for HARMONY-4623, NON-BUG DIFF with RI
		BigDecimal zerotest = new BigDecimal("0.0000");
		assertTrue("stripTrailingZero failed for 0.0000",
				((zerotest.stripTrailingZeros()).scale() == 0)
				);		
	}	

	public void testMathContextConstruction() {
        String a = "-12380945E+61";
        BigDecimal aNumber = new BigDecimal(a);
        int precision = 6;
        RoundingMode rm = RoundingMode.HALF_DOWN;
        MathContext mcIntRm = new MathContext(precision, rm);
        MathContext mcStr = new MathContext("precision=6 roundingMode=HALF_DOWN");
        MathContext mcInt = new MathContext(precision);
        BigDecimal res = aNumber.abs(mcInt);
        assertEquals("MathContext Constructer with int precision failed",
                res, 
                new BigDecimal("1.23809E+68"));
        
        assertEquals("Equal MathContexts are not Equal ",
                mcIntRm,
                mcStr);
        
        assertEquals("Different MathContext are reported as Equal ",
        		mcInt.equals(mcStr),
                false);
        
        assertEquals("Equal MathContexts have different hashcodes ",
                mcIntRm.hashCode(),
                mcStr.hashCode());
       
        assertEquals("MathContext.toString() returning incorrect value",
                mcIntRm.toString(),
                "precision=6 roundingMode=HALF_DOWN");
	}

}
