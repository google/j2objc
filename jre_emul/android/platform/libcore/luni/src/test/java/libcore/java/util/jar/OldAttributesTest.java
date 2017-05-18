/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package libcore.java.util.jar;

import java.util.jar.Attributes;
import junit.framework.TestCase;

public class OldAttributesTest extends TestCase {
    private Attributes a;

    @Override
    protected void setUp() {
        a = new Attributes();
        a.putValue("1", "one");
        a.putValue("2", "two");
        a.putValue("3", "three");
        a.putValue("4", "four");
    }

    public void test_getLjava_lang_Object() {

        try {
            a.getValue("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
            // expected
        }
    }

    public void test_Constructor() {
        Attributes attr = new Attributes();
        assertTrue(attr.size() >= 0);
    }

    public void test_ConstructorI() {
        Attributes attr = new Attributes(10);
        assertTrue(attr.size() >= 0);
    }

    public void test_getLjava_lang_Object_true() {
        assertEquals("a) Incorrect value returned", "one", a
                .get(new Attributes.Name("1")));
        assertNull("b) Incorrect value returned", a.get("0"));
        assertNull("b) Incorrect value returned", a.get("1"));
    }

    public void test_getValueLjava_util_jar_Attributes_Name() {
        assertEquals("a) Incorrect value returned", "one", a
                .getValue(new Attributes.Name("1")));
        assertNull("b) Incorrect value returned", a
                .getValue(new Attributes.Name("0")));
    }

    public void test_hashCode() {
        Attributes b = (Attributes) a.clone();
        b.putValue("33", "Thirty three");
        assertNotSame(a.hashCode(), b.hashCode());
        b = (Attributes) a.clone();
        b.clear();
        assertNotSame(a.hashCode(), b.hashCode());
    }

    public void test_putValueLjava_lang_StringLjava_lang_String() {
        Attributes b = new Attributes();
        b.put(new Attributes.Name("1"), "one");
        b.putValue("2", "two");
        b.put(new Attributes.Name("3"), "three");
        b.putValue("4", "four");

        assertTrue(a.equals(b));

        try {
            b.putValue(null, "null");
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            // expected
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 0x10000; i++) {
            sb.append('3');
        }
        try {
            b.putValue(new String(sb), "wrong name");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
            // expected
        }
    }
}
