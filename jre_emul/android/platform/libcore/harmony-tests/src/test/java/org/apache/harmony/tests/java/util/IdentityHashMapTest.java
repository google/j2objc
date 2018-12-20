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

import com.google.j2objc.util.ReflectionUtil;
import libcore.java.util.SpliteratorTester;
import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;
import tests.support.Support_MapTest2;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;

public class IdentityHashMapTest extends junit.framework.TestCase {
    private static final String ID = "hello";

    class MockMap extends AbstractMap {
        public Set entrySet() {
            return null;
        }
        public int size(){
            return 0;
        }
    }
    /*
     * TODO: change all the statements testing the keys and values with equals()
     * method to check for reference equality instead
     */

    IdentityHashMap hm;

    final static int hmSize = 1000;

    Object[] objArray;

    Object[] objArray2;

    /**
     * java.util.IdentityHashMap#IdentityHashMap()
     */
    public void test_Constructor() {
        // Test for method java.util.IdentityHashMap()
        new Support_MapTest2(new IdentityHashMap()).runTest();

        IdentityHashMap hm2 = new IdentityHashMap();
        assertEquals("Created incorrect IdentityHashMap", 0, hm2.size());
    }

    /**
     * java.util.IdentityHashMap#IdentityHashMap(int)
     */
    public void test_ConstructorI() {
        // Test for method java.util.IdentityHashMap(int)
        IdentityHashMap hm2 = new IdentityHashMap(5);
        assertEquals("Created incorrect IdentityHashMap", 0, hm2.size());
        try {
            new IdentityHashMap(-1);
            fail("Failed to throw IllegalArgumentException for initial capacity < 0");
        } catch (IllegalArgumentException e) {
            //expected
        }

        IdentityHashMap empty = new IdentityHashMap(0);
        assertNull("Empty IdentityHashMap access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
    }

    /**
     * java.util.IdentityHashMap#IdentityHashMap(java.util.Map)
     */
    public void test_ConstructorLjava_util_Map() {
        // Test for method java.util.IdentityHashMap(java.util.Map)
        Map myMap = new TreeMap();
        for (int counter = 0; counter < hmSize; counter++)
            myMap.put(objArray2[counter], objArray[counter]);
        IdentityHashMap hm2 = new IdentityHashMap(myMap);
        for (int counter = 0; counter < hmSize; counter++)
            assertTrue("Failed to construct correct IdentityHashMap", hm
                    .get(objArray2[counter]) == hm2.get(objArray2[counter]));

        Map mockMap = new MockMap();
        hm2 = new IdentityHashMap(mockMap);
        assertEquals("Size should be 0", 0, hm2.size());

        try {
            new IdentityHashMap(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.IdentityHashMap#clear()
     */
    public void test_clear() {
        // Test for method void java.util.IdentityHashMap.clear()
        hm.clear();
        assertEquals("Clear failed to reset size", 0, hm.size());
        for (int i = 0; i < hmSize; i++)
            assertNull("Failed to clear all elements",
                    hm.get(objArray2[i]));

    }

    /**
     * java.util.IdentityHashMap#clone()
     */
    public void test_clone() {
        // Test for method java.lang.Object java.util.IdentityHashMap.clone()
        IdentityHashMap hm2 = (IdentityHashMap) hm.clone();
        assertTrue("Clone answered equivalent IdentityHashMap", hm2 != hm);
        for (int counter = 0; counter < hmSize; counter++)
            assertTrue("Clone answered unequal IdentityHashMap", hm
                    .get(objArray2[counter]) == hm2.get(objArray2[counter]));

        IdentityHashMap map = new IdentityHashMap();
        map.put("key", "value");
        // get the keySet() and values() on the original Map
        Set keys = map.keySet();
        Collection values = map.values();
        assertEquals("values() does not work",
                "value", values.iterator().next());
        assertEquals("keySet() does not work",
                "key", keys.iterator().next());
        AbstractMap map2 = (AbstractMap) map.clone();
        map2.put("key", "value2");
        Collection values2 = map2.values();
        assertTrue("values() is identical", values2 != values);
        // values() and keySet() on the cloned() map should be different
        assertEquals("values() was not cloned",
                "value2", values2.iterator().next());
        map2.clear();
        map2.put("key2", "value3");
        Set key2 = map2.keySet();
        assertTrue("keySet() is identical", key2 != keys);
        assertEquals("keySet() was not cloned",
                "key2", key2.iterator().next());
    }

    /**
     * java.util.IdentityHashMap#containsKey(java.lang.Object)
     */
    public void test_containsKeyLjava_lang_Object() {
        // Test for method boolean
        // java.util.IdentityHashMap.containsKey(java.lang.Object)
        assertTrue("Returned false for valid key", hm
                .containsKey(objArray2[23]));
        /* J2ObjC. This assert fails due to an Objective-C optimization for short strings:
           the string value is encoded directly in the pointer (NSTaggedPointerString).
        assertTrue("Returned true for copy of valid key", !hm
                .containsKey(new Integer(23).toString())); */
        assertTrue("Returned true for invalid key", !hm.containsKey("KKDKDKD"));

        IdentityHashMap m = new IdentityHashMap();
        m.put(null, "test");
        assertTrue("Failed with null key", m.containsKey(null));
        assertTrue("Failed with missing key matching null hash", !m
                .containsKey(new Integer(0)));
    }

    /**
     * java.util.IdentityHashMap#containsValue(java.lang.Object)
     */
    public void test_containsValueLjava_lang_Object() {
        // Test for method boolean
        // java.util.IdentityHashMap.containsValue(java.lang.Object)
        assertTrue("Returned false for valid value", hm
                .containsValue(objArray[19]));
        assertTrue("Returned true for invalid valie", !hm
                .containsValue(new Integer(-9)));
    }

    /**
     * java.util.IdentityHashMap#entrySet()
     */
    public void test_entrySet() {
        // Test for method java.util.Set java.util.IdentityHashMap.entrySet()
        Set s = hm.entrySet();
        Iterator i = s.iterator();
        assertTrue("Returned set of incorrect size", hm.size() == s.size());
        while (i.hasNext()) {
            Map.Entry m = (Map.Entry) i.next();
            assertTrue("Returned incorrect entry set", hm.containsKey(m
                    .getKey())
                    && hm.containsValue(m.getValue()));
        }
    }

    /**
     * java.util.IdentityHashMap#get(java.lang.Object)
     */
    public void test_getLjava_lang_Object() {
        // Test for method java.lang.Object
        // java.util.IdentityHashMap.get(java.lang.Object)
        assertNull("Get returned non-null for non existent key",
                hm.get("T"));
        hm.put("T", "HELLO");
        assertEquals("Get returned incorecct value for existing key", "HELLO", hm.get("T")
                );

        IdentityHashMap m = new IdentityHashMap();
        m.put(null, "test");
        assertEquals("Failed with null key", "test", m.get(null));
        assertNull("Failed with missing key matching null hash", m
                .get(new Integer(0)));
    }

    /**
     * java.util.IdentityHashMap#isEmpty()
     */
    public void test_isEmpty() {
        // Test for method boolean java.util.IdentityHashMap.isEmpty()
        assertTrue("Returned false for new map", new IdentityHashMap()
                .isEmpty());
        assertTrue("Returned true for non-empty", !hm.isEmpty());
    }

    /**
     * java.util.IdentityHashMap#keySet()
     */
    public void test_keySet() {
        // Test for method java.util.Set java.util.IdentityHashMap.keySet()
        Set s = hm.keySet();
        assertTrue("Returned set of incorrect size()", s.size() == hm.size());
        for (int i = 0; i < objArray.length; i++) {
            assertTrue("Returned set does not contain all keys", s
                    .contains(objArray2[i]));
        }

        IdentityHashMap m = new IdentityHashMap();
        m.put(null, "test");
        assertTrue("Failed with null key", m.keySet().contains(null));
        assertNull("Failed with null key", m.keySet().iterator().next());

        Map map = new IdentityHashMap(101);
        map.put(new Integer(1), "1");
        map.put(new Integer(102), "102");
        map.put(new Integer(203), "203");
        Iterator it = map.keySet().iterator();
        Integer remove1 = (Integer) it.next();
        it.hasNext();
        it.remove();
        Integer remove2 = (Integer) it.next();
        it.remove();
        ArrayList list = new ArrayList(Arrays.asList(new Integer[] {
                new Integer(1), new Integer(102), new Integer(203) }));
        list.remove(remove1);
        list.remove(remove2);
        assertTrue("Wrong result", it.next().equals(list.get(0)));
        assertEquals("Wrong size", 1, map.size());
        assertTrue("Wrong contents", map.keySet().iterator().next().equals(
                list.get(0)));

        Map map2 = new IdentityHashMap(101);
        map2.put(new Integer(1), "1");
        map2.put(new Integer(4), "4");
        Iterator it2 = map2.keySet().iterator();
        Integer remove3 = (Integer) it2.next();
        Integer next;
        if (remove3.intValue() == 1)
            next = new Integer(4);
        else
            next = new Integer(1);
        it2.hasNext();
        it2.remove();
        assertTrue("Wrong result 2", it2.next().equals(next));
        assertEquals("Wrong size 2", 1, map2.size());
        assertTrue("Wrong contents 2", map2.keySet().iterator().next().equals(
                next));
    }

    /**
     * java.util.IdentityHashMap#put(java.lang.Object, java.lang.Object)
     */
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        // Test for method java.lang.Object
        // java.util.IdentityHashMap.put(java.lang.Object, java.lang.Object)
        hm.put("KEY", "VALUE");
        assertEquals("Failed to install key/value pair",
                "VALUE", hm.get("KEY"));

        IdentityHashMap m = new IdentityHashMap();
        Short s0 = new Short((short) 0);
        m.put(s0, "short");
        m.put(null, "test");
        Integer i0 = new Integer(0);
        m.put(i0, "int");
        assertEquals("Failed adding to bucket containing null",
                "short", m.get(s0));
        assertEquals("Failed adding to bucket containing null2", "int", m.get(i0)
                );

        IdentityHashMap<Object, Object> map = new IdentityHashMap<Object, Object>();

        // Test null as a key.
        Object value = "Some value";
        map.put(null, value);
        assertSame("Assert 0: Failure getting null key", value, map.get(null));

        // Test null as a value
        Object key = "Some key";
        map.put(key, null);
        assertNull("Assert 1: Failure getting null value", map.get(key));
    }

    /**
     * java.util.IdentityHashMap#putAll(java.util.Map)
     */
    public void test_putAllLjava_util_Map() {
        // Test for method void java.util.IdentityHashMap.putAll(java.util.Map)
        IdentityHashMap hm2 = new IdentityHashMap();
        hm2.putAll(hm);
        for (int i = 0; i < 1000; i++)
            assertTrue("Failed to clear all elements", hm2.get(objArray2[i])
                    .equals((new Integer(i))));

        hm2 = new IdentityHashMap();
        Map mockMap = new MockMap();
        hm2.putAll(mockMap);
        assertEquals("Size should be 0", 0, hm2.size());

        try {
            hm2.putAll(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.IdentityHashMap#remove(java.lang.Object)
     */
    public void test_removeLjava_lang_Object() {
        // Test for method java.lang.Object
        // java.util.IdentityHashMap.remove(java.lang.Object)
        int size = hm.size();
        Integer x = ((Integer) hm.remove(objArray2[9]));
        assertTrue("Remove returned incorrect value", x.equals(new Integer(9)));
        assertNull("Failed to remove given key", hm.get(objArray2[9]));
        assertTrue("Failed to decrement size", hm.size() == (size - 1));
        assertNull("Remove of non-existent key returned non-null", hm
                .remove("LCLCLC"));

        IdentityHashMap m = new IdentityHashMap();
        m.put(null, "test");
        assertNull("Failed with same hash as null",
                m.remove(objArray[0]));
        assertEquals("Failed with null key", "test", m.remove(null));

        // Regression for HARMONY-37
        IdentityHashMap<String, String> hashMap = new IdentityHashMap<String, String>();
        hashMap.remove("absent");
        assertEquals("Assert 0: Size is incorrect", 0, hashMap.size());

        hashMap.put("key", "value");
        hashMap.remove("key");
        assertEquals("Assert 1: After removing non-null element size is incorrect", 0, hashMap.size());

        hashMap.put(null, null);
        assertEquals("Assert 2: adding literal null failed", 1, hashMap.size());
        hashMap.remove(null);
        assertEquals("Assert 3: After removing null element size is incorrect", 0, hashMap.size());
    }

    /**
     * java.util.IdentityHashMap#size()
     */
    public void test_size() {
        // Test for method int java.util.IdentityHashMap.size()
        assertEquals("Returned incorrect size, ", (objArray.length + 2), hm
                .size());
    }

    /**
     * java.util.IdentityHashMap#Serialization()
     */
    public void test_Serialization() throws Exception {
        if (ReflectionUtil.isJreReflectionStripped()) {
            return;
        }

        IdentityHashMap<String, String> map = new IdentityHashMap<String, String>();
        map.put(ID, "world");
        // BEGIN Android-added
        // Regression test for null key in serialized IdentityHashMap (1178549)
        // Together with this change the IdentityHashMap.golden.ser resource
        // was replaced by a version that contains a map with a null key.
        map.put(null, "null");
        // END Android-added
        SerializationTest.verifySelf(map, comparator);
        SerializationTest.verifyGolden(this, map, comparator);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        objArray = new Object[hmSize];
        objArray2 = new Object[hmSize];
        for (int i = 0; i < objArray.length; i++) {
            objArray[i] = new Integer(i);
            // Android-changed: the containsKey test requires unique strings.
            objArray2[i] = new String(objArray[i].toString());
        }

        hm = new IdentityHashMap();
        for (int i = 0; i < objArray.length; i++)
            hm.put(objArray2[i], objArray[i]);
        hm.put("test", null);
        hm.put(null, "test");
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        objArray = null;
        objArray2 = null;
        hm = null;
    }

    private static final SerializationTest.SerializableAssert comparator = new
                             SerializationTest.SerializableAssert() {

        public void assertDeserialized(Serializable initial, Serializable deserialized) {
            IdentityHashMap<String, String> initialMap = (IdentityHashMap<String, String>) initial;
            IdentityHashMap<String, String> deseriaMap = (IdentityHashMap<String, String>) deserialized;
            assertEquals("should be equal", initialMap.size(), deseriaMap.size());
        }

    };

    /**
     * java.util.IdentityHashMap#containsKey(java.lang.Object)
     * java.util.IdentityHashMap#containsValue(java.lang.Object)
     * java.util.IdentityHashMap#put(java.lang.Object, java.lang.Object)
     * java.util.IdentityHashMap#get(java.lang.Object)
     */
    public void test_null_Keys_and_Values() {
        // tests with null keys and values
        IdentityHashMap map = new IdentityHashMap();
        Object result;

        // null key and null value
        result = map.put(null, null);
        assertTrue("testA can not find null key", map.containsKey(null));
        assertTrue("testA can not find null value", map.containsValue(null));
        assertNull("testA can not get null value for null key",
                map.get(null));
        assertNull("testA put returned wrong value", result);

        // null value
        String value = "a value";
        result = map.put(null, value);
        assertTrue("testB can not find null key", map.containsKey(null));
        assertTrue("testB can not find a value with null key", map
                .containsValue(value));
        assertTrue("testB can not get value for null key",
                map.get(null) == value);
        assertNull("testB put returned wrong value", result);

        // a null key
        String key = "a key";
        result = map.put(key, null);
        assertTrue("testC can not find a key with null value", map
                .containsKey(key));
        assertTrue("testC can not find null value", map.containsValue(null));
        assertNull("testC can not get null value for key", map.get(key));
        assertNull("testC put returned wrong value", result);

        // another null key
        String anothervalue = "another value";
        result = map.put(null, anothervalue);
        assertTrue("testD can not find null key", map.containsKey(null));
        assertTrue("testD can not find a value with null key", map
                .containsValue(anothervalue));
        assertTrue("testD can not get value for null key",
                map.get(null) == anothervalue);
        assertTrue("testD put returned wrong value", result == value);

        // remove a null key
        result = map.remove(null);
        assertTrue("testE remove returned wrong value", result == anothervalue);
        assertTrue("testE should not find null key", !map.containsKey(null));
        assertTrue("testE should not find a value with null key", !map
                .containsValue(anothervalue));
        assertNull("testE should not get value for null key",
                map.get(null));
    }

    /**
     * java.util.IdentityHashMap#remove(java.lang.Object)
     * java.util.IdentityHashMap#keySet()
     */
    public void test_remove() {
        IdentityHashMap map = new IdentityHashMap();
        map.put(null, null);
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.remove("key1");

        assertTrue("Did not remove key1", !map.containsKey("key1"));
        assertTrue("Did not remove the value for key1", !map
                .containsValue("value1"));

        assertTrue("Modified key2", map.get("key2") != null
                && map.get("key2") == "value2");
        assertNull("Modified null entry", map.get(null));
    }

    /**
     * java.util.IdentityHashMap#entrySet()
     * java.util.IdentityHashMap#keySet()
     * java.util.IdentityHashMap#values()
     */
    public void test_sets() {
        // tests with null keys and values
        IdentityHashMap map = new IdentityHashMap();

        // null key and null value
        map.put("key", "value");
        map.put(null, null);
        map.put("a key", null);
        map.put("another key", null);

        Set keyset = map.keySet();
        Collection valueset = map.values();
        Set entries = map.entrySet();
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            assertTrue("EntrySetIterator can not find entry ", entries
                    .contains(entry));

            assertTrue("entry key not found in map", map.containsKey(entry
                    .getKey()));
            assertTrue("entry value not found in map", map.containsValue(entry
                    .getValue()));

            assertTrue("entry key not found in the keyset", keyset
                    .contains(entry.getKey()));
            assertTrue("entry value not found in the valueset", valueset
                    .contains(entry.getValue()));
        }
    }

    /**
     * java.util.IdentityHashMap#entrySet()
     * java.util.IdentityHashMap#remove(java.lang.Object)
     */
    public void test_entrySet_removeAll() {
        IdentityHashMap map = new IdentityHashMap();
        for (int i = 0; i < 1000; i++) {
            map.put(new Integer(i), new Integer(i));
        }
        Set set = map.entrySet();

        set.removeAll(set);
        assertEquals("did not remove all elements in the map", 0, map.size());
        assertTrue("did not remove all elements in the entryset", set.isEmpty());

        Iterator it = set.iterator();
        assertTrue("entrySet iterator still has elements", !it.hasNext());
    }

    /**
     * java.util.IdentityHashMap#keySet()
     * java.util.IdentityHashMap#clear()
     */
    public void test_keySet_clear() {
        IdentityHashMap map = new IdentityHashMap();
        for (int i = 0; i < 1000; i++) {
            map.put(new Integer(i), new Integer(i));
        }
        Set set = map.keySet();
        set.clear();

        assertEquals("did not remove all elements in the map", 0, map.size());
        assertTrue("did not remove all elements in the keyset", set.isEmpty());

        Iterator it = set.iterator();
        assertTrue("keySet iterator still has elements", !it.hasNext());
    }

    /**
     * java.util.IdentityHashMap#values()
     */
    public void test_values() {

        IdentityHashMap map = new IdentityHashMap();
        for (int i = 0; i < 10; i++) {
            map.put(new Integer(i), new Integer(i));
        }

        Integer key = new Integer(20);
        Integer value = new Integer(40);
        map.put(key, value);

        Collection vals = map.values();
        boolean result = vals.remove(key);
        assertTrue("removed entries incorrectly", map.size() == 11 && !result);
        assertTrue("removed key incorrectly", map.containsKey(key));
        assertTrue("removed value incorrectly", map.containsValue(value));

        result = vals.remove(value);
        assertTrue("Did not remove entry as expected", map.size() == 10
                && result);
        assertTrue("Did not remove key as expected", !map.containsKey(key));
        assertTrue("Did not remove value as expected", !map
                .containsValue(value));

        // put an equivalent key to a value
        key = new Integer(1);
        value = new Integer(100);
        map.put(key, value);

        result = vals.remove(key);
        assertTrue("TestB. removed entries incorrectly", map.size() == 11
                && !result);
        assertTrue("TestB. removed key incorrectly", map.containsKey(key));
        assertTrue("TestB. removed value incorrectly", map.containsValue(value));

        result = vals.remove(value);
        assertTrue("TestB. Did not remove entry as expected", map.size() == 10
                && result);
        assertTrue("TestB. Did not remove key as expected", !map
                .containsKey(key));
        assertTrue("TestB. Did not remove value as expected", !map
                .containsValue(value));

        vals.clear();
        assertEquals("Did not remove all entries as expected", 0, map.size());
    }

    /**
     * java.util.IdentityHashMap#keySet()
     * java.util.IdentityHashMap#remove(java.lang.Object)
     */
    public void test_keySet_removeAll() {
        IdentityHashMap map = new IdentityHashMap();
        for (int i = 0; i < 1000; i++) {
            map.put(new Integer(i), new Integer(i));
        }
        Set set = map.keySet();
        set.removeAll(set);

        assertEquals("did not remove all elements in the map", 0, map.size());
        assertTrue("did not remove all elements in the keyset", set.isEmpty());

        Iterator it = set.iterator();
        assertTrue("keySet iterator still has elements", !it.hasNext());
    }

    /**
     * java.util.IdentityHashMap#keySet()
     */
    public void test_keySet_retainAll() {
        IdentityHashMap map = new IdentityHashMap();
        for (int i = 0; i < 1000; i++) {
            map.put(new Integer(i), new Integer(i));
        }
        Set set = map.keySet();

        // retain all the elements
        boolean result = set.retainAll(set);
        assertTrue("retain all should return false", !result);
        assertEquals("did not retain all", 1000, set.size());

        // send empty set to retainAll
        result = set.retainAll(new TreeSet());
        assertTrue("retain all should return true", result);
        assertEquals("did not remove all elements in the map", 0, map.size());
        assertTrue("did not remove all elements in the keyset", set.isEmpty());

        Iterator it = set.iterator();
        assertTrue("keySet iterator still has elements", !it.hasNext());
    }

    /**
     * java.util.IdentityHashMap#keySet()
     * java.util.IdentityHashMap#remove(java.lang.Object)
     */
    public void test_keyset_remove() {
        IdentityHashMap map = new IdentityHashMap();

        Integer key = new Integer(21);

        map.put(new Integer(1), null);
        map.put(new Integer(11), null);
        map.put(key, null);
        map.put(new Integer(31), null);
        map.put(new Integer(41), null);
        map.put(new Integer(51), null);
        map.put(new Integer(61), null);
        map.put(new Integer(71), null);
        map.put(new Integer(81), null);
        map.put(new Integer(91), null);

        Set set = map.keySet();

        Set newset = new HashSet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Object element = it.next();
            if (element == key) {
                it.remove();
            } else
                newset.add(element);
        }
        int size = newset.size();
        assertTrue("keyset and newset don't have same size",
                newset.size() == size);
        assertTrue("element is in newset ", !newset.contains(key));
        assertTrue("element not removed from keyset", !set.contains(key));
        assertTrue("element not removed from map", !map.containsKey(key));

        assertTrue("newset and keyset do not have same elements 1", newset
                .equals(set));
        assertTrue("newset and keyset do not have same elements 2", set
                .equals(newset));
    }

    public void test_clone_scenario1() {
        IdentityHashMap hashMap = new IdentityHashMap();
        assertEquals(0, hashMap.hashCode());
        Object cloneHashMap = hashMap.clone();
        ((IdentityHashMap) cloneHashMap).put("key", "value");
        assertEquals(0, hashMap.hashCode());
        assertTrue(0 != cloneHashMap.hashCode());
    }

    public void test_clone_scenario2() {
        IdentityHashMap hashMap = new IdentityHashMap();
        assertEquals(0, hashMap.hashCode());
        Object cloneHashMap = hashMap.clone();
        hashMap.put("key", "value");
        assertEquals(1, hashMap.size());
        assertEquals(0, ((IdentityHashMap) cloneHashMap).size());
        assertEquals("value", hashMap.get("key"));
        assertNull(((IdentityHashMap) cloneHashMap).get("key"));
        assertTrue(0 != hashMap.hashCode());
        assertEquals(0, cloneHashMap.hashCode());
    }

    public void test_clone_scenario3() {
        IdentityHashMap hashMap = new IdentityHashMap();
        assertEquals(0, hashMap.hashCode());
        hashMap.put("key", "value");
        Object cloneHashMap = hashMap.clone();
        assertEquals(1, hashMap.size());
        assertEquals(1, ((IdentityHashMap) cloneHashMap).size());
        assertEquals("value", hashMap.get("key"));
        assertEquals("value", ((IdentityHashMap) cloneHashMap).get("key"));
        assertEquals(hashMap.hashCode(), cloneHashMap.hashCode());
    }

    public void test_clone_scenario4() {
        IdentityHashMap hashMap = new IdentityHashMap();
        Object cloneHashMap = hashMap.clone();
        assertNull(((IdentityHashMap) cloneHashMap).get((Object) null));
        hashMap.put((Object) null, cloneHashMap);
        assertNull(((IdentityHashMap) cloneHashMap).get((Object) null));
        assertEquals(cloneHashMap, hashMap.get((Object) null));
    }

    public void test_clone_scenario5() throws Exception {
        IdentityHashMap hashMap = new IdentityHashMap();
        Object cloneHashMap = hashMap.clone();
        assertNull(hashMap.remove((Object) null));
        ((IdentityHashMap) cloneHashMap).put((Object) null, cloneHashMap);
        assertNull(hashMap.remove((Object) null));
        assertEquals(cloneHashMap, ((IdentityHashMap) cloneHashMap)
                .get((Object) null));
    }

    /*
    * Regression test for HARMONY-6419
    */
    public void test_underlyingMap() {
        IdentityHashMap<String, String> ihm = new IdentityHashMap<String, String>();
        String key = "key";
        String value = "value";
        ihm.put(key, value);

        Set<Map.Entry<String, String>> set = ihm.entrySet();
        assertEquals(1, set.size());

        Map.Entry<String, String> entry = set.iterator().next();

        String newValue = "newvalue";
        entry.setValue(newValue);
        assertSame(newValue, ihm.get(key));
    }

    public void test_forEach() throws Exception {
        IdentityHashMap<String, String> map = new IdentityHashMap<>();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        IdentityHashMap<String, String> output = new IdentityHashMap<>();
        map.forEach((k, v) -> output.put(k,v));
        assertEquals(map, output);

        HashSet<String> setOutput = new HashSet<>();
        map.keySet().forEach((k) -> setOutput.add(k));
        assertEquals(map.keySet(), setOutput);

        setOutput.clear();
        map.values().forEach((v) -> setOutput.add(v));
        assertEquals(new HashSet<>(map.values()), setOutput);

        HashSet<Map.Entry<String,String>> entrySetOutput = new HashSet<>();
        map.entrySet().forEach((v) -> entrySetOutput.add(v));
        assertEquals(map.entrySet(), entrySetOutput);
    }


    public void test_forEach_NPE() throws Exception {
        IdentityHashMap<String, String> map = new IdentityHashMap<>();
        try {
            map.forEach(null);
            fail();
        } catch(NullPointerException expected) {}

        try {
            map.keySet().forEach(null);
            fail();
        } catch(NullPointerException expected) {}

        try {
            map.values().forEach(null);
            fail();
        } catch(NullPointerException expected) {}

        try {
            map.entrySet().forEach(null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_forEach_CME() throws Exception {
        IdentityHashMap<String, String> map = new IdentityHashMap<>();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        IdentityHashMap<String, String> outputMap = new IdentityHashMap<>();
        try {
            map.forEach(new java.util.function.BiConsumer<String, String>() {
                    @Override
                    public void accept(String k, String v) {
                        outputMap.put(k, v);
                        map.put("foo1", v);
                    }
                });
            fail();
        } catch(ConcurrentModificationException expected) {}
        // We should get a CME and DO NOT continue forEach evaluation
        assertEquals(1, outputMap.size());

        outputMap.clear();
        try {
            map.keySet().forEach(new java.util.function.Consumer<String>() {
                    @Override
                    public void accept(String k) {
                        outputMap.put(k, "foo");
                        map.put("foo2", "boo");
                    }
                });
            fail();
        } catch(ConcurrentModificationException expected) {}
        // We should get a CME and DO NOT continue forEach evaluation
        assertEquals(1, outputMap.size());

        outputMap.clear();
        try {
            map.values().forEach(new java.util.function.Consumer<String>() {
                    @Override
                    public void accept(String k)  {
                        outputMap.put(k, "foo");
                        map.put("foo3", "boo");
                    }
                });
            fail();
        } catch(ConcurrentModificationException expected) {}
        // We should get a CME and DO NOT continue forEach evaluation
        assertEquals(1, outputMap.size());

        outputMap.clear();
        try {
            map.entrySet().forEach(new java.util.function.Consumer<Map.Entry<String,String>>() {
                    @Override
                    public void accept(Map.Entry<String,String> k)  {
                        outputMap.put(k.getKey(), "foo");
                        map.put("foo4", "boo");
                    }
                });
            fail();
        } catch(ConcurrentModificationException expected) {}
        // We should get a CME and DO NOT continue forEach evaluation
        assertEquals(1, outputMap.size());
    }

    public void test_spliterator_keySet() {
        IdentityHashMap<String, String> hashMap = new IdentityHashMap<>();
        hashMap.put("a", "1");
        hashMap.put("b", "2");
        hashMap.put("c", "3");
        hashMap.put("d", "4");
        hashMap.put("e", "5");
        hashMap.put("f", "6");
        hashMap.put("g", "7");
        hashMap.put("h", "8");
        hashMap.put("i", "9");
        hashMap.put("j", "10");
        hashMap.put("k", "11");
        hashMap.put("l", "12");
        hashMap.put("m", "13");
        hashMap.put("n", "14");
        hashMap.put("o", "15");
        hashMap.put("p", "16");

        Set<String> keys = hashMap.keySet();
        ArrayList<String> expectedKeys = new ArrayList<>(keys);

        SpliteratorTester.runBasicIterationTests(keys.spliterator(), expectedKeys);
        SpliteratorTester.runBasicSplitTests(keys, expectedKeys);
        SpliteratorTester.testSpliteratorNPE(keys.spliterator());
        SpliteratorTester.assertSupportsTrySplit(keys);
    }

    public void test_spliterator_valueSet() {
        IdentityHashMap<String, String> hashMap = new IdentityHashMap<>();
        hashMap.put("a", "1");
        hashMap.put("b", "2");
        hashMap.put("c", "3");
        hashMap.put("d", "4");
        hashMap.put("e", "5");
        hashMap.put("f", "6");
        hashMap.put("g", "7");
        hashMap.put("h", "8");
        hashMap.put("i", "9");
        hashMap.put("j", "10");
        hashMap.put("k", "11");
        hashMap.put("l", "12");
        hashMap.put("m", "13");
        hashMap.put("n", "14");
        hashMap.put("o", "15");
        hashMap.put("p", "16");

        Collection<String> values = hashMap.values();
        ArrayList<String> expectedValues = new ArrayList<>(values);

        SpliteratorTester.runBasicIterationTests(values.spliterator(), expectedValues);
        SpliteratorTester.runBasicSplitTests(values, expectedValues);
        SpliteratorTester.testSpliteratorNPE(values.spliterator());
        SpliteratorTester.assertSupportsTrySplit(values);
    }

    public void test_spliterator_entrySet() {
        IdentityHashMap<String, String> hashMap = new IdentityHashMap<>();
        hashMap.put("a", "1");
        hashMap.put("b", "2");
        hashMap.put("c", "3");
        hashMap.put("d", "4");
        hashMap.put("e", "5");
        hashMap.put("f", "6");
        hashMap.put("g", "7");
        hashMap.put("h", "8");
        hashMap.put("i", "9");
        hashMap.put("j", "10");
        hashMap.put("k", "11");
        hashMap.put("l", "12");
        hashMap.put("m", "13");
        hashMap.put("n", "14");
        hashMap.put("o", "15");
        hashMap.put("p", "16");

        Set<Map.Entry<String, String>> values = hashMap.entrySet();
        ArrayList<Map.Entry<String, String>> expectedValues = new ArrayList<>(values);

        Comparator<Map.Entry<String, String>> comparator =
                (a, b) -> (a.getKey().compareTo(b.getKey()));

        SpliteratorTester.runBasicIterationTests(values.spliterator(), expectedValues);
        SpliteratorTester.runBasicSplitTests(values, expectedValues, comparator);
        SpliteratorTester.testSpliteratorNPE(values.spliterator());
        SpliteratorTester.assertSupportsTrySplit(values);
    }

    public void test_replaceAll() {
        IdentityHashMap<String, String> map = new IdentityHashMap<>();
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";

        map.put(key1, "1");
        map.put(key2, "2");
        map.put(key3, "3");

        map.replaceAll((k, v) -> k + v);

        assertEquals("key11", map.get(key1));
        assertEquals("key22", map.get(key2));
        assertEquals("key33", map.get(key3));
        assertEquals(3, map.size());

        try {
            map.replaceAll(new BiFunction<String, String, String>() {
                @Override
                public String apply(String s, String s2) {
                    map.put("key4", "4");
                    return "";
                }
            });
            fail();
        } catch (ConcurrentModificationException expected) {}
    }


    // comparator for IdentityHashMap objects
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            IdentityHashMap init = (IdentityHashMap) initial;
            IdentityHashMap desr = (IdentityHashMap) deserialized;

            assertEquals("Size", init.size(), desr.size());
        }
    };
}
