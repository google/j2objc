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

public class CopyOnWriteArraySetTest extends JSR166TestCase {
    /*public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return new TestSuite(CopyOnWriteArraySetTest.class);
    }*/

    static CopyOnWriteArraySet populatedSet(int n){
	CopyOnWriteArraySet a = new CopyOnWriteArraySet();
        assertTrue(a.isEmpty());
        for (int i = 0; i < n; ++i) 
            a.add(new Integer(i));
        assertFalse(a.isEmpty());
        assertEquals(n, a.size());
        return a;
    }

    /**
     * Default-constructed set is empty
     */
    public void testConstructor() {
	CopyOnWriteArraySet a = new CopyOnWriteArraySet();
        assertTrue(a.isEmpty());
    }

    /**
     * Collection-constructed set holds all of its elements
     */
    public void testConstructor3() {
        Integer[] ints = new Integer[SIZE];
        for (int i = 0; i < SIZE-1; ++i)
            ints[i] = new Integer(i);
	CopyOnWriteArraySet a = new CopyOnWriteArraySet(Arrays.asList(ints));
        for (int i = 0; i < SIZE; ++i) 
            assertTrue(a.contains(ints[i]));
    }
        

    /**
     *   addAll  adds each element from the given collection
     */
    public void testAddAll() {
	CopyOnWriteArraySet full = populatedSet(3);
	Vector v = new Vector();
	v.add(three);
	v.add(four);
	v.add(five);
	full.addAll(v);
	assertEquals(6, full.size());
    }

    /**
     *   addAll adds each element from the given collection that did not
     *  already exist in the set
     */
    public void testAddAll2() {
	CopyOnWriteArraySet full = populatedSet(3);
	Vector v = new Vector();
	v.add(three);
	v.add(four);
	v.add(one); // will not add this element
	full.addAll(v);
	assertEquals(5, full.size());
    }

    /**
     *   add will not add the element if it already exists in the set
     */
    public void testAdd2() {
	CopyOnWriteArraySet full = populatedSet(3);
	full.add(one);
	assertEquals(3, full.size());
    }

    /**
     *   add  adds the element when it does not exist
     *   in the set
     */
    public void testAdd3() {
	CopyOnWriteArraySet full = populatedSet(3);
        full.add(three);
        assertTrue(full.contains(three));
    }

    /**
     *   clear  removes all elements from the set
     */
    public void testClear() {
	CopyOnWriteArraySet full = populatedSet(3);
	full.clear();
	assertEquals(0, full.size());
    }

    /**
     *   contains returns true for added elements
     */
    public void testContains() {
	CopyOnWriteArraySet full = populatedSet(3);
	assertTrue(full.contains(one));
	assertFalse(full.contains(five));
    }

    /**
     * Sets with equal elements are equal
     */
    public void testEquals() {
	CopyOnWriteArraySet a = populatedSet(3);
	CopyOnWriteArraySet b = populatedSet(3);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
        a.add(m1);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        b.add(m1);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }

    
    /**
     *   containsAll returns true for collections with subset of elements
     */
    public void testContainsAll() {
	CopyOnWriteArraySet full = populatedSet(3);
	Vector v = new Vector();
	v.add(one);
	v.add(two);
	assertTrue(full.containsAll(v));
	v.add(six);
	assertFalse(full.containsAll(v));
    }

    /**
     *   isEmpty is true when empty, else false
     */
    public void testIsEmpty() {
	CopyOnWriteArraySet empty = new CopyOnWriteArraySet();
	CopyOnWriteArraySet full = populatedSet(3);
	assertTrue(empty.isEmpty());
	assertFalse(full.isEmpty());
    }

    /**
     *   iterator() returns an iterator containing the elements of the set 
     */
    public void testIterator() {
	CopyOnWriteArraySet full = populatedSet(3);
	Iterator i = full.iterator();
	int j;
	for(j = 0; i.hasNext(); j++)
	    assertEquals(j, ((Integer)i.next()).intValue());
	assertEquals(3, j);
    }

    /**
     * iterator remove is unsupported
     */
    public void testIteratorRemove () {
	CopyOnWriteArraySet full = populatedSet(3);
        Iterator it = full.iterator();
        it.next();
        try {
            it.remove();
            shouldThrow();
        }
        catch (UnsupportedOperationException success) {}
    }

    /**
     * toString holds toString of elements
     */
    public void testToString() {
	CopyOnWriteArraySet full = populatedSet(3);
        String s = full.toString();
        for (int i = 0; i < 3; ++i) {
            assertTrue(s.indexOf(String.valueOf(i)) >= 0);
        }
    }        


    /**
     *   removeAll  removes all elements from the given collection
     */
    public void testRemoveAll() {
	CopyOnWriteArraySet full = populatedSet(3);
	Vector v = new Vector();
	v.add(one);
	v.add(two);
	full.removeAll(v);
	assertEquals(1, full.size());
    }


    /**
     * remove removes an element
     */
    public void testRemove() {
	CopyOnWriteArraySet full = populatedSet(3);
	full.remove(one);
        assertFalse(full.contains(one));
	assertEquals(2, full.size());
    }

    /**
     *   size returns the number of elements
     */
    public void testSize() {
	CopyOnWriteArraySet empty = new CopyOnWriteArraySet();
	CopyOnWriteArraySet full = populatedSet(3);
	assertEquals(3, full.size());
	assertEquals(0, empty.size());
    }

    /**
     *   toArray returns an Object array containing all elements from the set
     */
    public void testToArray() {
	CopyOnWriteArraySet full = populatedSet(3);
	Object[] o = full.toArray();
	assertEquals(3, o.length);
	assertEquals(0, ((Integer)o[0]).intValue());
	assertEquals(1, ((Integer)o[1]).intValue());
	assertEquals(2, ((Integer)o[2]).intValue());
    }

    /**
     *   toArray returns an Integer array containing all elements from
     *   the set
     */
    public void testToArray2() {
	CopyOnWriteArraySet full = populatedSet(3);
	Integer[] i = new Integer[3];
	i = (Integer[])full.toArray(i);
	assertEquals(3, i.length);
	assertEquals(0, i[0].intValue());
	assertEquals(1, i[1].intValue());
	assertEquals(2, i[2].intValue());
    }


    /**
     *  toArray throws an ArrayStoreException when the given array can
     *  not store the objects inside the set
     */
    public void testToArray_ArrayStoreException() {
        try {
            CopyOnWriteArraySet c = new CopyOnWriteArraySet();
            c.add("zfasdfsdf");
            c.add("asdadasd");
            c.toArray(new Long[5]);
	    shouldThrow();
        } catch(ArrayStoreException e){}
    }

    /**
     * A deserialized serialized set is equal
     */
    /*public void testSerialization() {
        CopyOnWriteArraySet q = populatedSet(SIZE);

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(q);
            out.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            CopyOnWriteArraySet r = (CopyOnWriteArraySet)in.readObject();
            assertEquals(q.size(), r.size());
            assertTrue(q.equals(r));
            assertTrue(r.equals(q));
        } catch(Exception e){
            unexpectedException();
        }
    }*/

}
