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

import java.math.BigInteger;
import javax.security.auth.x500.X500Principal;
import org.apache.harmony.security.asn1.ASN1BitString;
import org.apache.harmony.security.asn1.ASN1Explicit;
import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.asn1.BitString;
import org.apache.harmony.security.x501.Name;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with TBSCertificate structure which is the part of X.509 certificate
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *  TBSCertificate  ::=  SEQUENCE  {
 *       version         [0]  EXPLICIT Version DEFAULT v1,
 *       serialNumber         CertificateSerialNumber,
 *       signature            AlgorithmIdentifier,
 *       issuer               Name,
 *       validity             Validity,
 *       subject              Name,
 *       subjectPublicKeyInfo SubjectPublicKeyInfo,
 *       issuerUniqueID  [1]  IMPLICIT UniqueIdentifier OPTIONAL,
 *                            -- If present, version MUST be v2 or v3
 *       subjectUniqueID [2]  IMPLICIT UniqueIdentifier OPTIONAL,
 *                            -- If present, version MUST be v2 or v3
 *       extensions      [3]  EXPLICIT Extensions OPTIONAL
 *                            -- If present, version MUST be v3
 *  }
 * </pre>
 */
public final class TBSCertificate {

    /** the value of version field of the structure */
    private final int version;
    /** the value of serialNumber field of the structure */
    private final BigInteger serialNumber;
    /** the value of signature field of the structure */
    private final AlgorithmIdentifier signature;
    /** the value of issuer field of the structure */
    private final Name issuer;
    /** the value of validity field of the structure */
    private final Validity validity;
    /** the value of subject field of the structure */
    private final Name subject;
    /** the value of subjectPublicKeyInfo field of the structure */
    private final SubjectPublicKeyInfo subjectPublicKeyInfo;
    /** the value of issuerUniqueID field of the structure */
    private final boolean[] issuerUniqueID;
    /** the value of subjectUniqueID field of the structure */
    private final boolean[] subjectUniqueID;
    /** the value of extensions field of the structure */
    private final Extensions extensions;
    /** the ASN.1 encoded form of TBSCertificate */
    private byte[] encoding;

    public TBSCertificate(int version, BigInteger serialNumber,
                          AlgorithmIdentifier signature, Name issuer,
                          Validity validity, Name subject,
                          SubjectPublicKeyInfo subjectPublicKeyInfo,
                          boolean[] issuerUniqueID, boolean[] subjectUniqueID,
                          Extensions extensions) {
        this.version = version;
        this.serialNumber = serialNumber;
        this.signature = signature;
        this.issuer = issuer;
        this.validity = validity;
        this.subject = subject;
        this.subjectPublicKeyInfo = subjectPublicKeyInfo;
        this.issuerUniqueID = issuerUniqueID;
        this.subjectUniqueID = subjectUniqueID;
        this.extensions = extensions;
    }

    private TBSCertificate(int version, BigInteger serialNumber,
                          AlgorithmIdentifier signature, Name issuer,
                          Validity validity, Name subject,
                          SubjectPublicKeyInfo subjectPublicKeyInfo,
                          boolean[] issuerUniqueID, boolean[] subjectUniqueID,
                          Extensions extensions, byte[] encoding) {
        this(version, serialNumber, signature, issuer, validity, subject,
             subjectPublicKeyInfo, issuerUniqueID, subjectUniqueID, extensions);
        this.encoding = encoding;
    }

    /**
     * Returns the value of version field of the structure.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the value of serialNumber field of the structure.
     */
    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    /**
     * Returns the value of signature field of the structure.
     */
    public AlgorithmIdentifier getSignature() {
        return signature;
    }

    /**
     * Returns the value of issuer field of the structure.
     */
    public Name getIssuer() {
        return issuer;
    }

    /**
     * Returns the value of validity field of the structure.
     */
    public Validity getValidity() {
        return validity;
    }

    /**
     * Returns the value of subject field of the structure.
     */
    public Name getSubject() {
        return subject;
    }

