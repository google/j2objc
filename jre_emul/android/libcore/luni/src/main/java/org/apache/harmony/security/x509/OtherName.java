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

import org.apache.harmony.security.asn1.ASN1Any;
import org.apache.harmony.security.asn1.ASN1Explicit;
import org.apache.harmony.security.asn1.ASN1Oid;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.asn1.ObjectIdentifier;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with OtherName structure which is a subpart of GeneralName
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *   OtherName ::= SEQUENCE {
 *        type-id    OBJECT IDENTIFIER,
 *        value      [0] EXPLICIT ANY DEFINED BY type-id
 *   }
 * </pre>
 */
public final class OtherName {
    /** the value of typeID field of the structure */
    private String typeID;
    /** the value of value field of the structure */
    private byte[] value;
    /** the ASN.1 encoded form of OtherName */
    private byte[] encoding;

    public OtherName(String typeID, byte[] value) {
        this(typeID, value, null);
    }

    private OtherName(String typeID, byte[] value, byte[] encoding) {
        this.typeID = typeID;
        this.value = value;
        this.encoding = encoding;
    }

    /**
     * Returns the value of value field of the structure.
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 OtherName value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    /**
     * ASN.1 DER X.509 OtherName encoder/decoder class.
     */
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Oid.getInstance(),
            new ASN1Explicit(0, ASN1Any.getInstance()) }) {

        @Override protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new OtherName(ObjectIdentifier.toString((int[]) values[0]),
                    (byte[]) values[1], in.getEncoded());
        }

        @Override protected void getValues(Object object, Object[] values) {
            OtherName on = (OtherName) object;
            values[0] = ObjectIdentifier.toIntArray(on.typeID);
            values[1] = on.value;
        }
    };
}

