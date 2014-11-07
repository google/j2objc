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
 * An ListIterator is used to sequence over a List of objects. ListIterator can
 * move backwards or forwards through the list.
 */
public interface ListIterator<E> extends Iterator<E> {

    /**
     * Inserts the specified object into the list between {@code next} and
     * {@code previous}. The object inserted will be the previous object.
     *
     * @param object
     *            the object to insert.
     * @throws UnsupportedOperationException
     *             if adding is not supported by the list being iterated.
     * @throws ClassCastException
     *             if the class of the object is inappropriate for the list.
     * @throws IllegalArgumentException
     *             if the object cannot be added to the list.
     */
    void add(E object);

    /**
     * Returns whether there are more elements to iterate.
     *
     * @return {@code true} if there are more elements, {@code false} otherwise.
     * @see #next
     */
    public boolean hasNext();

    /**
     * Returns whether there are previous elements to iterate.
     *
     * @return {@code true} if there are previous elements, {@code false}
     *         otherwise.
     * @see #previous
     */
    public boolean hasPrevious();

    /**
     * Returns the next object in the iteration.
     *
     * @return the next object.
     * @throws NoSuchElementException
     *             if there are no more elements.
     * @see #hasNext
     */
    public E next();

    /**
     * Returns the index of the next object in the iteration.
     *
     * @return the index of the next object, or the size of the list if the
     *         iterator is at the end.
     * @throws NoSuchElementException
     *             if there are no more elements.
     * @see #next
     */
    public int nextIndex();

    /**
     * Returns the previous object in the iteration.
     *
     * @return the previous object.
     * @throws NoSuchElementException
     *             if there are no previous elements.
     * @see #hasPrevious
     */
    public E previous();

    /**
     * Returns the index of the previous object in the iteration.
     *
     * @return the index of the previous object, or -1 if the iterator is at the
     *         beginning.
     * @throws NoSuchElementException
     *             if there are no previous elements.
     * @see #previous
     */
    public int previousIndex();

    /**
     * Removes the last object returned by {@code next} or {@code previous} from
     * the list.
     *
     * @throws UnsupportedOperationException
     *             if removing is not supported by the list being iterated.
     * @throws IllegalStateException
     *             if {@code next} or {@code previous} have not been called, or
     *             {@code remove} or {@code add} have already been called after
     *             the last call to {@code next} or {@code previous}.
     */
    public void remove();

    /**
     * Replaces the last object returned by {@code next} or {@code previous}
     * with the specified object.
     *
     * @param object
     *            the object to set.
     * @throws UnsupportedOperationException
     *             if setting is not supported by the list being iterated
     * @throws ClassCastException
     *             if the class of the object is inappropriate for the list.
     * @throws IllegalArgumentException
     *             if the object cannot be added to the list.
     * @throws IllegalStateException
     *             if {@code next} or {@code previous} have not been called, or
     *             {@code remove} or {@code add} have already been called after
     *             the last call to {@code next} or {@code previous}.
     */
    void set(E object);
}
