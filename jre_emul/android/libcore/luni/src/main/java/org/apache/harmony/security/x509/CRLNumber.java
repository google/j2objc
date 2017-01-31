/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.harmony.security.x509;

import java.io.IOException;
import java.math.BigInteger;
import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1Type;

/**
 * CRL Entry's CRL Number Extension (OID = 2.5.29.20).
 * <pre>
 *   id-ce-cRLNumber OBJECT IDENTIFIER ::= { id-ce 20 }
 *
 *   CRLNumber ::= INTEGER (0..MAX)
 * </pre>
 * (as specified in RFC 3280 http://www.ietf.org/rfc/rfc3280.txt)
 */
public final class CRLNumber extends ExtensionValue {
    /** crl number value */
    private final BigInteger number;

    /**
     * Constructs the object on the base of its encoded form.
     */
    public CRLNumber(byte[] encoding) throws IOException {
        super(encoding);
        number = new BigInteger((byte[]) ASN1.decode(encoding));
    }

    /**
     * Returns the invalidity date.
     */
    public BigInteger getNumber() {
        return number;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 CRLNumber value.
     */
    @Override public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(number.toByteArray());
        }
        return encoding;
    }

    @Override public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append("CRL Number: [ ").append(number).append(" ]\n");
    }

    /**
     * ASN.1 Encoder/Decoder.
     */
    public static final ASN1Type ASN1 = ASN1Integer.getInstance();
}
