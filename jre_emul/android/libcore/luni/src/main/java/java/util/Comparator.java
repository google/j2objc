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
 * A {@code Comparator} is used to compare two objects to determine their ordering with
 * respect to each other. On a given {@code Collection}, a {@code Comparator} can be used to
 * obtain a sorted {@code Collection} which is <i>totally ordered</i>. For a {@code Comparator}
 * to be <i>consistent with equals</i>, its {code #compare(Object, Object)}
 * method has to return zero for each pair of elements (a,b) where a.equals(b)
 * holds true. It is recommended that a {@code Comparator} implements
 * {@link java.io.Serializable}.
 *
 * @since 1.2
 */
public interface Comparator<T> {
    /**
     * Compares the two specified objects to determine their relative ordering. The ordering
     * implied by the return value of this method for all possible pairs of
     * {@code (lhs, rhs)} should form an <i>equivalence relation</i>.
     * This means that
     * <ul>
     * <li>{@code compare(a,a)} returns zero for all {@code a}</li>
     * <li>the sign of {@code compare(a,b)} must be the opposite of the sign of {@code
     * compare(b,a)} for all pairs of (a,b)</li>
     * <li>From {@code compare(a,b) > 0} and {@code compare(b,c) > 0} it must
     * follow {@code compare(a,c) > 0} for all possible combinations of {@code
     * (a,b,c)}</li>
     * </ul>
     *
     * @param lhs
     *            an {@code Object}.
     * @param rhs
     *            a second {@code Object} to compare with {@code lhs}.
     * @return an integer < 0 if {@code lhs} is less than {@code rhs}, 0 if they are
     *         equal, and > 0 if {@code lhs} is greater than {@code rhs}.
     * @throws ClassCastException
     *                if objects are not of the correct type.
     */
    public int compare(T lhs, T rhs);

    /**
     * Compares this {@code Comparator} with the specified {@code Object} and indicates whether they
     * are equal. In order to be equal, {@code object} must represent the same object
     * as this instance using a class-specific comparison.
     * <p>
     * A {@code Comparator} never needs to override this method, but may choose so for
     * performance reasons.
     *
     * @param object
     *            the {@code Object} to compare with this comparator.
     * @return boolean {@code true} if specified {@code Object} is the same as this
     *         {@code Object}, and {@code false} otherwise.
     * @see Object#hashCode
     * @see Object#equals
     */
    public boolean equals(Object object);
}
