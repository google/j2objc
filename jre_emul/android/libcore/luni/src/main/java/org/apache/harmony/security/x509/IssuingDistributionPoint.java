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
import org.apache.harmony.security.asn1.ASN1Boolean;
import org.apache.harmony.security.asn1.ASN1Explicit;
import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

/**
 * CRL's Issuing Distribution Point Extension (OID = 2.5.29.28).
 * <pre>
 *   id-ce-issuingDistributionPoint OBJECT IDENTIFIER ::= { id-ce 28 }
 *
 *   issuingDistributionPoint ::= SEQUENCE {
 *      distributionPoint          [0] DistributionPointName OPTIONAL,
 *      onlyContainsUserCerts      [1] BOOLEAN DEFAULT FALSE,
 *      onlyContainsCACerts        [2] BOOLEAN DEFAULT FALSE,
 *      onlySomeReasons            [3] ReasonFlags OPTIONAL,
 *      indirectCRL                [4] BOOLEAN DEFAULT FALSE,
 *      onlyContainsAttributeCerts [5] BOOLEAN DEFAULT FALSE
 *   }
 * </pre>
 * (as specified in RFC 3280 http://www.ietf.org/rfc/rfc3280.txt)
 */
public final class IssuingDistributionPoint extends ExtensionValue {
    // values of the fields of the structure
    private DistributionPointName distributionPoint;
    private boolean onlyContainsUserCerts = false;
    private boolean onlyContainsCACerts = false;
    private ReasonFlags onlySomeReasons;
    private boolean indirectCRL = false;
    private boolean onlyContainsAttributeCerts = false;

    /**
     * Constructs the object on the base of its distributionPoint and
     * onlySomeReasons fields values.
     */
    public IssuingDistributionPoint(DistributionPointName distributionPoint,
            ReasonFlags onlySomeReasons) {
        this.distributionPoint = distributionPoint;
        this.onlySomeReasons = onlySomeReasons;
    }

    /**
     * Creates the extension object on the base of its encoded form.
     */
    public static IssuingDistributionPoint decode(byte[] encoding)
            throws IOException {
        IssuingDistributionPoint idp =
            (IssuingDistributionPoint) ASN1.decode(encoding);
        idp.encoding = encoding;
        return idp;
    }

    /**
     * Sets the value of onlyContainsUserCerts field of the structure.
     */
    public void setOnlyContainsUserCerts(boolean onlyContainsUserCerts) {
        this.onlyContainsUserCerts = onlyContainsUserCerts;
    }

    /**
     * Sets the value of onlyContainsCACerts field of the structure.
     */
    public void setOnlyContainsCACerts(boolean onlyContainsCACerts) {
        this.onlyContainsCACerts = onlyContainsCACerts;
    }

    /**
     * Sets the value of indirectCRL field of the structure.
     */
    public void setIndirectCRL(boolean indirectCRL) {
        this.indirectCRL = indirectCRL;
    }

    /**
     * Sets the value of onlyContainsAttributeCerts field of the structure.
     */
    public void setOnlyContainsAttributeCerts(
            boolean onlyContainsAttributeCerts) {
        this.onlyContainsAttributeCerts = onlyContainsAttributeCerts;
    }

    @Override public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    @Override public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append("Issuing Distribution Point: [\n");
        if (distributionPoint != null) {
            distributionPoint.dumpValue(sb, "  " + prefix);
        }
        sb.append(prefix).append("  onlyContainsUserCerts: ").append(onlyContainsUserCerts).append('\n');
        sb.append(prefix).append("  onlyContainsCACerts: ").append(onlyContainsCACerts).append('\n');
        if (onlySomeReasons != null) {
            onlySomeReasons.dumpValue(sb, prefix + "  ");
        }
        sb.append(prefix).append("  indirectCRL: ").append(indirectCRL).append('\n');
        sb.append(prefix).append("  onlyContainsAttributeCerts: ").append(onlyContainsAttributeCerts).append('\n');
    }

    /**
     * ASN.1 Encoder/Decoder.
     */
    public static final ASN1Type ASN1 = new ASN1Sequence(
            new ASN1Type[] {
                // ASN.1 prohibits implicitly tagged CHOICE
                new ASN1Explicit(0, DistributionPointName.ASN1),
                new ASN1Implicit(1, ASN1Boolean.getInstance()),
                new ASN1Implicit(2, ASN1Boolean.getInstance()),
                new ASN1Implicit(3, ReasonFlags.ASN1),
                new ASN1Implicit(4, ASN1Boolean.getInstance()),
                new ASN1Implicit(5, ASN1Boolean.getInstance())
            }) {
        {
            setOptional(0);
            setOptional(3);
            setDefault(Boolean.FALSE, 1);
            setDefault(Boolean.FALSE, 2);
            setDefault(Boolean.FALSE, 4);
            setDefault(Boolean.FALSE, 5);
        }

        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            IssuingDistributionPoint idp = new IssuingDistributionPoint(
                    (DistributionPointName) values[0], (ReasonFlags) values[3]);
            idp.encoding = in.getEncoded();
            if (values[1] != null) {
                idp.setOnlyContainsUserCerts((Boolean) values[1]);
            }
            if (values[2] != null) {
                idp.setOnlyContainsCACerts((Boolean) values[2]);
            }
            if (values[4] != null) {
                idp.setIndirectCRL((Boolean) values[4]);
            }
            if (values[5] != null) {
                idp.setOnlyContainsAttributeCerts((Boolean) values[5]);
            }
            return idp;
        }

        protected void getValues(Object object, Object[] values) {
            IssuingDistributionPoint idp = (IssuingDistributionPoint) object;
            values[0] = idp.distributionPoint;
            values[1] = (idp.onlyContainsUserCerts) ? Boolean.TRUE : null;
            values[2] = (idp.onlyContainsCACerts) ? Boolean.TRUE : null;
            values[3] = idp.onlySomeReasons;
            values[4] = (idp.indirectCRL) ? Boolean.TRUE : null;
            values[5] = (idp.onlyContainsAttributeCerts) ? Boolean.TRUE : null;
        }
    };

}
