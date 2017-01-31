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

import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with the ORAddress structure which is a part of X.509 certificate:
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 * ORAddress ::= SEQUENCE {
 *   built-in-standard-attributes BuiltInStandardAttributes,
 *   built-in-domain-defined-attributes
 *                   BuiltInDomainDefinedAttributes OPTIONAL,
 *   extension-attributes ExtensionAttributes OPTIONAL
 * }
 * </pre>
 *
 * TODO: this class needs to be finished.
 */
public final class ORAddress {

    /** the ASN.1 encoded form of ORAddress */
    private byte[] encoding;

    /**
     * Returns ASN.1 encoded form of this X.509 ORAddress value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    /**
     * ASN.1 DER X.509 ORAddress encoder/decoder class.
     */
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            new ASN1Sequence(new ASN1Type[] {}) {
                @Override protected void getValues(Object object, Object[] values) {}
            }}) {

        @Override protected Object getDecodedObject(BerInputStream in) {
            return new ORAddress();
        }

        private final Object foo = new Object();

        @Override protected void getValues(Object object, Object[] values) {
            values[0] = foo;
        }
    };
}

