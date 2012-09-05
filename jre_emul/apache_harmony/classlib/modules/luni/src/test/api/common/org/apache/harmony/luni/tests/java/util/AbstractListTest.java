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

package org.apache.harmony.luni.tests.java.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

public class AbstractListTest extends junit.framework.TestCase {

	static class SimpleList extends AbstractList {
		ArrayList arrayList;

		SimpleList() {
			this.arrayList = new ArrayList();
		}

		public Object get(int index) {
			return this.arrayList.get(index);
		}

		public void add(int i, Object o) {
			this.arrayList.add(i, o);
		}

		public Object remove(int i) {
			return this.arrayList.remove(i);
		}

		public int size() {
			return this.arrayList.size();
		}
	}

	/**
	 * @tests java.util.AbstractList#hashCode()
	 */
	public void test_hashCode() {
		List list = new ArrayList();
		list.add(new Integer(3));
		list.add(new Integer(15));
		list.add(new Integer(5));
		list.add(new Integer(1));
		list.add(new Integer(7));
		int hashCode = 1;
		Iterator i = list.iterator();
		while (i.hasNext()) {
			Object obj = i.next();
			hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
		}
		assertTrue("Incorrect hashCode returned.  Wanted: " + hashCode
				+ " got: " + list.hashCode(), hashCode == list.hashCode());
	}

	/**
	 * @tests java.util.AbstractList#iterator()
	 */
	public void test_iterator() {
		SimpleList list = new SimpleList();
		list.add(new Object());
		list.add(new Object());
		Iterator it = list.iterator();
		it.next();
		it.remove();
		it.next();
	}

	/**
	 * @tests java.util.AbstractList#listIterator()
	 */
	public void test_listIterator() {
		Integer tempValue;
		List list = new ArrayList();
		list.add(new Integer(3));
		list.add(new Integer(15));
		list.add(new Integer(5));
		list.add(new Integer(1));
		list.add(new Integer(7));
		ListIterator lit = list.listIterator();
		assertTrue("Should not have previous", !lit.hasPrevious());
		assertTrue("Should have next", lit.hasNext());
		tempValue = (Integer) lit.next();
		assertTrue("next returned wrong value.  Wanted 3, got: " + tempValue,
				tempValue.intValue() == 3);
		tempValue = (Integer) lit.previous();

		SimpleList list2 = new SimpleList();
		list2.add(new Object());
		ListIterator lit2 = list2.listIterator();
		lit2.add(new Object());
		lit2.next();
        
        //Regression test for Harmony-5808
        list = new MockArrayList();
        ListIterator it = list.listIterator();
        it.add("one");
        it.add("two");
        assertEquals(2,list.size());
	}

	/**
	 * @tests java.util.AbstractList#subList(int, int)
	 */
	public void test_subListII() {
		// Test each of the SubList operations to ensure a
		// ConcurrentModificationException does not occur on an AbstractList
		// which does not update modCount
		SimpleList mList = new SimpleList();
		mList.add(new Object());
		mList.add(new Object());
		List sList = mList.subList(0, 2);
		sList.add(new Object()); // calls add(int, Object)
		sList.get(0);

		sList.add(0, new Object());
		sList.get(0);

		sList.addAll(Arrays.asList(new String[] { "1", "2" }));
		sList.get(0);

		sList.addAll(0, Arrays.asList(new String[] { "3", "4" }));
		sList.get(0);

		sList.remove(0);
		sList.get(0);

		ListIterator lit = sList.listIterator();
		lit.add(new Object());
		lit.next();
		lit.remove();
		lit.next();

		sList.clear(); // calls removeRange()
		sList.add(new Object());

		// test the type of sublist that is returned
		List al = new ArrayList();
		for (int i = 0; i < 10; i++) {
			al.add(new Integer(i));
		}
		assertTrue(
				"Sublist returned should have implemented Random Access interface",
				al.subList(3, 7) instanceof RandomAccess);

		List ll = new LinkedList();
		for (int i = 0; i < 10; i++) {
			ll.add(new Integer(i));
		}
		assertTrue(
				"Sublist returned should not have implemented Random Access interface",
				!(ll.subList(3, 7) instanceof RandomAccess));

        }

