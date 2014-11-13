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
 * An iterator over a sequence of objects, such as a collection.
 *
 * <p>If a collection has been changed since the iterator was created,
 * methods {@code next} and {@code hasNext()} may throw a {@code ConcurrentModificationException}.
 * It is not possible to guarantee that this mechanism works in all cases of unsynchronized
 * concurrent modification. It should only be used for debugging purposes. Iterators with this
 * behavior are called fail-fast iterators.
 *
 * <p>Implementing {@link Iterable} and returning an {@code Iterator} allows your
 * class to be used as a collection with the enhanced for loop.
 *
 * @param <E>
 *            the type of object returned by the iterator.
 */
public interface Iterator<E> {
    /**
     * Returns true if there is at least one more element, false otherwise.
     * @see #next
     */
    public boolean hasNext();

    /**
     * Returns the next object and advances the iterator.
     *
     * @return the next object.
     * @throws NoSuchElementException
     *             if there are no more elements.
     * @see #hasNext
     */
    public E next();

    /**
     * Removes the last object returned by {@code next} from the collection.
     * This method can only be called once between each call to {@code next}.
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
