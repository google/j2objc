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

public class LinkedListTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }

    public static Test suite() {
	return new TestSuite(LinkedListTest.class);
    }

    /**
     * Create a queue of given size containing consecutive
     * Integers 0 ... n.
     */
    private LinkedList populatedQueue(int n) {
        LinkedList q = new LinkedList();
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
        assertEquals(0, new LinkedList().size());
    }

    /**
     * Initializing from null Collection throws NPE
     */
    public void testConstructor3() {
        try {
            LinkedList q = new LinkedList((Collection)null);
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
            LinkedList q = new LinkedList(Arrays.asList(ints));
            for (int i = 0; i < SIZE; ++i)
                assertEquals(ints[i], q.poll());
        }
        finally {}
    }

    /**
     * isEmpty is true before add, false after
     */
    public void testEmpty() {
        LinkedList q = new LinkedList();
        assertTrue(q.isEmpty());
        q.add(new Integer(1));
        assertFalse(q.isEmpty());
        q.add(new Integer(2));
        q.remove();
        q.remove();
        assertTrue(q.isEmpty());
    }

    /**
     * size changes when elements added and removed
     */
    public void testSize() {
        LinkedList q = populatedQueue(SIZE);
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
     * offer(null) succeeds
     */
    public void testOfferNull() {
	try {
            LinkedList q = new LinkedList();
            q.offer(null);
        } catch (NullPointerException ie) { 
            unexpectedException();
        }   
    }

    /**
     * Offer succeeds 
     */
    public void testOffer() {
        LinkedList q = new LinkedList();
        assertTrue(q.offer(new Integer(0)));
        assertTrue(q.offer(new Integer(1)));
    }

    /**
     * add succeeds
     */
    public void testAdd() {
        LinkedList q = new LinkedList();
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
            LinkedList q = new LinkedList();
            q.addAll(null);
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
            LinkedList q = new LinkedList();
            assertFalse(q.addAll(Arrays.asList(empty)));
            assertTrue(q.addAll(Arrays.asList(ints)));
            for (int i = 0; i < SIZE; ++i)
                assertEquals(ints[i], q.poll());
        }
        finally {}
    }

    /**
     * addAll with too large an index throws IOOBE
     */
    public void testAddAll2_IndexOutOfBoundsException() {
	try {
	    LinkedList l = new LinkedList();
	    l.add(new Object());
	    LinkedList m = new LinkedList();
	    m.add(new Object());
	    l.addAll(4,m);
	    shouldThrow();
	} catch(IndexOutOfBoundsException  success) {}
    }

    /**
     * addAll with negative index throws IOOBE
     */
    public void testAddAll4_BadIndex() {
	try {
	    LinkedList l = new LinkedList();
	    l.add(new Object());
	    LinkedList m = new LinkedList();
	    m.add(new Object());
	    l.addAll(-1,m);
	    shouldThrow();
	} catch(IndexOutOfBoundsException  success){}
    }

    /**
     *  poll succeeds unless empty
     */
    public void testPoll() {
        LinkedList q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, ((Integer)q.poll()).intValue());
        }
	assertNull(q.poll());
    }

    /**
     *  peek returns next element, or null if empty
     */
    public void testPeek() {
        LinkedList q = populatedQueue(SIZE);
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
        LinkedList q = populatedQueue(SIZE);
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
        LinkedList q = populatedQueue(SIZE);
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
        LinkedList q = populatedQueue(SIZE);
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
        LinkedList q = populatedQueue(SIZE);
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
        LinkedList q = populatedQueue(SIZE);
        q.clear();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
        q.add(new Integer(1));
        assertFalse(q.isEmpty());
        q.clear();
        assertTrue(q.isEmpty());
    }

    /**
     * containsAll(c) is true when c contains a subset of elements
     */
    public void testContainsAll() {
        LinkedList q = populatedQueue(SIZE);
        LinkedList p = new LinkedList();
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(q.containsAll(p));
            assertFalse(p.containsAll(q));
            p.add(new Integer(i));
        }
        assertTrue(p.containsAll(q));
    }

    /**
     * retainAll(c) retains only those elements of c and reports true if changed
     */
    public void testRetainAll() {
        LinkedList q = populatedQueue(SIZE);
        LinkedList p = populatedQueue(SIZE);
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
            LinkedList q = populatedQueue(SIZE);
            LinkedList p = populatedQueue(i);
            assertTrue(q.removeAll(p));
            assertEquals(SIZE-i, q.size());
            for (int j = 0; j < i; ++j) {
                Integer I = (Integer)(p.remove());
                assertFalse(q.contains(I));
            }
        }
    }

    /**
     *  toArray contains all elements
     */
    public void testToArray() {
        LinkedList q = populatedQueue(SIZE);
	Object[] o = q.toArray();
        Arrays.sort(o);
	for(int i = 0; i < o.length; i++)
	    assertEquals(o[i], q.poll());
    }

    /**
     *  toArray(a) contains all elements
     */
    public void testToArray2() {
        LinkedList q = populatedQueue(SIZE);
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
	    LinkedList l = new LinkedList();
	    l.add(new Object());
	    Object o[] = l.toArray(null);
	    shouldThrow();
	} catch(NullPointerException success){}
    }

    /**
     * toArray with incompatable aray type throws CCE
     */
    public void testToArray1_BadArg() {
	try {
	    LinkedList l = new LinkedList();
	    l.add(new Integer(5));
	    Object o[] = l.toArray(new String[10] );
	    shouldThrow();
	} catch(ArrayStoreException  success){}
    }
    
    /**
     *  iterator iterates through all elements
     */
    public void testIterator() {
        LinkedList q = populatedQueue(SIZE);
        int i = 0;
	Iterator it = q.iterator();
        while(it.hasNext()) {
            assertTrue(q.contains(it.next()));
            ++i;
        }
        assertEquals(i, SIZE);
    }

    /**
     *  iterator ordering is FIFO
     */
    public void testIteratorOrdering() {
        final LinkedList q = new LinkedList();
        q.add(new Integer(1));
        q.add(new Integer(2));
        q.add(new Integer(3));
        int k = 0;
        for (Iterator it = q.iterator(); it.hasNext();) {
            int i = ((Integer)(it.next())).intValue();
            assertEquals(++k, i);
        }

        assertEquals(3, k);
    }

    /**
     * iterator.remove removes current element
     */
    public void testIteratorRemove () {
        final LinkedList q = new LinkedList();
        q.add(new Integer(1));
        q.add(new Integer(2));
        q.add(new Integer(3));
        Iterator it = q.iterator();
        it.next();
        it.remove();
        it = q.iterator();
        assertEquals(it.next(), new Integer(2));
        assertEquals(it.next(), new Integer(3));
        assertFalse(it.hasNext());
    }


    /**
     * toString contains toStrings of elements
     */
    public void testToString() {
        LinkedList q = populatedQueue(SIZE);
        String s = q.toString();
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(s.indexOf(String.valueOf(i)) >= 0);
        }
    }        

    /**
     * peek returns element inserted with addFirst
     */
    public void testAddFirst() {
        LinkedList q = populatedQueue(3);
	q.addFirst(four);
	assertEquals(four,q.peek());
    }	

}
