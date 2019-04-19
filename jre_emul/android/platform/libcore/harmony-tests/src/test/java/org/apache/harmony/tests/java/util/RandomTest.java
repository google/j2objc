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

package org.apache.harmony.tests.java.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import org.apache.harmony.testframework.serialization.SerializationTest;

public class RandomTest extends junit.framework.TestCase {

    private Random r;

    /**
     * java.util.Random#Random()
     */
    public void test_Constructor() {
        // Test for method java.util.Random()
        assertTrue("Used to test", true);
    }

    /**
     * java.util.Random#Random(long)
     */
    public void test_ConstructorJ() {
        Random r = new Random(8409238L);
        Random r2 = new Random(8409238L);
        for (int i = 0; i < 100; i++)
            assertTrue("Values from randoms with same seed don't match", r
                    .nextInt() == r2.nextInt());
    }

    public void test_setSeed() {
        Random r = new Random();
        r.setSeed(1337);
        Random r2 = new Random();
        r2.setSeed(1337);
        for (int i = 0; i < 100; i++)
            assertTrue("Values from randoms with same seed don't match", r
                    .nextInt() == r2.nextInt());
    }


    /**
     * java.util.Random#nextBoolean()
     */
    public void test_nextBoolean() {
        // Test for method boolean java.util.Random.nextBoolean()
        boolean falseAppeared = false, trueAppeared = false;
        for (int counter = 0; counter < 100; counter++)
            if (r.nextBoolean())
                trueAppeared = true;
            else
                falseAppeared = true;
        assertTrue("Calling nextBoolean() 100 times resulted in all trues",
                falseAppeared);
        assertTrue("Calling nextBoolean() 100 times resulted in all falses",
                trueAppeared);
    }

    /**
     * java.util.Random#nextBytes(byte[])
     */
    public void test_nextBytes$B() {
        // Test for method void java.util.Random.nextBytes(byte [])
        boolean someDifferent = false;
        byte[] randomBytes = new byte[100];
        r.nextBytes(randomBytes);
        byte firstByte = randomBytes[0];
        for (int counter = 1; counter < randomBytes.length; counter++)
            if (randomBytes[counter] != firstByte)
                someDifferent = true;
        assertTrue(
                "nextBytes() returned an array of length 100 of the same byte",
                someDifferent);
    }

    /**
     * java.util.Random#nextDouble()
     */
    public void test_nextDouble() {
        // Test for method double java.util.Random.nextDouble()
        double lastNum = r.nextDouble();
        double nextNum;
        boolean someDifferent = false;
        boolean inRange = true;
        for (int counter = 0; counter < 100; counter++) {
            nextNum = r.nextDouble();
            if (nextNum != lastNum)
                someDifferent = true;
            if (!(0 <= nextNum && nextNum < 1.0))
                inRange = false;
            lastNum = nextNum;
        }
        assertTrue("Calling nextDouble 100 times resulted in same number",
                someDifferent);
        assertTrue(
                "Calling nextDouble resulted in a number out of range [0,1)",
                inRange);
    }

    /**
     * java.util.Random#nextFloat()
     */
    public void test_nextFloat() {
        // Test for method float java.util.Random.nextFloat()
        float lastNum = r.nextFloat();
        float nextNum;
        boolean someDifferent = false;
        boolean inRange = true;
        for (int counter = 0; counter < 100; counter++) {
            nextNum = r.nextFloat();
            if (nextNum != lastNum)
                someDifferent = true;
            if (!(0 <= nextNum && nextNum < 1.0))
                inRange = false;
            lastNum = nextNum;
        }
        assertTrue("Calling nextFloat 100 times resulted in same number",
                someDifferent);
        assertTrue("Calling nextFloat resulted in a number out of range [0,1)",
                inRange);
    }

    /**
     * java.util.Random#nextGaussian()
     */
    public void test_nextGaussian() {
        // Test for method double java.util.Random.nextGaussian()
        double lastNum = r.nextGaussian();
        double nextNum;
        boolean someDifferent = false;
        boolean someInsideStd = false;
        for (int counter = 0; counter < 100; counter++) {
            nextNum = r.nextGaussian();
            if (nextNum != lastNum)
                someDifferent = true;
            if (-1.0 <= nextNum && nextNum <= 1.0)
                someInsideStd = true;
            lastNum = nextNum;
        }
        assertTrue("Calling nextGaussian 100 times resulted in same number",
                someDifferent);
        assertTrue(
                "Calling nextGaussian 100 times resulted in no number within 1 std. deviation of mean",
                someInsideStd);
    }

