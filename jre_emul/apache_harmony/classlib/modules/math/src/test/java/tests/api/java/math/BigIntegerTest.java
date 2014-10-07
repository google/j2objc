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

import java.math.BigInteger;
import java.util.Random;

public class BigIntegerTest extends junit.framework.TestCase {

	BigInteger minusTwo = new BigInteger("-2", 10);

	BigInteger minusOne = new BigInteger("-1", 10);

	BigInteger zero = new BigInteger("0", 10);

	BigInteger one = new BigInteger("1", 10);

	BigInteger two = new BigInteger("2", 10);

	BigInteger ten = new BigInteger("10", 10);

	BigInteger sixteen = new BigInteger("16", 10);

	BigInteger oneThousand = new BigInteger("1000", 10);

	BigInteger aZillion = new BigInteger(
			"100000000000000000000000000000000000000000000000000", 10);

	BigInteger twoToTheTen = new BigInteger("1024", 10);

	BigInteger twoToTheSeventy = two.pow(70);

	Random rand = new Random();

	BigInteger bi;

	BigInteger bi1;

	BigInteger bi2;

	BigInteger bi3;

	BigInteger bi11;

	BigInteger bi22;

	BigInteger bi33;

	BigInteger bi12;

	BigInteger bi23;

	BigInteger bi13;

	BigInteger largePos;

	BigInteger smallPos;

	BigInteger largeNeg;

	BigInteger smallNeg;

	BigInteger[][] booleanPairs;

	/**
	 * @tests java.math.BigInteger#BigInteger(int, java.util.Random)
	 */
	public void test_ConstructorILjava_util_Random() {
        // regression test for HARMONY-1047
		try {
			new BigInteger(Integer.MAX_VALUE, (Random)null);
			fail("NegativeArraySizeException expected");
		} catch (NegativeArraySizeException e) {
            // PASSED
		}
		
		bi = new BigInteger(70, rand);
		bi2 = new BigInteger(70, rand);
		assertTrue("Random number is negative", bi.compareTo(zero) >= 0);
		assertTrue("Random number is too big",
				bi.compareTo(twoToTheSeventy) < 0);
		assertTrue(
				"Two random numbers in a row are the same (might not be a bug but it very likely is)",
				!bi.equals(bi2));
		assertTrue("Not zero", new BigInteger(0, rand).equals(BigInteger.ZERO));
	}

	/**
	 * @tests java.math.BigInteger#BigInteger(int, int, java.util.Random)
	 */
	public void test_ConstructorIILjava_util_Random() {
		bi = new BigInteger(10, 5, rand);
		bi2 = new BigInteger(10, 5, rand);
		assertTrue("Random number one is negative", bi.compareTo(zero) >= 0);
		assertTrue("Random number one is too big",
				bi.compareTo(twoToTheTen) < 0);
		assertTrue("Random number two is negative", bi2.compareTo(zero) >= 0);
		assertTrue("Random number two is too big",
				bi2.compareTo(twoToTheTen) < 0);

		Random rand = new Random();
		BigInteger bi;
		int certainty[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
				Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -2, -1 };
		for (int i = 2; i <= 20; i++) {
            for (int c = 0; c < certainty.length; c++) {
				bi = new BigInteger(i, c, rand); // Create BigInteger
				assertTrue("Bit length incorrect", bi.bitLength() == i);
			}
        }
	}

	/**
	 * @tests java.math.BigInteger#BigInteger(byte[])
	 */
	public void test_Constructor$B() {
		byte[] myByteArray;
		myByteArray = new byte[] { (byte) 0x00, (byte) 0xFF, (byte) 0xFE };
		bi = new BigInteger(myByteArray);
		assertTrue("Incorrect value for pos number", bi.equals(BigInteger.ZERO
				.setBit(16).subtract(two)));
		myByteArray = new byte[] { (byte) 0xFF, (byte) 0xFE };
		bi = new BigInteger(myByteArray);
		assertTrue("Incorrect value for neg number", bi.equals(minusTwo));
	}

	/**
	 * @tests java.math.BigInteger#BigInteger(int, byte[])
	 */
	public void test_ConstructorI$B() {
		byte[] myByteArray;
		myByteArray = new byte[] { (byte) 0xFF, (byte) 0xFE };
		bi = new BigInteger(1, myByteArray);
		assertTrue("Incorrect value for pos number", bi.equals(BigInteger.ZERO
				.setBit(16).subtract(two)));
		bi = new BigInteger(-1, myByteArray);
		assertTrue("Incorrect value for neg number", bi.equals(BigInteger.ZERO
				.setBit(16).subtract(two).negate()));
		myByteArray = new byte[] { (byte) 0, (byte) 0 };
		bi = new BigInteger(0, myByteArray);
		assertTrue("Incorrect value for zero", bi.equals(zero));
		myByteArray = new byte[] { (byte) 1 };
		try {
			new BigInteger(0, myByteArray);
            fail("Failed to throw NumberFormatException");
		} catch (NumberFormatException e) {
			// correct
		}
	}
	
