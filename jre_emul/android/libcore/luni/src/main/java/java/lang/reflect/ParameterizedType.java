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
 * This interface represents a parameterized type such as {@code
 * 'Set&lt;String&gt;'}.
 *
 * @since 1.5
 */
public interface ParameterizedType extends Type {

    /**
     * Returns an array of the actual type arguments for this type.
     * <p>
     * If this type models a non parameterized type nested within a
     * parameterized type, this method returns a zero length array. The generic
     * type of the following {@code field} declaration is an example for a
     * parameterized type without type arguments.
     *
     * <pre>
     * A&lt;String&gt;.B field;
     *
     * class A&lt;T&gt; {
     *     class B {
     *     }
     * }</pre>
     *
     *
     * @return the actual type arguments
     *
     * @throws TypeNotPresentException
     *             if one of the type arguments cannot be found
     * @throws MalformedParameterizedTypeException
     *             if one of the type arguments cannot be instantiated for some
     *             reason
     */
    Type[] getActualTypeArguments();

    /**
     * Returns the parent / owner type, if this type is an inner type, otherwise
     * {@code null} is returned if this is a top-level type.
     *
     * @return the owner type or {@code null} if this is a top-level type
     *
     * @throws TypeNotPresentException
     *             if one of the type arguments cannot be found
     * @throws MalformedParameterizedTypeException
     *             if the owner type cannot be instantiated for some reason
     */
    Type getOwnerType();

    /**
     * Returns the declaring type of this parameterized type.
     * <p>
     * The raw type of {@code Set<String> field;} is {@code Set}.
     *
     * @return the raw type of this parameterized type
     */
    Type getRawType();
}
