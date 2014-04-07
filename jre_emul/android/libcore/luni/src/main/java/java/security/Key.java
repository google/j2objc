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

package java.security;

import java.io.Serializable;

/**
 * {@code Key} is the common interface for all keys.
 *
 * @see PublicKey
 * @see PrivateKey
 */
public interface Key extends Serializable {

    /**
     * The {@code serialVersionUID} to be compatible with JDK1.1.
     */
    public static final long serialVersionUID = 6603384152749567654L;

    /**
     * Returns the name of the algorithm of this key. If the algorithm is
     * unknown, {@code null} is returned.
     *
     * @return the name of the algorithm of this key or {@code null} if the
     *         algorithm is unknown.
     */
    public String getAlgorithm();

    /**
     * Returns the name of the format used to encode this key, or {@code null}
     * if it can not be encoded.
     *
     * @return the name of the format used to encode this key, or {@code null}
     *         if it can not be encoded.
     */
    public String getFormat();

    /**
     * Returns the encoded form of this key, or {@code null} if encoding is not
     * supported by this key.
     *
     * @return the encoded form of this key, or {@code null} if encoding is not
     *         supported by this key.
     */
    public byte[] getEncoded();
}
