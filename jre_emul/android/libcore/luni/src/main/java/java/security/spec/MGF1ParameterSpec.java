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
 * The parameter specification for the Mask Generation Function (MGF1) in
 * the RSA-PSS Signature and OAEP Padding scheme.
 * <p>
 * Defined in the <a
 * href="http://www.rsa.com/rsalabs/pubs/PKCS/html/pkcs-1.html">PKCS #1 v2.1</a>
 * standard
 */
public class MGF1ParameterSpec implements AlgorithmParameterSpec {

    /**
     * The predefined MGF1 parameter specification with an "SHA-1" message
     * digest.
     */
    public static final MGF1ParameterSpec SHA1 =
        new MGF1ParameterSpec("SHA-1");

    /**
     * The predefined MGF1 parameter specification with an "SHA-256" message
     * digest.
     */
    public static final MGF1ParameterSpec SHA256 =
        new MGF1ParameterSpec("SHA-256");

    /**
     * The predefined MGF1 parameter specification with an "SHA-384" message
     * digest.
     */
    public static final MGF1ParameterSpec SHA384 =
        new MGF1ParameterSpec("SHA-384");

    /**
     * The predefined MGF1 parameter specification with an "SHA-512" message
     * digest.
     */
    public static final MGF1ParameterSpec SHA512 =
        new MGF1ParameterSpec("SHA-512");

    //  Message digest algorithm name
    private final String mdName;

    /**
     * Creates a new {@code MGF1ParameterSpec} with the specified message digest
     * algorithm name.
     *
     * @param mdName
     *            the name of the message digest algorithm.
     */
    public MGF1ParameterSpec(String mdName) {
        this.mdName = mdName;
        if (this.mdName == null) {
            throw new NullPointerException("mdName == null");
        }
    }

    /**
     * Returns the name of the message digest algorithm.
     *
     * @return the name of the message digest algorithm.
     */
    public String getDigestAlgorithm() {
        return mdName;
    }
}
