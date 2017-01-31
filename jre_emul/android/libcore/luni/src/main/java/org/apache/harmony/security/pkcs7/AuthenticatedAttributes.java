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

import java.util.List;
import org.apache.harmony.security.asn1.ASN1SetOf;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.x501.AttributeTypeAndValue;

/**
 * As defined in PKCS #7: Cryptographic Message Syntax Standard
 * (http://www.ietf.org/rfc/rfc2315.txt):
 * authenticatedAttributes is a set of attributes that are signed (i.e., authenticated) by the signer
 */
final class AuthenticatedAttributes {
    private byte[] encoding;
    private final List<AttributeTypeAndValue> authenticatedAttributes;

    private AuthenticatedAttributes(byte[] encoding,
            List<AttributeTypeAndValue> authenticatedAttributes) {
        this.encoding = encoding;
        this.authenticatedAttributes = authenticatedAttributes;
    }

    public List<AttributeTypeAndValue> getAttributes() {
        return authenticatedAttributes;
    }

    /**
     * Returns ASN.1 encoded form of this authenticatedAttributes.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    public static final ASN1SetOf ASN1 =
        new ASN1SetOf(AttributeTypeAndValue.ASN1) {
        @Override public Object getDecodedObject(BerInputStream in) {
            return new AuthenticatedAttributes(in.getEncoded(),
                    (List<AttributeTypeAndValue>) in.content);
        }
    };
}

