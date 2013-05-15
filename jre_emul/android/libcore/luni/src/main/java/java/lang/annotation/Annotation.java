/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.lang.annotation;

/**
 * Defines the interface implemented by all annotations. Note that the interface
 * itself is <i>not</i> an annotation, and neither is an interface that simply
 * extends this one. Only the compiler is able to create proper annotation
 * types.
 *
 * @since 1.5
 */
public interface Annotation {

    /**
     * Returns the type of this annotation.
     *
     * @return A {@code Class} instance representing the annotation type.
     */
    Class<? extends Annotation> annotationType();

    /**
     * Determines whether or not this annotation is equivalent to the annotation
     * passed. This is determined according to the following rules:
     *
     * <ul>
     *     <li>
     *         Two annotations {@code x} and {@code y} are equal if and only if
     *         they are members of the same annotation type and all the member
     *         values of {@code x} are equal to the corresponding member values
     *         of {@code y}.
     *     </li>
     *     <li>
     *         The equality of primitive member values {@code x} and {@code y}
     *         is determined (in a way similar to) using the corresponding
     *         wrapper classes. For example,
     *         {@code Integer.valueOf(x).equals(Integer.valueOf(y)} is used for
     *         {@code int} values. Note: The behavior is identical to the
     *         {@code ==} operator for all but the floating point type, so the
     *         implementation may as well use {@code ==} in these cases for
     *         performance reasons. Only for the {@code float} and {@code double}
     *         types the result will be slightly different: {@code NaN} is equal
     *         to {@code NaN}, and {@code -0.0} is equal to {@code 0.0}, both of
     *         which is normally not the case.
     *     </li>
     *     <li>
     *         The equality of two array member values {@code x} and {@code y}
     *         is determined using the corresponding {@code equals(x, y)}
     *         helper function in {@link java.util.Arrays}.
     *     </li>
     *     <li>
     *         The hash code for all other member values is determined by simply
     *         calling their {@code equals()} method.
     *     </li>
     * </ul>
     *
     * @param obj
     *            The object to compare to.
     *
     * @return {@code true} if {@code obj} is equal to this annotation,
     *            {@code false} otherwise.
     */
    boolean equals(Object obj);

    /**
     * Returns the hash code of this annotation. The hash code is determined
     * according to the following rules:
     *
     * <ul>
     *     <li>
     *         The hash code of an annotation is the sum of the hash codes of
     *         its annotation members.
     *     </li>
     *     <li>
     *         The hash code of an annotation member is calculated as {@code
     *         (0x7f * n.hashCode()) ^ v.hashCode())}, where {@code n} is the
     *         name of the member (as a {@code String}) and {@code v} its value.
     *     </li>
     *     <li>
     *         The hash code for a primitive member value is determined using
     *         the corresponding wrapper type. For example, {@code
     *         Integer.valueOf(v).hashCode()} is used for an {@code int} value
     *         {@code v}.
     *     </li>
     *     <li>
     *         The hash code for an array member value {@code v} is determined
     *         using the corresponding {@code hashCode(v)} helper function in
     *         {@link java.util.Arrays}.
     *     </li>
     *     <li>
     *         The hash code for all other member values is determined by simply
     *         calling their {@code hashCode} method.
     *     </li>
     * </ul>
     *
     * @return the hash code.
     */
    int hashCode();

    /**
     * Returns a {@code String} representation of this annotation. It is not
     * strictly defined what the representation has to look like, but it usually
     * consists of the name of the annotation, preceded by a "@". If the
     * annotation contains field members, their names and values are also
     * included in the result.
     *
     * @return the {@code String} that represents this annotation.
     */
    String toString();
}
