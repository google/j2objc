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

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

import junit.framework.TestCase;

public class AbstractSequentialListTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    class ASLT<E> extends AbstractSequentialList<E> {

        LinkedList<E> l = new LinkedList<E>();

        @Override
        public ListIterator<E> listIterator(int index) {
            return l.listIterator(index);
        }

        @Override
        public int size() {
            return l.size();
        }
    }
    
    /**
     * @tests java.util.AbstractSequentialList#addAll(int, java.util.Collection)
     */
    public void test_addAll_ILCollection() {
        AbstractSequentialList<String> al = new ASLT<String>();
        String[] someList = { "Aardvark",  //$NON-NLS-1$
                "Bear",  //$NON-NLS-1$
                "Chimpanzee",  //$NON-NLS-1$
                "Duck" }; //$NON-NLS-1$
        Collection<String> c = Arrays.asList(someList);
        al.addAll(c);
        assertTrue("Should return true", al.addAll(2, c)); //$NON-NLS-1$
    }
    

    /**
     * @tests java.util.AbstractSequentialList#get(int)
     */
    public void test_get() {
        AbstractSequentialList list = new MyAbstractSequentialList();
        list.add(1);
        list.add("value");
        assertEquals(1, list.get(0));
        assertEquals("value", list.get(1));

        // get value by index which is out of bounds
        try {
            list.get(list.size());
            fail("Should throw IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            list.get(-1);
            fail("Should throw IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

    }

    /**
     * @tests java.util.AbstractSequentialList#remove(int)
     */
    public void test_remove() {
        AbstractSequentialList list = new MyAbstractSequentialList();
        list.add(1);

        // normal test
        assertEquals(1, list.remove(0));

        list.add("value");
        assertEquals("value", list.remove(0));

        // remove index is out of bounds
        try {
            list.remove(list.size());
            fail("Should throw IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            list.remove(-1);
            fail("Should throw IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        // list dont't support remove operation
        try {
            AbstractSequentialList mylist = new MockAbstractSequentialList();
            mylist.remove(0);
            fail("Should throw UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    public void test_set() throws Exception {
		MyAbstractSequentialList list = new MyAbstractSequentialList();
		try {
			list.set(0, new Object());
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
	}

	static class MyAbstractSequentialList extends AbstractSequentialList {

		private LinkedList list = new LinkedList();

		public ListIterator listIterator(int index) {
			ListIterator iter = list.listIterator(index);
			return iter;
		}

		@Override
		public int size() {
			return list.size();
		}
	}
    
    static class MockAbstractSequentialList<E> extends AbstractSequentialList {

        public ListIterator listIterator(int index) {
            return new MockListIterator();
        }

        @Override
        public int size() {
            return 0;
        }
    }
    
    static class MockListIterator implements ListIterator{

        public void add(Object object) {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            throw new UnsupportedOperationException();
        }

        public boolean hasPrevious() {
            throw new UnsupportedOperationException();
        }

        public Object next() {
            throw new UnsupportedOperationException();
        }

        public int nextIndex() {
            throw new UnsupportedOperationException();
        }

        public Object previous() {
            throw new UnsupportedOperationException();
        }

        public int previousIndex() {
            throw new UnsupportedOperationException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(Object object) {
            throw new UnsupportedOperationException();
        }
        
    }
}
