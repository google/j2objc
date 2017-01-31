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

/**
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.security.x509;

import java.util.Arrays;
import org.apache.harmony.security.asn1.ASN1Any;
import org.apache.harmony.security.asn1.ASN1Oid;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.asn1.ObjectIdentifier;
import org.apache.harmony.security.utils.AlgNameMapper;


/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with the Algorithm Identifier which is a part of X.509 certificate
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *  AlgorithmIdentifier ::= SEQUENCE {
 *      algorithm OBJECT IDENTIFIER,
 *      parameters ANY DEFINED BY algorithm OPTIONAL
 *  }
 * </pre>
 */
public final class AlgorithmIdentifier {
    /** the value of algorithm field */
    private String algorithm;
    /** the name of the algorithm */
    private String algorithmName;
    /** the value of parameters field */
    private byte[] parameters;
    /** the encoding of AlgorithmIdentifier value */
    private byte[] encoding;

    public AlgorithmIdentifier(String algorithm) {
        this(algorithm, null, null);
    }

    public AlgorithmIdentifier(String algorithm, byte[] parameters) {
        this(algorithm, parameters, null);
    }

    private AlgorithmIdentifier(String algorithm, byte[] parameters, byte[] encoding) {
        this.algorithm = algorithm;
        this.parameters = parameters;
        this.encoding = encoding;
    }

    /**
     * For testing when algorithmName is not known, but algorithm OID is.
     */
    public AlgorithmIdentifier(String algorithm, String algorithmName) {
        this(algorithm, null, null);
        this.algorithmName = algorithmName;
    }

    /**
     * Returns the value of algorithm field of the structure.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the name of the algorithm corresponding to
     * its OID. If there is no the such correspondence,
     * algorithm OID is returned.
     */
    public String getAlgorithmName() {
        if (algorithmName == null) {
            algorithmName = AlgNameMapper.map2AlgName(algorithm);
            if (algorithmName == null) {
                algorithmName = algorithm;
            }
        }
        return algorithmName;
    }

    /**
     * Returns the value of parameters field of the structure.
     */
    public byte[] getParameters() {
        return parameters;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 AlgorithmIdentifier value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    @Override public boolean equals(Object ai) {
        if (!(ai instanceof AlgorithmIdentifier)) {
            return false;
        }
        AlgorithmIdentifier algid = (AlgorithmIdentifier) ai;
        return (algorithm.equals(algid.algorithm))
            && ((parameters == null)
                    ? algid.parameters == null
                    : Arrays.equals(parameters, algid.parameters));
    }

    @Override public int hashCode() {
        return algorithm.hashCode() * 37 + (parameters != null ? Arrays.hashCode(parameters) : 0);
    }

    public void dumpValue(StringBuilder sb) {
        sb.append(getAlgorithmName());
        if (parameters == null) {
            sb.append(", no params, ");
        } else {
            sb.append(", params unparsed, ");
        }
        sb.append("OID = ");
        sb.append(getAlgorithm());
    }

    /**
     * Custom AlgorithmIdentifier DER encoder/decoder
     */
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Oid.getInstance(), ASN1Any.getInstance() }) {
        {
            setOptional(1); // parameters are optional
        }

        @Override protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new AlgorithmIdentifier(ObjectIdentifier
                    .toString((int[]) values[0]), (byte[]) values[1]);
        }

        @Override protected void getValues(Object object, Object[] values) {

            AlgorithmIdentifier aID = (AlgorithmIdentifier) object;

            values[0] = ObjectIdentifier.toIntArray(aID.getAlgorithm());
            values[1] = aID.getParameters();
        }
    };
}
