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

import libcore.java.util.ForEachRemainingTester;
import libcore.java.util.SpliteratorTester;
import tests.support.Support_ListTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Vector;

import static libcore.java.util.RemoveIfTester.*;

public class VectorTest extends junit.framework.TestCase {

    private Vector tVector = new Vector();

    Object[] objArray;

    private String vString = "[Test 0, Test 1, Test 2, Test 3, Test 4, Test 5, Test 6, Test 7, Test 8, Test 9, Test 10, Test 11, Test 12, Test 13, Test 14, Test 15, Test 16, Test 17, Test 18, Test 19, Test 20, Test 21, Test 22, Test 23, Test 24, Test 25, Test 26, Test 27, Test 28, Test 29, Test 30, Test 31, Test 32, Test 33, Test 34, Test 35, Test 36, Test 37, Test 38, Test 39, Test 40, Test 41, Test 42, Test 43, Test 44, Test 45, Test 46, Test 47, Test 48, Test 49, Test 50, Test 51, Test 52, Test 53, Test 54, Test 55, Test 56, Test 57, Test 58, Test 59, Test 60, Test 61, Test 62, Test 63, Test 64, Test 65, Test 66, Test 67, Test 68, Test 69, Test 70, Test 71, Test 72, Test 73, Test 74, Test 75, Test 76, Test 77, Test 78, Test 79, Test 80, Test 81, Test 82, Test 83, Test 84, Test 85, Test 86, Test 87, Test 88, Test 89, Test 90, Test 91, Test 92, Test 93, Test 94, Test 95, Test 96, Test 97, Test 98, Test 99]";

    /**
     * java.util.Vector#Vector()
     */
    public void test_Constructor() {
        // Test for method java.util.Vector()

        Vector tv = new Vector(100);
        for (int i = 0; i < 100; i++)
            tv.addElement(new Integer(i));
        new Support_ListTest("", tv).runTest();

        tv = new Vector(200);
        for (int i = -50; i < 150; i++)
            tv.addElement(new Integer(i));
        new Support_ListTest("", tv.subList(50, 150)).runTest();

        Vector v = new Vector();
        assertEquals("Vector creation failed", 0, v.size());
        assertEquals("Wrong capacity", 10, v.capacity());
    }