	/**
	 * @tests java.math.BigInteger#BigInteger(java.lang.String)
	 */
	public void test_constructor_String_empty() {
		try {
			new BigInteger("");			
            fail("Expected NumberFormatException for new BigInteger(\"\")");
		} catch (NumberFormatException e) {
		}
	}

	/**
	 * @tests java.math.BigInteger#toByteArray()
	 */
	public void test_toByteArray() {
		byte[] myByteArray, anotherByteArray;
		myByteArray = new byte[] { 97, 33, 120, 124, 50, 2, 0, 0, 0, 12, 124,
				42 };
		anotherByteArray = new BigInteger(myByteArray).toByteArray();
		assertTrue("Incorrect byte array returned",
				myByteArray.length == anotherByteArray.length);
		for (int counter = myByteArray.length - 1; counter >= 0; counter--) {
			assertTrue("Incorrect values in returned byte array",
					myByteArray[counter] == anotherByteArray[counter]);
		}
	}

	/**
	 * @tests java.math.BigInteger#isProbablePrime(int)
	 */
	public void test_isProbablePrimeI() {
		int fails = 0;
		bi = new BigInteger(20, 20, rand);
		if (!bi.isProbablePrime(17)) {
            fails++;
        }
		bi = new BigInteger("4", 10);
		if (bi.isProbablePrime(17)) {
            fail("isProbablePrime failed for: " + bi);
        }
		bi = BigInteger.valueOf(17L * 13L);
		if (bi.isProbablePrime(17)) {
            fail("isProbablePrime failed for: " + bi);
        }
		for (long a = 2; a < 1000; a++) {
            if (isPrime(a)) {
                assertTrue("false negative on prime number <1000", BigInteger
						.valueOf(a).isProbablePrime(5));
            } else if (BigInteger.valueOf(a).isProbablePrime(17)) {
				System.out.println("isProbablePrime failed for: " + a);
				fails++;
			}
        }
		for (int a = 0; a < 1000; a++) {
			bi = BigInteger.valueOf(rand.nextInt(1000000)).multiply(
					BigInteger.valueOf(rand.nextInt(1000000)));
			if (bi.isProbablePrime(17)) {
				System.out.println("isProbablePrime failed for: " + bi);
				fails++;
			}
		}
		for (int a = 0; a < 200; a++) {
			bi = new BigInteger(70, rand).multiply(new BigInteger(70, rand));
			if (bi.isProbablePrime(17)) {
				System.out.println("isProbablePrime failed for: " + bi);
				fails++;
			}
		}
		assertTrue("Too many false positives - may indicate a problem",
				fails <= 1);
	}

	/**
	 * @tests java.math.BigInteger#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		assertTrue("0=0", zero.equals(BigInteger.valueOf(0)));
		assertTrue("-123=-123", BigInteger.valueOf(-123).equals(
				BigInteger.valueOf(-123)));
		assertTrue("0=1", !zero.equals(one));
		assertTrue("0=-1", !zero.equals(minusOne));
		assertTrue("1=-1", !one.equals(minusOne));
		assertTrue("bi3=bi3", bi3.equals(bi3));
		assertTrue("bi3=copy of bi3", bi3.equals(bi3.negate().negate()));
		assertTrue("bi3=bi2", !bi3.equals(bi2));
	}

	/**
	 * @tests java.math.BigInteger#compareTo(java.math.BigInteger)
	 */
	public void test_compareToLjava_math_BigInteger() {
		assertTrue("Smaller number returned >= 0", one.compareTo(two) < 0);
		assertTrue("Larger number returned >= 0", two.compareTo(one) > 0);
		assertTrue("Equal numbers did not return 0", one.compareTo(one) == 0);
		assertTrue("Neg number messed things up",
				two.negate().compareTo(one) < 0);
	}

	/**
	 * @tests java.math.BigInteger#intValue()
	 */
	public void test_intValue() {
		assertTrue("Incorrect intValue for 2**70",
				twoToTheSeventy.intValue() == 0);
		assertTrue("Incorrect intValue for 2", two.intValue() == 2);
	}

	/**
	 * @tests java.math.BigInteger#longValue()
	 */
	public void test_longValue() {
		assertTrue("Incorrect longValue for 2**70",
				twoToTheSeventy.longValue() == 0);
		assertTrue("Incorrect longValue for 2", two.longValue() == 2);
	}

