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
 * The parameter specification for the RSA-PSS Signature scheme.
 * <p>
 * Defined in the <a
 * href="http://www.rsa.com/rsalabs/pubs/PKCS/html/pkcs-1.html">PKCS #1 v2.1</a>
 * standard.
 */
public class PSSParameterSpec implements AlgorithmParameterSpec {

    /**
     * The default parameter specification. It specifies the following parameters:
     * <ul>
     * <li>message digest: {@code "SHA-1"}</li>
     * <li>mask generation function (<i>mgf</i>): {@code "MGF1"}</li>
     * <li>parameters for the <i>mgf</i>: {@link MGF1ParameterSpec#SHA1}</li>
     * <li>salt length: {@code 20}</li>
     * <li>trailer field: {@code -1}</li>
     * </ul>
     */
    public static final PSSParameterSpec DEFAULT = new PSSParameterSpec(20);

    // Message digest algorithm name
    private final String mdName;
    // Mask generation function algorithm name
    private final String mgfName;
    // Mask generation function parameters
    private final AlgorithmParameterSpec mgfSpec;
    // Trailer field value
    private final int trailerField;
    // Salt length in bits
    private final int saltLen;

    /**
     * Creates a new {@code PSSParameterSpec} with the specified salt length
     * and the default values.
     *
     * @param saltLen
     *            the salt length (in bits).
     * @throws IllegalArgumentException
     *             if {@code saltLen} is negative.
     */
    public PSSParameterSpec(int saltLen) {
        if (saltLen < 0) {
            throw new IllegalArgumentException("saltLen < 0");
        }
        this.saltLen = saltLen;
        this.mdName = "SHA-1";
        this.mgfName = "MGF1";
        this.mgfSpec = MGF1ParameterSpec.SHA1;
        this.trailerField = 1;
    }

    /**
     * Creates a new {@code PSSParameterSpec} with the specified message digest
     * name, mask generation function name, mask generation function parameters,
     * salt length, and trailer field value.
     *
     * @param mdName
     *            the name of the message digest algorithm.
     * @param mgfName
     *            the name of the mask generation function algorithm.
     * @param mgfSpec
     *            the parameter for the mask generation function algorithm.
     * @param saltLen
     *            the salt length (in bits).
     * @param trailerField
     *            the trailer field value.
     * @throws IllegalArgumentException
     *             if {@code saltLen} or {@code trailerField} is negative.
     */
    public PSSParameterSpec(String mdName, String mgfName,
            AlgorithmParameterSpec mgfSpec, int saltLen, int trailerField) {

        if (mdName == null) {
            throw new NullPointerException("mdName == null");
        }
        if (mgfName == null) {
            throw new NullPointerException("mgfName == null");
        }
        if (saltLen < 0) {
            throw new IllegalArgumentException("saltLen < 0");
        }
        if (trailerField < 0) {
            throw new IllegalArgumentException("trailerField < 0");
        }
        this.mdName = mdName;
        this.mgfName = mgfName;
        this.mgfSpec = mgfSpec;
        this.saltLen = saltLen;
        this.trailerField = trailerField;
    }

    /**
     * Returns the length of the salt (in bits).
     *
     * @return the length of the salt (in bits).
     */
    public int getSaltLength() {
        return saltLen;
    }

    /**
     * Returns the name of the message digest algorithm.
     *
     * @return the name of the message digest algorithm.
     */
    public String getDigestAlgorithm() {
        return mdName;
    }

    /**
     * Returns the name of the mask generation function algorithm.
     *
     * @return the name of the mask generation function algorithm.
     */
    public String getMGFAlgorithm() {
        return mgfName;
    }

    /**
     * Returns the parameter for the mask generation function algorithm.
     *
     * @return the parameter for the mask generation function algorithm, or
     *         {@code null} if none specified.
     */
    public AlgorithmParameterSpec getMGFParameters() {
        return mgfSpec;
    }

    /**
     * Returns the trailer field value.
     *
     * @return the trailer field value.
     */
    public int getTrailerField() {
        return trailerField;
    }
}
