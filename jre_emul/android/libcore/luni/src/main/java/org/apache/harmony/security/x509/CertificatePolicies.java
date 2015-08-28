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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with Certificate Policies structure which is a part of X.509 certificate
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *   certificatePolicies ::= SEQUENCE SIZE (1..MAX) OF PolicyInformation
 * </pre>
 */
public final class CertificatePolicies extends ExtensionValue {
    /** the values of policyInformation field of the structure */
    private List<PolicyInformation> policyInformations;
    /** the ASN.1 encoded form of CertificatePolicies */
    private byte[] encoding;

    /**
     * Constructs an object representing the value of CertificatePolicies.
     */
    public CertificatePolicies() {}

    public static CertificatePolicies decode(byte[] encoding) throws IOException {
        CertificatePolicies cps = ((CertificatePolicies) ASN1.decode(encoding));
        cps.encoding = encoding;
        return cps;
    }

    private CertificatePolicies(List<PolicyInformation> policyInformations, byte[] encoding) {
        this.policyInformations = policyInformations;
        this.encoding = encoding;
    }

    /**
     * Returns the values of policyInformation field of the structure.
     */
    public List<PolicyInformation> getPolicyInformations() {
        return new ArrayList<PolicyInformation>(policyInformations);
    }

    public CertificatePolicies addPolicyInformation(PolicyInformation policyInformation) {
        encoding = null;
        if (policyInformations == null) {
            policyInformations = new ArrayList<PolicyInformation>();
        }
        policyInformations.add(policyInformation);
        return this;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 CertificatePolicies value.
     */
    @Override public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    @Override public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append("CertificatePolicies [\n");
        for (PolicyInformation policyInformation : policyInformations) {
            sb.append(prefix);
            sb.append("  ");
            policyInformation.dumpValue(sb);
            sb.append('\n');
        }
        sb.append(prefix).append("]\n");
    }

    /**
     * ASN.1 DER X.509 CertificatePolicies encoder/decoder class.
     */
    public static final ASN1Type ASN1 = new ASN1SequenceOf(PolicyInformation.ASN1) {
        @Override public Object getDecodedObject(BerInputStream in) {
            return new CertificatePolicies((List<PolicyInformation>) in.content, in.getEncoded());
        }

        @Override public Collection getValues(Object object) {
            CertificatePolicies cps = (CertificatePolicies) object;
            return cps.policyInformations;
        }
    };
}
