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

package org.apache.harmony.security.x509;

import java.io.IOException;
import javax.security.auth.x500.X500Principal;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.x501.Name;

/**
 * CRL Entry's Certificate Issuer Extension (OID = 2.5.29.29).
 * It is a CRL entry extension and contains the GeneralNames describing
 * the issuer of revoked certificate. Its ASN.1 notation is as follows:
 * <pre>
 *   id-ce-certificateIssuer   OBJECT IDENTIFIER ::= { id-ce 29 }
 *
 *   certificateIssuer ::=     GeneralNames
 * </pre>
 * (as specified in RFC 3280)
 * In java implementation it is presumed that GeneralNames consist of
 * one element and its type is directoryName.
 */
public final class CertificateIssuer extends ExtensionValue {
    /** certificate issuer value */
    private X500Principal issuer;

    /**
     * Creates an object on the base of its encoded form.
     */
    public CertificateIssuer(byte[] encoding) {
        super(encoding);
    }

    public X500Principal getIssuer() throws IOException {
        if (issuer == null) {
            issuer = (X500Principal) ASN1.decode(getEncoded());
        }
        return issuer;
    }

    @Override public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append("Certificate Issuer: ");
        if (issuer == null) {
            try {
                issuer = getIssuer();
            } catch (IOException e) {
                // incorrect extension value encoding
                sb.append("Unparseable (incorrect!) extension value:\n");
                super.dumpValue(sb);
            }
        }
        sb.append(issuer).append('\n');
    }

    /**
     * ASN.1 Encoder/Decoder.
     */
    public static final ASN1Type ASN1 = new ASN1Sequence(new ASN1Type[] { GeneralName.ASN1 }) {
        @Override public Object getDecodedObject(BerInputStream in) {
            return ((Name) ((GeneralName) ((Object[]) in.content)[0])
                    .getName()).getX500Principal();
        }

        @Override protected void getValues(Object object, Object[] values) {
            values[0] = object;
        }
    };
}
