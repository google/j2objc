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
 * Common interface for language constructs that declare type parameters.
 *
 * @since 1.5
 */
public interface GenericDeclaration {

    /**
     * Returns the declared type parameters in declaration order. If there are
     * no type parameters, this method returns a zero length array.
     *
     * @return the declared type parameters in declaration order
     * @throws GenericSignatureFormatError
     *             if the signature is malformed
     */
    TypeVariable<?>[] getTypeParameters();
}
