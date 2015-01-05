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

package java.security.spec;

/**
 * The key specification of an X.509 encoded key in ASN.1 format.
 */
public class X509EncodedKeySpec extends EncodedKeySpec {

    /**
     * Creates a new {@code X509EncodedKeySpec} with the specified encoded key
     * bytes.
     *
     * @param encodedKey
     *            the encoded key bytes.
     */
    public X509EncodedKeySpec(byte[] encodedKey) {
        // Super class' ctor makes defensive parameter copy
        super(encodedKey);
    }

    /**
     * Returns the encoded key bytes.
     *
     * @return the encoded key bytes.
     */
    public byte[] getEncoded() {
        // Super class' getEncoded() always returns a new array
        return super.getEncoded();
    }

    /**
     * Returns the name of the encoding format of this encoded key
     * specification.
     *
     * @return the string "X.509".
     */
    public final String getFormat() {
        return "X.509";
    }
}
