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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;

import libcore.java.util.ForEachRemainingTester;
import libcore.java.util.SpliteratorTester;
import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

import tests.support.Support_ListTest;

import static libcore.java.util.RemoveIfTester.*;

public class LinkedListTest extends junit.framework.TestCase {

    LinkedList ll;

    LinkedList<Object> testList;

    private Object testObjOne;

    private Object testObjTwo;

    private Object testObjThree;

    private Object testObjFour;

    private Object testObjLast;

    Object[] objArray;

    /**
     * java.util.LinkedList#LinkedList()
     */
    public void test_Constructor() {
        // Test for method java.util.LinkedList()
        new Support_ListTest("", ll).runTest();

        LinkedList subList = new LinkedList();
        for (int i = -50; i < 150; i++)
            subList.add(new Integer(i));
        new Support_ListTest("", subList.subList(50, 150)).runTest();
    }

    /**
     * java.util.LinkedList#LinkedList(java.util.Collection)
     */
    public void test_ConstructorLjava_util_Collection() {
        // Test for method java.util.LinkedList(java.util.Collection)
        assertTrue("Incorrect LinkedList constructed", new LinkedList(ll)
                .equals(ll));

        try {
            new LinkedList(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.LinkedList#add(int, java.lang.Object)
     */
    public void test_addILjava_lang_Object() {
        // Test for method void java.util.LinkedList.add(int, java.lang.Object)
        Object o;
        ll.add(50, o = "Test");
        assertTrue("Failed to add Object>: " + ll.get(50).toString(), ll
                .get(50) == o);
        assertTrue("Failed to fix up list after insert",
                ll.get(51) == objArray[50] && (ll.get(52) == objArray[51]));
        ll.add(50, null);
        assertNull("Did not add null correctly", ll.get(50));

        try {
            ll.add(-1, "Test");
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            ll.add(-1, null);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            ll.add(ll.size() + 1, "Test");
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            ll.add(ll.size() + 1, null);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Excepted
        }
    }

    /**
     * java.util.LinkedList#add(java.lang.Object)
     */
    public void test_addLjava_lang_Object() {
        // Test for method boolean java.util.LinkedList.add(java.lang.Object)
        Object o;
        ll.add(o = new Object());
        assertTrue("Failed to add Object", ll.getLast() == o);
        ll.add(null);
        assertNull("Did not add null correctly", ll.get(ll.size() - 1));
    }

    /**
     * java.util.LinkedList#addAll(int, java.util.Collection)
     */
    public void test_addAllILjava_util_Collection() {
        // Test for method boolean java.util.LinkedList.addAll(int,
        // java.util.Collection)
        ll.addAll(50, (Collection) ll.clone());
        assertEquals("Returned incorrect size after adding to existing list", 200, ll
                .size());
        for (int i = 0; i < 50; i++)
            assertTrue("Manipulated elements < index", ll.get(i) == objArray[i]);
        for (int i = 0; i >= 50 && (i < 150); i++)
            assertTrue("Failed to ad elements properly",
                    ll.get(i) == objArray[i - 50]);
        for (int i = 0; i >= 150 && (i < 200); i++)
            assertTrue("Failed to ad elements properly",
                    ll.get(i) == objArray[i - 100]);
        List myList = new LinkedList();
        myList.add(null);
        myList.add("Blah");
        myList.add(null);
        myList.add("Booga");
        myList.add(null);
        ll.addAll(50, myList);
        assertNull("a) List w/nulls not added correctly", ll.get(50));
        assertEquals("b) List w/nulls not added correctly",
                "Blah", ll.get(51));
        assertNull("c) List w/nulls not added correctly", ll.get(52));
        assertEquals("d) List w/nulls not added correctly",
                "Booga", ll.get(53));
        assertNull("e) List w/nulls not added correctly", ll.get(54));

        try {
            ll.addAll(-1, (Collection) null);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }

        try {
            ll.addAll(ll.size() + 1, (Collection) null);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }

        try {
            ll.addAll(0, null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.LinkedList#addAll(int, java.util.Collection)
     */
    public void test_addAllILjava_util_Collection_2() {
        // Regression for HARMONY-467
        LinkedList obj = new LinkedList();
        try {
            obj.addAll(-1, (Collection) null);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * java.util.LinkedList#addAll(java.util.Collection)
     */
    public void test_addAllLjava_util_Collection() {
        // Test for method boolean
        // java.util.LinkedList.addAll(java.util.Collection)
        List l = new ArrayList();
        l.addAll((Collection) ll.clone());
        for (int i = 0; i < ll.size(); i++)
            assertTrue("Failed to add elements properly", l.get(i).equals(
                    ll.get(i)));
        ll.addAll((Collection) ll.clone());
        assertEquals("Returned incorrect siZe after adding to existing list", 200, ll
                .size());
        for (int i = 0; i < 100; i++) {
            assertTrue("Added to list in incorrect order", ll.get(i).equals(
                    l.get(i)));
            assertTrue("Failed to add to existing list", ll.get(i + 100)
                    .equals(l.get(i)));
        }
        List myList = new LinkedList();
        myList.add(null);
        myList.add("Blah");
        myList.add(null);
        myList.add("Booga");
        myList.add(null);
        ll.addAll(myList);
        assertNull("a) List w/nulls not added correctly", ll.get(200));
        assertEquals("b) List w/nulls not added correctly",
                "Blah", ll.get(201));
        assertNull("c) List w/nulls not added correctly", ll.get(202));
        assertEquals("d) List w/nulls not added correctly",
                "Booga", ll.get(203));
        assertNull("e) List w/nulls not added correctly", ll.get(204));

        try {
            ll.addAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Excepted
        }
    }

    public void test_addAll_Self_Ljava_util_Collection() {
        LinkedList linkedList = new LinkedList();
        linkedList.addLast(1);
        assertEquals(1, linkedList.size());
        assertTrue(linkedList.addAll(linkedList));
        assertEquals(2, linkedList.size());
    }

    public void test_addAll_Self_ILjava_util_Collection() {
        LinkedList linkedList = new LinkedList();
        linkedList.addLast(1);
        assertEquals(1, linkedList.size());
        assertTrue(linkedList.addAll(1, linkedList));
        assertEquals(2, linkedList.size());
    }

    /**
     * java.util.LinkedList#addFirst(java.lang.Object)
     */
    public void test_addFirstLjava_lang_Object() {
        // Test for method void java.util.LinkedList.addFirst(java.lang.Object)
        Object o;
        ll.addFirst(o = new Object());
        assertTrue("Failed to add Object", ll.getFirst() == o);
        ll.addFirst(null);
        assertNull("Failed to add null", ll.getFirst());
    }

    /**
     * java.util.LinkedList#addLast(java.lang.Object)
     */
    public void test_addLastLjava_lang_Object() {
        // Test for method void java.util.LinkedList.addLast(java.lang.Object)
        Object o;
        ll.addLast(o = new Object());
        assertTrue("Failed to add Object", ll.getLast() == o);
        ll.addLast(null);
        assertNull("Failed to add null", ll.getLast());
    }

    /**
     * java.util.LinkedList#clear()
     */
    public void test_clear() {
        // Test for method void java.util.LinkedList.clear()
        ll.clear();
        for (int i = 0; i < ll.size(); i++)
            assertNull("Failed to clear list", ll.get(i));
    }

    /**
     * java.util.LinkedList#clone()
     */
    public void test_clone() {
        // Test for method java.lang.Object java.util.LinkedList.clone()
        Object x = ll.clone();
        assertTrue("Cloned list was inequal to cloned", x.equals(ll));
        for (int i = 0; i < ll.size(); i++)
            assertTrue("Cloned list contains incorrect elements", ll.get(i)
                    .equals(((LinkedList) x).get(i)));
        ll.addFirst(null);
        x = ll.clone();
        assertTrue("List with a null did not clone properly", ll.equals(x));
    }

    /**
     * java.util.LinkedList#contains(java.lang.Object)
     */
    public void test_containsLjava_lang_Object() {
        // Test for method boolean
        // java.util.LinkedList.contains(java.lang.Object)
        assertTrue("Returned false for valid element", ll
                .contains(objArray[99]));
        assertTrue("Returned false for equal element", ll.contains(new Integer(
                8)));
        assertTrue("Returned true for invalid element", !ll
                .contains(new Object()));
        assertTrue("Should not contain null", !ll.contains(null));
        ll.add(25, null);
        assertTrue("Should contain null", ll.contains(null));
    }

    /**
     * java.util.LinkedList#get(int)
     */
    public void test_getI() {
        // Test for method java.lang.Object java.util.LinkedList.get(int)
        assertTrue("Returned incorrect element", ll.get(22) == objArray[22]);
        try {
            ll.get(8765);
            fail("Failed to throw expected exception for index > size");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * {@link java.util.LinkedList#peek()}
     */
    public void test_peek() {
        LinkedList list = new LinkedList();

        assertNull("Should return null if this list is empty", list.peek());

        assertEquals("Returned incorrect first element", ll.peek(), objArray[0]);
        assertEquals("Peek remove the head (first element) of this list", ll.getFirst(), objArray[0]);
    }

    /**
     * java.util.LinkedList#getFirst()
     */
    public void test_getFirst() {
        // Test for method java.lang.Object java.util.LinkedList.getFirst()
        assertTrue("Returned incorrect first element", ll.getFirst().equals(
                objArray[0]));

        LinkedList list = new LinkedList();
        try {
            list.getFirst();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Excepted
        }
    }

    /**
     * java.util.LinkedList#getLast()
     */
    public void test_getLast() {
        // Test for method java.lang.Object java.util.LinkedList.getLast()
        assertTrue("Returned incorrect first element", ll.getLast().equals(
                objArray[objArray.length - 1]));

        LinkedList list = new LinkedList();
        try {
            list.getLast();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Excepted
        }
    }

    /**
     * java.util.LinkedList#indexOf(java.lang.Object)
     */
    public void test_indexOfLjava_lang_Object() {
        // Test for method int java.util.LinkedList.indexOf(java.lang.Object)
        assertEquals("Returned incorrect index", 87, ll.indexOf(objArray[87]));
        assertEquals("Returned index for invalid Object", -1, ll
                .indexOf(new Object()));
        ll.add(20, null);
        ll.add(24, null);
        assertTrue("Index of null should be 20, but got: " + ll.indexOf(null),
                ll.indexOf(null) == 20);
    }

    /**
     * java.util.LinkedList#lastIndexOf(java.lang.Object)
     */
    public void test_lastIndexOfLjava_lang_Object() {
        // Test for method int
        // java.util.LinkedList.lastIndexOf(java.lang.Object)
        ll.add(new Integer(99));
        assertEquals("Returned incorrect index",
                100, ll.lastIndexOf(objArray[99]));
        assertEquals("Returned index for invalid Object", -1, ll
                .lastIndexOf(new Object()));
        ll.add(20, null);
        ll.add(24, null);
        assertTrue("Last index of null should be 20, but got: "
                + ll.lastIndexOf(null), ll.lastIndexOf(null) == 24);
    }

    /**
     * java.util.LinkedList#listIterator(int)
     */
    public void test_listIteratorI() {
        // Test for method java.util.ListIterator
        // java.util.LinkedList.listIterator(int)
        ListIterator i1 = ll.listIterator();
        ListIterator i2 = ll.listIterator(0);
        Object elm;
        int n = 0;
        while (i2.hasNext()) {
            if (n == 0 || n == objArray.length - 1) {
                if (n == 0)
                    assertTrue("First element claimed to have a previous", !i2
                            .hasPrevious());
                if (n == objArray.length)
                    assertTrue("Last element claimed to have next", !i2
                            .hasNext());
            }
            elm = i2.next();
            assertTrue("Iterator returned elements in wrong order",
                    elm == objArray[n]);
            if (n > 0 && n < objArray.length - 1) {
                assertTrue("Next index returned incorrect value",
                        i2.nextIndex() == n + 1);
                assertTrue("previousIndex returned incorrect value : "
                        + i2.previousIndex() + ", n val: " + n, i2
                        .previousIndex() == n);
            }
            elm = i1.next();
            assertTrue("Iterator returned elements in wrong order",
                    elm == objArray[n]);
            ++n;
        }

        i2 = ll.listIterator(ll.size()/2);
        assertTrue((Integer)i2.next() == ll.size()/2);
        List myList = new LinkedList();
        myList.add(null);
        myList.add("Blah");
        myList.add(null);
        myList.add("Booga");
        myList.add(null);
        ListIterator li = myList.listIterator();
        assertTrue("li.hasPrevious() should be false", !li.hasPrevious());
        assertNull("li.next() should be null", li.next());
        assertTrue("li.hasPrevious() should be true", li.hasPrevious());
        assertNull("li.prev() should be null", li.previous());
        assertNull("li.next() should be null", li.next());
        assertEquals("li.next() should be Blah", "Blah", li.next());
        assertNull("li.next() should be null", li.next());
        assertEquals("li.next() should be Booga", "Booga", li.next());
        assertTrue("li.hasNext() should be true", li.hasNext());
        assertNull("li.next() should be null", li.next());
        assertTrue("li.hasNext() should be false", !li.hasNext());

        try {
            ll.listIterator(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }

        try {
            ll.listIterator(ll.size() + 1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.LinkedList#remove(int)
     */
    public void test_removeI() {
        // Test for method java.lang.Object java.util.LinkedList.remove(int)
        ll.remove(10);
        assertEquals("Failed to remove element", -1, ll.indexOf(objArray[10]));
        try {
            ll.remove(999);
            fail("Failed to throw expected exception when index out of range");
        } catch (IndexOutOfBoundsException e) {
            // Correct
        }

        ll.add(20, null);
        ll.remove(20);
        assertNotNull("Should have removed null", ll.get(20));
    }

    /**
     * java.util.LinkedList#remove(java.lang.Object)
     */
    public void test_removeLjava_lang_Object() {
        // Test for method boolean java.util.LinkedList.remove(java.lang.Object)
        assertTrue("Failed to remove valid Object", ll.remove(objArray[87]));
        assertTrue("Removed invalid object", !ll.remove(new Object()));
        assertEquals("Found Object after removal", -1, ll.indexOf(objArray[87]));
        ll.add(null);
        ll.remove(null);
        assertTrue("Should not contain null afrer removal", !ll.contains(null));
    }

    /**
     * java.util.LinkedList#removeFirst()
     */
    public void test_removeFirst() {
        // Test for method java.lang.Object java.util.LinkedList.removeFirst()
        ll.removeFirst();
        assertTrue("Failed to remove first element",
                ll.getFirst() != objArray[0]);

        LinkedList list = new LinkedList();
        try {
            list.removeFirst();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Excepted
        }
    }

    /**
     * java.util.LinkedList#removeLast()
     */
    public void test_removeLast() {
        // Test for method java.lang.Object java.util.LinkedList.removeLast()
        ll.removeLast();
        assertTrue("Failed to remove last element",
                ll.getLast() != objArray[objArray.length - 1]);

        LinkedList list = new LinkedList();
        try {
            list.removeLast();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Excepted
        }
    }

    /**
     * java.util.LinkedList#set(int, java.lang.Object)
     */
    public void test_setILjava_lang_Object() {
        // Test for method java.lang.Object java.util.LinkedList.set(int,
        // java.lang.Object)
        Object obj;
        ll.set(65, obj = new Object());
        assertTrue("Failed to set object", ll.get(65) == obj);

        try {
            ll.set(-1, obj = new Object());
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }

        try {
            ll.set(ll.size() + 1, obj = new Object());
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.LinkedList#size()
     */
    public void test_size() {
        // Test for method int java.util.LinkedList.size()
        assertTrue("Returned incorrect size", ll.size() == objArray.length);
        ll.removeFirst();
        assertTrue("Returned incorrect size", ll.size() == objArray.length - 1);
    }

    /**
     * java.util.LinkedList#toArray()
     */
    public void test_toArray() {
        // Test for method java.lang.Object [] java.util.LinkedList.toArray()
        ll.add(null);
        Object[] obj = ll.toArray();
        assertEquals("Returned array of incorrect size", objArray.length + 1, obj.length);

        for (int i = 0; i < obj.length - 1; i++)
            assertTrue("Returned incorrect array: " + i, obj[i] == objArray[i]);
        assertNull("Returned incorrect array--end isn't null",
                obj[obj.length - 1]);
    }

    /**
     * java.util.LinkedList#toArray(java.lang.Object[])
     */
    public void test_toArray$Ljava_lang_Object() {
        // Test for method java.lang.Object []
        // java.util.LinkedList.toArray(java.lang.Object [])
        Integer[] argArray = new Integer[100];
        Object[] retArray;
        retArray = ll.toArray(argArray);
        assertTrue("Returned different array than passed", retArray == argArray);
        List retList = new LinkedList(Arrays.asList(retArray));
        Iterator li = ll.iterator();
        Iterator ri = retList.iterator();
        while (li.hasNext())
            assertTrue("Lists are not equal", li.next() == ri.next());
        argArray = new Integer[1000];
        retArray = ll.toArray(argArray);
        assertNull("Failed to set first extra element to null", argArray[ll
                .size()]);
        for (int i = 0; i < ll.size(); i++)
            assertTrue("Returned incorrect array: " + i,
                    retArray[i] == objArray[i]);
        ll.add(50, null);
        argArray = new Integer[101];
        retArray = ll.toArray(argArray);
        assertTrue("Returned different array than passed", retArray == argArray);
        retArray = ll.toArray(argArray);
        assertTrue("Returned different array than passed", retArray == argArray);
        retList = new LinkedList(Arrays.asList(retArray));
        li = ll.iterator();
        ri = retList.iterator();
        while (li.hasNext())
            assertTrue("Lists are not equal", li.next() == ri.next());

        try {
            ll.toArray(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }

        LinkedList<String> lls = new LinkedList<String>();
        lls.add("First");
        lls.add("Second");

        try {
            lls.toArray(argArray);
            fail("ArrayStoreException expected");
        } catch (ArrayStoreException e) {
            //expected
        }
    }

    public void test_offer() {
        int origSize = ll.size();
        assertTrue("offer() should return true'", ll.offer(objArray[0]));
        assertEquals("offer() should add an element as the last one", origSize, ll.lastIndexOf(objArray[0]));
    }

    public void test_poll() {
        for (int i = 0; i < objArray.length; i++) {
            assertEquals("should remove the head", objArray[i], ll.poll());
        }
        assertEquals("should be empty", 0, ll.size());
        assertNull("should return 'null' if list is empty", ll.poll());
    }

    public void test_remove() {
        for (int i = 0; i < objArray.length; i++) {
            assertEquals("should remove the head", objArray[i], ll.remove());
        }
        assertEquals("should be empty", 0, ll.size());
        try {
            ll.remove();
            fail("NoSuchElementException is expected when removing from the empty list");
        } catch (NoSuchElementException e) {
            //-- expected
        }
    }

    public void test_element() {
        assertEquals("should return the head", objArray[0], ll.element());
        assertEquals("element() should remove nothing", objArray.length, ll.size());
        try {
            new LinkedList().remove();
            fail("NoSuchElementException is expected when the list is empty");
        } catch (NoSuchElementException e) {
            //-- expected
        }
    }

    /**
     * {@link java.util.LinkedList#removeFirstOccurrence(Object)}
     */
    public void test_removeFirstOccurrence() throws Exception {
        assertTrue(testList.offerLast(testObjOne));
        assertTrue(testList.offerLast(testObjTwo));
        assertTrue(testList.offerLast(testObjOne));
        assertTrue(testList.offerLast(testObjThree));
        assertTrue(testList.offerLast(testObjOne));
        assertEquals(5, testList.size());
        assertTrue(testList.removeFirstOccurrence(testObjOne));
        assertFalse(testList.removeFirstOccurrence(testObjFour));
        assertEquals(testObjTwo, testList.peekFirst());
        assertEquals(testObjOne, testList.peekLast());
        assertEquals(4, testList.size());
        assertTrue(testList.removeFirstOccurrence(testObjOne));
        assertEquals(3, testList.size());
        assertEquals(testObjOne, testList.peekLast());
        assertTrue(testList.removeFirstOccurrence(testObjOne));
        assertEquals(2, testList.size());
        assertEquals(testObjThree, testList.peekLast());
        assertFalse(testList.removeFirstOccurrence(testObjOne));
    }

    /**
     * {@link java.util.LinkedList#removeLastOccurrence(Object)}
     */
    public void test_removeLastOccurrence() throws Exception {
        assertTrue(testList.offerLast(testObjOne));
        assertTrue(testList.offerLast(testObjTwo));
        assertTrue(testList.offerLast(testObjOne));
        assertTrue(testList.offerLast(testObjThree));
        assertTrue(testList.offerLast(testObjOne));
        assertEquals(5, testList.size());
        assertTrue(testList.removeLastOccurrence(testObjOne));
        assertFalse(testList.removeLastOccurrence(testObjFour));
        assertEquals(testObjOne, testList.peekFirst());
        assertEquals(testObjThree, testList.peekLast());
        assertEquals(4, testList.size());
        assertTrue(testList.removeLastOccurrence(testObjOne));
        assertEquals(3, testList.size());
        assertEquals(testObjOne, testList.peekFirst());
        assertEquals(testObjThree, testList.peekLast());
        assertTrue(testList.removeLastOccurrence(testObjOne));
        assertEquals(2, testList.size());
        assertEquals(testObjThree, testList.peekLast());
        assertFalse(testList.removeLastOccurrence(testObjOne));
    }

    /**
     * {@link java.util.LinkedList#offerFirst(Object)}
     */
    public void test_offerFirst() throws Exception {
        assertTrue(testList.offerFirst(testObjOne));
        assertEquals(1, testList.size());
        assertEquals(testObjOne, testList.peek());
        assertTrue(testList.offerFirst(testObjOne));
        assertEquals(2, testList.size());
        assertEquals(testObjOne, testList.peek());
        assertTrue(testList.offerFirst(testObjTwo));
        assertEquals(3, testList.size());
        assertEquals(testObjTwo, testList.peek());
        assertEquals(testObjOne, testList.getLast());
        assertTrue(testList.offerFirst(null));
        assertEquals(4, testList.size());
    }

    /**
     * {@link java.util.LinkedList#offerLast(Object)}
     */
    public void test_offerLast() throws Exception {
        assertTrue(testList.offerLast(testObjOne));
        assertEquals(1, testList.size());
        assertEquals(testObjOne, testList.peek());
        assertTrue(testList.offerLast(testObjOne));
        assertEquals(2, testList.size());
        assertEquals(testObjOne, testList.peek());
        assertTrue(testList.offerLast(testObjTwo));
        assertEquals(3, testList.size());
        assertEquals(testObjOne, testList.peek());
        assertEquals(testObjTwo, testList.getLast());
        assertTrue(testList.offerLast(null));
        assertEquals(4, testList.size());
    }

    /**
     * {@link java.util.LinkedList#push(Object)}
     */
    public void test_push() throws Exception {
        testList.push(testObjOne);
        assertEquals(1, testList.size());
        assertEquals(testObjOne, testList.peek());
        testList.push(testObjOne);
        assertEquals(2, testList.size());
        assertEquals(testObjOne, testList.peek());
        testList.push(testObjTwo);
        assertEquals(3, testList.size());
        assertEquals(testObjTwo, testList.peek());
        assertEquals(testObjOne, testList.getLast());
        testList.push(null);
        assertEquals(4, testList.size());
    }

    /**
     * {@link java.util.LinkedList#pop()}
     */
    public void test_pop() throws Exception {
        assertTrue(testList.offerLast(testObjOne));
        assertTrue(testList.offerLast(testObjTwo));
        assertTrue(testList.offerLast(testObjThree));
        assertEquals(3, testList.size());
        assertEquals(testObjOne, testList.pop());
        assertEquals(2, testList.size());
        assertEquals(testObjTwo, testList.pop());
        assertEquals(testObjThree, testList.pop());
        assertEquals(0, testList.size());
        testList.push(null);
        assertEquals(1, testList.size());
        assertNull(testList.pop());
        try {
            testList.pop();
            fail("should throw NoSuchElementException ");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * {@link java.util.LinkedList#descendingIterator()}
     */
    public void test_descendingIterator() throws Exception {
        assertFalse(testList.descendingIterator().hasNext());
        assertTrue(testList.add(testObjOne));
        assertTrue(testList.add(testObjTwo));
        assertTrue(testList.add(testObjOne));
        assertTrue(testList.add(testObjThree));
        assertTrue(testList.add(testObjLast));
        Iterator result = testList.descendingIterator();
        assertEquals(5, testList.size());
        try {
            result.remove();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        assertTrue(testList.add(testObjFour));

        try {
            assertEquals(testObjLast, result.next());
            fail("should throw ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
            // expected
        }

        result = testList.descendingIterator();
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
    }

    /**
     * {@link java.util.LinkedList#pollFirst()}
     */
    public void test_pollFirst() throws Exception {
        assertTrue(testList.offerLast(testObjOne));
        assertTrue(testList.offerLast(testObjTwo));
        assertTrue(testList.offerLast(testObjThree));
        assertEquals(3, testList.size());
        assertEquals(testObjOne, testList.pollFirst());
        assertEquals(2, testList.size());
        assertEquals(testObjTwo, testList.pollFirst());
        assertEquals(testObjThree, testList.pollFirst());
        assertEquals(0, testList.size());
        assertNull(testList.pollFirst());
    }

    /**
     * {@link java.util.LinkedList#pollLast()}
     */
    public void test_pollLast() throws Exception {
        assertTrue(testList.offerLast(testObjOne));
        assertTrue(testList.offerLast(testObjTwo));
        assertTrue(testList.offerLast(testObjThree));
        assertEquals(3, testList.size());
        assertEquals(testObjThree, testList.pollLast());
        assertEquals(2, testList.size());
        assertEquals(testObjTwo, testList.pollLast());
        assertEquals(testObjOne, testList.pollLast());
        assertEquals(0, testList.size());
        assertNull(testList.pollFirst());
    }

    /**
     * {@link java.util.LinkedList#peekFirst()}
     */
    public void test_peekFirst() throws Exception {
        assertTrue(testList.offerLast(testObjOne));
        assertTrue(testList.offerLast(testObjTwo));
        assertTrue(testList.offerLast(testObjThree));
        assertEquals(3, testList.size());
        assertEquals(testObjOne, testList.peekFirst());
        assertEquals(3, testList.size());
        assertEquals(testObjOne, testList.pollFirst());
        assertEquals(testObjTwo, testList.peekFirst());
        assertEquals(testObjTwo, testList.pollFirst());
        assertEquals(testObjThree, testList.pollFirst());
        assertEquals(0, testList.size());
        assertEquals(null, testList.peekFirst());
    }

    /**
     * {@link java.util.LinkedList#peek()}
     */
    public void test_peekLast() throws Exception {
        assertTrue(testList.offerLast(testObjOne));
        assertTrue(testList.offerLast(testObjTwo));
        assertTrue(testList.offerLast(testObjThree));
        assertEquals(3, testList.size());
        assertEquals(testObjThree, testList.peekLast());
        assertEquals(3, testList.size());
        assertEquals(testObjThree, testList.pollLast());
        assertEquals(testObjTwo, testList.peekLast());
        assertEquals(testObjTwo, testList.pollLast());
        assertEquals(testObjOne, testList.pollLast());
        assertEquals(0, testList.size());
        assertNull(testList.peekLast());
    }

    public void test_forEachRemaining_iterator() throws Exception {
        ForEachRemainingTester.runTests(LinkedList.class, new String[]{ "foo", "bar", "baz "});
        ForEachRemainingTester.runTests(LinkedList.class, new String[] { "foo" });
    }

    public void test_spliterator() throws Exception {
        ArrayList<Integer> testElements = new ArrayList<>(
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16));
        LinkedList<Integer> list = new LinkedList<>();
        list.addAll(testElements);

        SpliteratorTester.runBasicIterationTests(list.spliterator(), testElements);
        SpliteratorTester.runBasicSplitTests(list, testElements);
        SpliteratorTester.testSpliteratorNPE(list.spliterator());

        assertTrue(list.spliterator().hasCharacteristics(
                Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED));

        SpliteratorTester.runOrderedTests(list);
        SpliteratorTester.runSizedTests(list, 16 /* expected size */);
        SpliteratorTester.runSubSizedTests(list, 16 /* expected size */);
        SpliteratorTester.assertSupportsTrySplit(list);
    }

    public void test_spliterator_CME() throws Exception {
        LinkedList<Integer> list = new LinkedList<>();
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

    public void test_removeIf() {
        runBasicRemoveIfTests(LinkedList<Integer>::new);
        runBasicRemoveIfTestsUnordered(LinkedList<Integer>::new);
        runRemoveIfOnEmpty(LinkedList<Integer>::new);
        testRemoveIfNPE(LinkedList<Integer>::new);
        testRemoveIfCME(LinkedList<Integer>::new);
    }

    /**
     * java.util.LinkedList#Serialization()
     */
    public void test_serialization() throws Exception {
        assertTrue(ll.add(new Integer(1)));
        assertTrue(ll.add(new Integer(2)));
        assertTrue(ll.add(new Integer(3)));
        assertTrue(ll.add(new Integer(4)));
        assertTrue(ll.add(new Integer(5)));
        SerializationTest.verifySelf(ll, new SerializableAssert() {
            public void assertDeserialized(Serializable initial,
                    Serializable deserialized) {
                LinkedList<Object> formerQue = (LinkedList) initial;
                LinkedList<Object> deserializedQue = (LinkedList) deserialized;
                assertEquals(formerQue.remove(), deserializedQue.remove());
            }
        });
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();

        objArray = new Object[100];
        for (int i = 0; i < objArray.length; i++) {
            objArray[i] = new Integer(i);
        }

        ll = new LinkedList();
        for (int i = 0; i < objArray.length; i++) {
            ll.add(objArray[i]);
        }

        testList = new LinkedList<Object>();
        testObjOne = new Object();
        testObjTwo = new Object();
        testObjThree = new Object();
        testObjFour = new Object();
        testObjLast = new Object();
    }
}
