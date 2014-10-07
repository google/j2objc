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
 * A pattern type, such as the upper bounded wildcard {@code
 * ? extends Closeable} or the lower bounded wildcard {@code ? super String}.
 *
 * <p>Although this interface permits an arbitrary number of upper and lower
 * bounds, all wildcard types of Java language programs are in one of two forms:
 * <ol>
 * <li><strong>No lower bound and one upper bound.</strong> Such types are
 *     written like {@code ? extends java.lang.Number}. When the upper bound is
 *     {@code java.lang.Object}, the {@code extends java.lang.Object} suffix is
 *     optional: {@code Set<?>} is shorthand for {@code
 *     Set<? extends java.lang.Object>}.
 * <li><strong>One lower bound and an upper bound of {@code
 *     java.lang.Object}.</strong> Such types are written like {@code
 *     ? super java.lang.String}.
 * </ol>
 */
public interface WildcardType extends Type {
    /**
     * Returns the array of types that represent the upper bounds of this type.
     * The default upper bound is {@code Object}.
     *
     * @return an array containing the upper bounds types
     *
     * @throws TypeNotPresentException
     *             if any of the bounds points to a missing type
     * @throws MalformedParameterizedTypeException
     *             if any bound points to a type that cannot be instantiated for
     *             some reason
     */
    Type[] getUpperBounds();

    /**
     * Returns the array of types that represent the lower bounds of this type.
     * The default lower bound is {@code null}, in which case an empty array is
     * returned. Since only one lower bound is allowed, the returned array's
     * length will never exceed one.
     *
     * @return an array containing the lower bounds types
     *
     * @throws TypeNotPresentException
     *             if any of the bounds points to a missing type
     * @throws MalformedParameterizedTypeException
     *             if any of the bounds points to a type that cannot be
     *             instantiated for some reason
     */
    Type[] getLowerBounds();
}
