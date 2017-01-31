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

import java.util.Date;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with Validity structure which is the part of X.509 certificate
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *  Validity ::= SEQUENCE {
 *       notBefore      Time,
 *       notAfter       Time
 *  }
 * </pre>
 */
public final class Validity {
    /** the value of notBefore field of the structure */
    private final Date notBefore;
    /** the value of notAfter field of the structure */
    private final Date notAfter;
    /** the ASN.1 encoded form of Validity */
    private byte[] encoding;

    public Validity(Date notBefore, Date notAfter) {
        this.notBefore = notBefore;
        this.notAfter = notAfter;
    }

    /**
     * Returns the value of notBefore field of the structure.
     */
    public Date getNotBefore() {
        return notBefore;
    }

    /**
     * Returns the value of notAfter field of the structure.
     */
    public Date getNotAfter() {
        return notAfter;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 Validity value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    /**
     * ASN.1 DER X.509 Validity encoder/decoder class.
     */
    public static final ASN1Sequence ASN1
        = new ASN1Sequence(new ASN1Type[] {Time.ASN1, Time.ASN1 }) {

        @Override protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new Validity((Date) values[0], (Date) values[1]);
        }

        @Override protected void getValues(Object object, Object[] values) {
            Validity validity = (Validity) object;
            values[0] = validity.notBefore;
            values[1] = validity.notAfter;
        }
    };
}
