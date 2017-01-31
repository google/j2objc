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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import org.apache.harmony.security.asn1.ASN1Explicit;
import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.x501.Name;


/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with TBSCertList structure which is the part of X.509 CRL
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *   TBSCertList  ::=  SEQUENCE  {
 *        version                 Version OPTIONAL,
 *                                     -- if present, MUST be v2
 *        signature               AlgorithmIdentifier,
 *        issuer                  Name,
 *        thisUpdate              Time,
 *        nextUpdate              Time OPTIONAL,
 *        revokedCertificates     SEQUENCE OF SEQUENCE  {
 *             userCertificate         CertificateSerialNumber,
 *             revocationDate          Time,
 *             crlEntryExtensions      Extensions OPTIONAL
 *                                           -- if present, MUST be v2
 *                                  }  OPTIONAL,
 *        crlExtensions           [0]  EXPLICIT Extensions OPTIONAL
 *                                           -- if present, MUST be v2
 *   }
 * </pre>
 */
public final class TBSCertList {
    /** the value of version field of the structure */
    private final int version;
    /** the value of signature field of the structure */
    private final AlgorithmIdentifier signature;
    /** the value of issuer field of the structure */
    private final Name issuer;
    /** the value of thisUpdate of the structure */
    private final Date thisUpdate;
    /** the value of nextUpdate of the structure */
    private final Date nextUpdate;
    /** the value of revokedCertificates of the structure */
    private final List<RevokedCertificate> revokedCertificates;
    /** the value of crlExtensions field of the structure */
    private final Extensions crlExtensions;
    /** the ASN.1 encoded form of TBSCertList */
    private byte[] encoding;

    public static class RevokedCertificate {
        private final BigInteger userCertificate;
        private final Date revocationDate;
        private final Extensions crlEntryExtensions;

        private boolean issuerRetrieved;
        private X500Principal issuer;
        private byte[] encoding;

        public RevokedCertificate(BigInteger userCertificate,
                Date revocationDate, Extensions crlEntryExtensions) {
            this.userCertificate = userCertificate;
            this.revocationDate = revocationDate;
            this.crlEntryExtensions = crlEntryExtensions;
        }

        public Extensions getCrlEntryExtensions() {
            return crlEntryExtensions;
        }

        public BigInteger getUserCertificate() {
            return userCertificate;
        }

        public Date getRevocationDate() {
            return revocationDate;
        }

        /**
         * Returns the value of Certificate Issuer Extension, if it is
         * presented.
         */
        public X500Principal getIssuer() {
            if (crlEntryExtensions == null) {
                return null;
            }
            if (!issuerRetrieved) {
                try {
                    issuer =
                        crlEntryExtensions.valueOfCertificateIssuerExtension();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                issuerRetrieved = true;
            }
            return issuer;
        }

        public byte[] getEncoded() {
            if (encoding == null) {
                encoding = ASN1.encode(this);
            }
            return encoding;
        }

        public boolean equals(Object rc) {
            if (!(rc instanceof RevokedCertificate)) {
                return false;
            }
            RevokedCertificate rcert = (RevokedCertificate) rc;
            return userCertificate.equals(rcert.userCertificate)
                && ((revocationDate.getTime() / 1000)
                        == (rcert.revocationDate.getTime() / 1000))
                && ((crlEntryExtensions == null)
                    ? rcert.crlEntryExtensions == null
                    : crlEntryExtensions.equals(rcert.crlEntryExtensions));
        }

        public int hashCode() {
            return userCertificate.hashCode() * 37 + (int)revocationDate.getTime() / 1000
                    + (crlEntryExtensions == null ? 0 : crlEntryExtensions.hashCode());
        }

        public void dumpValue(StringBuilder sb, String prefix) {
            sb.append(prefix).append("Certificate Serial Number: ").append(userCertificate).append('\n');
            sb.append(prefix).append("Revocation Date: ").append(revocationDate);
            if (crlEntryExtensions != null) {
                sb.append('\n').append(prefix).append("CRL Entry Extensions: [");
                crlEntryExtensions.dumpValue(sb, prefix + "  ");
                sb.append(prefix).append(']');
            }
        }

        public static final ASN1Sequence ASN1 = new ASN1Sequence(
                new ASN1Type[] {ASN1Integer.getInstance(), Time.ASN1,
                Extensions.ASN1}) {
            {
                setOptional(2);
            }

            @Override protected Object getDecodedObject(BerInputStream in) {
                Object[] values = (Object[]) in.content;
                return new RevokedCertificate(
                            new BigInteger((byte[]) values[0]),
                            (Date) values[1],
                            (Extensions) values[2]
                        );
            }

            @Override protected void getValues(Object object, Object[] values) {
                RevokedCertificate rcert = (RevokedCertificate) object;
                values[0] = rcert.userCertificate.toByteArray();
                values[1] = rcert.revocationDate;
                values[2] = rcert.crlEntryExtensions;
            }
        };
    }

