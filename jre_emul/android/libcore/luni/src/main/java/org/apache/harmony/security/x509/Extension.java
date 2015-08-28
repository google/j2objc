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
import java.io.OutputStream;
import java.util.Arrays;
import org.apache.harmony.security.asn1.ASN1Boolean;
import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.asn1.ASN1Oid;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.asn1.ObjectIdentifier;
import org.apache.harmony.security.utils.Array;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with the Extension part of X.509 certificate
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *  Extension  ::=  SEQUENCE  {
 *       extnID      OBJECT IDENTIFIER,
 *       critical    BOOLEAN DEFAULT FALSE,
 *       extnValue   OCTET STRING
 *  }
 * </pre>
 */
public final class Extension implements java.security.cert.Extension {
    // critical constants
    public static final boolean CRITICAL = true;
    public static final boolean NON_CRITICAL = false;

    // constants: the extension OIDs
    // certificate extensions:
    static final int[] SUBJ_DIRECTORY_ATTRS = {2, 5, 29, 9};
    static final int[] SUBJ_KEY_ID = {2, 5, 29, 14};
    static final int[] KEY_USAGE = {2, 5, 29, 15};
    static final int[] PRIVATE_KEY_USAGE_PERIOD = {2, 5, 29, 16};
    static final int[] SUBJECT_ALT_NAME = {2, 5, 29, 17};
    static final int[] ISSUER_ALTERNATIVE_NAME = {2, 5, 29, 18};
    static final int[] BASIC_CONSTRAINTS = {2, 5, 29, 19};
    static final int[] NAME_CONSTRAINTS = {2, 5, 29, 30};
    static final int[] CRL_DISTR_POINTS = {2, 5, 29, 31};
    static final int[] CERTIFICATE_POLICIES = {2, 5, 29, 32};
    static final int[] POLICY_MAPPINGS = {2, 5, 29, 33};
    static final int[] AUTH_KEY_ID = {2, 5, 29, 35};
    static final int[] POLICY_CONSTRAINTS = {2, 5, 29, 36};
    static final int[] EXTENDED_KEY_USAGE = {2, 5, 29, 37};
    static final int[] FRESHEST_CRL = {2, 5, 29, 46};
    static final int[] INHIBIT_ANY_POLICY = {2, 5, 29, 54};
    static final int[] AUTHORITY_INFO_ACCESS =
                                            {1, 3, 6, 1, 5, 5, 7, 1, 1};
    static final int[] SUBJECT_INFO_ACCESS =
                                            {1, 3, 6, 1, 5, 5, 7, 1, 11};
    // crl extensions:
    static final int[] ISSUING_DISTR_POINT = {2, 5, 29, 28};
    // crl entry extensions:
    static final int[] CRL_NUMBER = {2, 5, 29, 20};
    static final int[] CERTIFICATE_ISSUER = {2, 5, 29, 29};
    static final int[] INVALIDITY_DATE = {2, 5, 29, 24};
    static final int[] REASON_CODE = {2, 5, 29, 21};
    static final int[] ISSUING_DISTR_POINTS = {2, 5, 29, 28};

    // the value of extnID field of the structure
    private final int[] extnID;
    private String extnID_str;
    // the value of critical field of the structure
    private final boolean critical;
    // the value of extnValue field of the structure
    private final byte[] extnValue;
    // the ASN.1 encoded form of Extension
    private byte[] encoding;
    // the raw (not decoded) value of extnValue field of the structure
    private byte[] rawExtnValue;
    // the decoded extension value
    protected ExtensionValue extnValueObject;
    // tells whether extension value has been decoded or not
    private volatile boolean valueDecoded = false;

    public Extension(String extnID, boolean critical,
            ExtensionValue extnValueObject) {
        this.extnID_str = extnID;
        this.extnID = ObjectIdentifier.toIntArray(extnID);
        this.critical = critical;
        this.extnValueObject = extnValueObject;
        this.valueDecoded = true;
        this.extnValue = extnValueObject.getEncoded();
    }

    public Extension(String extnID, boolean critical, byte[] extnValue) {
        this.extnID_str = extnID;
        this.extnID = ObjectIdentifier.toIntArray(extnID);
        this.critical = critical;
        this.extnValue = extnValue;
    }

    public Extension(int[] extnID, boolean critical, byte[] extnValue) {
        this.extnID = extnID;
        this.critical = critical;
        this.extnValue = extnValue;
    }

    public Extension(String extnID, byte[] extnValue) {
        this(extnID, NON_CRITICAL, extnValue);
    }

    public Extension(int[] extnID, byte[] extnValue) {
        this(extnID, NON_CRITICAL, extnValue);
    }

    private Extension(int[] extnID, boolean critical, byte[] extnValue,
            byte[] rawExtnValue, byte[] encoding,
            ExtensionValue decodedExtValue) {
        this(extnID, critical, extnValue);
        this.rawExtnValue = rawExtnValue;
        this.encoding = encoding;
        this.extnValueObject = decodedExtValue;
        this.valueDecoded = (decodedExtValue != null);
    }

