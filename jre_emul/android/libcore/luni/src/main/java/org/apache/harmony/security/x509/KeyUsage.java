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
import org.apache.harmony.security.asn1.ASN1BitString;
import org.apache.harmony.security.asn1.ASN1Type;

/**
 * Key Usage Extension (OID = 2.5.29.15).
 *
 * The ASN.1 definition for Key Usage Extension is:
 *
 * <pre>
 * id-ce-keyUsage OBJECT IDENTIFIER ::=  { id-ce 15 }
 *
 * KeyUsage ::= BIT STRING {
 *     digitalSignature        (0),
 *     nonRepudiation          (1),
 *     keyEncipherment         (2),
 *     dataEncipherment        (3),
 *     keyAgreement            (4),
 *     keyCertSign             (5),
 *     cRLSign                 (6),
 *     encipherOnly            (7),
 *     decipherOnly            (8)
 * }
 * </pre>
 * (as specified in RFC 3280 http://www.ietf.org/rfc/rfc3280.txt)
 */
public final class KeyUsage extends ExtensionValue {

    /** The names of the usages. */
    private static final String[] USAGES = {
        "digitalSignature",
        "nonRepudiation",
        "keyEncipherment",
        "dataEncipherment",
        "keyAgreement",
        "keyCertSign",
        "cRLSign",
        "encipherOnly",
        "decipherOnly",
    };

    /** the value of extension */
    private final boolean[] keyUsage;

    /**
     * Creates the extension object on the base of its encoded form.
     */
    public KeyUsage(byte[] encoding) throws IOException {
        super(encoding);
        this.keyUsage = (boolean[]) ASN1.decode(encoding);
    }

    public boolean[] getKeyUsage() {
        return keyUsage;
    }

    @Override public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(keyUsage);
        }
        return encoding;
    }

    @Override public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append("KeyUsage [\n");
        for (int i = 0; i < keyUsage.length; i++) {
            if (keyUsage[i]) {
                sb.append(prefix).append("  ").append(USAGES[i]).append('\n');
            }
        }
        sb.append(prefix).append("]\n");
    }

    /**
     * X.509 Extension value encoder/decoder.
     */
    private static final ASN1Type ASN1 = new ASN1BitString.ASN1NamedBitList(9);

}