	/**
	 * @tests java.math.BigInteger#valueOf(long)
	 */
	public void test_valueOfJ() {
		assertTrue("Incurred number returned for 2", BigInteger.valueOf(2L)
				.equals(two));
		assertTrue("Incurred number returned for 200", BigInteger.valueOf(200L)
				.equals(BigInteger.valueOf(139).add(BigInteger.valueOf(61))));
	}

	/**
	 * @tests java.math.BigInteger#add(java.math.BigInteger)
	 */
	public void test_addLjava_math_BigInteger() {
		assertTrue("Incorrect sum--wanted a zillion", aZillion.add(aZillion)
				.add(aZillion.negate()).equals(aZillion));
		assertTrue("0+0", zero.add(zero).equals(zero));
		assertTrue("0+1", zero.add(one).equals(one));
		assertTrue("1+0", one.add(zero).equals(one));
		assertTrue("1+1", one.add(one).equals(two));
		assertTrue("0+(-1)", zero.add(minusOne).equals(minusOne));
		assertTrue("(-1)+0", minusOne.add(zero).equals(minusOne));
		assertTrue("(-1)+(-1)", minusOne.add(minusOne).equals(minusTwo));
		assertTrue("1+(-1)", one.add(minusOne).equals(zero));
		assertTrue("(-1)+1", minusOne.add(one).equals(zero));

		for (int i = 0; i < 200; i++) {
			BigInteger midbit = zero.setBit(i);
			assertTrue("add fails to carry on bit " + i, midbit.add(midbit)
					.equals(zero.setBit(i + 1)));
		}
		BigInteger bi2p3 = bi2.add(bi3);
		BigInteger bi3p2 = bi3.add(bi2);
		assertTrue("bi2p3=bi3p2", bi2p3.equals(bi3p2));

		// add large positive + small positive

		// add large positive + small negative

		// add large negative + small positive

		// add large negative + small negative
	}

	/**
	 * @tests java.math.BigInteger#negate()
	 */
	public void test_negate() {
		assertTrue("Single negation of zero did not result in zero", zero
				.negate().equals(zero));
		assertTrue("Single negation resulted in original nonzero number",
				!aZillion.negate().equals(aZillion));
		assertTrue("Double negation did not result in original number",
				aZillion.negate().negate().equals(aZillion));

		assertTrue("0.neg", zero.negate().equals(zero));
		assertTrue("1.neg", one.negate().equals(minusOne));
		assertTrue("2.neg", two.negate().equals(minusTwo));
		assertTrue("-1.neg", minusOne.negate().equals(one));
		assertTrue("-2.neg", minusTwo.negate().equals(two));
		assertTrue("0x62EB40FEF85AA9EBL*2.neg", BigInteger.valueOf(
				0x62EB40FEF85AA9EBL * 2).negate().equals(
				BigInteger.valueOf(-0x62EB40FEF85AA9EBL * 2)));
		for (int i = 0; i < 200; i++) {
			BigInteger midbit = zero.setBit(i);
			BigInteger negate = midbit.negate();
			assertTrue("negate negate", negate.negate().equals(midbit));
			assertTrue("neg fails on bit " + i, midbit.negate().add(midbit)
					.equals(zero));
		}
	}

	/**
	 * @tests java.math.BigInteger#signum()
	 */
	public void test_signum() {
		assertTrue("Wrong positive signum", two.signum() == 1);
		assertTrue("Wrong zero signum", zero.signum() == 0);
		assertTrue("Wrong neg zero signum", zero.negate().signum() == 0);
		assertTrue("Wrong neg signum", two.negate().signum() == -1);
	}

	/**
	 * @tests java.math.BigInteger#abs()
	 */
	public void test_abs() {
		assertTrue("Invalid number returned for zillion", aZillion.negate()
				.abs().equals(aZillion.abs()));
		assertTrue("Invalid number returned for zero neg", zero.negate().abs()
				.equals(zero));
		assertTrue("Invalid number returned for zero", zero.abs().equals(zero));
		assertTrue("Invalid number returned for two", two.negate().abs()
				.equals(two));
	}

	/**
	 * @tests java.math.BigInteger#pow(int)
	 */
	public void test_powI() {
		assertTrue("Incorrect exponent returned for 2**10", two.pow(10).equals(
				twoToTheTen));
		assertTrue("Incorrect exponent returned for 2**70", two.pow(30)
				.multiply(two.pow(40)).equals(twoToTheSeventy));
		assertTrue("Incorrect exponent returned for 10**50", ten.pow(50)
				.equals(aZillion));
	}

