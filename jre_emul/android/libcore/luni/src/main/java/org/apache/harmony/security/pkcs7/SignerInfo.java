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
* @author Boris Kuznetsov
* @version $Revision$
*/
package org.apache.harmony.security.pkcs7;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1SetOf;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.x501.AttributeTypeAndValue;
import org.apache.harmony.security.x501.Name;
import org.apache.harmony.security.x509.AlgorithmIdentifier;


/**
 * As defined in PKCS #7: Cryptographic Message Syntax Standard
 * (http://www.ietf.org/rfc/rfc2315.txt)
 *
 * SignerInfo ::= SEQUENCE {
 *   version Version,
 *   issuerAndSerialNumber IssuerAndSerialNumber,
 *   digestAlgorithm DigestAlgorithmIdentifier,
 *   authenticatedAttributes
 *     [0] IMPLICIT Attributes OPTIONAL,
 *   digestEncryptionAlgorithm
 *     DigestEncryptionAlgorithmIdentifier,
 *   encryptedDigest EncryptedDigest,
 *   unauthenticatedAttributes
 *     [1] IMPLICIT Attributes OPTIONAL
 *  }
 */
public final class SignerInfo {
    private final int version;
    private final X500Principal issuer;
    private final BigInteger serialNumber;
    private final AlgorithmIdentifier digestAlgorithm;
    private final AuthenticatedAttributes authenticatedAttributes;
    private final AlgorithmIdentifier digestEncryptionAlgorithm;
    private final byte[] encryptedDigest;
    private final List<?> unauthenticatedAttributes;

    private SignerInfo(int version,
            Object[] issuerAndSerialNumber,
            AlgorithmIdentifier digestAlgorithm,
            AuthenticatedAttributes authenticatedAttributes,
            AlgorithmIdentifier digestEncryptionAlgorithm,
            byte[] encryptedDigest,
            List<?> unauthenticatedAttributes) {
        this.version = version;
        this.issuer = ((Name)issuerAndSerialNumber[0]).getX500Principal();
        this.serialNumber = ASN1Integer.toBigIntegerValue(issuerAndSerialNumber[1]);
        this.digestAlgorithm = digestAlgorithm;
        this.authenticatedAttributes = authenticatedAttributes;
        this.digestEncryptionAlgorithm = digestEncryptionAlgorithm;
        this.encryptedDigest = encryptedDigest;
        this.unauthenticatedAttributes = unauthenticatedAttributes;
    }

    public X500Principal getIssuer() {
        return issuer;
    }

    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm.getAlgorithm();
    }

    public String getDigestAlgorithmName() {
        return digestAlgorithm.getAlgorithmName();
    }

    public String getDigestEncryptionAlgorithm() {
        return digestEncryptionAlgorithm.getAlgorithm();
    }

    public String getDigestEncryptionAlgorithmName() {
        return digestEncryptionAlgorithm.getAlgorithmName();
    }

    public List<AttributeTypeAndValue> getAuthenticatedAttributes() {
        if (authenticatedAttributes == null) {
            return null;
        }
        return authenticatedAttributes.getAttributes();
    }

    /**
     * Returns the non-IMPLICIT ASN.1 encoding of the "authAttrs" from this
     * SignerInfo. That is, it will return as the encoding of a generic ASN.1
     * SET.
     */
    public byte[] getEncodedAuthenticatedAttributes() {
        if (authenticatedAttributes == null) {
            return null;
        }
        return AuthenticatedAttributes.ASN1.encode(authenticatedAttributes.getAttributes());
    }

    public byte[] getEncryptedDigest() {
        return encryptedDigest;
    }


    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("-- SignerInfo:");
        res.append("\n version : ");
        res.append(version);
        res.append("\nissuerAndSerialNumber:  ");
        res.append(issuer);
        res.append("   ");
        res.append(serialNumber);
        res.append("\ndigestAlgorithm:  ");
        res.append(digestAlgorithm.toString());
        res.append("\nauthenticatedAttributes:  ");
        if (authenticatedAttributes != null) {
            res.append(authenticatedAttributes.toString());
        }
        res.append("\ndigestEncryptionAlgorithm: ");
        res.append(digestEncryptionAlgorithm.toString());
        res.append("\nunauthenticatedAttributes: ");
        if (unauthenticatedAttributes != null) {
            res.append(unauthenticatedAttributes.toString());
        }
        res.append("\n-- SignerInfo End\n");
        return res.toString();
    }


    public static final ASN1Sequence ISSUER_AND_SERIAL_NUMBER =
            new ASN1Sequence(new ASN1Type[] {
                Name.ASN1,                       // issuer
                ASN1Integer.getInstance(),       // serialNumber
            })
        {
            // method to encode
            @Override public void getValues(Object object, Object[] values) {
                Object [] issAndSerial = (Object[])object;
                values[0] = issAndSerial[0];
                values[1] = issAndSerial[1];
        }
    };

    public static final ASN1Sequence ASN1 =
        new ASN1Sequence(new ASN1Type[] {
                ASN1Integer.getInstance(),         //version
                ISSUER_AND_SERIAL_NUMBER,
                AlgorithmIdentifier.ASN1,           //digestAlgorithm
                new ASN1Implicit(0, AuthenticatedAttributes.ASN1),//authenticatedAttributes
                AlgorithmIdentifier.ASN1,            //digestEncryptionAlgorithm
                ASN1OctetString.getInstance(),       //encryptedDigest
                 new ASN1Implicit(1, new ASN1SetOf(
                         AttributeTypeAndValue.ASN1)),//unauthenticatedAttributes
                })  {
        {
            setOptional(3); // authenticatedAttributes is optional
            setOptional(6); // unauthenticatedAttributes is optional
        }

        @Override protected void getValues(Object object, Object[] values) {
            SignerInfo si = (SignerInfo) object;
            values[0] = new byte[] {(byte)si.version};
            try {
                values[1] = new Object[] { new Name(si.issuer.getName()),
                        si.serialNumber.toByteArray() };
            } catch (IOException e) {
                // The exception is never thrown, because si.issuer
                // is created using Name.getX500Principal().
                // Throw a RuntimeException just to be safe.
                throw new RuntimeException("Failed to encode issuer name", e);
            }
            values[2] = si.digestAlgorithm;
            values[3] = si.authenticatedAttributes;
            values[4] = si.digestEncryptionAlgorithm;
            values[5] = si.encryptedDigest;
            values[6] = si.unauthenticatedAttributes;
        }

        @Override protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new SignerInfo(
                        ASN1Integer.toIntValue(values[0]),
                        (Object[]) values[1],
                        (AlgorithmIdentifier) values[2],
                        (AuthenticatedAttributes) values[3],
                        (AlgorithmIdentifier) values[4],
                        (byte[]) values[5],
                        (List) values[6]
                    );
        }
   };
}
