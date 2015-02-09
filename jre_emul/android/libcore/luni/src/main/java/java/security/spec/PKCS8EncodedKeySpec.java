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
 * The key specification for an encoded private key in ASN.1 format as defined
 * in the PKCS#8 standard.
 */
public class PKCS8EncodedKeySpec extends EncodedKeySpec {

    /**
     * Creates a new {@code PKCS8EncodedKeySpec} with the specified encoded key
     * bytes.
     *
     * @param encodedKey
     *            the encoded key bytes.
     */
    public PKCS8EncodedKeySpec(byte[] encodedKey) {
        // Super class' ctor makes defensive parameter copy
        super(encodedKey);
    }

    /**
     * Returns a copy of the encoded key bytes.
     *
     * @return a copy of the encoded key bytes.
     */
    public byte[] getEncoded() {
        // Super class' getEncoded() always returns a new array
        return super.getEncoded();
    }

    /**
     * Returns the name of the encoding format of this encoded key
     * specification.
     *
     * @return the string "PKCS#8".
     */
    public final String getFormat() {
        return "PKCS#8";
    }
}