	/**
	 * @tests java.math.BigInteger#modInverse(java.math.BigInteger)
	 */
	public void test_modInverseLjava_math_BigInteger() {
		BigInteger a = zero, mod, inv;
		for (int j = 3; j < 50; j++) {
			mod = BigInteger.valueOf(j);
			for (int i = -j + 1; i < j; i++) {
                try {
					a = BigInteger.valueOf(i);
					inv = a.modInverse(mod);
					assertTrue("bad inverse: " + a + " inv mod " + mod
							+ " equals " + inv, one.equals(a.multiply(inv).mod(
							mod)));
					assertTrue("inverse greater than modulo: " + a
							+ " inv mod " + mod + " equals " + inv, inv
							.compareTo(mod) < 0);
					assertTrue("inverse less than zero: " + a + " inv mod "
							+ mod + " equals " + inv, inv
							.compareTo(BigInteger.ZERO) >= 0);
				} catch (ArithmeticException e) {
					assertTrue("should have found inverse for " + a + " mod "
							+ mod, !one.equals(a.gcd(mod)));
				}
            }
		}
		for (int j = 1; j < 10; j++) {
			mod = bi2.add(BigInteger.valueOf(j));
			for (int i = 0; i < 20; i++) {
                try {
					a = bi3.add(BigInteger.valueOf(i));
					inv = a.modInverse(mod);
					assertTrue("bad inverse: " + a + " inv mod " + mod
							+ " equals " + inv, one.equals(a.multiply(inv).mod(
							mod)));
					assertTrue("inverse greater than modulo: " + a
							+ " inv mod " + mod + " equals " + inv, inv
							.compareTo(mod) < 0);
					assertTrue("inverse less than zero: " + a + " inv mod "
							+ mod + " equals " + inv, inv
							.compareTo(BigInteger.ZERO) >= 0);
				} catch (ArithmeticException e) {
					assertTrue("should have found inverse for " + a + " mod "
							+ mod, !one.equals(a.gcd(mod)));
				}
            }
		}
	}

	/**
	 * @tests java.math.BigInteger#shiftRight(int)
	 */
	public void test_shiftRightI() {
		assertTrue("1 >> 0", BigInteger.valueOf(1).shiftRight(0).equals(
				BigInteger.ONE));
		assertTrue("1 >> 1", BigInteger.valueOf(1).shiftRight(1).equals(
				BigInteger.ZERO));
		assertTrue("1 >> 63", BigInteger.valueOf(1).shiftRight(63).equals(
				BigInteger.ZERO));
		assertTrue("1 >> 64", BigInteger.valueOf(1).shiftRight(64).equals(
				BigInteger.ZERO));
		assertTrue("1 >> 65", BigInteger.valueOf(1).shiftRight(65).equals(
				BigInteger.ZERO));
		assertTrue("1 >> 1000", BigInteger.valueOf(1).shiftRight(1000).equals(
				BigInteger.ZERO));
		assertTrue("-1 >> 0", BigInteger.valueOf(-1).shiftRight(0).equals(
				minusOne));
		assertTrue("-1 >> 1", BigInteger.valueOf(-1).shiftRight(1).equals(
				minusOne));
		assertTrue("-1 >> 63", BigInteger.valueOf(-1).shiftRight(63).equals(
				minusOne));
		assertTrue("-1 >> 64", BigInteger.valueOf(-1).shiftRight(64).equals(
				minusOne));
		assertTrue("-1 >> 65", BigInteger.valueOf(-1).shiftRight(65).equals(
				minusOne));
		assertTrue("-1 >> 1000", BigInteger.valueOf(-1).shiftRight(1000)
				.equals(minusOne));

		BigInteger a = BigInteger.ONE;
		BigInteger c = bi3;
		BigInteger E = bi3.negate();
		BigInteger e = E;
		for (int i = 0; i < 200; i++) {
			BigInteger b = BigInteger.ZERO.setBit(i);
			assertTrue("a==b", a.equals(b));
			a = a.shiftLeft(1);
			assertTrue("a non-neg", a.signum() >= 0);

			BigInteger d = bi3.shiftRight(i);
			assertTrue("c==d", c.equals(d));
			c = c.shiftRight(1);
			assertTrue(">>1 == /2", d.divide(two).equals(c));
			assertTrue("c non-neg", c.signum() >= 0);

			BigInteger f = E.shiftRight(i);
			assertTrue("e==f", e.equals(f));
			e = e.shiftRight(1);
			assertTrue(">>1 == /2", f.subtract(one).divide(two).equals(e));
			assertTrue("e negative", e.signum() == -1);

			assertTrue("b >> i", b.shiftRight(i).equals(one));
			assertTrue("b >> i+1", b.shiftRight(i + 1).equals(zero));
			assertTrue("b >> i-1", b.shiftRight(i - 1).equals(two));
		}
	}

