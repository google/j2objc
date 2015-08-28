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
import java.math.BigInteger;
import org.apache.harmony.security.asn1.ASN1Boolean;
import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

/**
 * Basic Constraints Extension (OID == 2.5.29.19).
 *
 * The ASN.1 definition for Basic Constraints Extension is:
 *
 * <pre>
 *   id-ce-basicConstraints OBJECT IDENTIFIER ::=  { id-ce 19 }
 *
 *   BasicConstraints ::= SEQUENCE {
 *        ca                      BOOLEAN DEFAULT FALSE,
 *        pathLenConstraint       INTEGER (0..MAX) OPTIONAL
 *   }
 * </pre>
 * (as specified in RFC 3280)
 */
public final class BasicConstraints extends ExtensionValue {
    /** is CA */
    private boolean ca = false;
    /** path len constraint */
    private int pathLenConstraint = Integer.MAX_VALUE;

    /**
     * Creates the extension object on the base of its encoded form.
     */
    public BasicConstraints(byte[] encoding) throws IOException {
        super(encoding);
        Object[] values = (Object[]) ASN1.decode(encoding);
        ca = (Boolean) values[0];
        if (values[1] != null) {
            pathLenConstraint = new BigInteger((byte[]) values[1]).intValue();
        }
    }

    public boolean getCa() {
        return ca;
    }

    public int getPathLenConstraint() {
        return pathLenConstraint;
    }

    /**
     * Returns the encoded form of the object.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(new Object[]{ca, BigInteger.valueOf(pathLenConstraint) });
        }
        return encoding;
    }

    public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append("BasicConstraints [\n").append(prefix)
            .append("  CA: ").append(ca)
            .append("\n  ").append(prefix).append("pathLenConstraint: ")
            .append(pathLenConstraint).append('\n').append(prefix)
            .append("]\n");
    }

    /**
     * ASN.1 Encoder/Decoder.
     */
    public static final ASN1Type ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Boolean.getInstance(), ASN1Integer.getInstance() }) {
        {
            setDefault(Boolean.FALSE, 0);
            setOptional(1);
        }

        public Object getDecodedObject(BerInputStream in) throws IOException {
            return in.content;
        }

        protected void getValues(Object object, Object[] values) {
            Object[] array = (Object[]) object;
            values[0] = array[0];
            values[1] = ((BigInteger) array[1]).toByteArray();
        }
    };
}