	/**
     * @tests java.util.AbstractList#subList(int, int)
     */
    public void test_subList_empty() {
        // Regression for HARMONY-389
        List al = new ArrayList();
        al.add("one");
        List emptySubList = al.subList(0, 0);

        try {
            emptySubList.get(0);
            fail("emptySubList.get(0) should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            emptySubList.set(0, "one");
            fail("emptySubList.set(0,Object) should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            emptySubList.remove(0);
            fail("emptySubList.remove(0) should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    class MockArrayList<E> extends AbstractList<E> {
    	/**
    	 * 
    	 */
    	private static final long serialVersionUID = 1L;
    	
    	ArrayList<E> list = new ArrayList<E>();
    	
    	public E remove(int idx) {
    		modCount++;
    		return list.remove(idx);
    	}

    	@Override
    	public E get(int index) {
    		return list.get(index);
    	}

    	@Override
    	public int size() {
    		return list.size();
    	}
    	
    	public void add(int idx, E o) {
    		modCount += 10;
    		list.add(idx, o);
    	}
    }
    
    /*
     * Regression test for HY-4398
     */
    public void test_iterator_next() {
		MockArrayList<String> t = new MockArrayList<String>();
		t.list.add("a");
		t.list.add("b");
		
		Iterator it = t.iterator();
		
		while (it.hasNext()) {
			it.next();
		}
		try {
            it.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException cme) {
            // expected
        }

        t.add("c");
        try {
            it.remove();
            fail("Should throw NoSuchElementException");
        } catch (ConcurrentModificationException cme) {
            // expected
		}
		
		it = t.iterator();
		try {
			it.remove();
			fail("Should throw IllegalStateException");
		} catch (IllegalStateException ise) {
			// expected
		}

		Object value = it.next();
		assertEquals("a", value);
	}

    /**
     * @tests java.util.AbstractList#subList(int, int)
     */
    public void test_subList_addAll() {
        // Regression for HARMONY-390
        List mainList = new ArrayList();
        Object[] mainObjects = { "a", "b", "c" };
        mainList.addAll(Arrays.asList(mainObjects));
        List subList = mainList.subList(1, 2);
        assertFalse("subList should not contain \"a\"", subList.contains("a"));
        assertFalse("subList should not contain \"c\"", subList.contains("c"));
        assertTrue("subList should contain \"b\"", subList.contains("b"));

        Object[] subObjects = { "one", "two", "three" };
        subList.addAll(Arrays.asList(subObjects));
        assertFalse("subList should not contain \"a\"", subList.contains("a"));
        assertFalse("subList should not contain \"c\"", subList.contains("c"));

        Object[] expected = { "b", "one", "two", "three" };
        ListIterator iter = subList.listIterator();
        for (int i = 0; i < expected.length; i++) {
            assertTrue("subList should contain " + expected[i], subList
                    .contains(expected[i]));
            assertTrue("should be more elements", iter.hasNext());
            assertEquals("element in incorrect position", expected[i], iter
                    .next());
        }
    }

    protected void doneSuite() {}
    
    class MockRemoveFailureArrayList<E> extends AbstractList<E> {

		@Override
		public E get(int location) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public E remove(int idx) {
    		modCount+=2;
    		return null;
    	}
		
		public int getModCount(){
			return modCount;
		}
    }
    
    //test remove for failure by inconsistency of modCount and expectedModCount 
    public void test_remove(){
    	MockRemoveFailureArrayList<String> mrfal = new MockRemoveFailureArrayList<String>();
    	Iterator<String> imrfal= mrfal.iterator();
    	imrfal.next();
    	imrfal.remove();
    	try{
    		imrfal.remove();
    	}
    	catch(ConcurrentModificationException e){
    		fail("Excepted to catch IllegalStateException not ConcurrentModificationException");
    	}
    	catch(IllegalStateException e){
    		//Excepted to catch IllegalStateException here
    	}
    }

    public void test_subListII2() {
        List<Integer> holder = new ArrayList<Integer>(16);
        for (int i=0; i<10; i++) {
            holder.add(i);
        }

        // parent change should cause sublist concurrentmodification fail
        List<Integer> sub = holder.subList(0, holder.size());
        holder.add(11);
        try {
            sub.size();
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.add(12);
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.add(0, 11);
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.clear();
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.contains(11);
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.get(9);
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.indexOf(10);
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.isEmpty();
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.iterator();
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.lastIndexOf(10);
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.listIterator();
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.listIterator(0);
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.remove(0);
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.remove(9);
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.set(0, 0);
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.size();
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.toArray();
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}
        try {
            sub.toString();
            fail("Should throw ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {}

        holder.clear();
    }

    /**
     * @tests {@link java.util.AbstractList#indexOf(Object)}
     */
    public void test_indexOf_Ljava_lang_Object() {
        AbstractList<Integer> list = new MockArrayList<Integer>();
        Integer[] array = { 1, 2, 3, 4, 5 };
        list.addAll(Arrays.asList(array));

        assertEquals("find 0 in the list do not contain 0", -1, list
                .indexOf(new Integer(0)));
        assertEquals("did not return the right location of element 3", 2, list
                .indexOf(new Integer(3)));
        assertEquals("find null in the list do not contain null element", -1,
                list.indexOf(null));
        list.add(null);
        assertEquals("did not return the right location of element null", 5,
                list.indexOf(null));
    }

    /**
     * @add tests {@link java.util.AbstractList#lastIndexOf(Object)}
     */
    public void test_lastIndexOf_Ljava_lang_Object() {
        AbstractList<Integer> list = new MockArrayList<Integer>();
        Integer[] array = { 1, 2, 3, 4, 5, 5, 4, 3, 2, 1 };
        list.addAll(Arrays.asList(array));

        assertEquals("find 6 in the list do not contain 6", -1, list
                .lastIndexOf(new Integer(6)));
        assertEquals("did not return the right location of element 4", 6, list
                .lastIndexOf(new Integer(4)));
        assertEquals("find null in the list do not contain null element", -1,
                list.lastIndexOf(null));
        list.add(null);
        assertEquals("did not return the right location of element null", 10,
                list.lastIndexOf(null));
    }

    /**
     * @add tests {@link java.util.AbstractList#remove(int)}
     * @add tests {@link java.util.AbstractList#set(int, Object)}
     */
    public void test_remove_I() {
        class MockList<E> extends AbstractList<E> {
            private ArrayList<E> list;

            @Override
            public E get(int location) {
                return list.get(location);
            }

            @Override
            public int size() {
                return list.size();
            }

        }
        AbstractList<Integer> list = new MockList<Integer>();
        try {
            list.remove(0);
            fail("should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            list.set(0, null);
            fail("should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}
