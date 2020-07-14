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

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Spliterator;

import junit.framework.TestCase;

import libcore.java.util.ForEachRemainingTester;
import libcore.java.util.SpliteratorTester;
import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class ArrayDequeTest extends TestCase {

    private Object testObjOne;

    private Object testObjTwo;

    private Object testObjThree;

    private Object testObjFour;

    private Object testObjLast;

    private ArrayDeque<Object> testQue;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testObjOne = new Object();
        testObjTwo = new Object();
        testObjThree = new Object();
        testObjFour = new Object();
        testObjLast = new Object();
        testQue = new ArrayDeque<Object>();
    }

    /**
     * {@link java.util.ArrayDeque#ArrayDeque()}
     */
    public void test_Constructor() throws Exception {
        assertEquals(0, new ArrayDeque<Object>().size());
    }

    /**
     * {@link java.util.ArrayDeque#ArrayDeque(java.util.Collection)}
     */
    public void test_Constructor_LCollection() throws Exception {
        assertEquals(0, new ArrayDeque<Object>(new ArrayList<Object>()).size());
        try {
            new ArrayDeque<Object>(null);
            fail("should throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#ArrayDeque(int)}
     */
    public void test_Constructor_Int() throws Exception {
        assertEquals(0, new ArrayDeque<Object>(8).size());
        ArrayDeque<Object> zeroCapQue = new ArrayDeque<Object>(0);
        assertEquals(0, zeroCapQue.size());
        zeroCapQue.add(testObjOne);
        assertEquals(1, zeroCapQue.size());
        assertEquals(0, new ArrayDeque<Object>(0).size());
        ArrayDeque<Object> negCapQue = new ArrayDeque<Object>(-1);
        assertEquals(0, negCapQue.size());
        negCapQue.add(testObjOne);
        assertEquals(1, negCapQue.size());
        ArrayDeque<Object> oneCapQue = new ArrayDeque<Object>(1);
        assertEquals(0, oneCapQue.size());
        oneCapQue.add(testObjOne);
        assertEquals(1, oneCapQue.size());
        oneCapQue.add(testObjOne);
        assertEquals(2, oneCapQue.size());
    }

    /**
     * {@link java.util.ArrayDeque#addFirst(Object)}
     */
    public void test_addFirst() throws Exception {
        testQue.addFirst(testObjOne);
        assertEquals(1, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        testQue.addFirst(testObjOne);
        assertEquals(2, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        testQue.addFirst(testObjTwo);
        assertEquals(3, testQue.size());
        assertEquals(testObjTwo, testQue.peek());
        assertEquals(testObjOne, testQue.getLast());
        try {
            testQue.addFirst(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#addLast(Object)}
     */
    public void test_addLast() throws Exception {
        testQue.addLast(testObjOne);
        assertEquals(1, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        testQue.addLast(testObjOne);
        assertEquals(2, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        testQue.addLast(testObjTwo);
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        assertEquals(testObjTwo, testQue.getLast());
        try {
            testQue.addLast(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#offerFirst(Object)}
     */
    public void test_offerFirst() throws Exception {
        assertTrue(testQue.offerFirst(testObjOne));
        assertEquals(1, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        assertTrue(testQue.offerFirst(testObjOne));
        assertEquals(2, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        assertTrue(testQue.offerFirst(testObjTwo));
        assertEquals(3, testQue.size());
        assertEquals(testObjTwo, testQue.peek());
        assertEquals(testObjOne, testQue.getLast());
        try {
            testQue.offerFirst(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#offerLast(Object)}
     */
    public void test_offerLast() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertEquals(1, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        assertTrue(testQue.offerLast(testObjOne));
        assertEquals(2, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        assertTrue(testQue.offerLast(testObjTwo));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        assertEquals(testObjTwo, testQue.getLast());
        try {
            testQue.offerLast(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#removeFirst()}
     */
    public void test_removeFirst() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.removeFirst());
        assertEquals(2, testQue.size());
        assertEquals(testObjTwo, testQue.removeFirst());
        assertEquals(testObjThree, testQue.removeFirst());
        assertEquals(0, testQue.size());
        try {
            testQue.removeFirst();
            fail("should throw NoSuchElementException ");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#removeLast()}
     */
    public void test_removeLast() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjThree, testQue.removeLast());
        assertEquals(2, testQue.size());
        assertEquals(testObjTwo, testQue.removeLast());
        assertEquals(testObjOne, testQue.removeLast());
        assertEquals(0, testQue.size());
        try {
            testQue.removeLast();
            fail("should throw NoSuchElementException ");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#pollFirst()}
     */
    public void test_pollFirst() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.pollFirst());
        assertEquals(2, testQue.size());
        assertEquals(testObjTwo, testQue.pollFirst());
        assertEquals(testObjThree, testQue.pollFirst());
        assertEquals(0, testQue.size());
        assertNull(testQue.pollFirst());
    }

    /**
     * {@link java.util.ArrayDeque#peekLast()}
     */
    public void test_pollLast() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjThree, testQue.pollLast());
        assertEquals(2, testQue.size());
        assertEquals(testObjTwo, testQue.pollLast());
        assertEquals(testObjOne, testQue.pollLast());
        assertEquals(0, testQue.size());
        assertNull(testQue.pollFirst());
    }

    /**
     * {@link java.util.ArrayDeque#getFirst()}
     */
    public void test_getFirst() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.getFirst());
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.pollFirst());
        assertEquals(testObjTwo, testQue.getFirst());
        assertEquals(testObjTwo, testQue.pollFirst());
        assertEquals(testObjThree, testQue.pollFirst());
        assertEquals(0, testQue.size());
        try {
            testQue.getFirst();
            fail("should throw NoSuchElementException ");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#getLast()}
     */
    public void test_getLast() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjThree, testQue.getLast());
        assertEquals(3, testQue.size());
        assertEquals(testObjThree, testQue.pollLast());
        assertEquals(testObjTwo, testQue.getLast());
        assertEquals(testObjTwo, testQue.pollLast());
        assertEquals(testObjOne, testQue.pollLast());
        assertEquals(0, testQue.size());
        try {
            testQue.getLast();
            fail("should throw NoSuchElementException ");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#peekFirst()}
     */
    public void test_peekFirst() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.peekFirst());
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.pollFirst());
        assertEquals(testObjTwo, testQue.peekFirst());
        assertEquals(testObjTwo, testQue.pollFirst());
        assertEquals(testObjThree, testQue.pollFirst());
        assertEquals(0, testQue.size());
        assertEquals(null, testQue.peekFirst());
    }

    /**
     * {@link java.util.ArrayDeque#peekLast()}
     */
    public void test_peekLast() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjThree, testQue.peekLast());
        assertEquals(3, testQue.size());
        assertEquals(testObjThree, testQue.pollLast());
        assertEquals(testObjTwo, testQue.peekLast());
        assertEquals(testObjTwo, testQue.pollLast());
        assertEquals(testObjOne, testQue.pollLast());
        assertEquals(0, testQue.size());
        assertNull(testQue.peekLast());
    }

    /**
     * {@link java.util.ArrayDeque#removeFirstOccurrence(Object)}
     */
    public void test_removeFirstOccurrence() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjThree));
        assertTrue(testQue.offerLast(testObjOne));
        assertEquals(5, testQue.size());
        assertTrue(testQue.removeFirstOccurrence(testObjOne));
        assertFalse(testQue.removeFirstOccurrence(testObjFour));
        assertEquals(testObjTwo, testQue.peekFirst());
        assertEquals(testObjOne, testQue.peekLast());
        assertEquals(4, testQue.size());
        assertTrue(testQue.removeFirstOccurrence(testObjOne));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.peekLast());
        assertTrue(testQue.removeFirstOccurrence(testObjOne));
        assertEquals(2, testQue.size());
        assertEquals(testObjThree, testQue.peekLast());
        assertFalse(testQue.removeFirstOccurrence(testObjOne));
    }

    /**
     * {@link java.util.ArrayDeque#removeLastOccurrence(Object)}
     */
    public void test_removeLastOccurrence() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjThree));
        assertTrue(testQue.offerLast(testObjOne));
        assertEquals(5, testQue.size());
        assertTrue(testQue.removeLastOccurrence(testObjOne));
        assertFalse(testQue.removeLastOccurrence(testObjFour));
        assertEquals(testObjOne, testQue.peekFirst());
        assertEquals(testObjThree, testQue.peekLast());
        assertEquals(4, testQue.size());
        assertTrue(testQue.removeLastOccurrence(testObjOne));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.peekFirst());
        assertEquals(testObjThree, testQue.peekLast());
        assertTrue(testQue.removeLastOccurrence(testObjOne));
        assertEquals(2, testQue.size());
        assertEquals(testObjThree, testQue.peekLast());
        assertFalse(testQue.removeLastOccurrence(testObjOne));
    }

    /**
     * {@link java.util.ArrayDeque#add(Object)}
     */
    public void test_add() throws Exception {
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjTwo));
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjThree));
        assertEquals(testObjOne, testQue.peekFirst());
        assertEquals(testObjThree, testQue.peekLast());
        try {
            testQue.add(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#offer(Object)}
     */
    public void test_offer() throws Exception {
        assertTrue(testQue.offer(testObjOne));
        assertTrue(testQue.offer(testObjTwo));
        assertTrue(testQue.offer(testObjOne));
        assertTrue(testQue.offer(testObjThree));
        assertEquals(testObjOne, testQue.peekFirst());
        assertEquals(testObjThree, testQue.peekLast());
        try {
            testQue.offer(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#remove()}
     */
    public void test_remove() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.remove());
        assertEquals(2, testQue.size());
        assertEquals(testObjTwo, testQue.remove());
        assertEquals(testObjThree, testQue.remove());
        assertEquals(0, testQue.size());
        try {
            testQue.remove();
            fail("should throw NoSuchElementException ");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#poll()}
     */
    public void test_poll() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.poll());
        assertEquals(2, testQue.size());
        assertEquals(testObjTwo, testQue.poll());
        assertEquals(testObjThree, testQue.poll());
        assertEquals(0, testQue.size());
        assertNull(testQue.poll());
    }

    /**
     * {@link java.util.ArrayDeque#element()}
     */
    public void test_element() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.element());
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.pollFirst());
        assertEquals(testObjTwo, testQue.element());
        assertEquals(testObjTwo, testQue.pollFirst());
        assertEquals(testObjThree, testQue.element());
        assertEquals(testObjThree, testQue.pollFirst());
        assertEquals(0, testQue.size());
        try {
            testQue.element();
            fail("should throw NoSuchElementException ");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#peek()}
     */
    public void test_peek() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.pollFirst());
        assertEquals(testObjTwo, testQue.peek());
        assertEquals(testObjTwo, testQue.pollFirst());
        assertEquals(testObjThree, testQue.pollFirst());
        assertEquals(0, testQue.size());
        assertEquals(null, testQue.peek());
    }

    /**
     * {@link java.util.ArrayDeque#push(Object)}
     */
    public void test_push() throws Exception {
        testQue.push(testObjOne);
        assertEquals(1, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        testQue.push(testObjOne);
        assertEquals(2, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        testQue.push(testObjTwo);
        assertEquals(3, testQue.size());
        assertEquals(testObjTwo, testQue.peek());
        assertEquals(testObjOne, testQue.getLast());
        try {
            testQue.push(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#pop()}
     */
    public void test_pop() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjThree));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.pop());
        assertEquals(2, testQue.size());
        assertEquals(testObjTwo, testQue.pop());
        assertEquals(testObjThree, testQue.pop());
        assertEquals(0, testQue.size());
        try {
            testQue.pop();
            fail("should throw NoSuchElementException ");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * {@link java.util.ArrayDeque#addFirst(Object)}
     */
    public void test_size() throws Exception {
        assertEquals(0, testQue.size());
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjTwo));
        assertEquals(2, testQue.size());
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjThree));
        assertEquals(4, testQue.size());
        testQue.remove();
        testQue.remove();
        assertEquals(2, testQue.size());
        testQue.clear();
        assertEquals(0, testQue.size());
    }

    /**
     * {@link java.util.ArrayDeque#isEmpty()}
     */
    public void test_isEmpty() throws Exception {
        assertTrue(testQue.isEmpty());
        assertTrue(testQue.add(testObjOne));
        assertFalse(testQue.isEmpty());
        assertTrue(testQue.add(testObjTwo));
        assertFalse(testQue.isEmpty());
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjThree));
        assertFalse(testQue.isEmpty());
        testQue.remove();
        testQue.remove();
        assertFalse(testQue.isEmpty());
        testQue.clear();
        assertTrue(testQue.isEmpty());
    }

    /**
     * {@link java.util.ArrayDeque#iterator()}
     */
    public void test_iterator() throws Exception {
        assertFalse(testQue.iterator().hasNext());
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjTwo));
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjThree));
        assertTrue(testQue.add(testObjLast));
        Iterator result = testQue.iterator();
        assertEquals(5, testQue.size());
        try {
            result.remove();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        assertTrue(testQue.add(testObjThree));
        try {
            result.next();
            fail("should throw ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
            // expected
        }
        result = testQue.iterator();
        assertEquals(testObjOne, result.next());
        assertEquals(testObjTwo, result.next());
        assertEquals(testObjOne, result.next());
        assertEquals(testObjThree, result.next());
        assertEquals(testObjLast, result.next());
        assertTrue(result.hasNext());
        result.remove();
        assertEquals(testObjThree, result.next());
        assertFalse(result.hasNext());
        try {
            result.next();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
        // test a full array
        ArrayDeque<Object> ad = new ArrayDeque<Object>();
        // fill the array
        for (int i = 0; i < 16; ++i) {
            ad.addLast(new Object());
        }
        assertTrue(ad.iterator().hasNext());
        Iterator<Object> iter = ad.iterator();
        for (int i = 0; i < 16; ++i) {
            assertTrue(iter.hasNext());
            iter.next();
        }
        iter.remove();
        // test un-full array
        ad = new ArrayDeque<Object>();
        // fill the array
        for (int i = 0; i < 5; ++i) {
            ad.addLast(new Object());
        }
        iter = ad.iterator();
        for (int i = 0; i < 5; ++i) {
            assertTrue(iter.hasNext());
            iter.next();
        }
        iter.remove();

        ad = new ArrayDeque<Object>();
        // fill the array
        for (int i = 0; i < 16; ++i) {
            ad.addLast(new Object());
        }
        iter = ad.iterator();
        assertTrue(iter.hasNext());
        for (int i = 0; i < ad.size(); ++i) {
            iter.next();
        }
        assertFalse(iter.hasNext());
        iter.remove();
        ad.add(new Object());
        assertFalse(iter.hasNext());
    }

    /**
     * {@link java.util.ArrayDeque#descendingIterator()}
     */
    public void test_descendingIterator() throws Exception {
        assertFalse(testQue.descendingIterator().hasNext());
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjTwo));
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjThree));
        assertTrue(testQue.add(testObjLast));
        Iterator result = testQue.descendingIterator();
        assertEquals(5, testQue.size());
        try {
            result.remove();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        assertTrue(testQue.add(testObjFour));

        // a strange behavior here, RI's descendingIterator() and iterator() is
        // properly different. Notice spec: "The iterators returned by this
        // class's iterator method are fail-fast". RI shows descendingIterator()
        // is not an iterator method.
        assertEquals(testObjLast, result.next());

        result = testQue.descendingIterator();
        assertEquals(testObjFour, result.next());
        assertEquals(testObjLast, result.next());
        assertEquals(testObjThree, result.next());
        assertEquals(testObjOne, result.next());
        assertEquals(testObjTwo, result.next());
        assertTrue(result.hasNext());
        result.remove();
        assertEquals(testObjOne, result.next());
        assertFalse(result.hasNext());
        try {
            result.next();
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
        // test a full array
        ArrayDeque<Object> ad = new ArrayDeque<Object>();
        // fill the array
        for (int i = 0; i < 16; ++i) {
            ad.addLast(new Object());
        }
        assertTrue(ad.descendingIterator().hasNext());
        Iterator<Object> iter = ad.descendingIterator();
        for (int i = 0; i < 16; ++i) {
            assertTrue(iter.hasNext());
            iter.next();
        }
        iter.remove();
        // test un-full array
        ad = new ArrayDeque<Object>();
        // fill the array
        for (int i = 0; i < 5; ++i) {
            ad.addLast(new Object());
        }
        iter = ad.descendingIterator();
        for (int i = 0; i < 5; ++i) {
            assertTrue(iter.hasNext());
            iter.next();
        }
        iter.remove();

        ad = new ArrayDeque<Object>();
        // fill the array
        for (int i = 0; i < 16; ++i) {
            ad.addLast(new Object());
        }
        iter = ad.descendingIterator();
        assertTrue(iter.hasNext());
        for (int i = 0; i < ad.size(); ++i) {
            iter.next();
        }
        assertFalse(iter.hasNext());
        iter.remove();
        ad.add(new Object());
        assertFalse(iter.hasNext());
    }

    /**
     * {@link java.util.ArrayDeque#contains(Object)}
     */
    public void test_contains() throws Exception {
        assertFalse(testQue.contains(testObjFour));
        assertFalse(testQue.contains(null));
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjTwo));
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjThree));
        assertTrue(testQue.add(testObjLast));

        assertTrue(testQue.contains(testObjOne));
        assertTrue(testQue.contains(testObjTwo));
        assertTrue(testQue.contains(testObjThree));
        assertTrue(testQue.contains(testObjLast));
        assertFalse(testQue.contains(null));
        testQue.clear();
        assertFalse(testQue.contains(testObjOne));
        assertFalse(testQue.contains(testObjTwo));
    }

    /**
     * {@link java.util.ArrayDeque#remove(Object)}
     */
    public void test_remove_LObject() throws Exception {
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjTwo));
        assertTrue(testQue.offerLast(testObjOne));
        assertTrue(testQue.offerLast(testObjThree));
        assertTrue(testQue.offerLast(testObjOne));
        assertEquals(5, testQue.size());
        assertTrue(testQue.remove(testObjOne));
        assertFalse(testQue.remove(testObjFour));
        assertEquals(testObjTwo, testQue.peekFirst());
        assertEquals(testObjOne, testQue.peekLast());
        assertEquals(4, testQue.size());
        assertTrue(testQue.remove(testObjOne));
        assertEquals(3, testQue.size());
        assertEquals(testObjOne, testQue.peekLast());
        assertTrue(testQue.remove(testObjOne));
        assertEquals(2, testQue.size());
        assertEquals(testObjThree, testQue.peekLast());
        assertFalse(testQue.remove(testObjOne));
    }

    /**
     * {@link java.util.ArrayDeque#clear()}
     */
    public void test_clear() throws Exception {
        assertTrue(testQue.isEmpty());
        testQue.clear();
        assertTrue(testQue.isEmpty());
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjTwo));
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjThree));
        testQue.clear();
        assertTrue(testQue.isEmpty());
    }

    /**
     * {@link java.util.ArrayDeque#toArray()}
     */
    public void test_toArray() throws Exception {
        assertEquals(0, testQue.toArray().length);
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjTwo));
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjThree));
        assertTrue(testQue.add(testObjLast));
        Object[] result = testQue.toArray();
        assertEquals(5, testQue.size());
        assertEquals(testObjOne, result[0]);
        assertEquals(testObjTwo, result[1]);
        assertEquals(testObjOne, result[2]);
        assertEquals(testObjThree, result[3]);
        assertEquals(testObjLast, result[4]);
        // change in array do not affect ArrayDeque
        result[0] = null;
        assertEquals(5, testQue.size());
        assertEquals(testObjOne, testQue.peek());
    }

    /**
     * {@link java.util.ArrayDeque#toArray(Object[])}
     */
    public void test_toArray_$LObject() throws Exception {
        Object[] array = new Object[0];
        Object[] result = testQue.toArray(array);
        assertEquals(0, result.length);
        assertEquals(array, result);
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjTwo));
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjThree));
        assertTrue(testQue.add(testObjLast));
        result = testQue.toArray(array);
        assertEquals(5, testQue.size());
        assertEquals(5, result.length);
        assertEquals(0, array.length);
        assertFalse(array == result);
        assertEquals(testObjOne, result[0]);
        assertEquals(testObjTwo, result[1]);
        assertEquals(testObjOne, result[2]);
        assertEquals(testObjThree, result[3]);
        assertEquals(testObjLast, result[4]);
        // change in array do not affect ArrayDeque
        result[0] = null;
        assertEquals(5, testQue.size());
        assertEquals(testObjOne, testQue.peek());
        try {
            testQue.toArray(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

    }

    /**
     * {@link java.util.ArrayDeque#clone()}
     */
    public void test_clone() throws Exception {
        ArrayDeque<Object> cloned = testQue.clone();
        assertEquals(0, cloned.size());
        assertFalse(cloned == testQue);
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjTwo));
        assertTrue(testQue.add(testObjOne));
        assertTrue(testQue.add(testObjThree));
        assertTrue(testQue.add(testObjLast));
        assertTrue(testQue.add(testQue));
        cloned = testQue.clone();
        assertEquals(6, cloned.size());
        while (0 != testQue.size()) {
            assertEquals(testQue.remove(), cloned.remove());
        }
    }

    public void test_forEachRemaining_iterator() throws Exception {
        ForEachRemainingTester.test_forEachRemaining(new ArrayDeque<>(),
                new String[]{ "foo", "bar", "baz "});
        ForEachRemainingTester.test_forEachRemaining_NPE(new ArrayDeque<>(),
                new String[]{"foo", "bar", "baz "});
    }

    public void test_forEachRemaining_CME() throws Exception {
        ArrayDeque<String> adq = new ArrayDeque<>();
        adq.add("foo");

        // The ArrayDeque forEachRemaining implementation doesn't use a precise check
        // for concurrent modifications.
        adq.iterator().forEachRemaining(s -> adq.add(s));
    }

    public void test_spliterator() throws Exception {
        ArrayList<Integer> testElements = new ArrayList<>(
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16));
        ArrayDeque<Integer> adq = new ArrayDeque<>();
        adq.addAll(testElements);

        SpliteratorTester.runBasicIterationTests(adq.spliterator(), testElements);
        SpliteratorTester.runBasicSplitTests(adq, testElements);
        SpliteratorTester.testSpliteratorNPE(adq.spliterator());

        assertTrue(adq.spliterator().hasCharacteristics(
                Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED));

        SpliteratorTester.runOrderedTests(adq);
        SpliteratorTester.runSizedTests(adq, 16 /* expected size */);
        SpliteratorTester.runSubSizedTests(adq, 16 /* expected size */);
        SpliteratorTester.assertSupportsTrySplit(adq);
    }

    public void test_spliterator_CME() throws Exception {
        ArrayDeque<Integer> adq = new ArrayDeque<>();
        adq.add(52);

        Spliterator<Integer> sp = adq.spliterator();

        // Spliterators from ArrayDequeues never throw CME. The following statements
        // would have thrown a CME on most other collection classes.
        assertTrue(sp.tryAdvance(value -> adq.add(value)));
        sp.forEachRemaining(value -> adq.add(value));
    }

    /**
     * java.util.ArrayDeque#Serialization()
     */
    public void test_serialization() throws Exception {
        assertTrue(testQue.add(new Integer(1)));
        assertTrue(testQue.add(new Integer(2)));
        assertTrue(testQue.add(new Integer(3)));
        assertTrue(testQue.add(new Integer(4)));
        assertTrue(testQue.add(new Integer(5)));
        SerializationTest.verifySelf(testQue, new SerializableAssert() {
            public void assertDeserialized(Serializable initial,
                    Serializable deserialized) {
                ArrayDeque<Object> formerQue = (ArrayDeque) initial;
                ArrayDeque<Object> deserializedQue = (ArrayDeque) deserialized;
                assertEquals(formerQue.remove(), deserializedQue.remove());
            }
        });
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    @SuppressWarnings({ "unchecked", "boxing" })
    public void testSerializationCompatibility() throws Exception {
        assertTrue(testQue.add(new Integer(1)));
        assertTrue(testQue.add(new Integer(2)));
        assertTrue(testQue.add(new Integer(3)));
        assertTrue(testQue.add(new Integer(4)));
        assertTrue(testQue.add(new Integer(5)));
        SerializationTest.verifyGolden(this, testQue, new SerializableAssert() {
            public void assertDeserialized(Serializable initial,
                    Serializable deserialized) {
                ArrayDeque<Object> formerQue = (ArrayDeque) initial;
                ArrayDeque<Object> deserializedQue = (ArrayDeque) deserialized;
                assertEquals(formerQue.remove(), deserializedQue.remove());
            }
        });
    }
}