    /**
     * Returns the value of extnID field of the structure.
     */
    @Override
    public String getId() {
        if (extnID_str == null) {
            extnID_str = ObjectIdentifier.toString(extnID);
        }
        return extnID_str;
    }

    /**
     * Returns the value of critical field of the structure.
     */
    @Override
    public boolean isCritical() {
        return critical;
    }

    /**
     * Returns the value of extnValue field of the structure.
     */
    @Override
    public byte[] getValue() {
        return extnValue;
    }

    /**
     * Returns the raw (undecoded octet string) value of extnValue
     * field of the structure.
     */
    public byte[] getRawExtnValue() {
        if (rawExtnValue == null) {
            rawExtnValue = ASN1OctetString.getInstance().encode(extnValue);
        }
        return rawExtnValue;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 Extension value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = Extension.ASN1.encode(this);
        }
        return encoding;
    }

    @Override
    public void encode(OutputStream out) throws IOException {
        out.write(getEncoded());
    }

    @Override public boolean equals(Object ext) {
        if (!(ext instanceof Extension)) {
            return false;
        }
        Extension extension = (Extension) ext;
        return Arrays.equals(extnID, extension.extnID)
            && (critical == extension.critical)
            && Arrays.equals(extnValue, extension.extnValue);
    }

    @Override public int hashCode() {
        return (Arrays.hashCode(extnID) * 37 + (critical ? 1 : 0)) * 37 + Arrays.hashCode(extnValue);
    }

    public ExtensionValue getDecodedExtensionValue() throws IOException {
        if (!valueDecoded) {
            decodeExtensionValue();
        }
        return extnValueObject;
    }

    public KeyUsage getKeyUsageValue() {
        if (!valueDecoded) {
            try {
                decodeExtensionValue();
            } catch (IOException ignored) {
            }
        }
        if (extnValueObject instanceof KeyUsage) {
            return (KeyUsage) extnValueObject;
        } else {
            return null;
        }
    }

    public BasicConstraints getBasicConstraintsValue() {
        if (!valueDecoded) {
            try {
                decodeExtensionValue();
            } catch (IOException ignored) {
            }
        }
        if (extnValueObject instanceof BasicConstraints) {
            return (BasicConstraints) extnValueObject;
        } else {
            return null;
        }
    }

    private void decodeExtensionValue() throws IOException {
        if (valueDecoded) {
            return;
        }
        if (Arrays.equals(extnID, SUBJ_KEY_ID)) {
            extnValueObject = SubjectKeyIdentifier.decode(extnValue);
        } else if (Arrays.equals(extnID, KEY_USAGE)) {
            extnValueObject = new KeyUsage(extnValue);
        } else if (Arrays.equals(extnID, SUBJECT_ALT_NAME)) {
            extnValueObject = new AlternativeName(
                    AlternativeName.SUBJECT, extnValue);
        } else if (Arrays.equals(extnID, ISSUER_ALTERNATIVE_NAME)) {
            extnValueObject = new AlternativeName(
                    AlternativeName.SUBJECT, extnValue);
        } else if (Arrays.equals(extnID, BASIC_CONSTRAINTS)) {
            extnValueObject = new BasicConstraints(extnValue);
        } else if (Arrays.equals(extnID, NAME_CONSTRAINTS)) {
            extnValueObject = NameConstraints.decode(extnValue);
        } else if (Arrays.equals(extnID, CERTIFICATE_POLICIES)) {
            extnValueObject = CertificatePolicies.decode(extnValue);
        } else if (Arrays.equals(extnID, AUTH_KEY_ID)) {
            extnValueObject = AuthorityKeyIdentifier.decode(extnValue);
        } else if (Arrays.equals(extnID, POLICY_CONSTRAINTS)) {
            extnValueObject = new PolicyConstraints(extnValue);
        } else if (Arrays.equals(extnID, EXTENDED_KEY_USAGE)) {
            extnValueObject = new ExtendedKeyUsage(extnValue);
        } else if (Arrays.equals(extnID, INHIBIT_ANY_POLICY)) {
            extnValueObject = new InhibitAnyPolicy(extnValue);
        } else if (Arrays.equals(extnID, CERTIFICATE_ISSUER)) {
            extnValueObject = new CertificateIssuer(extnValue);
        } else if (Arrays.equals(extnID, CRL_DISTR_POINTS)) {
            extnValueObject = CRLDistributionPoints.decode(extnValue);
        } else if (Arrays.equals(extnID, CERTIFICATE_ISSUER)) {
            extnValueObject = new ReasonCode(extnValue);
        } else if (Arrays.equals(extnID, INVALIDITY_DATE)) {
            extnValueObject = new InvalidityDate(extnValue);
        } else if (Arrays.equals(extnID, REASON_CODE)) {
            extnValueObject = new ReasonCode(extnValue);
        } else if (Arrays.equals(extnID, CRL_NUMBER)) {
            extnValueObject = new CRLNumber(extnValue);
        } else if (Arrays.equals(extnID, ISSUING_DISTR_POINTS)) {
            extnValueObject = IssuingDistributionPoint.decode(extnValue);
        } else if (Arrays.equals(extnID, AUTHORITY_INFO_ACCESS)) {
            extnValueObject = InfoAccessSyntax.decode(extnValue);
        } else if (Arrays.equals(extnID, SUBJECT_INFO_ACCESS)) {
            extnValueObject = InfoAccessSyntax.decode(extnValue);
        }
        valueDecoded = true;
    }

    public void dumpValue(StringBuilder sb, String prefix) {
        sb.append("OID: ").append(getId()).append(", Critical: ").append(critical).append('\n');
        if (!valueDecoded) {
            try {
                decodeExtensionValue();
            } catch (IOException ignored) {
            }
        }
        if (extnValueObject != null) {
            extnValueObject.dumpValue(sb, prefix);
            return;
        }
        // else: dump unparsed hex representation
        sb.append(prefix);
        if (Arrays.equals(extnID, SUBJ_DIRECTORY_ATTRS)) {
            sb.append("Subject Directory Attributes Extension");
        } else if (Arrays.equals(extnID, SUBJ_KEY_ID)) {
            sb.append("Subject Key Identifier Extension");
        } else if (Arrays.equals(extnID, KEY_USAGE)) {
            sb.append("Key Usage Extension");
        } else if (Arrays.equals(extnID, PRIVATE_KEY_USAGE_PERIOD)) {
            sb.append("Private Key Usage Period Extension");
        } else if (Arrays.equals(extnID, SUBJECT_ALT_NAME)) {
            sb.append("Subject Alternative Name Extension");
        } else if (Arrays.equals(extnID, ISSUER_ALTERNATIVE_NAME)) {
            sb.append("Issuer Alternative Name Extension");
        } else if (Arrays.equals(extnID, BASIC_CONSTRAINTS)) {
            sb.append("Basic Constraints Extension");
        } else if (Arrays.equals(extnID, NAME_CONSTRAINTS)) {
            sb.append("Name Constraints Extension");
        } else if (Arrays.equals(extnID, CRL_DISTR_POINTS)) {
            sb.append("CRL Distribution Points Extension");
        } else if (Arrays.equals(extnID, CERTIFICATE_POLICIES)) {
            sb.append("Certificate Policies Extension");
        } else if (Arrays.equals(extnID, POLICY_MAPPINGS)) {
            sb.append("Policy Mappings Extension");
        } else if (Arrays.equals(extnID, AUTH_KEY_ID)) {
            sb.append("Authority Key Identifier Extension");
        } else if (Arrays.equals(extnID, POLICY_CONSTRAINTS)) {
            sb.append("Policy Constraints Extension");
        } else if (Arrays.equals(extnID, EXTENDED_KEY_USAGE)) {
            sb.append("Extended Key Usage Extension");
        } else if (Arrays.equals(extnID, INHIBIT_ANY_POLICY)) {
            sb.append("Inhibit Any-Policy Extension");
        } else if (Arrays.equals(extnID, AUTHORITY_INFO_ACCESS)) {
            sb.append("Authority Information Access Extension");
        } else if (Arrays.equals(extnID, SUBJECT_INFO_ACCESS)) {
            sb.append("Subject Information Access Extension");
        } else if (Arrays.equals(extnID, INVALIDITY_DATE)) {
            sb.append("Invalidity Date Extension");
        } else if (Arrays.equals(extnID, CRL_NUMBER)) {
            sb.append("CRL Number Extension");
        } else if (Arrays.equals(extnID, REASON_CODE)) {
            sb.append("Reason Code Extension");
        } else {
            sb.append("Unknown Extension");
        }
        sb.append('\n').append(prefix).append("Unparsed Extension Value:\n");
        sb.append(Array.toString(extnValue, prefix));
    }


    /**
     * X.509 Extension encoder/decoder.
     */
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Oid.getInstance(),
            ASN1Boolean.getInstance(),
            new ASN1OctetString() {
                @Override public Object getDecodedObject(BerInputStream in) throws IOException {
                    // first - decoded octet string,
                    // second - raw encoding of octet string
                    return new Object[]
                        {super.getDecodedObject(in), in.getEncoded()};
                }
            }
        }) {
        {
            setDefault(Boolean.FALSE, 1);
        }

        @Override protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;

            int[] oid = (int[]) values[0];
            byte[] extnValue = (byte[]) ((Object[]) values[2])[0];
            byte[] rawExtnValue = (byte[]) ((Object[]) values[2])[1];

            ExtensionValue decodedExtValue = null;
            // decode Key Usage and Basic Constraints extension values
            if (Arrays.equals(oid, KEY_USAGE)) {
                decodedExtValue = new KeyUsage(extnValue);
            } else if (Arrays.equals(oid, BASIC_CONSTRAINTS)) {
                decodedExtValue = new BasicConstraints(extnValue);
            }

            return new Extension((int[]) values[0], (Boolean) values[1],
                    extnValue, rawExtnValue, in.getEncoded(), decodedExtValue);
        }

        @Override protected void getValues(Object object, Object[] values) {
            Extension ext = (Extension) object;
            values[0] = ext.extnID;
            values[1] = (ext.critical) ? Boolean.TRUE : Boolean.FALSE;
            values[2] = ext.extnValue;
        }
    };
}
