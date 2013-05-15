/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang.reflect;

/**
 * This interface represents a type variables such as {@code 'T'} in {@code
 * 'public interface Comparable<T>'}, the bounded {@code 'T'} in {@code
 * 'public interface A<T extends Number>'} or the multiple bounded {@code
 * 'T'} in {@code 'public interface B<T extends Number & Cloneable>'}.
 *
 * @param <D>
 *            the generic declaration that declares this type variable
 * @since 1.5
 */
public interface TypeVariable<D extends GenericDeclaration> extends Type {

    /**
     * Returns the upper bounds of this type variable. {@code Object} is the
     * implicit upper bound if no other bounds are declared.
     *
     * @return the upper bounds of this type variable
     *
     * @throws TypeNotPresentException
     *             if any of the bounds points to a missing type
     * @throws MalformedParameterizedTypeException
     *             if any of the bounds points to a type that cannot be
     *             instantiated for some reason
     */
    Type[] getBounds();

    /**
     * Returns the language construct that declares this type variable.
     *
     * @return the generic declaration
     */
    D getGenericDeclaration();

    /**
     * Returns the name of this type variable as it is specified in source
     * code.
     *
     * @return the name of this type variable
     */
    String getName();
}
