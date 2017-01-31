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

import org.apache.harmony.security.asn1.ASN1BitString;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.asn1.BitString;
import org.apache.harmony.security.utils.Array;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with the X.509 CRL. Its ASN notation is as follows
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *  CertificateList  ::=  SEQUENCE  {
 *       tbsCertList          TBSCertList,
 *       signatureAlgorithm   AlgorithmIdentifier,
 *       signatureValue       BIT STRING
 *  }
 * </pre>
 */
public final class CertificateList {
    /** the value of tbsCertList field of the structure */
    private final TBSCertList tbsCertList;
    /** the value of signatureAlgorithm field of the structure */
    private final AlgorithmIdentifier signatureAlgorithm;
    /** the value of signatureValue field of the structure */
    private final byte[] signatureValue;
    /** the ASN.1 encoded form of CertList */
    private byte[] encoding;

    public CertificateList(TBSCertList tbsCertList,
                       AlgorithmIdentifier signatureAlgorithm,
                       byte[] signatureValue) {
        this.tbsCertList = tbsCertList;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signatureValue = new byte[signatureValue.length];
        System.arraycopy(signatureValue, 0, this.signatureValue, 0,
                                                    signatureValue.length);
    }

    private CertificateList(TBSCertList tbsCertList,
                       AlgorithmIdentifier signatureAlgorithm,
                       byte[] signatureValue, byte[] encoding) {
        this(tbsCertList, signatureAlgorithm, signatureValue);
        this.encoding = encoding;
    }

    /**
     * Returns the value of tbsCertList field of the structure.
     */
    public TBSCertList getTbsCertList() {
        return tbsCertList;
    }

    /**
     * Returns the value of signatureValue field of the structure.
     */
    public byte[] getSignatureValue() {
        byte[] result = new byte[signatureValue.length];
        System.arraycopy(signatureValue, 0, result, 0, signatureValue.length);
        return result;
    }

    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        tbsCertList.dumpValue(result);
        result.append("\nSignature Value:\n");
        result.append(Array.toString(signatureValue, ""));
        return result.toString();
    }

    /**
     * Returns ASN.1 encoded form of this X.509 TBSCertList value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = CertificateList.ASN1.encode(this);
        }
        return encoding;
    }

    /**
     * X.509 CertList encoder/decoder.
     */
    public static final ASN1Sequence ASN1 =
        new ASN1Sequence(new ASN1Type[]
                {TBSCertList.ASN1, AlgorithmIdentifier.ASN1,
                    ASN1BitString.getInstance()}) {

        @Override protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new CertificateList(
                    (TBSCertList) values[0],
                    (AlgorithmIdentifier) values[1],
                    ((BitString) values[2]).bytes, // FIXME keep as BitString object
                    in.getEncoded()
                    );
        }

        @Override protected void getValues(Object object, Object[] values) {
            CertificateList certificateList = (CertificateList) object;
            values[0] = certificateList.tbsCertList;
            values[1] = certificateList.signatureAlgorithm;
            values[2] = new BitString(certificateList.signatureValue, 0);
        }
    };
}
