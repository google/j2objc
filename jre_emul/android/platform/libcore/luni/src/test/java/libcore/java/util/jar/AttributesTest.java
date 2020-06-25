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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import junit.framework.TestCase;

public class AttributesTest extends TestCase {
    private Attributes a;

    @Override
    protected void setUp() {
        a = new Attributes();
        a.putValue("1", "one");
        a.putValue("2", "two");
        a.putValue("3", "three");
        a.putValue("4", "four");
    }

    /**
     * java.util.jar.Attributes#Attributes(java.util.jar.Attributes)
     */
    public void test_ConstructorLjava_util_jar_Attributes() {
        Attributes a2 = new Attributes(a);
        assertEquals(a, a2);
        a.putValue("1", "one(1)");
        assertTrue("equal", !a.equals(a2));
    }

    /**
     * java.util.jar.Attributes#clear()
     */
    public void test_clear() {
        a.clear();
        assertNull("a) All entries should be null after clear", a.get("1"));
        assertNull("b) All entries should be null after clear", a.get("2"));
        assertNull("c) All entries should be null after clear", a.get("3"));
        assertNull("d) All entries should be null after clear", a.get("4"));
        assertTrue("Should not contain any keys", !a.containsKey("1"));
    }

    /**
     * java.util.jar.Attributes#containsKey(java.lang.Object)
     */
    public void test_containsKeyLjava_lang_Object() {
        assertTrue("a) Should have returned false", !a.containsKey(new Integer(1)));
        assertTrue("b) Should have returned false", !a.containsKey("0"));
        assertTrue("Should have returned true", a.containsKey(new Attributes.Name("1")));
    }

    /**
     * java.util.jar.Attributes#containsValue(java.lang.Object)
     */
    public void test_containsValueLjava_lang_Object() {
        assertTrue("Should have returned false", !a.containsValue("One"));
        assertTrue("Should have returned true", a.containsValue("one"));
    }

    /**
     * java.util.jar.Attributes#entrySet()
     */
    public void test_entrySet() {
        Set<Map.Entry<Object, Object>> entrySet = a.entrySet();
        Set<Object> keySet = new HashSet<Object>();
        Set<Object> valueSet = new HashSet<Object>();
        Iterator<?> i;
        assertEquals(4, entrySet.size());
        i = entrySet.iterator();
        while (i.hasNext()) {
            java.util.Map.Entry<?, ?> e;
            e = (Map.Entry<?, ?>) i.next();
            keySet.add(e.getKey());
            valueSet.add(e.getValue());
        }
        assertTrue("a) Should contain entry", valueSet.contains("one"));
        assertTrue("b) Should contain entry", valueSet.contains("two"));
        assertTrue("c) Should contain entry", valueSet.contains("three"));
        assertTrue("d) Should contain entry", valueSet.contains("four"));
        assertTrue("a) Should contain key", keySet.contains(new Attributes.Name("1")));
        assertTrue("b) Should contain key", keySet.contains(new Attributes.Name("2")));
        assertTrue("c) Should contain key", keySet.contains(new Attributes.Name("3")));
        assertTrue("d) Should contain key", keySet.contains(new Attributes.Name("4")));
    }

    /**
     * java.util.jar.Attributes#get(java.lang.Object)
     */
    public void test_getLjava_lang_Object() {
        assertEquals("a) Incorrect value returned", "one", a.getValue("1"));
        assertNull("b) Incorrect value returned", a.getValue("0"));
    }

    /**
     * java.util.jar.Attributes#isEmpty()
     */
    public void test_isEmpty() {
        assertTrue("Should not be empty", !a.isEmpty());
        a.clear();
        assertTrue("a) Should be empty", a.isEmpty());
        a = new Attributes();
        assertTrue("b) Should be empty", a.isEmpty());
    }

    /**
     * java.util.jar.Attributes#keySet()
     */
    public void test_keySet() {
        Set<?> s = a.keySet();
        assertEquals(4, s.size());
        assertTrue("a) Should contain entry", s.contains(new Attributes.Name("1")));
        assertTrue("b) Should contain entry", s.contains(new Attributes.Name("2")));
        assertTrue("c) Should contain entry", s.contains(new Attributes.Name("3")));
        assertTrue("d) Should contain entry", s.contains(new Attributes.Name("4")));
    }

