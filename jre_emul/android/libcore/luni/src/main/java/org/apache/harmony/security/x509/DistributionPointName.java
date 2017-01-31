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
import javax.security.auth.x500.X500Principal;
import org.apache.harmony.security.asn1.ASN1Choice;
import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.x501.Name;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with the DistributionPointName structure which is the part
 * of X.509 CRL
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
public final class DistributionPointName {
    private final GeneralNames fullName;
    private final Name nameRelativeToCRLIssuer;

    public DistributionPointName(GeneralNames fullName) {
        this.fullName = fullName;
        this.nameRelativeToCRLIssuer = null;
    }

    public DistributionPointName(Name nameRelativeToCRLIssuer) {
        this.fullName = null;
        this.nameRelativeToCRLIssuer = nameRelativeToCRLIssuer;
    }

    public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix);
        sb.append("Distribution Point Name: [\n");
        if (fullName != null) {
            fullName.dumpValue(sb, prefix + "  ");
        } else {
            sb.append(prefix);
            sb.append("  ");
            sb.append(nameRelativeToCRLIssuer.getName(X500Principal.RFC2253));
        }
        sb.append(prefix);
        sb.append("]\n");
    }

    public static final ASN1Choice ASN1 = new ASN1Choice(new ASN1Type[] {
            new ASN1Implicit(0, GeneralNames.ASN1),
            new ASN1Implicit(1, Name.ASN1_RDN) }) {

        public int getIndex(java.lang.Object object) {
            DistributionPointName dpn = (DistributionPointName) object;
            return (dpn.fullName == null) ? 1 : 0;
        }

        @Override protected Object getDecodedObject(BerInputStream in) throws IOException {
            DistributionPointName result = null;
            if (in.choiceIndex == 0) {
                result = new DistributionPointName((GeneralNames) in.content);
            } else {
                // note: ASN.1 decoder will report an error if index
                // is neither 0 or 1
                result = new DistributionPointName((Name) in.content);
            }
            return result;
        }

        public Object getObjectToEncode(Object object) {
            DistributionPointName dpn = (DistributionPointName) object;
            if (dpn.fullName == null) {
                return dpn.nameRelativeToCRLIssuer;
            } else {
                return dpn.fullName;
            }
        }
    };
}