    /**
     * java.util.Vector#Vector(int)
     */
    public void test_ConstructorI() {
        // Test for method java.util.Vector(int)

        Vector v = new Vector(100);
        assertEquals("Vector creation failed", 0, v.size());
        assertEquals("Wrong capacity", 100, v.capacity());

        try {
            new Vector(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    /**
     * java.util.Vector#Vector(int, int)
     */
    public void test_ConstructorII() {
        // Test for method java.util.Vector(int, int)

        Vector v = new Vector(2, 10);
        v.addElement(new Object());
        v.addElement(new Object());
        v.addElement(new Object());

        assertEquals("Failed to inc capacity by proper amount",
                12, v.capacity());

        Vector grow = new Vector(3, -1);
        grow.addElement("one");
        grow.addElement("two");
        grow.addElement("three");
        grow.addElement("four");
        assertEquals("Wrong size", 4, grow.size());
        assertEquals("Wrong capacity", 6, grow.capacity());

        Vector emptyVector = new Vector(0, 0);
        emptyVector.addElement("one");
        assertEquals("Wrong size", 1, emptyVector.size());
        emptyVector.addElement("two");
        emptyVector.addElement("three");
        assertEquals("Wrong size", 3, emptyVector.size());

        try {
            Vector negativeVector = new Vector(-1, 0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Excepted
        }
    }

    /**
     * java.util.Vector#Vector(java.util.Collection)
     */
    public void test_ConstructorLjava_util_Collection() {
        // Test for method java.util.Vector(java.util.Collection)
        Collection l = new LinkedList();
        for (int i = 0; i < 100; i++)
            l.add("Test " + i);
        Vector myVector = new Vector(l);
        assertTrue("Vector is not correct size",
                myVector.size() == objArray.length);
        for (int counter = 0; counter < objArray.length; counter++)
            assertTrue("Vector does not contain correct elements", myVector
                    .contains(((List) l).get(counter)));

        try {
            new Vector(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.Vector#add(int, java.lang.Object)
     */
    public void test_addILjava_lang_Object() {
        // Test for method void java.util.Vector.add(int, java.lang.Object)
        Object o = new Object();
        Object prev = tVector.get(45);
        tVector.add(45, o);
        assertTrue("Failed to add Object", tVector.get(45) == o);
        assertTrue("Failed to fix-up existing indices", tVector.get(46) == prev);
        assertEquals("Wrong size after add", 101, tVector.size());

        prev = tVector.get(50);
        tVector.add(50, null);
        assertNull("Failed to add null", tVector.get(50));
        assertTrue("Failed to fix-up existing indices after adding null",
                tVector.get(51) == prev);
        assertEquals("Wrong size after add", 102, tVector.size());

        try {
            tVector.add(-5, null);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            tVector.add(tVector.size() + 1, null);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Vector#add(java.lang.Object)
     */
    public void test_addLjava_lang_Object() {
        // Test for method boolean java.util.Vector.add(java.lang.Object)
        Object o = new Object();
        tVector.add(o);
        assertTrue("Failed to add Object", tVector.lastElement() == o);
        assertEquals("Wrong size after add", 101, tVector.size());

        tVector.add(null);
        assertNull("Failed to add null", tVector.lastElement());
        assertEquals("Wrong size after add", 102, tVector.size());
    }

    /**
     * java.util.Vector#addAll(int, java.util.Collection)
     */
    public void test_addAllILjava_util_Collection() {
        // Test for method boolean java.util.Vector.addAll(int,
        // java.util.Collection)
        Collection l = new LinkedList();
        for (int i = 0; i < 100; i++)
            l.add("Test " + i);
        Vector v = new Vector();
        tVector.addAll(50, l);
        for (int i = 50; i < 100; i++)
            assertTrue("Failed to add all elements",
                    tVector.get(i) == ((List) l).get(i - 50));
        v = new Vector();
        v.add("one");
        int r = 0;
        try {
            v.addAll(3, Arrays.asList(new String[] { "two", "three" }));
        } catch (ArrayIndexOutOfBoundsException e) {
            r = 1;
        } catch (IndexOutOfBoundsException e) {
            r = 2;
        }
        assertTrue("Invalid add: " + r, r == 1);
        l = new LinkedList();
        l.add(null);
        l.add("gah");
        l.add(null);
        tVector.addAll(50, l);
        assertNull("Wrong element at position 50--wanted null",
                tVector.get(50));
        assertEquals("Wrong element at position 51--wanted 'gah'", "gah", tVector
                .get(51));
        assertNull("Wrong element at position 52--wanted null",
                tVector.get(52));

        try {
            tVector.addAll(-5, Arrays.asList(new String[] { "two", "three" }));
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            tVector.addAll(tVector.size() + 1, Arrays.asList(new String[] { "two", "three" }));
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            tVector.addAll(tVector.size() / 2, null);
            fail("NullPointerException expected");
        } catch(NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.Vector#addAll(java.util.Collection)
     */
    public void test_addAllLjava_util_Collection() {
        // Test for method boolean java.util.Vector.addAll(java.util.Collection)
        Vector v = new Vector();
        Collection l = new LinkedList();
        for (int i = 0; i < 100; i++)
            l.add("Test " + i);
        v.addAll(l);
        assertTrue("Failed to add all elements", tVector.equals(v));

        v.addAll(l);
        int vSize = tVector.size();
        for (int counter = vSize - 1; counter >= 0; counter--)
            assertTrue("Failed to add elements correctly", v.get(counter) == v
                    .get(counter + vSize));

        l = new LinkedList();
        l.add(null);
        l.add("gah");
        l.add(null);
        tVector.addAll(l);
        assertNull("Wrong element at 3rd last position--wanted null", tVector
                .get(vSize));
        assertEquals("Wrong element at 2nd last position--wanted 'gah'", "gah", tVector
                .get(vSize + 1));
        assertNull("Wrong element at last position--wanted null", tVector
                .get(vSize + 2));

        try {
            tVector.addAll(tVector.size() / 2, null);
            fail("NullPointerException expected");
        } catch(NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.Vector#addElement(java.lang.Object)
     */
    public void test_addElementLjava_lang_Object() {
        // Test for method void java.util.Vector.addElement(java.lang.Object)
        Vector v = vectorClone(tVector);
        v.addElement("Added Element");
        assertTrue("Failed to add element", v.contains("Added Element"));
        assertEquals("Added Element to wrong slot", "Added Element", ((String) v.elementAt(100))
        );
        v.addElement(null);
        assertTrue("Failed to add null", v.contains(null));
        assertNull("Added null to wrong slot", v.elementAt(101));
    }

    /**
     * java.util.Vector#addElement(java.lang.Object)
     */
    public void test_addElementLjava_lang_Object_subtest0() {
        // Test for method void java.util.Vector.addElement(java.lang.Object)
        Vector v = vectorClone(tVector);
        v.addElement("Added Element");
        assertTrue("Failed to add element", v.contains("Added Element"));
        assertEquals("Added Element to wrong slot", "Added Element", ((String) v.elementAt(100))
        );
        v.addElement(null);
        assertTrue("Failed to add null", v.contains(null));
        assertNull("Added null to wrong slot", v.elementAt(101));
    }

    /**
     * java.util.Vector#capacity()
     */
    public void test_capacity() {
        // Test for method int java.util.Vector.capacity()

        Vector v = new Vector(9);
        assertEquals("Incorrect capacity returned", 9, v.capacity());
    }

    /**
     * java.util.Vector#clear()
     */
    public void test_clear() {
        // Test for method void java.util.Vector.clear()
        Vector orgVector = vectorClone(tVector);
        tVector.clear();
        assertEquals("a) Cleared Vector has non-zero size", 0, tVector.size());
        Enumeration e = orgVector.elements();
        while (e.hasMoreElements())
            assertTrue("a) Cleared vector contained elements", !tVector
                    .contains(e.nextElement()));

        tVector.add(null);
        tVector.clear();
        assertEquals("b) Cleared Vector has non-zero size", 0, tVector.size());
        e = orgVector.elements();
        while (e.hasMoreElements())
            assertTrue("b) Cleared vector contained elements", !tVector
                    .contains(e.nextElement()));
    }

    /**
     * java.util.Vector#clone()
     */
    public void test_clone() {
        // Test for method java.lang.Object java.util.Vector.clone()
        tVector.add(25, null);
        tVector.add(75, null);
        Vector v = (Vector) tVector.clone();
        Enumeration orgNum = tVector.elements();
        Enumeration cnum = v.elements();

        while (orgNum.hasMoreElements()) {
            assertTrue("Not enough elements copied", cnum.hasMoreElements());
            assertTrue("Vector cloned improperly, elements do not match",
                    orgNum.nextElement() == cnum.nextElement());
        }
        assertTrue("Not enough elements copied", !cnum.hasMoreElements());

    }

    /**
     * java.util.Vector#contains(java.lang.Object)
     */
    public void test_containsLjava_lang_Object() {
        // Test for method boolean java.util.Vector.contains(java.lang.Object)
        assertTrue("Did not find element", tVector.contains("Test 42"));
        assertTrue("Found bogus element", !tVector.contains("Hello"));
        assertTrue(
                "Returned true looking for null in vector without null element",
                !tVector.contains(null));
        tVector.insertElementAt(null, 20);
        assertTrue(
                "Returned false looking for null in vector with null element",
                tVector.contains(null));
    }

    /**
     * java.util.Vector#containsAll(java.util.Collection)
     */
    public void test_containsAllLjava_util_Collection() {
        // Test for method boolean
        // java.util.Vector.containsAll(java.util.Collection)
        Collection s = new HashSet();
        for (int i = 0; i < 100; i++)
            s.add("Test " + i);

        assertTrue("Returned false for valid collection", tVector
                .containsAll(s));
        s.add(null);
        assertTrue("Returned true for invlaid collection containing null",
                !tVector.containsAll(s));
        tVector.add(25, null);
        assertTrue("Returned false for valid collection containing null",
                tVector.containsAll(s));
        s = new HashSet();
        s.add(new Object());
        assertTrue("Returned true for invalid collection", !tVector
                .containsAll(s));

        try {
            tVector.containsAll(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.Vector#copyInto(java.lang.Object[])
     */
    public void test_copyInto$Ljava_lang_Object() {
        // Test for method void java.util.Vector.copyInto(java.lang.Object [])

        Object[] a = new Object[100];
        tVector.setElementAt(null, 20);
        tVector.copyInto(a);

        for (int i = 0; i < 100; i++)
            assertTrue("copyInto failed", a[i] == tVector.elementAt(i));

        try {
            tVector.copyInto(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.Vector#elementAt(int)
     */
    public void test_elementAtI() {
        // Test for method java.lang.Object java.util.Vector.elementAt(int)
        assertEquals("Incorrect element returned", "Test 18", ((String) tVector
                .elementAt(18)));
        tVector.setElementAt(null, 20);
        assertNull("Incorrect element returned--wanted null", tVector
                .elementAt(20));

        try {
            tVector.elementAt(-5);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            tVector.elementAt(tVector.size() + 1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Vector#elements()
     */
    public void test_elements() {
        // Test for method java.util.Enumeration java.util.Vector.elements()
        tVector.insertElementAt(null, 20);
        Enumeration e = tVector.elements();
        int i = 0;
        while (e.hasMoreElements()) {
            assertTrue("Enumeration returned incorrect element at pos: " + i, e
                    .nextElement() == tVector.elementAt(i));
            i++;
        }
        assertTrue("Invalid enumeration", i == tVector.size());
    }

    /**
     * java.util.Vector#elements()
     */
    public void test_elements_subtest0() {
        final int iterations = 10000;
        final Vector v = new Vector();
        Thread t1 = new Thread() {
            public void run() {
                for (int i = 0; i < iterations; i++) {
                    synchronized (v) {
                        v.addElement(String.valueOf(i));
                        v.removeElementAt(0);
                    }
                }
            }
        };
        t1.start();
        for (int i = 0; i < iterations; i++) {
            Enumeration en = v.elements();
            try {
                while (true) {
                    Object result = en.nextElement();
                    if (result == null) {
                        fail("Null result: " + i);
                    }
                }
            } catch (NoSuchElementException e) {
            }
        }
    }

    /**
     * java.util.Vector#ensureCapacity(int)
     */
    public void test_ensureCapacityI() {
        // Test for method void java.util.Vector.ensureCapacity(int)

        Vector v = new Vector(9);
        v.ensureCapacity(20);
        assertEquals("ensureCapacity failed to set correct capacity", 20, v
                .capacity());
        v = new Vector(100);
        assertEquals("ensureCapacity reduced capacity", 100, v.capacity());

        v.ensureCapacity(150);
        assertEquals(
                "ensuieCapacity failed to set to be twice the old capacity",
                200, v.capacity());

        v = new Vector(9, -1);
        v.ensureCapacity(20);
        assertEquals("ensureCapacity failed to set to be minCapacity", 20, v
                .capacity());
        v.ensureCapacity(15);
        assertEquals("ensureCapacity reduced capacity", 20, v.capacity());
        v.ensureCapacity(35);
        assertEquals(
                "ensuieCapacity failed to set to be twice the old capacity",
                40, v.capacity());

        v = new Vector(9, 4);
        v.ensureCapacity(11);
        assertEquals("ensureCapacity failed to set correct capacity", 13, v
                .capacity());
        v.ensureCapacity(5);
        assertEquals("ensureCapacity reduced capacity", 13, v.capacity());
        v.ensureCapacity(20);
        assertEquals(
                "ensureCapacity failed to set to be twice the old capacity",
                20, v.capacity());
    }

    /**
     * java.util.Vector#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean java.util.Vector.equals(java.lang.Object)
        Vector v = new Vector();
        for (int i = 0; i < 100; i++)
            v.addElement("Test " + i);
        assertTrue("a) Equal vectors returned false", tVector.equals(v));
        v.addElement(null);
        assertTrue("b) UnEqual vectors returned true", !tVector.equals(v));
        tVector.addElement(null);
        assertTrue("c) Equal vectors returned false", tVector.equals(v));
        tVector.removeElementAt(22);
        assertTrue("d) UnEqual vectors returned true", !tVector.equals(v));
        assertTrue("e) Equal vectors returned false", tVector.equals(tVector));
        assertFalse("f) UnEqual vectors returned true", tVector
                .equals(new Object()));
        assertFalse("g) Unequal vectors returned true", tVector.equals(null));
    }

    /**
     * java.util.Vector#firstElement()
     */
    public void test_firstElement() {
        // Test for method java.lang.Object java.util.Vector.firstElement()
        assertEquals("Returned incorrect firstElement", "Test 0", tVector.firstElement()
        );
        tVector.insertElementAt(null, 0);
        assertNull("Returned incorrect firstElement--wanted null", tVector
                .firstElement());

        Vector v = new Vector(10);
        try {
            v.firstElement();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Excepted
        }
    }

    /**
     * java.util.Vector#get(int)
     */
    public void test_getI() {
        // Test for method java.lang.Object java.util.Vector.get(int)
        assertEquals("Get returned incorrect object",
                "Test 80", tVector.get(80));
        tVector.add(25, null);
        assertNull("Returned incorrect element--wanted null",
                tVector.get(25));

        try {
            tVector.get(-5);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            tVector.get(tVector.size() + 1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch(ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * java.util.Vector#hashCode()
     */
    public void test_hashCode() {
        // Test for method int java.util.Vector.hashCode()
        int hashCode = 1; // one
        tVector.insertElementAt(null, 20);
        for (int i = 0; i < tVector.size(); i++) {
            Object obj = tVector.elementAt(i);
            hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
        }
        assertTrue("Incorrect hashCode returned.  Wanted: " + hashCode
                + " got: " + tVector.hashCode(), tVector.hashCode() == hashCode);
    }

    /**
     * java.util.Vector#indexOf(java.lang.Object)
     */
    public void test_indexOfLjava_lang_Object() {
        // Test for method int java.util.Vector.indexOf(java.lang.Object)
        assertEquals("Incorrect index returned", 10, tVector.indexOf("Test 10"));
        assertEquals("Index returned for invalid Object", -1, tVector
                .indexOf("XXXXXXXXXXX"));
        tVector.setElementAt(null, 20);
        tVector.setElementAt(null, 40);
        assertTrue("Incorrect indexOf returned for null: "
                + tVector.indexOf(null), tVector.indexOf(null) == 20);
    }

    /**
     * java.util.Vector#indexOf(java.lang.Object, int)
     */
    public void test_indexOfLjava_lang_ObjectI() {
        // Test for method int java.util.Vector.indexOf(java.lang.Object, int)
        assertEquals("Failed to find correct index", tVector.indexOf("Test 98",
                50), 98);
        assertTrue("Found index of bogus element", (tVector.indexOf(
                "Test 1001", 50) == -1));
        tVector.setElementAt(null, 20);
        tVector.setElementAt(null, 40);
        tVector.setElementAt(null, 60);
        assertTrue("a) Incorrect indexOf returned for null: "
                + tVector.indexOf(null, 25), tVector.indexOf(null, 25) == 40);
        assertTrue("b) Incorrect indexOf returned for null: "
                + tVector.indexOf(null, 20), tVector.indexOf(null, 20) == 20);
        try {
            tVector.indexOf("Test 98", -1);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        assertEquals(-1, tVector.indexOf("Test 98", 1000));
        assertEquals(-1, tVector.indexOf("Test 98", Integer.MAX_VALUE));
        assertEquals(-1, tVector.indexOf("Test 98", tVector.size()));
        assertEquals(98, tVector.indexOf("Test 98", 0));
        try {
            tVector.indexOf("Test 98", Integer.MIN_VALUE);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }

    /**
     * java.util.Vector#insertElementAt(java.lang.Object, int)
     */
    public void test_insertElementAtLjava_lang_ObjectI() {
        // Test for method void
        // java.util.Vector.insertElementAt(java.lang.Object, int)
        Vector v = vectorClone(tVector);
        String prevElement = (String) v.elementAt(99);
        v.insertElementAt("Inserted Element", 99);
        assertEquals("Element not inserted", "Inserted Element", ((String) v.elementAt(99))
        );
        assertTrue("Elements shifted incorrectly", ((String) v.elementAt(100))
                .equals(prevElement));
        v.insertElementAt(null, 20);
        assertNull("null not inserted", v.elementAt(20));

        try {
            tVector.insertElementAt("Inserted Element", -1);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            tVector.insertElementAt(null, -1);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            tVector.insertElementAt("Inserted Element", tVector.size() + 1);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            tVector.insertElementAt(null, tVector.size() + 1);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }
    }

    /**
     * java.util.Vector#isEmpty()
     */
    public void test_isEmpty() {
        // Test for method boolean java.util.Vector.isEmpty()Vector
        Vector v = new java.util.Vector();
        assertTrue("Empty vector returned false", v.isEmpty());
        v.addElement(new Object());
        assertTrue("non-Empty vector returned true", !v.isEmpty());
    }

    /**
     * java.util.Vector#isEmpty()
     */
    public void test_isEmpty_subtest0() {
        final Vector v = new Vector();
        v.addElement("initial");
        Thread t1 = new Thread() {
            public void run() {
                while (!v.isEmpty())
                    ;
                v.addElement("final");
            }
        };
        t1.start();
        for (int i = 0; i < 10000; i++) {
            synchronized (v) {
                v.removeElementAt(0);
                v.addElement(String.valueOf(i));
            }
            int size;
            if ((size = v.size()) != 1) {
                String result = "Size is not 1: " + size + " " + v;
                // terminate the thread
                v.removeAllElements();
                fail(result);
            }
        }
        // terminate the thread
        v.removeElementAt(0);
    }

    /**
     * java.util.Vector#lastElement()
     */
    public void test_lastElement() {
        // Test for method java.lang.Object java.util.Vector.lastElement()
        assertEquals("Incorrect last element returned", "Test 99", tVector.lastElement()
        );
        tVector.addElement(null);
        assertNull("Incorrect last element returned--wanted null", tVector
                .lastElement());

        Vector vector = new Vector();
        try {
            vector.lastElement();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Excepted
        }
    }

    /**
     * java.util.Vector#lastIndexOf(java.lang.Object)
     */
    public void test_lastIndexOfLjava_lang_Object() {
        // Test for method int java.util.Vector.lastIndexOf(java.lang.Object)
        Vector v = new Vector(9);
        for (int i = 0; i < 9; i++)
            v.addElement("Test");
        v.addElement("z");
        assertEquals("Failed to return correct index", 8, v.lastIndexOf("Test"));
        tVector.setElementAt(null, 20);
        tVector.setElementAt(null, 40);
        assertTrue("Incorrect lastIndexOf returned for null: "
                + tVector.lastIndexOf(null), tVector.lastIndexOf(null) == 40);
    }

    /**
     * java.util.Vector#lastIndexOf(java.lang.Object, int)
     */
    public void test_lastIndexOfLjava_lang_ObjectI() {
        // Test for method int java.util.Vector.lastIndexOf(java.lang.Object,
        // int)
        assertEquals("Failed to find object",
                0, tVector.lastIndexOf("Test 0", 0));
        assertTrue("Found Object outside of index", (tVector.lastIndexOf(
                "Test 0", 10) > -1));
        tVector.setElementAt(null, 20);
        tVector.setElementAt(null, 40);
        tVector.setElementAt(null, 60);
        assertTrue("Incorrect lastIndexOf returned for null: "
                + tVector.lastIndexOf(null, 15),
                tVector.lastIndexOf(null, 15) == -1);
        assertTrue("Incorrect lastIndexOf returned for null: "
                + tVector.lastIndexOf(null, 45),
                tVector.lastIndexOf(null, 45) == 40);

        assertEquals(-1, tVector.lastIndexOf("Test 98", -1));
        assertEquals(-1, tVector.lastIndexOf("Test 98", 0));
        try {
            assertEquals(-1, tVector.lastIndexOf("Test 98", 1000));
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            assertEquals(-1, tVector.lastIndexOf("Test 98", Integer.MAX_VALUE));
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            tVector.lastIndexOf("Test 98", tVector.size());
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            tVector.indexOf("Test 98", Integer.MIN_VALUE);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    // http://b/30974375
    public void test_listIterator_addAndPrevious() {
        ListIterator<String> it = new Vector<String>().listIterator();
        assertFalse(it.hasNext());
        it.add("value");
        assertEquals("value", it.previous());
        assertTrue(it.hasNext());
    }

    /**
     * java.util.Vector#remove(int)
     */
    public void test_removeI() {
        // Test for method java.lang.Object java.util.Vector.remove(int)
        Object removeElement = tVector.get(36);
        Object result = tVector.remove(36);
        assertFalse("Contained element after remove", tVector
                .contains("Test 36"));
        assertEquals("Should return the element that was removed",
                removeElement, result);
        assertEquals("Failed to decrement size after remove",
                99, tVector.size());
        tVector.add(20, null);
        removeElement = tVector.get(19);
        result = tVector.remove(19);
        assertNull("Didn't move null element over", tVector.get(19));
        assertEquals("Should return the element that was removed",
                removeElement, result);
        removeElement = tVector.get(19);
        result = tVector.remove(19);
        assertNotNull("Didn't remove null element", tVector.get(19));
        assertEquals("Should return the element that was removed",
                removeElement, result);
        assertEquals("Failed to decrement size after removing null", 98, tVector
                .size());

        try {
            tVector.remove(-1);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            tVector.remove(tVector.size());
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }
    }

    /**
     * java.util.Vector#remove(java.lang.Object)
     */
    public void test_removeLjava_lang_Object() {
        // Test for method boolean java.util.Vector.remove(java.lang.Object)
        tVector.remove("Test 0");
        assertTrue("Contained element after remove", !tVector
                .contains("Test 0"));
        assertEquals("Failed to decrement size after remove",
                99, tVector.size());
        tVector.add(null);
        tVector.remove(null);
        assertTrue("Contained null after remove", !tVector.contains(null));
        assertEquals("Failed to decrement size after removing null", 99, tVector
                .size());
    }

    /**
     * java.util.Vector#removeAll(java.util.Collection)
     */
    public void test_removeAllLjava_util_Collection() {
        // Test for method boolean
        // java.util.Vector.removeAll(java.util.Collection)
        Vector v = new Vector();
        Collection l = new LinkedList();
        for (int i = 0; i < 5; i++)
            l.add("Test " + i);
        v.addElement(l);

        Collection s = new HashSet();
        Object o;
        s.add(o = v.firstElement());
        v.removeAll(s);
        assertTrue("Failed to remove items in collection", !v.contains(o));
        v.removeAll(l);
        assertTrue("Failed to remove all elements", v.isEmpty());

        v.add(null);
        v.add(null);
        v.add("Boom");
        v.removeAll(s);
        assertEquals("Should not have removed any elements", 3, v.size());
        l = new LinkedList();
        l.add(null);
        v.removeAll(l);
        assertEquals("Should only have one element", 1, v.size());
        assertEquals("Element should be 'Boom'", "Boom", v.firstElement());

        try {
            v.removeAll(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.util.Vector#removeAllElements()
     */
    public void test_removeAllElements() {
        // Test for method void java.util.Vector.removeAllElements()
        Vector v = vectorClone(tVector);
        v.removeAllElements();
        assertEquals("Failed to remove all elements", 0, v.size());
    }

    /**
     * java.util.Vector#removeElement(java.lang.Object)
     */
    public void test_removeElementLjava_lang_Object() {
        // Test for method boolean
        // java.util.Vector.removeElement(java.lang.Object)
        Vector v = vectorClone(tVector);
        v.removeElement("Test 98");
        assertEquals("Element not removed", "Test 99", ((String) v.elementAt(98))
        );
        assertTrue("Vector is wrong size after removal: " + v.size(),
                v.size() == 99);
        tVector.addElement(null);
        v.removeElement(null);
        assertTrue("Vector is wrong size after removing null: " + v.size(), v
                .size() == 99);
    }

    /**
     * java.util.Vector#removeElementAt(int)
     */
    public void test_removeElementAtI() {
        // Test for method void java.util.Vector.removeElementAt(int)
        Vector v = vectorClone(tVector);
        int size = v.size();
        v.removeElementAt(50);
        assertEquals("Failed to remove element", -1, v.indexOf("Test 50", 0));
        assertEquals("Test 51", v.get(50));
        assertEquals(size - 1, v.size());

        tVector.insertElementAt(null, 60);
        assertNull(tVector.get(60));
        size = tVector.size();
        tVector.removeElementAt(60);
        assertNotNull("Element at 60 should not be null after removal", tVector
                .elementAt(60));
        assertEquals(size - 1, tVector.size());

        try {
            tVector.removeElementAt(-1);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            tVector.removeElementAt(tVector.size());
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }
    }

    /**
     * {@link java.util.Vector#removeRange(int, int)}
     */
    public void test_removeRange() {
        MockVector myVector = new MockVector();
        myVector.removeRange(0, 0);

        try {
            myVector.removeRange(0, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Excepted
        }

        int[] data = { 1, 2, 3, 4 };
        for (int i = 0; i < data.length; i++) {
            myVector.add(i, data[i]);
        }

        myVector.removeRange(0, 2);
        assertEquals(data[2], myVector.get(0));
        assertEquals(data[3], myVector.get(1));

        try {
            myVector.removeRange(-1, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            myVector.removeRange(0, -1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            myVector.removeRange(1, 0);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            myVector.removeRange(2, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Excepted
        }
    }

    /**
     * java.util.Vector#retainAll(java.util.Collection)
     */
    public void test_retainAllLjava_util_Collection() {
        // Test for method boolean
        // java.util.Vector.retainAll(java.util.Collection)
        Object o = tVector.firstElement();
        tVector.add(null);
        Collection s = new HashSet();
        s.add(o);
        s.add(null);
        tVector.retainAll(s);
        assertTrue("Retained items other than specified", tVector.size() == 2
                && tVector.contains(o) && tVector.contains(null));
    }

    /**
     * java.util.Vector#set(int, java.lang.Object)
     */
    public void test_setILjava_lang_Object() {
        // Test for method java.lang.Object java.util.Vector.set(int,
        // java.lang.Object)
        Object o = new Object();
        Object previous = tVector.get(23);
        Object result = tVector.set(23, o);
        assertEquals(
                "Should return the element previously at the specified position",
                previous, result);
        assertTrue("Failed to set Object", tVector.get(23) == o);

        previous = tVector.get(0);
        result = tVector.set(0, null);
        assertEquals(
                "Should return the element previously at the specified position",
                previous, result);
        assertNull("Failed to set Object", tVector.get(0));

        try {
            tVector.set(-1, o);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            tVector.set(-1, null);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            tVector.set(tVector.size(), o);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            tVector.set(tVector.size(), null);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }
    }

    /**
     * java.util.Vector#setElementAt(java.lang.Object, int)
     */
    public void test_setElementAtLjava_lang_ObjectI() {
        // Test for method void java.util.Vector.setElementAt(java.lang.Object,
        // int)
        Vector v = vectorClone(tVector);
        v.setElementAt("Inserted Element", 99);
        assertEquals("Element not set", "Inserted Element", ((String) v.elementAt(99))
        );

        v.setElementAt(null, 0);
        assertNull("Null element not set", v.elementAt(0));

        try {
            v.setElementAt("Inserted Element", -1);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            v.setElementAt(null, -1);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            v.setElementAt("Inserted Element", v.size());
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }

        try {
            v.setElementAt(null, v.size());
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }
    }

    /**
     * java.util.Vector#setSize(int)
     */
    public void test_setSizeI() {
        // Test for method void java.util.Vector.setSize(int)
        Vector v = vectorClone(tVector);
        int oldSize = v.size();
        Object preElement = v.get(10);
        v.setSize(10);
        assertEquals("Failed to set size", 10, v.size());
        assertEquals(
                "All components at index newSize and greater should be discarded",
                -1, v.indexOf(preElement));
        try {
            v.get(oldSize - 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted;
        }

        oldSize = v.size();
        v.setSize(20);
        assertEquals("Failed to set size", 20, v.size());
        for (int i = oldSize; i < v.size(); i++) {
            assertNull(v.get(i));
        }

        try {
            v.setSize(-1);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Excepted
        }
    }

    /**
     * java.util.Vector#size()
     */
    public void test_size() {
        // Test for method int java.util.Vector.size()
        assertEquals("Returned incorrect size", 100, tVector.size());

        final Vector v = new Vector();
        v.addElement("initial");
        Thread t1 = new Thread() {
            public void run() {
                while (v.size() > 0)
                    ;
                v.addElement("final");
            }
        };
        t1.start();
        for (int i = 0; i < 10000; i++) {
            synchronized (v) {
                v.removeElementAt(0);
                v.addElement(String.valueOf(i));
            }
            int size;
            if ((size = v.size()) != 1) {
                String result = "Size is not 1: " + size + " " + v;
                // terminate the thread
                v.removeAllElements();
                fail(result);
            }
        }
        // terminate the thread
        v.removeElementAt(0);
    }

    /**
     * java.util.Vector#subList(int, int)
     */
    public void test_subListII() {
        // Test for method java.util.List java.util.Vector.subList(int, int)
        List sl = tVector.subList(10, 25);
        assertEquals("Returned sublist of incorrect size", 15, sl.size());
        for (int i = 10; i < 25; i++)
            assertTrue("Returned incorrect sublist", sl
                    .contains(tVector.get(i)));

        assertEquals("Not synchronized random access", "java.util.Collections$SynchronizedRandomAccessList", sl.getClass().getName()
        );

    }

    /**
     * java.util.Vector#toArray()
     */
    public void test_toArray() {
        // Test for method java.lang.Object [] java.util.Vector.toArray()
        assertTrue("Returned incorrect array", Arrays.equals(objArray, tVector
                .toArray()));
    }

    /**
     * java.util.Vector#toArray(java.lang.Object[])
     */
    public void test_toArray$Ljava_lang_Object() {
        // Test for method java.lang.Object []
        // java.util.Vector.toArray(java.lang.Object [])
        Object[] o = new Object[1000];
        Object f = new Object();
        for (int i = 0; i < o.length; i++)
            o[i] = f;
        tVector.toArray(o);
        assertNull("Failed to set slot to null", o[100]);
        for (int i = 0; i < tVector.size(); i++)
            assertTrue("Returned incorrect array", tVector.elementAt(i) == o[i]);
    }


    class SubVector<E> extends Vector<E> {

        private static final long serialVersionUID = 1L;

        public SubVector() {
            super();
        }

        public synchronized boolean add(E obj) {
            super.addElement(obj);
            return true;
        }

        public synchronized void addElement(E obj) {
            super.add(obj);
        }

        /**
         * java.util.Vector#add(Object)
         */
        @SuppressWarnings("nls")
        public void test_add() {
            SubVector<String> subvector = new SubVector<String>();
            subvector.add("foo");
            subvector.addElement("bar");
            assertEquals("Expected two elements in vector", 2, subvector.size());
        }

    }

    /**
     * java.util.Vector#toString()
     */
    public void test_toString() {
        // Ensure toString works with self-referencing elements.
        Vector<Object> vec = new Vector<Object>(3);
        vec.add(null);
        vec.add(new Object());
        vec.add(vec);
        assertNotNull(vec.toString());

        // Test for method java.lang.String java.util.Vector.toString()
        assertTrue("Incorrect String returned", tVector.toString().equals(
                vString));

        Vector v = new Vector();
        v.addElement("one");
        v.addElement(v);
        v.addElement("3");
        // test last element
        v.addElement(v);
        String result = v.toString();
        assertTrue("should contain self ref", result.indexOf("(this") > -1);
    }

    public void test_override_size() throws Exception {
        Vector v = new Vector();
        Vector testv = new MockVector();
        // though size is overriden, it should passed without exception
        testv.add(1);
        testv.add(2);
        testv.clear();

        testv.add(1);
        testv.add(2);
        v.add(1);
        v.add(2);
        // RI's bug here
        assertTrue(testv.equals(v));
    }

    /**
     * java.util.Vector#trimToSize()
     */
    public void test_trimToSize() {
        // Test for method void java.util.Vector.trimToSize()
        Vector v = new Vector(10);
        v.addElement(new Object());
        v.trimToSize();
        assertEquals("Failed to trim capacity", 1, v.capacity());
    }

    public void test_removeRangeII() {
        MockVector mv = new MockVector();
        mv.add("First");
        mv.add("Second");
        mv.add("One more");
        mv.add("Last");
        mv.removeRange(1, 3);
        assertTrue(mv.contains("First"));
        assertFalse(mv.contains("Second"));
        assertFalse(mv.contains("One more"));
        assertTrue(mv.contains("Last"));
    }

    protected Vector vectorClone(Vector s) {
        return (Vector) s.clone();
    }

    public class MockVector extends Vector {
        @Override
        public synchronized int size() {
            return 0;
        }

        public void removeRange(int start, int end) {
            super.removeRange(start, end);
        }
    }

    public void test_forEach() throws Exception {
      Vector<Integer> vector = new Vector<Integer>();
      vector.add(0);
      vector.add(1);
      vector.add(2);

      Vector<Integer> output = new Vector<Integer>();
      vector.forEach ( k -> output.add(k) );

      assertEquals(vector, output);
    }


    public void test_forEach_NPE() throws Exception {
        Vector<Integer> vector = new Vector<>();
        try {
            vector.forEach(null);
            fail();
        } catch(NullPointerException expected) {}
    }

    public void test_forEach_CME() throws Exception {
        Vector<Integer> vector = new Vector<>();
        vector.add(1);
        vector.add(2);
        try {
            vector.forEach(new java.util.function.Consumer<Integer>() {
                    @Override
                    public void accept(Integer t) {vector.add(t);}
                });
            fail();
        } catch(ConcurrentModificationException expected) {}
    }

    public void test_forEachRemaining_iterator() throws Exception {
        ForEachRemainingTester.runTests(Vector.class, new String[] { "foo", "bar", "baz" });
        ForEachRemainingTester.runTests(Vector.class, new String[] { "foo" });
    }

    public void test_spliterator() throws Exception {
        ArrayList<Integer> testElements = new ArrayList<>(
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16));
        Vector<Integer> list = new Vector<>();
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
        Vector<Integer> list = new Vector<>();
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
        runBasicRemoveIfTests(Vector<Integer>::new);
        runBasicRemoveIfTestsUnordered(Vector<Integer>::new);
        runRemoveIfOnEmpty(Vector<Integer>::new);
        testRemoveIfNPE(Vector<Integer>::new);
        testRemoveIfCME(Vector<Integer>::new);
    }

    // http://b/25867131 et al.
    public void testIteratorAddAfterCompleteIteration() {
        Vector<String> strings = new Vector<>();
        strings.add("string1");
        Iterator<String> it = strings.iterator();
        assertTrue(it.hasNext());
        assertEquals("string1", it.next());
        assertFalse(it.hasNext());
        strings.add("string2");
        // The value of hasNext() must not flap between true and false. If we returned "true"
        // here, we'd fail with a CME on the next call to next() anyway.
        assertFalse(it.hasNext());
    }

    public void testHasNextAfterRemoval() {
        Vector<String> strings = new Vector<>();
        strings.add("string1");
        Iterator<String> it = strings.iterator();
        it.next();
        it.remove();
        assertFalse(it.hasNext());

        strings = new Vector<>();
        strings.add("string1");
        strings.add("string2");
        it = strings.iterator();
        it.next();
        it.remove();
        assertTrue(it.hasNext());
        assertEquals("string2", it.next());
    }

    // http://b/27430229
    public void testRemoveAllDuringIteration() {
        Vector<String> vector = new Vector<>();
        vector.add("food");
        Iterator<String> vectorIterator = vector.iterator();
        vectorIterator.next();
        vector.removeAllElements();
        assertFalse(vectorIterator.hasNext());
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        for (int i = 0; i < 100; i++) {
            tVector.addElement("Test " + i);
        }
        objArray = new Object[100];
        for (int i = 0; i < 100; i++) {
            objArray[i] = "Test " + i;
        }
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}