    /**
     * java.util.jar.Attributes#putAll(java.util.Map)
     */
    public void test_putAllLjava_util_Map() {
        Attributes b = new Attributes();
        b.putValue("3", "san");
        b.putValue("4", "shi");
        b.putValue("5", "go");
        b.putValue("6", "roku");
        a.putAll(b);
        assertEquals("Should not have been replaced", "one", a.getValue("1"));
        assertEquals("Should have been replaced", "san", a.getValue("3"));
        assertEquals("Should have been added", "go", a.getValue("5"));
        Attributes atts = new Attributes();
        assertNull("Assert 0: ", atts.put(Attributes.Name.CLASS_PATH, "tools.jar"));
        assertNull("Assert 1: ", atts.put(Attributes.Name.MANIFEST_VERSION, "1"));
        Attributes atts2 = new Attributes();
        atts2.putAll(atts);
        assertEquals("Assert 2:", "tools.jar", atts2.get(Attributes.Name.CLASS_PATH));
        assertEquals("Assert 3: ", "1", atts2.get(Attributes.Name.MANIFEST_VERSION));
        try {
            atts.putAll(Collections.EMPTY_MAP);
            fail("Assert 4: no class cast from attrib parameter");
        } catch (ClassCastException e) {
            // Expected
        }
    }

    /**
     * java.util.jar.Attributes#remove(java.lang.Object)
     */
    public void test_removeLjava_lang_Object() {
        a.remove(new Attributes.Name("1"));
        a.remove(new Attributes.Name("3"));
        assertNull("Should have been removed", a.getValue("1"));
        assertEquals("Should not have been removed", "four", a.getValue("4"));
    }

    /**
     * java.util.jar.Attributes#size()
     */
    public void test_size() {
        assertEquals("Incorrect size returned", 4, a.size());
        a.clear();
        assertEquals(0, a.size());
    }

    /**
     * java.util.jar.Attributes#values()
     */
    public void test_values() {
        Collection<?> valueCollection = a.values();
        assertTrue("a) Should contain entry", valueCollection.contains("one"));
        assertTrue("b) Should contain entry", valueCollection.contains("two"));
        assertTrue("c) Should contain entry", valueCollection.contains("three"));
        assertTrue("d) Should contain entry", valueCollection.contains("four"));
    }

    /**
     * java.util.jar.Attributes#clone()
     */
    public void test_clone() {
        Attributes a2 = (Attributes) a.clone();
        assertEquals(a, a2);
        a.putValue("1", "one(1)");
        assertTrue("equal", !a.equals(a2));
    }

    /**
     * java.util.jar.Attributes#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        Attributes.Name n1 = new Attributes.Name("name"), n2 = new Attributes.Name("Name");
        assertEquals(n1, n2);
        Attributes a1 = new Attributes();
        a1.putValue("one", "1");
        a1.putValue("two", "2");
        Attributes a2 = new Attributes();
        a2.putValue("One", "1");
        a2.putValue("TWO", "2");
        assertEquals(a1, a2);
        assertEquals(a1, a1);
        a2 = null;
        assertFalse(a1.equals(a2));
    }

    /**
     * java.util.jar.Attributes.put(java.lang.Object, java.lang.Object)
     */
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        Attributes atts = new Attributes();
        assertNull("Assert 0: ", atts.put(Attributes.Name.CLASS_PATH, "tools.jar"));
        assertEquals("Assert 1: ", "tools.jar", atts.getValue(Attributes.Name.CLASS_PATH));
        // Regression for HARMONY-79
        try {
            atts.put("not a name", "value");
            fail("Assert 2: no class cast from key parameter");
        } catch (ClassCastException e) {
            // Expected
        }
        try {
            atts.put(Attributes.Name.CLASS_PATH, Boolean.TRUE);
            fail("Assert 3: no class cast from value parameter");
        } catch (ClassCastException e) {
            // Expected
        }
    }

    /**
     * java.util.jar.Attributes.put(java.lang.Object, java.lang.Object)
     */
    public void test_putLjava_lang_ObjectLjava_lang_Object_Null() {

        Attributes attribute = new Attributes();

        assertFalse(attribute.containsKey(null));
        assertFalse(attribute.containsValue(null));
        attribute.put(null, null);
        attribute.put(null, null);
        assertEquals(1, attribute.size());
        assertTrue(attribute.containsKey(null));
        assertTrue(attribute.containsValue(null));
        assertNull(attribute.get(null));

        String value = "It's null";
        attribute.put(null, value);
        assertEquals(1, attribute.size());
        assertEquals(value, attribute.get(null));

        Attributes.Name name = new Attributes.Name("null");
        attribute.put(name, null);
        assertEquals(2, attribute.size());
        assertNull(attribute.get(name));
    }

    /**
     * java.util.jar.Attributes.hashCode()
     */
    public void test_hashCode() {
        MockAttributes mockAttr = new MockAttributes();
        mockAttr.putValue("1", "one");
        assertEquals(mockAttr.getMap().hashCode(), mockAttr.hashCode());
    }

    private static class MockAttributes extends Attributes {
        public Map<Object, Object> getMap() {
            return map;
        }
    }
}
