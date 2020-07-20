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

package org.apache.harmony.tests.java.math;

import java.math.BigInteger;
import java.util.Random;

public class OldBigIntegerTest extends junit.framework.TestCase {

    BigInteger minusOne = new BigInteger("-1", 10);

    BigInteger two = new BigInteger("2", 10);

    BigInteger aZillion = new BigInteger("100000000000000000000000000000000000000000000000000", 10);

    Random rand = new Random();

    BigInteger bi;

    BigInteger bi2;

    BigInteger bi3;

    /**
     * java.math.BigInteger#BigInteger(int, java.util.Random)
     */
    public void test_ConstructorILjava_util_Random() {
        // regression test for HARMONY-1047
        try {
            new BigInteger(128, (Random) null);
            fail();
        } catch (NullPointerException expected) {
        }

        bi = new BigInteger(70, rand);
        bi2 = new BigInteger(70, rand);
        assertTrue("Random number is negative", bi.compareTo(BigInteger.ZERO) >= 0);
        assertTrue("Random number is too big", bi.compareTo(two.pow(70)) < 0);
        assertTrue(
                "Two random numbers in a row are the same (might not be a bug but it very likely is)",
                !bi.equals(bi2));
        assertTrue("Not zero", new BigInteger(0, rand).equals(BigInteger.ZERO));

        try {
            new BigInteger(-1, (Random)null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // PASSED
        }
    }

    /**
     * java.math.BigInteger#BigInteger(int, int, java.util.Random)
     */
    // BIGNUM returns no Primes smaller than 16 bits.
    public void test_ConstructorIILjava_util_Random() {
        BigInteger bi1 = new BigInteger(10, 5, rand);
        BigInteger bi2 = new BigInteger(10, 5, rand);
        assertTrue(bi1 + " is negative", bi1.compareTo(BigInteger.ZERO) >= 0);
        assertTrue(bi1 + " is too big", bi1.compareTo(new BigInteger("1024", 10)) < 0);
        assertTrue(bi2 + " is negative", bi2.compareTo(BigInteger.ZERO) >= 0);
        assertTrue(bi2 + " is too big", bi2.compareTo(new BigInteger("1024", 10)) < 0);

        Random rand = new Random();
        BigInteger bi;
        int certainty[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -2, -1 };
        for (int i = 2; i <= 20; i++) {
            for (int c = 0; c < certainty.length; c++) {
                bi = new BigInteger(i, c, rand); // Create BigInteger
                assertEquals(i, bi.bitLength());
            }
        }

        try {
            new BigInteger(1, 80, (Random)null);
            fail("ArithmeticException expected");
        } catch (ArithmeticException expected) {
        }

        try {
            new BigInteger(-1, (Random)null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

//    public void test_SpecialPrimes() {
//        System.out.println("test_SpecialPrimes");
//        final BigInteger TWO = BigInteger.valueOf(2);
//        BigInteger p, q;
//        for (;;) {
//            p = new BigInteger(1024, 23, new Random());
//            q = p.subtract(BigInteger.ONE).divide(TWO);
//            if (q.isProbablePrime(20)) {
//                System.out.println(q);
//                System.out.println(p);
//                break;
//            }
//            System.out.print(".");
//        }
//        fail("isProbablePrime failed for: " + bi);
//    }

    /**
     * java.math.BigInteger#isProbablePrime(int)
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

        //
        // And now some tests on real big integers:
        //
        bi = new BigInteger("153890972191202256150310830154922163807316525358455215516067727076235016932726922093888770552128767458882963869421440585369743", 10);
        if (!bi.isProbablePrime(80)) {
            fail("isProbablePrime failed for: " + bi);
        }
        bi = new BigInteger("2090575416269141767246491983797422123741252476560371649798066134123893524014911825188890458270426076468664046568752890122415061377308817346303546688282957897504000216241497550243010257911214329646877810655164658470278901030511157372440751259674247310396158238588463284702737181653", 10);
        if (!bi.isProbablePrime(80)) {
            fail("isProbablePrime failed for: " + bi);
        }
        //
        for (int bitLength = 100; bitLength <= 600; bitLength += 100) {
            BigInteger a = BigInteger.probablePrime(bitLength, rand);
            BigInteger b = BigInteger.probablePrime(bitLength, rand);
            BigInteger c = a.multiply(b);
            assertFalse("isProbablePrime failed for product of two large primes" +
                            a + " * " + b + " = " + c +
                            " (bitLength = " + bitLength + ")",
                    c.isProbablePrime(80) );
        }
    }

    /**
     * java.math.BigInteger#nextProbablePrime()
     */
    public void test_nextProbablePrime() {
        largePrimesProduct(
                new BigInteger("2537895984043447429238717358455377929009126353874925049325287329295635198252046158619999217453233889378619619008359011789"),
                new BigInteger("1711501451602688337873833423534849678524059393231999670806585630179374689152366029939952735718718709436427337762082614710093"),
                "4343612660706993434504106787562106084038357258130862545477481433639575850237346784798851102536616749334772541987502120552264920040629526028540204698334741815536099373917351194423681128374184971846099257056996626343051832131340568120612204287123"
        );

        largePrimesProduct(
                new BigInteger("4617974730611208463200675282934641082129817404749925308887287017217158545765190433369842932770197341032031682222405074564586462802072184047198214312142847809259437477387527466762251087500170588962277514858557309036550499896961735701485020851"),
                new BigInteger("4313158964405728158057980867015758419530142215799386331265837224051830838583266274443105715022196238165196727467066901495701708590167750818040112544031694506528759169669442493029999154074962566165293254671176670719518898684698255068313216294333"),
                "19918059106734861363335842730108905466210762564765297409619920041621379008685530738918145604092111306972524565803236031571858280032420140331838737621152630780261815015157696362550138161774466814661069892975003440654998880587960037013294137372709096788892473385003457361736563927256562678181177287998121131179907762285048659075843995525830945659905573174849006768920618442371027575308854641789533211132313916836205357976988977849024687805212304038260207820679964201211309384057458137851"
        );
    }

    static void largePrimesProduct(BigInteger a, BigInteger b, String c) {
        BigInteger wp = a.multiply(b);
        assertFalse("isProbablePrime failed for product of two large primes" +
                        a + " * " + b + " = " + c,
                wp.isProbablePrime(80) );
        BigInteger wpMinusOne = wp.subtract(BigInteger.ONE);
        BigInteger next = wpMinusOne.nextProbablePrime();
//        System.out.println(c);
//        System.out.println(next);
        assertTrue("nextProbablePrime returns wrong number: " + next +
                        "instead of expected: " + c,
                next.toString().equals(c) );
    }

    /**
     * java.math.BigInteger#probablePrime(int, java.util.Random)
     */
    public void test_probablePrime() {
        for (int bitLength = 50; bitLength <= 1050; bitLength += 100) {
            BigInteger a = BigInteger.probablePrime(bitLength, rand);
            assertTrue("isProbablePrime(probablePrime()) failed for: " + bi,
                    a.isProbablePrime(80));
//            System.out.println(a);
//            BigInteger prime = a.nextProbablePrime();
//            System.out.print("Next Probable Prime is ");
//            System.out.println(prime);
        }
    }

// BEGIN Android-added
//    public void testModPowPerformance() {
//        Random rnd = new Random();
//        for (int i = 0; i < 10; i++) {
//            BigInteger a = new BigInteger(512, rnd);
//            BigInteger m = new BigInteger(1024, rnd);
//            BigInteger p = new BigInteger(256, rnd);
//            BigInteger mp = a.modPow(p, m);
//            System.out.println(mp);
//        }
//    }

// shows factor 20 speed up (BIGNUM to Harmony Java):
//    public void testNextProbablePrime() {
//        Random rnd = new Random();
//        rnd.setSeed(0);
//        for (int i = 1; i <= 32; i += 1) {
//            BigInteger a = new BigInteger(i, rnd);
//            System.out.println(a);
//            BigInteger prime = a.nextProbablePrime();
//            System.out.print("Next Probable Prime is ");
//            System.out.println(prime);
//        }
//        for (int i = 1; i <= 32; i += 4) {
//            BigInteger a = new BigInteger(32 * i, rnd);
//            System.out.println(a);
//            BigInteger prime = a.nextProbablePrime();
//            System.out.print("Next Probable Prime is ");
//            System.out.println(prime);
//        }
//    }

// shows factor 20 speed up (BIGNUM to Harmony Java):
// shows that certainty 80 is "practically aquivalent" to certainty 100
//    public void testPrimeGenPerformance() {
//        Random rnd = new Random();
//        rnd.setSeed(0);
//        for (int i = 1; i <= 32; i +=8 ) {
//            BigInteger a = new BigInteger(32 * i, 80, rnd);
//            System.out.println(a);
//            System.out.println("Now testing it again:");
//            if (a.isProbablePrime(100)) {
//                System.out.println("************************ PASSED! **************************");
//            } else {
//                System.out.println("************************ FAILED!!! **************************");
//                System.out.println("************************ FAILED!!! **************************");
//                System.out.println("************************ FAILED!!! **************************");
//                System.out.println("************************ FAILED!!! **************************");
//                System.out.println("************************ FAILED!!! **************************");
//                System.out.println("************************ FAILED!!! **************************");
//            }
//        }
//    }
// END Android-added



    /**
     * java.math.BigInteger#add(java.math.BigInteger)
     */
    public void test_addLjava_math_BigInteger() {
        assertTrue("Incorrect sum--wanted a zillion", aZillion.add(aZillion)
                .add(aZillion.negate()).equals(aZillion));
        assertTrue("0+0", BigInteger.ZERO.add(BigInteger.ZERO).equals(BigInteger.ZERO));
        assertTrue("0+1", BigInteger.ZERO.add(BigInteger.ONE).equals(BigInteger.ONE));
        assertTrue("1+0", BigInteger.ONE.add(BigInteger.ZERO).equals(BigInteger.ONE));
        assertTrue("1+1", BigInteger.ONE.add(BigInteger.ONE).equals(two));
        assertTrue("0+(-1)", BigInteger.ZERO.add(minusOne).equals(minusOne));
        assertTrue("(-1)+0", minusOne.add(BigInteger.ZERO).equals(minusOne));
        assertTrue("(-1)+(-1)", minusOne.add(minusOne).equals(new BigInteger("-2", 10)));
        assertTrue("1+(-1)", BigInteger.ONE.add(minusOne).equals(BigInteger.ZERO));
        assertTrue("(-1)+1", minusOne.add(BigInteger.ONE).equals(BigInteger.ZERO));

        for (int i = 0; i < 200; i++) {
            BigInteger midbit = BigInteger.ZERO.setBit(i);
            assertTrue("add fails to carry on bit " + i, midbit.add(midbit)
                .equals(BigInteger.ZERO.setBit(i + 1)));
        }
        BigInteger bi2p3 = bi2.add(bi3);
        BigInteger bi3p2 = bi3.add(bi2);
        assertTrue("bi2p3=bi3p2", bi2p3.equals(bi3p2));


        // BESSER UEBERGREIFENDE TESTS MACHEN IN FORM VON STRESS TEST.
        // add large positive + small positive
        BigInteger sum = aZillion;
        BigInteger increment = BigInteger.ONE;
        for (int i = 0; i < 20; i++) {

        }

        // add large positive + small negative

        // add large negative + small positive

        // add large negative + small negative
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
                throw new AssertionError(e); // Android-changed
            }
        }
    }

    @Override
    protected void setUp() {
        bi2 = new BigInteger("4576829475724387584378543764555", 16);
        bi3 = new BigInteger("43987298363278574365732645872643587624387563245", 16);
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
}
