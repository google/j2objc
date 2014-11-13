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

package java.util;

/**
 * AbstractSequentialList is an abstract implementation of the List interface.
 * This implementation does not support adding. A subclass must implement the
 * abstract method listIterator().
 *
 * @since 1.2
 */
public abstract class AbstractSequentialList<E> extends AbstractList<E> {

    /**
     * Constructs a new instance of this AbstractSequentialList.
     */
    protected AbstractSequentialList() {
    }

    @Override
    public void add(int location, E object) {
        listIterator(location).add(object);
    }

    @Override
    public boolean addAll(int location, Collection<? extends E> collection) {
        ListIterator<E> it = listIterator(location);
        Iterator<? extends E> colIt = collection.iterator();
        int next = it.nextIndex();
        while (colIt.hasNext()) {
            it.add(colIt.next());
        }
        return next != it.nextIndex();
    }

    @Override
    public E get(int location) {
        try {
            return listIterator(location).next();
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return listIterator(0);
    }

    @Override
    public abstract ListIterator<E> listIterator(int location);

    @Override
    public E remove(int location) {
        try {
            ListIterator<E> it = listIterator(location);
            E result = it.next();
            it.remove();
            return result;
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public E set(int location, E object) {
        ListIterator<E> it = listIterator(location);
        if (!it.hasNext()) {
            throw new IndexOutOfBoundsException();
        }
        E result = it.next();
        it.set(object);
        return result;
    }
}
