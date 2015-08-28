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
import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.utils.Array;

/**
 * Subject Key Identifier Extension (OID = 2.5.29.14).
 *
 * The ASN.1 definition for extension is:
 *
 * <pre>
 *  id-ce-subjectKeyIdentifier OBJECT IDENTIFIER ::=  { id-ce 14 }
 *
 *  SubjectKeyIdentifier ::= KeyIdentifier
 *
 *  KeyIdentifier ::= OCTET STRING
 * </pre>
 * (as specified in RFC 3280 http://www.ietf.org/rfc/rfc3280.txt)
 */
public final class SubjectKeyIdentifier extends ExtensionValue {

    // the value of key identifier
    private final byte[] keyIdentifier;

    /**
     * Creates the object on the base of the value of key identifier.
     */
    public SubjectKeyIdentifier(byte[] keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }

    /**
     * Creates an object on the base of its encoded form.
     */
    public static SubjectKeyIdentifier decode(byte[] encoding)
            throws IOException {
        SubjectKeyIdentifier res = new SubjectKeyIdentifier((byte[])
                ASN1OctetString.getInstance().decode(encoding));
        res.encoding = encoding;
        return res;
    }

    /**
     * The key identifier for this subject.
     */
    public byte[] getKeyIdentifier() {
        return keyIdentifier;
    }

    @Override public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1OctetString.getInstance().encode(keyIdentifier);
        }
        return encoding;
    }

    @Override public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append("SubjectKeyIdentifier: [\n");
        sb.append(Array.toString(keyIdentifier, prefix));
        sb.append(prefix).append("]\n");
    }
}
