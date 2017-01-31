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
import org.apache.harmony.security.asn1.ASN1Explicit;
import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with the DistributionPoint structure which is the part of X.509 CRL
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *  CRLDistributionPoints ::= SEQUENCE SIZE (1..MAX) OF DistributionPoint
 *
 *  DistributionPoint ::= SEQUENCE {
 *        distributionPoint       [0]     DistributionPointName OPTIONAL,
 *        reasons                 [1]     ReasonFlags OPTIONAL,
 *        cRLIssuer               [2]     GeneralNames OPTIONAL
 *  }
 *
 *  DistributionPointName ::= CHOICE {
 *        fullName                [0]     GeneralNames,
 *        nameRelativeToCRLIssuer [1]     RelativeDistinguishedName
 *  }
 *
 *  ReasonFlags ::= BIT STRING {
 *        unused                  (0),
 *        keyCompromise           (1),
 *        cACompromise            (2),
 *        affiliationChanged      (3),
 *        superseded              (4),
 *        cessationOfOperation    (5),
 *        certificateHold         (6),
 *        privilegeWithdrawn      (7),
 *        aACompromise            (8)
 *  }
 * </pre>
 */
public final class DistributionPoint {
    private final DistributionPointName distributionPoint;
    private final ReasonFlags reasons;
    private final GeneralNames cRLIssuer;

    public DistributionPoint(DistributionPointName distributionPoint,
            ReasonFlags reasons, GeneralNames cRLIssuer) {
        if ((reasons != null) && (distributionPoint == null) && (cRLIssuer == null)) {
            throw new IllegalArgumentException("DistributionPoint MUST NOT consist of only the reasons field");
        }
        this.distributionPoint = distributionPoint;
        this.reasons = reasons;
        this.cRLIssuer = cRLIssuer;
    }

    public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix);
        sb.append("Distribution Point: [\n");
        if (distributionPoint != null) {
            distributionPoint.dumpValue(sb, prefix + "  ");
        }
        if (reasons != null) {
            reasons.dumpValue(sb, prefix + "  ");
        }
        if (cRLIssuer != null) {
            sb.append(prefix);
            sb.append("  CRL Issuer: [\n");
            cRLIssuer.dumpValue(sb, prefix + "    ");
            sb.append(prefix);
            sb.append("  ]\n");
        }
        sb.append(prefix);
        sb.append("]\n");
    }

    /**
     * Custom X.509 decoder.
     */
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
                new ASN1Explicit(0, DistributionPointName.ASN1),
                new ASN1Implicit(1, ReasonFlags.ASN1),
                new ASN1Implicit(2, GeneralNames.ASN1)
            }) {
        {
            setOptional(0);
            setOptional(1);
            setOptional(2);
        }

        @Override protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;
            return new DistributionPoint((DistributionPointName) values[0],
                    (ReasonFlags) values[1], (GeneralNames) values[2]);
        }

        @Override protected void getValues(Object object, Object[] values) {
            DistributionPoint dp = (DistributionPoint) object;
            values[0] = dp.distributionPoint;
            values[1] = dp.reasons;
            values[2] = dp.cRLIssuer;
        }
    };
}
