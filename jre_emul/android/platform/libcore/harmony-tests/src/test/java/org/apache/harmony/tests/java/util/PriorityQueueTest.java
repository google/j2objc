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

package org.apache.harmony.tests.java.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;

import junit.framework.TestCase;
import libcore.java.util.SpliteratorTester;
import tests.util.SerializationTester;

public class PriorityQueueTest extends TestCase {

    private static final String SERIALIZATION_FILE_NAME = "serialization/org/apache/harmony/tests/java/util/PriorityQueue.golden.ser";

    /**
     * java.util.PriorityQueue#iterator()
     */
    public void test_iterator() {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        Integer[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.offer(array[i]);
        }
        Iterator<Integer> iter = integerQueue.iterator();
        assertNotNull(iter);
        ArrayList<Integer> iterResult = new ArrayList<Integer>();
        while (iter.hasNext()) {
            iterResult.add(iter.next());
        }
        Object[] resultArray = iterResult.toArray();
        Arrays.sort(array);
        Arrays.sort(resultArray);
        assertTrue(Arrays.equals(array, resultArray));
    }

    /**
     * java.util.PriorityQueue#iterator()
     */
    public void test_iterator_empty() {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        Iterator<Integer> iter = integerQueue.iterator();
        try {
            iter.next();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        iter = integerQueue.iterator();
        try {
            iter.remove();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#iterator()
     */
    public void test_iterator_outofbound() {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        integerQueue.offer(0);
        Iterator<Integer> iter = integerQueue.iterator();
        iter.next();
        try {
            iter.next();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        iter = integerQueue.iterator();
        iter.next();
        iter.remove();
        try {
            iter.next();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#iterator()
     */
    public void test_iterator_remove() {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        Integer[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.offer(array[i]);
        }
        Iterator<Integer> iter = integerQueue.iterator();
        assertNotNull(iter);
        for (int i = 0; i < array.length; i++) {
            iter.next();
            if (2 == i) {
                iter.remove();
            }
        }
        assertEquals(array.length - 1, integerQueue.size());

        iter = integerQueue.iterator();
        Integer[] newArray = new Integer[array.length - 1];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = iter.next();
        }

        Arrays.sort(newArray);
        for (int i = 0; i < integerQueue.size(); i++) {
            assertEquals(newArray[i], integerQueue.poll());
        }
    }

    public void test_iterator_removeEquals() {
        PriorityQueue<String> integerQueue = new PriorityQueue<String>(10, new MockComparatorStringByLength());
        String[] array = { "ONE", "TWO", "THREE", "FOUR", "FIVE" };
        for (int i = 0; i < array.length; i++) {
            integerQueue.offer(array[i]);
        }
        // Try removing an entry that the comparator says is equal
        assertFalse(integerQueue.remove("123"));
        assertFalse(integerQueue.remove("one"));
        assertTrue(integerQueue.remove("THREE"));
    }

    /**
     * java.util.PriorityQueue#iterator()
     */
    public void test_iterator_remove_illegalState() {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        Integer[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.offer(array[i]);
        }
        Iterator<Integer> iter = integerQueue.iterator();
        assertNotNull(iter);
        try {
            iter.remove();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        iter.next();
        iter.remove();
        try {
            iter.remove();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }

    }

    /**
     * java.util.PriorityQueue.size()
     */
    public void test_size() {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        assertEquals(0, integerQueue.size());
        int[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.offer(array[i]);
        }
        assertEquals(array.length, integerQueue.size());
    }

    /**
     * java.util.PriorityQueue#PriorityQueue()
     */
    public void test_Constructor() {
        PriorityQueue<Object> queue = new PriorityQueue<Object>();
        assertNotNull(queue);
        assertEquals(0, queue.size());
        assertNull(queue.comparator());
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(int)
     */
    public void test_ConstructorI() {
        PriorityQueue<Object> queue = new PriorityQueue<Object>(100);
        assertNotNull(queue);
        assertEquals(0, queue.size());
        assertNull(queue.comparator());
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(int, Comparator<? super E>)
     */
    public void test_ConstructorILjava_util_Comparator() {
        PriorityQueue<Object> queue = new PriorityQueue<Object>(100,
                (Comparator<Object>) null);
        assertNotNull(queue);
        assertEquals(0, queue.size());
        assertNull(queue.comparator());

        MockComparator<Object> comparator = new MockComparator<Object>();
        queue = new PriorityQueue<Object>(100, comparator);
        assertNotNull(queue);
        assertEquals(0, queue.size());
        assertEquals(comparator, queue.comparator());
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(int, Comparator<? super E>)
     */
    public void test_ConstructorILjava_util_Comparator_illegalCapacity() {
        try {
            new PriorityQueue<Object>(0, new MockComparator<Object>());
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new PriorityQueue<Object>(-1, new MockComparator<Object>());
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(int, Comparator<? super E>)
     */
    public void test_ConstructorILjava_util_Comparator_cast() {
        MockComparatorCast<Object> objectComparator = new MockComparatorCast<Object>();
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>(100,
                objectComparator);
        assertNotNull(integerQueue);
        assertEquals(0, integerQueue.size());
        assertEquals(objectComparator, integerQueue.comparator());
        Integer[] array = { 2, 45, 7, -12, 9 };
        List<Integer> list = Arrays.asList(array);
        integerQueue.addAll(list);
        assertEquals(list.size(), integerQueue.size());
        // just test here no cast exception raises.
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(Collection)
     */
    public void test_ConstructorLjava_util_Colleciton() {
        Integer[] array = { 2, 45, 7, -12, 9 };
        List<Integer> list = Arrays.asList(array);
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>(list);
        assertEquals(array.length, integerQueue.size());
        assertNull(integerQueue.comparator());
        Arrays.sort(array);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i], integerQueue.poll());
        }
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(Collection)
     */
    public void test_ConstructorLjava_util_Colleciton_null() {
        ArrayList<Object> list = new ArrayList<Object>();
        list.add(new Float(11));
        list.add(null);
        list.add(new Integer(10));
        try {
            new PriorityQueue<Object>(list);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(Collection)
     */
    public void test_ConstructorLjava_util_Colleciton_non_comparable() {
        ArrayList<Object> list = new ArrayList<Object>();
        list.add(new Float(11));
        list.add(new Integer(10));
        try {
            new PriorityQueue<Object>(list);
            fail("should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(Collection)
     */
    public void test_ConstructorLjava_util_Colleciton_from_priorityqueue() {
        String[] array = { "AAAAA", "AA", "AAAA", "AAAAAAAA" };
        PriorityQueue<String> queue = new PriorityQueue<String>(4,
                new MockComparatorStringByLength());
        for (int i = 0; i < array.length; i++) {
            queue.offer(array[i]);
        }
        Collection<String> c = queue;
        PriorityQueue<String> constructedQueue = new PriorityQueue<String>(c);
        assertEquals(queue.comparator(), constructedQueue.comparator());
        while (queue.size() > 0) {
            assertEquals(queue.poll(), constructedQueue.poll());
        }
        assertEquals(0, constructedQueue.size());
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(Collection)
     */
    public void test_ConstructorLjava_util_Colleciton_from_sortedset() {
        int[] array = { 3, 5, 79, -17, 5 };
        TreeSet<Integer> treeSet = new TreeSet<Integer>(new MockComparator<Integer>());
        for (int i = 0; i < array.length; i++) {
            treeSet.add(array[i]);
        }
        Collection<? extends Integer> c = treeSet;
        PriorityQueue<Integer> queue = new PriorityQueue<Integer>(c);
        assertEquals(treeSet.comparator(), queue.comparator());
        Iterator<Integer> iter = treeSet.iterator();
        while (iter.hasNext()) {
            assertEquals(iter.next(), queue.poll());
        }
        assertEquals(0, queue.size());
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(PriorityQueue<? * extends
     *E>)
     */
    public void test_ConstructorLjava_util_PriorityQueue() {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        int[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.offer(array[i]);
        }
        PriorityQueue<Object> objectQueue = new PriorityQueue<Object>(
                integerQueue);
        assertEquals(integerQueue.size(), objectQueue.size());
        assertEquals(integerQueue.comparator(), objectQueue.comparator());
        Arrays.sort(array);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i], objectQueue.poll());
        }
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(PriorityQueue<? * extends
     *E>)
     */
    public void test_ConstructorLjava_util_PriorityQueue_null() {
        try {
            new PriorityQueue<Object>((PriorityQueue<Integer>) null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(SortedSet<? extends E>)
     */
    public void test_ConstructorLjava_util_SortedSet() {
        int[] array = { 3, 5, 79, -17, 5 };
        TreeSet<Integer> treeSet = new TreeSet<Integer>();
        for (int i = 0; i < array.length; i++) {
            treeSet.add(array[i]);
        }
        PriorityQueue<Integer> queue = new PriorityQueue<Integer>(treeSet);
        Iterator<Integer> iter = treeSet.iterator();
        while (iter.hasNext()) {
            assertEquals(iter.next(), queue.poll());
        }
    }

    /**
     * java.util.PriorityQueue#PriorityQueue(SortedSet<? extends E>)
     */
    public void test_ConstructorLjava_util_SortedSet_null() {
        try {
            new PriorityQueue<Integer>((SortedSet<? extends Integer>) null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#offer(Object)
     */
    public void test_offerLjava_lang_Object() {
        PriorityQueue<String> queue = new PriorityQueue<String>(10,
                new MockComparatorStringByLength());
        String[] array = { "AAAAA", "AA", "AAAA", "AAAAAAAA" };
        for (int i = 0; i < array.length; i++) {
            queue.offer(array[i]);
        }
        String[] sortedArray = { "AA", "AAAA", "AAAAA", "AAAAAAAA" };
        for (int i = 0; i < sortedArray.length; i++) {
            assertEquals(sortedArray[i], queue.poll());
        }
        assertEquals(0, queue.size());
        assertNull(queue.poll());
    }

    /**
     * java.util.PriorityQueue#offer(Object)
     */
    public void test_offerLjava_lang_Object_null() {
        PriorityQueue<Object> queue = new PriorityQueue<Object>();
        try {
            queue.offer(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#offer(Object)
     */
    public void test_offer_Ljava_lang_Object_non_Comparable() {
        PriorityQueue<Object> queue = new PriorityQueue<Object>();
        queue.offer(new Integer(10));
        try {
            queue.offer(new Float(1.3));
            fail("should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        queue = new PriorityQueue<Object>();
        queue.offer(new Integer(10));
        try {
            queue.offer(new Object());
            fail("should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#poll()
     */
    public void test_poll() {
        PriorityQueue<String> stringQueue = new PriorityQueue<String>();
        String[] array = { "MYTESTSTRING", "AAAAA", "BCDEF", "ksTRD", "AAAAA" };
        for (int i = 0; i < array.length; i++) {
            stringQueue.offer(array[i]);
        }
        Arrays.sort(array);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i], stringQueue.poll());
        }
        assertEquals(0, stringQueue.size());
        assertNull(stringQueue.poll());
    }

    /**
     * java.util.PriorityQueue#poll()
     */
    public void test_poll_empty() {
        PriorityQueue<Object> queue = new PriorityQueue<Object>();
        assertEquals(0, queue.size());
        assertNull(queue.poll());
    }

    /**
     * java.util.PriorityQueue#peek()
     */
    public void test_peek() {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        int[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.add(array[i]);
        }
        Arrays.sort(array);
        assertEquals(new Integer(array[0]), integerQueue.peek());
        assertEquals(new Integer(array[0]), integerQueue.peek());
    }

    /**
     * java.util.PriorityQueue#peek()
     */
    public void test_peek_empty() {
        PriorityQueue<Object> queue = new PriorityQueue<Object>();
        assertEquals(0, queue.size());
        assertNull(queue.peek());
        assertNull(queue.peek());
    }

    /**
     * java.util.PriorityQueue#Clear()
     */
    public void test_clear() {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        int[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.offer(array[i]);
        }
        integerQueue.clear();
        assertTrue(integerQueue.isEmpty());
    }

    /**
     * java.util.PriorityQueue#add(Object)
     */
    public void test_add_Ljava_lang_Object() {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        Integer[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.add(array[i]);
        }
        Arrays.sort(array);
        assertEquals(array.length, integerQueue.size());
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i], integerQueue.poll());
        }
        assertEquals(0, integerQueue.size());
    }

    /**
     * java.util.PriorityQueue#add(Object)
     */
    public void test_add_Ljava_lang_Object_null() {
        PriorityQueue<Object> queue = new PriorityQueue<Object>();
        try {
            queue.add(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#add(Object)
     */
    public void test_add_Ljava_lang_Object_non_Comparable() {
        PriorityQueue<Object> queue = new PriorityQueue<Object>();
        queue.add(new Integer(10));
        try {
            queue.add(new Float(1.3));
            fail("should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        queue = new PriorityQueue<Object>();
        queue.add(new Integer(10));
        try {
            queue.add(new Object());
            fail("should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }
    }

    /**
     * java.util.PriorityQueue#remove(Object)
     */
    public void test_remove_Ljava_lang_Object() {
        Integer[] array = { 2, 45, 7, -12, 9, 23, 17, 1118, 10, 16, 39 };
        List<Integer> list = Arrays.asList(array);
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>(list);
        assertTrue(integerQueue.remove(16));
        Integer[] newArray = { 2, 45, 7, -12, 9, 23, 17, 1118, 10, 39 };
        Arrays.sort(newArray);
        for (int i = 0; i < newArray.length; i++) {
            assertEquals(newArray[i], integerQueue.poll());
        }
        assertEquals(0, integerQueue.size());
    }

    /**
     * java.util.PriorityQueue#remove(Object)
     */
    public void test_remove_Ljava_lang_Object_using_comparator() {
        PriorityQueue<String> queue = new PriorityQueue<String>(10,
                new MockComparatorStringByLength());
        String[] array = { "AAAAA", "AA", "AAAA", "AAAAAAAA" };
        for (int i = 0; i < array.length; i++) {
            queue.offer(array[i]);
        }
        assertFalse(queue.contains("BB"));
        assertTrue(queue.remove("AA"));
    }

    /**
     * java.util.PriorityQueue#remove(Object)
     */
    public void test_remove_Ljava_lang_Object_not_exists() {
        Integer[] array = { 2, 45, 7, -12, 9, 23, 17, 1118, 10, 16, 39 };
        List<Integer> list = Arrays.asList(array);
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>(list);
        assertFalse(integerQueue.remove(111));
        assertFalse(integerQueue.remove(null));
        assertFalse(integerQueue.remove(""));
    }

    /**
     * java.util.PriorityQueue#remove(Object)
     */
    public void test_remove_Ljava_lang_Object_null() {
        Integer[] array = { 2, 45, 7, -12, 9, 23, 17, 1118, 10, 16, 39 };
        List<Integer> list = Arrays.asList(array);
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>(list);
        assertFalse(integerQueue.remove(null));
    }

    /**
     * java.util.PriorityQueue#remove(Object)
     */
    public void test_remove_Ljava_lang_Object_not_Compatible() {
        Integer[] array = { 2, 45, 7, -12, 9, 23, 17, 1118, 10, 16, 39 };
        List<Integer> list = Arrays.asList(array);
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>(list);
        assertFalse(integerQueue.remove(new Float(1.3F)));

        // although argument element type is not compatible with those in queue,
        // but comparator supports it.
        MockComparator<Object> comparator = new MockComparator<Object>();
        PriorityQueue<Integer> integerQueue1 = new PriorityQueue<Integer>(100,
                comparator);
        integerQueue1.offer(1);
        assertFalse(integerQueue1.remove(new Float(1.3F)));

        PriorityQueue<Object> queue = new PriorityQueue<Object>();
        Object o = new Object();
        queue.offer(o);
        assertTrue(queue.remove(o));
    }

    /**
     * java.util.PriorityQueue#comparator()
     */
    public void test_comparator() {
        PriorityQueue<Object> queue = new PriorityQueue<Object>();
        assertNull(queue.comparator());

        MockComparator<Object> comparator = new MockComparator<Object>();
        queue = new PriorityQueue<Object>(100, comparator);
        assertEquals(comparator, queue.comparator());
    }

    /**
     * serialization/deserialization.
     */
    public void test_Serialization() throws Exception {
        Integer[] array = { 2, 45, 7, -12, 9, 23, 17, 1118, 10, 16, 39 };
        List<Integer> list = Arrays.asList(array);
        PriorityQueue<Integer> srcIntegerQueue = new PriorityQueue<Integer>(
                list);
        PriorityQueue<Integer> destIntegerQueue = (PriorityQueue<Integer>) SerializationTester
                .getDeserilizedObject(srcIntegerQueue);
        Arrays.sort(array);
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i], destIntegerQueue.poll());
        }
        assertEquals(0, destIntegerQueue.size());
    }

    /**
     * serialization/deserialization.
     */
    public void test_Serialization_casting() throws Exception {
        Integer[] array = { 2, 45, 7, -12, 9, 23, 17, 1118, 10, 16, 39 };
        List<Integer> list = Arrays.asList(array);
        PriorityQueue<Integer> srcIntegerQueue = new PriorityQueue<Integer>(
                list);
        PriorityQueue<String> destStringQueue = (PriorityQueue<String>) SerializationTester
                .getDeserilizedObject(srcIntegerQueue);
        // will not incur class cast exception.
        Object o = destStringQueue.peek();
        Arrays.sort(array);
        Integer I = (Integer) o;
        assertEquals(array[0], I);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void test_SerializationCompatibility_cast() throws Exception {
        Integer[] array = { 2, 45, 7, -12, 9, 23, 17, 1118, 10, 16, 39 };
        List<Integer> list = Arrays.asList(array);
        PriorityQueue<Integer> srcIntegerQueue = new PriorityQueue<Integer>(
                list);
        PriorityQueue<String> destStringQueue = (PriorityQueue<String>) SerializationTester
                .readObject(srcIntegerQueue, SERIALIZATION_FILE_NAME);

        // will not incur class cast exception.
        Object o = destStringQueue.peek();
        Arrays.sort(array);
        Integer I = (Integer) o;
        assertEquals(array[0], I);
    }

    /**
     * {@link PriorityQueue#contains(Object)}
     */
    public void test_contains() throws Exception {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        Integer[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.add(array[i]);
        }
        for (int i = 0; i < array.length; i++) {
            assertTrue(integerQueue.contains(array[i]));
        }
        assertFalse(integerQueue.contains(null));
    }

    /**
     * {@link PriorityQueue#toArray()}
     */
    public void test_toArray() throws Exception {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        Integer[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.add(array[i]);
        }
        Object[] returnArray = integerQueue.toArray();
        assertEquals(returnArray.length, integerQueue.size());
        for (int i = 0; i < returnArray.length; i++) {
            assertTrue(integerQueue.contains(returnArray[i]));
        }
    }

    /**
     * {@link PriorityQueue#toArray(T[])}
     */
    public void test_toArray_$T() throws Exception {
        PriorityQueue<Integer> integerQueue = new PriorityQueue<Integer>();
        Integer[] array = { 2, 45, 7, -12, 9 };
        for (int i = 0; i < array.length; i++) {
            integerQueue.add(array[i]);
        }
        Object[] returnArray = integerQueue.toArray(new Integer[0]);
        assertEquals(returnArray.length, integerQueue.size());
        for (int i = 0; i < returnArray.length; i++) {
            assertTrue(integerQueue.contains(returnArray[i]));
        }
        returnArray = integerQueue.toArray(new Integer[10]);
        assertEquals(10, returnArray.length);
        for (int i = 0; i < array.length; i++) {
            assertTrue(integerQueue.contains(returnArray[i]));
        }
        for (int i = array.length; i < 10; i++) {
            assertNull(returnArray[i]);
        }
        try {
            integerQueue.toArray(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            integerQueue.toArray(new String[1]);
            fail("should throw ArrayStoreException");
        } catch (ArrayStoreException e) {
            // expected
        }
    }

    public void test_spliterator() throws Exception {
        ArrayList<Integer> testElements = new ArrayList<>(
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16));
        PriorityQueue<Integer> list = new PriorityQueue<>();
        list.addAll(testElements);

        SpliteratorTester.runBasicIterationTests(list.spliterator(), testElements);
        SpliteratorTester.runBasicSplitTests(list, testElements);
        SpliteratorTester.testSpliteratorNPE(list.spliterator());

        assertTrue(list.spliterator().hasCharacteristics(
                Spliterator.SIZED | Spliterator.SUBSIZED));

        SpliteratorTester.runSizedTests(list, 16 /* expected size */);
        SpliteratorTester.runSubSizedTests(list, 16 /* expected size */);
        SpliteratorTester.assertSupportsTrySplit(list);
    }

    public void test_spliterator_CME() throws Exception {
        PriorityQueue<Integer> list = new PriorityQueue<>();
        list.add(52);

        Spliterator<Integer> sp = list.spliterator();
        try {
            sp.tryAdvance(value -> list.add(value));
            fail();
        } catch (ConcurrentModificationException expected) {
        }

        try {
            sp.forEachRemaining(value -> list.add(value));
            fail();
        } catch (ConcurrentModificationException expected) {
        }
    }


    private static class MockComparator<E> implements Comparator<E> {

        public int compare(E object1, E object2) {
            int hashcode1 = object1.hashCode();
            int hashcode2 = object2.hashCode();
            if (hashcode1 > hashcode2) {
                return 1;
            } else if (hashcode1 == hashcode2) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    private static class MockComparatorStringByLength implements
            Comparator<String> {

        public int compare(String object1, String object2) {
            int length1 = object1.length();
            int length2 = object2.length();
            if (length1 > length2) {
                return 1;
            } else if (length1 == length2) {
                return 0;
            } else {
                return -1;
            }
        }

    }

    private static class MockComparatorCast<E> implements Comparator<E> {

        public int compare(E object1, E object2) {
            return 0;
        }
    }

}
