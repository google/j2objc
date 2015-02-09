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
 * {@code KeyPair} is a container for a public key and a private key. Since the
 * private key can be accessed, instances must be treated like a private key.
 *
 * @see PrivateKey
 * @see PublicKey
 */
public final class KeyPair implements Serializable {

    private static final long serialVersionUID = -7565189502268009837L;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    /**
     * Constructs a new instance of {@code KeyPair} with a public key and the
     * corresponding private key.
     *
     * @param publicKey
     *            the public key.
     * @param privateKey
     *            the private key.
     */
    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /**
     * Returns the private key.
     *
     * @return the private key.
     */
    public PrivateKey getPrivate() {
        return privateKey;
    }

    /**
     * Returns the public key.
     *
     * @return the public key.
     */
    public PublicKey getPublic() {
        return publicKey;
    }
}
