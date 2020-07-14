/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.lang;

import java.util.HashMap;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

import tests.util.SerializationTester;

public class EnumTest extends TestCase {

    enum Sample {
        LARRY, MOE, CURLY
    }

    Sample larry = Sample.LARRY;

    Sample moe = Sample.MOE;

    enum Empty {
    }

    enum Bogus {
        UNUSED
    }

    enum Color {
        Red, Green, Blue {};
    }

    enum MockCloneEnum {
        ONE;

        public void callClone() throws CloneNotSupportedException {
            super.clone();
        }
    }

    /**
     * java.lang.Enum#compareTo(java.lang.Enum)
     */
    public void test_compareToLjava_lang_Enum() {
        assertTrue(0 < Sample.MOE.compareTo(Sample.LARRY));
        assertEquals(0, Sample.MOE.compareTo(Sample.MOE));
        assertTrue(0 > Sample.MOE.compareTo(Sample.CURLY));
        try {
            Sample.MOE.compareTo((Sample) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.Enum#equals(Object)
     */
    public void test_equalsLjava_lang_Object() {
        assertFalse(moe.equals("bob"));
        assertTrue(moe.equals(Sample.MOE));
        assertFalse(Sample.LARRY.equals(Sample.CURLY));
        assertTrue(Sample.LARRY.equals(larry));
        assertFalse(Sample.CURLY.equals(null));
    }

    /**
     * java.lang.Enum#getDeclaringClass()
     */
    public void test_getDeclaringClass() {
        assertEquals(Sample.class, moe.getDeclaringClass());
    }

    /**
     * java.lang.Enum#hashCode()
     */
    public void test_hashCode() {
        assertEquals(moe.hashCode(), moe.hashCode());
    }

    /**
     * java.lang.Enum#name()
     */
    public void test_name() {
        assertEquals("MOE", moe.name());
    }

    /**
     * java.lang.Enum#ordinal()
     */
    public void test_ordinal() {
        assertEquals(0, larry.ordinal());
        assertEquals(1, moe.ordinal());
        assertEquals(2, Sample.CURLY.ordinal());
    }

    /**
     * java.lang.Enum#toString()
     */
    public void test_toString() {
        assertTrue(moe.toString().equals("MOE"));
    }

    /**
     * java.lang.Enum#valueOf(Class, String)
     */
    public void test_valueOfLjava_lang_String() {
        assertSame(Sample.CURLY, Sample.valueOf("CURLY"));
        assertSame(Sample.LARRY, Sample.valueOf("LARRY"));
        assertSame(moe, Sample.valueOf("MOE"));
        try {
            Sample.valueOf("non-existant");
            fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            Sample.valueOf(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // May be caused by some compilers' code
        } catch (IllegalArgumentException e) {
            // other compilers will throw this
        }


        Sample s = Enum.valueOf(Sample.class, "CURLY");
        assertSame(s, Sample.CURLY);
        s = Enum.valueOf(Sample.class, "LARRY");
        assertSame(larry, s);
        s = Enum.valueOf(Sample.class, "MOE");
        assertSame(s, moe);
        try {
            Enum.valueOf(Bogus.class, "MOE");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            Enum.valueOf((Class<Sample>) null, "a string");
            fail("Expected an exception");
        } catch (NullPointerException e) {
            // May be caused by some compilers' code
        } catch (IllegalArgumentException e) {
            // other compilers will throw this
        }
        try {
            Enum.valueOf(Sample.class, null);
            fail("Expected an exception");
        } catch (NullPointerException e) {
            // May be caused by some compilers' code
        } catch (IllegalArgumentException e) {
            // other compilers will throw this
        }
        try {
            Enum.valueOf((Class<Sample>) null, (String) null);
            fail("Expected an exception");
        } catch (NullPointerException e) {
            // May be caused by some compilers' code
        } catch (IllegalArgumentException e) {
            // other compilers will throw this
        }
    }

    /**
     * java.lang.Enum#values
     */
    public void test_values() {
        Sample[] myValues = Sample.values();
        assertEquals(3, myValues.length);

        assertEquals(Sample.LARRY, myValues[0]);
        assertEquals(Sample.MOE, myValues[1]);
        assertEquals(Sample.CURLY, myValues[2]);

        assertEquals(0, Empty.values().length);
    }

    /**
     * java.lang.Enum#clone()
     */
    public void test_clone() {
        try {
            MockCloneEnum.ONE.callClone();
            fail("Should throw CloneNotSupprotedException");
        } catch (CloneNotSupportedException e1) {
            // expected
        }

    }

    public void test_compatibilitySerialization_inClass_Complex_Harmony() throws Exception {
        // TODO migrate to the new testing framework 
        assertTrue(SerializationTester.assertCompabilityEquals(new MockEnum2(),
                "serialization/org/apache/harmony/tests/java/lang/EnumTest.harmony.ser"));
    }

    /**
     * serialization/deserialization compatibility.
     */
    public void testSerializationSelf() throws Exception {

        // test a map class that has enums.
        // regression test for Harmony-1163
        HashMap<Color, Integer> enumColorMap = new HashMap<Color, Integer>();
        enumColorMap.put(Color.Red, 1);
        enumColorMap.put(Color.Blue, 3);

        Object[] testCases = { enumColorMap, Sample.CURLY };

        SerializationTest.verifySelf(testCases);

        // test a class that has enums as its fields.
        MockEnum mock = new MockEnum();
        MockEnum test = (MockEnum) SerializationTest.copySerializable(mock);
        assertEquals(mock.i, test.i);
        assertEquals(mock.str, test.str);
        assertEquals(mock.samEnum, test.samEnum);

        // test a class that has enums and a string of same name as its fields.
        MockEnum2 mock2 = new MockEnum2();
        MockEnum2 test2 = (MockEnum2) SerializationTest.copySerializable(mock2);
        assertEquals(mock2.i, test2.i);
        assertEquals(mock2.str, test2.str);
        assertEquals(mock2.samEnum, test2.samEnum);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        // regression test for Harmony-1163
        HashMap<Color, Integer> enumColorMap = new HashMap<Color, Integer>();
        enumColorMap.put(Color.Red, 1);
        enumColorMap.put(Color.Blue, 3);

        Object[] testCases = { Sample.CURLY, new MockEnum(),
                // test a class that has enums and a string of same name as its fields.
                new MockEnum2(),
                // test a map class that has enums.
                enumColorMap, };

        SerializationTest.verifyGolden(this, testCases);
    }
}
