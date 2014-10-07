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

package java.lang.reflect;

/**
 * Common interface providing access to reflective information on class members.
 *
 * @see Field
 * @see Constructor
 * @see Method
 */
public interface Member {

    /**
     * Designates all public members of a class or interface (including
     * inherited members).
     */
    public static final int PUBLIC = 0;

    /**
     * Designates all declared members of a class or interface (without
     * inherited members).
     */
    public static final int DECLARED = 1;

    /**
     * Returns the class that declares this member.
     *
     * @return the declaring class
     */
    @SuppressWarnings("unchecked")
    Class<?> getDeclaringClass();

    /**
     * Returns the modifiers for this member. The {@link Modifier} class should
     * be used to decode the result.
     *
     * @return the modifiers for this member
     *
     * @see Modifier
     */
    int getModifiers();

    /**
     * Returns the name of this member.
     *
     * @return the name of this member
     */
    String getName();

    /**
     * Indicates whether or not this member is synthetic (artificially
     * introduced by the compiler).
     *
     * @return {@code true} if this member is synthetic, {@code false} otherwise
     */
    boolean isSynthetic();
}
