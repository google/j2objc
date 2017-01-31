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

import java.io.IOException;
import java.math.BigInteger;
import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.utils.Array;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with Authority Key Identifier Extension (OID = 2.5.29.35).
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *   id-ce-authorityKeyIdentifier OBJECT IDENTIFIER ::=  { id-ce 35 }
 *
 *   AuthorityKeyIdentifier ::= SEQUENCE {
 *      keyIdentifier             [0] KeyIdentifier           OPTIONAL,
 *      authorityCertIssuer       [1] GeneralNames            OPTIONAL,
 *      authorityCertSerialNumber [2] CertificateSerialNumber OPTIONAL  }
 *
 *   KeyIdentifier ::= OCTET STRING
 * </pre>
 */
public final class AuthorityKeyIdentifier extends ExtensionValue {
    private final byte[] keyIdentifier;
    private final GeneralNames authorityCertIssuer;
    private final BigInteger authorityCertSerialNumber;

    public AuthorityKeyIdentifier(byte[] keyIdentifier,
            GeneralNames authorityCertIssuer,
            BigInteger authorityCertSerialNumber) {
        this.keyIdentifier = keyIdentifier;
        this.authorityCertIssuer = authorityCertIssuer;
        this.authorityCertSerialNumber = authorityCertSerialNumber;
    }

    public static AuthorityKeyIdentifier decode(byte[] encoding) throws IOException {
        AuthorityKeyIdentifier aki = (AuthorityKeyIdentifier) ASN1.decode(encoding);
        aki.encoding = encoding;
        return aki;
    }

    /**
     * The key identifier for the authority.
     *
     * @return key identifier or {@code null}
     */
    public byte[] getKeyIdentifier() {
        return keyIdentifier;
    }

    /**
     * The GeneralNames for this authority key identifier.
     *
     * @return names for the authority certificate issuer or {@code null}
     */
    public GeneralNames getAuthorityCertIssuer() {
        return authorityCertIssuer;
    }

    /**
     * The serial number of the certificate identified by this authority key
     * identifier.
     *
     * @return authority's certificate serial number or {@code null}
     */
    public BigInteger getAuthorityCertSerialNumber() {
        return authorityCertSerialNumber;
    }

    @Override public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    @Override public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append("AuthorityKeyIdentifier [\n");
        if (keyIdentifier != null) {
            sb.append(prefix).append("  keyIdentifier:\n");
            sb.append(Array.toString(keyIdentifier, prefix + "    "));
        }
        if (authorityCertIssuer != null) {
            sb.append(prefix).append("  authorityCertIssuer: [\n");
            authorityCertIssuer.dumpValue(sb, prefix + "    ");
            sb.append(prefix).append("  ]\n");
        }
        if (authorityCertSerialNumber != null) {
            sb.append(prefix).append("  authorityCertSerialNumber: ");
            sb.append(authorityCertSerialNumber).append('\n');
        }
        sb.append(prefix).append("]\n");
    }

    public static final ASN1Type ASN1 = new ASN1Sequence(
            new ASN1Type[] {
                new ASN1Implicit(0, ASN1OctetString.getInstance()),
                new ASN1Implicit(1, GeneralNames.ASN1),
                new ASN1Implicit(2, ASN1Integer.getInstance()),
            }) {
        {
            setOptional(0);
            setOptional(1);
            setOptional(2);
        }

        @Override protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;

            byte[] bytes = (byte[]) values[2];
            BigInteger authorityCertSerialNumber = null;
            if (bytes != null) {
                authorityCertSerialNumber = new BigInteger(bytes);
            }

            return new AuthorityKeyIdentifier((byte[]) values[0],
                    (GeneralNames) values[1], authorityCertSerialNumber);
        }

        @Override protected void getValues(Object object, Object[] values) {
            AuthorityKeyIdentifier akid = (AuthorityKeyIdentifier) object;
            values[0] = akid.keyIdentifier;
            values[1] = akid.authorityCertIssuer;
            if (akid.authorityCertSerialNumber != null) {
                values[2] = akid.authorityCertSerialNumber.toByteArray();
            }
        }
    };
}
