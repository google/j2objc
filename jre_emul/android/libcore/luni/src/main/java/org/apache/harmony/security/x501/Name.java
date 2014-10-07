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
* @author Alexander V. Esin
* @version $Revision$
*/

package org.apache.harmony.security.x501;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.security.auth.x500.X500Principal;
import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1SetOf;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.asn1.DerInputStream;
import org.apache.harmony.security.x509.DNParser;


/**
 * X.501 Name
 */
public final class Name {

    /** ASN.1 DER encoding of Name */
    private volatile byte[] encoded;

    /** RFC1779 string */
    private String rfc1779String;

    /** RFC2253 string */
    private String rfc2253String;

    /** CANONICAL string */
    private String canonicalString;

    /** Collection of RDNs */
    private List<List<AttributeTypeAndValue>> rdn;

    /**
     * Creates new <code>Name</code> instance from its DER encoding
     *
     * @param encoding - ASN.1 DER encoding
     * @throws IOException - if encoding is wrong
     */
    public Name(byte[] encoding) throws IOException {
        DerInputStream in = new DerInputStream(encoding);

        if (in.getEndOffset() != encoding.length) {
            throw new IOException("Wrong content length");
        }

        ASN1.decode(in);

        this.rdn = (List<List<AttributeTypeAndValue>>) in.content;
    }

    /**
     * Creates new <code>Name</code> instance
     *
     * @param name - Name as String
     * @throws IOException - if string is wrong
     */
    public Name(String name) throws IOException {
        rdn = new DNParser(name).parse();
    }

    private Name(List<List<AttributeTypeAndValue>> rdn) {
        this.rdn = rdn;
    }

    /**
     * Returns <code>X500Principal</code> instance corresponding to this
     * <code>Name</code> instance
     *
     * @return equivalent X500Principal object
     */
    public X500Principal getX500Principal(){
        return new X500Principal(getEncoded());
    }

    /**
     * Returns Relative Distinguished Name as <code>String</code> according
     * the format requested
     *
     * @param format one of X500Principal.CANONICAL, X500Principal.RFC1779, or
     *     X500Principal.RFC2253, case insensitive
     */
    public String getName(String format) {
        //
        // check X500Principal constants first
        //
        if (X500Principal.RFC1779.equals(format)) {

            if (rfc1779String == null) {
                rfc1779String = getName0(format);
            }
            return rfc1779String;

        } else if (X500Principal.RFC2253.equals(format)) {

            if (rfc2253String == null) {
                rfc2253String = getName0(format);
            }
            return rfc2253String;

        } else if (X500Principal.CANONICAL.equals(format)) {

            if (canonicalString == null) {
                canonicalString = getName0(format);
            }
            return canonicalString;

        }
        //
        // compare ignore case
        //
        else if (X500Principal.RFC1779.equalsIgnoreCase(format)) {

            if (rfc1779String == null) {
                rfc1779String = getName0(X500Principal.RFC1779);
            }
            return rfc1779String;

        } else if (X500Principal.RFC2253.equalsIgnoreCase(format)) {

            if (rfc2253String == null) {
                rfc2253String = getName0(X500Principal.RFC2253);
            }
            return rfc2253String;

        } else if (X500Principal.CANONICAL.equalsIgnoreCase(format)) {

            if (canonicalString == null) {
                canonicalString = getName0(X500Principal.CANONICAL);
            }
            return canonicalString;

        } else {
            throw new IllegalArgumentException("Illegal format: " + format);
        }
    }

    /**
     * Returns Relative Distinguished Name as <code>String</code> according
     * the format requested, format is int value
     */
    private String getName0(String format) {
        StringBuilder name = new StringBuilder();

        // starting with the last element and moving to the first.
        for (int i = rdn.size() - 1; i >= 0; i--) {
            List<AttributeTypeAndValue> atavList = rdn.get(i);

            if (X500Principal.CANONICAL == format) {
                atavList = new ArrayList<AttributeTypeAndValue>(atavList);
                Collections.sort(atavList, new AttributeTypeAndValueComparator());
            }

            // Relative Distinguished Name to string
            Iterator<AttributeTypeAndValue> it = atavList.iterator();
            while (it.hasNext()) {
                AttributeTypeAndValue attributeTypeAndValue = it.next();
                attributeTypeAndValue.appendName(format, name);
                if (it.hasNext()) {
                    // multi-valued RDN
                    if (X500Principal.RFC1779 == format) {
                        name.append(" + ");
                    } else {
                        name.append('+');
                    }
                }
            }

            if (i != 0) {
                name.append(',');
                if (format == X500Principal.RFC1779) {
                    name.append(' ');
                }
            }
        }

        String sName = name.toString();
        if (X500Principal.CANONICAL.equals(format)) {
            sName = sName.toLowerCase(Locale.US);
        }
        return sName;
    }

    /**
     * Gets encoded form of DN
     *
     * @return return encoding, no copying is performed
     */
    public byte[] getEncoded() {
        if (encoded == null) {
            encoded = ASN1.encode(this);
        }
        return encoded;
    }

    /**
     * According to RFC 3280 (http://www.ietf.org/rfc/rfc3280.txt)
     * X.501 Name structure is defined as follows:
     *
     * Name ::= CHOICE {
     *     RDNSequence }
     *
     * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     *
     * RelativeDistinguishedName ::=
     *     SET OF AttributeTypeAndValue
     *
     */
    public static final ASN1SetOf ASN1_RDN = new ASN1SetOf(
            AttributeTypeAndValue.ASN1);

    public static final ASN1SequenceOf ASN1 = new ASN1SequenceOf(ASN1_RDN) {

        public Object getDecodedObject(BerInputStream in) {
            return new Name((List<List<AttributeTypeAndValue>>) in.content);
        }

        public Collection getValues(Object object) {
            return ((Name) object).rdn;
        }
    };
}
