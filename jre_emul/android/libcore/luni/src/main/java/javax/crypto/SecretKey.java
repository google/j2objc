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

package javax.crypto;

import java.security.Key;

/**
 * A cryptographic secret (symmetric) key.
 * <p>
 * This interface is a <i>marker interface</i> to group secret keys and to
 * provide type safety for.
 * <p>
 * Implementations of this interface have to overwrite the
 * {@link Object#equals(Object) equals} and {@link Object#hashCode() hashCode}
 * from {@link java.lang.Object} so comparison is done using the actual key data
 * and not the object reference.
 */
public interface SecretKey extends Key {

    /**
     * The serialization version identifier.
     *
     * @serial
     */
    public static final long serialVersionUID = -4795878709595146952L;
}