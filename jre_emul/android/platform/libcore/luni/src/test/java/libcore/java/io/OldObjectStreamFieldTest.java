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

package libcore.java.io;

import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;

public class OldObjectStreamFieldTest extends junit.framework.TestCase {

    static class DummyClass implements Serializable {
        private static final long serialVersionUID = 999999999999998L;

        boolean bField = true;
        char cField = 'c';
        double dField = 424242.4242;
        float fField = 24.12F;
        int iField = 1965;
        long lField = 9999999L;
        short sField = 42;

        long bam = 999L;

        int ham = 9999;

        int sam = 8888;

        Object hola = new Object();

        public static long getUID() {
            return serialVersionUID;
        }
    }

    class MyObjectStreamField extends ObjectStreamField {
        public MyObjectStreamField(String name, Class<?> cl) {
            super(name, cl);
        }

        public void setOffset(int newValue) {
            super.setOffset(newValue);
        }
    }

    ObjectStreamClass osc;

    ObjectStreamField hamField;

    ObjectStreamField samField;

    ObjectStreamField bamField;

    ObjectStreamField holaField;

    public void test_ConstructorLjava_lang_StringLjava_lang_Class() {
        ObjectStreamField osf = new ObjectStreamField("aField", int.class);
        assertTrue("Test 1: Name member not set correctly.",
                   osf.getName().equals("aField"));
        assertTrue("Test 2: Type member not set correctly.",
                   osf.getType().equals(int.class));

        // Repeat the tests with a different object to make sure
        // that we have not tested against default values.
        osf = new ObjectStreamField("anotherField", String.class);
        assertTrue("Test 3: Name member not set correctly.",
                   osf.getName().equals("anotherField"));
        assertTrue("Test 4: Type member not set correctly.",
                   osf.getType().equals(String.class));

        // Invalid argument tests.
        try {
            osf = new ObjectStreamField(null, boolean.class);
            fail("Test 5: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
        try {
            osf = new ObjectStreamField("thisField", null);
            fail("Test 6: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
    }

    public void test_ConstructorLjava_lang_StringLjava_lang_ClassB() {
        ObjectStreamField osf = new ObjectStreamField("aField", int.class, false);
        assertTrue("Test 1: Name member not set correctly.",
                   osf.getName().equals("aField"));
        assertTrue("Test 2: Type member not set correctly.",
                   osf.getType().equals(int.class));
        assertFalse("Test 3: Unshared member not set correctly.",
                    osf.isUnshared());

        // Repeat the tests with a different object to make sure
        // that we have not tested against default values.
        osf = new ObjectStreamField("anotherField", String.class, true);
        assertTrue("Test 4: Name member not set correctly.",
                   osf.getName().equals("anotherField"));
        assertTrue("Test 5: Type member not set correctly.",
                   osf.getType().equals(String.class));
        assertTrue("Test 6: Unshared member not set correctly.",
                   osf.isUnshared());

        // Invalid argument tests.
        try {
            osf = new ObjectStreamField(null, boolean.class);
            fail("Test 7: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
        try {
            osf = new ObjectStreamField("thisField", null);
            fail("Test 8: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
    }


    public void test_getOffset() {
        ObjectStreamField[] osfArray;
        osfArray = osc.getFields();
        int[] expectedOffsets = {0, 1, 9, 11, 19, 23, 27, 31, 39, 41, 0};

        assertTrue("getOffset() did not return reasonable values.", osfArray[0]
                .getOffset() != osfArray[1].getOffset());

        for (int i = 0; i < expectedOffsets.length; i++) {
            assertEquals(String.format("Unexpected value for osfArray[%d].getOffset(): ", i),
                    expectedOffsets[i], osfArray[i].getOffset());

        }
    }

    public void test_setOffsetI() {
        MyObjectStreamField f = new MyObjectStreamField("aField", int.class);
        f.setOffset(42);
        assertEquals("Test 1: Unexpected offset value.", 42, f.getOffset());
        f.setOffset(2008);
        assertEquals("Test 2: Unexpected offset value.", 2008, f.getOffset());
    }

    public void test_isPrimitive() {
        // Test for method int java.io.ObjectStreamField.getOffset()
        ObjectStreamField[] osfArray;
        osfArray = osc.getFields();

        for (int i = 0; i < (osfArray.length - 1); i++) {
            assertTrue(String.format("osfArray[%d].isPrimitive() should return true.", i),
                       osfArray[i].isPrimitive());
        }
        assertFalse(String.format("osfArray[%d].isPrimitive() should return false.",
                                  osfArray.length - 1),
                    osfArray[(osfArray.length - 1)].isPrimitive());
    }

    protected void setUp() {
        osc = ObjectStreamClass.lookup(DummyClass.class);
        bamField = osc.getField("bam");
        samField = osc.getField("sam");
        hamField = osc.getField("ham");
        holaField = osc.getField("hola");
    }
}
