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
 * An {@code Iterator} is used to sequence over a collection of objects.
 * Conceptually, an iterator is always positioned between two elements of a
 * collection. A fresh iterator is always positioned in front of the first
 * element.
 * 
 * If a collection has been changed since its creation, methods {@code next} and
 * {@code hasNext()} may throw a {@code ConcurrentModificationException}.
 * Iterators with this behavior are called fail-fast iterators.
 * 
 * @param <E>
 *            the type of object returned by the iterator.
 */
public interface Iterator<E> {
    /**
     * Returns whether there are more elements to iterate, i.e. whether the
     * iterator is positioned in front of an element.
     * 
     * @return {@code true} if there are more elements, {@code false} otherwise.
     * @see #next
     */
    public boolean hasNext();

    /**
     * Returns the next object in the iteration, i.e. returns the element in
     * front of the iterator and advances the iterator by one position.
     * 
     * @return the next object.
     * @throws NoSuchElementException
     *             if there are no more elements.
     * @see #hasNext
     */
    public E next();

    /**
     * Removes the last object returned by {@code next} from the collection.
     * This method can only be called once after {@code next} was called.
     * 
     * @throws UnsupportedOperationException
     *             if removing is not supported by the collection being
     *             iterated.
     * @throws IllegalStateException
     *             if {@code next} has not been called, or {@code remove} has
     *             already been called after the last call to {@code next}.
     */
    public void remove();
}
