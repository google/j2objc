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
 * An AbstractSet is an abstract implementation of the Set interface. This
 * implementation does not support adding. A subclass must implement the
 * abstract methods iterator() and size().
 * 
 * @since 1.2
 */
public abstract class AbstractSet<E> extends AbstractCollection<E> implements
        Set<E> {

    /**
     * Constructs a new instance of this AbstractSet.
     */
    protected AbstractSet() {
        super();
    }

    /**
     * Compares the specified object to this Set and returns true if they are
     * equal. The object must be an instance of Set and contain the same
     * objects.
     * 
     * @param object
     *            the object to compare with this set.
     * @return {@code true} if the specified object is equal to this set,
     *         {@code false} otherwise
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Set) {
            Set<?> s = (Set<?>) object;

            try {
                return size() == s.size() && containsAll(s);
            } catch (NullPointerException ignored) {
                return false;
            } catch (ClassCastException ignored) {
                return false;
            }
        }
        return false;
    }

    /**
     * Returns the hash code for this set. Two set which are equal must return
     * the same value. This implementation calculates the hash code by adding
     * each element's hash code.
     * 
     * @return the hash code of this set.
     * @see #equals
     */
    @Override
    public int hashCode() {
        int result = 0;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            Object next = it.next();
            result += next == null ? 0 : next.hashCode();
        }
        return result;
    }

    /**
     * Removes all occurrences in this collection which are contained in the
     * specified collection.
     * 
     * @param collection
     *            the collection of objects to remove.
     * @return {@code true} if this collection was modified, {@code false}
     *         otherwise.
     * @throws UnsupportedOperationException
     *                if removing from this collection is not supported.
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean result = false;
        if (size() <= collection.size()) {
            Iterator<?> it = iterator();
            while (it.hasNext()) {
                if (collection.contains(it.next())) {
                    it.remove();
                    result = true;
                }
            }
        } else {
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                result = remove(it.next()) || result;
            }
        }
        return result;
    }
}
