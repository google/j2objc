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

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * {@code KeyFactorySpi} is the Service Provider Interface (SPI) definition for
 * {@link KeyFactory}.
 *
 * @see KeyFactory
 */
public abstract class KeyFactorySpi {

    /**
     * Generates a instance of {@code PublicKey} from the given key
     * specification.
     *
     * @param keySpec
     *            the specification of the public key.
     * @return the public key.
     * @throws InvalidKeySpecException
     *             if the specified {@code keySpec} is invalid.
     */
    protected abstract PublicKey engineGeneratePublic(KeySpec keySpec)
                                    throws InvalidKeySpecException;

    /**
     * Generates a instance of {@code PrivateKey} from the given key
     * specification.
     *
     * @param keySpec
     *            the specification of the private key.
     * @return the private key.
     * @throws InvalidKeySpecException
     *             if the specified {@code keySpec} is invalid.
     */
    protected abstract PrivateKey engineGeneratePrivate(KeySpec keySpec)
                                    throws InvalidKeySpecException;

    /**
     * Returns the key specification for the specified key.
     *
     * @param key
     *            the key from which the specification is requested.
     * @param keySpec
     *            the type of the requested {@code KeySpec}.
     * @return the key specification for the specified key.
     * @throws InvalidKeySpecException
     *             if the key can not be processed, or the requested requested
     *             {@code KeySpec} is inappropriate for the given key.
     */
    protected abstract <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec)
                                    throws InvalidKeySpecException;
    //FIXME 1.5 signature: protected abstract <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException

    /**
     * Translates the given key into a key from this key factory.
     *
     * @param key
     *            the key to translate.
     * @return the translated key.
     * @throws InvalidKeyException
     *             if the specified key can not be translated by this key
     *             factory.
     */
    protected abstract Key engineTranslateKey(Key key) throws InvalidKeyException;

}