    /**
     * java.util.Random#nextInt()
     */
    public void test_nextInt() {
        // Test for method int java.util.Random.nextInt()
        int lastNum = r.nextInt();
        int nextNum;
        boolean someDifferent = false;
        for (int counter = 0; counter < 100; counter++) {
            nextNum = r.nextInt();
            if (nextNum != lastNum)
                someDifferent = true;
            lastNum = nextNum;
        }
        assertTrue("Calling nextInt 100 times resulted in same number",
                someDifferent);
    }

    /**
     * java.util.Random#nextInt(int)
     */
    public void test_nextIntI() {
        // Test for method int java.util.Random.nextInt(int)
        final int range = 10;
        int lastNum = r.nextInt(range);
        int nextNum;
        boolean someDifferent = false;
        boolean inRange = true;
        for (int counter = 0; counter < 100; counter++) {
            nextNum = r.nextInt(range);
            if (nextNum != lastNum)
                someDifferent = true;
            if (!(0 <= nextNum && nextNum < range))
                inRange = false;
            lastNum = nextNum;
        }
        assertTrue("Calling nextInt (range) 100 times resulted in same number",
                someDifferent);
        assertTrue(
                "Calling nextInt (range) resulted in a number outside of [0, range)",
                inRange);

    }

    /**
     * java.util.Random#nextLong()
     */
    public void test_nextLong() {
        // Test for method long java.util.Random.nextLong()
        long lastNum = r.nextLong();
        long nextNum;
        boolean someDifferent = false;
        for (int counter = 0; counter < 100; counter++) {
            nextNum = r.nextLong();
            if (nextNum != lastNum)
                someDifferent = true;
            lastNum = nextNum;
        }
        assertTrue("Calling nextLong 100 times resulted in same number",
                someDifferent);
    }

    /**
     * java.util.Random#setSeed(long)
     */
    public void test_setSeedJ() {
        // Test for method void java.util.Random.setSeed(long)
        long[] random1Values = new long[100];
        long[] random2Values = new long[100];
        long[] random3Values = new long[100];

        Random random1 = new Random();
        Random random2 = new Random();
        Random random3 = new Random();

        random1.setSeed(1337);
        random2.setSeed(1337);
        random3.setSeed(5000);

        for (int i = 0; i < 100; ++i) {
            random1Values[i] = random1.nextLong();
            random2Values[i] = random2.nextLong();
            random3Values[i] = random3.nextLong();
        }

        assertTrue(Arrays.equals(random1Values, random2Values));
        assertFalse(Arrays.equals(random2Values, random3Values));

        // Set random3's seed to 1337 and assert it results in the same sequence of
        // values as the first two randoms.
        random3.setSeed(1337);
        for (int i = 0; i < 100; ++i) {
            random3Values[i] = random3.nextLong();
        }

        assertTrue(Arrays.equals(random1Values, random3Values));
    }

    static final class Mock_Random extends Random {
        private boolean nextCalled = false;

        public boolean getFlag () {
            boolean retVal = nextCalled;
            nextCalled = false;
            return retVal;
        }

        @Override
        protected int next(int bits) {
            nextCalled = true;
            return super.next(bits);
        }
    }

    public void test_next() {
        Mock_Random mr = new Mock_Random();
        assertFalse(mr.getFlag());
        mr.nextBoolean();
        assertTrue(mr.getFlag());
        mr.nextBytes(new byte[10]);
        assertTrue(mr.getFlag());
        mr.nextDouble();
        assertTrue(mr.getFlag());
        mr.nextFloat();
        assertTrue(mr.getFlag());
        mr.nextGaussian();
        assertTrue(mr.getFlag());
        mr.nextInt();
        assertTrue(mr.getFlag());
        mr.nextInt(10);
        assertTrue(mr.getFlag());
        mr.nextLong();
        assertTrue(mr.getFlag());
    }

    @Override
    protected void setUp() {
        r = new Random();
    }

    @Override
    protected void tearDown() {
    }

    public void testSerializationCompatibility() throws Exception {
        Random rand = new Random(0x8123aea6267e055dL);
        rand.nextGaussian();
        // SerializationTest.createGoldenFile("/tmp", this, rand);
        SerializationTest.verifyGolden(this, rand, comparator);

        rand = new Random(0x8123aea6267e055dL);
        rand.nextGaussian();
        SerializationTest.verifySelf(rand, comparator);
    }

    public static final SerializationTest.SerializableAssert comparator =
            new SerializationTest.SerializableAssert() {
        public void assertDeserialized(Serializable initial, Serializable deserialized) {
            Random initialRand = (Random) initial;
            Random deserializedRand = (Random) deserialized;
            assertEquals("should be equal", initialRand.nextInt(), deserializedRand.nextInt());
        }
    };
}