	/**
	 * @tests java.math.BigInteger#shiftLeft(int)
	 */
	public void test_shiftLeftI() {
		assertTrue("1 << 0", one.shiftLeft(0).equals(one));
		assertTrue("1 << 1", one.shiftLeft(1).equals(two));
		assertTrue("1 << 63", one.shiftLeft(63).equals(
				new BigInteger("8000000000000000", 16)));
		assertTrue("1 << 64", one.shiftLeft(64).equals(
				new BigInteger("10000000000000000", 16)));
		assertTrue("1 << 65", one.shiftLeft(65).equals(
				new BigInteger("20000000000000000", 16)));
		assertTrue("-1 << 0", minusOne.shiftLeft(0).equals(minusOne));
		assertTrue("-1 << 1", minusOne.shiftLeft(1).equals(minusTwo));
		assertTrue("-1 << 63", minusOne.shiftLeft(63).equals(
				new BigInteger("-9223372036854775808")));
		assertTrue("-1 << 64", minusOne.shiftLeft(64).equals(
				new BigInteger("-18446744073709551616")));
		assertTrue("-1 << 65", minusOne.shiftLeft(65).equals(
				new BigInteger("-36893488147419103232")));

		BigInteger a = bi3;
		BigInteger c = minusOne;
		for (int i = 0; i < 200; i++) {
			BigInteger b = bi3.shiftLeft(i);
			assertTrue("a==b", a.equals(b));
			assertTrue("a >> i == bi3", a.shiftRight(i).equals(bi3));
			a = a.shiftLeft(1);
			assertTrue("<<1 == *2", b.multiply(two).equals(a));
			assertTrue("a non-neg", a.signum() >= 0);
			assertTrue("a.bitCount==b.bitCount", a.bitCount() == b.bitCount());

			BigInteger d = minusOne.shiftLeft(i);
			assertTrue("c==d", c.equals(d));
			c = c.shiftLeft(1);
			assertTrue("<<1 == *2 negative", d.multiply(two).equals(c));
			assertTrue("c negative", c.signum() == -1);
			assertTrue("d >> i == minusOne", d.shiftRight(i).equals(minusOne));
		}
	}

	/**
	 * @tests java.math.BigInteger#multiply(java.math.BigInteger)
	 */
	public void test_multiplyLjava_math_BigInteger() {
		assertTrue("Incorrect sum--wanted three zillion", aZillion
				.add(aZillion).add(aZillion).equals(
						aZillion.multiply(new BigInteger("3", 10))));

		assertTrue("0*0", zero.multiply(zero).equals(zero));
		assertTrue("0*1", zero.multiply(one).equals(zero));
		assertTrue("1*0", one.multiply(zero).equals(zero));
		assertTrue("1*1", one.multiply(one).equals(one));
		assertTrue("0*(-1)", zero.multiply(minusOne).equals(zero));
		assertTrue("(-1)*0", minusOne.multiply(zero).equals(zero));
		assertTrue("(-1)*(-1)", minusOne.multiply(minusOne).equals(one));
		assertTrue("1*(-1)", one.multiply(minusOne).equals(minusOne));
		assertTrue("(-1)*1", minusOne.multiply(one).equals(minusOne));

		testAllMults(bi1, bi1, bi11);
		testAllMults(bi2, bi2, bi22);
		testAllMults(bi3, bi3, bi33);
		testAllMults(bi1, bi2, bi12);
		testAllMults(bi1, bi3, bi13);
		testAllMults(bi2, bi3, bi23);
	}

