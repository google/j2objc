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
* @author Vladimir N. Molotkov, Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.security.x509;

import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with the GeneralSubtree structure which is a part of X.509 certificate:
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *
 *   GeneralSubtree ::= SEQUENCE {
 *        base                    GeneralName,
 *        minimum         [0]     BaseDistance DEFAULT 0,
 *        maximum         [1]     BaseDistance OPTIONAL }
 *
 *   BaseDistance ::= INTEGER (0..MAX)
 *
 * </pre>
 *
 * @see org.apache.harmony.security.x509.NameConstraints
 * @see org.apache.harmony.security.x509.GeneralName
 */
public final class GeneralSubtree {
    /** the value of base field of the structure */
    private final GeneralName base;
    /** the value of minimum field of the structure */
    private final int minimum;
    /** the value of maximum field of the structure */
    private final int maximum;
    /** the ASN.1 encoded form of GeneralSubtree */
    private byte[] encoding;

    public GeneralSubtree(GeneralName base, int minimum, int maximum) {
        this.base = base;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Returns the value of base field of the structure.
     */
    public GeneralName getBase() {
        return base;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 GeneralSubtree value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append("General Subtree: [\n");
        sb.append(prefix).append("  base: ").append(base).append('\n');
        sb.append(prefix).append("  minimum: ").append(minimum).append('\n');
        if (maximum >= 0) {
            sb.append(prefix).append("  maximum: ").append(maximum).append('\n');
        }
        sb.append(prefix).append("]\n");
    }

    /**
     * ASN.1 DER X.509 GeneralSubtree encoder/decoder class.
     */
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            GeneralName.ASN1,
            new ASN1Implicit(0, ASN1Integer.getInstance()),
            new ASN1Implicit(1, ASN1Integer.getInstance()) }) {
        {
            setDefault(new byte[] {0}, 1);  // minimum 0
            setOptional(2);                 // maximum optional
        }

        @Override protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            int maximum = -1; // is optional maximum missing?
            if (values[2] != null) {
                maximum = ASN1Integer.toIntValue(values[2]); // no!
            }
            return new GeneralSubtree((GeneralName) values[0],
                    ASN1Integer.toIntValue(values[1]),
                    maximum);
        }

        @Override protected void getValues(Object object, Object[] values) {
            GeneralSubtree gs = (GeneralSubtree) object;
            values[0] = gs.base;
            values[1] = ASN1Integer.fromIntValue(gs.minimum);
            if (gs.maximum > -1) {
                values[2] = ASN1Integer.fromIntValue(gs.maximum);
            }
        }
    };
}
