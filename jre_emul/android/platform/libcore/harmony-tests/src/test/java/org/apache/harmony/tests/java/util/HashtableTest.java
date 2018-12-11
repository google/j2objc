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

import tests.support.Support_MapTest2;
import tests.support.Support_UnmodifiableCollectionTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class HashtableTest extends junit.framework.TestCase {

    private Hashtable ht10;

    private Hashtable ht100;

    private Hashtable htfull;

    private Vector keyVector;

    private Vector elmVector;

    private String h10sVal;

    /**
     * java.util.Hashtable#Hashtable()
     */
    public void test_Constructor() {
        // Test for method java.util.Hashtable()
        new Support_MapTest2(new Hashtable()).runTest();

        Hashtable h = new Hashtable();

        assertEquals("Created incorrect hashtable", 0, h.size());
    }

    /**
     * java.util.Hashtable#Hashtable(int)
     */
    public void test_ConstructorI() {
        // Test for method java.util.Hashtable(int)
        Hashtable h = new Hashtable(9);

        assertEquals("Created incorrect hashtable", 0, h.size());

        Hashtable empty = new Hashtable(0);
        assertNull("Empty hashtable access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
    }

    /**
     * java.util.Hashtable#Hashtable(int, float)
     */
    public void test_ConstructorIF() {
        // Test for method java.util.Hashtable(int, float)
        Hashtable h = new java.util.Hashtable(10, 0.5f);
        assertEquals("Created incorrect hashtable", 0, h.size());

        Hashtable empty = new Hashtable(0, 0.75f);
        assertNull("Empty hashtable access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");

        try {
            new Hashtable(-1, 0.75f);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            new Hashtable(0, -0.75f);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    /**
     * java.util.Hashtable#Hashtable(java.util.Map)
     */
    public void test_ConstructorLjava_util_Map() {
        // Test for method java.util.Hashtable(java.util.Map)
        Map map = new TreeMap();
        Object firstVal = "Gabba";
        Object secondVal = new Integer(5);
        map.put("Gah", firstVal);
        map.put("Ooga", secondVal);
        Hashtable ht = new Hashtable(map);
        assertTrue("a) Incorrect Hashtable constructed",
                ht.get("Gah") == firstVal);
        assertTrue("b) Incorrect Hashtable constructed",
                ht.get("Ooga") == secondVal);

        try {
            new Hashtable(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    public void test_HashTable_Constructor() {
        Hashtable hashTable = new Hashtable();
        hashTable.put(hashTable, hashTable.keySet());
        new Hashtable(hashTable);
    }

    /**
     * java.util.Hashtable#clear()
     */
    public void test_clear() {
        // Test for method void java.util.Hashtable.clear()
        Hashtable h = hashtableClone(htfull);
        h.clear();
        assertEquals("Hashtable was not cleared", 0, h.size());
        Enumeration el = h.elements();
        Enumeration keys = h.keys();
        assertTrue("Hashtable improperly cleared", !el.hasMoreElements()
                && !(keys.hasMoreElements()));
    }

    /**
     * java.util.Hashtable#clone()
     */
    public void test_clone() {
        // Test for method java.lang.Object java.util.Hashtable.clone()

        Hashtable h = (Hashtable) htfull.clone();
        assertTrue("Clone different size than original", h.size() == htfull
                .size());

        Enumeration org = htfull.keys();
        Enumeration cpy = h.keys();

        String okey, ckey;
        while (org.hasMoreElements()) {
            assertTrue("Key comparison failed", (okey = (String) org
                    .nextElement()).equals(ckey = (String) cpy.nextElement()));
            assertTrue("Value comparison failed", ((String) htfull.get(okey))
                    .equals((String) h.get(ckey)));
        }
        assertTrue("Copy has more keys than original", !cpy.hasMoreElements());
    }

    /**
     * java.util.Hashtable#contains(java.lang.Object)
     */
    public void test_containsLjava_lang_Object() {
        // Test for method boolean
        // java.util.Hashtable.contains(java.lang.Object)
        assertTrue("Element not found", ht10.contains("Val 7"));
        assertTrue("Invalid element found", !ht10.contains("ZZZZZZZZZZZZZZZZ"));

        try {
            ht10.contains(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.Hashtable#containsKey(java.lang.Object)
     */
    public void test_containsKeyLjava_lang_Object() {
        // Test for method boolean
        // java.util.Hashtable.containsKey(java.lang.Object)

        assertTrue("Failed to find key", htfull.containsKey("FKey 4"));
        assertTrue("Failed to find key", !htfull.containsKey("FKey 99"));

        try {
            htfull.containsKey(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.Hashtable#containsValue(java.lang.Object)
     */
    public void test_containsValueLjava_lang_Object() {
        // Test for method boolean
        // java.util.Hashtable.containsValue(java.lang.Object)
        Enumeration e = elmVector.elements();
        while (e.hasMoreElements())
            assertTrue("Returned false for valid value", ht10.containsValue(e
                    .nextElement()));
        assertTrue("Returned true for invalid value", !ht10
                .containsValue(new Object()));

        try {
            ht10.containsValue(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            //expected
        }
    }

    /**
     * java.util.Hashtable#elements()
     */
    public void test_elements() {
        // Test for method java.util.Enumeration java.util.Hashtable.elements()
        Enumeration elms = ht10.elements();
        int i = 0;
        while (elms.hasMoreElements()) {
            String s = (String) elms.nextElement();
            assertTrue("Missing key from enumeration", elmVector.contains(s));
            ++i;
        }

        assertEquals("All keys not retrieved", 10, ht10.size());

        assertFalse(elms.hasMoreElements());
        try {
            elms.nextElement();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

// BEGIN Android-removed
// implementation dependent
//    /**
//     * java.util.Hashtable#elements()
//     */
//    public void test_elements_subtest0() {
//        // this is the reference implementation behavior
//        final Hashtable ht = new Hashtable(7);
//        ht.put("1", "a");
//        // these three elements hash to the same bucket in a 7 element Hashtable
//        ht.put("2", "b");
//        ht.put("9", "c");
//        ht.put("12", "d");
//        // Hashtable looks like:
//        // 0: "1"
//        // 1: "12" -> "9" -> "2"
//        Enumeration en = ht.elements();
//        // cache the first entry
//        en.hasMoreElements();
//        ht.remove("12");
//        ht.remove("9");
//        boolean exception = false;
//        try {
//            // cached "12"
//            Object result = en.nextElement();
//            assertNull("unexpected: " + result, result);
//            // next is removed "9"
//            result = en.nextElement();
//            assertNull("unexpected: " + result, result);
//            result = en.nextElement();
//            assertTrue("unexpected: " + result, "b".equals(result));
//        } catch (NoSuchElementException e) {
//            exception = true;
//        }
//        assertTrue("unexpected NoSuchElementException", !exception);
//    }
// END Android-removed

    /**
     * java.util.Hashtable#entrySet()
     */
    public void test_entrySet() {
        // Test for method java.util.Set java.util.Hashtable.entrySet()
        Set s = ht10.entrySet();
        Set s2 = new HashSet();
        Iterator i = s.iterator();
        while (i.hasNext())
            s2.add(((Map.Entry) i.next()).getValue());
        Enumeration e = elmVector.elements();
        while (e.hasMoreElements())
            assertTrue("Returned incorrect entry set", s2.contains(e
                    .nextElement()));
// BEGIN Android-removed
// implementation dependent
//        assertEquals("Not synchronized",
//                "java.util.Collections$SynchronizedSet", s.getClass().getName());
// END Android-removed

        boolean exception = false;
        try {
            ((Map.Entry) ht10.entrySet().iterator().next()).setValue(null);
        } catch (NullPointerException e1) {
            exception = true;
        }
        assertTrue(
                "Should not be able to assign null to a Hashtable entrySet() Map.Entry",
                exception);
    }

    /**
     * java.util.Hashtable#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean java.util.Hashtable.equals(java.lang.Object)
        Hashtable h = hashtableClone(ht10);
        assertTrue("Returned false for equal tables", ht10.equals(h));
        assertTrue("Returned true for unequal tables", !ht10.equals(htfull));
    }

    /**
     * java.util.Hashtable#get(java.lang.Object)
     */
    public void test_getLjava_lang_Object() {
        // Test for method java.lang.Object
        // java.util.Hashtable.get(java.lang.Object)
        Hashtable h = hashtableClone(htfull);
        assertEquals("Could not retrieve element", "FVal 2", ((String) h.get("FKey 2"))
                );

// BEGIN Android-removed
// implementation dependent
//        // Regression for HARMONY-262
//        ReusableKey k = new ReusableKey();
//        Hashtable h2 = new Hashtable();
//        k.setKey(1);
//        h2.put(k, "value1");
//
//        k.setKey(13);
//        assertNull(h2.get(k));
//
//        k.setKey(12);
//        assertNull(h2.get(k));
//
//        try {
//            h2.get(null);
//            fail("NullPointerException expected");
//        } catch (NullPointerException e) {
//            //expected
//        }
// END Android-removed
    }

    /**
     * java.util.Hashtable#hashCode()
     */
    public void test_hashCode() {
        // Test for method int java.util.Hashtable.hashCode()
        Set entrySet = ht10.entrySet();
        Iterator iterator = entrySet.iterator();
        int expectedHash;
        for (expectedHash = 0; iterator.hasNext(); expectedHash += iterator
                .next().hashCode())
            ;
        assertTrue("Incorrect hashCode returned.  Wanted: " + expectedHash
                + " got: " + ht10.hashCode(), expectedHash == ht10.hashCode());
    }

    /**
     * java.util.Hashtable#isEmpty()
     */
    public void test_isEmpty() {
        // Test for method boolean java.util.Hashtable.isEmpty()

        assertTrue("isEmpty returned incorrect value", !ht10.isEmpty());
        assertTrue("isEmpty returned incorrect value",
                new java.util.Hashtable().isEmpty());

        final Hashtable ht = new Hashtable();
        ht.put("0", "");
        Thread t1 = new Thread() {
            public void run() {
                while (!ht.isEmpty())
                    ;
                ht.put("final", "");
            }
        };
        t1.start();
        for (int i = 1; i < 10000; i++) {
            synchronized (ht) {
                ht.remove(String.valueOf(i - 1));
                ht.put(String.valueOf(i), "");
            }
            int size;
            if ((size = ht.size()) != 1) {
                String result = "Size is not 1: " + size + " " + ht;
                // terminate the thread
                ht.clear();
                fail(result);
            }
        }
        // terminate the thread
        ht.clear();
    }

    /**
     * java.util.Hashtable#keys()
     */
    public void test_keys() {
        // Test for method java.util.Enumeration java.util.Hashtable.keys()

        Enumeration keys = ht10.keys();
        int i = 0;
        while (keys.hasMoreElements()) {
            String s = (String) keys.nextElement();
            assertTrue("Missing key from enumeration", keyVector.contains(s));
            ++i;
        }

        assertEquals("All keys not retrieved", 10, ht10.size());

        assertFalse(keys.hasMoreElements());
        try {
            keys.nextElement();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

    /**
     * java.util.Hashtable#keys()
     */
    public void test_keys_subtest0() {
        // this is the reference implementation behavior
        final Hashtable ht = new Hashtable(3);
        ht.put("initial", "");
        Enumeration en = ht.keys();
        en.hasMoreElements();
        ht.remove("initial");
        boolean exception = false;
        try {
            Object result = en.nextElement();
            assertTrue("unexpected: " + result, "initial".equals(result));
        } catch (NoSuchElementException e) {
            exception = true;
        }
        assertTrue("unexpected NoSuchElementException", !exception);
    }

    /**
     * java.util.Hashtable#keySet()
     */
    public void test_keySet() {
        // Test for method java.util.Set java.util.Hashtable.keySet()
        Set s = ht10.keySet();
        Enumeration e = keyVector.elements();
        while (e.hasMoreElements())
            assertTrue("Returned incorrect key set", s
                    .contains(e.nextElement()));

// BEGIN Android-removed
// implementation dependent
//        assertEquals("Not synchronized",
//                "java.util.Collections$SynchronizedSet", s.getClass().getName());
// END Android-removed

        Map map = new Hashtable(101);
        map.put(new Integer(1), "1");
        map.put(new Integer(102), "102");
        map.put(new Integer(203), "203");
        Iterator it = map.keySet().iterator();
        Integer remove1 = (Integer) it.next();
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

        Map map2 = new Hashtable(101);
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
     * java.util.Hashtable#keySet()
     */
    public void test_keySet_subtest0() {
        Set s1 = ht10.keySet();
        assertTrue("should contain key", s1.remove("Key 0"));
        assertTrue("should not contain key", !s1.remove("Key 0"));

        final int iterations = 10000;
        final Hashtable ht = new Hashtable();
        Thread t1 = new Thread() {
            public void run() {
                for (int i = 0; i < iterations; i++) {
                    ht.put(String.valueOf(i), "");
                    ht.remove(String.valueOf(i));
                }
            }
        };
        t1.start();
        Set set = ht.keySet();
        for (int i = 0; i < iterations; i++) {
            Iterator it = set.iterator();
            try {
                it.next();
                it.remove();
                int size;
                // ensure removing with the iterator doesn't corrupt the
                // Hashtable
                if ((size = ht.size()) < 0) {
                    fail("invalid size: " + size);
                }
            } catch (NoSuchElementException e) {
            } catch (ConcurrentModificationException e) {
            }
        }
    }

// BEGIN Android-removed
// implementation dependent
//    /**
//     * java.util.Hashtable#keySet()
//     */
//    public void test_keySet_subtest1() {
//        // this is the reference implementation behavior
//        final Hashtable ht = new Hashtable(7);
//        ht.put("1", "a");
//        // these three elements hash to the same bucket in a 7 element Hashtable
//        ht.put("2", "b");
//        ht.put("9", "c");
//        ht.put("12", "d");
//        // Hashtable looks like:
//        // 0: "1"
//        // 1: "12" -> "9" -> "2"
//        Enumeration en = ht.elements();
//        // cache the first entry
//        en.hasMoreElements();
//        Iterator it = ht.keySet().iterator();
//        // this is mostly a copy of the test in test_elements_subtest0()
//        // test removing with the iterator does not null the values
//        while (it.hasNext()) {
//            String key = (String) it.next();
//            if ("12".equals(key) || "9".equals(key)) {
//                it.remove();
//            }
//        }
//        it.remove();
//        boolean exception = false;
//        try {
//            // cached "12"
//            Object result = en.nextElement();
//            assertTrue("unexpected: " + result, "d".equals(result));
//            // next is removed "9"
//            result = en.nextElement();
//            assertTrue("unexpected: " + result, "c".equals(result));
//            result = en.nextElement();
//            assertTrue("unexpected: " + result, "b".equals(result));
//        } catch (NoSuchElementException e) {
//            exception = true;
//        }
//        assertTrue("unexpected NoSuchElementException", !exception);
//    }
// END Android-removed

    /**
     * java.util.Hashtable#put(java.lang.Object, java.lang.Object)
     */
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        // Test for method java.lang.Object
        // java.util.Hashtable.put(java.lang.Object, java.lang.Object)
        Hashtable h = hashtableClone(ht100);
        Integer key = new Integer(100);
        h.put("Value 100", key);
        assertTrue("Key/Value not inserted", h.size() == 1 && (h.contains(key)));

        // Put into "full" table
        h = hashtableClone(htfull);
        h.put("Value 100", key);
        assertTrue("Key/Value not inserted into full table", h.size() == 8
                && (h.contains(key)));

        try {
            h.put(null, key);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }

        try {
            h.put("Value 100", null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.Hashtable#putAll(java.util.Map)
     */
    public void test_putAllLjava_util_Map() {
        // Test for method void java.util.Hashtable.putAll(java.util.Map)
        Hashtable h = new Hashtable();
        h.putAll(ht10);
        Enumeration e = keyVector.elements();
        while (e.hasMoreElements()) {
            Object x = e.nextElement();
            assertTrue("Failed to put all elements", h.get(x).equals(
                    ht10.get(x)));
        }

        try {
            h.putAll(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            //expected
        }
    }

    /**
     * java.util.Hashtable#remove(java.lang.Object)
     */
    public void test_removeLjava_lang_Object() {
        // Test for method java.lang.Object
        // java.util.Hashtable.remove(java.lang.Object)
        Hashtable h = hashtableClone(htfull);
        Object k = h.remove("FKey 0");
        assertTrue("Remove failed", !h.containsKey("FKey 0") || k == null);
        assertNull(h.remove("FKey 0"));

        try {
            h.remove(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    public void test_HashTable_remove_scenario1() {
        Hashtable hashTable = new Hashtable();
        Set keySet = hashTable.keySet();
        hashTable.put(hashTable, keySet);
        hashTable.remove(hashTable);
    }

    public void test_HashTable_remove_scenario2() {
        Hashtable hashTable = new Hashtable();
        Set keySet = hashTable.keySet();
        hashTable.put(hashTable, hashTable);
        hashTable.remove(hashTable);
    }

    public void test_HashTable_remove_scenario3() {
        Hashtable hashTable = new Hashtable();
        Hashtable keyHashTable = new Hashtable();
        keyHashTable.put(hashTable, keyHashTable);
        hashTable.put(keyHashTable, hashTable);
        hashTable.remove(keyHashTable);
    }

    /**
     * java.util.Hashtable#size()
     */
    public void test_size() {
        // Test for method int java.util.Hashtable.size()
        assertTrue("Returned invalid size", ht10.size() == 10
                && (ht100.size() == 0));

        final Hashtable ht = new Hashtable();
        ht.put("0", "");
        Thread t1 = new Thread() {
            public void run() {
                while (ht.size() > 0)
                    ;
                ht.put("final", "");
            }
        };
        t1.start();
        for (int i = 1; i < 10000; i++) {
            synchronized (ht) {
                ht.remove(String.valueOf(i - 1));
                ht.put(String.valueOf(i), "");
            }
            int size;
            if ((size = ht.size()) != 1) {
                String result = "Size is not 1: " + size + " " + ht;
                // terminate the thread
                ht.clear();
                fail(result);
            }
        }
        // terminate the thread
        ht.clear();
    }

    /**
     * java.util.Hashtable#toString()
     */
    public void test_toString() {
        // Test for method java.lang.String java.util.Hashtable.toString()
        Hashtable h = new Hashtable();
        assertEquals("Incorrect toString for Empty table",
                "{}", h.toString());

        h.put("one", "1");
        h.put("two", h);
        h.put(h, "3");
        h.put(h, h);
        String result = h.toString();
        assertTrue("should contain self ref", result.indexOf("(this") > -1);
    }

    /**
     * java.util.Hashtable#values()
     */
    public void test_values() {
        // Test for method java.util.Collection java.util.Hashtable.values()
        Collection c = ht10.values();
        Enumeration e = elmVector.elements();
        while (e.hasMoreElements())
            assertTrue("Returned incorrect values", c.contains(e.nextElement()));

// BEGIN Android-removed
// implementation dependent
//        assertEquals("Not synchronized",
//                "java.util.Collections$SynchronizedCollection", c.getClass().getName());
// END Android-removed

        Hashtable myHashtable = new Hashtable();
        for (int i = 0; i < 100; i++)
            myHashtable.put(new Integer(i), new Integer(i));
        Collection values = myHashtable.values();
        new Support_UnmodifiableCollectionTest(
                "Test Returned Collection From Hashtable.values()", values)
                .runTest();
        values.remove(new Integer(0));
        assertTrue(
                "Removing from the values collection should remove from the original map",
                !myHashtable.containsValue(new Integer(0)));
    }

    /**
     * Regression Test for JIRA 2181
     */
    public void test_entrySet_remove() {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("my.nonexistent.prop", "AAA");
        hashtable.put("parse.error", "BBB");
        Iterator<Map.Entry<String, String>> iterator =
                hashtable.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = iterator.next();
            final Object value = entry.getValue();
            if (value.equals("AAA")) {
                iterator.remove();
            }
        }
        assertFalse(hashtable.containsKey("my.nonexistent.prop"));
    }

    class Mock_Hashtable extends Hashtable {
        boolean flag = false;

        public Mock_Hashtable(int i) {
            super(i);
        }

        @Override
        protected void rehash() {
            flag = true;
            super.rehash();
        }

        public boolean isRehashed() {
            return flag;
        }
    }

    public void test_rehash() {
        Mock_Hashtable mht = new Mock_Hashtable(5);

        assertFalse(mht.isRehashed());
        for(int i = 0; i < 10; i++) {
            mht.put(i, "New value");
        }
        assertTrue(mht.isRehashed());
    }

    /**
     * java.util.Hashtable#elements()
     * java.util.Hashtable#keys()
     * java.util.Hashtable#keySet()
     */
    public void test_keys_elements_keySet_Exceptions() {
        Hashtable hashTable = new Hashtable();
        String key = "key";
        String value = "value";
        hashTable.put(key, value);

        Enumeration enumeration = hashTable.keys();
        assertTrue(enumeration.hasMoreElements());
        enumeration.nextElement();
        assertFalse(enumeration.hasMoreElements());

        enumeration = hashTable.elements();
        assertTrue(enumeration.hasMoreElements());
        enumeration.nextElement();
        assertFalse(enumeration.hasMoreElements());

        Iterator iterator = hashTable.keySet().iterator();
        assertTrue(iterator.hasNext());
        try {
            iterator.remove();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
        iterator.next();
        iterator.remove();
        assertFalse(iterator.hasNext());

        hashTable.clear();
        for (int i = 0; i < 10; i++) {
            hashTable.put(key + i, value + i);
        }

        enumeration = hashTable.keys();
        assertTrue(enumeration.hasMoreElements());
        for (int i = 0; i < 10; i++) {
            enumeration.nextElement();
        }
        assertFalse(enumeration.hasMoreElements());
        try {
            enumeration.nextElement();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }

        enumeration = hashTable.elements();
        assertTrue(enumeration.hasMoreElements());
        for (int i = 0; i < 10; i++) {
            enumeration.nextElement();
        }
        assertFalse(enumeration.hasMoreElements());
        try {
            enumeration.nextElement();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }

        iterator = hashTable.keySet().iterator();
        assertTrue(iterator.hasNext());
        for (int i = 0; i < 10; i++) {
            iterator.next();
        }
        assertFalse(iterator.hasNext());
        try {
            iterator.next();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

    public void test_forEach() throws Exception {
        Hashtable<String, String> ht = new Hashtable<>();
        ht.put("1", "one");
        ht.put("2", "two");
        ht.put("3", "three");
        Hashtable<String, String> output = new Hashtable<>();

        ht.forEach((k,v) -> output.put(k,v));
        assertEquals(ht, output);
    }

    public void test_forEach_NPE() throws Exception {
        Hashtable<String, String> ht = new Hashtable<>();
        try {
            ht.forEach(null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_forEach_CME() throws Exception {
        Hashtable<String, String> ht = new Hashtable<>();
        ht.put("one", "1");
        ht.put("two", "2");
        ht.put("three", "3");

        Hashtable<String, String> outputHt = new Hashtable<>();
        try {
            ht.forEach(new java.util.function.BiConsumer<String, String>() {
                    @Override
                    public void accept(String k, String v) {
                        outputHt.put(k, v);
                        ht.put("foo", v);
                    }
                });
            fail();
        } catch(ConcurrentModificationException expected) {}
        // We should get a CME and DO NOT continue forEach evaluation
        assertEquals(1, outputHt.size());
    }

    protected Hashtable hashtableClone(Hashtable s) {
        return (Hashtable) s.clone();
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {

        ht10 = new Hashtable(10);
        ht100 = new Hashtable(100);
        htfull = new Hashtable(10);
        keyVector = new Vector(10);
        elmVector = new Vector(10);

        for (int i = 0; i < 10; i++) {
            ht10.put("Key " + i, "Val " + i);
            keyVector.addElement("Key " + i);
            elmVector.addElement("Val " + i);
        }

        for (int i = 0; i < 7; i++)
            htfull.put("FKey " + i, "FVal " + i);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        ht10 = null;
        ht100 = null;
        htfull = null;
        keyVector = null;
        elmVector = null;
    }
}