	/**
	 * @tests java.math.BigInteger#divide(java.math.BigInteger)
	 */
	public void test_divideLjava_math_BigInteger() {
		testAllDivs(bi33, bi3);
		testAllDivs(bi22, bi2);
		testAllDivs(bi11, bi1);
		testAllDivs(bi13, bi1);
		testAllDivs(bi13, bi3);
		testAllDivs(bi12, bi1);
		testAllDivs(bi12, bi2);
		testAllDivs(bi23, bi2);
		testAllDivs(bi23, bi3);
		testAllDivs(largePos, bi1);
		testAllDivs(largePos, bi2);
		testAllDivs(largePos, bi3);
		testAllDivs(largeNeg, bi1);
		testAllDivs(largeNeg, bi2);
		testAllDivs(largeNeg, bi3);
		testAllDivs(largeNeg, largePos);
		testAllDivs(largePos, largeNeg);
		testAllDivs(bi3, bi3);
		testAllDivs(bi2, bi2);
		testAllDivs(bi1, bi1);
		testDivRanges(bi1);
		testDivRanges(bi2);
		testDivRanges(bi3);
		testDivRanges(smallPos);
		testDivRanges(largePos);
		testDivRanges(new BigInteger("62EB40FEF85AA9EB", 16));
		testAllDivs(BigInteger.valueOf(0xCC0225953CL), BigInteger
				.valueOf(0x1B937B765L));

		try {
			largePos.divide(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			bi1.divide(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			bi3.negate().divide(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			zero.divide(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}
	}

	/**
	 * @tests java.math.BigInteger#remainder(java.math.BigInteger)
	 */
	public void test_remainderLjava_math_BigInteger() {
		try {
			largePos.remainder(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			bi1.remainder(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			bi3.negate().remainder(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			zero.remainder(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}
	}

	/**
	 * @tests java.math.BigInteger#mod(java.math.BigInteger)
	 */
	public void test_modLjava_math_BigInteger() {
		try {
			largePos.mod(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			bi1.mod(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			bi3.negate().mod(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			zero.mod(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}
	}

	/**
	 * @tests java.math.BigInteger#divideAndRemainder(java.math.BigInteger)
	 */
	public void test_divideAndRemainderLjava_math_BigInteger() {
		try {
			largePos.divideAndRemainder(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			bi1.divideAndRemainder(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			bi3.negate().divideAndRemainder(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}

		try {
			zero.divideAndRemainder(zero);
            fail("ArithmeticException expected");
		} catch (ArithmeticException e) {
		}
	}

	/**
	 * @tests java.math.BigInteger#BigInteger(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		assertTrue("new(0)", new BigInteger("0").equals(BigInteger.valueOf(0)));
		assertTrue("new(1)", new BigInteger("1").equals(BigInteger.valueOf(1)));
		assertTrue("new(12345678901234)", new BigInteger("12345678901234")
				.equals(BigInteger.valueOf(12345678901234L)));
		assertTrue("new(-1)", new BigInteger("-1").equals(BigInteger
				.valueOf(-1)));
		assertTrue("new(-12345678901234)", new BigInteger("-12345678901234")
				.equals(BigInteger.valueOf(-12345678901234L)));
	}

	/**
	 * @tests java.math.BigInteger#BigInteger(java.lang.String, int)
	 */
	public void test_ConstructorLjava_lang_StringI() {
		assertTrue("new(0,16)", new BigInteger("0", 16).equals(BigInteger
				.valueOf(0)));
		assertTrue("new(1,16)", new BigInteger("1", 16).equals(BigInteger
				.valueOf(1)));
		assertTrue("new(ABF345678901234,16)", new BigInteger("ABF345678901234",
				16).equals(BigInteger.valueOf(0xABF345678901234L)));
		assertTrue("new(abf345678901234,16)", new BigInteger("abf345678901234",
				16).equals(BigInteger.valueOf(0xABF345678901234L)));
		assertTrue("new(-1,16)", new BigInteger("-1", 16).equals(BigInteger
				.valueOf(-1)));
		assertTrue("new(-ABF345678901234,16)", new BigInteger(
				"-ABF345678901234", 16).equals(BigInteger
				.valueOf(-0xABF345678901234L)));
		assertTrue("new(-abf345678901234,16)", new BigInteger(
				"-abf345678901234", 16).equals(BigInteger
				.valueOf(-0xABF345678901234L)));
		assertTrue("new(-101010101,2)", new BigInteger("-101010101", 2)
				.equals(BigInteger.valueOf(-341)));
	}

	/**
	 * @tests java.math.BigInteger#toString()
	 */
	public void test_toString() {
		assertTrue("0.toString", "0".equals(BigInteger.valueOf(0).toString()));
		assertTrue("1.toString", "1".equals(BigInteger.valueOf(1).toString()));
		assertTrue("12345678901234.toString", "12345678901234"
				.equals(BigInteger.valueOf(12345678901234L).toString()));
		assertTrue("-1.toString", "-1"
				.equals(BigInteger.valueOf(-1).toString()));
		assertTrue("-12345678901234.toString", "-12345678901234"
				.equals(BigInteger.valueOf(-12345678901234L).toString()));
	}

	/**
	 * @tests java.math.BigInteger#toString(int)
	 */
	public void test_toStringI() {
		assertTrue("0.toString(16)", "0".equals(BigInteger.valueOf(0).toString(
				16)));
		assertTrue("1.toString(16)", "1".equals(BigInteger.valueOf(1).toString(
				16)));
		assertTrue("ABF345678901234.toString(16)", "abf345678901234"
				.equals(BigInteger.valueOf(0xABF345678901234L).toString(16)));
		assertTrue("-1.toString(16)", "-1".equals(BigInteger.valueOf(-1)
				.toString(16)));
		assertTrue("-ABF345678901234.toString(16)", "-abf345678901234"
				.equals(BigInteger.valueOf(-0xABF345678901234L).toString(16)));
		assertTrue("-101010101.toString(2)", "-101010101".equals(BigInteger
				.valueOf(-341).toString(2)));
	}

	/**
	 * @tests java.math.BigInteger#and(java.math.BigInteger)
	 */
	public void test_andLjava_math_BigInteger() {
		for (BigInteger[] element : booleanPairs) {
			BigInteger i1 = element[0], i2 = element[1];
			BigInteger res = i1.and(i2);
			assertTrue("symmetry of and", res.equals(i2.and(i1)));
			int len = Math.max(i1.bitLength(), i2.bitLength()) + 66;
			for (int i = 0; i < len; i++) {
                assertTrue("and", (i1.testBit(i) && i2.testBit(i)) == res
						.testBit(i));
            }
		}
	}

	/**
	 * @tests java.math.BigInteger#or(java.math.BigInteger)
	 */
	public void test_orLjava_math_BigInteger() {
		for (BigInteger[] element : booleanPairs) {
			BigInteger i1 = element[0], i2 = element[1];
			BigInteger res = i1.or(i2);
			assertTrue("symmetry of or", res.equals(i2.or(i1)));
			int len = Math.max(i1.bitLength(), i2.bitLength()) + 66;
			for (int i = 0; i < len; i++) {
                assertTrue("or", (i1.testBit(i) || i2.testBit(i)) == res
						.testBit(i));
            }
		}
	}

	/**
	 * @tests java.math.BigInteger#xor(java.math.BigInteger)
	 */
	public void test_xorLjava_math_BigInteger() {
		for (BigInteger[] element : booleanPairs) {
			BigInteger i1 = element[0], i2 = element[1];
			BigInteger res = i1.xor(i2);
			assertTrue("symmetry of xor", res.equals(i2.xor(i1)));
			int len = Math.max(i1.bitLength(), i2.bitLength()) + 66;
			for (int i = 0; i < len; i++) {
                assertTrue("xor", (i1.testBit(i) ^ i2.testBit(i)) == res
						.testBit(i));
            }
		}
	}

	/**
	 * @tests java.math.BigInteger#not()
	 */
	public void test_not() {
		for (BigInteger[] element : booleanPairs) {
			BigInteger i1 = element[0];
			BigInteger res = i1.not();
			int len = i1.bitLength() + 66;
			for (int i = 0; i < len; i++) {
                assertTrue("not", !i1.testBit(i) == res.testBit(i));
            }
		}
	}

	/**
	 * @tests java.math.BigInteger#andNot(java.math.BigInteger)
	 */
	public void test_andNotLjava_math_BigInteger() {
		for (BigInteger[] element : booleanPairs) {
			BigInteger i1 = element[0], i2 = element[1];
			BigInteger res = i1.andNot(i2);
			int len = Math.max(i1.bitLength(), i2.bitLength()) + 66;
			for (int i = 0; i < len; i++) {
                assertTrue("andNot", (i1.testBit(i) && !i2.testBit(i)) == res
						.testBit(i));
            }
			// asymmetrical
			i1 = element[1];
			i2 = element[0];
			res = i1.andNot(i2);
			for (int i = 0; i < len; i++) {
                assertTrue("andNot reversed",
						(i1.testBit(i) && !i2.testBit(i)) == res.testBit(i));
            }
		}
        //regression for HARMONY-4653
        try{
            BigInteger.ZERO.andNot(null);
            fail("should throw NPE");
        }catch(Exception e){
            //expected
        }
        BigInteger bi = new BigInteger(0, new byte[]{});
        assertEquals(BigInteger.ZERO, bi.andNot(BigInteger.ZERO));
	}
    

     public void testClone() {
        // Regression test for HARMONY-1770
        MyBigInteger myBigInteger = new MyBigInteger("12345");
        myBigInteger = (MyBigInteger) myBigInteger.clone();
    }

    static class MyBigInteger extends BigInteger implements Cloneable {
        public MyBigInteger(String val) {
            super(val);
        }
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }

	@Override
    protected void setUp() {
		bi1 = new BigInteger("2436798324768978", 16);
		bi2 = new BigInteger("4576829475724387584378543764555", 16);
		bi3 = new BigInteger("43987298363278574365732645872643587624387563245",
				16);

		bi33 = new BigInteger(
				"10730846694701319120609898625733976090865327544790136667944805934175543888691400559249041094474885347922769807001",
				10);
		bi22 = new BigInteger(
				"33301606932171509517158059487795669025817912852219962782230629632224456249",
				10);
		bi11 = new BigInteger("6809003003832961306048761258711296064", 10);
		bi23 = new BigInteger(
				"597791300268191573513888045771594235932809890963138840086083595706565695943160293610527214057",
				10);
		bi13 = new BigInteger(
				"270307912162948508387666703213038600031041043966215279482940731158968434008",
				10);
		bi12 = new BigInteger(
				"15058244971895641717453176477697767050482947161656458456", 10);

		largePos = new BigInteger(
				"834759814379857314986743298675687569845986736578576375675678998612743867438632986243982098437620983476924376",
				16);
		smallPos = new BigInteger("48753269875973284765874598630960986276", 16);
		largeNeg = new BigInteger(
				"-878824397432651481891353247987891423768534321387864361143548364457698487264387568743568743265873246576467643756437657436587436",
				16);
		smallNeg = new BigInteger("-567863254343798609857456273458769843", 16);
		booleanPairs = new BigInteger[][] { { largePos, smallPos },
				{ largePos, smallNeg }, { largeNeg, smallPos },
				{ largeNeg, smallNeg } };
	}

	private void testDiv(BigInteger i1, BigInteger i2) {
		BigInteger q = i1.divide(i2);
		BigInteger r = i1.remainder(i2);
		BigInteger[] temp = i1.divideAndRemainder(i2);

		assertTrue("divide and divideAndRemainder do not agree", q
				.equals(temp[0]));
		assertTrue("remainder and divideAndRemainder do not agree", r
				.equals(temp[1]));
		assertTrue("signum and equals(zero) do not agree on quotient", q
				.signum() != 0
				|| q.equals(zero));
		assertTrue("signum and equals(zero) do not agree on remainder", r
				.signum() != 0
				|| r.equals(zero));
		assertTrue("wrong sign on quotient", q.signum() == 0
				|| q.signum() == i1.signum() * i2.signum());
		assertTrue("wrong sign on remainder", r.signum() == 0
				|| r.signum() == i1.signum());
		assertTrue("remainder out of range", r.abs().compareTo(i2.abs()) < 0);
		assertTrue("quotient too small", q.abs().add(one).multiply(i2.abs())
				.compareTo(i1.abs()) > 0);
		assertTrue("quotient too large", q.abs().multiply(i2.abs()).compareTo(
				i1.abs()) <= 0);
		BigInteger p = q.multiply(i2);
		BigInteger a = p.add(r);
		assertTrue("(a/b)*b+(a%b) != a", a.equals(i1));
		try {
			BigInteger mod = i1.mod(i2);
			assertTrue("mod is negative", mod.signum() >= 0);
			assertTrue("mod out of range", mod.abs().compareTo(i2.abs()) < 0);
			assertTrue("positive remainder == mod", r.signum() < 0
					|| r.equals(mod));
			assertTrue("negative remainder == mod - divisor", r.signum() >= 0
					|| r.equals(mod.subtract(i2)));
		} catch (ArithmeticException e) {
			assertTrue("mod fails on negative divisor only", i2.signum() <= 0);
		}
	}

	private void testDivRanges(BigInteger i) {
		BigInteger bound = i.multiply(two);
		for (BigInteger j = bound.negate(); j.compareTo(bound) <= 0; j = j
				.add(i)) {
			BigInteger innerbound = j.add(two);
			BigInteger k = j.subtract(two);
			for (; k.compareTo(innerbound) <= 0; k = k.add(one)) {
                testDiv(k, i);
            }
		}
	}

	private boolean isPrime(long b) {
		if (b == 2) {
            return true;
        }
		// check for div by 2
		if ((b & 1L) == 0) {
            return false;
        }
		long maxlen = ((long) Math.sqrt(b)) + 2;
		for (long x = 3; x < maxlen; x += 2) {
            if (b % x == 0) {
                return false;
            }
        }
		return true;
	}

	private void testAllMults(BigInteger i1, BigInteger i2, BigInteger ans) {
		assertTrue("i1*i2=ans", i1.multiply(i2).equals(ans));
		assertTrue("i2*i1=ans", i2.multiply(i1).equals(ans));
		assertTrue("-i1*i2=-ans", i1.negate().multiply(i2).equals(ans.negate()));
		assertTrue("-i2*i1=-ans", i2.negate().multiply(i1).equals(ans.negate()));
		assertTrue("i1*-i2=-ans", i1.multiply(i2.negate()).equals(ans.negate()));
		assertTrue("i2*-i1=-ans", i2.multiply(i1.negate()).equals(ans.negate()));
		assertTrue("-i1*-i2=ans", i1.negate().multiply(i2.negate()).equals(ans));
		assertTrue("-i2*-i1=ans", i2.negate().multiply(i1.negate()).equals(ans));
	}

	private void testAllDivs(BigInteger i1, BigInteger i2) {
		testDiv(i1, i2);
		testDiv(i1.negate(), i2);
		testDiv(i1, i2.negate());
		testDiv(i1.negate(), i2.negate());
	}
}