    /**
     * Returns the value of subjectPublicKeyInfo field of the structure.
     */
    public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
        return subjectPublicKeyInfo;
    }

    /**
     * Returns the value of issuerUniqueID field of the structure.
     */
    public boolean[] getIssuerUniqueID() {
        return issuerUniqueID;
    }

    /**
     * Returns the value of subjectUniqueID field of the structure.
     */
    public boolean[] getSubjectUniqueID() {
        return subjectUniqueID;
    }

    /**
     * Returns the value of extensions field of the structure.
     */
    public Extensions getExtensions() {
        return extensions;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 TBSCertificate value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    public void dumpValue(StringBuilder sb) {
        sb.append('[');
        sb.append("\n  Version: V").append(version+1);
        sb.append("\n  Subject: ").append(subject.getName(X500Principal.RFC2253));
        sb.append("\n  Signature Algorithm: ");
        signature.dumpValue(sb);
        sb.append("\n  Key: ").append(subjectPublicKeyInfo.getPublicKey().toString());
        sb.append("\n  Validity: [From: ").append(validity.getNotBefore());
        sb.append("\n               To: ").append(validity.getNotAfter()).append(']');
        sb.append("\n  Issuer: ").append(issuer.getName(X500Principal.RFC2253));
        sb.append("\n  Serial Number: ").append(serialNumber);
        if (issuerUniqueID != null) {
            sb.append("\n  Issuer Id: ");
            for (boolean b : issuerUniqueID) {
                sb.append(b ? '1' : '0');
            }
        }
        if (subjectUniqueID != null) {
            sb.append("\n  Subject Id: ");
            for (boolean b : subjectUniqueID) {
                sb.append(b ? '1' : '0');
            }
        }
        if (extensions != null) {
            sb.append("\n\n  Extensions: ");
            sb.append("[\n");
            extensions.dumpValue(sb, "    ");
            sb.append("  ]");
        }
        sb.append("\n]");
    }

    /**
     * X.509 TBSCertificate encoder/decoder.
     */
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            new ASN1Explicit(0, ASN1Integer.getInstance()), ASN1Integer.getInstance(),
            AlgorithmIdentifier.ASN1, Name.ASN1,
            Validity.ASN1, Name.ASN1, SubjectPublicKeyInfo.ASN1,
            new ASN1Implicit(1, ASN1BitString.getInstance()),
            new ASN1Implicit(2, ASN1BitString.getInstance()),
            new ASN1Explicit(3, Extensions.ASN1)}) {
        {
            setDefault(new byte[] {0}, 0);
            setOptional(7);
            setOptional(8);
            setOptional(9);
        }

        @Override protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;

            boolean[] issuerUniqueID = (values[7] == null)
                ? null : ((BitString) values[7]).toBooleanArray();
            boolean[] subjectUniqueID = (values[8] == null)
                ? null : ((BitString) values[8]).toBooleanArray();
            return new TBSCertificate(
                        ASN1Integer.toIntValue(values[0]),
                        new BigInteger((byte[]) values[1]),
                        (AlgorithmIdentifier) values[2],
                        (Name) values[3],
                        (Validity) values[4],
                        (Name) values[5],
                        (SubjectPublicKeyInfo) values[6],
                        issuerUniqueID,
                        subjectUniqueID,
                        (Extensions) values[9],
                        in.getEncoded()
                    );
        }

        @Override protected void getValues(Object object, Object[] values) {
            TBSCertificate tbs = (TBSCertificate) object;
            values[0] = ASN1Integer.fromIntValue(tbs.version);
            values[1] = tbs.serialNumber.toByteArray();
            values[2] = tbs.signature;
            values[3] = tbs.issuer;
            values[4] = tbs.validity;
            values[5] = tbs.subject;
            values[6] = tbs.subjectPublicKeyInfo;
            if (tbs.issuerUniqueID != null) {
                values[7] = new BitString(tbs.issuerUniqueID);
            }
            if (tbs.subjectUniqueID != null) {
                values[8] = new BitString(tbs.subjectUniqueID);
            }
            values[9] = tbs.extensions;
        }
    };
}
