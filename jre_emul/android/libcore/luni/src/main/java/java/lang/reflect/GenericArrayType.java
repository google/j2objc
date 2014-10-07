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
 * This interface represents an array type with a component type that is either
 * a parameterized type or a type variable.
 *
 * @since 1.5
 */
public interface GenericArrayType extends Type {
    /**
     * Returns the component type of this array.
     *
     * @return the component type of this array
     *
     * @throws TypeNotPresentException
     *             if the component type points to a missing type
     * @throws MalformedParameterizedTypeException
     *             if the component type points to a type that cannot be
     *             instantiated for some reason
     */
    Type getGenericComponentType();
}
