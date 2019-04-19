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

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;
import tests.support.Support_CollectionTest;
import tests.support.Support_ListTest;
import tests.support.Support_SetTest;
import tests.support.Support_UnmodifiableCollectionTest;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class CollectionsTest extends junit.framework.TestCase {

    private LinkedList ll;

    private LinkedList myll;

    private LinkedList reversedLinkedList;

    private LinkedList myReversedLinkedList;

    private Set s;

    private Set mys;

    private HashMap hm;

    private Integer[] objArray;

    private Object[] myobjArray;

    public static class ReversedMyIntComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return -((MyInt) o1).compareTo((MyInt) o2);
        }

        public int equals(Object o1, Object o2) {
            return ((MyInt) o1).compareTo((MyInt) o2);
        }
    }

    public static class SynchCollectionChecker implements Runnable {
        Collection col;

        int colSize;

        int totalToRun;

        boolean offset;

        volatile int numberOfChecks = 0;

        boolean result = true;

        ArrayList normalCountingList;

        ArrayList offsetCountingList;

        public void run() {
            // ensure the list either contains the numbers from 0 to size-1 or
            // the numbers from size to 2*size -1
            while (numberOfChecks < totalToRun) {
                synchronized (col) {
                    if (!(col.isEmpty() || col.containsAll(normalCountingList) || col
                            .containsAll(offsetCountingList)))
                        result = false;
                    col.clear();
                }
                if (offset)
                    col.addAll(offsetCountingList);
                else
                    col.addAll(normalCountingList);
                numberOfChecks++;
            }
        }

        public SynchCollectionChecker(Collection c, boolean offset,
                int totalChecks) {
            // The collection to test, whether to offset the filler values by
            // size or not, and the min number of iterations to run
            totalToRun = totalChecks;
            col = c;
            colSize = c.size();
            normalCountingList = new ArrayList(colSize);
            offsetCountingList = new ArrayList(colSize);
            for (int i = 0; i < colSize; i++)
                normalCountingList.add(new Integer(i));
            for (int i = 0; i < colSize; i++)
                offsetCountingList.add(new Integer(i + colSize));
            col.clear();
            if (offset)
                col.addAll(offsetCountingList);
            else
                col.addAll(normalCountingList);
        }

        public boolean offset() {
            // answer true iff the list is filled with a counting sequence
            // starting at the value size to 2*size - 1
            // else the list with be filled starting at 0 to size - 1
            return offset;
        }

        public boolean getResult() {
            // answer true iff no corruption has been found in the collection
            return result;
        }

        public int getNumberOfChecks() {
            // answer the number of checks that have been performed on the list
            return numberOfChecks;
        }
    }

    public static class SynchMapChecker implements Runnable {
        Map map;

        int mapSize;

        int totalToRun;

        boolean offset;

        volatile int numberOfChecks = 0;

        boolean result = true;

        Map normalCountingMap;

        Map offsetCountingMap;

        public void run() {
            Object firstNormalValue = normalCountingMap.get(new Integer(0));
            Object lastNormalValue = normalCountingMap.get(new Integer(
                    mapSize - 1));
            Object firstOffsetValue = offsetCountingMap
                    .get(new Integer(mapSize));
            Object lastOffsetValue = offsetCountingMap.get(new Integer(
                    2 * mapSize - 1));
            // ensure the list either contains the numbers from 0 to size-1 or
            // the numbers from size to 2*size -1
            while (numberOfChecks < totalToRun) {
                synchronized (map) {
                    if (!(map.isEmpty()
                            || (map.containsValue(firstNormalValue) && map
                            .containsValue(lastNormalValue)) || (map
                            .containsValue(firstOffsetValue) && map
                            .containsValue(lastOffsetValue))))
                        result = false;
                    map.clear();
                }
                if (offset)
                    map.putAll(offsetCountingMap);
                else
                    map.putAll(normalCountingMap);
                numberOfChecks++;
            }
        }

        public SynchMapChecker(Map m, boolean offset, int totalChecks) {
            // The collection to test, whether to offset the filler values by
            // size or not, and the min number of iterations to run
            Integer myInt;
            totalToRun = totalChecks;
            map = m;
            mapSize = m.size();
            normalCountingMap = new HashMap(mapSize);
            offsetCountingMap = new HashMap(mapSize);
            for (int i = 0; i < mapSize; i++) {
                myInt = new Integer(i);
                normalCountingMap.put(myInt, myInt);
            }
            for (int i = 0; i < mapSize; i++) {
                myInt = new Integer(i + mapSize);
                offsetCountingMap.put(myInt, myInt);
            }
            map.clear();
            if (offset)
                map.putAll(offsetCountingMap);
            else
                map.putAll(normalCountingMap);
        }

        public boolean offset() {
            // answer true iff the list is filled with a counting sequence
            // starting at the value size to 2*size - 1
            // else the list with be filled starting at 0 to size - 1
            return offset;
        }

        public boolean getResult() {
            // answer true iff no corruption has been found in the collection
            return result;
        }

        public int getNumberOfChecks() {
            // answer the number of checks that have been performed on the list
            return numberOfChecks;
        }
    }

    static class MyInt {
        int data;

        public MyInt(int value) {
            data = value;
        }

        public int compareTo(MyInt object) {
            return data > object.data ? 1 : (data < object.data ? -1 : 0);
        }
    }

    public void test_binarySearchLjava_util_ListLjava_lang_Object() {
        // Test for method int
        // java.util.Collections.binarySearch(java.util.List, java.lang.Object)
        // assumes ll is sorted and has no duplicate keys
        final int llSize = ll.size();
        // Ensure a NPE is thrown if the list is NULL
        try {
            Collections.binarySearch(null, new Object());
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        for (int i = 0; i < llSize; i++) {
            assertEquals("Returned incorrect binary search item position", ll
                    .get(i), ll.get(Collections.binarySearch(ll, ll
                    .get(i))));
        }
    }

    public void test_binarySearchLjava_util_ListLjava_lang_ObjectLjava_util_Comparator() {
        // Test for method int
        // java.util.Collections.binarySearch(java.util.List, java.lang.Object,
        // java.util.Comparator)
        // assumes reversedLinkedList is sorted in reversed order and has no
        // duplicate keys
        final int rSize = myReversedLinkedList.size();
        ReversedMyIntComparator comp = new ReversedMyIntComparator();
        // Ensure a NPE is thrown if the list is NULL
        try {
            Collections.binarySearch(null, new Object(), comp);
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        for (int i = 0; i < rSize; i++) {
            assertEquals(
                    "Returned incorrect binary search item position using custom comparator",
                    myReversedLinkedList.get(i), myReversedLinkedList
                    .get(Collections.binarySearch(myReversedLinkedList,
                            myReversedLinkedList.get(i), comp)));
        }
    }

    class Mock_ArrayList extends ArrayList {
        @Override
        public
        Object set (int index, Object o){
            throw new UnsupportedOperationException();
        }
    }

    public void test_copyLjava_util_ListLjava_util_List() {
        // Test for method void java.util.Collections.copy(java.util.List,
        // java.util.List)
        // Ensure a NPE is thrown if the list is NULL
        try {
            Collections.copy(null, ll);
            fail("Expected NullPointerException for null list first parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        try {
            Collections.copy(ll, null);
            fail("Expected NullPointerException for null list second parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        final int llSize = ll.size();
        ll.set(25, null);
        ArrayList al = new ArrayList();
        Integer extraElement = new Integer(1);
        Integer extraElement2 = new Integer(2);
        al.addAll(myReversedLinkedList);
        al.add(extraElement);
        al.add(extraElement2);
        Collections.copy(al, ll);
        for (int i = 0; i < llSize; i++) {
            assertEquals("Elements do not match after copying collection", ll
                    .get(i), al.get(i));
        }
        assertTrue("Elements after copied elements affected by copy",
                extraElement == al.get(llSize)
                        && extraElement2 == al.get(llSize + 1));

        ArrayList ar1 = new ArrayList();
        ArrayList ar2 = new ArrayList();

        int i;

        for(i = 0; i < 5; i ++) {
            ar2.add(new Integer(i));
        }

        for(i = 0; i < 10; i ++) {
            ar1.add(new Integer(i));
        }

        try {
            Collections.copy(ar2, ar1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }

        Mock_ArrayList mal1 = new Mock_ArrayList();
        Mock_ArrayList mal2 = new Mock_ArrayList();

        for(i = 0; i < 10; i ++) {
            mal1.add(new Integer(i));
            mal2.add(new Integer(10 - i));
        }

        try {
            Collections.copy(mal1, mal2);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
            //expected
        }
    }

    public void test_copy_check_index() {
        ArrayList a1 = new ArrayList();
        a1.add("one");
        a1.add("two");

        ArrayList a2 = new ArrayList();
        a2.add("aa");

        try {
            Collections.copy(a2, a1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //Expected
        }

        assertEquals("aa", a2.get(0));
    }

    public void test_enumerationLjava_util_Collection() {
        // Test for method java.util.Enumeration
        // java.util.Collections.enumeration(java.util.Collection)
        TreeSet ts = new TreeSet();
        ts.addAll(s);
        Enumeration e = Collections.enumeration(ts);
        int count = 0;
        while (e.hasMoreElements()) {
            assertEquals("Returned incorrect enumeration", e.nextElement(),
                    objArray[count++]);
        }
        assertEquals("Enumeration missing elements: " + count, objArray.length,
                count);
    }

    public void test_fillLjava_util_ListLjava_lang_Object() {
        // Test for method void java.util.Collections.fill(java.util.List,
        // java.lang.Object)
        try {
            Collections.fill(null, new Object());
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        final int size = ll.size();
        Collections.fill(ll, "k");
        assertEquals("Fill modified list size", size, ll.size());
        Iterator i = ll.iterator();
        while (i.hasNext())
            assertEquals("Failed to fill elements", "k", i.next());

        Collections.fill(ll, null);
        assertEquals("Fill with nulls modified list size", size, ll.size());
        i = ll.iterator();
        while (i.hasNext())
            assertNull("Failed to fill with nulls", i.next());

        Mock_ArrayList mal = new Mock_ArrayList();

        mal.add("one");
        mal.add("two");

        try {
            Collections.fill(mal, "value");
            fail("UnsupportedOperationException ecpected");
        } catch (UnsupportedOperationException e) {
            //expected
        }
    }

    public void test_maxLjava_util_Collection() {
        // Test for method java.lang.Object
        // java.util.Collections.max(java.util.Collection)
        // assumes s, objArray are sorted
        assertEquals("Returned incorrect max element", Collections.max(s),
                objArray[objArray.length - 1]);

        ArrayList al = new ArrayList();

        try {
            Collections.max(al);
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
            //expected
        }

        al.add("String");
        al.add(new Integer(1));
        al.add(new Double(3.14));

        try {
            Collections.max(al);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    public void test_maxLjava_util_CollectionLjava_util_Comparator() {
        // Test for method java.lang.Object
        // java.util.Collections.max(java.util.Collection, java.util.Comparator)
        // assumes s, objArray are sorted

        // With this custom (backwards) comparator the 'max' element should be
        // the smallest in the list
        assertEquals("Returned incorrect max element using custom comparator",
                Collections.max(mys, new ReversedMyIntComparator()),
                myobjArray[0]);
    }

    public void test_minLjava_util_Collection() {
        // Test for method java.lang.Object
        // java.util.Collections.min(java.util.Collection)
        // assumes s, objArray are sorted
        assertEquals("Returned incorrect min element", Collections.min(s),
                objArray[0]);
    }

    public void test_minLjava_util_CollectionLjava_util_Comparator() {
        // Test for method java.lang.Object
        // java.util.Collections.min(java.util.Collection, java.util.Comparator)
        // assumes s, objArray are sorted

        // With this custom (backwards) comparator the 'min' element should be
        // the largest in the list
        assertEquals("Returned incorrect min element using custom comparator",
                Collections.min(mys, new ReversedMyIntComparator()),
                myobjArray[objArray.length - 1]);
    }

    public void test_nCopiesILjava_lang_Object() {
        // Test for method java.util.List java.util.Collections.nCopies(int,
        // java.lang.Object)
        Object o = new Object();
        List l = Collections.nCopies(100, o);
        Iterator iterator = l.iterator();
        Object first = iterator.next();
        assertEquals("Returned list consists of copies not refs", first, o);
        assertEquals("Returned list of incorrect size", 100, l.size());
        assertTrue("Contains", l.contains(o));
        assertFalse("Contains null", l.contains(null));
        assertFalse("null nCopies contains", Collections.nCopies(2, null)
                .contains(o));
        assertTrue("null nCopies contains null", Collections.nCopies(2, null)
                .contains(null));
        l = Collections.nCopies(20, null);
        iterator = l.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            assertTrue("List is too large", i < 20);
            assertNull("Element should be null: " + i, iterator.next());
        }
        try {
            l.add(o);
            fail("Returned list is not immutable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        try {
            Collections.nCopies(-2, new HashSet());
            fail("nCopies with negative arg didn't throw IAE");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void test_reverseLjava_util_List() {
        // Test for method void java.util.Collections.reverse(java.util.List)
        try {
            Collections.reverse(null);
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        Collections.reverse(ll);
        Iterator i = ll.iterator();
        int count = objArray.length - 1;
        while (i.hasNext()) {
            assertEquals("Failed to reverse collection", objArray[count], i
                    .next());
            --count;
        }
        ArrayList myList = new ArrayList();
        myList.add(null);
        myList.add(new Integer(20));
        Collections.reverse(myList);
        assertEquals("Did not reverse correctly--first element is: "
                + myList.get(0), new Integer(20), myList.get(0));
        assertNull("Did not reverse correctly--second element is: "
                + myList.get(1), myList.get(1));
    }

    public void test_reverseOrder() {
        // Test for method java.util.Comparator
        // java.util.Collections.reverseOrder()
        // assumes no duplicates in ll
        Comparator comp = Collections.reverseOrder();
        LinkedList list2 = new LinkedList(ll);
        Collections.sort(list2, comp);
        final int llSize = ll.size();
        for (int i = 0; i < llSize; i++)
            assertEquals("New comparator does not reverse sorting order", list2
                    .get(llSize - i - 1), ll.get(i));
    }

    public void test_shuffleLjava_util_List() {
        // Test for method void java.util.Collections.shuffle(java.util.List)
        // Assumes ll is sorted and has no duplicate keys and is large ( > 20
        // elements)

        // test shuffling a Sequential Access List
        try {
            Collections.shuffle(null);
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        ArrayList al = new ArrayList();
        al.addAll(ll);
        testShuffle(al, "Sequential Access", false);

        // test shuffling a Random Access List
        LinkedList ll2 = new LinkedList();
        ll2.addAll(ll);
        testShuffle(ll2, "Random Access", false);
    }

    public void testShuffleRandomAccessWithSeededRandom() {
        List<String> list = Arrays.asList("A", "B", "C", "D", "E", "F", "G");
        Collections.shuffle(list, new Random(0));
        assertEquals(Arrays.asList("B", "A", "D", "C", "G", "E", "F"), list);
    }

    public void testShuffleWithSeededRandom() {
        List<String> list = new LinkedList<String>(Arrays.asList(
                "A", "B", "C", "D", "E", "F", "G"));
        Collections.shuffle(list, new Random(0));
        assertEquals(Arrays.asList("B", "A", "D", "C", "G", "E", "F"), list);
    }

    private void testShuffle(List list, String type, boolean random) {
        boolean sorted = true;
        boolean allMatch = true;
        int index = 0;
        final int size = list.size();

        if (random)
            Collections.shuffle(list);
        else
            Collections.shuffle(list, new Random(200));

        for (int i = 0; i < size - 1; i++) {
            if (((Integer) list.get(i)).compareTo((Integer) list.get(i + 1)) > 0) {
                sorted = false;
            }
        }
        assertFalse("Shuffling sorted " + type
                + " list resulted in sorted list (should be unlikely)", sorted);
        for (int i = 0; i < 20; i++) {
            index = 30031 * i % (size + 1); // 30031 is a large prime
            if (list.get(index) != ll.get(index))
                allMatch = false;
        }
        assertFalse("Too many element positions match in shuffled " + type
                + " list", allMatch);
    }

    public void test_shuffleLjava_util_ListLjava_util_Random() {
        // Test for method void java.util.Collections.shuffle(java.util.List,
        // java.util.Random)
        // Assumes ll is sorted and has no duplicate keys and is large ( > 20
        // elements)

        // test shuffling a Sequential Access List
        try {
            Collections.shuffle(null, new Random(200));
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Excepted
        }
        ArrayList al = new ArrayList();
        al.addAll(ll);
        testShuffle(al, "Sequential Access", true);

        // test shuffling a Random Access List
        LinkedList ll2 = new LinkedList();
        ll2.addAll(ll);
        testShuffle(ll2, "Random Access", true);

        List l = new ArrayList();
        l.add('a');
        l.add('b');
        l.add('c');
        Collections.shuffle(l, new Random(12345678921L));
        assertEquals("acb", l.get(0).toString() + l.get(1) + l.get(2));
    }

    public void test_singletonLjava_lang_Object() {
        // Test for method java.util.Set
        // java.util.Collections.singleton(java.lang.Object)
        Object o = new Object();
        Set single = Collections.singleton(o);
        assertEquals("Wrong size", 1, single.size());
        assertTrue("Contains", single.contains(o));
        assertFalse("Contains null", single.contains(null));
        assertFalse("null nCopies contains", Collections.singleton(null)
                .contains(o));
        assertTrue("null nCopies contains null", Collections.singleton(null)
                .contains(null));
        try {
            single.add("l");
            fail("Allowed modification of singleton");
        } catch (UnsupportedOperationException e) {
            // Excepted
        }
    }

    public void test_sortLjava_util_List() {
        // Test for method void java.util.Collections.sort(java.util.List)
        // assumes no duplicate keys in ll
        final int llSize = ll.size();
        final int rllSize = reversedLinkedList.size();
        try {
            Collections.sort((List) null);
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        Collections.shuffle(ll);
        Collections.sort(ll);
        Collections.sort(reversedLinkedList);
        for (int i = 0; i < llSize - 1; i++) {
            assertTrue(
                    "Sorting shuffled list resulted in unsorted list",
                    ((Integer) ll.get(i)).compareTo((Integer) ll.get(i + 1)) < 0);
        }

        for (int i = 0; i < rllSize - 1; i++) {
            assertTrue("Sorting reversed list resulted in unsorted list",
                    ((Integer) reversedLinkedList.get(i))
                            .compareTo((Integer) reversedLinkedList.get(i + 1)) < 0);
        }
    }

    public void test_sortLjava_util_ListLjava_util_Comparator() {
        // Test for method void java.util.Collections.sort(java.util.List,
        // java.util.Comparator)
        Comparator comp = new ReversedMyIntComparator();
        try {
            Collections.sort(null, comp);
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        Collections.shuffle(myll);
        Collections.sort(myll, comp);
        final int llSize = myll.size();

        for (int i = 0; i < llSize - 1; i++) {
            assertTrue(
                    "Sorting shuffled list with custom comparator resulted in unsorted list",
                    ((MyInt) myll.get(i)).compareTo((MyInt) myll
                            .get(i + 1)) >= 0);
        }

        ArrayList al = new ArrayList();

        al.add("String");
        al.add(new Integer(1));
        al.add(new Double(3.14));

        try {
            Collections.sort(al, comp);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }

        List mal = new AbstractList() {
            private final List delegate = Arrays.asList(new MyInt(1), new MyInt(2));
            @Override public Object get(int index) { return delegate.get(index); }
            @Override public int size() { return delegate.size(); }
        };

        try {
            Collections.sort(mal, comp);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
            //expected
        }
    }

    public void test_swapLjava_util_ListII() {
        // Test for method swap(java.util.List, int, int)

        LinkedList smallList = new LinkedList();
        for (int i = 0; i < 10; i++) {
            smallList.add(objArray[i]);
        }

        // test exception cases
        try {
            Collections.swap(smallList, -1, 6);
            fail("Expected IndexOutOfBoundsException for -1");
        } catch (IndexOutOfBoundsException e) {
            //Expected
        }

        try {
            Collections.swap(smallList, 6, -1);
            fail("Expected IndexOutOfBoundsException for -1");
        } catch (IndexOutOfBoundsException e) {
            //Expected
        }

        try {
            Collections.swap(smallList, 6, 11);
            fail("Expected IndexOutOfBoundsException for 11");
        } catch (IndexOutOfBoundsException e) {
            //Expected
        }

        try {
            Collections.swap(smallList, 11, 6);
            fail("Expected IndexOutOfBoundsException for 11");
        } catch (IndexOutOfBoundsException e) {
            //Expected
        }

        // Ensure a NPE is thrown if the list is NULL
        try {
            Collections.swap(null, 1, 1);
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }

        // test with valid parameters
        Collections.swap(smallList, 4, 7);
        assertEquals("Didn't Swap the element at position 4 ", new Integer(7),
                smallList.get(4));
        assertEquals("Didn't Swap the element at position 7 ", new Integer(4),
                smallList.get(7));

        // make sure other elements didn't get swapped by mistake
        for (int i = 0; i < 10; i++) {
            if (i != 4 && i != 7)
                assertEquals("shouldn't have swapped the element at position "
                        + i, new Integer(i), smallList.get(i));
        }
    }

    public void test_replaceAllLjava_util_ListLjava_lang_ObjectLjava_lang_Object() {
        // Test for method replaceAll(java.util.List, java.lang.Object,
        // java.lang.Object)

        String string1 = "A-B-C-D-E-S-JF-SUB-G-H-I-J-SUBL-K-L-LIST-M-N--S-S-O-SUBLIS-P-Q-R-SUBLIST-S-T-U-V-W-X-Y-Z";
        char[] chars = string1.toCharArray();
        List list = new ArrayList();
        for (int i = 0; i < chars.length; i++) {
            list.add(new Character(chars[i]));
        }

        try {
            Collections.replaceAll(null, new Object(), new Object());
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }

        // test replace for an element that is not in the list
        boolean result = Collections.replaceAll(list, new Character('1'),
                new Character('Z'));
        assertFalse("Test1: Collections.replaceAll() returned wrong result",
                result);
        assertEquals("Test2 : ReplaceAll modified the list incorrectly",
                string1, getString(list));

        // test replace for an element that is in the list
        result = Collections.replaceAll(list, new Character('S'),
                new Character('K'));
        assertTrue("Test3: Collections.replaceAll() returned wrong result",
                result);
        assertEquals("Test4: ReplaceAll modified the list incorrectly",
                (string1 = string1.replace('S', 'K')), getString(list));

        // test replace for the last element in the list
        result = Collections.replaceAll(list, new Character('Z'),
                new Character('N'));
        assertTrue("Test5: Collections.replaceAll() returned wrong result",
                result);
        assertEquals("Test6: ReplaceAll modified the list incorrectly",
                (string1 = string1.replace('Z', 'N')), getString(list));

        // test replace for the first element in the list
        result = Collections.replaceAll(list, new Character('A'),
                new Character('B'));
        assertTrue("Test7: Collections.replaceAll() returned wrong result",
                result);
        assertEquals("Test8: ReplaceAll modified the list incorrectly",
                (string1 = string1.replace('A', 'B')), getString(list));

        // test replacing elements with null
        LinkedList smallList = new LinkedList();
        for (int i = 0; i < 10; i++) {
            smallList.add(objArray[i]);
        }
        smallList.set(4, new Integer(5));
        result = Collections.replaceAll(smallList, new Integer(5), null);
        assertTrue("Test9: Collections.replaceAll() returned wrong result",
                result);
        for (int i = 0; i < smallList.size(); i++) {
            if (i == 4 || i == 5)
                assertSame("Test9: ReplaceAll didn't replace element at " + i,
                        null, smallList.get(i));
            else
                assertEquals(
                        "Test9: ReplaceAll shouldn't have replaced element at "
                                + i, new Integer(i), smallList.get(i));
        }

        // test replacing null elements with another value
        result = Collections.replaceAll(smallList, null, new Integer(99));
        assertTrue("Test10: Collections.replaceAll() returned wrong result",
                result);

        for (int i = 0; i < smallList.size(); i++) {
            if (i == 4 || i == 5)
                assertEquals("Test10: ReplaceAll didn't replace element at "
                        + i, new Integer(99), smallList.get(i));
            else
                assertEquals(
                        "Test10: ReplaceAll shouldn't have replaced element at "
                                + i, new Integer(i), smallList.get(i));
        }

        Mock_ArrayList mal = new Mock_ArrayList();

        mal.add("First");
        mal.add("Second");

        try {
            Collections.replaceAll(mal, "Second", null);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
            //expected
        }
    }

    public void test_rotateLjava_util_ListI() {
        // Test for method rotate(java.util.List, int)

        try {
            Collections.rotate(null, 0);
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }

        // Test rotating a Sequential Access List
        LinkedList list1 = new LinkedList();
        for (int i = 0; i < 10; i++) {
            list1.add(objArray[i]);
        }
        testRotate(list1, "Sequential Access");

        // Test rotating a Random Access List
        ArrayList list2 = new ArrayList();
        for (int i = 0; i < 10; i++) {
            list2.add(objArray[i]);
        }
        testRotate(list2, "Random Access");
    }

    private void testRotate(List list, String type) {
        // rotate with positive distance
        Collections.rotate(list, 7);
        assertEquals("Test1: rotate modified the " + type
                + " list incorrectly,", "3456789012", getString(list));

        // rotate with negative distance
        Collections.rotate(list, -2);
        assertEquals("Test2: rotate modified the " + type
                + " list incorrectly,", "5678901234", getString(list));

        // rotate sublist with negative distance
        List subList = list.subList(1, 5);
        Collections.rotate(subList, -1);
        assertEquals("Test3: rotate modified the " + type
                + " list incorrectly,", "5789601234", getString(list));

        // rotate sublist with positive distance
        Collections.rotate(subList, 2);
        assertEquals("Test4: rotate modified the " + type
                + " list incorrectly,", "5967801234", getString(list));

        // rotate with positive distance that is larger than list size
        Collections.rotate(list, 23);
        assertEquals("Test5: rotate modified the " + type
                + " list incorrectly,", "2345967801", getString(list));

        // rotate with negative distance that is larger than list size
        Collections.rotate(list, -23);
        assertEquals("Test6: rotate modified the " + type
                + " list incorrectly,", "5967801234", getString(list));

        // rotate with 0 and equivalent distances, this should make no
        // modifications to the list
        Collections.rotate(list, 0);
        assertEquals("Test7: rotate modified the " + type
                + " list incorrectly,", "5967801234", getString(list));

        Collections.rotate(list, -30);
        assertEquals("Test8: rotate modified the " + type
                + " list incorrectly,", "5967801234", getString(list));

        Collections.rotate(list, 30);
        assertEquals("Test9: rotate modified the " + type
                + " list incorrectly,", "5967801234", getString(list));
    }

    private String getString(List list) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            buffer.append(list.get(i));
        }
        return buffer.toString();
    }

    public void test_rotate2() {
        List list = new ArrayList();
        try {
            Collections.rotate(list, 5);
        } catch (UnsupportedOperationException e) {
            fail("Unexpected UnsupportedOperationException for empty list, "
                    + e);
        }

        list.add(0, "zero");
        list.add(1, "one");
        list.add(2, "two");
        list.add(3, "three");
        list.add(4, "four");

        Collections.rotate(list, Integer.MIN_VALUE);
        assertEquals("Rotated incorrectly at position 0, ", "three",
                (String) list.get(0));
        assertEquals("Rotated incorrectly at position 1, ", "four",
                (String) list.get(1));
        assertEquals("Rotated incorrectly at position 2, ", "zero",
                (String) list.get(2));
        assertEquals("Rotated incorrectly at position 3, ", "one",
                (String) list.get(3));
        assertEquals("Rotated incorrectly at position 4, ", "two",
                (String) list.get(4));
    }

    public void test_indexOfSubListLjava_util_ListLjava_util_List() {
        // Test for method int indexOfSubList(java.util.List, java.util.List)
        List list = new ArrayList();
        try {
            Collections.indexOfSubList(null, list);
            fail("Expected NullPointerException for null list first parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        try {
            Collections.indexOfSubList(list, null);
            fail("Expected NullPointerException for null list second parameter");
        } catch (NullPointerException e) {
            //Expected
        }

        String string1 = "A-B-C-D-E-S-JF-SUB-G-H-I-J-SUBL-K-L-LIST-M-N--S-S-O-SUBLIS-P-Q-R-SUBLIST-S-T-U-V-W-X-Y-Z";

        testwithCharList(1, string1, "B", true);
        testwithCharList(2, string1, "LIST", true);
        testwithCharList(3, string1, "SUBLIST", true);
        testwithCharList(4, string1, "NONE", true);
        testwithCharList(5, string1, "END", true);

        // test boundary conditions:
        testwithCharList(6, "", "", true);
        testwithCharList(7, "LIST", "", true);
        testwithCharList(8, "", "SUBLIST", true);
    }

    public void test_indexOfSubList2() {
        ArrayList sub = new ArrayList();
        sub.add(new Integer(1));
        sub.add(new Integer(2));
        sub.add(new Integer(3));

        ArrayList sub2 = new ArrayList();
        sub2.add(new Integer(7));
        sub2.add(new Integer(8));

        ArrayList src = new ArrayList();
        src.addAll(sub);
        src.addAll(sub);
        src.addAll(sub);
        src.add(new Integer(5));
        src.add(new Integer(6));

        // so src becomes a list like this:
        // [1, 2, 3, 1, 2, 3, 1, 2, 3, 5, 6]

        sub = new ArrayList(src.subList(3, 11));
        // [1, 2, 3, 1, 2, 3, 5, 6]
        assertEquals("TestA : Returned wrong indexOfSubList, ", 3, Collections
                .indexOfSubList(src, sub));

        sub = new ArrayList(src.subList(6, 11));
        // [1, 2, 3, 5, 6]
        assertEquals("TestB : Returned wrong indexOfSubList, ", 6, Collections
                .indexOfSubList(src, sub));

        sub = new ArrayList(src.subList(0, 3));
        // [1, 2, 3]
        assertEquals("TestCC : Returned wrong indexOfSubList, ", 0, Collections
                .indexOfSubList(src, sub));

        sub = new ArrayList(src.subList(9, 11));
        // [5, 6]
        assertEquals("TestD : Returned wrong indexOfSubList, ", 9, Collections
                .indexOfSubList(src, sub));

        sub = new ArrayList(src.subList(10, 11));
        // [6]
        assertEquals("TestE : Returned wrong indexOfSubList, ", 10, Collections
                .indexOfSubList(src, sub));

        sub = new ArrayList(src.subList(0, 11));
        // the whole list
        assertEquals("TestH : Returned wrong indexIndexOfSubList, ", 0,
                Collections.indexOfSubList(src, sub));

        // a non-matching list
        assertEquals("TestI : Returned wrong indexOfSubList, ", -1, Collections
                .indexOfSubList(src, sub2));
    }

    private void testwithCharList(int count, String string1, String string2,
            boolean first) {
        char[] chars = string1.toCharArray();
        List list = new ArrayList();
        for (int i = 0; i < chars.length; i++) {
            list.add(new Character(chars[i]));
        }
        chars = string2.toCharArray();
        List sublist = new ArrayList();
        for (int i = 0; i < chars.length; i++) {
            sublist.add(new Character(chars[i]));
        }

        if (first)
            assertEquals("Test " + count + ": Returned wrong index:", string1
                    .indexOf(string2), Collections
                    .indexOfSubList(list, sublist));
        else
            assertEquals("Test " + count + ": Returned wrong index:", string1
                    .lastIndexOf(string2), Collections.lastIndexOfSubList(list,
                    sublist));
    }

    public void test_lastIndexOfSubListLjava_util_ListLjava_util_List() {
        // Test for method int lastIndexOfSubList(java.util.List,
        // java.util.List)
        String string1 = "A-B-C-D-E-S-JF-SUB-G-H-I-J-SUBL-K-L-LIST-M-N--S-S-O-SUBLIS-P-Q-R-SUBLIST-S-T-U-V-W-X-Y-Z-END";

        List list = new ArrayList();
        try {
            Collections.lastIndexOfSubList(null, list);
            fail("Expected NullPointerException for null list first parameter");
        } catch (NullPointerException e) {
            //Expected
        }
        try {
            Collections.lastIndexOfSubList(list, null);
            fail("Expected NullPointerException for null list second parameter");
        } catch (NullPointerException e) {
            //Expected
        }

        testwithCharList(1, string1, "B", false);
        testwithCharList(2, string1, "LIST", false);
        testwithCharList(3, string1, "SUBLIST", false);
        testwithCharList(4, string1, "END", false);
        testwithCharList(5, string1, "NONE", false);

        // test boundary conditions
        testwithCharList(6, "", "", false);
        testwithCharList(7, "LIST", "", false);
        testwithCharList(8, "", "SUBLIST", false);
    }

    public void test_lastIndexOfSubList2() {
        ArrayList sub = new ArrayList();
        sub.add(new Integer(1));
        sub.add(new Integer(2));
        sub.add(new Integer(3));

        ArrayList sub2 = new ArrayList();
        sub2.add(new Integer(7));
        sub2.add(new Integer(8));

        ArrayList src = new ArrayList();
        src.addAll(sub);
        src.addAll(sub);
        src.addAll(sub);
        src.add(new Integer(5));
        src.add(new Integer(6));

        // so src is a list like this:
        // [1, 2, 3, 1, 2, 3, 1, 2, 3, 5, 6]

        Collections.reverse(src);
        // it becomes like this :
        // [6, 5, 3, 2, 1, 3, 2, 1, 3, 2, 1]

        sub = new ArrayList(src.subList(0, 8));
        // [6, 5, 3, 2, 1, 3, 2, 1]
        assertEquals("TestA : Returned wrong lastIndexOfSubList, ", 0,
                Collections.lastIndexOfSubList(src, sub));

        sub = new ArrayList(src.subList(0, 5));
        // [6, 5, 3, 2, 1]
        assertEquals("TestB : Returned wrong lastIndexOfSubList, ", 0,
                Collections.lastIndexOfSubList(src, sub));

        sub = new ArrayList(src.subList(2, 5));
        // [3, 2, 1]
        assertEquals("TestC : Returned wrong lastIndexOfSubList, ", 8,
                Collections.lastIndexOfSubList(src, sub));

        sub = new ArrayList(src.subList(9, 11));
        // [2, 1]
        assertEquals("TestD : Returned wrong lastIndexOfSubList, ", 9,
                Collections.lastIndexOfSubList(src, sub));

        sub = new ArrayList(src.subList(10, 11));
        // [1]
        assertEquals("TestE : Returned wrong lastIndexOfSubList, ", 10,
                Collections.lastIndexOfSubList(src, sub));

        sub = new ArrayList(src.subList(0, 2));
        // [6, 5]
        assertEquals("TestF : Returned wrong lastIndexOfSubList, ", 0,
                Collections.lastIndexOfSubList(src, sub));

        sub = new ArrayList(src.subList(0, 1));
        // [6]
        assertEquals("TestG : Returned wrong lastIndexOfSubList, ", 0,
                Collections.lastIndexOfSubList(src, sub));

        sub = new ArrayList(src.subList(0, 11));
        // the whole list
        assertEquals("TestH : Returned wrong lastIndexOfSubList, ", 0,
                Collections.lastIndexOfSubList(src, sub));

        // a non-matching list
        assertEquals("TestI : Returned wrong lastIndexOfSubList, ", -1,
                Collections.lastIndexOfSubList(src, sub2));
    }

    public void test_listLjava_util_Enumeration() {
        // Test for method java.util.ArrayList list(java.util.Enumeration)

        Enumeration e = Collections.enumeration(ll);
        ArrayList al = Collections.list(e);

        int size = al.size();
        assertEquals("Wrong size", ll.size(), size);

        for (int i = 0; i < size; i++) {
            assertEquals("wrong element at position " + i + ",", ll.get(i), al
                    .get(i));
        }
    }

    public void test_synchronizedCollectionLjava_util_Collection() {
        // Test for method java.util.Collection
        // java.util.Collections.synchronizedCollection(java.util.Collection)

        LinkedList smallList = new LinkedList();
        for (int i = 0; i < 50; i++) {
            smallList.add(objArray[i]);
        }

        final int numberOfLoops = 200;
        Collection synchCol = Collections.synchronizedCollection(smallList);
        // Replacing the previous line with the line below *should* cause the
        // test to fail--the collecion below isn't synchronized
        // Collection synchCol = smallList;

        SynchCollectionChecker normalSynchChecker = new SynchCollectionChecker(
                synchCol, false, numberOfLoops);
        SynchCollectionChecker offsetSynchChecker = new SynchCollectionChecker(
                synchCol, true, numberOfLoops);
        Thread normalThread = new Thread(normalSynchChecker);
        Thread offsetThread = new Thread(offsetSynchChecker);
        normalThread.start();
        offsetThread.start();
        while ((normalSynchChecker.getNumberOfChecks() < numberOfLoops)
                || (offsetSynchChecker.getNumberOfChecks() < numberOfLoops)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        assertTrue("Returned collection corrupted by multiple thread access",
                normalSynchChecker.getResult()
                        && offsetSynchChecker.getResult());
        try {
            normalThread.join(5000);
            offsetThread.join(5000);
        } catch (InterruptedException e) {
            fail("join() interrupted");
        }

        synchCol.add(null);
        assertTrue("Trying to use nulls in collection failed", synchCol
                .contains(null));

        smallList = new LinkedList();
        for (int i = 0; i < 100; i++) {
            smallList.add(objArray[i]);
        }
        new Support_CollectionTest("", Collections
                .synchronizedCollection(smallList)).runTest();

        //Test self reference
        synchCol = Collections.synchronizedCollection(smallList);
        synchCol.add(smallList);
        assertTrue("should contain self ref", synchCol.toString().indexOf("(this") > -1);
    }

    public void test_synchronizedListLjava_util_List() {
        try {
            Collections.synchronizedList(null);
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
            //Expected
        }

        // test with a Sequential Access List
        List smallList = new LinkedList();
        testSynchronizedList(smallList, "Sequential Access");

        smallList = new LinkedList();
        List myList;
        for (int i = 0; i < 100; i++) {
            smallList.add(objArray[i]);
        }
        myList = Collections.synchronizedList(smallList);
        new Support_ListTest("", myList).runTest();

        // test with a Random Access List
        smallList = new ArrayList();
        testSynchronizedList(smallList, "Random Access");

        smallList = new ArrayList();
        for (int i = 0; i < 100; i++) {
            smallList.add(objArray[i]);
        }
        myList = Collections.synchronizedList(smallList);
        new Support_ListTest("", myList).runTest();

        //Test self reference
        myList = Collections.synchronizedList(smallList);
        myList.add(smallList);
        assertTrue("should contain self ref", myList.toString().indexOf("(this") > -1);
    }

    private void testSynchronizedList(List smallList, String type) {
        for (int i = 0; i < 50; i++) {
            smallList.add(objArray[i]);
        }
        final int numberOfLoops = 200;
        List synchList = Collections.synchronizedList(smallList);
        if (type.equals("Random Access"))
            assertTrue(
                    "Returned synchronized list should implement the Random Access interface",
                    synchList instanceof RandomAccess);
        else
            assertTrue(
                    "Returned synchronized list should not implement the Random Access interface",
                    !(synchList instanceof RandomAccess));

        // Replacing the previous line with the line below *should* cause the
        // test to fail--the list below isn't synchronized
        // List synchList = smallList;
        SynchCollectionChecker normalSynchChecker = new SynchCollectionChecker(
                synchList, false, numberOfLoops);
        SynchCollectionChecker offsetSynchChecker = new SynchCollectionChecker(
                synchList, true, numberOfLoops);
        Thread normalThread = new Thread(normalSynchChecker);
        Thread offsetThread = new Thread(offsetSynchChecker);
        normalThread.start();
        offsetThread.start();
        while ((normalSynchChecker.getNumberOfChecks() < numberOfLoops)
                || (offsetSynchChecker.getNumberOfChecks() < numberOfLoops)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //Expected
            }
        }
        assertTrue(
                type
                        + " list tests: Returned list corrupted by multiple thread access",
                normalSynchChecker.getResult()
                        && offsetSynchChecker.getResult());
        try {
            normalThread.join(5000);
            offsetThread.join(5000);
        } catch (InterruptedException e) {
            fail(type + " list tests: join() interrupted");
        }
        synchList.set(25, null);
        assertNull(type + " list tests: Trying to use nulls in list failed",
                synchList.get(25));
    }

    public void test_synchronizedMapLjava_util_Map() {
        // Test for method java.util.Map
        // java.util.Collections.synchronizedMap(java.util.Map)
        HashMap smallMap = new HashMap();
        for (int i = 0; i < 50; i++) {
            smallMap.put(objArray[i], objArray[i]);
        }

        final int numberOfLoops = 200;
        Map synchMap = Collections.synchronizedMap(smallMap);
        // Replacing the previous line with the line below should cause the test
        // to fail--the list below isn't synchronized
        // Map synchMap = smallMap;

        SynchMapChecker normalSynchChecker = new SynchMapChecker(synchMap,
                false, numberOfLoops);
        SynchMapChecker offsetSynchChecker = new SynchMapChecker(synchMap,
                true, numberOfLoops);
        Thread normalThread = new Thread(normalSynchChecker);
        Thread offsetThread = new Thread(offsetSynchChecker);
        normalThread.start();
        offsetThread.start();
        while ((normalSynchChecker.getNumberOfChecks() < numberOfLoops)
                || (offsetSynchChecker.getNumberOfChecks() < numberOfLoops)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //Expected
            }
        }
        assertTrue("Returned map corrupted by multiple thread access",
                normalSynchChecker.getResult()
                        && offsetSynchChecker.getResult());
        try {
            normalThread.join(5000);
            offsetThread.join(5000);
        } catch (InterruptedException e) {
            fail("join() interrupted");
        }

        // synchronized map does not have to permit null keys or values
        synchMap.put(new Long(25), null);
        synchMap.put(null, new Long(30));
        assertNull("Trying to use a null value in map failed", synchMap
                .get(new Long(25)));
        assertTrue("Trying to use a null key in map failed", synchMap.get(null)
                .equals(new Long(30)));

        smallMap = new HashMap();
        for (int i = 0; i < 100; i++) {
            smallMap.put(objArray[i].toString(), objArray[i]);
        }
        synchMap = Collections.synchronizedMap(smallMap);
        new MapTestSupport(synchMap).runTest();
        synchMap.keySet().remove(objArray[50].toString());
        assertNull(
                "Removing a key from the keySet of the synchronized map did not remove it from the synchronized map: ",
                synchMap.get(objArray[50].toString()));
        assertNull(
                "Removing a key from the keySet of the synchronized map did not remove it from the original map",
                smallMap.get(objArray[50].toString()));
    }

    public void test_unmodifiableMap_LinkedHashMap() {
        // LinkedHashMap has a well defined iteration order and shows ordering issues with
        // entrySet() / keySet() methods: iterator(), toArray(T[]) and toArray(). See bug 72073.
        LinkedHashMap<String, Integer> smallMap = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < 100; i++) {
            Integer object = objArray[i];
            smallMap.put(object.toString(), object);
        }
        new MapTestSupport(smallMap).runTest();
    }

    public void test_synchronizedSetLjava_util_Set() {
        // Test for method java.util.Set
        // java.util.Collections.synchronizedSet(java.util.Set)
        HashSet smallSet = new HashSet();
        for (int i = 0; i < 50; i++) {
            smallSet.add(objArray[i]);
        }

        final int numberOfLoops = 200;
        Set synchSet = Collections.synchronizedSet(smallSet);
        // Replacing the previous line with the line below should cause the test
        // to fail--the set below isn't synchronized
        // Set synchSet = smallSet;

        SynchCollectionChecker normalSynchChecker = new SynchCollectionChecker(
                synchSet, false, numberOfLoops);
        SynchCollectionChecker offsetSynchChecker = new SynchCollectionChecker(
                synchSet, true, numberOfLoops);
        Thread normalThread = new Thread(normalSynchChecker);
        Thread offsetThread = new Thread(offsetSynchChecker);
        normalThread.start();
        offsetThread.start();
        while ((normalSynchChecker.getNumberOfChecks() < numberOfLoops)
                || (offsetSynchChecker.getNumberOfChecks() < numberOfLoops)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //Expected
            }
        }
        assertTrue("Returned set corrupted by multiple thread access",
                normalSynchChecker.getResult()
                        && offsetSynchChecker.getResult());
        try {
            normalThread.join(5000);
            offsetThread.join(5000);
        } catch (InterruptedException e) {
            fail("join() interrupted");
        }

        Set mySet = Collections.synchronizedSet(smallSet);
        mySet.add(null);
        assertTrue("Trying to use nulls in list failed", mySet.contains(null));

        smallSet = new HashSet();
        for (int i = 0; i < 100; i++) {
            smallSet.add(objArray[i]);
        }
        new Support_SetTest("", Collections.synchronizedSet(smallSet))
                .runTest();

        //Test self reference
        mySet = Collections.synchronizedSet(smallSet);
        mySet.add(smallSet);
        assertTrue("should contain self ref", mySet.toString().indexOf("(this") > -1);
    }

    public void test_synchronizedSortedMapLjava_util_SortedMap() {
        // Test for method java.util.SortedMap
        // java.util.Collections.synchronizedSortedMap(java.util.SortedMap)
        TreeMap smallMap = new TreeMap();
        for (int i = 0; i < 50; i++) {
            smallMap.put(objArray[i], objArray[i]);
        }

        final int numberOfLoops = 200;
        Map synchMap = Collections.synchronizedMap(smallMap);
        // Replacing the previous line with the line below should cause the test
        // to fail--the list below isn't synchronized
        // Map synchMap = smallMap;

        SynchMapChecker normalSynchChecker = new SynchMapChecker(synchMap,
                false, numberOfLoops);
        SynchMapChecker offsetSynchChecker = new SynchMapChecker(synchMap,
                true, numberOfLoops);
        Thread normalThread = new Thread(normalSynchChecker);
        Thread offsetThread = new Thread(offsetSynchChecker);
        normalThread.start();
        offsetThread.start();
        while ((normalSynchChecker.getNumberOfChecks() < numberOfLoops)
                || (offsetSynchChecker.getNumberOfChecks() < numberOfLoops)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //Expected
            }
        }
        assertTrue("Returned map corrupted by multiple thread access",
                normalSynchChecker.getResult()
                        && offsetSynchChecker.getResult());
        try {
            normalThread.join(5000);
            offsetThread.join(5000);
        } catch (InterruptedException e) {
            fail("join() interrupted");
        }

        smallMap = new TreeMap();
        for (int i = 0; i < 100; i++) {
            smallMap.put(objArray[i].toString(), objArray[i]);
        }
        synchMap = Collections.synchronizedSortedMap(smallMap);
        new MapTestSupport(synchMap).runTest();
        synchMap.keySet().remove(objArray[50].toString());
        assertNull(
                "Removing a key from the keySet of the synchronized map did not remove it from the synchronized map",
                synchMap.get(objArray[50].toString()));
        assertNull(
                "Removing a key from the keySet of the synchronized map did not remove it from the original map",
                smallMap.get(objArray[50].toString()));
    }

    public void test_synchronizedSortedSetLjava_util_SortedSet() {
        // Test for method java.util.SortedSet
        // java.util.Collections.synchronizedSortedSet(java.util.SortedSet)
        TreeSet smallSet = new TreeSet();
        for (int i = 0; i < 50; i++) {
            smallSet.add(objArray[i]);
        }

        final int numberOfLoops = 200;
        Set synchSet = Collections.synchronizedSet(smallSet);
        // Replacing the previous line with the line below should cause the test
        // to fail--the list below isn't synchronized
        // Set synchSet = smallSet;

        SynchCollectionChecker normalSynchChecker = new SynchCollectionChecker(
                synchSet, false, numberOfLoops);
        SynchCollectionChecker offsetSynchChecker = new SynchCollectionChecker(
                synchSet, true, numberOfLoops);
        Thread normalThread = new Thread(normalSynchChecker);
        Thread offsetThread = new Thread(offsetSynchChecker);
        normalThread.start();
        offsetThread.start();
        while ((normalSynchChecker.getNumberOfChecks() < numberOfLoops)
                || (offsetSynchChecker.getNumberOfChecks() < numberOfLoops)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //Expected
            }
        }
        assertTrue("Returned set corrupted by multiple thread access",
                normalSynchChecker.getResult()
                        && offsetSynchChecker.getResult());
        try {
            normalThread.join(5000);
            offsetThread.join(5000);
        } catch (InterruptedException e) {
            fail("join() interrupted");
        }
    }

    public void test_unmodifiableCollectionLjava_util_Collection() {
        // Test for method java.util.Collection
        // java.util.Collections.unmodifiableCollection(java.util.Collection)
        boolean exception = false;
        Collection c = Collections.unmodifiableCollection(ll);
        assertTrue("Returned collection is of incorrect size", c.size() == ll
                .size());
        Iterator iterator = ll.iterator();
        while (iterator.hasNext())
            assertTrue("Returned list missing elements", c.contains(iterator.next()));
        try {
            c.add(new Object());
        } catch (UnsupportedOperationException e) {
            exception = true;
            // Correct
        }
        if (!exception) {
            fail("Allowed modification of collection");
        }

        try {
            c.remove(new Object());
            fail("Allowed modification of collection");
        } catch (UnsupportedOperationException e) {
            // Correct
        }

        try {
            c.removeIf(x -> true);
            fail("Allowed modification of collection");
        } catch (UnsupportedOperationException e) {
            // Correct
        }

        Collection myCollection = new ArrayList();
        myCollection.add(new Integer(20));
        myCollection.add(null);
        c = Collections.unmodifiableCollection(myCollection);
        assertTrue("Collection should contain null", c.contains(null));
        assertTrue("Collection should contain Integer(20)", c
                .contains(new Integer(20)));

        myCollection = new ArrayList();
        for (int i = 0; i < 100; i++) {
            myCollection.add(objArray[i]);
        }
        new Support_UnmodifiableCollectionTest("", Collections
                .unmodifiableCollection(myCollection)).runTest();
    }

    public void test_unmodifiableListLjava_util_List() {
        // Test for method java.util.List
        // java.util.Collections.unmodifiableList(java.util.List)

        // test with a Sequential Access List
        boolean exception = false;
        List c = Collections.unmodifiableList(ll);
        // Ensure a NPE is thrown if the list is NULL
        try {
            Collections.unmodifiableList(null);
            fail("Expected NullPointerException for null list parameter");
        } catch (NullPointerException e) {
        }

        assertTrue("Returned list is of incorrect size", c.size() == ll.size());
        assertTrue(
                "Returned List should not implement Random Access interface",
                !(c instanceof RandomAccess));

        Iterator iterator = ll.iterator();
        while (iterator.hasNext())
            assertTrue("Returned list missing elements", c.contains(iterator.next()));
        try {
            c.add(new Object());
        } catch (UnsupportedOperationException e) {
            exception = true;
            // Correct
        }
        if (!exception) {
            fail("Allowed modification of list");
        }

        try {
            c.remove(new Object());
            fail("Allowed modification of list");
        } catch (UnsupportedOperationException e) {
            // Correct
        }

        try {
            c.removeIf(x -> true);
            fail("Allowed modification of list");
        } catch (UnsupportedOperationException e) {
            // Correct
        }

        // test with a Random Access List
        List smallList = new ArrayList();
        smallList.add(null);
        smallList.add("yoink");
        c = Collections.unmodifiableList(smallList);
        assertNull("First element should be null", c.get(0));
        assertTrue("List should contain null", c.contains(null));
        assertTrue(
                "T1. Returned List should implement Random Access interface",
                c instanceof RandomAccess);

        smallList = new ArrayList();
        for (int i = 0; i < 100; i++) {
            smallList.add(objArray[i]);
        }
        List myList = Collections.unmodifiableList(smallList);
        assertTrue("List should not contain null", !myList.contains(null));
        assertTrue(
                "T2. Returned List should implement Random Access interface",
                myList instanceof RandomAccess);

        assertTrue("get failed on unmodifiable list", myList.get(50).equals(
                new Integer(50)));
        ListIterator listIterator = myList.listIterator();
        for (int i = 0; listIterator.hasNext(); i++) {
            assertTrue("List has wrong elements", ((Integer) listIterator
                    .next()).intValue() == i);
        }
        new Support_UnmodifiableCollectionTest("", smallList).runTest();
    }

    public void test_unmodifiableMapLjava_util_Map() {
        // Test for method java.util.Map
        // java.util.Collections.unmodifiableMap(java.util.Map)
        boolean exception = false;
        Map c = Collections.unmodifiableMap(hm);
        assertTrue("Returned map is of incorrect size", c.size() == hm.size());
        Iterator iterator = hm.keySet().iterator();
        while (iterator.hasNext()) {
            Object x = iterator.next();
            assertTrue("Returned map missing elements", c.get(x).equals(
                    hm.get(x)));
        }
        try {
            c.put(new Object(), "");
        } catch (UnsupportedOperationException e) {
            exception = true;
            // Correct
        }
        assertTrue("Allowed modification of map", exception);

        exception = false;
        try {
            c.remove(new Object());
        } catch (UnsupportedOperationException e) {
            // Correct
            exception = true;
        }
        assertTrue("Allowed modification of map", exception);

        exception = false;
        Iterator entrySetIterator = c.entrySet().iterator();
        Map.Entry entry = (Map.Entry) entrySetIterator.next();
        try {
            entry.setValue("modified");
        } catch (UnsupportedOperationException e) {
            // Correct
            exception = true;
        }
        assertTrue("Allowed modification of entry", exception);

        exception = false;
        Object[] array = c.entrySet().toArray();
        try {
            ((Map.Entry) array[0]).setValue("modified");
        } catch (UnsupportedOperationException e) {
            // Correct
            exception = true;
        }
        assertTrue("Allowed modification of array entry", exception);

        exception = false;
        Map.Entry[] array2 = (Map.Entry[]) c.entrySet().toArray(
                new Map.Entry[0]);
        try {
            array2[0].setValue("modified");
        } catch (UnsupportedOperationException e) {
            // Correct
            exception = true;
        }
        assertTrue("Allowed modification of array entry2", exception);

        HashMap smallMap = new HashMap();
        smallMap.put(null, new Long(30));
        smallMap.put(new Long(25), null);
        Map unmodMap = Collections.unmodifiableMap(smallMap);

        assertNull("Trying to use a null value in map failed", unmodMap
                .get(new Long(25)));
        assertTrue("Trying to use a null key in map failed", unmodMap.get(null)
                .equals(new Long(30)));

        smallMap = new HashMap();
        for (int i = 0; i < 100; i++) {
            smallMap.put(objArray[i].toString(), objArray[i]);
        }
        new MapTestSupport(smallMap).runTest();
    }

    public void test_unmodifiableSetLjava_util_Set() {
        // Test for method java.util.Set
        // java.util.Collections.unmodifiableSet(java.util.Set)
        boolean exception = false;
        Set c = Collections.unmodifiableSet(s);
        assertTrue("Returned set is of incorrect size", c.size() == s.size());
        Iterator iterator = ll.iterator();
        while (iterator.hasNext())
            assertTrue("Returned set missing elements", c.contains(iterator.next()));
        try {
            c.add(new Object());
        } catch (UnsupportedOperationException e) {
            exception = true;
            // Correct
        }
        if (!exception) {
            fail("Allowed modification of set");
        }
        try {
            c.remove(new Object());
            fail("Allowed modification of set");
        } catch (UnsupportedOperationException e) {
            // Correct
        }
        try {
            c.removeIf(x -> true);
            fail("Allowed modification of set");
        } catch (UnsupportedOperationException e) {
            // Correct
        }

        Set mySet = Collections.unmodifiableSet(new HashSet());
        assertTrue("Should not contain null", !mySet.contains(null));
        mySet = Collections.unmodifiableSet(Collections.singleton(null));
        assertTrue("Should contain null", mySet.contains(null));

        mySet = new TreeSet();
        for (int i = 0; i < 100; i++) {
            mySet.add(objArray[i]);
        }
        new Support_UnmodifiableCollectionTest("", Collections
                .unmodifiableSet(mySet)).runTest();
    }

    public void test_unmodifiableSortedMapLjava_util_SortedMap() {
        // Test for method java.util.SortedMap
        // java.util.Collections.unmodifiableSortedMap(java.util.SortedMap)
        boolean exception = false;
        TreeMap tm = new TreeMap();
        tm.putAll(hm);
        Map c = Collections.unmodifiableSortedMap(tm);
        assertTrue("Returned map is of incorrect size", c.size() == tm.size());
        Iterator i = hm.keySet().iterator();
        while (i.hasNext()) {
            Object x = i.next();
            assertTrue("Returned map missing elements", c.get(x).equals(
                    tm.get(x)));
        }
        try {
            c.put(new Object(), "");
        } catch (UnsupportedOperationException e) {
            exception = true;
            // Correct
        }

        if (!exception) {
            fail("Allowed modification of map");
        }
        try {
            c.remove(new Object());
        } catch (UnsupportedOperationException e) {
            // Correct
            return;
        }
        fail("Allowed modification of map");
    }

    public void test_unmodifiableSortedSetLjava_util_SortedSet() {
        // Test for method java.util.SortedSet
        // java.util.Collections.unmodifiableSortedSet(java.util.SortedSet)
        boolean exception = false;
        SortedSet ss = new TreeSet();
        ss.addAll(s);
        SortedSet c = Collections.unmodifiableSortedSet(ss);
        assertTrue("Returned set is of incorrect size", c.size() == ss.size());
        Iterator i = ll.iterator();
        while (i.hasNext())
            assertTrue("Returned set missing elements", c.contains(i.next()));
        try {
            c.add(new Object());
            fail("Allowed modification of set");
        } catch (UnsupportedOperationException e) {}

        try {
            c.remove(new Object());
            fail("Allowed modification of set");
        } catch (UnsupportedOperationException expected) {}

        try {
            c.removeIf(x -> true);
            fail("Allowed modification of set");
        } catch (UnsupportedOperationException expected) {}
    }

    /**
     * Test unmodifiable objects toString methods
     */
    public void test_unmodifiable_toString_methods() {
        // Regression for HARMONY-552
        ArrayList al = new ArrayList();
        al.add("a");
        al.add("b");
        Collection uc = Collections.unmodifiableCollection(al);
        assertEquals("[a, b]", uc.toString());
        HashMap m = new HashMap();
        m.put("one", "1");
        m.put("two", "2");
        Map um = Collections.unmodifiableMap(m);
        assertTrue("{one=1, two=2}".equals(um.toString()) ||
                   "{two=2, one=1}".equals(um.toString()));
    }


    public void test_singletonListLjava_lang_Object() {
        // Test for method java.util.Set
        // java.util.Collections.singleton(java.lang.Object)
        String str = "Singleton";

        List single = Collections.singletonList(str);
        assertEquals(1, single.size());
        assertTrue(single.contains(str));
        assertFalse(single.contains(null));
        assertFalse(Collections.singletonList(null).contains(str));
        assertTrue(Collections.singletonList(null).contains(null));

        try {
            single.add("New element");
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
            //expected
        }
    }

    public void test_singletonMapLjava_lang_Object() {
        // Test for method java.util.Set
        // java.util.Collections.singleton(java.lang.Object)
        Double key = new Double (3.14);
        String value = "Fundamental constant";

        Map single = Collections.singletonMap(key, value);
        assertEquals(1, single.size());
        assertTrue(single.containsKey(key));
        assertTrue(single.containsValue(value));
        assertFalse(single.containsKey(null));
        assertFalse(single.containsValue(null));
        assertFalse(Collections.singletonMap(null, null).containsKey(key));
        assertFalse(Collections.singletonMap(null, null).containsValue(value));
        assertTrue(Collections.singletonMap(null, null).containsKey(null));
        assertTrue(Collections.singletonMap(null, null).containsValue(null));

        try {
            single.clear();
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
            //expected
        }

        try {
            single.put(new Double(1), "one wrong value");
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
            //expected
        }
    }

    // Test on a non-public method that isn't guaranteed to exist.
    //
    // public void test_checkType_Ljava_lang_Object_Ljava_lang_Class() throws Exception {
    //     Method m = Collections.class.getDeclaredMethod("checkType", Object.class, Class.class);
    //    m.setAccessible(true);
    //     m.invoke(null, new Object(), Object.class);
    //
    //     try {
    //         m.invoke(null, new Object(), int.class);
    //         fail();
    //     } catch (InvocationTargetException expected) {
    //     }
    // }

    public void test_binarySearch_asymmetry_with_comparator() throws Exception {
        List list = new ArrayList();
        String s1 = new String("a");
        String s2 = new String("aa");
        String s3 = new String("aaa");
        list.add(s1);
        list.add(s2);
        list.add(s3);
        Collections.sort(list);
        Object o = Collections.binarySearch(list, 1, new StringComparator());
        assertSame(0, o);
    }

    public void test_binarySearch_asymmetry() throws Exception {
        List list = new LinkedList();
        String s1 = new String("a");
        String s2 = new String("aa");
        String s3 = new String("aaa");
        list.add(new MyComparable(s1));
        list.add(new MyComparable(s2));
        list.add(new MyComparable(s3));
        Collections.sort(list);
        Object o = Collections.binarySearch(list, 1);
        assertSame(0, o);
    }


    private class MyComparable implements Comparable {

        public String s;

        public MyComparable(String s) {
            this.s = s;

        }

        public int compareTo(Object another) {
            int length = 0;
            if (another instanceof MyComparable) {
                length = (((MyComparable) another).s).length();
            } else {
                length = (Integer) another;
            }
            return s.length() - length;
        }

    }

    private class StringComparator implements Comparator {

        public int compare(Object object1, Object object2) {
            String s = (String) object1;
            int length;
            if (object2 instanceof String) {
                length = ((String) object2).length();
            } else {
                length = (Integer) object2;
            }
            return s.length() - length;
        }
    }


    public void test_newSetFromMap_LMap() throws Exception {
        Integer testInt[] = new Integer[100];
        for (int i = 0; i < testInt.length; i++) {
            testInt[i] = new Integer(i);
        }
        Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
        Set<Integer> set = Collections.newSetFromMap(map);
        for (int i = 0; i < testInt.length; i++) {
            map.put(testInt[i], true);
        }
        // operater on map successed
        map.put(testInt[1], false);
        assertTrue(map.containsKey(testInt[1]));
        assertEquals(100, map.size());
        assertFalse(map.get(testInt[1]));
        assertEquals(100, set.size());
        assertTrue(set.contains(testInt[16]));
        Iterator setIter = set.iterator();
        Iterator mapIter = map.keySet().iterator();
        int i = 0;
        // in the same order
        while (setIter.hasNext()) {
            assertEquals(mapIter.next(), setIter.next());
        }

        // operator on set successed
        Integer testInt101 = new Integer(101);
        Integer testInt102 = new Integer(102);
        set.add(testInt101);
        assertTrue(set.contains(testInt101));
        assertTrue(map.get(testInt101));

        // operator on map still passes
        map.put(testInt102, false);
        assertTrue(set.contains(testInt102));
        assertFalse(map.get(testInt102));

        // exception thrown
        try {
            Collections.newSetFromMap(map);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * serialization/deserialization.
     */
    @SuppressWarnings({ "unchecked", "boxing" })
    public void testSerializationSelf_newSetFromMap() throws Exception {
        Integer testInt[] = new Integer[100];
        for (int i = 0; i < testInt.length; i++) {
            testInt[i] = new Integer(i);
        }
        Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
        Set<Integer> set = Collections.newSetFromMap(map);
        for (int i = 0; i < testInt.length; i++) {
            map.put(testInt[i], true);
        }
        SerializationTest.verifySelf(set);
    }

    public void test_asLifoQueue() throws Exception {
        Integer testInt[] = new Integer[100];
        Integer test101 = new Integer(101);
        for (int i = 0; i < testInt.length; i++) {
            testInt[i] = new Integer(i);
        }
        Deque deque = new ArrayDeque<Integer>();
        Queue<Integer> que = Collections.asLifoQueue(deque);
        for (int i = 0; i < testInt.length; i++) {
            que.add(testInt[i]);
        }
        assertEquals(100, deque.size());
        assertEquals(100, que.size());
        for (int i = testInt.length - 1; i >= 0; i--) {
            assertEquals(testInt[i], deque.pop());
        }
        assertEquals(0, deque.size());
        assertEquals(0, que.size());
        for (int i = 0; i < testInt.length; i++) {
            deque.push(testInt[i]);
        }
        assertEquals(100, deque.size());
        assertEquals(100, que.size());
        Collection col = new LinkedList<Integer>();
        col.add(test101);
        que.addAll(col);
        assertEquals(test101, que.remove());
        for (int i = testInt.length - 1; i >= 0; i--) {
            assertEquals(testInt[i], que.remove());
        }
        assertEquals(0, deque.size());
        assertEquals(0, que.size());
    }

    /**
     * serialization/deserialization.
     */
    @SuppressWarnings({ "unchecked", "boxing" })
    public void testSerializationSelf_asLifoQueue() throws Exception {
        Integer testInt[] = new Integer[100];
        for (int i = 0; i < testInt.length; i++) {
            testInt[i] = new Integer(i);
        }
        Deque deque = new ArrayDeque<Integer>();
        Queue<Integer> que = Collections.asLifoQueue(deque);
        for (int i = 0; i < testInt.length; i++) {
            que.add(testInt[i]);
        }
        SerializationTest.verifySelf(que, new SerializableAssert() {
            public void assertDeserialized(Serializable initial, Serializable deserialized) {
                Queue<Integer> initque = (Queue) initial;
                Queue<Integer> deserque = (Queue) deserialized;
                while (!initque.isEmpty()) {
                    assertEquals(initque.remove(), deserque.remove());
                }
            }
        });
    }

    public void test_emptyList() {
        List<String> list = Collections.emptyList();
        assertTrue("should be true", list.isEmpty());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        objArray = new Integer[1000];
        myobjArray = new Object[1000];
        for (int i = 0; i < objArray.length; i++) {
            objArray[i] = i;
            myobjArray[i] = new MyInt(i);
        }

        ll = new LinkedList();
        myll = new LinkedList();
        s = new HashSet();
        mys = new HashSet();
        reversedLinkedList = new LinkedList(); // to be sorted in reverse order
        myReversedLinkedList = new LinkedList(); // to be sorted in reverse
        // order
        hm = new HashMap();
        for (int i = 0; i < objArray.length; i++) {
            ll.add(objArray[i]);
            myll.add(myobjArray[i]);
            s.add(objArray[i]);
            mys.add(myobjArray[i]);
            reversedLinkedList.add(objArray[objArray.length - i - 1]);
            myReversedLinkedList.add(myobjArray[myobjArray.length - i - 1]);
            hm.put(objArray[i].toString(), objArray[i]);
        }
    }

    /**
     * A class shared by various Map-related tests that checks the properties and contents of a
     * supplied Map and compares the some methods to the same map when wrapped with
     * {@link Collections#unmodifiableMap(java.util.Map)}.
     */
    static class MapTestSupport {

        // must be a map containing the string keys "0"-"99" paired with the Integer
        // values Integer(0) to Integer(99)
        private final Map<String, Integer> modifiableMap;
        private final Map<String, Integer> unmodifiableMap;

        public MapTestSupport(Map<String, Integer> modifiableMap) {
            this.modifiableMap = modifiableMap;
            unmodifiableMap = Collections.unmodifiableMap(modifiableMap);
        }

        public void runTest() {
            testContents(modifiableMap);
            testContents(unmodifiableMap);

            // values()
            new Support_UnmodifiableCollectionTest("values() from map test", modifiableMap.values())
                    .runTest();
            new Support_UnmodifiableCollectionTest("values() from unmodifiable map test",
                    unmodifiableMap.values()).runTest();

            // entrySet()
            testEntrySet(modifiableMap.entrySet(), unmodifiableMap.entrySet());

            // keySet()
            testKeySet(modifiableMap.keySet(), unmodifiableMap.keySet());
        }

        private static void testContents(Map<String, Integer> map) {
            // size
            assertTrue("Size should return 100, returned: " + map.size(), map.size() == 100);

            // containsKey
            assertTrue("Should contain the key \"0\"", map.containsKey("0"));
            assertTrue("Should contain the key \"50\"", map.containsKey("50"));
            assertTrue("Should not contain the key \"100\"", !map.containsKey("100"));

            // containsValue
            assertTrue("Should contain the value 0", map.containsValue(0));
            assertTrue("Should contain the value 50", map.containsValue(50));
            assertTrue("Should not contain value 100", !map.containsValue(100));

            // get
            assertTrue("getting \"0\" didn't return 0", map.get("0") == 0);
            assertTrue("getting \"50\" didn't return 50", map.get("50") == 50);
            assertNull("getting \"100\" didn't return null", map.get("100"));

            // isEmpty
            assertTrue("should have returned false to isEmpty", !map.isEmpty());
        }

        private static void testEntrySet(
                Set<Map.Entry<String, Integer>> referenceEntrySet,
                Set<Map.Entry<String, Integer>> entrySet) {
            // entrySet should be a set of mappings {"0", 0}, {"1",1}... {"99", 99}
            assertEquals(100, referenceEntrySet.size());
            assertEquals(100, entrySet.size());

            // The ordering may be undefined for a map implementation but the ordering must be the
            // same across iterator(), toArray() and toArray(T[]) for a given map *and* the same for the
            // modifiable and unmodifiable map.
            crossCheckOrdering(referenceEntrySet, entrySet, Map.Entry.class);
        }

        private static void testKeySet(Set<String> referenceKeySet, Set<String> keySet) {
            // keySet should be a set of the strings "0" to "99"
            testKeySetContents(referenceKeySet);
            testKeySetContents(keySet);

            // The ordering may be undefined for a map implementation but the ordering must be the
            // same across iterator(), toArray() and toArray(T[]) for a given map *and* the same for the
            // modifiable and unmodifiable map.
            crossCheckOrdering(referenceKeySet, keySet, String.class);
        }

        private static void testKeySetContents(Set<String> keySet) {
            // contains
            assertTrue("should contain \"0\"", keySet.contains("0"));
            assertTrue("should contain \"50\"", keySet.contains("50"));
            assertTrue("should not contain \"100\"", !keySet.contains("100"));

            // containsAll
            HashSet<String> hs = new HashSet<String>();
            hs.add("0");
            hs.add("25");
            hs.add("99");
            assertTrue("Should contain set of \"0\", \"25\", and \"99\"", keySet.containsAll(hs));
            hs.add("100");
            assertTrue("Should not contain set of \"0\", \"25\", \"99\" and \"100\"",
                    !keySet.containsAll(hs));

            // isEmpty
            assertTrue("Should not be empty", !keySet.isEmpty());

            // size
            assertEquals("Returned wrong size.", 100, keySet.size());
        }

        private static <T> void crossCheckOrdering(Set<T> set1, Set<T> set2, Class<?> elementType) {
            Iterator<T> set1Iterator = set1.iterator();
            Iterator<T> set2Iterator = set2.iterator();

            T[] zeroLengthArray = createArray(elementType, 0);
            T[] set1TypedArray1 = set1.toArray(zeroLengthArray);
            assertEquals(set1.size(), set1TypedArray1.length);

            // Compare set1.iterator(), set2.iterator() and set1.toArray(new T[0])
            int entryCount = 0;
            while (set1Iterator.hasNext()) {
                T set1Entry = set1Iterator.next();
                T set2Entry = set2Iterator.next();

                // Compare set1 with set2
                assertEquals(set1Entry, set2Entry);

                // Compare the iterator with the array. The arrays will be checked against each other.
                assertEquals(set1Entry, set1TypedArray1[entryCount]);

                entryCount++;
            }
            assertFalse(set2Iterator.hasNext());
            assertEquals(set1.size(), entryCount);

            // Compare the various arrays with each other.

            // set1.toArray(new T[size])
            T[] parameterArray1 = createArray(elementType, set1.size());
            T[] set1TypedArray2 = set1.toArray(parameterArray1);
            assertSame(set1TypedArray2, parameterArray1);
            assertArrayEquals(set1TypedArray1, set1TypedArray2);

            // set1.toArray()
            Object[] set1UntypedArray = set1.toArray();
            assertEquals(set1.size(), set1UntypedArray.length);
            assertArrayEquals(set1TypedArray1, set1UntypedArray);

            // set2.toArray(new T[0])
            T[] set2TypedArray1 = set2.toArray(zeroLengthArray);
            assertEquals(set1.size(), set2TypedArray1.length);
            assertArrayEquals(set1TypedArray1, set2TypedArray1);

            // set2.toArray(new T[size])
            T[] parameterArray2 = createArray(elementType, set2.size());
            T[] set2TypedArray2 = set1.toArray(parameterArray2);
            assertSame(set2TypedArray2, parameterArray2);
            assertArrayEquals(set1TypedArray1, set1TypedArray2);

            // set2.toArray()
            Object[] set2UntypedArray = set2.toArray();
            assertArrayEquals(set1TypedArray1, set2UntypedArray);
        }

        private static <T> void assertArrayEquals(T[] array1, T[] array2) {
            assertTrue(Arrays.equals(array1, array2));
        }

        @SuppressWarnings("unchecked")
        private static <T> T[] createArray(Class<?> elementType, int size) {
            return (T[]) Array.newInstance(elementType, size);
        }
    }
}