    /** Constructs the object with associated ASN.1 encoding */
    private TBSCertList(int version, AlgorithmIdentifier signature,
            Name issuer, Date thisUpdate, Date nextUpdate,
            List<RevokedCertificate> revokedCertificates, Extensions crlExtensions,
            byte[] encoding) {
        this.version = version;
        this.signature = signature;
        this.issuer = issuer;
        this.thisUpdate = thisUpdate;
        this.nextUpdate = nextUpdate;
        this.revokedCertificates = revokedCertificates;
        this.crlExtensions = crlExtensions;
        this.encoding = encoding;
    }

    /**
     * Returns the value of version field of the structure.
     */
    public int getVersion() {
        return version;
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
     * Returns the value of thisUpdate field of the structure.
     */
    public Date getThisUpdate() {
        return thisUpdate;
    }

    /**
     * Returns the value of nextUpdate field of the structure.
     */
    public Date getNextUpdate() {
        return nextUpdate;
    }

    /**
     * Returns the value of revokedCertificates field of the structure.
     */
    public List<RevokedCertificate> getRevokedCertificates() {
        return revokedCertificates;
    }

    /**
     * Returns the value of crlExtensions field of the structure.
     */
    public Extensions getCrlExtensions() {
        return crlExtensions;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 TBSCertList value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof TBSCertList)) {
            return false;
        }
        TBSCertList that = (TBSCertList) other;
        return version == that.version
            && signature.equals(that.signature)
            && Arrays.equals(issuer.getEncoded(), that.issuer.getEncoded())
            && thisUpdate.getTime() / 1000
                    == that.thisUpdate.getTime() / 1000
            && (nextUpdate == null
                    ? that.nextUpdate == null
                    : nextUpdate.getTime() / 1000
                        == that.nextUpdate.getTime() / 1000)
            && ((revokedCertificates == null || that.revokedCertificates == null)
                && revokedCertificates == that.revokedCertificates
                || revokedCertificates.equals(that.revokedCertificates))
            && (crlExtensions == null
                    ? that.crlExtensions == null
                    : crlExtensions.equals(that.crlExtensions));
    }

    @Override public int hashCode() {
        return ((version * 37 + signature.hashCode()) * 37
                + Arrays.hashCode(issuer.getEncoded())) * 37
                + (int)thisUpdate.getTime() / 1000;
    }

    public void dumpValue(StringBuilder sb) {
        sb.append("X.509 CRL v").append(version);
        sb.append("\nSignature Algorithm: [");
        signature.dumpValue(sb);
        sb.append(']');
        sb.append("\nIssuer: ").append(issuer.getName(X500Principal.RFC2253));
        sb.append("\n\nThis Update: ").append(thisUpdate);
        sb.append("\nNext Update: ").append(nextUpdate).append('\n');
        if (revokedCertificates != null) {
            sb.append("\nRevoked Certificates: ").append(revokedCertificates.size()).append(" [");
            int number = 1;
            for (RevokedCertificate revokedCertificate : revokedCertificates) {
                sb.append("\n  [").append(number++).append(']');
                revokedCertificate.dumpValue(sb, "  ");
                sb.append('\n');
            }
            sb.append("]\n");
        }
        if (crlExtensions != null) {
            sb.append("\nCRL Extensions: ").append(crlExtensions.size()).append(" [");
            crlExtensions.dumpValue(sb, "  ");
            sb.append("]\n");
        }
    }

    /**
     * X.509 TBSCertList encoder/decoder.
     */
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Integer.getInstance(), // version
            AlgorithmIdentifier.ASN1,  // signature
            Name.ASN1, // issuer
            Time.ASN1, // thisUpdate
            Time.ASN1, // nextUpdate
            new ASN1SequenceOf(RevokedCertificate.ASN1), // revokedCertificates
            new ASN1Explicit(0, Extensions.ASN1) // crlExtensions
                }) {
        {
            setOptional(0);
            setOptional(4);
            setOptional(5);
            setOptional(6);
        }

        @Override protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;
            return new TBSCertList(
                        (values[0] == null)
                            ? 1
                            : ASN1Integer.toIntValue(values[0])+1,
                        (AlgorithmIdentifier) values[1],
                        (Name) values[2],
                        (Date) values[3],
                        (Date) values[4],
                        (List<RevokedCertificate>) values[5],
                        (Extensions) values[6],
                        in.getEncoded()
                    );
        }

        @Override protected void getValues(Object object, Object[] values) {
            TBSCertList tbs = (TBSCertList) object;
            values[0] = (tbs.version > 1)
                ? ASN1Integer.fromIntValue(tbs.version - 1) : null;
            values[1] = tbs.signature;
            values[2] = tbs.issuer;
            values[3] = tbs.thisUpdate;
            values[4] = tbs.nextUpdate;
            values[5] = tbs.revokedCertificates;
            values[6] = tbs.crlExtensions;
        }
    };
}
