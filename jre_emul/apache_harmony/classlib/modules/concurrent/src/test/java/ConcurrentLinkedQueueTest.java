/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

import junit.framework.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class ConcurrentLinkedQueueTest extends JSR166TestCase {

    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());
    }

    public static Test suite() {
	return new TestSuite(ConcurrentLinkedQueueTest.class);
    }

    /**
     * Create a queue of given size containing consecutive
     * Integers 0 ... n.
     */
    private ConcurrentLinkedQueue populatedQueue(int n) {
        ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
        assertTrue(q.isEmpty());
	for(int i = 0; i < n; ++i)
	    assertTrue(q.offer(new Integer(i)));
        assertFalse(q.isEmpty());
	assertEquals(n, q.size());
        return q;
    }

    /**
     * new queue is empty
     */
    public void testConstructor1() {
        assertEquals(0, new ConcurrentLinkedQueue().size());
    }

    /**
     *  Initializing from null Collection throws NPE
     */
    public void testConstructor3() {
        try {
            ConcurrentLinkedQueue q = new ConcurrentLinkedQueue((Collection)null);
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }

    /**
     * Initializing from Collection of null elements throws NPE
     */
    public void testConstructor4() {
        try {
            Integer[] ints = new Integer[SIZE];
            ConcurrentLinkedQueue q = new ConcurrentLinkedQueue(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }

    /**
     * Initializing from Collection with some null elements throws NPE
     */
    public void testConstructor5() {
        try {
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE-1; ++i)
                ints[i] = new Integer(i);
            ConcurrentLinkedQueue q = new ConcurrentLinkedQueue(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }

    /**
     * Queue contains all elements of collection used to initialize
     */
    public void testConstructor6() {
        try {
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE; ++i)
                ints[i] = new Integer(i);
            ConcurrentLinkedQueue q = new ConcurrentLinkedQueue(Arrays.asList(ints));
            for (int i = 0; i < SIZE; ++i)
                assertEquals(ints[i], q.poll());
        }
        finally {}
    }

    /**
     * isEmpty is true before add, false after
     */
    public void testEmpty() {
        ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
        assertTrue(q.isEmpty());
        q.add(one);
        assertFalse(q.isEmpty());
        q.add(two);
        q.remove();
        q.remove();
        assertTrue(q.isEmpty());
    }

    /**
     * size changes when elements added and removed
     */
    public void testSize() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(SIZE-i, q.size());
            q.remove();
        }
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.size());
            q.add(new Integer(i));
        }
    }

    /**
     * offer(null) throws NPE
     */
    public void testOfferNull() {
	try {
            ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
            q.offer(null);
            shouldThrow();
        } catch (NullPointerException success) { }
    }

    /**
     * add(null) throws NPE
     */
    public void testAddNull() {
	try {
            ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
            q.add(null);
            shouldThrow();
        } catch (NullPointerException success) { }
    }


    /**
     * Offer returns true
     */
    public void testOffer() {
        ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
        assertTrue(q.offer(zero));
        assertTrue(q.offer(one));
    }

    /**
     * add returns true
     */
    public void testAdd() {
        ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.size());
            assertTrue(q.add(new Integer(i)));
        }
    }

    /**
     * addAll(null) throws NPE
     */
    public void testAddAll1() {
        try {
            ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
            q.addAll(null);
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }

    /**
     * addAll(this) throws IAE
     */
    public void testAddAllSelf() {
        try {
            ConcurrentLinkedQueue q = populatedQueue(SIZE);
            q.addAll(q);
            shouldThrow();
        }
        catch (IllegalArgumentException success) {}
    }

    /**
     * addAll of a collection with null elements throws NPE
     */
    public void testAddAll2() {
        try {
            ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
            Integer[] ints = new Integer[SIZE];
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    /**
     *  addAll of a collection with any null elements throws NPE after
     * possibly adding some elements
     */
    public void testAddAll3() {
        try {
            ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE-1; ++i)
                ints[i] = new Integer(i);
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }

    /**
     * Queue contains all elements, in traversal order, of successful addAll
     */
    public void testAddAll5() {
        try {
            Integer[] empty = new Integer[0];
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE; ++i)
                ints[i] = new Integer(i);
            ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
            assertFalse(q.addAll(Arrays.asList(empty)));
            assertTrue(q.addAll(Arrays.asList(ints)));
            for (int i = 0; i < SIZE; ++i)
                assertEquals(ints[i], q.poll());
        }
        finally {}
    }

    /**
     * poll succeeds unless empty
     */
    public void testPoll() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, ((Integer)q.poll()).intValue());
        }
	assertNull(q.poll());
    }

    /**
     * peek returns next element, or null if empty
     */
    public void testPeek() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, ((Integer)q.peek()).intValue());
            q.poll();
            assertTrue(q.peek() == null ||
                       i != ((Integer)q.peek()).intValue());
        }
	assertNull(q.peek());
    }

    /**
     * element returns next element, or throws NSEE if empty
     */
    public void testElement() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, ((Integer)q.element()).intValue());
            q.poll();
        }
        try {
            q.element();
            shouldThrow();
        }
        catch (NoSuchElementException success) {}
    }

    /**
     *  remove removes next element, or throws NSEE if empty
     */
    public void testRemove() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, ((Integer)q.remove()).intValue());
        }
        try {
            q.remove();
            shouldThrow();
        } catch (NoSuchElementException success){
	}
    }

    /**
     * remove(x) removes x and returns true if present
     */
    public void testRemoveElement() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        for (int i = 1; i < SIZE; i+=2) {
            assertTrue(q.remove(new Integer(i)));
        }
        for (int i = 0; i < SIZE; i+=2) {
            assertTrue(q.remove(new Integer(i)));
            assertFalse(q.remove(new Integer(i+1)));
        }
        assertTrue(q.isEmpty());
    }

    /**
     * contains(x) reports true when elements added but not yet removed
     */
    public void testContains() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(q.contains(new Integer(i)));
            q.poll();
            assertFalse(q.contains(new Integer(i)));
        }
    }

    /**
     * clear removes all elements
     */
    public void testClear() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        q.clear();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
        q.add(one);
        assertFalse(q.isEmpty());
        q.clear();
        assertTrue(q.isEmpty());
    }

    /**
     * containsAll(c) is true when c contains a subset of elements
     */
    public void testContainsAll() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        ConcurrentLinkedQueue p = new ConcurrentLinkedQueue();
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(q.containsAll(p));
            assertFalse(p.containsAll(q));
            p.add(new Integer(i));
        }
        assertTrue(p.containsAll(q));
    }

    /**
     * retainAll(c) retains only those elements of c and reports true if change
     */
    public void testRetainAll() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        ConcurrentLinkedQueue p = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            boolean changed = q.retainAll(p);
            if (i == 0)
                assertFalse(changed);
            else
                assertTrue(changed);

            assertTrue(q.containsAll(p));
            assertEquals(SIZE-i, q.size());
            p.remove();
        }
    }

    /**
     * removeAll(c) removes only those elements of c and reports true if changed
     */
    public void testRemoveAll() {
        for (int i = 1; i < SIZE; ++i) {
            ConcurrentLinkedQueue q = populatedQueue(SIZE);
            ConcurrentLinkedQueue p = populatedQueue(i);
            assertTrue(q.removeAll(p));
            assertEquals(SIZE-i, q.size());
            for (int j = 0; j < i; ++j) {
                Integer I = (Integer)(p.remove());
                assertFalse(q.contains(I));
            }
        }
    }

    /**
     * toArray contains all elements
     */
    public void testToArray() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
	Object[] o = q.toArray();
        Arrays.sort(o);
	for(int i = 0; i < o.length; i++)
	    assertEquals(o[i], q.poll());
    }

    /**
     *  toArray(a) contains all elements
     */
    public void testToArray2() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
	Integer[] ints = new Integer[SIZE];
	ints = (Integer[])q.toArray(ints);
        Arrays.sort(ints);
        for(int i = 0; i < ints.length; i++)
            assertEquals(ints[i], q.poll());
    }

    /**
     * toArray(null) throws NPE
     */
    public void testToArray_BadArg() {
	try {
            ConcurrentLinkedQueue q = populatedQueue(SIZE);
	    Object o[] = q.toArray(null);
	    shouldThrow();
	} catch(NullPointerException success){}
    }

    /**
     * toArray with incompatible array type throws CCE
     */
    public void testToArray1_BadArg() {
	try {
            ConcurrentLinkedQueue q = populatedQueue(SIZE);
	    Object o[] = q.toArray(new String[10] );
	    shouldThrow();
	} catch(ArrayStoreException  success){}
    }

    /**
     *  iterator iterates through all elements
     */
    public void testIterator() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        int i = 0;
	Iterator it = q.iterator();
        while(it.hasNext()) {
            assertTrue(q.contains(it.next()));
            ++i;
        }
        assertEquals(i, SIZE);
    }

    /**
     * iterator ordering is FIFO
     */
    public void testIteratorOrdering() {
        final ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
        q.add(one);
        q.add(two);
        q.add(three);

        int k = 0;
        for (Iterator it = q.iterator(); it.hasNext();) {
            int i = ((Integer)(it.next())).intValue();
            assertEquals(++k, i);
        }

        assertEquals(3, k);
    }

    /**
     * Modifications do not cause iterators to fail
     */
    public void testWeaklyConsistentIteration () {
        final ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
        q.add(one);
        q.add(two);
        q.add(three);

        try {
            for (Iterator it = q.iterator(); it.hasNext();) {
                q.remove();
                it.next();
            }
        }
        catch (ConcurrentModificationException e) {
            shouldThrow();
        }

        assertEquals("queue should be empty again", 0, q.size());
    }

    /**
     * iterator.remove removes current element
     */
    public void testIteratorRemove () {
        final ConcurrentLinkedQueue q = new ConcurrentLinkedQueue();
        q.add(one);
        q.add(two);
        q.add(three);
        Iterator it = q.iterator();
        it.next();
        it.remove();
        it = q.iterator();
        assertEquals(it.next(), two);
        assertEquals(it.next(), three);
        assertFalse(it.hasNext());
    }


    /**
     * toString contains toStrings of elements
     */
    public void testToString() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        String s = q.toString();
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(s.indexOf(String.valueOf(i)) >= 0);
        }
    }

    /**
     * A deserialized serialized queue has same elements in same order
     *
    TODO(tball): enable when serialization is supported.
    public void testSerialization() {
        ConcurrentLinkedQueue q = populatedQueue(SIZE);
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(q);
            out.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            ConcurrentLinkedQueue r = (ConcurrentLinkedQueue)in.readObject();
            assertEquals(q.size(), r.size());
            while (!q.isEmpty())
                assertEquals(q.remove(), r.remove());
        } catch(Exception e){
            unexpectedException();
        }
    }
    */

}
