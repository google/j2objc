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

import libcore.java.util.SpliteratorTester;
import org.apache.harmony.testframework.serialization.SerializationTest;
import tests.support.Support_MapTest2;
import tests.support.Support_UnmodifiableCollectionTest;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeMap;

public class HashMapTest extends junit.framework.TestCase {
    class MockMap extends AbstractMap {
        public Set entrySet() {
            return Collections.EMPTY_SET;
        }

        public int size() {
            return 0;
        }
    }

    private static class MockMapNull extends AbstractMap {
        public Set entrySet() {
            return null;
        }

        public int size() {
            return 10;
        }
    }

    interface MockInterface {
        public String mockMethod();
    }

    class MockClass implements MockInterface {
        public String mockMethod() {
            return "This is a MockClass";
        }
    }

    class MockHandler implements InvocationHandler {

        Object obj;

        public MockHandler(Object o) {
            obj = o;
        }

        public Object invoke(Object proxy, Method m, Object[] args)
                throws Throwable {
            Object result = null;

            try {
                result = m.invoke(obj, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }


    HashMap hm;

    final static int hmSize = 1000;

    Object[] objArray;

    Object[] objArray2;

    /**
     * java.util.HashMap#HashMap()
     */
    public void test_Constructor() {
        // Test for method java.util.HashMap()
        new Support_MapTest2(new HashMap()).runTest();

        HashMap hm2 = new HashMap();
        assertEquals("Created incorrect HashMap", 0, hm2.size());
    }

    /**
     * java.util.HashMap#HashMap(int)
     */
    public void test_ConstructorI() {
        // Test for method java.util.HashMap(int)
        HashMap hm2 = new HashMap(5);
        assertEquals("Created incorrect HashMap", 0, hm2.size());
        try {
            new HashMap(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        HashMap empty = new HashMap(0);
        assertNull("Empty hashmap access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
    }

    /**
     * java.util.HashMap#HashMap(int, float)
     */
    public void test_ConstructorIF() {
        // Test for method java.util.HashMap(int, float)
        HashMap hm2 = new HashMap(5, (float) 0.5);
        assertEquals("Created incorrect HashMap", 0, hm2.size());
        try {
            new HashMap(0, 0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        HashMap empty = new HashMap(0, 0.75f);
        assertNull("Empty hashtable access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
    }

    /**
     * java.util.HashMap#HashMap(java.util.Map)
     */
    public void test_ConstructorLjava_util_Map() {
        // Test for method java.util.HashMap(java.util.Map)
        Map myMap = new TreeMap();
        for (int counter = 0; counter < hmSize; counter++)
            myMap.put(objArray2[counter], objArray[counter]);
        HashMap hm2 = new HashMap(myMap);
        for (int counter = 0; counter < hmSize; counter++)
            assertTrue("Failed to construct correct HashMap", hm
                    .get(objArray2[counter]) == hm2.get(objArray2[counter]));

        Map mockMap = new MockMap();
        hm = new HashMap(mockMap);
        assertEquals(hm, mockMap);
    }

    /**
     * java.util.HashMap#clear()
     */
    public void test_clear() {
        hm.clear();
        assertEquals("Clear failed to reset size", 0, hm.size());
        for (int i = 0; i < hmSize; i++)
            assertNull("Failed to clear all elements",
                    hm.get(objArray2[i]));

        // Check clear on a large loaded map of Integer keys
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        for (int i = -32767; i < 32768; i++) {
            map.put(i, "foobar");
        }
        map.clear();
        assertEquals("Failed to reset size on large integer map", 0, hm.size());
        for (int i = -32767; i < 32768; i++) {
            assertNull("Failed to clear integer map values", map.get(i));
        }
    }

    /**
     * java.util.HashMap#clone()
     */
    public void test_clone() {
        // Test for method java.lang.Object java.util.HashMap.clone()
        HashMap hm2 = (HashMap) hm.clone();
        assertTrue("Clone answered equivalent HashMap", hm2 != hm);
        for (int counter = 0; counter < hmSize; counter++)
            assertTrue("Clone answered unequal HashMap", hm
                    .get(objArray2[counter]) == hm2.get(objArray2[counter]));

        HashMap map = new HashMap();
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

        // regresion test for HARMONY-4603
        HashMap hashmap = new HashMap();
        MockClonable mock = new MockClonable(1);
        hashmap.put(1, mock);
        assertEquals(1, ((MockClonable) hashmap.get(1)).i);
        HashMap hm3 = (HashMap) hashmap.clone();
        assertEquals(1, ((MockClonable) hm3.get(1)).i);
        mock.i = 0;
        assertEquals(0, ((MockClonable) hashmap.get(1)).i);
        assertEquals(0, ((MockClonable) hm3.get(1)).i);
    }

    /**
     * java.util.HashMap#containsKey(java.lang.Object)
     */
    public void test_containsKeyLjava_lang_Object() {
        // Test for method boolean
        // java.util.HashMap.containsKey(java.lang.Object)
        assertTrue("Returned false for valid key", hm.containsKey(new Integer(
                876).toString()));
        assertTrue("Returned true for invalid key", !hm.containsKey("KKDKDKD"));

        HashMap m = new HashMap();
        m.put(null, "test");
        assertTrue("Failed with null key", m.containsKey(null));
        assertTrue("Failed with missing key matching null hash", !m
                .containsKey(new Integer(0)));
    }

    /**
     * java.util.HashMap#containsValue(java.lang.Object)
     */
    public void test_containsValueLjava_lang_Object() {
        // Test for method boolean
        // java.util.HashMap.containsValue(java.lang.Object)
        assertTrue("Returned false for valid value", hm
                .containsValue(new Integer(875)));
        assertTrue("Returned true for invalid valie", !hm
                .containsValue(new Integer(-9)));
    }

    /**
     * java.util.HashMap#entrySet()
     */
    public void test_entrySet() {
        // Test for method java.util.Set java.util.HashMap.entrySet()
        Set s = hm.entrySet();
        Iterator i = s.iterator();
        assertTrue("Returned set of incorrect size", hm.size() == s.size());
        while (i.hasNext()) {
            Map.Entry m = (Map.Entry) i.next();
            assertTrue("Returned incorrect entry set", hm.containsKey(m
                    .getKey())
                    && hm.containsValue(m.getValue()));
        }

        Iterator iter = s.iterator();
        s.remove(iter.next());
        assertEquals(1001, s.size());
    }

    /**
     * java.util.HashMap#entrySet()
     */
    public void test_entrySetEquals() {
        Set s1 = hm.entrySet();
        Set s2 = new HashMap(hm).entrySet();
        assertEquals(s1, s2);
    }

    /**
     * java.util.HashMap#entrySet()
     */
    public void test_removeFromViews() {
        hm.put("A", null);
        hm.put("B", null);
        assertTrue(hm.keySet().remove("A"));

        Map<String, String> m2 = new HashMap<String, String>();
        m2.put("B", null);
        assertTrue(hm.entrySet().remove(m2.entrySet().iterator().next()));
    }


    /**
     * java.util.HashMap#get(java.lang.Object)
     */
    public void test_getLjava_lang_Object() {
        // Test for method java.lang.Object
        // java.util.HashMap.get(java.lang.Object)
        assertNull("Get returned non-null for non existent key",
                hm.get("T"));
        hm.put("T", "HELLO");
        assertEquals("Get returned incorrect value for existing key", "HELLO", hm.get("T")
        );

        HashMap m = new HashMap();
        m.put(null, "test");
        assertEquals("Failed with null key", "test", m.get(null));
        assertNull("Failed with missing key matching null hash", m
                .get(new Integer(0)));

        // Regression for HARMONY-206
        ReusableKey k = new ReusableKey();
        HashMap map = new HashMap();
        k.setKey(1);
        map.put(k, "value1");

        k.setKey(18);
        assertNull(map.get(k));

        k.setKey(17);
        assertNull(map.get(k));
    }

    /**
     * java.util.HashMap#isEmpty()
     */
    public void test_isEmpty() {
        // Test for method boolean java.util.HashMap.isEmpty()
        assertTrue("Returned false for new map", new HashMap().isEmpty());
        assertTrue("Returned true for non-empty", !hm.isEmpty());
    }

    /**
     * java.util.HashMap#keySet()
     */
    public void test_keySet() {
        // Test for method java.util.Set java.util.HashMap.keySet()
        Set s = hm.keySet();
        assertTrue("Returned set of incorrect size()", s.size() == hm.size());
        for (int i = 0; i < objArray.length; i++)
            assertTrue("Returned set does not contain all keys", s
                    .contains(objArray[i].toString()));

        HashMap m = new HashMap();
        m.put(null, "test");
        assertTrue("Failed with null key", m.keySet().contains(null));
        assertNull("Failed with null key", m.keySet().iterator().next());

        Map map = new HashMap(101);
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

        Map map2 = new HashMap(101);
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
     * java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        hm.put("KEY", "VALUE");
        assertEquals("Failed to install key/value pair", "VALUE", hm.get("KEY"));

        HashMap<Object, Object> m = new HashMap<Object, Object>();
        m.put(new Short((short) 0), "short");
        m.put(null, "test");
        m.put(new Integer(0), "int");
        assertEquals("Failed adding to bucket containing null", "short", m
                .get(new Short((short) 0)));
        assertEquals("Failed adding to bucket containing null2", "int", m
                .get(new Integer(0)));

        // Check my actual key instance is returned
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        for (int i = -32767; i < 32768; i++) {
            map.put(i, "foobar");
        }
        Integer myKey = new Integer(0);
        // Put a new value at the old key position
        map.put(myKey, "myValue");
        assertTrue(map.containsKey(myKey));
        assertEquals("myValue", map.get(myKey));
        boolean found = false;
        for (Iterator<Integer> itr = map.keySet().iterator(); itr.hasNext(); ) {
            Integer key = itr.next();
            if (found = key == myKey) {
                break;
            }
        }
        assertFalse("Should not find new key instance in hashmap", found);

        // Add a new key instance and check it is returned
        assertNotNull(map.remove(myKey));
        map.put(myKey, "myValue");
        assertTrue(map.containsKey(myKey));
        assertEquals("myValue", map.get(myKey));
        for (Iterator<Integer> itr = map.keySet().iterator(); itr.hasNext(); ) {
            Integer key = itr.next();
            if (found = key == myKey) {
                break;
            }
        }
        assertTrue("Did not find new key instance in hashmap", found);

        // Ensure keys with identical hashcode are stored separately
        HashMap<Object, Object> objmap = new HashMap<Object, Object>();
        for (int i = 0; i < 32768; i++) {
            objmap.put(i, "foobar");
        }
        // Put non-equal object with same hashcode
        MyKey aKey = new MyKey();
        assertNull(objmap.put(aKey, "value"));
        assertNull(objmap.remove(new MyKey()));
        assertEquals("foobar", objmap.get(0));
        assertEquals("value", objmap.get(aKey));
    }

    static class MyKey {
        public MyKey() {
            super();
        }

        public int hashCode() {
            return 0;
        }
    }

    /**
     * java.util.HashMap#putAll(java.util.Map)
     */
    public void test_putAllLjava_util_Map() {
        // Test for method void java.util.HashMap.putAll(java.util.Map)
        HashMap hm2 = new HashMap();
        hm2.putAll(hm);
        for (int i = 0; i < 1000; i++)
            assertTrue("Failed to clear all elements", hm2.get(
                    new Integer(i).toString()).equals((new Integer(i))));

        Map mockMap = new MockMap();
        hm2 = new HashMap();
        hm2.putAll(mockMap);
        assertEquals("Size should be 0", 0, hm2.size());
    }

    /**
     * java.util.HashMap#putAll(java.util.Map)
     */
    public void test_putAllLjava_util_Map_Null() {
        HashMap hashMap = new HashMap();
        try {
            hashMap.putAll(new MockMapNull());
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected.
        }

        try {
            hashMap = new HashMap(new MockMapNull());
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected.
        }
    }

    public void test_putAllLjava_util_Map_Resize() {
        Random rnd = new Random(666);

        Map<Integer,Integer> m1 = new HashMap<Integer, Integer>();
        int MID = 10000;
        for (int i = 0; i < MID; i++) {
            Integer j = rnd.nextInt();
            m1.put(j, j);
        }

        Map<Integer,Integer> m2 = new HashMap<Integer, Integer>();
        int HI = 30000;
        for (int i = MID; i < HI; i++) {
            Integer j = rnd.nextInt();
            m2.put(j, j);
        }

        m1.putAll(m2);

        rnd = new Random(666);
        for (int i = 0; i < HI; i++) {
            Integer j = rnd.nextInt();
            assertEquals(j, m1.get(j));
        }
    }

    /**
     * java.util.HashMap#remove(java.lang.Object)
     */
    public void test_removeLjava_lang_Object() {
        int size = hm.size();
        Integer y = new Integer(9);
        Integer x = ((Integer) hm.remove(y.toString()));
        assertTrue("Remove returned incorrect value", x.equals(new Integer(9)));
        assertNull("Failed to remove given key", hm.get(new Integer(9)));
        assertTrue("Failed to decrement size", hm.size() == (size - 1));
        assertNull("Remove of non-existent key returned non-null", hm
                .remove("LCLCLC"));

        HashMap m = new HashMap();
        m.put(null, "test");
        assertNull("Failed with same hash as null",
                m.remove(new Integer(0)));
        assertEquals("Failed with null key", "test", m.remove(null));

        HashMap<Integer, Object> map = new HashMap<Integer, Object>();
        for (int i = 0; i < 32768; i++) {
            map.put(i, "const");
        }
        Object[] values = new Object[32768];
        for (int i = 0; i < 32768; i++) {
            values[i] = new Object();
            map.put(i, values[i]);
        }
        for (int i = 32767; i >= 0; i--) {
            assertEquals("Failed to remove same value", values[i], map.remove(i));
        }

        // Ensure keys with identical hashcode are removed properly
        map = new HashMap<Integer, Object>();
        for (int i = -32767; i < 32768; i++) {
            map.put(i, "foobar");
        }
        // Remove non equal object with same hashcode
        assertNull(map.remove(new MyKey()));
        assertEquals("foobar", map.get(0));
        map.remove(0);
        assertNull(map.get(0));
    }

    /**
     * java.util.HashMap#size()
     */
    public void test_size() {
        // Test for method int java.util.HashMap.size()
        assertTrue("Returned incorrect size",
                hm.size() == (objArray.length + 2));
    }

    /**
     * java.util.HashMap#values()
     */
    public void test_values() {
        // Test for method java.util.Collection java.util.HashMap.values()
        Collection c = hm.values();
        assertTrue("Returned collection of incorrect size()", c.size() == hm
                .size());
        for (int i = 0; i < objArray.length; i++)
            assertTrue("Returned collection does not contain all keys", c
                    .contains(objArray[i]));

        HashMap myHashMap = new HashMap();
        for (int i = 0; i < 100; i++)
            myHashMap.put(objArray2[i], objArray[i]);
        Collection values = myHashMap.values();
        new Support_UnmodifiableCollectionTest(
                "Test Returned Collection From HashMap.values()", values)
                .runTest();
        values.remove(new Integer(0));
        assertTrue(
                "Removing from the values collection should remove from the original map",
                !myHashMap.containsValue(new Integer(0)));

    }

    /**
     * java.util.AbstractMap#toString()
     */
    public void test_toString() {

        HashMap m = new HashMap();
        m.put(m, m);
        String result = m.toString();
        assertTrue("should contain self ref", result.indexOf("(this") > -1);
    }

    static class ReusableKey {
        private int key = 0;

        public void setKey(int key) {
            this.key = key;
        }

        public int hashCode() {
            return key;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ReusableKey)) {
                return false;
            }
            return key == ((ReusableKey) o).key;
        }
    }

    public void test_Map_Entry_hashCode() {
        //Related to HARMONY-403
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(10);
        Integer key = new Integer(1);
        Integer val = new Integer(2);
        map.put(key, val);
        int expected = key.hashCode() ^ val.hashCode();
        assertEquals(expected, map.hashCode());
        key = new Integer(4);
        val = new Integer(8);
        map.put(key, val);
        expected += key.hashCode() ^ val.hashCode();
        assertEquals(expected, map.hashCode());
    }

    class MockClonable implements Cloneable {
        public int i;

        public MockClonable(int i) {
            this.i = i;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return new MockClonable(i);
        }
    }

    /*
    * Regression test for HY-4750
    */
    public void test_EntrySet() {
        HashMap map = new HashMap();
        map.put(new Integer(1), "ONE");

        Set entrySet = map.entrySet();
        Iterator e = entrySet.iterator();
        Object real = e.next();
        Map.Entry copyEntry = new MockEntry();
        assertEquals(real, copyEntry);
        assertTrue(entrySet.contains(copyEntry));

        entrySet.remove(copyEntry);
        assertFalse(entrySet.contains(copyEntry));
    }

    public void test_forEach() throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        HashMap<String, String> output = new HashMap<>();
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
        HashMap<String, String> map = new HashMap<>();
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
        HashMap<String, String> map = new HashMap<>();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        HashMap<String, String> outputMap = new HashMap<>();
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

    private static class MockEntry implements Map.Entry {

        public Object getKey() {
            return new Integer(1);
        }

        public Object getValue() {
            return "ONE";
        }

        public Object setValue(Object object) {
            return null;
        }
    }

    public void test_spliterator_keySet() {
        HashMap<String, String> hashMap = new HashMap<>();
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

        assertTrue(keys.spliterator().hasCharacteristics(Spliterator.SIZED | Spliterator.DISTINCT));

        SpliteratorTester.runSizedTests(keys.spliterator(), 16);
        SpliteratorTester.runDistinctTests(keys);
        SpliteratorTester.assertSupportsTrySplit(keys);
    }

    public void test_spliterator_valueSet() {
        HashMap<String, String> hashMap = new HashMap<>();
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

        assertTrue(values.spliterator().hasCharacteristics(Spliterator.SIZED));

        SpliteratorTester.runSizedTests(values.spliterator(), 16);
        SpliteratorTester.assertSupportsTrySplit(values);
    }

    public void test_spliterator_entrySet() {
        HashMap<String, String> hashMap = new HashMap<>();
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

        assertTrue(values.spliterator().hasCharacteristics(Spliterator.SIZED | Spliterator.DISTINCT));

        SpliteratorTester.runSizedTests(values.spliterator(), 16);
        SpliteratorTester.runDistinctTests(values);
        SpliteratorTester.assertSupportsTrySplit(values);
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
            objArray2[i] = objArray[i].toString();
        }

        hm = new HashMap();
        for (int i = 0; i < objArray.length; i++)
            hm.put(objArray2[i], objArray[i]);
        hm.put("test", null);
        hm.put(null, "test");
    }

    protected void tearDown() {
        hm = null;
        objArray = null;
        objArray2 = null;
    }

    class SubMap<K, V> extends HashMap<K, V> {
        public SubMap(Map<? extends K, ? extends V> m) {
            super(m);
        }

        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("key", "value");

        SerializationTest.verifySelf(hm);

        //  regression for HARMONY-1583
        hm.put(null, "null");
        SerializationTest.verifySelf(hm);
    }
}

