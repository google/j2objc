/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package libcore.java.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import junit.framework.TestCase;
import libcore.util.SerializationTester;
import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class OldCollectionsTest extends TestCase {

    private static final SerializableAssert comparator = new SerializableAssert() {
        public void assertDeserialized(Serializable reference, Serializable test) {
            assertSame(reference, test);
        }
    };

    /**
     * java.util.Collections#binarySearch(java.util.List,
     *        java.lang.Object, java.util.Comparator)
     */
    public void test_binarySearchLjava_util_ListLjava_lang_ObjectLjava_util_Comparator() {
        // Regression for HARMONY-94
        LinkedList<Integer> lst = new LinkedList<Integer>();
        lst.add(new Integer(30));
        Collections.sort(lst, null);
        int index = Collections.binarySearch(lst, new Integer(2), null);
        assertEquals(-1, index);

        LinkedList<String> lls = new LinkedList<String>();
        lls.add("1");
        lls.add("2");
        lls.add("3");
        lls.add("4");
        lls.add("");
        LinkedList<String> ll = lls;

        try {
            Collections.binarySearch(ll, new Integer(10), null);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    /**
     * java.util.Collections#binarySearch(java.util.List,
     *        java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void test_binarySearchLjava_util_ListLjava_lang_Object() {
        // regression for Harmony-1367
        List localList = new LinkedList();
        assertEquals(-1, Collections.binarySearch(localList, new Object()));
        localList.add(new Object());
        try {
            Collections.binarySearch(localList, new Integer(1));
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        LinkedList<String> lls = new LinkedList<String>();
        lls.add("1");
        lls.add("2");
        lls.add("3");
        lls.add("4");
        lls.add("");
        LinkedList ll = lls;

        try {
            Collections.binarySearch(ll, new Integer(10));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    /**
     * java.util.Collections#rotate(java.util.List, int)
     */
    public void test_rotateLjava_util_ListI() {
        // Regression for HARMONY-19 Rotate an *empty* list
        Collections.rotate(new ArrayList<Object>(), 25);

        // Regression for HARMONY-20
        List<String> list = new ArrayList<String>();
        list.add(0, "zero");
        list.add(1, "one");
        list.add(2, "two");
        list.add(3, "three");
        list.add(4, "four");

        Collections.rotate(list, Integer.MIN_VALUE);
        assertEquals("Rotated incorrectly at position 0, ", "three",
                list.get(0));
        assertEquals("Rotated incorrectly at position 1, ", "four",
                list.get(1));
        assertEquals("Rotated incorrectly at position 2, ", "zero",
                list.get(2));
        assertEquals("Rotated incorrectly at position 3, ", "one",
                list.get(3));
        assertEquals("Rotated incorrectly at position 4, ", "two",
                list.get(4));
    }

    /**
     * java.util.Collections#synchronizedCollection(java.util.Collection)
     */
    public void test_synchronizedCollectionLjava_util_Collection() {
        try {
            // Regression for HARMONY-93
            Collections.synchronizedCollection(null);
            fail("Assert 0: synchronizedCollection(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.Collections#synchronizedSortedMap(java.util.SortedMap)
     */
    public void test_synchronizedSortedMapLjava_util_SortedMap() {
        try {
            // Regression for HARMONY-93
            Collections.synchronizedSortedMap(null);
            fail("Assert 0: synchronizedSortedMap(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.Collections#synchronizedMap(java.util.Map)
     */
    public void test_synchronizedMapLjava_util_Map() {
        try {
            // Regression for HARMONY-93
            Collections.synchronizedMap(null);
            fail("Assert 0: synchronizedMap(map) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.Collections#synchronizedSet(java.util.Set)
     */
    public void test_synchronizedSetLjava_util_Set() {
        try {
            // Regression for HARMONY-93
            Collections.synchronizedSet(null);
            fail("Assert 0: synchronizedSet(set) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.Collections#synchronizedSortedSet(java.util.SortedSet)
     */
    public void test_synchronizedSortedSetLjava_util_SortedSet() {
        try {
            // Regression for HARMONY-93
            Collections.synchronizedSortedSet(null);
            fail("Assert 0: synchronizedSortedSet(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.Collections#unmodifiableCollection(java.util.Collection)
     */
    public void test_unmodifiableCollectionLjava_util_Collection() {
        try {
            // Regression for HARMONY-93
            Collections.unmodifiableCollection(null);
            fail("Assert 0: unmodifiableCollection(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.Collections#unmodifiableMap(java.util.Map)
     */
    public void test_unmodifiableMapLjava_util_Map() {
        try {
            // Regression for HARMONY-93
            Collections.unmodifiableMap(null);
            fail("Assert 0: unmodifiableMap(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.Collections#unmodifiableSet(java.util.Set)
     */
    public void test_unmodifiableSetLjava_util_Set() {
        try {
            // Regression for HARMONY-93
            Collections.unmodifiableSet(null);
            fail("Assert 0: unmodifiableSet(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.Collections#unmodifiableSortedMap(java.util.SortedMap)
     */
    public void test_unmodifiableSortedMapLjava_util_SortedMap() {
        try {
            // Regression for HARMONY-93
            Collections.unmodifiableSortedMap(null);
            fail("Assert 0: unmodifiableSortedMap(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.Collections#unmodifiableSortedSet(java.util.SortedSet)
     */
    public void test_unmodifiableSortedSetLjava_util_SortedSet() {
        try {
            // Regression for HARMONY-93
            Collections.unmodifiableSortedSet(null);
            fail("Assert 0: unmodifiableSortedSet(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.Collections#frequency(java.util.Collection,Object)
     */
    public void test_frequencyLjava_util_CollectionLint() {
        try {
            Collections.frequency(null, null);
            fail("Assert 0: frequency(null,<any>) must throw NPE");
        } catch (NullPointerException e) {}

        List<String> strings = Arrays.asList(new String[] { "1", "2", "3", "1", "1" });

        assertEquals("Assert 1: did not find three \"1\" strings", 3,
                Collections.frequency(strings, "1"));

        assertEquals("Assert 2: did not find one \"2\" strings", 1, Collections
                .frequency(strings, "2"));

        assertEquals("Assert 3: did not find three \"3\" strings", 1,
                Collections.frequency(strings, "3"));

        assertEquals("Assert 4: matched on null when there are none", 0,
                Collections.frequency(strings, null));

        List<Object> objects = Arrays.asList(new Object[] { new Integer(1), null, null,
                new Long(1) });

        assertEquals("Assert 5: did not find one Integer(1)", 1, Collections
                .frequency(objects, new Integer(1)));

        assertEquals("Assert 6: did not find one Long(1)", 1, Collections
                .frequency(objects, new Long(1)));

        assertEquals("Assert 7: did not find two null references", 2,
                Collections.frequency(objects, null));
    }

    /**
     * java.util.Collections#reverseOrder()
     */
    public void test_reverseOrder() {
        Comparator<String> roc = Collections.reverseOrder();
        assertNotNull("Assert 0: comparator must not be null", roc);

        assertTrue("Assert 1: comparator must implement Serializable",
                roc instanceof Serializable);

        String[] fixtureDesc = new String[] { "2", "1", "0" };
        String[] numbers = new String[] { "0", "1", "2" };
        Arrays.sort(numbers, roc);
        assertTrue("Assert 2: the arrays are not equal, the sort failed",
                Arrays.equals(fixtureDesc, numbers));
    }

    /**
     * java.util.Collections#reverseOrder(java.util.Comparator)
     */
    public void test_reverseOrderLjava_util_Comparator() {
        Comparator<String> roc = Collections
                .reverseOrder(String.CASE_INSENSITIVE_ORDER);
        assertNotNull("Assert 0: comparator must not be null", roc);

        assertTrue("Assert 1: comparator must implement Serializable",
                roc instanceof Serializable);

        String[] fixtureDesc = new String[] { "2", "1", "0" };
        String[] numbers = new String[] { "0", "1", "2" };
        Arrays.sort(numbers, roc);
        assertTrue("Assert 2: the arrays are not equal, the sort failed",
                Arrays.equals(fixtureDesc, numbers));

        roc = Collections.reverseOrder(null);
        assertNotNull("Assert 3: comparator must not be null", roc);

        assertTrue("Assert 4: comparator must implement Serializable",
                roc instanceof Serializable);

        numbers = new String[] { "0", "1", "2" };
        Arrays.sort(numbers, roc);
        assertTrue("Assert 5: the arrays are not equal, the sort failed",
                Arrays.equals(fixtureDesc, numbers));
    }

    class Mock_Collection implements Collection {
        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            return false;
        }

        public void clear() {
        }

        public boolean contains(Object o) {
            return false;
        }

        public boolean containsAll(Collection c) {
            return false;
        }

        public boolean isEmpty() {
            return false;
        }

        public Iterator iterator() {
            return null;
        }

        public boolean remove(Object o) {
            return false;
        }

        public boolean removeAll(Collection c) {
            return false;
        }

        public boolean retainAll(Collection c) {
            return false;
        }

        public int size() {
            return 0;
        }

        public Object[] toArray() {
            return null;
        }

        public Object[] toArray(Object[] a) {
            return null;
        }
    }

    class Mock_WrongCollection implements Collection {
        final String wrongElement = "Wrong element";
        public boolean add(Object o) {
            if (o.equals(wrongElement)) throw new IllegalArgumentException();
            if (o == null) throw new NullPointerException();
            return false;
        }

        public boolean addAll(Collection c) {
            return false;
        }

        public void clear() {
        }

        public boolean contains(Object o) {
            return false;
        }

        public boolean containsAll(Collection c) {
            return false;
        }

        public boolean isEmpty() {
            return false;
        }

        public Iterator iterator() {
            return null;
        }

        public boolean remove(Object o) {
            return false;
        }

        public boolean removeAll(Collection c) {
            return false;
        }

        public boolean retainAll(Collection c) {
            return false;
        }

        public int size() {
            return 0;
        }

        public Object[] toArray() {
            return null;
        }

        public Object[] toArray(Object[] a) {
            return null;
        }
    }

    public void test_AddAll() {
        List<Object> l = new ArrayList<Object>();
        assertFalse(Collections.addAll(l, new Object[] {}));
        assertTrue(l.isEmpty());
        assertTrue(Collections.addAll(l, new Object[] { new Integer(1),
                new Integer(2), new Integer(3) }));
        assertFalse(l.isEmpty());
        assertTrue(l.equals(Arrays.asList(new Object[] { new Integer(1),
                new Integer(2), new Integer(3) })));

        try {
            Collections.addAll(null,new Object[] { new Integer(1),
                    new Integer(2), new Integer(3) });
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //fail
        }

        Collection c = new Mock_Collection();
        try {
            Collections.addAll(c, new Object[] { new Integer(1),
                    new Integer(2), new Integer(3) });
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
            //expected
        }

        c = new Mock_WrongCollection ();

        try {
            Collections.addAll(c, new String[] { "String",
                    "Correct element", null });
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //fail
        }

        try {
            Collections.addAll(c, new String[] { "String",
                    "Wrong element", "Correct element" });
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //fail
        }

        Collections.addAll(c, new String[] { "String",
                "", "Correct element" });
    }
    public void test_Disjoint() {
        Object[] arr1 = new Object[10];
        for (int i = 0; i < arr1.length; i++) {
            arr1[i] = new Integer(i);
        }
        Object[] arr2 = new Object[20];
        for (int i = 0; i < arr2.length; i++) {
            arr2[i] = new Integer(100 + i);
        }
        Collection<Object> c1 = new ArrayList<Object>();
        Collection<Object> c2 = new ArrayList<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        c1 = new LinkedList<Object>();
        c2 = new LinkedList<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        c1 = new TreeSet<Object>();
        c2 = new TreeSet<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        c1 = new HashSet<Object>();
        c2 = new HashSet<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        c1 = new LinkedList<Object>();
        c2 = new TreeSet<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        c1 = new Vector<Object>();
        c2 = new HashSet<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        try {
            Collections.disjoint(c1, null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }

        try {
            Collections.disjoint(null, c2);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.Collections.EmptyList#readResolve()
     */
    public void test_EmptyList_readResolve() throws Exception {
        SerializationTest.verifySelf(Collections.EMPTY_LIST, comparator);
    }

    /**
     * java.util.Collections.EmptyMap#readResolve()
     */
    public void test_EmptyMap_readResolve() throws Exception {
        SerializationTest.verifySelf(Collections.EMPTY_MAP, comparator);
    }

    /**
     * java.util.Collections.EmptySet#readResolve()
     */
    public void test_EmptySet_readResolve() throws Exception {
        SerializationTest.verifySelf(Collections.EMPTY_SET, comparator);
    }

    public void test_checkedCollectionSerializationCompatibility() throws Exception {
        String s = "aced0005737200276a6176612e7574696c2e436f6c6c656374696f6e73244368"
                + "65636b6564436f6c6c656374696f6e15e96dfd18e6cc6f0200034c00016374001"
                + "64c6a6176612f7574696c2f436f6c6c656374696f6e3b4c000474797065740011"
                + "4c6a6176612f6c616e672f436c6173733b5b00167a65726f4c656e677468456c6"
                + "56d656e7441727261797400135b4c6a6176612f6c616e672f4f626a6563743b78"
                + "707372001e6a6176612e7574696c2e436f6c6c656374696f6e7324456d7074795"
                + "3657415f5721db403cb280200007870767200106a6176612e6c616e672e537472"
                + "696e67a0f0a4387a3bb342020000787070";
        assertSerialized(Collections.checkedCollection(
                Collections.<String>emptySet(), String.class), s, false);
    }
    public void test_checkedListRandomAccessSerializationCompatibility() throws Exception {
        String s = "aced00057372002d6a6176612e7574696c2e436f6c6c656374696f6e73244368"
                + "65636b656452616e646f6d4163636573734c69737416bc0e55a2d7f2f10200007"
                + "87200216a6176612e7574696c2e436f6c6c656374696f6e7324436865636b6564"
                + "4c69737400e7ce7692c45f7c0200014c00046c6973747400104c6a6176612f757"
                + "4696c2f4c6973743b787200276a6176612e7574696c2e436f6c6c656374696f6e"
                + "7324436865636b6564436f6c6c656374696f6e15e96dfd18e6cc6f0200034c000"
                + "1637400164c6a6176612f7574696c2f436f6c6c656374696f6e3b4c0004747970"
                + "657400114c6a6176612f6c616e672f436c6173733b5b00167a65726f4c656e677"
                + "468456c656d656e7441727261797400135b4c6a6176612f6c616e672f4f626a65"
                + "63743b7870737200136a6176612e7574696c2e41727261794c6973747881d21d9"
                + "9c7619d03000149000473697a6578700000000077040000000a78767200106a61"
                + "76612e6c616e672e537472696e67a0f0a4387a3bb34202000078707071007e0009";
        assertSerialized(Collections.checkedList(new ArrayList<String>(), String.class), s, true);
    }
    public void test_checkedListSerializationCompatibility() throws Exception {
        String s = "aced0005737200216a6176612e7574696c2e436f6c6c656374696f6e73244368"
                + "65636b65644c69737400e7ce7692c45f7c0200014c00046c6973747400104c6a6"
                + "176612f7574696c2f4c6973743b787200276a6176612e7574696c2e436f6c6c65"
                + "6374696f6e7324436865636b6564436f6c6c656374696f6e15e96dfd18e6cc6f0"
                + "200034c0001637400164c6a6176612f7574696c2f436f6c6c656374696f6e3b4c"
                + "0004747970657400114c6a6176612f6c616e672f436c6173733b5b00167a65726"
                + "f4c656e677468456c656d656e7441727261797400135b4c6a6176612f6c616e67"
                + "2f4f626a6563743b7870737200146a6176612e7574696c2e4c696e6b65644c697"
                + "3740c29535d4a608822030000787077040000000078767200106a6176612e6c61"
                + "6e672e537472696e67a0f0a4387a3bb34202000078707071007e0008";
        assertSerialized(Collections.checkedList(new LinkedList<String>(), String.class), s, true);
    }
    public void test_checkedSetSerializationCompatibility() throws Exception {
        String s = "aced0005737200206a6176612e7574696c2e436f6c6c656374696f6e73244368"
                + "65636b656453657441249ba27ad9ffab020000787200276a6176612e7574696c2"
                + "e436f6c6c656374696f6e7324436865636b6564436f6c6c656374696f6e15e96d"
                + "fd18e6cc6f0200034c0001637400164c6a6176612f7574696c2f436f6c6c65637"
                + "4696f6e3b4c0004747970657400114c6a6176612f6c616e672f436c6173733b5b"
                + "00167a65726f4c656e677468456c656d656e7441727261797400135b4c6a61766"
                + "12f6c616e672f4f626a6563743b7870737200116a6176612e7574696c2e486173"
                + "68536574ba44859596b8b7340300007870770c000000103f40000000000000787"
                + "67200106a6176612e6c616e672e537472696e67a0f0a4387a3bb3420200007870"
                + "70";
        assertSerialized(Collections.checkedSet(new HashSet<String>(), String.class), s, true);
    }
    public void test_checkedMapSerializationCompatibility() throws Exception {
        String s = "aced0005737200206a6176612e7574696c2e436f6c6c656374696f6e73244368"
                + "65636b65644d61704fb2bcdf0d1863680200054c00076b6579547970657400114"
                + "c6a6176612f6c616e672f436c6173733b4c00016d74000f4c6a6176612f757469"
                + "6c2f4d61703b4c000976616c75655479706571007e00015b00127a65726f4c656"
                + "e6774684b657941727261797400135b4c6a6176612f6c616e672f4f626a656374"
                + "3b5b00147a65726f4c656e67746856616c7565417272617971007e00037870767"
                + "200106a6176612e6c616e672e537472696e67a0f0a4387a3bb342020000787073"
                + "7200116a6176612e7574696c2e486173684d61700507dac1c31660d1030002460"
                + "00a6c6f6164466163746f724900097468726573686f6c6478703f400000000000"
                + "0c770800000010000000007871007e00067070";
        assertSerialized(Collections.checkedMap(
                new HashMap<String, String>(), String.class, String.class), s);
    }
    public void test_checkedSortedSetSerializationCompatibility() throws Exception {
        String s = "aced0005737200266a6176612e7574696c2e436f6c6c656374696f6e73244368"
                + "65636b6564536f72746564536574163406ba7362eb0f0200014c0002737374001"
                + "54c6a6176612f7574696c2f536f727465645365743b787200206a6176612e7574"
                + "696c2e436f6c6c656374696f6e7324436865636b656453657441249ba27ad9ffa"
                + "b020000787200276a6176612e7574696c2e436f6c6c656374696f6e7324436865"
                + "636b6564436f6c6c656374696f6e15e96dfd18e6cc6f0200034c0001637400164"
                + "c6a6176612f7574696c2f436f6c6c656374696f6e3b4c0004747970657400114c"
                + "6a6176612f6c616e672f436c6173733b5b00167a65726f4c656e677468456c656"
                + "d656e7441727261797400135b4c6a6176612f6c616e672f4f626a6563743b7870"
                + "737200116a6176612e7574696c2e54726565536574dd98509395ed875b0300007"
                + "8707077040000000078767200106a6176612e6c616e672e537472696e67a0f0a4"
                + "387a3bb34202000078707071007e0009";
        assertSerialized(Collections.checkedSortedSet(new TreeSet<String>(), String.class), s, true);
    }
    public void test_checkedSortedMapSerializationCompatibility() throws Exception {
        String s = "aced0005737200266a6176612e7574696c2e436f6c6c656374696f6e73244368"
                + "65636b6564536f727465644d617016332c973afe036e0200014c0002736d74001"
                + "54c6a6176612f7574696c2f536f727465644d61703b787200206a6176612e7574"
                + "696c2e436f6c6c656374696f6e7324436865636b65644d61704fb2bcdf0d18636"
                + "80200054c00076b6579547970657400114c6a6176612f6c616e672f436c617373"
                + "3b4c00016d74000f4c6a6176612f7574696c2f4d61703b4c000976616c7565547"
                + "9706571007e00035b00127a65726f4c656e6774684b657941727261797400135b"
                + "4c6a6176612f6c616e672f4f626a6563743b5b00147a65726f4c656e677468566"
                + "16c7565417272617971007e00057870767200106a6176612e6c616e672e537472"
                + "696e67a0f0a4387a3bb3420200007870737200116a6176612e7574696c2e54726"
                + "5654d61700cc1f63e2d256ae60300014c000a636f6d70617261746f727400164c"
                + "6a6176612f7574696c2f436f6d70617261746f723b78707077040000000078710"
                + "07e0008707071007e000b";
        assertSerialized(Collections.checkedSortedMap(
                new TreeMap<String, String>(), String.class, String.class), s);
    }

    private void assertSerialized(Collection<?> collection, String s, final boolean definesEquals) {
        new SerializationTester<Collection<?>>(collection, s) {
            @SuppressWarnings("unchecked")
            @Override protected void verify(Collection<?> deserialized) throws Exception {
                try {
                    ((Collection) deserialized).add(Boolean.TRUE);
                    fail();
                } catch (ClassCastException expected) {
                }
            }
            @Override protected boolean equals(Collection<?> a, Collection<?> b) {
                boolean equal = definesEquals
                        ? a.equals(b)
                        : Arrays.equals(a.toArray(), b.toArray());
                return equal
                        && (a instanceof SortedSet == b instanceof SortedSet)
                        && (a instanceof RandomAccess == b instanceof RandomAccess);
            }
        }.test();
    }

    private void assertSerialized(Map<?, ?> map, String s) {
        new SerializationTester<Map<?, ?>>(map, s) {
            @SuppressWarnings("unchecked")
            @Override protected void verify(Map<?, ?> deserialized) throws Exception {
                try {
                    ((Map) deserialized).put(Boolean.TRUE, "a");
                    fail();
                } catch (ClassCastException expected) {
                }
                try {
                    ((Map) deserialized).put("a", Boolean.TRUE);
                    fail();
                } catch (ClassCastException expected) {
                }
            }
            @Override protected boolean equals(Map<?, ?> a, Map<?, ?> b) {
                return super.equals(a, b)
                        && (a instanceof SortedMap == b instanceof SortedMap);
            }
        }.test();
    }

    public void test_checkedCollectionLjava_util_CollectionLjava_lang_Class() {
        ArrayList al = new ArrayList<Integer>();

        Collection c = Collections.checkedCollection(al, Integer.class);

        c.add(new Integer(1));

        try {
            c.add(new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    public void test_checkedListLjava_util_ListLjava_lang_Class() {
        ArrayList al = new ArrayList<Integer>();

        List l = Collections.checkedList(al, Integer.class);

        l.add(new Integer(1));

        try {
            l.add(new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    public void test_checkedMapLjava_util_MapLjava_lang_ClassLjava_lang_Class() {
        HashMap hm = new HashMap<Integer, String>();

        Map m = Collections.checkedMap(hm, Integer.class, String.class);

        m.put(1, "one");
        m.put(2, "two");

        try {
            m.put("wron key", null);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }

        try {
            m.put(3, new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    public void test_checkedSetLjava_util_SetLjava_lang_Class() {
        HashSet hs = new HashSet<Integer>();

        Set s = Collections.checkedSet(hs, Integer.class);

        s.add(new Integer(1));

        try {
            s.add(new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    public void test_checkedSortedMapLjava_util_SortedMapLjava_lang_ClassLjava_lang_Class() {
        TreeMap tm = new TreeMap<Integer, String>();

        SortedMap sm = Collections.checkedSortedMap(tm, Integer.class, String.class);

        sm.put(1, "one");
        sm.put(2, "two");

        try {
            sm.put("wron key", null);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }

        try {
            sm.put(3, new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    public void test_checkedSortedSetLjava_util_SortedSetLjava_lang_Class() {
        TreeSet ts = new TreeSet<Integer>();

        SortedSet ss = Collections.checkedSortedSet(ts, Integer.class);

        ss.add(new Integer(1));

        try {
            ss.add(new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    public void test_emptyList() {
        List<String> ls = Collections.emptyList();
        List<Integer> li = Collections.emptyList();

        assertTrue(ls.equals(li));
        assertTrue(li.equals(Collections.EMPTY_LIST));
    }

    public void test_emptyMap() {
        Map<Integer, String> mis = Collections.emptyMap();
        Map<String, Integer> msi = Collections.emptyMap();

        assertTrue(mis.equals(msi));
        assertTrue(msi.equals(Collections.EMPTY_MAP));
    }

    public void test_emptySet() {
        Set<String> ss = Collections.emptySet();
        Set<Integer> si = Collections.emptySet();

        assertTrue(ss.equals(si));
        assertTrue(si.equals(Collections.EMPTY_SET));
    }
}
